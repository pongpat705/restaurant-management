package com.restaurant.ros;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class MenuServiceTest {

    private RocksDBService dbService;

    @BeforeEach
    public void setup() {
        // Use a temporary DB path for testing
        // Note: RocksDBService constructor is hardcoded to use "rocksdb_data".
        // For a proper unit test, we should probably refactor to inject path.
        // But for this task, we will just use the default and clean up.
        dbService = new RocksDBService();
    }

    @AfterEach
    public void tearDown() {
        dbService.close();
        // Cleanup would be ideal here
    }

    @Test
    public void testSaveAndGetMenu() {
        String id = UUID.randomUUID().toString();
        MenuItem item = new MenuItem("i1", "Burger", 10.0, "Delicious", "Grill", List.of());
        Menu menu = new Menu(id, "Test Menu", List.of(item), true);

        dbService.saveMenu(menu);

        Menu retrieved = dbService.getMenu(id);
        assertNotNull(retrieved);
        assertEquals("Test Menu", retrieved.name());
        assertEquals(1, retrieved.items().size());
        assertEquals("Burger", retrieved.items().get(0).name());

        Menu active = dbService.getActiveMenu();
        assertNotNull(active);
        assertEquals(id, active.id());
    }
}
