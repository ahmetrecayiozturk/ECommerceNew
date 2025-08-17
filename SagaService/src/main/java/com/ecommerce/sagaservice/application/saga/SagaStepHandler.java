package com.ecommerce.sagaservice.application.saga;

import com.ecommerce.sagaservice.domain.aggregate.SagaState;
import com.ecommerce.sagaservice.domain.events.*;
import com.ecommerce.sagaservice.infrastructure.repository.SagaStateRepository;
import com.ecommerce.sagaservice.infrastructure.messaging.KafkaEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class SagaStepHandler {
    private final SagaStateRepository sagaStateRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final ObjectMapper objectMapper;
    public SagaStepHandler(SagaStateRepository sagaStateRepository, KafkaEventPublisher kafkaEventPublisher, ObjectMapper objectMapper) {
        this.sagaStateRepository = sagaStateRepository;
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.objectMapper = objectMapper;
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
        System.out.println(">>> Compensation başlatıldı");
        sagaState.setStatus("COMPENSATING");
        sagaStateRepository.save(sagaState);
        try {
            CompensationEvent compensationEvent = new CompensationEvent();
            compensationEvent.setOrderId(sagaState.getOrderId());
            compensationEvent.setUserId(sagaState.getUserId());
            compensationEvent.setProductId(sagaState.getProductId());
            compensationEvent.setQuantity(sagaState.getQuantity()); // <-- BURADA
            compensationEvent.setFailedStep(sagaState.getCurrentStep());
            compensationEvent.setReason("Compensation due to failure in step: " + sagaState.getCurrentStep());
            System.out.println("SagaState: " + sagaState);
            String compensationPayload = objectMapper.writeValueAsString(compensationEvent);
            kafkaEventPublisher.publishEvent("compensation-commands", compensationPayload);
            System.out.println("Compensation commands sent successfully: " + compensationPayload);
        } catch (Exception e) {
            throw new RuntimeException("Error creating/sending compensation event", e);
        }
    }
}
/*
chatgpt'ye yaptığımı söyledim şamasını çizmesini istedim bunu verdi, aslında yaptığım çok basit, command yayınlandıktan sonra event yayınlanır ve öylece devam eder
eğer bir adımda hata olursa o adımın compensation eventini yayınlar ve her servis işlemi rollback yapar, tabi burada biz compensation eventini yayınlarken
sagastate'sini kullanıyoruz, o yüzden bu sagaState hep güncel oluyor ve her adımda currentStep'i ve gerekli yerleri güncelliyoruz,
mesela paymentten sonra product var, ama product ile dolacak alan sagaStatete boş, o zaman ne productservice ne de delivery service rollback yapacak, sadece producr service
yapacak çünkü onun kısmına kadar dolu, he mesela producttat çıktı hata, ama delivery nesnesi oluşmadığından(delivery nesnesi producttan sonra oluşur eğer productta hata olursa oluşmayacaktır)
delivery'deöyle bir nesne bulunmadı hatası loglarız ama orderId ile bulunan her şeyde rollback olur
ayrıca idempotent denen bir şey de yapıyoruz o da mesela bir event statusu birden çok kere cancalled edilebilir başka servisler yüzünden, onun için eğer cancalled ise zaten cancalled
diye bi log basıyoruz ve o adımda rollback yapmıyoruz, çünkü zaten cancalled.
+----------------------+     +-----------------------+     +-----------------------+     +---------------------+
|  Order Service       |     |   Payment Service     |     |   Product Service     |     |  Delivery Service   |
+----------------------+     +-----------------------+     +-----------------------+     +---------------------+
            |                          |                           |                           |
            |   1. order-events        |                           |                           |
            +------------------------->|                           |                           |
            |                          |                           |                           |
            |                          |                           |                           |
            |                          | 2. payment-commands       |                           |
            |                          +-------------------------> |                           |
            |                          |                           |                           |
            |                          | 3. payment-events         |                           |
            |                          <-------------------------+ |                           |
            |                          |                           |                           |
            |                          |                           | 4. product-commands       |
            |                          |                           +-------------------------> |
            |                          |                           |                           |
            |                          |                           | 5. product-events         |
            |                          |                           <-------------------------+ |
            |                          |                           |                           |
            |                          |                           | 6. delivery-commands      |
            |                          |                           |   +---------------------->|
            |                          |                           |   |                       |
            |                          |                           |   | 7. delivery-events    |
            |                          |                           |   |<---------------------+|
            |                          |                           |   |                       |
+----------------------------------+   +-----------------------+   +-----------------------+   +---------------------+
|         SAGA ORCHESTRATOR        | (handleOrderEvent, handlePaymentEvent, handleProductEvent, handleDeliveryEvent)  |
+----------------------------------+--------------------------------------------------------------------+
            |                          |                           |                           |
            |---stepHandler--->(PAYMENT)---stepHandler--->(PRODUCT)---stepHandler--->(DELIVERY)--stepHandler-->
 */