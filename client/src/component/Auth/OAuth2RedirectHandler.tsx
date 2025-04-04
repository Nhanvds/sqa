import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const OAuth2RedirectHandler = () => {
    const navigate = useNavigate();

    useEffect(() => {
        const token = document.cookie.split('; ').find(row => row.startsWith('authToken='))
            ?.split('=')[1];
        if (token) {
            localStorage.setItem('token', token);
            navigate('/account');
        } else {
            navigate('/account/login');
        }
    }, [navigate]);

    return null;
};

export default OAuth2RedirectHandler;
