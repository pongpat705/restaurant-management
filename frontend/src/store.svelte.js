let socket;
let orders = $state([]);
let role = $state('Host');
let notifications = $state([]);

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
                const index = orders.findIndex(o => o.orderId === ticket.orderId);
                if (index !== -1) {
                    orders[index] = ticket;
                } else {
                    orders.push(ticket);
                }
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
    get notifications() { return notifications },

    addNotification(message, type = 'info') {
        const id = Date.now();
        notifications.push({ id, message, type });
        setTimeout(() => {
            const index = notifications.findIndex(n => n.id === id);
            if (index !== -1) {
                notifications.splice(index, 1);
            }
        }, 3000);
    },

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
