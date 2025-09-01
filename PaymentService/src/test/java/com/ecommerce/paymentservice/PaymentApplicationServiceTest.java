package com.ecommerce.paymentservice;

import com.ecommerce.paymentservice.api.PaymentCreateRequest;
import com.ecommerce.paymentservice.application.PaymentApplicationService;
import com.ecommerce.paymentservice.domain.aggregate.Payment;
import com.ecommerce.paymentservice.domain.model.Status;
import com.ecommerce.paymentservice.infrastructure.repository.PaymentRepository;
import com.ecommerce.paymentservice.infrastructure.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PaymentApplicationServiceTest {

	private PaymentRepository paymentRepository;
	private KafkaTemplate<String, String> kafkaTemplate;
	private ObjectMapper objectMapper;
	private PasswordEncoder passwordEncoder;
	private JwtUtil jwtUtil;
	private PaymentApplicationService paymentApplicationService;

	@BeforeEach
	void setUp() {
		paymentRepository = mock(PaymentRepository.class);
		kafkaTemplate = mock(KafkaTemplate.class);
		objectMapper = new ObjectMapper();
		passwordEncoder = mock(PasswordEncoder.class);
		jwtUtil = mock(JwtUtil.class);

		// OutboxRepository parametresini de ekleyin (varsa)
		paymentApplicationService = new PaymentApplicationService(
				objectMapper, kafkaTemplate, paymentRepository, passwordEncoder, jwtUtil, /* outboxRepository= */ null
		);
	}

	@Test
	void createPayment_ShouldSavePaymentAndSetFieldsCorrectly() {
		PaymentCreateRequest request = new PaymentCreateRequest();
		request.setOrderId(123L); // Long tipinde!
		request.setUserId(456L);  // Long tipinde!
		request.setProductId("789"); // String ise örnek değer verildi

		ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
		when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// createPayment metodu PaymentApplicationService'de olmalı, yoksa eklemelisin!
		paymentApplicationService.createPayment(request);

		verify(paymentRepository, times(1)).save(paymentCaptor.capture());
		Payment savedPayment = paymentCaptor.getValue();

		assertThat(savedPayment.getOrderId()).isEqualTo(123L);
		assertThat(savedPayment.getUserId()).isEqualTo(456L);
		assertThat(savedPayment.getStatus()).isEqualTo(Status.PENDING);
		assertThat(savedPayment.getSuccess()).isFalse();
		assertThat(savedPayment.getIsPaid()).isFalse();
		assertThat(savedPayment.getCreatedAt()).isNotNull();
		// createdAt'in güncel bir zaman olup olmadığını da kontrol edebilirsin
		assertThat(savedPayment.getCreatedAt()).isBeforeOrEqualTo(ZonedDateTime.now(ZoneId.of("Europe/Istanbul")));
	}
}