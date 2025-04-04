"use client";

import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { CustomAlertProps } from "@/components/ui/CustomAlert";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useCreateCategoryMutation, useUpdateCategoryMutation } from "@/redux/api/category-api";
import { CategoryResponse } from "@/types/category";
import { StatusEnum } from "@/types/enums";
import { useEffect } from "react";
import { Controller, useForm } from "react-hook-form";

interface CreateOrUpdateCategoryProps {
    isOpen: boolean;
    onClose: () => void;
    category?: CategoryResponse;
    setAlert: (alert: CustomAlertProps) => void;
    refetch: () => void;
}

const CreateOrUpdateCategory = ({ isOpen, onClose, category, setAlert, refetch }: CreateOrUpdateCategoryProps) => {
    const { register, handleSubmit, reset, formState: { errors }, control } = useForm({
        defaultValues: {
            name: category?.name ?? "",
            description: category?.description ?? "",
            status: category?.status ? category.status === StatusEnum.ACTIVE : true,
        },
    });

    const [createCategory, { isLoading: isCreating }] = useCreateCategoryMutation();
    const [updateCategory, { isLoading: isUpdating }] = useUpdateCategoryMutation();

    useEffect(() => {
        if (category) {
            reset({
                name: category.name,
                description: category.description,
                status: category?.status ? category.status === StatusEnum.ACTIVE : true,
            });
        } else {
            reset({
                name: "",
                description: "",
                status: true,
            });
        }
    }, [category, reset]);

    const onSubmit = async (data: any) => {
        try {
            if (category) {
                await updateCategory({ id: category.id, category: {
                    name: data.name,
                    description: data.description,
                    status: data.status ? StatusEnum.ACTIVE : StatusEnum.INACTIVE,
                } }).unwrap().then(() => {
                    setAlert({
                        show: true,
                        variant: "success",
                        message: data.name + " updated successfully",
                    });
                });
            } else {
                await createCategory({
                    name: data.name,
                    description: data.description,
                    status: data.status ? StatusEnum.ACTIVE : StatusEnum.INACTIVE,
                }).unwrap().then(() => {
                    setAlert({
                        show: true,
                        variant: "success",
                        message: data.name + " added successfully",
                    });
                });
            }
            refetch();
            handleClose();
        } catch (error: any) {
            setAlert({
                show: true,
                variant: "destructive",
                message: data.name + ": " + (error.data?.message ?? "An error occurred"),
            })
            handleClose();
        }
    };

    const handleClose = () => {
        reset();
        onClose();
    }

    return (
        <Dialog open={isOpen} onOpenChange={handleClose}>
            <DialogContent className="sm:max-w-[425px]">
                <DialogHeader>
                    <DialogTitle>{category ? "Edit Category" : "Add Category"}</DialogTitle>
                    <DialogDescription>
                        {category ? "Update the category details below." : "Fill in the details to create a new category."}
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
                        <Label htmlFor="status" className="text-right">
                            Status
                        </Label>
                        <Controller
                            name="status"
                            control={control}
                            render={({ field }) => (
                                <Checkbox
                                    id="status"
                                    checked={field.value}
                                    onCheckedChange={field.onChange}
                                    className="mr-2"
                                />
                            )}
                        />
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={handleClose} disabled={isCreating || isUpdating}>
                            Cancel
                        </Button>
                        <Button type="submit" disabled={isCreating || isUpdating}>
                            {category ? (isUpdating ? "Saving..." : "Save Changes") : (isCreating ? "Adding..." : "Add Category")}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
};


export default CreateOrUpdateCategory;