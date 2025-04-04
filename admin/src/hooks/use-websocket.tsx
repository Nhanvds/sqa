import { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export const useWebSocket = (chatRoomId: string, onMessageReceived: (message: any) => void) => {
    const [client, setClient] = useState<Client | null>(null);

    useEffect(() => {
        const token = localStorage.getItem("token");
        const stompClient = new Client({
            brokerURL: 'ws://localhost:8080/chat',
            connectHeaders: {
                Authorization: `Bearer ${token}`
            },
            webSocketFactory: () => {
                return new SockJS('http://localhost:8080/chat');
            },
            debug: (str) => {
                console.log(str);
            },
            onConnect: () => {
                stompClient.subscribe(`/topic/chatRoom/${chatRoomId}`, (messageOutput) => {

                    const message = JSON.parse(messageOutput.body);
                    onMessageReceived(message);
                });
            },
            onDisconnect: () => {
                console.log("Disconnected from WebSocket");
            }
        });

        setClient(stompClient);
        stompClient.activate();

        return () => {
            stompClient.deactivate();
        };
    }, [chatRoomId, onMessageReceived]);

    return client;
};
