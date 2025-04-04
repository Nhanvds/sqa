import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Input, Button, List, Space, Typography, Popover, Avatar, Image } from 'antd';
import { useGetChatRoomQuery, useSendMessageMutation } from '../../redux/api/chat-api';
import { useSelector } from 'react-redux';
import { RootState } from '../../redux/store';
import { useWebSocket } from '../../hook/useWebSocket ';
import { MessageOutlined, SendOutlined, UserOutlined, CloseOutlined } from '@ant-design/icons';
import "../../sass/scroll.scss"
import { UserInfo } from '../../types/user-info';
import { RoleEnum } from '../../types/enums';
import bot from '../../img/bot.png';

const { Text } = Typography;

interface Message {
    sender: string;
    content: string;
    timestamp: string;
}

const ChatWidget: React.FC = () => {
    const [newMessage, setNewMessage] = useState<string>('');
    const userInfo = useSelector((state: RootState) => state.user.user);
    const { data: getChatRoom, refetch } = useGetChatRoomQuery(userInfo?.id ?? "", {
        skip: !userInfo,
    });
    const [sendMessage, { isLoading }] = useSendMessageMutation();
    const messages = getChatRoom?.result?.messages ?? [];
    const [visibleMessageId, setVisibleMessageId] = useState<string | null>(null);

    const [popoverVisible, setPopoverVisible] = useState(false);

    const handleSubscription = useCallback((message: any) => {
        refetch();
    }, [refetch]);

    const chatListRef = useRef<HTMLDivElement | null>(null);

    useEffect(() => {
        if (chatListRef.current) {
            chatListRef.current.scrollTop = chatListRef.current.scrollHeight;
        }
    }, [messages]);

    useWebSocket(getChatRoom?.result?.id ?? "", handleSubscription);

    const handleSendMessage = async () => {
        if (newMessage.trim()) {
            await sendMessage({ chatRoomId: getChatRoom?.result?.id ?? "", content: newMessage });
            setNewMessage('');
        }
    };

    const checkChatBot = (user: UserInfo) => {
        const rolesSet = new Set(user.roles);
        return rolesSet.has(RoleEnum.CHATBOT);
    }

    const chatContent = (
        <div className='flex flex-column justify-space-between'>
            <List
                ref={chatListRef}
                dataSource={messages}
                renderItem={(message, index) => (
                    <List.Item key={index} className="border-none" style={{ padding: '4px 8px' }}>
                        <Space className={`flex w-100 ${message.sender.id === userInfo?.id ? 'justify-end' : 'justify-start'}`}>
                            {message.sender.id !== userInfo?.id && (
                                <Avatar size="small" icon={!checkChatBot(message.sender) ? <UserOutlined /> : <Image src={bot} alt='bot' />} />
                            )}
                            <div
                                style={{
                                    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
                                    wordBreak: 'break-word',
                                }}
                                className={`p-1 border-radius-8 max-w-280 ${message.sender.id === userInfo?.id ? 'background-primary text-white' : 'background-card'}`}
                                onClick={() => setVisibleMessageId(visibleMessageId === message.id ? null : message.id)}
                            >
                                <span>{message.content}</span>
                            </div>
                            {visibleMessageId === message.id && (
                                <div className="fs-10 text-color-secondary mt-1">
                                    {new Date(message.timestamp).toLocaleString()}
                                </div>
                            )}
                        </Space>
                    </List.Item>
                )}
                className='scrollbar-thin scrollbar-thumb-gray-400 scrollbar-track-gray-100 hover:scrollbar-thumb-gray-500 scrollbar-rounded-md height-380 overflow-y-scroll mb-2'
            />

            <div className='w-100 flex gap-1'>
                <Input
                    placeholder="Type your message..."
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    onPressEnter={handleSendMessage}
                />
                <Button type="primary" onClick={handleSendMessage} loading={isLoading}>
                    <SendOutlined size={16} />
                </Button>
            </div>
        </div>
    );

    return (
        <div style={{ position: 'absolute', bottom: 20, right: 20 }}>
            <Popover
                content={chatContent}
                title={
                    <Space>
                        <MessageOutlined />
                        <Text strong>Chat</Text>
                        <Button
                            type="text"
                            icon={<CloseOutlined />}
                            onClick={() => setPopoverVisible(false)}
                            className='absolute top-1 right-1'
                        />
                    </Space>
                }
                trigger="click"
                placement="topRight"
                open={popoverVisible}
                overlayStyle={{
                    boxShadow: '0 4px 8px rgba(0, 0, 0, 0.2)',
                    width: '400px',
                }}
                className='border-radius-8'
            >
                <Button
                    type="primary"
                    icon={<MessageOutlined />}
                    shape="circle"
                    size="large"
                    style={{
                        position: 'fixed',
                        bottom: '20px',
                        right: '20px',
                        borderRadius: '50%',
                        padding: '0',
                        width: '50px',
                        height: '50px',
                        boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
                    }}
                    onClick={() => setPopoverVisible(true)}
                />
            </Popover>
        </div>
    );
};

export default ChatWidget;
