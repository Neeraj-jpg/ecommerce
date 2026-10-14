package com.klu.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.klu.dto.CreatePaymentRequest;
import com.klu.dto.VerifyPaymentRequest;
import com.klu.service.RazorpayService;
import com.razorpay.Order;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    private final RazorpayService razorpayService;

    public PaymentController(RazorpayService razorpayService) {
        this.razorpayService = razorpayService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            @RequestBody CreatePaymentRequest request,
            Principal principal) {

        try {

            Order razorpayOrder =
                    razorpayService.createRazorpayOrder(
                            request,
                            principal.getName()
                    );

            JSONObject orderJson =
                    razorpayOrder.toJson();

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "id",
                    orderJson.getString("id")
            );

            response.put(
                    "amount",
                    orderJson.getInt("amount")
            );

            response.put(
                    "currency",
                    orderJson.getString("currency")
            );

            response.put(
                    "receipt",
                    orderJson.getString("receipt")
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody VerifyPaymentRequest request) {

        try {

            boolean verified =
                    razorpayService.verifyPayment(
                            request.getRazorpayOrderId(),
                            request.getRazorpayPaymentId(),
                            request.getRazorpaySignature()
                    );

            if (verified) {

                return ResponseEntity.ok(
                        Map.of(
                                "success",
                                true,
                                "message",
                                "Payment verified successfully"
                        )
                );
            }

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "success",
                                    false,
                                    "message",
                                    "Payment verification failed"
                            )
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "success",
                                    false,
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }
}