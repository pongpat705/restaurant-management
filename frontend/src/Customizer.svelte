<script>
    import { store } from './store.svelte.js';

    let selectedItemId = $state('');
    let customization = $state('');

    // Derived state for available items
    let menuItems = $derived(store.menu ? store.menu.items : []);

    function placeOrder() {
        const item = menuItems.find(i => i.id === selectedItemId);
        if (!item) return;

        store.addOrder({
            tableId: 1,
            items: [{
                name: item.name,
                customization: customization,
                station: item.station || 'Grill'
            }]
        });
        customization = '';
        alert('Order Placed!');
    }
</script>

<div class="customizer">
    <h2>Take Order</h2>
    {#if !store.menu}
        <p>Loading Menu...</p>
    {:else}
        <label>
            Item:
            <select bind:value={selectedItemId}>
                <option value="" disabled>Select Item</option>
                {#each menuItems as item}
                    <option value={item.id}>{item.name} (${item.price})</option>
                {/each}
            </select>
        </label>
    {/if}
    <label>
        Customization:
        <input type="text" bind:value={customization} placeholder="e.g. No Onion" />
    </label>
    <button onclick={placeOrder}>Place Order</button>
</div>

<style>
    .customizer {
        display: flex;
        flex-direction: column;
        gap: 1rem;
        max-width: 400px;
    }
</style>
