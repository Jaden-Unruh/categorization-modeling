<script lang="ts">
    async function handleStart() {
        window.location.href = "/start";
    }
</script>

<style>
    .main {
        margin: auto;
        max-width: 800px;
        padding: 1rem;
        border: 2px solid #32006e;
        border-radius: 8px;
        background-color: #e8e3d3;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
    }

    button {
        background-color: #4b2e83;
        color: #e8e3d3;
        display: inline-block;
        border-radius: 4px;
        border: 3px solid #32006e;
        text-align: center;
        font-size: 16px;
        padding: 10px;
        width: 100%;
        transition: all 0.5s;
        cursor: pointer;
        margin: 5px;
        overflow: hidden;
    }

    button:hover {
        background-color: #ffc700;
        color: #32006e;
    }

    button span {
        cursor: pointer;
        display: inline-block;
        position: relative;
        transition: 0.5s;
    }

    button span:after {
        content: '\00bb';
        position: absolute;
        opacity: 0;
        top: 0;
        right: -10px;
        transition: 0.5s;
        color: #32006e;
    }

    button:hover span {
        padding-right: 10px;
    }

    button:hover span:after {
        opacity: 1;
        right: 0;
    }
</style>

<div class="main">
    <h1>Categorization Modeling</h1>
    <p>Welcome to our project. To begin a new session, please press the button below.</p>
    <button on:click={handleStart}><span>Begin</span></button>
</div>