"use client";

import { FC, useState } from 'react';
import { User } from '@/types/user-info';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import DebouncedInput from '@/components/debounce-input/debounce-input';

interface SidebarUserProps {
    users: User[];
    onSelectUser: (user: User) => void;
    setSearchValue: (value: string) => void;
    selectedUser: User | null;
}

const SidebarUser: FC<SidebarUserProps> = ({ users, onSelectUser, setSearchValue, selectedUser }) => {
    return (
        <div className="flex flex-col w-96 p-4 rounded-lg h-ful">
            <h2 className="text-xl font-semibold mb-4">Users</h2>
            <DebouncedInput
                value={""}
                onChange={setSearchValue}
                placeholder="Search users by email"
                className='w-full h-10 mb-4 text-primary'
            />
            <div className="space-y-2">
                {users.map((user) => (
                    <Button
                        key={user.id}
                        className={`${user.id === selectedUser?.id ? "bg-secondary" : "bg-white"} w-full h-14 flex justify-start px-1 py-2 rounded-lg hover:bg-secondary text-primary border-none shadow-none`}
                        onClick={() => onSelectUser(user)}
                    >
                            <Avatar className="w-10 h-10 inline-block">
                                <AvatarFallback>{user?.name.slice(0, 2)?.toUpperCase() ?? 'CN'}</AvatarFallback>
                            </Avatar>
                            <div className="ml-3 flex flex-col items-start">
                                <span>{user.name}</span>
                            <span className="text-xs text-muted-foreground">{user.email}</span>
                            </div>
                    </Button>
                ))}
            </div>
        </div>
    );
};

export default SidebarUser;
