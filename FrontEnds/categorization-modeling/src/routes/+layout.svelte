<script>
    import Navbar from '$lib/components/Navbar.svelte';
    import Footer from '$lib/components/Footer.svelte';
</script>

<header>
    <Navbar />
</header>

<main>
    <slot />
</main>

<footer>
    <Footer />
</footer>

<style>
    main {
        min-height: 80vh;
        padding: 20px;
    }
</style>