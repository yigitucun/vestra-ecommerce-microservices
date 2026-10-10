"use client";

import * as React from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useAdminOrders } from "@/hooks/admin/use-admin-orders";
import { useAdminProducts } from "@/hooks/admin/use-admin-products";
import { useAdminCategories } from "@/hooks/admin/use-admin-categories";
import { useAdminUsers } from "@/hooks/admin/use-admin-users";

import { DashboardHeader } from "./dashboard-header";
import { DashboardMetrics } from "./dashboard-metrics";
import { RevenueChart } from "./revenue-chart";
import { RecentOrders } from "./recent-orders";

export function DashboardOverview() {
  const queryClient = useQueryClient();

  const ordersQuery = useAdminOrders({ page: 0, size: 20 });
  const productsQuery = useAdminProducts({ page: 0, size: 20 });
  const categoriesQuery = useAdminCategories();
  const usersQuery = useAdminUsers({ page: 0, size: 20 });

  const orders = ordersQuery.data?.content || [];
  const totalOrders = ordersQuery.data?.page?.totalElements ?? orders.length;

  const pendingOrdersCount = React.useMemo(() => {
    return orders.filter(
      (o) => o.status === "PENDING" || o.status === "STOCK_CONFIRMED"
    ).length;
  }, [orders]);

  const totalRevenue = React.useMemo(() => {
    return orders
      .filter((o) => o.status !== "CANCELLED" && o.status !== "PAYMENT_FAILED")
      .reduce((sum, o) => sum + (o.totalAmount || 0), 0);
  }, [orders]);

  const totalProducts =
    productsQuery.data?.page?.totalElements ??
    productsQuery.data?.totalElements ??
    productsQuery.data?.content?.length ??
    0;

  const totalCategories = Array.isArray(categoriesQuery.data)
    ? categoriesQuery.data.length
    : 0;

  const totalUsers =
    usersQuery.data?.page?.totalElements ??
    usersQuery.data?.totalElements ??
    usersQuery.data?.content?.length ??
    0;

  const isLoading =
    ordersQuery.isLoading ||
    productsQuery.isLoading ||
    categoriesQuery.isLoading ||
    usersQuery.isLoading;

  const isRefreshing =
    ordersQuery.isFetching ||
    productsQuery.isFetching ||
    categoriesQuery.isFetching ||
    usersQuery.isFetching;

  const handleRefreshAll = () => {
    void Promise.all([
      queryClient.invalidateQueries({ queryKey: ["admin-orders"] }),
      queryClient.invalidateQueries({ queryKey: ["admin-products"] }),
      queryClient.invalidateQueries({ queryKey: ["admin-categories"] }),
      queryClient.invalidateQueries({ queryKey: ["admin-users"] }),
      queryClient.invalidateQueries({ queryKey: ["me"] }),
    ]);
  };

  return (
    <div className="space-y-6">
      <DashboardHeader
        onRefreshAll={handleRefreshAll}
        isRefreshing={isRefreshing}
      />

      <DashboardMetrics
        isLoading={isLoading}
        totalRevenue={totalRevenue}
        totalOrders={totalOrders}
        pendingOrdersCount={pendingOrdersCount}
        totalProducts={totalProducts}
        totalCategories={totalCategories}
        totalUsers={totalUsers}
      />

      <div className="grid gap-6 grid-cols-1 lg:grid-cols-12">
        <RevenueChart
          orders={orders}
          isLoading={isLoading}
          className="lg:col-span-7 xl:col-span-8"
        />
        <RecentOrders
          orders={orders}
          isLoading={isLoading}
          className="lg:col-span-5 xl:col-span-4"
        />
      </div>
    </div>
  );
}
