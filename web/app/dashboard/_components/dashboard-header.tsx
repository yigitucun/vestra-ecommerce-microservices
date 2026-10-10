"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { useMe } from "@/hooks/auth/use-me";
import { PlusCircle, RefreshCw, ShoppingCart } from "lucide-react";
import { cn } from "cn";

interface DashboardHeaderProps {
  onRefreshAll: () => void;
  isRefreshing?: boolean;
}

export function DashboardHeader({ onRefreshAll, isRefreshing }: DashboardHeaderProps) {
  const router = useRouter();
  const { data: user } = useMe();

  const greeting = React.useMemo(() => {
    const hour = new Date().getHours();
    if (hour < 12) return "Günaydın";
    if (hour < 18) return "İyi günler";
    return "İyi akşamlar";
  }, []);

  const todayFormatted = React.useMemo(() => {
    return new Intl.DateTimeFormat("tr-TR", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    }).format(new Date());
  }, []);

  const userName = user?.firstName ? user.firstName : "Yönetici";

  return (
    <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-foreground md:text-3xl">
          {greeting}, {userName}
        </h1>
        <p className="text-sm text-muted-foreground mt-1">
          {todayFormatted} • Mağaza performans ve sipariş özeti.
        </p>
      </div>

      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="sm"
          onClick={onRefreshAll}
          disabled={isRefreshing}
        >
          <RefreshCw className={cn("size-4", isRefreshing && "animate-spin")} />
          Yenile
        </Button>

        <Button
          variant="outline"
          size="sm"
          onClick={() => router.push("/dashboard/orders")}
        >
          <ShoppingCart className="size-4" />
          Siparişler
        </Button>

        <Button
          size="sm"
          onClick={() => router.push("/dashboard/products")}
        >
          <PlusCircle className="size-4" />
          Ürün Ekle
        </Button>
      </div>
    </div>
  );
}
