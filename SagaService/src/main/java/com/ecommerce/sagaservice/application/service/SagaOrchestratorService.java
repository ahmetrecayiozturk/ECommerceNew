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

    @KafkaListener(topics = "order-events", groupId = "saga-orchestrator")
    public void handleOrderEvent(String payload){
        try{
            System.out.println("Received order event: " + payload);
            //önce gelen eventi deserialize et
            OrderEvent event = objectMapper.readValue(payload, OrderEvent.class);
            //şimdi bununla bir sagaeventi oluşturacağız22222
            SagaState sagaState = new SagaState();
            sagaState.setOrderId(event.getOrderId());
            sagaState.setCurrentStep("ORDER_CREATED");
            sagaState.setStatus("IN_PROGRESS"); // veya başka bir kısa değer
            sagaState.setPayload(payload);            //Şimdi bunu kaydedelim
            sagaStateRepository.save(sagaState);
            sagaStepHandler.handlePaymentStep(sagaState, payload);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    @KafkaListener(topics = "payment-events", groupId = "saga-orchestrator")
    public void handlePaymentEvent(String payload){
        try{
            System.out.println("Received payment event: " + payload);
            //önce gelen eventi deserialize et
            PaymentEvent paymentEvent = objectMapper.readValue(payload, PaymentEvent.class);
            //şimdi sagaState'yi bulalım orderId ile
            SagaState sagaState = sagaStateRepository.findByOrderId(paymentEvent.getOrderId());
            System.out.println("PaymentEvent found: " + paymentEvent);
            System.out.println(paymentEvent.isSuccess());
            //Şimdi bu sagaState'yi deliveryi yapalım, tabi eğer payment success olmuşsa
            if(paymentEvent.isSuccess()){
                sagaStepHandler.handleProductStep(sagaState, payload);
            }
            else{
                //eğer payment başarısız olduysa, compensating işlemi yapalım
                sagaStepHandler.compensate(sagaState, payload);
            }
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    @KafkaListener(topics = "product-events", groupId = "saga-orchestrator")
    public void handleProductEvent(String payload){
        try{
            System.out.println("Received product event: " + payload);
            //önce eventi bulalım ve deserialize edelim
            ProductEvent productEvent = objectMapper.readValue(payload, ProductEvent.class);
            //Şimdi de sagaState'yi bulalım orderId ile
            SagaState sagaState = sagaStateRepository.findByOrderId(productEvent.getOrderId());
            //Şimdi ise handle edelim eğer success ise
            if(productEvent.isSuccess()){
                sagaStepHandler.handleDeliveryStep(sagaState, payload);
            }
            else{
                //eğer product rezervasyonu başarısız olduysa, compensating işlemi yapalım
                sagaStepHandler.compensate(sagaState, payload);
            }
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    @KafkaListener(topics = "delivery-events", groupId = "saga-orchestrator")
    public void handleDeliveryEvent(String payload){
        try{
            System.out.println("Received delivery event: " + payload);
            //önce eventi deserialize edelim
            DeliveryEvent deliveryEvent = objectMapper.readValue(payload, DeliveryEvent.class);
            //Şimdi de sagaState'yi bulalım orderId ile
            SagaState sagaState = sagaStateRepository.findByOrderId(deliveryEvent.getOrderId());
            //Eğer delivery success ise, sagaState'i tamamlayalım
            if(deliveryEvent.isSuccess()){
                sagaState.setStatus("COMPLETED");
                sagaStateRepository.save(sagaState);
            }
            else{
                //eğer delivery başarısız olduysa, compensating işlemi yapalım
                sagaStepHandler.compensate(sagaState, payload);
            }
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
/*
    @KafkaListener(topics = "delivery-events", groupId = "saga-orchestrator")
    public void handleDeliveryEvent(String payload){
        try{
            DeliveryEvent deliveryEvent = objectMapper.readValue(payload, DeliveryEvent.class);
            SagaState sagaState = sagaStateRepository.findByOrderId(deliveryEvent.getOrderId());
            if(deliveryEvent.isSuccess()){
                //burada yeni bir event publish etmeyeceğiz çünkü zaten zincirin son halkası burası o yüzden tamamlamlandı olarak belirtip bitiriyoruz işlemi
                sagaState.setStatus("COMPLETED");
                sagaStateRepository.save(sagaState)
            }
            else{
                //eğer delivery başarısız olduysa, compensating işlemi yapalım
                sagaStepHandler.compensate(sagaState, payload);
            }
            sagaStateRepository.save(sagaState);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
 */