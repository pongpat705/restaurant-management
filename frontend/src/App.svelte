<script>
    import { store } from './store.svelte.js';
    import ChefView from './ChefView.svelte';
    import TableMap from './TableMap.svelte';
    import Customizer from './Customizer.svelte';
    import Notification from './Notification.svelte';

    let roles = ['Host', 'Waiter', 'Kitchen', 'Runner', 'Cashier', 'Busboy', 'Dishwasher'];

    let selectedRole = $state('Host');

    $effect(() => {
        store.role = selectedRole;
    });
</script>

<main>
    <Notification />
    <header>
        <h1>Restaurant OS</h1>
        <div class="role-selector">
            <label for="role">Role:</label>
            <select id="role" bind:value={selectedRole}>
                {#each roles as role}
                    <option value={role}>{role}</option>
                {/each}
            </select>
        </div>
    </header>

    <div class="content">
        {#if selectedRole === 'Kitchen'}
            <ChefView />
        {:else if selectedRole === 'Host'}
            <TableMap />
        {:else if selectedRole === 'Waiter'}
            <Customizer />
        {:else}
            <div class="placeholder">
                <h2>Dashboard for {selectedRole}</h2>
                <p>Not implemented yet.</p>
            </div>
        {/if}
    </div>
</main>

<style>
    main {
        display: flex;
        flex-direction: column;
        height: 100vh;
    }
    header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1rem;
        background: #333;
        color: white;
    }
    .content {
        flex: 1;
        padding: 1rem;
        overflow: auto;
    }
    .placeholder {
        text-align: center;
        margin-top: 2rem;
        color: #888;
    }
</style>
