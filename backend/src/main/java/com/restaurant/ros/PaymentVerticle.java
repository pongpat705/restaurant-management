package com.restaurant.ros;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class PaymentVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        vertx.eventBus().consumer("billing.payment", message -> {
            // Mock Stripe processing using Virtual Thread (blocking simulation)
            try {
                System.out.println("Processing payment for order: " + message.body());
                Thread.sleep(1000); // Simulate API call
                message.reply("Payment Successful");
            } catch (InterruptedException e) {
                message.fail(500, "Payment Interrupted");
            }
        });
        startPromise.complete();
    }
}
