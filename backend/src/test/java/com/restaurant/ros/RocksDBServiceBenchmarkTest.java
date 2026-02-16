package com.restaurant.ros;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RocksDBServiceBenchmarkTest {

    @TempDir
    Path tempDir;

    private RocksDBService rocksDBService;
    private String dbPath;
    private String backupPath;

    @BeforeEach
    public void setUp() {
        dbPath = tempDir.resolve("data").toString();
        backupPath = tempDir.resolve("backups").toString();
        rocksDBService = new RocksDBService(dbPath, backupPath);
    }

    @AfterEach
    public void tearDown() {
        if (rocksDBService != null) {
            rocksDBService.close();
        }
    }

    @Test
    public void testPerformance() {
        int orderCount = 5000;
        List<OrderTicket> orders = new ArrayList<>(orderCount);
        for (int i = 0; i < orderCount; i++) {
            List<OrderTicket.OrderItem> items = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                items.add(new OrderTicket.OrderItem("Item " + j, "None", "Station " + (j % 3)));
            }
            orders.add(new OrderTicket(UUID.randomUUID().toString(), i, items, null, 0));
        }

        long start = System.nanoTime();
        for (OrderTicket order : orders) {
            rocksDBService.saveOrder(order);
        }
        long end = System.nanoTime();

        double durationMs = (end - start) / 1_000_000.0;
        System.out.printf("Time to save %d orders: %.2f ms%n", orderCount, durationMs);
        System.out.printf("Average time per order: %.4f ms%n", durationMs / orderCount);
    }

    @Test
    public void testCorrectness() {
        OrderTicket order = new OrderTicket(UUID.randomUUID().toString(), 1, List.of(
            new OrderTicket.OrderItem("Burger", "No onion", "Grill"),
            new OrderTicket.OrderItem("Fries", null, "Fryer")
        ), null, 0);

        rocksDBService.saveOrder(order);

        List<OrderTicket> savedOrders = rocksDBService.getAllOrders();
        assertThat(savedOrders).hasSize(1);
        assertThat(savedOrders.get(0).orderId()).isEqualTo(order.orderId());
        assertThat(savedOrders.get(0).items()).hasSize(2);
    }
}
