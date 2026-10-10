"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { OrderListItem } from "@/types/order";
import { ArrowRight, ShoppingCart } from "lucide-react";
import { cn } from "cn";

interface RecentOrdersProps {
  orders: OrderListItem[];
  isLoading: boolean;
  className?: string;
}

export function RecentOrders({ orders, isLoading, className }: RecentOrdersProps) {
  const router = useRouter();
  const recentList = orders.slice(0, 5);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("tr-TR", {
      style: "currency",
      currency: "TRY",
      maximumFractionDigits: 0,
    }).format(amount);
  };

  return (
    <Card className={cn(className)}>
      <CardHeader className="flex flex-row items-center justify-between pb-3">
        <div>
          <CardTitle className="text-base font-semibold">Son Satışlar</CardTitle>
          <CardDescription className="text-xs text-muted-foreground">
            En son gerçekleşen müşteri siparişleri.
          </CardDescription>
        </div>
        <Button
          variant="ghost"
          size="sm"
          onClick={() => router.push("/dashboard/orders")}
        >
          Tümünü Gör
          <ArrowRight className="size-4" />
        </Button>
      </CardHeader>

      <CardContent>
        {isLoading ? (
          <div className="space-y-4">
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="flex items-center gap-4">
                <Skeleton className="size-9 rounded-full" />
                <div className="space-y-1 flex-1">
                  <Skeleton className="h-4 w-32" />
                  <Skeleton className="h-3 w-20" />
                </div>
                <Skeleton className="h-4 w-16" />
              </div>
            ))}
          </div>
        ) : recentList.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-12 text-center text-muted-foreground">
            <ShoppingCart className="size-8 text-muted-foreground/50 mb-2" />
            <p className="text-sm font-medium text-foreground">Henüz sipariş bulunmuyor</p>
            <p className="text-xs text-muted-foreground mt-1 max-w-xs">
              Yeni siparişler oluşturulduğunda burada listelenecektir.
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {recentList.map((order) => {
              const initials = (order.customerEmail || "M")
                .slice(0, 2)
                .toUpperCase();

              return (
                <div key={order.id} className="flex items-center gap-4">
                  <Avatar className="size-9">
                    <AvatarFallback className="text-xs font-semibold">
                      {initials}
                    </AvatarFallback>
                  </Avatar>
                  <div className="flex flex-col gap-0.5 overflow-hidden">
                    <p
                      className="text-sm font-medium text-foreground truncate"
                      title={order.customerEmail}
                    >
                      {order.customerEmail || "Müşteri"}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {order.orderNumber}
                    </p>
                  </div>
                  <div className="ml-auto text-sm font-semibold text-foreground whitespace-nowrap">
                    +{formatCurrency(order.totalAmount)}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
