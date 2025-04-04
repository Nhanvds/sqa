"use client";

import { useLazyGetUserInfoQuery } from "@/redux/api/user-api";
import { setUser } from "@/redux/slice/userSlice";
import { RootState } from "@/redux/store";
import { RoleEnum } from "@/types/enums";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";

export default function Home() {
  const router = useRouter();
  const dispatch = useDispatch();
  const userInfo = useSelector((state: RootState) => state.user.user);
  const [getUserInfo] = useLazyGetUserInfoQuery();
  useEffect(() => {
    if (userInfo) {
      router.push('/dashboard/overview');
    }
    else {
      const token = localStorage.getItem('token');
      if (!token) {
        router.push('/login');
      }
      else {
        getUserInfo().unwrap().then((result) => {
          if (result?.result) {
            const userRoles = result.result.roles;

            const hasRequiredRole = [RoleEnum.ADMIN, RoleEnum.STAFF, RoleEnum.SHIPPER].some(role =>
              Array.from(userRoles).includes(role)
            );
            if (hasRequiredRole) {
              dispatch(setUser(result.result));
            } else {
              router.push('/login');
            }
          }
        }).catch((error) => {
          console.error("Error getting user info:", error);
        });
      }
    }
  }, [userInfo]);

  return (
    <div className="text-center text-gray-500">{userInfo?.email}</div>
  )
}
