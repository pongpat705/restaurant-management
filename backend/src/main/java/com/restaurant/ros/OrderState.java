package com.restaurant.ros;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public sealed interface OrderState permits OrderState.Placed, OrderState.Cooking, OrderState.Ready, OrderState.Served, OrderState.Paid {
    record Placed() implements OrderState {}
    record Cooking() implements OrderState {}
    record Ready() implements OrderState {}
    record Served() implements OrderState {}
    record Paid() implements OrderState {}
}
