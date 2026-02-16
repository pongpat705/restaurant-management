package com.restaurant.ros;

import java.util.List;
import java.util.UUID;
import java.util.Collections;

public record OrderTicket(
    String orderId,
    int tableId,
    List<OrderItem> items,
    OrderState state,
    long timestamp
) {

    public OrderTicket {
        if (orderId == null) {
            orderId = UUID.randomUUID().toString();
        }
        if (items == null) {
            items = Collections.emptyList();
        }
        if (state == null) {
            state = new OrderState.Placed();
        }
        if (timestamp == 0) {
            timestamp = System.currentTimeMillis();
        }
    }

    public record OrderItem(String name, String customization, String station) {}
}
