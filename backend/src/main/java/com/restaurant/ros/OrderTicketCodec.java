package com.restaurant.ros;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import java.util.ArrayList;
import java.util.List;

public class OrderTicketCodec implements MessageCodec<OrderTicket, OrderTicket> {

    @Override
    public void encodeToWire(Buffer buffer, OrderTicket ticket) {
        writeString(buffer, ticket.orderId());
        buffer.appendInt(ticket.tableId());
        buffer.appendLong(ticket.timestamp());

        byte stateByte = 0;
        if (ticket.state() instanceof OrderState.Placed) stateByte = 0;
        else if (ticket.state() instanceof OrderState.Cooking) stateByte = 1;
        else if (ticket.state() instanceof OrderState.Ready) stateByte = 2;
        else if (ticket.state() instanceof OrderState.Served) stateByte = 3;
        else if (ticket.state() instanceof OrderState.Paid) stateByte = 4;
        buffer.appendByte(stateByte);

        List<OrderTicket.OrderItem> items = ticket.items();
        buffer.appendInt(items.size());
        for (OrderTicket.OrderItem item : items) {
            writeString(buffer, item.name());
            writeString(buffer, item.customization());
            writeString(buffer, item.station());
        }
    }

    private void writeString(Buffer buffer, String s) {
        if (s == null) {
            buffer.appendInt(-1);
        } else {
            byte[] bytes = s.getBytes();
            buffer.appendInt(bytes.length);
            buffer.appendBytes(bytes);
        }
    }

    private String readString(Buffer buffer, int[] posWrapper) {
        int pos = posWrapper[0];
        int len = buffer.getInt(pos);
        pos += 4;
        String s = null;
        if (len != -1) {
            s = buffer.getString(pos, pos + len);
            pos += len;
        }
        posWrapper[0] = pos;
        return s;
    }

    @Override
    public OrderTicket decodeFromWire(int pos, Buffer buffer) {
        int[] posWrapper = new int[]{pos};

        String orderId = readString(buffer, posWrapper);
        int tableId = buffer.getInt(posWrapper[0]);
        posWrapper[0] += 4;
        long timestamp = buffer.getLong(posWrapper[0]);
        posWrapper[0] += 8;

        byte stateByte = buffer.getByte(posWrapper[0]);
        posWrapper[0] += 1;
        OrderState state = switch (stateByte) {
            case 0 -> new OrderState.Placed();
            case 1 -> new OrderState.Cooking();
            case 2 -> new OrderState.Ready();
            case 3 -> new OrderState.Served();
            case 4 -> new OrderState.Paid();
            default -> new OrderState.Placed();
        };

        int itemsSize = buffer.getInt(posWrapper[0]);
        posWrapper[0] += 4;
        List<OrderTicket.OrderItem> items = new ArrayList<>(itemsSize);
        for (int i = 0; i < itemsSize; i++) {
            String name = readString(buffer, posWrapper);
            String customization = readString(buffer, posWrapper);
            String station = readString(buffer, posWrapper);
            items.add(new OrderTicket.OrderItem(name, customization, station));
        }

        return new OrderTicket(orderId, tableId, items, state, timestamp);
    }

    @Override
    public OrderTicket transform(OrderTicket ticket) {
        return ticket;
    }

    @Override
    public String name() {
        return "OrderTicketCodec";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
