'use client';

import CreateOrUpdateUser from '@/app/dashboard/user/component/create-update-user';
import { AlertModal } from '@/components/modal/alert-modal';
import { Button } from '@/components/ui/button';
import { CustomAlertProps } from '@/components/ui/CustomAlert';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuTrigger
} from '@/components/ui/dropdown-menu';
import { useDeleteUserMutation } from '@/redux/api/user-api';
import { User } from '@/types/user-info';
import { Edit, MoreHorizontal, Trash } from 'lucide-react';
import { useState } from 'react';

interface CellActionProps {
    data: User;
    refetch: () => void;
    setAlert: (alert: CustomAlertProps) => void;
}

export const CellActionUser: React.FC<CellActionProps> = ({ data, refetch, setAlert }) => {
    const [deletePromotion, { isLoading: isDeleting }] = useDeleteUserMutation();
    const [showUpdateModal, setShowUpdateModal] = useState(false);
    const [open, setOpen] = useState(false);

    const onConfirm = () => {
        deletePromotion(data.id).unwrap()
            .then(() => {
                setAlert({
                    show: true,
                    message: "User deleted successfully",
                    variant: "success"
                });
                setOpen(false);
                refetch();
            }).catch((error) => {
                setAlert({
                    show: true,
                    message: error.data?.message ?? "An error occurred",
                    variant: "destructive"
                });
                setOpen(false);
            });
    };

    return (
        <>
            <AlertModal
                isOpen={open}
                onClose={() => setOpen(false)}
                onConfirm={onConfirm}
                loading={isDeleting}
                title={`Delete ${data.name}`}
                description='Are you sure you want to delete this user?'
            />
            <CreateOrUpdateUser
                isOpen={showUpdateModal}
                onClose={() => setShowUpdateModal(false)}
                user={data}
                setAlert={setAlert}
                refetch={refetch}
            />
            <DropdownMenu modal={false}>
                <DropdownMenuTrigger asChild>
                    <Button variant="ghost" className="h-8 w-8 p-0">
                        <span className="sr-only">Open menu</span>
                        <MoreHorizontal className="h-4 w-4" />
                    </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                    <DropdownMenuLabel>Actions</DropdownMenuLabel>

                    <DropdownMenuItem
                        onClick={() => setShowUpdateModal(true)}
                    >
                        <Edit className="mr-2 h-4 w-4" /> Update
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => setOpen(true)}>
                        <Trash className="mr-2 h-4 w-4" /> Delete
                    </DropdownMenuItem>
                </DropdownMenuContent>
            </DropdownMenu>
        </>
    );
};
