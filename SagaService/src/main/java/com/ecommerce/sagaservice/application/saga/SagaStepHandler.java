package com.ecommerce.sagaservice.application.saga;

import com.ecommerce.sagaservice.domain.aggregate.SagaState;
import com.ecommerce.sagaservice.infrastructure.repository.SagaStateRepository;
import com.ecommerce.sagaservice.infrastructure.messaging.KafkaEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class SagaStepHandler {
    private final SagaStateRepository sagaStateRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    public SagaStepHandler(SagaStateRepository sagaStateRepository, KafkaEventPublisher kafkaEventPublisher) {
        this.sagaStateRepository = sagaStateRepository;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }
    public void handlePaymentStep(SagaState sagaState, String payload) {
        System.out.println("SagaStepHandler: handlePaymentStep");
        sagaState.setCurrentStep("PAYMENT");
        sagaStateRepository.save(sagaState);
        kafkaEventPublisher.publishEvent("payment-commands", payload);
        System.out.println("Payment commands sent successfully");
    }
    public void handleProductStep(SagaState sagaState, String eventPayload) {
        sagaState.setCurrentStep("PRODUCT");
        sagaStateRepository.save(sagaState);
        kafkaEventPublisher.publishEvent("product-commands", eventPayload);
        System.out.println("Product commands sent successfully");
    }
    public void handleDeliveryStep(SagaState sagaState, String eventPayload) {
        sagaState.setCurrentStep("DELIVERY");
        sagaStateRepository.save(sagaState);
        kafkaEventPublisher.publishEvent("delivery-commands", eventPayload);
        System.out.println("Delivery commands sent successfully");
    }

    public void compensate(SagaState sagaState, String eventPayload) {
        sagaState.setStatus("COMPENSATING");
        sagaStateRepository.save(sagaState);
        kafkaEventPublisher.publishEvent("compensation-commands", eventPayload);
        System.out.println("Compensation commands sent successfully");
    }
}
