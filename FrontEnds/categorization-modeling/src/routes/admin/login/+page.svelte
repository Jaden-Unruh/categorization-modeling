<script lang="ts">
	let username = '';
	let password = '';
	let error = '';

	async function login() {
		const res = await fetch('/api/auth/login', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({ username, password })
		});

		if (!res.ok) {
			error = 'Login failed';
			return;
		}

		const data = await res.json();
		console.log('Token:', data.token);
		localStorage.setItem('jwt', data.token);

		window.location.href = '/admin';
	}
</script>

<h1>Login</h1>

<form on:submit|preventDefault={login}>
	<label>
		Username:
		<input bind:value={username} required />
	</label>
	<br />
	<label>
		Password:
		<input type="password" bind:value={password} required />
	</label>
	<br />
	<button type="submit">Login</button>
</form>

{#if error}
	<p style="color: red;">{error}</p>
{/if}