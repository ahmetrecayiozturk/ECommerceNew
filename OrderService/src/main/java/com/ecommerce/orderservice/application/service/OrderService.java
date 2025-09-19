package com.ecommerce.orderservice.application.service;

import com.ecommerce.orderservice.application.ProductSnapshotProvider;
import com.ecommerce.orderservice.domain.aggregate.Order;
import com.ecommerce.orderservice.domain.events.CompensationEvent;
import com.ecommerce.orderservice.infrastructure.repository.OrderRepository;
import com.ecommerce.orderservice.domain.snapshout.ProductSnapshot;
import com.ecommerce.orderservice.domain.value.OrderStatus;
import com.ecommerce.orderservice.infrastructure.outbox.OutboxEvent;
import com.ecommerce.orderservice.infrastructure.outbox.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductSnapshotProvider productSnapshotProvider;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OrderService(OrderRepository orderRepository, ProductSnapshotProvider productSnapshotProvider, OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.productSnapshotProvider = productSnapshotProvider;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void createOrder(Long userId, int quantity, Long productId) throws JsonProcessingException {
        //snapshoutun alınması
        ProductSnapshot productSnapshot = productSnapshotProvider.getProductSnapshot(productId);
        if (productSnapshot == null) {
            throw new IllegalArgumentException("Product not found");
        }
        System.out.println("Product snapshot alındı: " + productSnapshot);
        //orderin oluşturulması
        Order order = new Order();
        order.setUserId(userId);
        order.setQuantity(quantity);
        order.setProductSnapshot(productSnapshot);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        Order savedOrder = orderRepository.save(order);

        //eventin oluşturulması
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setOrderId(savedOrder.getId());
        outboxEvent.setUserId(userId);
        outboxEvent.setProductId(productId);
        outboxEvent.setQuantity(quantity);
        outboxEvent.setAggregateType("Order");
        outboxEvent.setEventType("order-created");
        outboxEvent.setPublished(false);
        try {
            String payload = objectMapper.writeValueAsString(savedOrder);
            outboxEvent.setPayload(payload);

        } catch (Exception e) {
            throw new RuntimeException("There is an error while writing paylaod as string", e);
        }
        outboxEvent.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));
        //outbox eventinin kaydedilmesi
        outboxRepository.save(outboxEvent);
    }

    public List<Order> getAllOrders( ){
        List<Order> orders = orderRepository.findAll();
        return orders;
    }

    @KafkaListener(topics="compensation-commands", groupId = "order-service")
    @Transactional
    public void handleCompensationCommand(String payload){
        try{
            CompensationEvent compensationEvent = objectMapper.readValue(payload, CompensationEvent.class);
            Order order = orderRepository.findById(compensationEvent.getOrderId()).orElse(null);
            if(order == null){
                System.out.println("Order not found for orderId: " + compensationEvent.getOrderId() + " (Probably already compensated!)");
                return;
            }
            if(order.getStatus() == OrderStatus.CANCELLED){
                System.out.println("Order already cancelled for orderId: " + order.getId());
                return;
            }
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            System.out.println("Order cancelled for orderId: " + order.getId());
        } catch(Exception e){
            throw new RuntimeException("Order compensation failed: " + e.getMessage(), e);
        }
    }
}
