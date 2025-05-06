<style>
    nav {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1rem 2rem;
        background-color: #e8e3d3;
        color: black;
        border-bottom: 3px solid #32006e;
    }

    .menu {
        display: flex;
        gap: 1rem;
    }

    .menu a {
        color: #32006e;
        text-decoration: none;
        cursor: pointer;
    }

    .menu a:hover {
        text-decoration: underline;
    }

    .logo {
        color: #32006e;
        font-size: 1.2rem;
        font-weight: bold;
    }

    .logo img {
        height: 50px;
        width: auto;
        display: inline;
        vertical-align: middle;
    }
</style>

<script>
    import img from '$lib/images/uw-dept-logo-department-of-philosophy-horizontal.png';
    import { onMount } from 'svelte';
    import { isTokenValid } from '$lib/auth';
    import { goto } from '$app/navigation';

    let loggedIn = false;

    onMount(() => {
        loggedIn = isTokenValid();
    });

    function logOut() {
        localStorage.removeItem('jwt');
        loggedIn = isTokenValid();
        goto('/');
    }
</script>

<nav>
    <a href="https://phil.washington.edu/"><div class="logo"><img src={img}></div></a>
    <div class="menu">
        <a href="/"><div>Home</div></a>
        <a href="/about"><div>About</div></a>
        <a href="/resume"><div>Resume a Session</div></a>
        {#if loggedIn}
            <a href="/admin"><div>Admin</div></a>
            <button on:click={logOut}>Log Out</button>
        {:else}
            <a href="/admin/login"><div>Admin Log In</div></a>
        {/if}
    </div>
</nav>