"use client";

import { DateTimePicker12h } from "@/components/date-time-picker-24h";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { CustomAlertProps } from "@/components/ui/CustomAlert";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useCreatePromotionMutation, useUpdatePromotionMutation } from "@/redux/api/promotion-api";
import { PromotionResponse } from "@/types/promotion";
import { Controller, useForm } from "react-hook-form";

interface CreateOrUpdatePromotionProps {
    isOpen: boolean;
    onClose: () => void;
    promotion?: PromotionResponse;
    setAlert: (alert: CustomAlertProps) => void;
    refetch: () => void;
}

const CreateOrUpdatePromotion = ({ isOpen, onClose, promotion, setAlert, refetch }: CreateOrUpdatePromotionProps) => {
    const { register, handleSubmit, reset, formState: { errors }, control } = useForm({
        defaultValues: {
            name: promotion?.name ?? "",
            description: promotion?.description ?? "",
            discountPercentage: promotion?.discountPercentage ?? 1,
            startDate: promotion?.startDate ?? "",
            endDate: promotion?.endDate ?? "",
            applyToAll: promotion?.applyToAll ?? false,
            isActive: promotion?.isActive ?? true,
        },
    });

    const [createPromotion, { isLoading: isCreating }] = useCreatePromotionMutation();
    const [updatePromotion, { isLoading: isUpdating }] = useUpdatePromotionMutation();

    const onSubmit = async (data: any) => {
        try {
            if (promotion) {
                await updatePromotion({ id: promotion.id, promotion: data }).unwrap();
            } else {
                await createPromotion(data).unwrap();
            }
            setAlert({ show: true, variant: "success", message: promotion ? "Promotion updated successfully" : "Promotion added successfully" });
            refetch();
            handleClose();
        } catch (error: any) {
            setAlert({ show: true, variant: "destructive", message: error.data?.message ?? "An error occurred" });
            handleClose();
        }
    };

    const handleClose = () => {
        reset();
        onClose();
    }

    return (
        <Dialog open={isOpen} onOpenChange={handleClose} modal>
            <DialogContent className="sm:max-w-[425px]">
                <DialogHeader>
                    <DialogTitle>{promotion ? "Edit Promotion" : "Add Promotion"}</DialogTitle>
                    <DialogDescription>
                        {promotion ? "Update the promotion details below." : "Fill in the details to create a new promotion."}
                    </DialogDescription>
                </DialogHeader>
                <form onSubmit={handleSubmit(onSubmit)} className="grid gap-4 py-4">
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="name" className="text-right">
                            Name <span className="text-red-500">*</span>
                        </Label>
                        <Input
                            id="name"
                            {...register("name", { required: "Name is required" })}
                            className={`col-span-3 ${errors.name ? 'border-red-500' : ''}`}
                        />
                        {errors.name && <p className="col-span-4 text-red-500 text-sm">{errors.name.message}</p>}
                    </div>
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="description" className="text-right">
                            Description
                        </Label>
                        <Input
                            id="description"
                            {...register("description")}
                            className="col-span-3"
                        />
                    </div>
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="discountPercentage" className="text-right">
                            Discount % <span className="text-red-500">*</span>
                        </Label>
                        <Input
                            type="number"
                            min={1}
                            id="discountPercentage"
                            {...register("discountPercentage", {
                                required: "Discount percentage is required",
                                min: {
                                    value: 1,
                                    message: "Discount percentage must be greater than 0"
                                }
                            })}
                            className={`col-span-3 ${errors.discountPercentage ? 'border-red-500' : ''}`}
                        />
                        {errors.discountPercentage && <p className="col-span-4 text-red-500 text-sm">{errors.discountPercentage.message}</p>}
                    </div>
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="startDate" className="text-right">
                            Start Date <span className="text-red-500">*</span>
                        </Label>
                        <Controller
                            name="startDate"
                            control={control}
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
                        <Label htmlFor="endDate" className="text-right">
                            End Date <span className="text-red-500">*</span>
                        </Label>
                        <Controller
                            name="endDate"
                            control={control}
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
                        <Label htmlFor="applyToAll" className="text-right">
                            Apply to All
                        </Label>
                        <Controller
                            name="applyToAll"
                            control={control}
                            render={({ field }) => (
                                <Checkbox
                                    id="applyToAll"
                                    checked={field.value}
                                    onCheckedChange={field.onChange}
                                    className="mr-2"
                                />
                            )}
                        />
                    </div>
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="isActive" className="text-right">
                            Active
                        </Label>
                        <Controller
                            name="isActive"
                            control={control}
                            render={({ field }) => (
                                <Checkbox
                                    id="isActive"
                                    checked={field.value}
                                    onCheckedChange={field.onChange}
                                    className="mr-2"
                                />
                            )}
                        />
                    </div>
                    <DialogFooter>
                        <Button 
                            variant="outline" 
                            onClick={handleClose} 
                            disabled={isCreating || isUpdating}
                        >
                            Cancel
                        </Button>
                        <Button 
                            type="submit" 
                            disabled={isCreating || isUpdating} 
                            className="ml-2"
                        >
                            {promotion ? (isUpdating ? "Saving..." : "Save Changes") : (isCreating ? "Adding..." : "Add Promotion")}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
};

export default CreateOrUpdatePromotion;
