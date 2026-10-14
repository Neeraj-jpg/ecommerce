package com.klu.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import com.klu.model.Order;
import com.klu.model.Payment;
import com.klu.repository.OrderRepository;
import com.klu.repository.PaymentRepository;
import com.klu.repository.ProductRepository;
import com.klu.repository.UserRepository;
import com.razorpay.Utils;

class RazorpayServiceTest {

    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final BackgroundJobService backgroundJobService =
            mock(BackgroundJobService.class);

    private final RazorpayService service = new RazorpayService(
            paymentRepository,
            orderRepository,
            productRepository,
            userRepository,
            backgroundJobService
    );

    @Test
    void startsOrderJobsAfterFirstSuccessfulPaymentVerification() throws Exception {
        Order order = new Order();
        order.setOrderId(42L);
        order.setTotalAmount(new BigDecimal("25.00"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setStatus("CREATED");
        when(paymentRepository.findByRazorpayOrderId("order-1"))
                .thenReturn(Optional.of(payment));

        ReflectionTestUtils.setField(service, "keySecret", "test-secret");
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.verifySignature(
                    anyString(), anyString(), anyString())).thenReturn(true);

            assertTrue(service.verifyPayment("order-1", "payment-1", "signature"));

            verify(backgroundJobService).startOrderJobs(42L);
        }
    }

    @Test
    void doesNotStartDuplicateOrderJobsForAlreadyPaidOrder() throws Exception {
        Order order = new Order();
        order.setOrderId(42L);
        order.setTotalAmount(new BigDecimal("25.00"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setStatus("PAID");
        when(paymentRepository.findByRazorpayOrderId("order-1"))
                .thenReturn(Optional.of(payment));

        ReflectionTestUtils.setField(service, "keySecret", "test-secret");
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.verifySignature(
                    anyString(), anyString(), anyString())).thenReturn(true);

            assertTrue(service.verifyPayment("order-1", "payment-1", "signature"));

            verify(backgroundJobService, never()).startOrderJobs(42L);
        }
    }
}
