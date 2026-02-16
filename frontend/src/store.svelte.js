let socket;
let orders = $state([]);
let role = $state('Host');
let menu = $state(null);

function init() {
    if (socket) return;

    const wsUrl = window.location.hostname === 'localhost'
        ? 'ws://localhost:8080/ws/updates'
        : `ws://${window.location.host}/ws/updates`;

    socket = new WebSocket(wsUrl);

    socket.onopen = () => {
        console.log('Connected to WebSocket');
        fetchActiveMenu();
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
        } else if (data.startsWith('MENU:')) {
            try {
                menu = JSON.parse(data.substring(5));
            } catch (e) {
                console.error('Error parsing menu update:', e);
            }
        }
    };
}

function fetchActiveMenu() {
    fetch('/api/menu/active')
        .then(res => {
            if (res.ok) return res.json();
            throw new Error('No active menu');
        })
        .then(data => menu = data)
        .catch(err => console.log('No active menu loaded'));
}

if (typeof window !== 'undefined') {
    init();
}

export const store = {
    get orders() { return orders },
    get role() { return role },
    get menu() { return menu },
    set role(value) { role = value },

    saveMenu(newMenu) {
        fetch('/api/menu', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newMenu)
        });
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
