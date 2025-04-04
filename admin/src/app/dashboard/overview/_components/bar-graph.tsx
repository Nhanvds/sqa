'use client';

import * as React from 'react';
import { Bar, BarChart, CartesianGrid, XAxis } from 'recharts';

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle
} from '@/components/ui/card';
import {
  ChartConfig,
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent
} from '@/components/ui/chart';

export const description = 'An interactive bar chart';

export interface ChartData {
  label : string
  name : string
  value : number
}

type TransformedData = {
  label: string;
  [key: string]: number | string;
};

const transformData = (data: ChartData[]): TransformedData[] => {
  const groupedData: { [key: string]: { [key: string]: number } } = {};

  data.forEach(({ label, value, name }) => {
    if (!groupedData[label]) {
      groupedData[label] = {};
    }
    if (!groupedData[label][name]) {
      groupedData[label][name] = 0;
    }
    groupedData[label][name] += value;
  });

  return Object.entries(groupedData).map(([label, names]) => ({
    label,
    ...names,
  }));
};

export function BarGraph({chartData} : {chartData : ChartData[]}) {
  const color = ["hsl(var(--chart-1))", "hsl(var(--chart-2))", "hsl(var(--chart-3))", "hsl(var(--chart-4))", "hsl(var(--chart-5))"]
  const nameSet = React.useMemo(() => new Set(chartData.map((data) => data.name)), [chartData])

  const chartConfig = React.useMemo(
    () =>
      Array.from(nameSet).reduce((acc, name, index) => ({
        ...acc,
        [name]: {
          label: name,
          color: color[index % color.length]
        }
      }), {} as Record<string, { label: string; color: string }>),
    [nameSet]
  ) satisfies ChartConfig;

  const data = React.useMemo(() => transformData(chartData), [chartData]);

  const total = React.useMemo(
    () => 
      Array.from(nameSet).reduce((acc, name) => ({
        ...acc,
        [name]: chartData
          .filter((data) => data.name === name)
          .reduce((sum, data) => sum + data.value, 0)
      }), {} as Record<string, number>),
    [nameSet]
  );

  return (
    <Card>
      <CardHeader className="flex flex-col items-stretch space-y-0 border-b p-0 sm:flex-row">
        <div className="flex flex-1 flex-col justify-center gap-1 px-6 py-5 sm:py-6">
          <CardTitle>Bar Chart - Statistics</CardTitle>
        </div>
        <div className="flex">
          {Array.from(nameSet).map((key) => {
            const chart = key as keyof typeof chartConfig;
            return (
              <button
                key={chart}
                className="relative flex flex-1 flex-col justify-center gap-1 border-t px-6 py-4 text-left even:border-l data-[active=true]:bg-muted/50 sm:border-l sm:border-t-0 sm:px-8 sm:py-6"
              >
                <span className="text-xs text-muted-foreground">
                  {chartConfig[chart].label}
                </span>
                <span className="text-lg font-bold leading-none sm:text-3xl">
                  {total[key as keyof typeof total].toLocaleString()}
                </span>
              </button>
            );
          })}
        </div>
      </CardHeader>
      <CardContent className="px-2 sm:p-6">
        <ChartContainer config={chartConfig}>
          <BarChart accessibilityLayer data={data}>
            <CartesianGrid vertical={false} />
            <XAxis
              dataKey="label"
              tickLine={false}
              tickMargin={10}
              axisLine={false}
            />
            <ChartTooltip
              cursor={false}
              content={<ChartTooltipContent indicator="dashed" />}
            />
            {Array.from(nameSet).map((key) => {
              return (<Bar key={key} dataKey={key} fill={chartConfig[key].color} radius={4} />)
            })}
          </BarChart>
        </ChartContainer>
      </CardContent>
    </Card>
  );
}
