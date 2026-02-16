package com.restaurant.ros;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.core.json.Json;

public class MainVerticle extends AbstractVerticle {

    private RocksDBService dbService;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle())
             .onFailure(Throwable::printStackTrace)
             .onSuccess(id -> System.out.println("Deployed MainVerticle: " + id));
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        try {
            vertx.eventBus().registerDefaultCodec(OrderTicket.class, new OrderTicketCodec());
        } catch (IllegalStateException e) {
        }

        dbService = new RocksDBService();

        DeploymentOptions kitchenOptions = new DeploymentOptions()
            .setThreadingModel(ThreadingModel.VIRTUAL_THREAD);

        vertx.deployVerticle(() -> new KitchenVerticle(dbService), kitchenOptions)
             .onFailure(err -> System.err.println("Failed to deploy KitchenVerticle: " + err.getMessage()))
             .onSuccess(id -> System.out.println("Deployed KitchenVerticle with Virtual Threads: " + id));

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.post("/api/orders").handler(ctx -> {
            try {
                OrderTicket ticket = ctx.body().asPojo(OrderTicket.class);
                if (ticket == null) {
                    ctx.response().setStatusCode(400).end("Invalid JSON");
                    return;
                }

                vertx.eventBus().request("orders.new", ticket)
                    .onSuccess(reply -> {
                        ctx.response().putHeader("content-type", "application/json")
                            .end(Json.encode(ticket));
                    })
                    .onFailure(err -> {
                        ctx.response().setStatusCode(500).end(err.getMessage());
                    });
            } catch (Exception e) {
                e.printStackTrace();
                ctx.response().setStatusCode(400).end("Invalid Order: " + e.getMessage());
            }
        });

        server.webSocketHandler(ws -> {
            if (ws.path().equals("/ws/updates")) {
                var updatesConsumer = vertx.eventBus().consumer("orders.updates", msg -> {
                   try {
                       if (msg.body() instanceof OrderTicket ticket) {
                           ws.writeTextMessage("UPDATE:" + Json.encode(ticket));
                       }
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
                });

                var grillConsumer = vertx.eventBus().consumer("kitchen.station.grill", msg -> {
                    ws.writeTextMessage("STATION:GRILL:" + msg.body());
                });

                ws.closeHandler(v -> {
                    updatesConsumer.unregister();
                    grillConsumer.unregister();
                });
            } else {
                ws.close((short) 404);
            }
        });

        server.requestHandler(router).listen(8080)
            .onSuccess(http -> {
                startPromise.complete();
                System.out.println("HTTP server started on port 8080");
            })
            .onFailure(startPromise::fail);
    }

    @Override
    public void stop() {
        if (dbService != null) dbService.close();
    }
}
