package com.restaurant.ros;

import java.util.List;

public record Menu(
    String id,
    String name,
    List<MenuItem> items,
    boolean active
) {}
