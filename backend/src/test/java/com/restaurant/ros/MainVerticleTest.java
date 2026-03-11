package com.restaurant.ros;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class MainVerticleTest {

    private HttpClient httpClient;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        httpClient = vertx.createHttpClient();
        vertx.deployVerticle(new MainVerticle()).onComplete(testContext.succeeding(id -> testContext.completeNow()));
    }

    @AfterEach
    void tearDown(Vertx vertx, VertxTestContext testContext) {
        httpClient.close().onComplete(v -> {
            vertx.close().onComplete(testContext.succeeding(v2 -> testContext.completeNow()));
        });
    }

    @Test
    void testInvalidJsonOrder(Vertx vertx, VertxTestContext testContext) {
        String invalidJson = "{\"orderId\": \"123\", \"tableId\": "; // Malformed JSON

        httpClient.request(HttpMethod.POST, 8080, "localhost", "/api/orders")
            .compose(req -> req.send(io.vertx.core.buffer.Buffer.buffer(invalidJson)))
            .onComplete(testContext.succeeding(response -> {
                testContext.verify(() -> {
                    assertEquals(400, response.statusCode());
                    response.body().onComplete(testContext.succeeding(body -> {
                        testContext.verify(() -> {
                            assertTrue(body.toString().contains("Invalid Order") || body.toString().contains("Invalid JSON"));
                            testContext.completeNow();
                        });
                    }));
                });
            }));
    }
}
