<script>
    import { store } from './store.svelte.js';

    let menuName = $state('');
    let newItemName = $state('');
    let newItemPrice = $state(0);
    let items = $state([]);

    function addItem() {
        if (newItemName) {
            items.push({
                id: crypto.randomUUID(),
                name: newItemName,
                price: newItemPrice,
                station: 'Grill', // Default for now
                availableCustomizations: []
            });
            newItemName = '';
            newItemPrice = 0;
        }
    }

    function saveMenu() {
        const menu = {
            id: crypto.randomUUID(),
            name: menuName,
            items: items,
            active: true
        };
        store.saveMenu(menu);
        alert('Menu Saved!');
    }
</script>

<div class="menu-manager">
    <h2>Create Menu</h2>
    <label>Menu Name: <input type="text" bind:value={menuName} /></label>

    <div class="item-form">
        <h3>Add Item</h3>
        <input type="text" placeholder="Name" bind:value={newItemName} />
        <input type="number" placeholder="Price" bind:value={newItemPrice} />
        <button onclick={addItem}>Add</button>
    </div>

    <ul>
        {#each items as item}
            <li>{item.name} - ${item.price}</li>
        {/each}
    </ul>

    <button class="save-btn" onclick={saveMenu}>Save & Activate Menu</button>
</div>

<style>
    .menu-manager { padding: 1rem; }
    .item-form { margin: 1rem 0; display: flex; gap: 0.5rem; }
    .save-btn { background: #007bff; color: white; padding: 0.5rem 1rem; border: none; cursor: pointer; }
</style>
