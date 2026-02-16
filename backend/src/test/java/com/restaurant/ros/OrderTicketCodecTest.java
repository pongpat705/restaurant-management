package com.restaurant.ros;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class OrderTicketCodecTest {

    @Test
    public void testEncodeDecode() {
        OrderTicketCodec codec = new OrderTicketCodec();
        OrderTicket ticket = new OrderTicket(
            "order-123",
            5,
            List.of(new OrderTicket.OrderItem("Burger", "No Onion", "Grill")),
            new OrderState.Placed(),
            System.currentTimeMillis()
        );

        Buffer buffer = Buffer.buffer();
        codec.encodeToWire(buffer, ticket);

        OrderTicket decoded = codec.decodeFromWire(0, buffer);

        assertEquals(ticket.orderId(), decoded.orderId());
        assertEquals(ticket.tableId(), decoded.tableId());
        assertEquals(ticket.items().size(), decoded.items().size());
        assertEquals(ticket.items().get(0).name(), decoded.items().get(0).name());
        assertEquals(ticket.items().get(0).customization(), decoded.items().get(0).customization());
        assertTrue(decoded.state() instanceof OrderState.Placed);
    }
}
