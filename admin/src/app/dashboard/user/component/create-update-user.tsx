'use client';

import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Controller, useForm } from "react-hook-form";
import PromotionsSelect from "@/app/dashboard/product/component/promotion-select";
import { User } from "@/types/user-info";
import { useCreateUserMutation, useUpdateUserMutation } from "@/redux/api/user-api";
import CustomSelect from "@/components/Select/select";
import { LoyaltyTierEnum, RoleEnum, StatusEnum } from "@/types/enums";
import { CustomAlertProps } from "@/components/ui/CustomAlert";

interface CreateOrUpdateUserProps {
    isOpen: boolean;
    onClose: () => void;
    user?: User;
    setAlert: (alert: CustomAlertProps) => void;
    refetch: () => void;
}

const CreateOrUpdateUser = ({ isOpen, onClose, user, setAlert, refetch }: CreateOrUpdateUserProps) => {
    const { register, handleSubmit, reset, formState: { errors }, control, setValue } = useForm({
        defaultValues: {
            email: user?.email ?? "",
            name: user?.name ?? "",
            status: user?.status ?? "",
            loyaltyTier: user?.loyaltyTier ?? "",
            role: user?.roles?.values().next().value ?? "",
        },
    });

    const [createUser, { isLoading: isCreating }] = useCreateUserMutation();
    const [updateUser, { isLoading: isUpdating }] = useUpdateUserMutation();

    const onSubmit = async (data: any) => {
        debugger;
        try {
            if (user) {
                await updateUser({ id: user.id, user: {
                    ...data,
                    roles: [data.role]
                } }).unwrap();
            } else {
                await createUser({
                    ...data,
                    roles: [data.role]
                }).unwrap();
            }
            setAlert({
                show: true,
                message: user ? "User updated successfully" : "User added successfully",
                variant: "success"
            });
            refetch();
        } catch (error: any) {
            setAlert({
                show: true,
                message: data.name + ": " + (error.data?.message ?? "An error occurred"),
                variant: "destructive"
            });
        } finally {
            reset();
            onClose();
        }
    };

    const handleClose = () => {
        reset();
        onClose();
    }

    return (
        <Dialog open={isOpen} onOpenChange={handleClose} modal>
            <DialogContent className="sm:max-w-[500px]">
                <DialogHeader>
                    <DialogTitle>{user ? `Edit ${user.email}` : "Add User"}</DialogTitle>
                    <DialogDescription>
                        {user ? "Update the user details below." : "Fill in the details to create a new user."}
                    </DialogDescription>
                </DialogHeader>
                <form onSubmit={handleSubmit(onSubmit)} className="grid gap-4 py-4">
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="email" className="text-right">
                            Email <span className="text-red-500">*</span>
                        </Label>
                        <Input
                            id="email"
                            {...register("email", { required: "Email is required", pattern: { value: /^\S+@\S+$/i, message: "Invalid email" } })}
                            className={`col-span-3 ${errors.name ? 'border-red-500' : ''}`}
                        />
                        {errors.name && <p className="col-span-4 text-red-500 text-sm">{errors.name.message}</p>}
                    </div>
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
                        <Label htmlFor="status" className="text-right">
                            status <span className="text-red-500">*</span>
                        </Label>
                        <Controller
                            name="status"
                            control={control}
                            rules={{ required: "Status is required" }}
                            render={({ field: { value, onChange } }) => (
                                <CustomSelect
                                    placeholder="Select Status"
                                    value={value}
                                    onChange={onChange}
                                    options={Object.entries(StatusEnum).map(([key, value]) => ({
                                        label: key,
                                        value: value
                                    }))}
                                />
                            )}
                        />
                        {errors.status && (
                            <p className="col-span-4 text-red-500 text-sm">{errors.status.message}</p>
                        )}
                    </div>

                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="loyaltyTier" className="text-right">
                            Loyalty Tier <span className="text-red-500">*</span>
                        </Label>
                        <Controller
                            name="loyaltyTier"
                            control={control}
                            rules={{ required: "LoyaltyTier is required" }}
                            render={({ field: { value, onChange } }) => (
                                <CustomSelect
                                    placeholder="Select LoyaltyTier"
                                    value={value}
                                    onChange={onChange}
                                    options={Object.entries(LoyaltyTierEnum).map(([key, value]) => ({
                                        label: key,
                                        value: value
                                    }))}
                                />
                            )}
                        />
                        {errors.loyaltyTier && (
                            <p className="col-span-4 text-red-500 text-sm">{errors.loyaltyTier.message}</p>
                        )}
                    </div>

                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="role" className="text-right">
                            Roles <span className="text-red-500">*</span>
                        </Label>
                        <Controller
                            name="role"
                            control={control}
                            rules={{ required: "Role is required" }}
                            render={({ field: { value, onChange } }) => (
                                <CustomSelect
                                    placeholder="Select Role"
                                    value={value}
                                    onChange={onChange}
                                    options={Object.entries(RoleEnum).map(([key, value]) => ({
                                        label: key,
                                        value: value
                                    }))}
                                />
                            )}
                        />
                        {errors.role && (
                            <p className="col-span-4 text-red-500 text-sm">{errors.role.message}</p>
                        )}
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={handleClose} disabled={isCreating || isUpdating}>
                            Cancel
                        </Button>
                        <Button type="submit" disabled={isCreating || isUpdating}>
                            {user ? (isUpdating ? "Saving..." : "Save Changes") : (isCreating ? "Adding..." : "Add User")}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
};

export default CreateOrUpdateUser;