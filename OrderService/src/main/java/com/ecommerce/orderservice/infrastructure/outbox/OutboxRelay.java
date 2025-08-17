package com.ecommerce.orderservice.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxRelay {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    public OutboxRelay(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    //burada outbox event mantığını kullandım, bu transactional işlemlerde eventler eğer method transactional olduğundan db'ye commit etmez rollback yaparsa bile yayınlanır
    //bu da atıyorum ordereventini yayınlayacaksın ordercreate olunca ma db ye commit olmadı hata çıktı transactional olarak işlendiği için createOrder methodu rollback oldu
    //ve order oluşmadı ama event yine de yayınlandı, bu da aslında saçma ve yanlış bir duruma çıkar çükü order oluşmadı ama order oluştu eventi fırlatıldı kafka ile,
    //işte bunu engellemek için outbox patternini kullanıyoruz, bu eventleri bir outbox_events tablosuna kaydetmemizi sağlıyor, böylece biz eventleri aslında db'ye commit ediyoruz
    //ve eğer örneğimizden devam edersek, order oluşmazda db'ye commit edilmeyecek ve rollback olarak, aynı şekilde ordereventimiz de outbox_events tablomuza kaydolmayıp rollback
    //olacak, ve bu aşşağıdaki method gibi bir method ile scheculed olarka 5 saniyede 3 saniyede bir isPublished'i false olan tüm eventleri otubox_events tablomuzdan alıp yayınlıyoruz
    //yani burası bu işe yarıyor
    @Scheduled(fixedDelay = 5000)
    private void publishOutboxEvents(){
        System.out.println("Publishing outbox events...");
        System.out.println(outboxRepository.findAll());
        List<OutboxEvent> outboxEvents = outboxRepository.findByPublishedFalse();
        for(OutboxEvent outboxEvent : outboxEvents){
            try{
                String payload = objectMapper.writeValueAsString(outboxEvent);
                kafkaTemplate.send("order-events", payload);
                outboxEvent.setPublished(true);
                outboxRepository.save(outboxEvent);
                System.out.println("Outbox event published successfully: " + payload);
            }
            catch (Exception e) {
                // Handle serialization error
                e.printStackTrace();
                continue;
            }
        }
    }
}
