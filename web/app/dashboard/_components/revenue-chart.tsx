"use client";

import * as React from "react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { OrderListItem } from "@/types/order";
import { BarChart3 } from "lucide-react";
import { cn } from "cn";

interface RevenueChartProps {
  orders: OrderListItem[];
  isLoading: boolean;
  className?: string;
}

export function RevenueChart({ orders, isLoading, className }: RevenueChartProps) {
  // Aggregate real orders by date
  const chartData = React.useMemo(() => {
    if (!orders || orders.length === 0) return [];

    const map: Record<string, { date: string; revenue: number; count: number; timestamp: number }> = {};

    orders.forEach((order) => {
      if (order.status === "CANCELLED" || order.status === "PAYMENT_FAILED") return;

      const d = new Date(order.createdAt);
      if (isNaN(d.getTime())) return;

      const key = d.toISOString().slice(0, 10); // YYYY-MM-DD
      const label = new Intl.DateTimeFormat("tr-TR", {
        day: "numeric",
        month: "short",
      }).format(d);

      if (!map[key]) {
        map[key] = { date: label, revenue: 0, count: 0, timestamp: d.getTime() };
      }
      map[key].revenue += Number(order.totalAmount || 0);
      map[key].count += 1;
    });

    return Object.values(map).sort((a, b) => a.timestamp - b.timestamp);
  }, [orders]);

  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat("tr-TR", {
      style: "currency",
      currency: "TRY",
      maximumFractionDigits: 0,
    }).format(val);
  };

  return (
    <Card className={cn(className)}>
      <CardHeader className="pb-3">
        <CardTitle className="text-base font-semibold">Ciro Grafiği</CardTitle>
        <CardDescription className="text-xs text-muted-foreground">
          Gerçekleşen siparişlerin zamana göre toplam gelir dağılımı.
        </CardDescription>
      </CardHeader>

      <CardContent>
        {isLoading ? (
          <div className="flex h-[300px] items-center justify-center">
            <Skeleton className="h-full w-full rounded-md" />
          </div>
        ) : chartData.length === 0 ? (
          <div className="flex h-[300px] flex-col items-center justify-center text-center">
            <BarChart3 className="size-8 text-muted-foreground/50 mb-2" />
            <p className="text-sm font-medium text-foreground">Henüz grafik verisi bulunmuyor</p>
            <p className="text-xs text-muted-foreground mt-1 max-w-xs">
              Yeni siparişler oluştukça gelir dağılımı otomatik olarak burada görüntülenecektir.
            </p>
          </div>
        ) : (
          <div className="h-[300px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart
                data={chartData}
                margin={{ top: 10, right: 10, left: 0, bottom: 0 }}
              >
                <CartesianGrid
                  strokeDasharray="3 3"
                  vertical={false}
                  stroke="var(--color-border)"
                />
                <XAxis
                  dataKey="date"
                  tickLine={false}
                  axisLine={false}
                  tickMargin={8}
                  stroke="var(--color-muted-foreground)"
                  className="text-xs"
                />
                <YAxis
                  tickLine={false}
                  axisLine={false}
                  tickMargin={8}
                  stroke="var(--color-muted-foreground)"
                  className="text-xs"
                  tickFormatter={(val) => `₺${val}`}
                />
                <Tooltip
                  content={({ active, payload }) => {
                    if (active && payload && payload.length) {
                      const data = payload[0].payload;
                      return (
                        <div className="rounded-lg border border-border bg-popover p-2.5 text-xs text-popover-foreground shadow-md">
                          <p className="font-semibold mb-1">{data.date}</p>
                          <div className="space-y-0.5">
                            <p className="font-medium text-foreground">
                              Ciro: {formatCurrency(data.revenue)}
                            </p>
                            <p className="text-muted-foreground">
                              Sipariş: {data.count} adet
                            </p>
                          </div>
                        </div>
                      );
                    }
                    return null;
                  }}
                />
                <Bar
                  dataKey="revenue"
                  fill="var(--color-primary)"
                  radius={[4, 4, 0, 0]}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
