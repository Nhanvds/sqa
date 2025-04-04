"use client";

import { FC, useEffect, useState } from 'react';
import SidebarUser from '@/app/dashboard/chat/component/sidebar-user';
import ChatWindow from '@/app/dashboard/chat/component/chat-window';
import { useGetUsersQuery } from '@/redux/api/user-api';
import { User } from '@/types/user-info';


const ChatApp: FC = () => {
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const { data: usersData } = useGetUsersQuery();
    const [searchValue, setSearchValue] = useState('');
    const [filteredUsers, setFilteredUsers] = useState(usersData?.result ?? []);

    useEffect(() => {
        if (usersData?.result) {
            setFilteredUsers(usersData.result.filter((user) => user.email.toLowerCase().includes(searchValue.toLowerCase())));
        }
    }, [usersData, searchValue]);

    return (
        <div className="flex h-full">
            <SidebarUser users={filteredUsers} onSelectUser={setSelectedUser} setSearchValue={setSearchValue} selectedUser={selectedUser}/>
            <div className="flex-1 flex flex-col ml-6">
                {selectedUser && (
                    <ChatWindow selectedUser={selectedUser} />
                )}
            </div>
        </div>
    );
};

export default ChatApp;
