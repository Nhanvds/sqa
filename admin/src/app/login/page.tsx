"use client";

import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardContent, CardTitle } from "@/components/ui/card";
import { Button } from '@/components/ui/button';
import { useDispatch, useSelector } from 'react-redux';
import { useLazyGetUserInfoQuery } from '@/redux/api/user-api';
import { RootState } from '@/redux/store';
import { setUser } from '@/redux/slice/userSlice';
import { useLoginEmailMutation } from '@/redux/api/auth-api';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';
import { RoleEnum } from '@/types/enums';

const formSchema = z.object({
    email: z.string().email({ message: "Invalid email address." }),
    password: z.string().min(6, { message: "Password must be at least 6 characters." }),
});

type FormSchema = z.infer<typeof formSchema>;

export default function LoginPage() {
    const router = useRouter();
    const [login] = useLoginEmailMutation();
    const dispatch = useDispatch();
    const userInfo = useSelector((state: RootState) => state.user.user);
    const [getUserInfo] = useLazyGetUserInfoQuery();

    const handleGetUserInfo = async () => {
        try {
            const result = await getUserInfo().unwrap();
            if (result?.result) {
                const userRoles = result.result.roles;

                const hasRequiredRole = [RoleEnum.ADMIN, RoleEnum.STAFF, RoleEnum.SHIPPER].some(role =>
                    Array.from(userRoles).includes(role)
                );
                if(hasRequiredRole) {
                    dispatch(setUser(result.result));
                } else {
                    localStorage.removeItem("token");
                    console.error("You are not an admin.");
                }
            }
        } catch (error) {
            console.error("Error getting user info:", error);
        }
    }

    const form = useForm<FormSchema>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            email: '',
            password: '',
        },
    });

    const onSubmit = async (data: FormSchema) => {
        
        try {
            const result = await login({
                email: data.email,
                password: data.password,
            }).unwrap();

            localStorage.setItem("token", result.result?.token ?? "");
            await handleGetUserInfo();
        } catch (error) {
            localStorage.removeItem("token");
            console.error("Login failed:", error);
        }
    };

    useEffect(() => {
        if(userInfo) {
            router.push("/dashboard/overview");
        }
    }, [userInfo]);

    return (
        <Card className="w-full max-w-md mx-auto p-4 mt-10 shadow-lg">
            <CardHeader className="text-center mb-4">
                <CardTitle>Login admin</CardTitle>
            </CardHeader>
            <CardContent>
                <Form {...form}>
                    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                        <FormField
                            control={form.control}
                            name="email"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Email</FormLabel>
                                    <FormControl>
                                        <Input placeholder="you@example.com" {...field} />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />

                        <FormField
                            control={form.control}
                            name="password"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Password</FormLabel>
                                    <FormControl>
                                        <Input type="password" placeholder="******" {...field} />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />

                        <Button type="submit" className="w-full">Submit</Button>
                    </form>
                </Form>
            </CardContent>
        </Card>
    );
}