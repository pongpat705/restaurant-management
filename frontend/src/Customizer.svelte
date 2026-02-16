<script>
    import { store } from './store.svelte.js';

    let itemName = $state('Burger');
    let customization = $state('');

    function placeOrder() {
        store.addOrder({
            tableId: 1,
            items: [{
                name: itemName,
                customization: customization,
                station: itemName === 'Burger' ? 'Grill' : 'Salad'
            }]
        });
        customization = '';
        alert('Order Placed!');
    }
</script>

<div class="customizer">
    <h2>Take Order</h2>
    <label>
        Item:
        <select bind:value={itemName}>
            <option value="Burger">Burger</option>
            <option value="Salad">Salad</option>
            <option value="Steak">Steak</option>
        </select>
    </label>
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
