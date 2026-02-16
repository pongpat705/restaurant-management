package com.restaurant.ros;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rocksdb.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RocksDBService {

    private RocksDB db;
    private final String dbPath = "rocksdb_data";
    private final String backupPath = "rocksdb_backups";
    private final ObjectMapper mapper = new ObjectMapper();

    static {
        RocksDB.loadLibrary();
    }

    public RocksDBService() {
        try {
            final Options options = new Options().setCreateIfMissing(true);
            this.db = RocksDB.open(options, dbPath);
            performDailyBackup();
        } catch (RocksDBException e) {
            throw new RuntimeException("Failed to initialize RocksDB", e);
        }
    }

    private void performDailyBackup() {
        try {
            String today = LocalDate.now().toString();
            String dailyBackup = backupPath + "/" + today;
            File backupDir = new File(dailyBackup);
            if (!backupDir.exists()) {
                Checkpoint checkpoint = Checkpoint.create(db);
                checkpoint.createCheckpoint(dailyBackup);
                System.out.println("Created daily backup at " + dailyBackup);
            }
        } catch (RocksDBException e) {
            System.err.println("Failed to create backup: " + e.getMessage());
        }
    }

    public void saveOrder(OrderTicket order) {
        try {
            byte[] key = ("order:" + order.orderId()).getBytes(StandardCharsets.UTF_8);
            byte[] value = mapper.writeValueAsBytes(order);
            db.put(key, value);

            for (OrderTicket.OrderItem item : order.items()) {
                if (item.station() != null) {
                    String indexKey = "station:" + item.station() + ":" + order.orderId();
                    db.put(indexKey.getBytes(StandardCharsets.UTF_8), new byte[0]);
                }
            }
        } catch (RocksDBException | IOException e) {
            throw new RuntimeException("Failed to save order", e);
        }
    }

    public List<OrderTicket> getAllOrders() {
        List<OrderTicket> orders = new ArrayList<>();
        try (RocksIterator iterator = db.newIterator()) {
            for (iterator.seek("order:".getBytes(StandardCharsets.UTF_8)); iterator.isValid(); iterator.next()) {
                String key = new String(iterator.key(), StandardCharsets.UTF_8);
                if (!key.startsWith("order:")) break;
                try {
                    OrderTicket order = mapper.readValue(iterator.value(), OrderTicket.class);
                    orders.add(order);
                } catch (IOException e) {
                    System.err.println("Failed to parse order: " + e.getMessage());
                }
            }
        }
        return orders;
    }

    public void close() {
        if (db != null) db.close();
    }
}
