<script lang="ts">
    let part1 = '';
    let part2 = '';
    let sessionId = '';

    async function handleResume() {
        sessionId = part1 + part2;
        if (!sessionId || sessionId.length !== 6) {
            alert('Please enter a valid, 6-digit session ID.');
            return;
        }

        // API request to confirm session ID and resume session
        const response = await fetch(`/api/isValidSession/${sessionId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ sessionId })
        });

        if (!response.ok)
            return alert('There was an error contacting the server. Please try again later.');

        if (await response.json()) {
            window.location.href = `/session/${sessionId}`;
        } else {
            alert('No session found with that ID. Please check your ID or inform project admins.\nIf you are trying to start a new session, please go to the home page and start a new session.');
        }
    }

    function handleInput(event: Event & { currentTarget: EventTarget & HTMLInputElement }, part: 'part1' | 'part2') {
        const target = event.currentTarget as HTMLInputElement;
        const value = target.value.replace(/\D/g, ''); // Remove non-digit characters
        
        if (part === 'part1') {
            part1 = value.slice(0, 3);
            if (part1.length === 3) {
                part2 = value.slice(3, 6);
                document.getElementById('part2')?.focus();
            }
        } else if (part === 'part2') {
            part2 = value.slice(0, 3);
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

    .session-input {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        font-size: 2rem;
        margin-bottom: 1rem;
    }

    input[type='text'] {
        width: 4rem;
        text-align: center;
        font-size: 2rem;
        border: 2px solid #ccc;
        border-radius: 0.5rem;
        padding: 0.2rem 0.5rem;
        transition: border-color 0.2s;
        flex-basis: 50%;
    }

    input[type='text']:focus {
        border-color: #32006e;
        outline: none;
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
    <h1>Resume a study session</h1>
    <p>To resume a session if the page was closed, please enter your session ID below and press resume.</p>

    <div class="session-input">
        <input type="text" id="part1" inputmode="numeric" pattern="\d*" maxlength="3" bind:value={part1} on:input={(e) => handleInput(e, 'part1')} placeholder="XXX" />
        <span>-</span>
        <input type="text" id="part2" inputmode="numeric" pattern="\d*" maxlength="3" bind:value={part2} on:input={(e) => handleInput(e, 'part2')} placeholder="XXX" />
    </div>
    <button on:click={handleResume}><span>Resume</span></button>
</div>