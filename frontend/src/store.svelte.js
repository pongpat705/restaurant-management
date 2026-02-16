let socket;
let ordersMap = $state(new Map());
let orders = $derived(Array.from(ordersMap.values()));
let role = $state('Host');

function init() {
    if (socket) return;

    const wsUrl = window.location.hostname === 'localhost'
        ? 'ws://localhost:8080/ws/updates'
        : `ws://${window.location.host}/ws/updates`;

    socket = new WebSocket(wsUrl);

    socket.onopen = () => {
        console.log('Connected to WebSocket');
    };

    socket.onmessage = (event) => {
        const data = event.data;
        if (data.startsWith('UPDATE:')) {
            try {
                const ticket = JSON.parse(data.substring(7));
                ordersMap.set(ticket.orderId, ticket);
            } catch (e) {
                console.error('Error parsing update:', e);
            }
        }
    };
}

if (typeof window !== 'undefined') {
    init();
}

export const store = {
    get orders() { return orders },
    get role() { return role },
    set role(value) { role = value },

    addOrder(order) {
        fetch('/api/orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(order)
        }).then(res => {
            if (!res.ok) console.error('Failed to place order');
        });
    }
};
