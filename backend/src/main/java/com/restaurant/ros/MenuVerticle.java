package com.restaurant.ros;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;

public class MenuVerticle extends AbstractVerticle {

    private final RocksDBService dbService;

    public MenuVerticle(RocksDBService dbService) {
        this.dbService = dbService;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        EventBus eb = vertx.eventBus();

        eb.consumer("menu.create", message -> {
            try {
                // Assuming message body is JSON string of Menu
                Menu menu = Json.decodeValue((String) message.body(), Menu.class);
                dbService.saveMenu(menu);
                if (menu.active()) {
                    eb.publish("menu.updates", Json.encode(menu));
                }
                message.reply("Menu created");
            } catch (Exception e) {
                message.fail(500, e.getMessage());
            }
        });

        eb.consumer("menu.get", message -> {
            String id = (String) message.body();
            Menu menu = "active".equals(id) ? dbService.getActiveMenu() : dbService.getMenu(id);
            if (menu != null) {
                message.reply(Json.encode(menu));
            } else {
                message.fail(404, "Menu not found");
            }
        });

        eb.consumer("menu.delete", message -> {
            String id = (String) message.body();
            dbService.deleteMenu(id);
            message.reply("Menu deleted");
        });

        startPromise.complete();
    }
}
