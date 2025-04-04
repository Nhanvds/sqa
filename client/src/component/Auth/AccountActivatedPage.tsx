import React, { useEffect, useRef, useState } from 'react';
import { Result, Button, Typography, Spin } from 'antd';
import { CheckCircleTwoTone, CloseCircleTwoTone } from '@ant-design/icons';
import { useLocation, useNavigate } from 'react-router-dom';
import { useVerifyEmailMutation } from '../../redux/api/auth-api';

const AccountActivatedPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [activatedAccount] = useVerifyEmailMutation();
    const queryParams = new URLSearchParams(location.search);
    const token = queryParams.get('token');
    const hasActivatedRef = useRef<boolean>(false);

    const handleLoginRedirect = () => {
        navigate('/account/login');
    };

    useEffect(() => {
        if (token && !hasActivatedRef.current) {
            hasActivatedRef.current = true;
            activatedAccount(token)
                .unwrap()
                .then(() => {
                    setLoading(false);
                })
                .catch((error) => {
                    setLoading(false);
                    setError(error.data.message);
                });
        } else if (!token) {
            setError('Invalid token. Please try again.');
            setLoading(false);
        }
    }, [token]);

    const handleRetry = () => {
        navigate('/auth/verify');
    };

    if (loading) {
        return (
            <div className="flex justify-center w-100">
                <Spin size="large" />
            </div>
        )
    }

    if (!!error) {
        return (
            <Result
                status="error"
                title="Account Activation Failed"
                subTitle={error}
                icon={<CloseCircleTwoTone twoToneColor="#ff4d4f" style={{ fontSize: '64px' }} />}
                extra={[
                    <Button type="primary" key="retry" onClick={handleRetry}>
                        Retry
                    </Button>,
                    <Button key="home" onClick={() => navigate('/')}>
                        Go Home
                    </Button>,
                ]}
            />
        );
    }

    return (
        <Result
            status="success"
            title="Account Activated Successfully!"
            subTitle="Your account has been activated successfully. You can now log in to access all features."
            icon={<CheckCircleTwoTone twoToneColor="#52c41a" style={{ fontSize: '64px' }} />}
            extra={[
                <Button type="primary" key="login" onClick={handleLoginRedirect}>
                    Go to Login
                </Button>,
                <Button key="home" onClick={() => navigate('/')}>
                    Go Home
                </Button>,
            ]}
        />
    );
};

export default AccountActivatedPage;