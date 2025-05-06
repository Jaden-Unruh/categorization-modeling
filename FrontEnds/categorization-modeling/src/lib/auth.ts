import { jwtDecode } from 'jwt-decode';

export function isTokenValid(): boolean {
    const token = localStorage.getItem('jwt');
    if (!token) return false;

    try {
        const decoded: { exp: number } = jwtDecode(token);
        const now = Math.floor(Date.now() / 1000);
        return decoded.exp > now;
    } catch {
        return false;
    }
}