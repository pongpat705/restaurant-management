<script>
    import { store } from './store.svelte.js';

    let station = $state('Grill');

    let stationOrders = $derived(store.orders.filter(order => {
        const hasItems = order.items && order.items.some(item => item.station === station);
        const stateClass = order.state ? order.state['@class'] : '';
        const isActive = stateClass && (stateClass.includes('Placed') || stateClass.includes('Cooking'));

        return hasItems && isActive;
    }));

    function markReady(orderId) {
        console.log('Marking ready:', orderId);
    }
</script>

<div class="chef-dashboard">
    <div class="controls">
        <label>Station:
            <select bind:value={station}>
                <option value="Grill">Grill Station</option>
                <option value="Salad">Salad Station</option>
            </select>
        </label>
        <span class="count">Pending: {stationOrders.length}</span>
    </div>

    <div class="tickets">
        {#each stationOrders as order (order.orderId)}
            <div class="ticket">
                <div class="ticket-header">
                    <h3>#{order.orderId.substring(0, 4)}</h3>
                    <span class="timer">12m</span>
                </div>
                <div class="meta">Table {order.tableId}</div>
                <ul class="items">
                    {#each order.items.filter(i => i.station === station) as item}
                        <li>
                            <span class="qty">1x</span>
                            <span class="name">{item.name}</span>
                            {#if item.customization}
                                <div class="cust">{item.customization}</div>
                            {/if}
                        </li>
                    {/each}
                </ul>
                <div class="actions">
                    <button class="ready-btn" onclick={() => markReady(order.orderId)}>Ready</button>
                </div>
            </div>
        {/each}
    </div>
</div>

<style>
    .chef-dashboard {
        padding: 1rem;
        background: #f0f0f0;
        min-height: 100%;
        color: #333;
    }
    .controls {
        margin-bottom: 1rem;
        display: flex;
        gap: 1rem;
        align-items: center;
        font-size: 1.2rem;
    }
    .tickets {
        display: flex;
        flex-wrap: wrap;
        gap: 1rem;
    }
    .ticket {
        background: #fff;
        border-radius: 4px;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        width: 300px;
        display: flex;
        flex-direction: column;
        border-top: 5px solid #d32f2f;
    }
    .ticket-header {
        padding: 0.5rem 1rem;
        display: flex;
        justify-content: space-between;
        align-items: center;
        background: #fafafa;
        border-bottom: 1px solid #eee;
    }
    .meta {
        padding: 0.5rem 1rem;
        background: #eee;
        font-weight: bold;
        font-size: 0.9rem;
    }
    .items {
        list-style: none;
        padding: 1rem;
        margin: 0;
        flex: 1;
    }
    .items li {
        margin-bottom: 0.5rem;
        border-bottom: 1px dashed #eee;
        padding-bottom: 0.5rem;
    }
    .cust {
        color: #d32f2f;
        font-weight: bold;
        font-size: 0.9rem;
        margin-top: 0.2rem;
    }
    .actions {
        padding: 1rem;
        border-top: 1px solid #eee;
    }
    .ready-btn {
        width: 100%;
        background: #2e7d32;
        color: white;
        border: none;
        padding: 0.8rem;
        border-radius: 4px;
        font-size: 1rem;
        cursor: pointer;
    }
    .ready-btn:hover {
        background: #1b5e20;
    }
</style>
