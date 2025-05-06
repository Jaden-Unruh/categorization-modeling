<script lang="ts">
    import { onMount } from 'svelte';
    import { Client } from '@stomp/stompjs'
    import SockJS from 'sockjs-client';

    let info = "Loading...";
    interface AdminStats {
        firstName: string;
        lastName: string;
        email: string;
        pronouns: string;
        currentImageNum: number;
        currentImage: string;
        modelCorrect: number;
        prediction: number;
    }

    let stats: Record<string, AdminStats> = {};

    onMount(async () => {
        const token = localStorage.getItem('jwt');

        if (!token) {
            info = "You must be logged in to view admin data.";
            return;
        }

        const res = await fetch('/api/admin/stats', {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });

        if (!res.ok) {
            info = "Failed to fetch admin data. Please check your token or try again later.";
        }

        stats = await res.json();
        info = "Welcome, admin.";

        const socket = new SockJS(`/ws?token=${token}`);
        const stompClient = new Client({
            webSocketFactory: () => socket,
            debug: str => console.log(str),
            reconnectDelay: 5000,
        });

        stompClient.onConnect = (frame) => {
            console.log('Connected to WebSocket:', frame);
            stompClient.subscribe('/topic/adminStats', message => {
                console.log('Received message:', message.body);
            });
        };

        stompClient.onStompError = (frame) => {
            console.error('Broker reported error:', frame.headers['message']);
            console.error('Additional details:', frame.body);
        };

        stompClient.activate();
    });
</script>

<h1>Admin Dashboard</h1>
<p>{info}</p>
<table>
    <thead>
        <tr>
            <th>Session ID</th>
            <th>First Name</th>
            <th>Last Name</th>
            <th>Email</th>
            <th>Pronouns</th>
            <th>Trial Number</th>
            <th>Image</th>
            <th>Num Correctly Predicted</th>
            <th>Current Prediction</th>
        </tr>
    </thead>
    <tbody>
        {#each Object.entries(stats) as [sessionId, data]}
            <tr>
                <td>{sessionId}</td>
                <td>{data.firstName}</td>
                <td>{data.lastName}</td>
                <td>{data.email}</td>
                <td>{data.pronouns}</td>
                <td>{data.currentImageNum}</td>
                <td>{data.currentImage}</td>
                <td>{data.modelCorrect}</td>
                <td>{data.prediction}</td>
            </tr>
        {/each}
    </tbody>
</table>

<style>
    table {
        width: 100%;
        border-collapse: collapse;
    }

    th, td {
        border: 1px solid #ccc;
        padding: 0.5rem;
        text-align: left;
    }
</style>