<script lang="ts">
    import { slide, fade } from 'svelte/transition';
    import {tick} from 'svelte';

    let step = 0;

    let firstName = '';
    let lastName = '';
    let email = '';
    let pronouns = '';

    $: if (step === 0 && firstName && lastName) advance();
    $: if (step === 1 && email) advance();
    $: if (step === 2 && pronouns) advance();

    async function advance() {
        await tick();
        step += 1;
    }

    async function handleStart() {
        const response = await fetch('/api/startSession', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                "firstName": firstName,
                "lastName": lastName,
                "email": email,
                "pronouns": pronouns
            }) // TODO: backend support for this
        });

        if (!response.ok)
            return alert('There was an error contacting the server. Please try again later.');

        const sessionId = await response.json();
        if (sessionId) {
            window.location.href = `/session/${sessionId}`;
        } else {
            alert('Failed to start a new session. Please try again.');
        }
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

    .question {
        margin-bottom: 2rem;
        text-align: center;
    }

    .inputs {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: center;
    }

    label {
        display: block;
        margin-top: 0.5rem;
    }

    input {
        width: 200px;
        padding: 0.5rem;
        margin: 0.25rem;
        border: 1px solid #ccc;
        border-radius: 0.4rem;
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
    <h1>Welcome.</h1>
    
    <div class="question" in:slide>
        {#if step === 0}<div out:fade><p>Before we begin, what is your name?</p></div>{/if}
        <div class="inputs">
            <label for="firstName">First:</label>
            <input id="firstName" type="text" bind:value={firstName} />
            <label for="lastName">Last:</label>
            <input id="lastName" type="text" bind:value={lastName} />
        </div>
    </div>

    {#if step >= 1}
        <div class="question" in:slide>
            {#if step === 1}<div out:fade><p>Next, how can we contact you?</p></div>{/if}
                <div class="inputs">
                <label for="email">Email:</label>
                <input id="email" type="text" bind:value={email} />
            </div>
        </div>
    {/if}

    {#if step >= 2}
        <div class="question" in:slide>
            {#if step === 2}<div out:fade><p>Finally, how should we refer to you?</p></div>{/if}
            <div class="inputs">
                <label for="pronouns">Pronouns:</label>
                <input id="pronouns" type="text" bind:value={pronouns} />
            </div>
        </div>
    {/if}

    {#if step >= 3}
        <button on:click={handleStart}>
            <span>Start Session</span>
        </button>
    {/if}
</div>