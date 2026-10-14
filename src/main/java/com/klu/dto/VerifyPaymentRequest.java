package com.klu.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class VerifyPaymentRequest {

    @JsonAlias("razorpay_order_id")
    private String razorpayOrderId;

    @JsonAlias("razorpay_payment_id")
    private String razorpayPaymentId;

    @JsonAlias("razorpay_signature")
    private String razorpaySignature;

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public void setRazorpaySignature(String razorpaySignature) {
        this.razorpaySignature = razorpaySignature;
    }
}