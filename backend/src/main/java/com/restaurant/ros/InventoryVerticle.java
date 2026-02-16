package com.restaurant.ros;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class InventoryVerticle extends AbstractVerticle {

    private int plates = 100;
    private int forks = 100;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        vertx.eventBus().consumer("orders.updates", message -> {
            if (message.body() instanceof OrderTicket ticket) {
                if (ticket.state() instanceof OrderState.Served) {
                    plates -= ticket.items().size();
                    forks -= ticket.items().size();
                    checkInventory();
                }
            }
        });

        vertx.eventBus().consumer("inventory.dishwasher.clean", message -> {
            if (message.body() instanceof Integer) {
                int count = (Integer) message.body();
                plates += count;
                forks += count;
                System.out.println("Inventory Restocked: Plates=" + plates);
            }
        });

        startPromise.complete();
    }

    private void checkInventory() {
        if (plates < 20) {
            System.out.println("WARNING: Low Inventory! Plates: " + plates);
            // Notify Waiter?
        }
    }
}
