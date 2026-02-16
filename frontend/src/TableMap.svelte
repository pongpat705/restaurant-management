<script>
    import { store } from './store.svelte.js';

    let tables = $state([
        { id: 1, status: 'Free' },
        { id: 2, status: 'Free' },
        { id: 3, status: 'Free' },
        { id: 4, status: 'Free' },
        { id: 5, status: 'Free' },
        { id: 6, status: 'Free' },
    ]);

    $effect(() => {
        store.orders.forEach(order => {
            const isActive = order.state && order.state['@class'] &&
                             !order.state['@class'].includes('Paid');

            if (isActive) {
                const table = tables.find(t => t.id === order.tableId);
                if (table && table.status === 'Free') {
                    table.status = 'Occupied';
                }
            }
        });
    });

    function toggleStatus(table) {
        if (table.status === 'Free') table.status = 'Occupied';
        else if (table.status === 'Occupied') table.status = 'Dirty';
        else table.status = 'Free';
    }
</script>

<div class="table-map">
    {#each tables as table}
        <!-- svelte-ignore a11y_click_events_have_key_events -->
        <div class="table {table.status.toLowerCase()}" onclick={() => toggleStatus(table)} role="button" tabindex="0">
            <span class="id">Table {table.id}</span>
            <span class="status">{table.status}</span>
        </div>
    {/each}
</div>

<style>
    .table-map {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
        gap: 2rem;
        padding: 2rem;
    }
    .table {
        aspect-ratio: 1;
        border-radius: 50%;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        cursor: pointer;
        color: white;
        font-weight: bold;
        transition: transform 0.2s, box-shadow 0.2s;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
    }
    .table:hover {
        transform: translateY(-5px);
        box-shadow: 0 6px 12px rgba(0,0,0,0.15);
    }
    .free { background-color: #4caf50; }
    .occupied { background-color: #f44336; }
    .dirty { background-color: #ffeb3b; color: #333; }

    .id { font-size: 1.5rem; }
    .status { font-size: 0.8rem; margin-top: 0.5rem; text-transform: uppercase; letter-spacing: 1px; }
</style>
