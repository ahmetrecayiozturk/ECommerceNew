package com.ecommerce.productservice.application;

import com.ecommerce.productservice.domain.aggregate.Product;
import com.ecommerce.productservice.domain.events.CompensationEvent;
import com.ecommerce.productservice.domain.events.PaymentEvent;
import com.ecommerce.productservice.infrastructure.outbox.OutboxEvent;
import com.ecommerce.productservice.infrastructure.outbox.OutboxRepository;
import com.ecommerce.productservice.infrastructure.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class ProductCommandHandler {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;
    private final OutboxRepository outboxRepository;
    public ProductCommandHandler(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, ProductRepository productRepository, OutboxRepository outboxRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.productRepository = productRepository;
        this.outboxRepository = outboxRepository;
    }

    @KafkaListener(topics = "product-commands", groupId = "product-service")
    @Transactional
    public void handleProductCommand(String payload){
        try{
            System.out.println("handleProductCommand called! Payload: " + payload);
            OutboxEvent outboxEventTemp = objectMapper.readValue(payload, OutboxEvent.class);
            PaymentEvent paymentEvent = objectMapper.readValue(outboxEventTemp.getPayload(), PaymentEvent.class);
            System.out.println("handleProductCommand called! productId: " + paymentEvent.getProductId());
            System.out.println("PaymentEvent: " + paymentEvent);

            // DOĞRU SATIR! OutboxEvent'in productId'si root'tan al!
            Optional<Product> findedProduct = productRepository.findById(outboxEventTemp.getProductId());

            boolean success = false;
            if(findedProduct.isPresent()){
                Product product = findedProduct.get();
                int stockQuantity = product.getStockQuantity();
                if(stockQuantity>= paymentEvent.getQuantity()){
                    product.setStockQuantity(stockQuantity-paymentEvent.getQuantity());
                    System.out.println(product.getStockQuantity() + " adet ürün kaldı" + paymentEvent.getQuantity() + "kadar azaldı"
                            + product.getProductName() + " ürününden");
                    productRepository.save(product);
                    success = true;
                }
                OutboxEvent outboxEvent = new OutboxEvent();
                outboxEvent.setOrderId(paymentEvent.getOrderId());
                outboxEvent.setUserId(paymentEvent.getUserId());
                outboxEvent.setProductId(paymentEvent.getProductId());
                outboxEvent.setAggregateType("Product");
                outboxEvent.setEventType("PRODUCT_RESERVED");
                outboxEvent.setPayload(objectMapper.writeValueAsString(paymentEvent));
                outboxEvent.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));
                outboxEvent.setSuccess(success);
                outboxEvent.setPublished(false);
                outboxRepository.save(outboxEvent);
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
/*
    @KafkaListener(topics = "product-commands", groupId = "product-service")
    @Transactional
    public void handleProductCommand(String payload){
        try{
            System.out.println("handleProductCommand called! Payload: " + payload);
            //önce gidip eventi bulalım
            OutboxEvent outboxEventTemp = objectMapper.readValue(payload, OutboxEvent.class);
            //Önce gelen eventi deserialize edelim
            PaymentEvent paymentEvent = objectMapper.readValue(outboxEventTemp.getPayload(), PaymentEvent.class);
            System.out.println("handleProductCommand called! productId: " + paymentEvent.getProductId());
            //PaymentEventEnvelope envelope = objectMapper.readValue(payload, PaymentEventEnvelope.class);
            //PaymentEvent paymentEvent = objectMapper.readValue(envelope.getPayload(), PaymentEvent.class);
            System.out.println("PaymentEvent: " + paymentEvent);
            //Şimdi de bu eventten gidip productu bulalım ve doğru şeyler ile düşelim stoğu
            Optional<Product> findedProduct = productRepository.findById(outboxEventTemp.getPayload().getProductId());
            //şimdi biz bir success belirleyelim default false olsun eğer biz burada stoğu düşürüp save edersek success true olsun ve outbox eventi buna göre save edelim
            boolean success = false;
            if(findedProduct.isPresent()){
                Product product = findedProduct.get();
                int stockQuantity = product.getStockQuantity();
                if(stockQuantity>= paymentEvent.getQuantity()){
                    product.setStockQuantity(stockQuantity-paymentEvent.getQuantity());
                    System.out.println(product.getStockQuantity() + " adet ürün kaldı" + product.getStockQuantity() + "kadar azaldı"
                     + product.getProductName() + " ürününden");
                    productRepository.save(product);
                    success = true;
                }
                //şimdi de outbox eventini oluşturalım
                OutboxEvent outboxEvent = new OutboxEvent();
                outboxEvent.setOrderId(paymentEvent.getOrderId());
                outboxEvent.setUserId(paymentEvent.getUserId());
                outboxEvent.setAggregateType("Product");
                outboxEvent.setEventType("PRODUCT_RESERVED");
                outboxEvent.setPayload(objectMapper.writeValueAsString(paymentEvent));
                outboxEvent.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));
                outboxEvent.setSuccess(success);
                outboxEvent.setPublished(false);
                outboxRepository.save(outboxEvent);
                outboxEvent.setPayload(objectMapper.writeValueAsString(paymentEvent));
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    */
    @KafkaListener(topics="compensation-commands", groupId = "product-service")
    public void handleCompensationCommand(String payload){
        try{
            //önce compensatyion eventini bulalım payload ile sonra da içindekii orderId ile gidip product'u bulup o kadar arttıralım ama order'ımız yok, yani compensation işleminde
            //quantity belirtmek lazım, ayrıca productId de olabilir, boş şeyi oluştururuz o an nerede hata çıkmışsa zaten eksik kısımları sorun olmaz, çünkü oraları kullanmayız
            System.out.println("handleCompensationCommand called! Payload: " + payload);
            //Şimdi eventi bulduk, burdaki orderId ve quantity alanlarını kullanarak db'mize gerekli işlemleri commit edelim
            CompensationEvent compensationEvent = objectMapper.readValue(payload, CompensationEvent.class);
            //Product'un bulunması productId ile
            Product product = productRepository.findById(compensationEvent.getProductId()).orElse(null);
            if(product == null){
                System.out.println("Product not found for productId: " + compensationEvent.getProductId() + " (Probably already compensated!)");
            }
            else{
                product.setStockQuantity((int) (product.getStockQuantity() + compensationEvent.getQuantity()));
                System.out.println("Product stock quantity updated: " + product.getStockQuantity() + " for productId: " + compensationEvent.getProductId());
                productRepository.save(product);
            }
        }
        catch (Exception e){
            throw new RuntimeException("Compensation command handling failed: " + e.getMessage(), e);
        }

    }

}
