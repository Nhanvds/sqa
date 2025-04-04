'use client';
import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import { Edit, MoreHorizontal, Trash } from 'lucide-react';
import { OrderResponse } from '@/types/order';
import CreateOrUpdateOrder from '@/app/dashboard/order/component/create-update-order';
import { CustomAlertProps } from '@/components/ui/CustomAlert';

interface CellActionProps {
    data: OrderResponse;
    refetch: () => void;
    setAlert: (alert: CustomAlertProps) => void;
}

export const CellActionOrder: React.FC<CellActionProps> = ({ data, refetch, setAlert }) => {
    const [showUpdateModal, setShowUpdateModal] = useState(false);

    return (
        <>
            <CreateOrUpdateOrder
                isOpen={showUpdateModal}
                onClose={() => setShowUpdateModal(false)}
                order={data}
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
                    <DropdownMenuItem onClick={() => setShowUpdateModal(true)}>
                        <Edit className="mr-2 h-4 w-4" /> Update
                    </DropdownMenuItem>
                </DropdownMenuContent>
            </DropdownMenu>
        </>
    );
};
