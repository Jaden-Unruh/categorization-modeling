<script lang="ts">
    import { slide, fade } from 'svelte/transition';
    import {tick} from 'svelte';

    let step = 0;

    let numPpl = 1;

    let p1firstName = '';
    let p1lastName = '';
    let p1email = '';
    let p1pronouns = '';

    let p2firstName = '';
    let p2lastName = '';
    let p2email = '';
    let p2pronouns = '';

    $: if (numPpl === 1 && step === 0 && p1firstName && p1lastName) advance();
    $: if (numPpl === 1 && step === 1 && p1email) advance();
    $: if (numPpl === 1 && step === 2 && p1pronouns) advance();

    $: if (numPpl === 2 && step === 0 && p1firstName && p1lastName && p1email && p1pronouns) advance();
    $: if (numPpl === 2 && step === 1 && p2firstName && p2lastName && p2email && p2pronouns) advance(2);

    async function advance(steps?: number) {
        await tick();
        step += steps || 1;
    }

    async function handleStart() {
        if (numPpl === 1) {
            const response = await fetch('/api/startSession', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    "twoParticipants": false,
                    "firstName": p1firstName,
                    "lastName": p1lastName,
                    "email": p1email,
                    "pronouns": p1pronouns
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
        } else {
            const response = await fetch('/api/startSession', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    "twoParticipants": true,
                    "firstName1": p1firstName,
                    "lastName1": p1lastName,
                    "email1": p1email,
                    "pronouns1": p1pronouns,
                    "firstName2": p2firstName,
                    "lastName2": p2lastName,
                    "email2": p2email,
                    "pronouns2": p2pronouns
                })
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

    <div class="question">
        {#if step === 0}<div out:fade><p>First, how many people are completing this trial?</p></div>{/if}
        <div class="inputs">
            <select name="numPpl" id="numPpl" bind:value={numPpl}>
                <option value="1" selected>1 person</option>
                <option value="2">2 people</option>
            </select>
        </div>
    </div>

    {#if numPpl === 1}
    
        {#if step >= 1}
            <div class="question" in:slide>
                {#if step === 1}<div out:fade><p>And, what is your name?</p></div>{/if}
                <div class="inputs">
                    <label for="firstName">First:</label>
                    <input id="firstName" type="text" bind:value={firstName} />
                    <label for="lastName">Last:</label>
                    <input id="lastName" type="text" bind:value={lastName} />
                </div>
            </div>
        {/if}

        {#if step >= 2}
            <div class="question" in:slide>
                {#if step === 2}<div out:fade><p>Next, how can we contact you?</p></div>{/if}
                <div class="inputs">
                    <label for="email">Email:</label>
                    <input id="email" type="text" bind:value={email} />
                </div>
            </div>
        {/if}

        {#if step >= 3}
            <div class="question" in:slide>
                {#if step === 3}<div out:fade><p>Finally, how should we refer to you?</p></div>{/if}
                <div class="inputs">
                    <label for="pronouns">Pronouns:</label>
                    <input id="pronouns" type="text" bind:value={pronouns} />
                </div>
            </div>
        {/if}

    {:else}

        {#if step >= 1}
            <div class="question" in:slide>
                {#if step === 1}<div out:fade><p>Please have one of the two participants fill in the following information.</p></div>{/if}
                <div class="inputs">
                    <label for="p1firstName">First Name:</label>
                    <input id="p1firstName" type="text" bind:value={p1firstName} />
                    <label for="p1lastName">Last Name:</label>
                    <input id="p1lastName" type="text" bind:value={p1lastName} />
                    <label for="p1email">Email:</label>
                    <input id="p1email" type="text" bind:value={p1email} />
                    <label for="p1pronouns">Pronouns:</label>
                    <input id="p1pronouns" type="text" bind:value={p1pronouns} />
                </div>
            </div>
        {/if}

        {#if step >= 2}
            <div class="question" in:slide>
                {#if step === 2}<div out:fade><p>And now, please have the other participant complete their information</p></div>{/if}
                <div class="inputs">
                    <label for="p2firstName">First Name:</label>
                    <input id="p2firstName" type="text" bind:value={p2firstName} />
                    <label for="p2lastName">Last Name:</label>
                    <input id="p2lastName" type="text" bind:value={p2lastName} />
                    <label for="p2email">Email:</label>
                    <input id="p2email" type="text" bind:value={p2email} />
                    <label for="p2pronouns">Pronouns:</label>
                    <input id="p2pronouns" type="text" bind:value={p2pronouns} />
                </div>
            </div>
        {/if}
    {/if}

    {#if step >= 4}
        <button on:click={handleStart}>
            <span>Start Session</span>
        </button>
    {/if}
</div>