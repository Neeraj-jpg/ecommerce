package com.klu.dto;

import java.math.BigDecimal;
import java.util.List;

public class CreatePaymentRequest {

    private BigDecimal amount;
    private String receipt;
    private List<CreateOrderItemRequest> items;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public List<CreateOrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<CreateOrderItemRequest> items) {
        this.items = items;
    }
}