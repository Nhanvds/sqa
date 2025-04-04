"use client";
import { RootState } from '@/redux/store';
import { useRouter } from 'next/router';
import { useSelector } from 'react-redux';

export default function Dashboard() {

  const userInfo = useSelector((state: RootState) => state.user.user);
  debugger;
  const router = useRouter();
  if (!userInfo) {
    return router.push('/login');
  } else {
    return router.push('/dashboard/overview');
  }
}
