"use client";

import { BarGraph, ChartData } from './bar-graph';
import { CalendarDateRangePicker } from '@/components/date-range-picker';
import PageContainer from '@/components/layout/page-container';
import { RecentSales } from './recent-sales';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle
} from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useEffect, useRef, useState } from 'react';
import { DateRange } from 'react-day-picker';
import { useLazyGetCategoryRevenueQuery, useLazyGetCustomerRevenueQuery, useLazyGetProductRevenueQuery } from '@/redux/api/statistics-api';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { AlertCircle } from 'lucide-react';
import { getErrorMessage } from '@/constants/get-error';
import { Spinner } from '@/components/spinner';
import {
  format,
  getWeek,
  startOfDay,
  endOfDay,
  startOfWeek,
  endOfWeek,
  startOfMonth,
  endOfMonth,
  startOfYear,
  endOfYear,
  isSameDay,
  isSameWeek,
  isSameMonth,
  isSameYear,
  parseISO,
  getWeekOfMonth
} from 'date-fns';
import html2canvas from 'html2canvas';

enum TypeStatistics {
  CATEGORY = 'category',
  PRODUCT = 'product',
  CUSTOMER = 'customer'
}

// statistic type DAY , WEEK, MONTH, YEAR
// DAY: all day in a week now
// WEEK: all week in a month now
// MONTH: all month in a year now
// YEAR: all year now
// QUARTER: all quarter in a year now
enum TypeTime {
  DAY = 'day',
  WEEK = 'week',
  MONTH = 'month',
  QUARTER = 'quarter',
  YEAR = 'year'
}

export const getTimeGrouping = (
  date: Date,
  timeType: TypeTime,
  referenceDate: Date = new Date()
) => {
  switch (timeType) {
    case TypeTime.DAY:
      return {
        key: format(date, 'EEEE'),
        label: format(date, 'EEEE'),
        isCurrentPeriod: isSameWeek(date, referenceDate, { weekStartsOn: 1 })
      };

    case TypeTime.WEEK:
      return {
        key: `Week ${getWeek(date)}`,
        label: `Week ${getWeekOfMonth(date)}`,
        isCurrentPeriod: isSameMonth(date, referenceDate)
      };

    case TypeTime.MONTH:
      return {
        key: format(date, 'MMMM'),
        label: format(date, 'MMMM'),
        isCurrentPeriod: isSameYear(date, referenceDate)
      };
    
      case TypeTime.QUARTER:
      return {
        key: `Quarter ${Math.floor((date.getMonth() + 3) / 3)}`,
        label: `Quarter ${Math.floor((date.getMonth() + 3) / 3)}`,
        isCurrentPeriod: isSameYear(date, referenceDate)
      };

    case TypeTime.YEAR:
      return {
        key: format(date, 'yyyy'),
        label: format(date, 'yyyy'),
        isCurrentPeriod: true
      };

    default:
      return {
        key: format(date, 'yyyy-MM-dd'),
        label: format(date, 'yyyy-MM-dd'),
        isCurrentPeriod: isSameDay(date, referenceDate)
      };
  }
};

export const getCurrentPeriodInterval = (
  timeType: TypeTime,
  referenceDate: Date = new Date()
): { start: Date; end: Date } => {
  switch (timeType) {
    case TypeTime.DAY:
      return {
        start: startOfDay(referenceDate),
        end: endOfDay(referenceDate)
      };

    case TypeTime.WEEK:
      return {
        start: startOfWeek(referenceDate, { weekStartsOn: 1 }),
        end: endOfWeek(referenceDate, { weekStartsOn: 1 })
      };

    case TypeTime.MONTH:
      return {
        start: startOfMonth(referenceDate),
        end: endOfMonth(referenceDate)
      };

    case TypeTime.YEAR:
      return {
        start: startOfYear(referenceDate),
        end: endOfYear(referenceDate)
      };

    default:
      return {
        start: startOfDay(referenceDate),
        end: endOfDay(referenceDate)
      };
  }
};

export default function OverViewPage() {
  const chartRef = useRef<HTMLDivElement>(null);
  const [date, setDate] = useState<DateRange | undefined>({
    from: new Date(new Date().getFullYear(), 0, 1),
    to: new Date(),
  });

  const [selectedType, setSelectedType] = useState<TypeStatistics>(TypeStatistics.CUSTOMER);
  const [selectedTime, setSelectedTime] = useState<TypeTime>(TypeTime.MONTH);

  const [getProductRevenue, { isFetching: isProductFetching, data: productRevenue, error: productError }] = useLazyGetProductRevenueQuery();
  const [getCategoryRevenue, { isFetching: isCategoryFetching, data: categoryRevenue, error: categoryError }] = useLazyGetCategoryRevenueQuery();
  const [getCustomerRevenue, { isFetching: isCustomerFetching, data: customerRevenue, error: customerError }] = useLazyGetCustomerRevenueQuery();
  const error = productError ? productError : categoryError ? categoryError : customerError ? customerError : undefined;
  const isFetching = isProductFetching || isCategoryFetching || isCustomerFetching;
  const [chartData, setChartData] = useState<ChartData[]>([]);

  const [totalStatistics, setStatistics] = useState<{
    totalRevenue: number;
    totalPromotion: number;
    totalDiscount: number;
    totalOrder: number;
  }>({
    totalRevenue: 0,
    totalPromotion: 0,
    totalDiscount: 0,
    totalOrder: 0
  });

  useEffect(() => {
    if(selectedType === TypeStatistics.CATEGORY && categoryRevenue?.result) {
      const { totalRevenue, totalPromotion, totalDiscount, totalOrder } = categoryRevenue.result.reduce((acc, category) => {
        acc.totalRevenue += category.statistics.reduce((acc, product) => acc + product.price * product.quantity, 0);

        const totalCategoryDiscount = category.statistics.reduce((acc, product) => acc + product.price * (product?.discountPercentage ?? 0) / 100, 0);
        acc.totalPromotion += totalCategoryDiscount;
        acc.totalDiscount += totalCategoryDiscount;
        acc.totalOrder += category.statistics.length;

        return acc;
      }, {
        totalRevenue: 0,
        totalPromotion: 0,
        totalDiscount: 0,
        totalOrder: 0
      });
      setStatistics({
        totalRevenue: totalRevenue,
        totalPromotion: totalPromotion,
        totalDiscount: totalDiscount,
        totalOrder: totalOrder
      });
    } else if(selectedType === TypeStatistics.PRODUCT && productRevenue?.result) {
      const { totalRevenue, totalPromotion, totalDiscount, totalOrder } = productRevenue.result.reduce((acc, product) => {
        acc.totalRevenue += product.statistics.reduce((acc, product) => acc + product.price * product.quantity, 0);
        acc.totalPromotion += product.statistics.reduce((acc, product) => acc + product.price * (product?.discountPercentage ?? 0) / 100, 0);
        acc.totalOrder += product.statistics.length;
        return acc;
      }, {
        totalRevenue: 0,
        totalPromotion: 0,
        totalDiscount: 0,
        totalOrder: 0
      });
      setStatistics({
        totalRevenue: totalRevenue,
        totalPromotion: totalPromotion,
        totalDiscount: totalDiscount,
        totalOrder: totalOrder
      });
    } else if(selectedType === TypeStatistics.CUSTOMER && customerRevenue?.result) {
      const { totalRevenue, totalPromotion, totalDiscount, totalOrder } = customerRevenue.result.reduce((acc, customer) => {
        acc.totalRevenue += customer.totalValue;
        acc.totalOrder += customer.totalOrder;
        return acc;
      }, {
        totalRevenue: 0,
        totalPromotion: 0,
        totalDiscount: 0,
        totalOrder: 0
      });
      setStatistics({
        totalRevenue: totalRevenue,
        totalPromotion: totalPromotion,
        totalDiscount: totalDiscount,
        totalOrder: totalOrder
      });
    }
  }, [categoryRevenue, productRevenue, customerRevenue, selectedType]);

  useEffect(() => {
    const startDate = date?.from?.toISOString().split('T')[0] || '';
    const endDate = date?.to?.toISOString().split('T')[0] || '';
    if(selectedType === TypeStatistics.CATEGORY) {
      getCategoryRevenue({ startDate, endDate });
    } else if(selectedType === TypeStatistics.PRODUCT) {
      getProductRevenue({ startDate, endDate });
    } else {
      getCustomerRevenue({ startDate, endDate });
    }
  }, [productRevenue, categoryRevenue, customerRevenue, date, selectedType]);

   useEffect(() => {
    let data: ChartData[] = [];
    if(selectedType === TypeStatistics.CATEGORY && categoryRevenue?.result) {
      data = categoryRevenue.result.reduce((acc, category) => {
        const categoryData = category.statistics.reduce((acc, product) => {
          const timeGrouping = getTimeGrouping(parseISO(product.date), selectedTime);
          if(timeGrouping.isCurrentPeriod) {
            acc.push({
              label: timeGrouping.label,
              value: (product.price - product.price * (product.discountPercentage ?? 0) / 100) * product.quantity,
              name: category.categoryName
            })
          } 
          return acc;
        }, [] as ChartData[]);
        return acc.concat(categoryData);
      }, [] as ChartData[]);
    }

    else if(selectedType === TypeStatistics.PRODUCT && productRevenue?.result) {
      data = productRevenue.result.reduce((acc, product) => {
        const productData = product.statistics.reduce((acc, orderItem) => {
          const timeGrouping = getTimeGrouping(parseISO(orderItem.date), selectedTime);
          if(timeGrouping.isCurrentPeriod) {
            acc.push({
              label: timeGrouping.label,
              value: (orderItem.price - orderItem.price * (orderItem.discountPercentage ?? 0) / 100) * orderItem.quantity,
              name: product.productName
            })
          } 
          return acc;
        }, [] as ChartData[]);
        return acc.concat(productData);
      }, [] as ChartData[]);
    }

    else if(selectedType === TypeStatistics.CUSTOMER && customerRevenue?.result) {
      data = customerRevenue.result.reduce((acc, customer) => {
        const customerData = customer.statistics.reduce((acc, order) => {
          const timeGrouping = getTimeGrouping(parseISO(order.date), selectedTime);
          if(timeGrouping.isCurrentPeriod) {
            acc.push({
              label: timeGrouping.label,
              value: order.value,
              name: customer.name
            })
          } 
          return acc;
        }, [] as ChartData[]);
        return acc.concat(customerData);
      }, [] as ChartData[]);
    }

    setChartData(data);
  }, [productRevenue, customerRevenue, categoryRevenue, selectedType, selectedTime]);

  if (error) {
    return (
      <Alert variant="destructive" className='mx-4 w-100'>
        <AlertCircle className="h-4 w-4" />
        <AlertTitle>Error</AlertTitle>
        <AlertDescription>
          {getErrorMessage(error)}
        </AlertDescription>
      </Alert>
    )
  }

  const handleDownload = async () => {
    if (!chartRef.current) return;
    try {
      const canvas = await html2canvas(chartRef.current, {
        useCORS: true,
        logging: true,
        scale: 2
      });

      const image = canvas.toDataURL('image/png', 1.0);

      const link = document.createElement('a');
      link.download = `chart-${Date.now()}.png`;
      link.href = image;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (error) {
      console.error('Error generating image:', error);
    }
  };

  return (
    <PageContainer scrollable>
      <div className="space-y-2">
        <div className="flex items-center justify-between space-y-2">
          <h2 className="text-2xl font-bold tracking-tight">
            Hi, Welcome back 👋
          </h2>
          <div className="hidden items-center space-x-2 md:flex">
            <CalendarDateRangePicker
              dateRange={date}
              onDateRangeChange={(dateRange) => {
                setDate(dateRange)
              }}
            />
            <Button onClick={() => {
              handleDownload();
            }}>Download</Button>
          </div>
        </div>
        <Tabs defaultValue="overview" className="space-y-4">
          <div className='flex justify-between'>
            <TabsList>
              <TabsTrigger value="overview">Overview</TabsTrigger>
              <TabsTrigger value="analytics">
                Analytics
              </TabsTrigger>
            </TabsList>
            <div>
              <div className="flex space-x-4">
                <select
                  value={selectedType}
                  onChange={(e) => setSelectedType(e.target.value as TypeStatistics)}
                  className="border rounded-md p-2"
                >
                  <option value={TypeStatistics.CUSTOMER}>Customer</option>
                  <option value={TypeStatistics.PRODUCT}>Product</option>
                  <option value={TypeStatistics.CATEGORY}>Category</option>
                </select>

                <select
                  value={selectedTime}
                  onChange={(e) => setSelectedTime(e.target.value as TypeTime)}
                  className="border rounded-md p-2"
                >
                  <option value={TypeTime.DAY}>Day in week</option>
                  <option value={TypeTime.WEEK}>Week in month</option>
                  <option value={TypeTime.MONTH}>Month in year</option>
                  <option value={TypeTime.QUARTER}>Quarter in year</option>
                  <option value={TypeTime.YEAR}>All Year</option>
                </select>
              </div>
            </div>
          </div>
          <TabsContent value="overview" className="space-y-4">
            {isFetching ? (
              <Spinner size="large" className="mt-10" />
            ) : (
                <>
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                  <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">
                        Total Revenue
                      </CardTitle>
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth="2"
                        className="h-4 w-4 text-muted-foreground"
                      >
                        <path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
                      </svg>
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold">đ{' ' + totalStatistics.totalRevenue}</div>
                      <p className="text-xs text-muted-foreground">
                        +20.1% from last month
                      </p>
                    </CardContent>
                  </Card>
                  {selectedType === TypeStatistics.CUSTOMER && (
                      <Card>
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                          <CardTitle className="text-sm font-medium">
                            Total Order
                          </CardTitle>
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="2"
                            className="h-4 w-4 text-muted-foreground"
                          >
                            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
                            <circle cx="9" cy="7" r="4" />
                            <path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" />
                          </svg>
                        </CardHeader>
                        <CardContent>
                          <div className="text-2xl font-bold">{totalStatistics.totalOrder}</div>
                          <p className="text-xs text-muted-foreground">
                            +180.1% from last month
                          </p>
                        </CardContent>
                      </Card>
                    )}
                  <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">Total Promotion</CardTitle>
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth="2"
                        className="h-4 w-4 text-muted-foreground"
                      >
                        <rect width="20" height="14" x="2" y="5" rx="2" />
                        <path d="M2 10h20" />
                      </svg>
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold">đ{' ' + totalStatistics.totalPromotion}</div>
                    </CardContent>
                  </Card>
                  {selectedType === TypeStatistics.CUSTOMER && (
                      <Card>
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                          <CardTitle className="text-sm font-medium">
                            Total Discount
                          </CardTitle>
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="2"
                            className="h-4 w-4 text-muted-foreground"
                          >
                            <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
                          </svg>
                        </CardHeader>
                        <CardContent>
                          <div className="text-2xl font-bold">đ {' ' + totalStatistics.totalDiscount}</div>
                        </CardContent>
                      </Card>
                    )}
                </div>
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-7" ref={chartRef}>
                  <div className="col-span-4">
                    <BarGraph chartData={chartData} />
                  </div>
                  <Card className="col-span-4 md:col-span-3">
                    <CardHeader>
                      <CardTitle>Recent Sales</CardTitle>
                      <CardDescription>
                        You made {totalStatistics.totalOrder} sales {selectedTime === TypeTime.YEAR ? 'in all time' : selectedTime === TypeTime.QUARTER || selectedTime === TypeTime.MONTH ? 'in this year' : selectedTime === TypeTime.WEEK ? 'in this month' : 'in this week'}.
                      </CardDescription>
                    </CardHeader>
                    <CardContent>
                      <RecentSales data={chartData} />
                    </CardContent>
                  </Card>
                </div>
              </>
            )}
          </TabsContent>
        </Tabs>
      </div>
    </PageContainer>
  );
}
