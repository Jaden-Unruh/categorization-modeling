<script lang="ts">
    import { onMount } from "svelte";
    import { page } from "$app/stores";
    import { get } from "svelte/store";
    import Help from "$lib/components/Help.svelte";

    let sessionId = get(page).params.id;
    let firstName = "";
    let trialNumber = 0;
    let imageUrl = "";
    let isEvil = "false";
    let feedback = "";
    let loading = true;
    let showHelp = true;

    onMount(async () => {
        const res = await fetch(`/api/session/${sessionId}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        });

        if (res.ok) {
            const data = await res.json();
            firstName = data.firstName;
            trialNumber = data.trialNumber;
            await fetchTrial();
        } else {
            feedback =
                "Invalid session ID. Please check your URL or inform project admins.";
        }
    });

    async function fetchTrial() {
        const res = await fetch(`/api/trial/${sessionId}/${trialNumber}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        });
        if (res.ok) {
            const data = await res.json();
            imageUrl = data.imageUrl;
            isEvil = data.isEvil;
            loading = false;
            feedback = "";
        } else {
            feedback =
                "Error fetching trial data. Please try again later or inform project admins.";
        }
    }

    async function handleChoice(choice: "eat" | "discard") {
        loading = true;
        const res = await fetch(`/api/submit/${sessionId}/${trialNumber}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ choice }),
        });
        const data = await res.json();
        if (res.ok) {
            feedback = isEvil === "true"
                ? choice === "eat"
                    ? "You shouldn't have eaten that!"
                    : "Correct, that was inedible"
                : choice === "eat"
                  ? "Correct, that one was delicious!"
                  : "Eww, that was inedible!";

            trialNumber += 1;
            setTimeout(fetchTrial, 2000);
        } else {
            feedback =
                "Error submitting choice. Please try again later or inform project admins.";
        }
    }

    function toggleHelp() {
        showHelp = !showHelp;
    }
</script>

<main class="session-container">
    <header class="session-header">
        <div class="session-info">
            Session: {sessionId} | {firstName} | Trial: {trialNumber}
        </div>
        <a href="#" class="help-link" on:click={toggleHelp}>
            Help
            </a>
    </header>

    <Help visible={showHelp} onClose={() => (showHelp = false)} />

    {#if loading}
        <p>Loading...</p>
    {:else}
        <h2>Trial {trialNumber}</h2>
        <p>Is this fruit edible?</p>
    {/if}

    {#if imageUrl}
        <img src={imageUrl} alt="Trial Image" class="trial-image" />
    {/if}

    <div class="button-row">
        <button
            class="choice-button eat"
            on:click={() => handleChoice("eat")}
            disabled={loading}
        >
            Eat
        </button>
        <button
            class="choice-button discard"
            on:click={() => handleChoice("discard")}
            disabled={loading}
        >
            Discard
        </button>
    </div>

    {#if feedback}
        <p class="feedback">{feedback}</p>
    {/if}
</main>

<style>
    .session-container {
        padding: 1.5rem;
        max-width: 600px;
        margin: 0 auto;
        text-align: center;
        font-family: Arial, Helvetica, sans-serif;
    }

    .session-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1rem;
        font-size: 0.9rem;
        color: #333;
    }

    .help-link {
        color: #2ad2c9;
        text-decoration: none;
    }

    .help-link:hover {
        text-decoration: underline;
    }

    .trial-image {
        max-height: 300px;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        margin: 1rem auto;
        display: block;
    }

    .button-row {
        margin-top: 1rem;
    }

    .choice-button {
        padding: 0.6rem 1.2rem;
        margin: 0 0.5rem;
        border: none;
        border-radius: 6px;
        font-size: 1rem;
        cursor: pointer;
        color: white;
    }

    .choice-button.eat {
        background-color: #4caf50;
    }

    .choice-button.discard {
        background-color: #f44336;
    }

    .choice-button:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    .feedback {
        margin-top: 1rem;
        font-size: 1.1rem;
        font-weight: 600;
    }
</style>