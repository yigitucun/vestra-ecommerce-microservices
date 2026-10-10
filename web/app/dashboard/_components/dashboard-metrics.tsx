"use client";

import * as React from "react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

interface DashboardMetricsProps {
  isLoading: boolean;
  totalRevenue: number;
  totalOrders: number;
  pendingOrdersCount: number;
  totalProducts: number;
  totalCategories: number;
  totalUsers: number;
}

export function DashboardMetrics({
  isLoading,
  totalRevenue,
  totalOrders,
  pendingOrdersCount,
  totalProducts,
  totalCategories,
  totalUsers,
}: DashboardMetricsProps) {
  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat("tr-TR", {
      style: "currency",
      currency: "TRY",
      maximumFractionDigits: 0,
    }).format(val);
  };

  const revenueValue =
    totalOrders > 0 ? formatCurrency(totalRevenue) : "—";
  const revenueSubtext =
    totalOrders > 0 ? "Tamamlanan sipariş tutarı" : "Henüz sipariş bulunmuyor";

  const ordersValue =
    totalOrders > 0 ? totalOrders.toLocaleString("tr-TR") : "—";
  const ordersSubtext =
    pendingOrdersCount > 0
      ? `${pendingOrdersCount} sipariş onay bekliyor`
      : totalOrders > 0
        ? "Tüm siparişler güncel"
        : "Henüz sipariş bulunmuyor";

  const productsValue =
    totalProducts > 0 ? totalProducts.toLocaleString("tr-TR") : "—";
  const productsSubtext =
    totalCategories > 0
      ? `${totalCategories} kategoride listeleniyor`
      : totalProducts > 0
        ? "Katalogdaki aktif ürünler"
        : "Henüz ürün eklenmemiş";

  const usersValue =
    totalUsers > 0 ? totalUsers.toLocaleString("tr-TR") : "—";
  const usersSubtext =
    totalUsers > 0 ? "Kayıtlı kullanıcı hesabı" : "Henüz kullanıcı bulunmuyor";

  const cards = [
    {
      title: "Toplam Ciro",
      value: revenueValue,
      description: revenueSubtext,
    },
    {
      title: "Toplam Sipariş",
      value: ordersValue,
      description: ordersSubtext,
    },
    {
      title: "Aktif Ürünler",
      value: productsValue,
      description: productsSubtext,
    },
    {
      title: "Kayıtlı Kullanıcılar",
      value: usersValue,
      description: usersSubtext,
    },
  ];

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      {cards.map((card, idx) => (
        <Card key={idx}>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              {card.title}
            </CardTitle>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="space-y-2">
                <Skeleton className="h-7 w-28" />
                <Skeleton className="h-3 w-36" />
              </div>
            ) : (
              <div className="space-y-1">
                <div className="text-2xl font-bold tracking-tight text-foreground">
                  {card.value}
                </div>
                <p className="text-xs text-muted-foreground">
                  {card.description}
                </p>
              </div>
            )}
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
