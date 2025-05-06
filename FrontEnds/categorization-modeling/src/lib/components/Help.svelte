<script>
    export let visible = false;
    export let onClose = () => {};
</script>

{#if visible}
    <div class="overlay" on:click={onClose}>
        <div class="popup" on:click|stopPropagation>
            <button class="close-button" on:click={onClose}>X</button>
            <h2>Welcome to the Categorization Modeling study!</h2>
            <p>
                In this task, you'll sort cartoon images of fruits into "edible"
                or "inedible" categories.<br />
                The edibility is determined by a hidden rule based on fruit traits.
            </p>
            <p>Each fruit varies by four features:</p>
            <ul>
                <li>Shape: Apple- or Pear-shaped</li>
                <li>Color: Orange or Blue</li>
                <li>Spots: Present or Absent</li>
                <li>Stem: Present or Absent</li>
            </ul>
            <p>
                After seeing an image, choose whether to "eat" or "discard" it.
                You'll get immediate feedback whether you took appropriate
                action or not.<br />
                Your goal is to learn the hidden rule and apply it later in the session.
                The rule may involve one or multiple features.
            </p>
            <h3>Example</h3>
            <img
                src="/api/trialImages/0101.svg"
                alt="Example Image"
                class="example-image"
            />
            <p>
                In this example, the fruit is Apple-shaped, blue, has no spots,
                and has a stem. If, say, you thought the rule was that all blue
                fruits with stems were edible, you would choose to "Eat" this
                fruit.
            </p>
        </div>
    </div>
{/if}

<style>
    .overlay {
        position: fixed;
        inset: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 1000;
    }

    .popup {
        background: white;
        padding: 2rem;
        border-radius: 8px;
        width: 90%;
        max-width: 500px;
        box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
        position: relative;
    }

    .popup h2 {
        margin-top: 0;
    }

    .popup img {
        width: 100%;
        margin-top: 1rem;
        border: 1px solid #ccc;
        border-radius: 4px;
    }

    .close-button {
        position: absolute;
        top: 0.5rem;
        right: 0.5rem;
        background: none;
        border: none;
        font-size: 1.2rem;
        cursor: pointer;
    }

    ul {
        padding-left: 1.2rem;
    }

    li {
        margin-bottom: 0.4rem;
    }

    .example-image {
        max-width: 100px;
        margin: 0 auto;
        display: block;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        margin: 1rem auto;
    }
</style>
