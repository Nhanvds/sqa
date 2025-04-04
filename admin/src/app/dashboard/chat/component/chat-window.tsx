"use client";
import { FC, useState, useEffect, useRef, useCallback, useMemo } from 'react';
import { MessageResponse } from '@/types/message';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { useWebSocket } from '@/hooks/use-websocket';
import { useGetChatRoomQuery, useJoinChatRoomMutation, useLeaveChatRoomMutation, useSendMessageMutation } from '@/redux/api/chat-api';
import { Loader2, LogInIcon, LogOutIcon, Send } from 'lucide-react';
import "@/css/scroll.css";
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { User } from '@/types/user-info';
import { RoleEnum } from '@/types/enums';
import { useSelector } from 'react-redux';
import { RootState } from '@/redux/store';
import { Spinner } from '@/components/spinner';
import { CustomAlert, CustomAlertProps } from '@/components/ui/CustomAlert';

interface ChatWindowProps {
    selectedUser: User;
}

const ChatWindow: FC<ChatWindowProps> = ({ selectedUser }) => {
    const [message, setMessage] = useState('');
    const [messages, setMessages] = useState<MessageResponse[]>([]);
    const messageEndRef = useRef<HTMLDivElement>(null);
    const [visibleMessageId, setVisibleMessageId] = useState<string | null>(null);
    const [userInChatRoom, setUserInChatRoom] = useState<User[]>([]);
    const userInfo = useSelector((state: RootState) => state.user.user);

    const { data: chatRoomData } = useGetChatRoomQuery(selectedUser.id, {
        skip: !selectedUser.id,
    })
    const [sendMessage, { isLoading: isSending }] = useSendMessageMutation();
    const [joinChatRoom, { isLoading: isJoining }] = useJoinChatRoomMutation();
    const [leaveChatRoom, { isLoading: isLeaving }] = useLeaveChatRoomMutation();
    debugger;

    const [alert, setAlert] = useState<CustomAlertProps>({
            variant: "default",
            message: "",
        });

    useEffect(() => {
        if (chatRoomData?.result?.userInChatRoom) {
            setUserInChatRoom(chatRoomData.result.userInChatRoom);
        }
    }, [chatRoomData]);

    const handleSendMessage = () => {
        if (message && selectedUser.id) {
            sendMessage({
                chatRoomId: chatRoomData?.result?.id ?? "",
                content: message,
            }).unwrap()
                .then(() => setMessage(''))
                .catch((error) => console.error("Error sending message:", error));
        }
    };


    const handleSubscription = useCallback((message: MessageResponse) => {
        setMessages((prevMessages) => [...prevMessages, message]);
    }, [setMessages]);

    const { checkMeInChatRoom, checkStaffOrAdminInChat } = {
            checkMeInChatRoom: userInChatRoom.some((user) => user.id === userInfo?.id),
            checkStaffOrAdminInChat: userInChatRoom.some((user) => user.roles.includes(RoleEnum.STAFF) || user.roles.includes(RoleEnum.ADMIN)),
        }

    const handleJoinChatRoom = () => {
        void joinChatRoom(chatRoomData?.result?.id ?? "")
            .unwrap()
            .then((res) => {
                setUserInChatRoom((prevUserInChatRoom) => [...prevUserInChatRoom, userInfo as User]);;
                setAlert({ variant: "success", message: res.message, show: true });
            })
            .catch((error) => setAlert({ variant: "destructive", message: error.data?.message ?? "An error occurred" }));
    };

    const handleLeaveChatRoom = () => {
        leaveChatRoom(chatRoomData?.result?.id ?? "")
            .unwrap()
            .then((res) => {
                setUserInChatRoom((prevUserInChatRoom) => prevUserInChatRoom.filter((user) => user.id !== userInfo?.id));
                setAlert({ variant: "success", message: res.message, show: true });
            })
            .catch((error) => setAlert({ variant: "destructive", message: error.data?.message ?? "An error occurred" }));
    };

    useWebSocket(chatRoomData?.result?.id ?? "", handleSubscription);

    useEffect(() => {
        if (messageEndRef.current) {
            messageEndRef.current.scrollIntoView({ behavior: 'smooth' });
        }
    }, [messages]);

    useEffect(() => {
        if (chatRoomData?.result?.messages) {
            setMessages(chatRoomData.result.messages);
        }
    }, [chatRoomData]);

    return (
        <div className="flex flex-col flex-1 p-4 rounded-lg bg-white h-full">
            <CustomAlert {...alert} onClose={() => setAlert({ variant: "default", message: "", show: false })} />
            <div className="text-lg font-bold flex items-center justify-between mb-6">
                <div>
                    {selectedUser.name} <br />
                    <span className='text-sm text-gray-500'>
                        {checkMeInChatRoom ? (
                            checkStaffOrAdminInChat ? (
                                <>
                                    <span className="text-green-500 text-xs">✅</span> You have joined the chat room. You can now send messages.
                                </>
                            ) : (
                                <>
                                    <span className="text-red-500 text-xs">⛔</span> Your bot is blocked because no staff or admin is present in the chat room.
                                </>
                            )
                        ) : (
                            <>
                                <span className="text-yellow-500 text-xs">⚠️</span> Please join the chat room to send messages.
                            </>
                        )}
                    </span>
                </div>
                <div>
                    {(isJoining || isLeaving) ? (
                        <Spinner size={'small'} className='ml-4'/>
                    ) : !checkMeInChatRoom ? (
                        <Button onClick={handleJoinChatRoom} variant="link">
                            <LogInIcon size={20} />
                        </Button>
                    ) : (
                        <Button onClick={handleLeaveChatRoom} variant="link">
                            <LogOutIcon size={20} />
                        </Button>
                    )}
                </div>
            </div>
            <div className="flex-1 max-h-[700px] overflow-y-auto scrollbar-thin scrollbar-thumb-gray-400 scrollbar-track-gray-100 hover:scrollbar-thumb-gray-500 scrollbar-rounded-md mb-4 space-y-2 px-2">
                {messages.map((msg) => (
                    <div key={msg.id} className={`flex flex-col ${msg.sender.id === selectedUser.id ? 'items-start' : 'items-end'}`}>
                        <div className="flex items-center gap-2">
                            {msg.sender.id === selectedUser.id && (
                                <Avatar className="w-10 h-10 inline-block">
                                    <AvatarFallback>{selectedUser?.name.slice(0, 2)?.toUpperCase() ?? 'CN'}</AvatarFallback>
                                </Avatar>
                            )}
                            <Button onClick={() => setVisibleMessageId(visibleMessageId === msg.id ? null : msg.id)}
                                className={`px-4 rounded-lg max-w-xs h-auto text-white ${msg.sender.id === selectedUser.id ? 'bg-orange-300' : 'bg-primary'}`}
                            >
                                {msg.content}
                            </Button>
                        </div>
                        {visibleMessageId === msg.id && (
                            <div className="block text-xs text-gray-500 mt-1 text-end">
                                {new Date(msg.timestamp).toLocaleString()} <br />
                                {(msg.sender.id === selectedUser.id ? 'You' : msg.sender.name) + " sent this message"}
                            </div>
                        )}
                    </div>
                ))}
                <div ref={messageEndRef}></div>
            </div>
            {checkMeInChatRoom && (
                <div className="flex items-center space-x-4 mt-auto">
                    <Input
                        value={message}
                        onChange={(e) => setMessage(e.target.value)}
                        placeholder="Type a message..."
                        className="flex-1 p-3 rounded-lg"
                        onKeyDown={(e) => {
                            if (e.key === 'Enter' && !e.shiftKey) {
                                e.preventDefault();
                                handleSendMessage();
                            }
                        }}
                    />

                    <Button onClick={handleSendMessage} disabled={isSending} className="p-3 rounded-lg">
                        {isSending ? <Loader2 className="animate-spin" /> : <Send size={20} />}
                    </Button>

                </div>
            )}
        </div>

    );
};

export default ChatWindow;
