package com.klu.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class VerifyPaymentRequestTest {

    @Test
    void bindsRazorpayCheckoutCallbackFields() throws Exception {
        String json = """
                {
                  "razorpay_order_id": "order_test",
                  "razorpay_payment_id": "pay_test",
                  "razorpay_signature": "signature_test"
                }
                """;

        VerifyPaymentRequest request = new ObjectMapper()
                .readValue(json, VerifyPaymentRequest.class);

        assertEquals("order_test", request.getRazorpayOrderId());
        assertEquals("pay_test", request.getRazorpayPaymentId());
        assertEquals("signature_test", request.getRazorpaySignature());
    }
}