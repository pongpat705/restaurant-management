package com.restaurant.ros;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

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
        assertEquals(ticket.timestamp(), decoded.timestamp());
        assertEquals(ticket.items().size(), decoded.items().size());
        assertEquals(ticket.items().get(0).name(), decoded.items().get(0).name());
        assertEquals(ticket.items().get(0).customization(), decoded.items().get(0).customization());
        assertTrue(decoded.state() instanceof OrderState.Placed);
    }

    @Test
    public void testEncodeDecodeAllStates() {
        OrderTicketCodec codec = new OrderTicketCodec();
        OrderState[] states = {
            new OrderState.Placed(),
            new OrderState.Cooking(),
            new OrderState.Ready(),
            new OrderState.Served(),
            new OrderState.Paid()
        };

        for (OrderState state : states) {
            OrderTicket ticket = new OrderTicket(
                "order-" + state.getClass().getSimpleName(),
                1,
                Collections.emptyList(),
                state,
                System.currentTimeMillis()
            );

            Buffer buffer = Buffer.buffer();
            codec.encodeToWire(buffer, ticket);

            OrderTicket decoded = codec.decodeFromWire(0, buffer);

            assertEquals(ticket.orderId(), decoded.orderId());
            // Using instanceof check since equals might rely on object identity if not overridden in records (records do override equals, but just to be sure about type)
            // Actually, records implement equals based on components, so equals should work if components match.
            // But here we want to ensure the type is correct.
            assertTrue(decoded.state().getClass().isInstance(state));
            assertEquals(state.getClass(), decoded.state().getClass());
        }
    }

    @Test
    public void testEncodeDecodeMultipleItems() {
        OrderTicketCodec codec = new OrderTicketCodec();
        List<OrderTicket.OrderItem> items = new ArrayList<>();
        items.add(new OrderTicket.OrderItem("Burger", "No Onion", "Grill"));
        items.add(new OrderTicket.OrderItem("Fries", "Extra Salt", "Fryer"));
        items.add(new OrderTicket.OrderItem("Soda", null, "Bar"));

        OrderTicket ticket = new OrderTicket(
            "order-multi",
            2,
            items,
            new OrderState.Cooking(),
            System.currentTimeMillis()
        );

        Buffer buffer = Buffer.buffer();
        codec.encodeToWire(buffer, ticket);

        OrderTicket decoded = codec.decodeFromWire(0, buffer);

        assertEquals(ticket.items().size(), decoded.items().size());
        for (int i = 0; i < items.size(); i++) {
            assertEquals(items.get(i).name(), decoded.items().get(i).name());
            assertEquals(items.get(i).customization(), decoded.items().get(i).customization());
            assertEquals(items.get(i).station(), decoded.items().get(i).station());
        }
    }

    @Test
    public void testEncodeDecodeEmptyItems() {
        OrderTicketCodec codec = new OrderTicketCodec();
        OrderTicket ticket = new OrderTicket(
            "order-empty",
            3,
            Collections.emptyList(),
            new OrderState.Ready(),
            System.currentTimeMillis()
        );

        Buffer buffer = Buffer.buffer();
        codec.encodeToWire(buffer, ticket);

        OrderTicket decoded = codec.decodeFromWire(0, buffer);

        assertEquals(0, decoded.items().size());
    }

    @Test
    public void testEncodeDecodeNullItemFields() {
        OrderTicketCodec codec = new OrderTicketCodec();
        // Item with null fields
        OrderTicket.OrderItem item = new OrderTicket.OrderItem(null, null, null);
        OrderTicket ticket = new OrderTicket(
            "order-nulls",
            4,
            List.of(item),
            new OrderState.Served(),
            System.currentTimeMillis()
        );

        Buffer buffer = Buffer.buffer();
        codec.encodeToWire(buffer, ticket);

        OrderTicket decoded = codec.decodeFromWire(0, buffer);

        assertEquals(1, decoded.items().size());
        assertNull(decoded.items().get(0).name());
        assertNull(decoded.items().get(0).customization());
        assertNull(decoded.items().get(0).station());
    }

    @Test
    public void testTransform() {
        OrderTicketCodec codec = new OrderTicketCodec();
        OrderTicket ticket = new OrderTicket(
            "order-transform",
            5,
            Collections.emptyList(),
            new OrderState.Paid(),
            System.currentTimeMillis()
        );

        assertSame(ticket, codec.transform(ticket));
    }

    @Test
    public void testCodecNameAndSystemId() {
        OrderTicketCodec codec = new OrderTicketCodec();
        assertEquals("OrderTicketCodec", codec.name());
        assertEquals(-1, codec.systemCodecID());
    }
}
