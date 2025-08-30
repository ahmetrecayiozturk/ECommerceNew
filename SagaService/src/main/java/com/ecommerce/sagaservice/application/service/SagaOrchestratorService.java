package com.ecommerce.sagaservice.application.service;

import com.ecommerce.sagaservice.application.saga.SagaStepHandler;
import com.ecommerce.sagaservice.domain.events.*;
import com.ecommerce.sagaservice.domain.aggregate.SagaState;
import com.ecommerce.sagaservice.infrastructure.repository.SagaStateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SagaOrchestratorService {
    private final SagaStepHandler sagaStepHandler;
    private final ObjectMapper objectMapper;
    private final SagaStateRepository sagaStateRepository;

    public SagaOrchestratorService(SagaStepHandler sagaStepHandler, ObjectMapper objectMapper, SagaStateRepository sagaStateRepository) {
        this.sagaStepHandler = sagaStepHandler;
        this.objectMapper = objectMapper;
        this.sagaStateRepository = sagaStateRepository;
    }

    //burada order olutuurlduktan sonra order command yayınlancak bunu alan payment service bunu işleyip payment events yayınlayacak
    @KafkaListener(topics = "order-events", groupId = "saga-orchestrator")
    public void handleOrderEvent(String payload){
        try{
            //kontrol için log ekleyelim
            System.out.println("Received order event: " + payload);
            //önce gelen eventi deserialize et
            OrderEvent event = objectMapper.readValue(payload, OrderEvent.class);
            //şimdi bununla bir sagaeventi oluşturacağız, bu evente gerekli yerlerini event'ten gelen bilgiler ile dolduracağız
            SagaState sagaState = new SagaState();
            sagaState.setOrderId(event.getOrderId());
            sagaState.setUserId(event.getUserId());
            sagaState.setProductId(event.getProductId());
            sagaState.setQuantity(event.getQuantity());
            sagaState.setCurrentStep("ORDER_CREATED");
            sagaState.setStatus("IN_PROGRESS");
            //payload olarak da objectMapper ile string hale getirdiğimiz eventi koyalım
            sagaState.setPayload(payload);
            //şimdi sagaState'yi kaydedelim
            sagaStateRepository.save(sagaState);
            //şimdi de paymentten sonraki adıma geçelim
            sagaStepHandler.handlePaymentStep(sagaState, payload);
            //kontrol için log ekleyelim
            System.out.println("SagaState created by order and saved: " + sagaState);
        }
        catch(Exception e){
            System.err.println("Error handling order event: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    //payment eventi yakalayan bu method eğer payment başarılı ise bir sonraki adım için payment commands yayınlayacak, bunu alan payment service product eventsi yayınlayacak
    @KafkaListener(topics = "payment-events", groupId = "saga-orchestrator")
    public void handlePaymentEvent(String payload){
        try{
            System.out.println("Received payment event: " + payload);
            //önce gelen eventi deserialize et
            PaymentEvent paymentEvent = objectMapper.readValue(payload, PaymentEvent.class);
            System.out.println(paymentEvent.isSuccess());
            //şimdi sagaState'yi bulalım orderId ile
            System.out.println("PaymentEvent found: " + paymentEvent);
            //Şimdi bu sagaState'yi payment olmuşsa payment completed yapalım current stepini
            SagaState sagaState = sagaStateRepository.findByOrderId(paymentEvent.getOrderId());
            //SagaState'nin currentStep'ini güncelleyelim
            if(paymentEvent.isSuccess()){
                sagaState.setCurrentStep("PAYMENT_COMPLETED");
                sagaStateRepository.save(sagaState);
                sagaStepHandler.handleProductStep(sagaState, payload);
            }
            else{
                System.out.println("Payment failed for orderId: " + paymentEvent.getOrderId());
                //eğer payment başarısız olduysa, compensating işlemi yapalım
                sagaStepHandler.compensate(sagaState, payload);
            }
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    //product eventini yakalayan bu method eğer product rezervasyonu başarılı ise bir sonraki adım için product commands yayınlayacak, bunu alan product service delivery eventsi yayınlayacak
    @KafkaListener(topics = "product-events", groupId = "saga-orchestrator")
    public void handleProductEvent(String payload){
        System.out.println( "handleProductEvent çalıştı");
        try{
            System.out.println("Received product event: " + payload);
            //önce eventi bulalım ve deserialize edelim
            ProductEvent productEvent = objectMapper.readValue(payload, ProductEvent.class);
            //Şimdi de sagaState'yi bulalım orderId ile
            SagaState sagaState = sagaStateRepository.findByOrderId(productEvent.getOrderId());
            //Şimdi ise handle edelim eğer success ise
            if(productEvent.isSuccess()){
                //SagaState'nin currentStep'ini güncelleyelim ve prodcut ayrıldı olarak değiştirelim
                sagaState.setCurrentStep("PRODUCT_RESERVED");
                sagaStateRepository.save(sagaState);
                sagaStepHandler.handleDeliveryStep(sagaState, payload);
            }
            else{
                System.out.println("Product reservation failed for orderId: " + productEvent.getOrderId());
                //eğer product rezervasyonu başarısız olduysa, compensating işlemi yapalım
                sagaStepHandler.compensate(sagaState, payload);
            }
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    //delivery eventini yakalayan bu method eğer delivery başarılı ise sagaState'i tamamlayacak, eğer delivery başarısız ise compensating işlemi yapacak,
    //bu compensating işlemi tüm şeylerde methodlarda zaten olan bir adım
    @KafkaListener(topics = "delivery-events", groupId = "saga-orchestrator")
    public void handleDeliveryEvent(String payload){
        try{
            System.out.println("Received delivery event: " + payload);
            //gelen eventi deserialize edelim
            DeliveryEvent deliveryEvent = objectMapper.readValue(payload, DeliveryEvent.class);
            //saga stateyi bulalım bununla event atcaz şimdi
            SagaState sagaState = sagaStateRepository.findByOrderId(deliveryEvent.getOrderId());
            if(deliveryEvent.isSuccess()){
                System.out.println("Delivery successful for orderId: " + deliveryEvent.getOrderId());
                sagaState.setStatus("COMPLETED");
                sagaStateRepository.save(sagaState);
            }
            else{
                System.out.println("Delivery failed for orderId: " + deliveryEvent.getOrderId());
                sagaStepHandler.compensate(sagaState, payload);
            }
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
/*
    /*

    //compensation testi için bu methodu kullanıyorum
    @KafkaListener(topics = "delivery-events", groupId = "saga-orchestrator")
    public void handleDeliveryEvent(String payload){
        try{
            System.out.println(">>> handleDeliveryEvent ÇALIŞTI <<<");
            DeliveryEvent deliveryEvent = objectMapper.readValue(payload, DeliveryEvent.class);
            SagaState sagaState = sagaStateRepository.findByOrderId(deliveryEvent.getOrderId());
            System.out.println(">>> handleDeliveryEvent: sagaState=" + sagaState);
            sagaStepHandler.compensate(sagaState, payload);
            System.out.println(">>> compensate çağrıldı");
        } catch(Exception e){
            System.out.println("handleDeliveryEvent exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
     */
