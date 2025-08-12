package com.ecommerce.productservice.application.service;

import com.ecommerce.productservice.domain.aggregate.Order;
import com.ecommerce.productservice.domain.repository.OrderRepository;
import com.ecommerce.productservice.domain.snapshout.ProductSnapshot;
import com.ecommerce.productservice.domain.value.OrderStatus;
import com.ecommerce.productservice.infrastructure.outbox.OutboxEvent;
import com.ecommerce.productservice.infrastructure.outbox.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;

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
        outboxEvent.setAggregateType("Order");
        outboxEvent.setEventType("order-created");
        try {
            String payload = objectMapper.writeValueAsString(savedOrder);
            outboxEvent.setPayload(payload);

        } catch (Exception e) {
            throw new RuntimeException("There is an error while writing paylaod as string", e);
        }
        outboxEvent.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));
        outboxEvent.setPublished(true);
        //outbox eventinin kaydedilmesi
        outboxRepository.save(outboxEvent);
    }
}
