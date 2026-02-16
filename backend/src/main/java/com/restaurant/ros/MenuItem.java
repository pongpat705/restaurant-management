package com.restaurant.ros;

import java.util.List;

public record MenuItem(
    String id,
    String name,
    double price,
    String description,
    String station,
    List<String> availableCustomizations
) {}
