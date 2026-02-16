package com.restaurant.ros;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class MainVerticleTest {

    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new MainVerticle())
             .onComplete(testContext.succeeding(id -> testContext.completeNow()));
    }

    @Test
    void testCreateOrder(Vertx vertx, VertxTestContext testContext) {
        HttpClient client = vertx.createHttpClient();

        JsonObject orderJson = new JsonObject()
            .put("tableId", 5)
            .put("items", List.of(
                new JsonObject().put("name", "Burger").put("customization", "No Onion").put("station", "Grill")
            ))
            .put("state", new JsonObject().put("@class", "com.restaurant.ros.OrderState$Placed"));

        client.request(HttpMethod.POST, 8080, "localhost", "/api/orders")
            .compose(req -> req.send(orderJson.toBuffer()))
            .onComplete(testContext.succeeding(response -> {
                testContext.verify(() -> {
                    assertThat(response.statusCode()).isEqualTo(200);
                    response.body().onComplete(testContext.succeeding(body -> {
                         testContext.verify(() -> {
                            JsonObject json = body.toJsonObject();
                            assertThat(json.getInteger("tableId")).isEqualTo(5);
                            testContext.completeNow();
                         });
                    }));
                });
            }));
    }
}
