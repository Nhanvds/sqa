"use client";

import { useCreateDiscountMutation, useUpdateDiscountMutation } from "@/redux/api/discount-api";
import { DiscountResponse, DiscountRequest } from "@/types/discount";
import { useForm, Controller } from "react-hook-form";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { DateTimePicker12h } from "@/components/date-time-picker-24h";
import { Alert } from "@/components/ui/alert";
import { Select, SelectContent, SelectGroup, SelectItem, SelectLabel, SelectTrigger, SelectValue } from "@/components/ui/select";
import { DiscountTypeEnum } from "@/types/enums";
import { CustomAlertProps } from "@/components/ui/CustomAlert";

interface CreateOrUpdateDiscountProps {
    isOpen: boolean;
    onClose: () => void;
    discount?: DiscountResponse;
    setAlert: (alert: CustomAlertProps) => void;
    refetch: () => void;
}

const CreateOrUpdateDiscount = ({
    isOpen,
    onClose,
    discount,
    setAlert,
    refetch,
}: CreateOrUpdateDiscountProps) => {
    const { register, handleSubmit, reset, control, watch, formState: { errors } } = useForm({
        defaultValues: {
            code: discount?.code ?? "",
            discountType: discount?.discountType ?? "",
            discountPercentage: discount?.discountPercentage ?? "",
            discountValue: discount?.discountValue ?? 0,
            minOrderValue: discount?.minOrderValue ?? 0,
            maxDiscountValue: discount?.maxDiscountValue ?? 0,
            maxUses: discount?.maxUses ?? 1,
            startDate: discount?.startDate ?? "",
            expiryDate: discount?.expiryDate ?? "",
            autoApply: discount?.autoApply ?? false,
        },
    });
    const discountType = watch("discountType");

    const [createDiscount, { isLoading: isCreating }] = useCreateDiscountMutation();
    const [updateDiscount, { isLoading: isUpdating }] = useUpdateDiscountMutation();

    const onSubmit = async (data: any) => {
        try {
            if (discount) {
                await updateDiscount({ id: discount.id, discount: data }).unwrap();
            } else {
                await createDiscount(data).unwrap();
            }
            setAlert({
                show: true,
                variant: "success",
                message: discount ? "Discount updated successfully" : "Discount created successfully",
            });
            refetch();
            handleClose();
        } catch (error: any) {
            setAlert({
                show: true,
                variant: "destructive",
                message: error.data?.message ?? "An error occurred",
            })
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
                    <DialogTitle>{discount ? "Edit Discount" : "Add Discount"}</DialogTitle>
                    <DialogDescription>
                        {discount ? "Update the discount details below." : "Fill in the details to create a new discount."}
                    </DialogDescription>
                </DialogHeader>
                <form onSubmit={handleSubmit(onSubmit)} className="grid gap-4 py-4">
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="code" className="text-right">
                            Code <span className="text-red-500">*</span>
                        </Label>
                        <Input
                            id="code"
                            {...register("code", { required: "Code is required" })}
                            className={`col-span-3 ${errors.code ? 'border-red-500' : ''}`}
                        />
                        {errors.code && <p className="col-span-4 text-red-500 text-sm">{errors.code.message}</p>}
                    </div>
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="discountType" className="text-right">
                            Discount Type <span className="text-red-500">*</span>
                        </Label>
                        <Controller
                            name="discountType"
                            control={control}
                            rules={{ required: "Discount type is required" }}
                            render={({ field }) => (
                                <select
                                    {...field}
                                    className={`col-span-3 px-4 py-3 border rounded-md text-sm focus:outline-primary ${errors.discountType ? 'border-red-500' : 'border-gray-300'} `}
                                >
                                    <option value="">Select Option</option>
                                    <option value={DiscountTypeEnum.PERCENTAGE}>Percentage</option>
                                    <option value={DiscountTypeEnum.VALUE}>Value</option>
                                </select>
                            )}
                        />
                        {errors.discountType && <p className="col-span-4 text-red-500 text-sm">{errors.discountType.message}</p>}
                    </div>
                    {discountType === DiscountTypeEnum.PERCENTAGE && (
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="discountPercentage" className="text-right">
                                Discount Percentage <span className="text-red-500">*</span>
                            </Label>
                            <Input
                                id="discountPercentage"
                                type="number"
                                {...register("discountPercentage", {
                                    required: "Discount percentage is required",
                                    min: { value: 0, message: "Minimum value is 0" },
                                    max: { value: 100, message: "Maximum value is 100" }
                                })}
                                className={`col-span-3 ${errors.discountPercentage ? 'border-red-500' : ''}`}
                            />
                            {errors.discountPercentage && <p className="col-span-4 text-red-500 text-sm">{errors.discountPercentage.message}</p>}
                        </div>
                    )}

                    {discountType === DiscountTypeEnum.VALUE && (
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="discountValue" className="text-right">
                                Discount Value <span className="text-red-500">*</span>
                            </Label>
                            <Input
                                id="discountValue"
                                type="number"
                                {...register("maxDiscountValue", { required: "Discount value is required" })}
                                className={`col-span-3 ${errors.discountValue ? 'border-red-500' : ''}`}
                            />
                            {errors.discountValue && <p className="col-span-4 text-red-500 text-sm">{errors.discountValue.message}</p>}
                        </div>
                    )}
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="minOrderValue" className="text-right">
                            Min Order Value
                        </Label>
                        <Input
                            type="number"
                            id="minOrderValue"
                            {...register("minOrderValue", { required: "Min order value is required" })}
                            className="col-span-3"
                        />
                        {errors.minOrderValue && (
                            <p className="col-span-4 text-red-500 text-sm">
                                {errors.minOrderValue.message}
                            </p>
                        )}
                    </div>
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="maxDiscountValue" className="text-right">
                            Max Discount Value
                        </Label>
                        <Input
                            type="number"
                            id="maxDiscountValue"
                            {...register("maxDiscountValue", { required: "Max discount value is required" })}
                            className="col-span-3"
                        />
                        {errors.maxDiscountValue && (
                            <p className="col-span-4 text-red-500 text-sm">
                                {errors.maxDiscountValue.message}
                            </p>
                        )}
                    </div>
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="startDate" className="text-right">
                            Start Date
                        </Label>
                        <Controller
                            control={control}
                            name="startDate"
                            render={({ field }) => (
                                <div className="col-span-3">
                                    <DateTimePicker12h
                                        value={field.value ? new Date(field.value) : undefined}
                                        onChange={field.onChange}
                                    />
                                </div>
                            )}
                        />
                    </div>
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="expiryDate" className="text-right">
                            Expiry Date
                        </Label>
                        <Controller
                            control={control}
                            name="expiryDate"
                            render={({ field }) => (
                                <div className="col-span-3">
                                    <DateTimePicker12h
                                        value={field.value ? new Date(field.value) : undefined}
                                        onChange={field.onChange}
                                    />
                                </div>
                            )}
                        />
                    </div>
                    <DialogFooter>
                        <Button
                            variant="secondary"
                            onClick={handleClose}
                            disabled={isCreating || isUpdating}
                        >
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            className="ml-2"
                            disabled={isCreating || isUpdating}
                        >
                            {discount ? "Update" : "Create"}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
};

export default CreateOrUpdateDiscount;
