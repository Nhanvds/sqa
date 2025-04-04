'use client';

import { useCreateOrderMutation, useUpdateOrderMutation } from '@/redux/api/order-api';
import { OrderResponse, OrderRequest, UpdateOrderRequest } from '@/types/order';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Controller, useForm } from 'react-hook-form';
import { useEffect } from 'react';
import { OrderStatusEnum } from '@/types/enums';
import { useGetShippingAddressByUserIdQuery, useLazyGetShippingAddressByUserIdQuery } from '@/redux/api/shipping-address';
import { useGetUsersQuery } from '@/redux/api/user-api';
import UserSelect from '@/app/dashboard/order/component/user-select';
import ShippingAddressSelect from '@/app/dashboard/order/component/shipping-address-select';
import { useGetCurrentDiscountQuery } from '@/redux/api/discount-api';
import DiscountSelect from '@/app/dashboard/order/component/discount-select';
import { CustomAlertProps } from '@/components/ui/CustomAlert';

interface CreateOrUpdateOrderProps {
    isOpen: boolean;
    onClose: () => void;
    order?: OrderResponse;
    setAlert: (alert: CustomAlertProps) => void;
    refetch: () => void;
}

const CreateOrUpdateOrder = ({ isOpen, onClose, order, setAlert, refetch }: CreateOrUpdateOrderProps) => {
    const { register, handleSubmit, reset, formState: { errors }, control, watch } = useForm({
        defaultValues: {
            userId: order?.user.id || '',
            discountId: order?.userDiscount?.discount.id || '',
            shippingAddressId: order?.shippingAddress.id || '',
            status: order?.status || OrderStatusEnum.PENDING,
        },
    });
    const userSelect = watch("userId");

    const { data: shippingAddresses } = useGetShippingAddressByUserIdQuery(userSelect ?? "", {
        skip: !userSelect,
    });
    const { data: allDiscount } = useGetCurrentDiscountQuery();
    const { data: allUser } = useGetUsersQuery();
    const [createOrder, { isLoading: isCreating }] = useCreateOrderMutation();
    const [updateOrder, { isLoading: isUpdating }] = useUpdateOrderMutation();

    const onSubmit = async (data: any) => {
        try {
            if (order) {
                await updateOrder({ orderId: order.id, order: data }).unwrap();
            } else {
                await createOrder(data).unwrap();
            }
            setAlert({ show: true, variant: "success", message: order ? "Order updated successfully" : "Order created successfully" });
            refetch();
            handleClose();
        } catch (error: any) {
            setAlert({ show: true, variant: "destructive", message: error.message });
            handleClose();
        }
    };

    const handleClose = () => {
        reset();
        onClose();
    };

    return (
        <Dialog open={isOpen} onOpenChange={handleClose} modal>
            <DialogContent className="sm:max-w-[425px]">
                <DialogHeader>
                    <DialogTitle>{order ? 'Edit Order' : 'Create Order'}</DialogTitle>
                </DialogHeader>
                <form onSubmit={handleSubmit(onSubmit)} className="grid gap-4 py-4">
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="productName" className="text-right">
                            User <span className="text-red-500">*</span>
                        </Label>
                        <Controller
                            name="userId"
                            control={control}
                            render={({ field: { value, onChange } }) => (
                                <UserSelect 
                                    value={value}
                                    onChange={onChange}
                                    users={allUser?.result ?? []} />
                            )}
                        />
                        {errors.userId && <p className="col-span-4 text-red-500 text-sm">{errors.userId.message}</p>}
                    </div>

                    {userSelect && (
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="shippingAddressId" className="text-right">
                                Shipping Address <span className="text-red-500">*</span>
                            </Label>
                            <Controller
                                name="shippingAddressId"
                                control={control}
                                render={({ field: { value, onChange } }) => (
                                    <ShippingAddressSelect
                                        value={value}
                                        onChange={onChange}
                                        shippingAddresses={shippingAddresses?.result ?? []} />
                                )}
                            />
                            {errors.shippingAddressId && <p className="col-span-4 text-red-500 text-sm">{errors.shippingAddressId.message}</p>}
                        </div>
                    )}

                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="discountId" className="text-right">
                            Discount
                        </Label>
                        <Controller
                            name="discountId"
                            control={control}
                            render={({ field: { value, onChange } }) => (
                                <DiscountSelect value={value} onChange={onChange} discounts={allDiscount?.result ?? []} />
                            )}
                        />
                    </div>

                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="status" className="text-right">
                            Order Status
                        </Label>
                        <Controller
                            name="status"
                            control={control}
                            render={({ field }) => (
                                <select 
                                    {...field}
                                    className={`col-span-3 px-4 py-3 border rounded-md text-sm focus:outline-primary ${errors.status ? 'border-red-500' : 'border-gray-300'} `}
                                >
                                    <option value={OrderStatusEnum.PENDING}>Pending</option>
                                    <option value={OrderStatusEnum.CONFIRMED}>Confirmed</option>
                                    <option value={OrderStatusEnum.COMPLETED}>Shipped</option>
                                    <option value={OrderStatusEnum.DELIVERED}>Delivered</option>
                                    <option value={OrderStatusEnum.CANCELLED}>Cancelled</option>
                                </select>
                            )}
                        />
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={handleClose} disabled={isCreating || isUpdating}>
                            Cancel
                        </Button>
                        <Button type="submit" disabled={isCreating || isUpdating} className="ml-2">
                            {order ? (isUpdating ? "Saving..." : "Save Changes") : (isCreating ? "Adding..." : "Add Order")}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
};

export default CreateOrUpdateOrder;
