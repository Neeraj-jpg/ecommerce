package com.klu.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.klu.dto.CreateOrderItemRequest;
import com.klu.dto.CreatePaymentRequest;
import com.klu.model.Order;
import com.klu.model.OrderItem;
import com.klu.model.Payment;
import com.klu.model.Product;
import com.klu.model.User;
import com.klu.repository.OrderRepository;
import com.klu.repository.PaymentRepository;
import com.klu.repository.ProductRepository;
import com.klu.repository.UserRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

@Service
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BackgroundJobService backgroundJobService;

    public RazorpayService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            BackgroundJobService backgroundJobService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.backgroundJobService = backgroundJobService;
    }

    @Transactional
    public com.razorpay.Order createRazorpayOrder(
            CreatePaymentRequest request,
            String userEmail) throws Exception {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart must contain at least one item");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getProductId() == null
                    || itemRequest.getQuantity() == null
                    || itemRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException("Each cart item needs a product and positive quantity");
            }

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product not found: " + itemRequest.getProductId()));
            if (product.getPrice() == null || product.getPrice() < 0) {
                throw new IllegalArgumentException("Product has an invalid price");
            }

            BigDecimal price = BigDecimal.valueOf(product.getPrice())
                    .setScale(2, RoundingMode.HALF_UP);
            total = total.add(price.multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(price);
            orderItems.add(orderItem);
        }

        RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", total.multiply(BigDecimal.valueOf(100)).intValueExact());
        orderRequest.put("currency", "INR");
        String receipt = request.getReceipt();
        orderRequest.put("receipt", receipt == null || receipt.isBlank()
                ? "ORDER_" + UUID.randomUUID()
                : receipt);

        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        orderItems.forEach(item -> item.setOrder(order));
        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setRazorpayOrderId(razorpayOrder.get("id"));
        payment.setAmount(savedOrder.getTotalAmount());
        paymentRepository.save(payment);

        return razorpayOrder;
    }

    @Transactional
    public boolean verifyPayment(
        String razorpayOrderId,
        String razorpayPaymentId,
        String razorpaySignature) throws Exception {

    String payload =
            razorpayOrderId + "|" + razorpayPaymentId;

    boolean verified = Utils.verifySignature(
            payload,
            razorpaySignature,
            keySecret
    );

    if (!verified) {
        return false;
    }

    Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Payment order not found"));
    boolean alreadyPaid = "PAID".equals(payment.getStatus());
    payment.setRazorpayPaymentId(razorpayPaymentId);
    payment.setRazorpaySignature(razorpaySignature);
    payment.setStatus("PAID");
    payment.getOrder().setStatus("PAID");
    paymentRepository.save(payment);

    if (!alreadyPaid) {
        backgroundJobService.startOrderJobs(payment.getOrder().getOrderId());
    }

    return true;
}
}