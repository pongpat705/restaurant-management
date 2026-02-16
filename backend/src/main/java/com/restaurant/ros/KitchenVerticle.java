package com.restaurant.ros;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;

public class KitchenVerticle extends AbstractVerticle {

    private final RocksDBService dbService;

    public KitchenVerticle(RocksDBService dbService) {
        this.dbService = dbService;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        EventBus eb = vertx.eventBus();

        eb.consumer("orders.new", message -> {
            try {
                if (!(message.body() instanceof OrderTicket)) {
                    return;
                }

                OrderTicket ticket = (OrderTicket) message.body();
                System.out.println("Kitchen received order: " + ticket.orderId());

                dbService.saveOrder(ticket);

                for (OrderTicket.OrderItem item : ticket.items()) {
                    if (item.station() != null) {
                        String stationAddress = "kitchen.station." + item.station().toLowerCase();
                        eb.publish(stationAddress, "Order " + ticket.orderId() + ": " + item.name() + " (" + item.customization() + ")");
                    }
                }

                eb.publish("orders.updates", new OrderTicket(
                    ticket.orderId(),
                    ticket.tableId(),
                    ticket.items(),
                    new OrderState.Cooking(),
                    System.currentTimeMillis()
                ));

                message.reply("Order processed");

            } catch (Exception e) {
                System.err.println("Error processing order: " + e.getMessage());
                message.fail(500, e.getMessage());
            }
        });

        startPromise.complete();
    }
}
