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

    //burada order oluşturulma eventi yakalandıktan sonra order command yayınlancak bunu alan payment service bunu işleyip payment events yayınlayacak, eğer hata çıkarsa compansate işlemi yapılacak
    @KafkaListener(topics = "order-events", groupId = "saga-orchestrator")
    public void handleOrderEvent(String payload){
        try{
            //kontrol için log ekleyelim
            System.out.println("Received order event: " + payload);
            //gelen eventi deserialize edelim
            OrderEvent event = objectMapper.readValue(payload, OrderEvent.class);
            //önceden böyle bir sagastate var mıymış onu kontrol ediyoruz var ise ve eğer bir sonraki adıma geçmiş ise sagastate idempotency kuralı yüzünden tekrar işlemiyoruz
            SagaState existingSagaState = sagaStateRepository.findByOrderId(event.getOrderId());
            //eğer varsa ve bir sonraki adımda ise sagastate bunu işlemeyeceğiz
            if (existingSagaState != null) {
                if (
                                "PAYMENT".equals(existingSagaState.getCurrentStep())   ||
                                "PRODUCT".equals(existingSagaState.getCurrentStep())   ||
                                "DELIVERY".equals(existingSagaState.getCurrentStep())  ||
                                "COMPENSATED".equals(existingSagaState.getStatus())    ||
                                "COMPLETED".equals(existingSagaState.getStatus())
                ) {
                    System.out.println("Order event already processed " + event.getOrderId());
                    return;
                }
            }
            //Şimdi bir sagastate oluşturalım, normalde burada ilk kez oluşturuluyor sagastate sonraki adımlarda bu oluşturulan sagastate güncelleniyor, eğer hata çıkarsa compansate işlemi yapılacak
            SagaState sagaState = new SagaState();
            sagaState.setOrderId(event.getOrderId());
            sagaState.setUserId(event.getUserId());
            sagaState.setProductId(event.getProductId());
            sagaState.setQuantity(event.getQuantity());
            sagaState.setCurrentStep("ORDER");
            sagaState.setStatus("IN_PROGRESS");
            sagaState.setPayload(payload);

            sagaStateRepository.save(sagaState);
            sagaStepHandler.handlePaymentStep(sagaState, payload);
            System.out.println("SagaState created by order and saved: " + sagaState);
        }
        catch(Exception e){
            System.err.println("Error handling order event: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    //burada payment eventi yakalandıktan sonra payment command yayınlancak bunu alan product service bunu işleyip product events yayınlayacak, eğer hata çıkarsa compansate işlemi yapılacak

    @KafkaListener(topics = "payment-events", groupId = "saga-orchestrator")
    public void handlePaymentEvent(String payload){
        try{
            System.out.println("Received payment event: " + payload);
            //önce gelen eventi deserialize edelim
            PaymentEvent paymentEvent = objectMapper.readValue(payload, PaymentEvent.class);
            //şimdi sagaState'yi bulalım orderId ile
            SagaState sagaState = sagaStateRepository.findByOrderId(paymentEvent.getOrderId());
            //eğer sagaState null ise bu orderId ile bir sagaState yok demektir
            if (sagaState == null) {
                System.out.println("SagaState not found for orderId: " + paymentEvent.getOrderId());
                return;
            }
            //idempotency kontrolü yapıyoruz, eğer bir sonraki adıma geçmiş ise bu event bir daha işlenmeyecektir
            if (
                            "PRODUCT".equals(sagaState.getCurrentStep()) ||
                            "DELIVERY".equals(sagaState.getCurrentStep()) ||
                            "COMPENSATED".equals(sagaState.getStatus()) ||
                            "COMPLETED".equals(sagaState.getStatus())
            ) {
                System.out.println("Payment event already processed for orderId: " + paymentEvent.getOrderId());
                return;
            }
            //Eğer ödeme başarılı ise şu anki adım olarak payment yapalım
            if(paymentEvent.isSuccess()){
                sagaState.setCurrentStep("PAYMENT");
                sagaStateRepository.save(sagaState);
                sagaStepHandler.handleProductStep(sagaState, payload);
            }
            //Eğer ödeme başarılı değilse log basalım ve compansate işlemi yapalım
            else{
                System.out.println("Payment failed for orderId: " + paymentEvent.getOrderId());
                sagaStepHandler.compensate(sagaState, payload);
            }
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    //burada product eventi yakalandıktan sonra product command yayınlancak bunu alan delivery service bunu işleyip delivery events yayınlayacak, eğer hata çıkarsa compansate işlemi yapılacak

    @KafkaListener(topics = "product-events", groupId = "saga-orchestrator")
    public void handleProductEvent(String payload){
        System.out.println("handleProductEvent çalıştı");
        try{
            System.out.println("Received product event: " + payload);
            //önce gelen eventi deserialize edelim
            ProductEvent productEvent = objectMapper.readValue(payload, ProductEvent.class);
            //şimdi sagaState'yi bulalım orderId ile
            SagaState sagaState = sagaStateRepository.findByOrderId(productEvent.getOrderId());
            //eğer sagastate bulunmazsa log basalım
            if (sagaState == null) {
                System.out.println("SagaState not found for orderId: " + productEvent.getOrderId());
                return;
            }
            //idempotency kontrolü yapıyoruz, eğer bir sonraki adıma geçmiş ise bu event bir daha işlenmeyecektir
            if (
                            "DELIVERY".equals(sagaState.getCurrentStep()) ||
                            "COMPENSATED".equals(sagaState.getStatus()) ||
                            "COMPLETED".equals(sagaState.getStatus())
            ) {
                System.out.println("Product event already processed for orderId: " + productEvent.getOrderId());
                return;
            }
            //Eğer ürün rezervasyonu başarılı ise şu anki adım olarak product yapalım
            if(productEvent.isSuccess()){
                sagaState.setCurrentStep("PRODUCT");
                sagaStateRepository.save(sagaState);
                sagaStepHandler.handleDeliveryStep(sagaState, payload);
            }
            //Eğer ürün rezervasyonu başarılı değilse log basalım ve compansate işlemi yapalım
            else{
                System.out.println("Product reservation failed for orderId: " + productEvent.getOrderId());
                sagaStepHandler.compensate(sagaState, payload);
            }
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    //burada delivery eventi yakalandıktan sonra işlem tamamlanmış olacak, eğer hata çıkarsa compansate işlemi yapılacak
    @KafkaListener(topics = "delivery-events", groupId = "saga-orchestrator")
    public void handleDeliveryEvent(String payload){
        try{
            System.out.println("Received delivery event: " + payload);
            //gelen eventi deserialize edelim
            DeliveryEvent deliveryEvent = objectMapper.readValue(payload, DeliveryEvent.class);
            //şimdi sagaState'yi bulalım orderId ile
            SagaState sagaState = sagaStateRepository.findByOrderId(deliveryEvent.getOrderId());
            //eğer sagastate bulunmazsa log basalım
            if (sagaState == null) {
                System.out.println("SagaState not found for orderId: " + deliveryEvent.getOrderId());
                return;
            }
            //idempotency kontrolü yapıyoruz, eğer bir sonraki adıma geçmiş ise bu event bir daha işlenmeyecektir
            if (
                            "COMPENSATED".equals(sagaState.getStatus()) ||
                            "COMPLETED".equals(sagaState.getStatus())
            ) {
                System.out.println("Delivery event already processed for orderId: " + deliveryEvent.getOrderId());
                return;
            }
            //Eğer delivery başarılı ise sagastatein durumunu completed olarak işaretleyelim
            if(deliveryEvent.isSuccess()){
                System.out.println("Delivery successful for orderId: " + deliveryEvent.getOrderId());
                sagaState.setStatus("COMPLETED");
                sagaStateRepository.save(sagaState);
            }
            //Eğer delivey başarılı değilse log basalım ve compansate işlemi yapalım
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