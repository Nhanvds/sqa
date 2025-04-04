import {notification} from 'antd';
import React from 'react';
import {
    CheckCircleOutlined,
    CloseCircleOutlined,
    InfoCircleOutlined,
    ExclamationCircleOutlined, CloseOutlined
} from '@ant-design/icons';
import '../sass/notification.scss';

export interface NotificationProps {
    message: string;
    type: "success" | "error" | "info" | "warning";
    duration?: number;
    showProgressBar?: boolean;
    className?: string;
}

const notificationStyles = {
    success: {
        color: '#52c41a',
        icon: React.createElement(CheckCircleOutlined, {style: {color: '#52c41a'}}),
        backgroundColor: '#f6ffed',
    },
    error: {
        color: '#ff4d4f',
        icon: React.createElement(CloseCircleOutlined, {style: {color: '#ff4d4f'}}),
        backgroundColor: '#fff1f0',
    },
    info: {
        color: '#1890ff',
        icon: React.createElement(InfoCircleOutlined, {style: {color: '#1890ff'}}),
        backgroundColor: '#e6f7ff',
    },
    warning: {
        color: '#faad14',
        icon: React.createElement(ExclamationCircleOutlined, {style: {color: '#faad14'}}),
        backgroundColor: '#fffbe6',
    }
};

export const showCustomNotification = ({
                                           type,
                                           showProgressBar = true,
                                           duration,
                                           message,
                                           className
                                       }: NotificationProps) => {
    const {color, icon, backgroundColor} = notificationStyles[type];

    notification.open({
        message: <span style={{color: color, fontWeight: 500}}>{message}</span>,
        placement: 'topRight',
        duration: duration ?? 2,
        style: {
            width: 400,
            border: `1px solid ${color}`,
            backgroundColor,
        },
        icon,
        className: `${className || ''} ${showProgressBar ? 'notification-progress' : ''}`,
        closeIcon: <CloseOutlined style={{
            color
        }}/>
    });
};