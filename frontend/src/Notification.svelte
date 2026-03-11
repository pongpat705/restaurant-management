<script>
    import { store } from './store.svelte.js';
</script>

<div class="notifications">
    {#each store.notifications as notification (notification.id)}
        <div class="notification {notification.type}">
            {notification.message}
        </div>
    {/each}
</div>

<style>
    .notifications {
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 1000;
        display: flex;
        flex-direction: column;
        gap: 10px;
    }
    .notification {
        padding: 10px 20px;
        border-radius: 4px;
        background: #333;
        color: white;
        box-shadow: 0 2px 4px rgba(0,0,0,0.2);
        animation: slideIn 0.3s ease-out;
    }
    .notification.success {
        background: #4caf50;
    }
    .notification.error {
        background: #f44336;
    }
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
</style>
