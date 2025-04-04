import { ChartData } from '@/app/dashboard/overview/_components/bar-graph';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import React from 'react';

export function RecentSales({data}: {data: ChartData[]}) {

  const dataTable = React.useMemo(() => {
    return Object.entries(
      data.reduce((acc, {name, value}) => {
        if (!acc[name]) {
          acc[name] = 0;
        }
        acc[name] += value;
        return acc;
      }, {} as Record<string, number>)
    )
  }, [data]);

  return (
    dataTable.map(([name, value]) => (
    <div key={name} className="space-y-8">
      <div className="flex items-center">
        <Avatar className="h-9 w-9">
          <AvatarImage src="/avatars/01.png" alt="Avatar" />
            <AvatarFallback>{ name.slice(0, 2)?.toUpperCase() ?? 'CN'}</AvatarFallback>
        </Avatar>
        <div className="ml-4 space-y-1">
          <p className="text-sm font-medium leading-none">{name}</p>
        </div>
        <div className="ml-auto font-medium">+đ {value}</div>
      </div>
    </div>
    ))
  );
}
