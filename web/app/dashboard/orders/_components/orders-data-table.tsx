"use client";

import * as React from "react";
import { useState, useMemo } from "react";
import {
  Table,
  TableHeader,
  TableBody,
  TableHead,
  TableRow,
  TableCell,
} from "@/components/ui/table";
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
} from "@/components/ui/card";
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { toast } from "@/components/ui/toast";
import { useAdminOrders, useUpdateOrderStatus } from "@/hooks/admin/use-admin-orders";
import { OrderListItem, OrderStatus } from "@/types/order";
import {
  Search,
  X,
  RefreshCw,
  MoreHorizontal,
  Copy,
  Check,
  Eye,
  ShoppingCart,
  Truck,
  CheckCircle2,
  XCircle,
  Clock,
  Package,
} from "lucide-react";

const STATUS_CONFIG: Record<
  OrderStatus,
  { label: string; variant: "default" | "secondary" | "destructive" | "outline"; icon: React.ReactNode; colorClass: string }
> = {
  PENDING: {
    label: "Beklemede",
    variant: "outline",
    icon: <Clock className="h-3.5 w-3.5 text-amber-500" />,
    colorClass: "border-amber-400 bg-amber-50 text-amber-700 dark:bg-amber-950 dark:text-amber-300",
  },
  STOCK_CONFIRMED: {
    label: "Stok Onaylandı",
    variant: "outline",
    icon: <Package className="h-3.5 w-3.5 text-blue-500" />,
    colorClass: "border-blue-400 bg-blue-50 text-blue-700 dark:bg-blue-950 dark:text-blue-300",
  },
  STOCK_FAILED: {
    label: "Stok Yetersiz",
    variant: "destructive",
    icon: <XCircle className="h-3.5 w-3.5" />,
    colorClass: "border-red-400 bg-red-50 text-red-700 dark:bg-red-950 dark:text-red-300",
  },
  PAID: {
    label: "Ödendi",
    variant: "outline",
    icon: <CheckCircle2 className="h-3.5 w-3.5 text-emerald-500" />,
    colorClass: "border-emerald-400 bg-emerald-50 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300",
  },
  PAYMENT_FAILED: {
    label: "Ödeme Başarısız",
    variant: "destructive",
    icon: <XCircle className="h-3.5 w-3.5" />,
    colorClass: "border-red-400 bg-red-50 text-red-700 dark:bg-red-950 dark:text-red-300",
  },
  SHIPPED: {
    label: "Kargoya Verildi",
    variant: "outline",
    icon: <Truck className="h-3.5 w-3.5 text-indigo-500" />,
    colorClass: "border-indigo-400 bg-indigo-50 text-indigo-700 dark:bg-indigo-950 dark:text-indigo-300",
  },
  DELIVERED: {
    label: "Teslim Edildi",
    variant: "outline",
    icon: <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600" />,
    colorClass: "border-emerald-500 bg-emerald-50 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-200",
  },
  CANCELLED: {
    label: "İptal Edildi",
    variant: "outline",
    icon: <XCircle className="h-3.5 w-3.5 text-zinc-500" />,
    colorClass: "border-zinc-300 bg-zinc-100 text-zinc-600 dark:bg-zinc-800 dark:text-zinc-400",
  },
};

export function OrdersDataTable() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [selectedStatus, setSelectedStatus] = useState<OrderStatus | "">("");
  const [searchTerm, setSearchTerm] = useState("");
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [selectedOrder, setSelectedOrder] = useState<OrderListItem | null>(null);

  const { data, isLoading, isError, refetch, isFetching } = useAdminOrders({
    page,
    size,
    status: selectedStatus,
  });

  const updateStatusMutation = useUpdateOrderStatus();

  const orders = data?.content || [];
  const pageInfo = data?.page;

  // Local filter for search term
  const filteredOrders = useMemo(() => {
    if (!searchTerm.trim()) return orders;
    const term = searchTerm.toLowerCase();
    return orders.filter(
      (order) =>
        order.orderNumber.toLowerCase().includes(term) ||
        (order.customerEmail && order.customerEmail.toLowerCase().includes(term)) ||
        order.id.toLowerCase().includes(term)
    );
  }, [orders, searchTerm]);

  const handleCopy = async (text: string, label: string) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopiedId(text);
      toast.add({
        title: "Kopyalandı",
        description: `${label} panoya kopyalandı.`,
      });
      setTimeout(() => setCopiedId(null), 2000);
    } catch {
      toast.add({
        title: "Kopyalama Başarısız",
        description: "Panoya kopyalanırken bir hata oluştu.",
      });
    }
  };

  const handleStatusChange = (orderId: string, newStatus: OrderStatus) => {
    updateStatusMutation.mutate(
      { id: orderId, status: newStatus },
      {
        onSuccess: () => {
          toast.add({
            title: "Durum Güncellendi",
            description: `Sipariş durumu "${STATUS_CONFIG[newStatus]?.label || newStatus}" olarak güncellendi.`,
          });
          if (selectedOrder && selectedOrder.id === orderId) {
            setSelectedOrder((prev) => (prev ? { ...prev, status: newStatus } : null));
          }
        },
        onError: (err: unknown) => {
          toast.add({
            title: "Hata",
            description: err instanceof Error ? err.message : "Durum güncellenemedi.",
          });
        },
      }
    );
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("tr-TR", {
      style: "currency",
      currency: "TRY",
    }).format(amount);
  };

  const formatDate = (dateStr: string) => {
    try {
      return new Intl.DateTimeFormat("tr-TR", {
        dateStyle: "medium",
        timeStyle: "short",
      }).format(new Date(dateStr));
    } catch {
      return dateStr;
    }
  };

  return (
    <Card className="border-border">
      <CardHeader className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <CardTitle className="text-xl font-bold flex items-center gap-2">
            <ShoppingCart className="h-5 w-5 text-primary" />
            Siparişler
          </CardTitle>
          <CardDescription className="text-sm text-muted-foreground mt-1">
            Gelen siparişleri izleyin, müşteri bilgilerini görüntüleyin ve sipariş statülerini yönetin.
          </CardDescription>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            disabled={isFetching}
            className="flex items-center gap-1.5"
          >
            <RefreshCw className={`h-4 w-4 ${isFetching ? "animate-spin" : ""}`} />
            Yenile
          </Button>
        </div>
      </CardHeader>

      <CardContent className="space-y-4">
        {/* Filtre ve Arama Alanı */}
        <div className="flex flex-col sm:flex-row items-center gap-3 justify-between">
          <div className="relative w-full sm:w-80">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Sipariş no veya e-posta ile ara..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-9 pr-8"
            />
            {searchTerm && (
              <button
                onClick={() => setSearchTerm("")}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
              >
                <X className="h-4 w-4" />
              </button>
            )}
          </div>

          <div className="flex items-center gap-2 w-full sm:w-auto">
            <Select
              value={selectedStatus}
              onValueChange={(val) => {
                setSelectedStatus(val as OrderStatus | "");
                setPage(0);
              }}
            >
              <SelectTrigger className="w-full sm:w-48">
                <SelectValue placeholder="Tüm Durumlar" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="">Tüm Durumlar</SelectItem>
                <SelectItem value="PENDING">Beklemede</SelectItem>
                <SelectItem value="STOCK_CONFIRMED">Stok Onaylandı</SelectItem>
                <SelectItem value="STOCK_FAILED">Stok Yetersiz</SelectItem>
                <SelectItem value="PAID">Ödendi</SelectItem>
                <SelectItem value="PAYMENT_FAILED">Ödeme Başarısız</SelectItem>
                <SelectItem value="SHIPPED">Kargoya Verildi</SelectItem>
                <SelectItem value="DELIVERED">Teslim Edildi</SelectItem>
                <SelectItem value="CANCELLED">İptal Edildi</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        {/* Tablo Alanı */}
        <div className="rounded-md border overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow className="bg-muted/50">
                <TableHead className="w-[180px]">Sipariş No</TableHead>
                <TableHead>Müşteri</TableHead>
                <TableHead>Kalem Sayısı</TableHead>
                <TableHead>Tutar</TableHead>
                <TableHead>Durum</TableHead>
                <TableHead>Tarih</TableHead>
                <TableHead className="text-right">İşlemler</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <TableRow key={i}>
                    <TableCell><Skeleton className="h-5 w-28" /></TableCell>
                    <TableCell><Skeleton className="h-5 w-36" /></TableCell>
                    <TableCell><Skeleton className="h-5 w-16" /></TableCell>
                    <TableCell><Skeleton className="h-5 w-20" /></TableCell>
                    <TableCell><Skeleton className="h-6 w-24 rounded-full" /></TableCell>
                    <TableCell><Skeleton className="h-5 w-28" /></TableCell>
                    <TableCell className="text-right"><Skeleton className="h-8 w-8 ml-auto rounded" /></TableCell>
                  </TableRow>
                ))
              ) : isError ? (
                <TableRow>
                  <TableCell colSpan={7} className="h-32 text-center text-destructive">
                    Siparişler yüklenirken bir hata oluştu. Lütfen bağlantınızı kontrol edin.
                  </TableCell>
                </TableRow>
              ) : filteredOrders.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="h-36 text-center text-muted-foreground">
                    <div className="flex flex-col items-center justify-center gap-2">
                      <ShoppingCart className="h-8 w-8 text-muted-foreground/50" />
                      <span>Kriterlere uygun sipariş bulunamadı.</span>
                    </div>
                  </TableCell>
                </TableRow>
              ) : (
                filteredOrders.map((order) => {
                  const statusConf = STATUS_CONFIG[order.status] || {
                    label: order.status,
                    variant: "outline",
                    icon: null,
                    colorClass: "",
                  };

                  const totalQuantity = order.items?.reduce((acc, it) => acc + it.quantity, 0) || 0;

                  return (
                    <TableRow key={order.id} className="hover:bg-muted/30">
                      <TableCell className="font-medium">
                        <div className="flex items-center gap-1.5">
                          <span>{order.orderNumber}</span>
                          <button
                            onClick={() => handleCopy(order.orderNumber, "Sipariş Numarası")}
                            className="text-muted-foreground hover:text-foreground transition-colors"
                            title="Kopyala"
                          >
                            {copiedId === order.orderNumber ? (
                              <Check className="h-3.5 w-3.5 text-emerald-600" />
                            ) : (
                              <Copy className="h-3.5 w-3.5" />
                            )}
                          </button>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex flex-col">
                          <span className="font-medium text-sm text-foreground">
                            {order.customerEmail || "Belirtilmemiş"}
                          </span>
                          <span className="text-xs text-muted-foreground truncate max-w-[180px]">
                            ID: {order.userId}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell>
                        <span className="text-sm font-medium">
                          {order.items?.length || 0} kalem ({totalQuantity} adet)
                        </span>
                      </TableCell>
                      <TableCell className="font-semibold text-foreground">
                        {formatCurrency(order.totalAmount)}
                      </TableCell>
                      <TableCell>
                        <Badge
                          variant="outline"
                          className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 text-xs font-medium rounded-full ${statusConf.colorClass}`}
                        >
                          {statusConf.icon}
                          {statusConf.label}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-xs text-muted-foreground">
                        {formatDate(order.createdAt)}
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex items-center justify-end gap-1">
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => setSelectedOrder(order)}
                            title="Detay Görüntüle"
                            className="h-8 w-8 text-muted-foreground hover:text-foreground"
                          >
                            <Eye className="h-4 w-4" />
                          </Button>
                          <DropdownMenu>
                            <DropdownMenuTrigger
                              render={
                                <Button
                                  variant="ghost"
                                  size="icon"
                                  className="h-8 w-8 text-muted-foreground hover:text-foreground"
                                />
                              }
                            >
                              <MoreHorizontal className="h-4 w-4" />
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="w-48">
                              <DropdownMenuLabel>Durumu Değiştir</DropdownMenuLabel>
                              <DropdownMenuSeparator />
                              <DropdownMenuItem
                                onClick={() => handleStatusChange(order.id, "STOCK_CONFIRMED")}
                                disabled={order.status === "STOCK_CONFIRMED"}
                              >
                                <Package className="h-4 w-4 mr-2 text-blue-500" />
                                Stok Onayla
                              </DropdownMenuItem>
                              <DropdownMenuItem
                                onClick={() => handleStatusChange(order.id, "PAID")}
                                disabled={order.status === "PAID"}
                              >
                                <CheckCircle2 className="h-4 w-4 mr-2 text-emerald-500" />
                                Ödendi Olarak İşaretle
                              </DropdownMenuItem>
                              <DropdownMenuItem
                                onClick={() => handleStatusChange(order.id, "SHIPPED")}
                                disabled={order.status === "SHIPPED"}
                              >
                                <Truck className="h-4 w-4 mr-2 text-indigo-500" />
                                Kargoya Ver
                              </DropdownMenuItem>
                              <DropdownMenuItem
                                onClick={() => handleStatusChange(order.id, "DELIVERED")}
                                disabled={order.status === "DELIVERED"}
                              >
                                <CheckCircle2 className="h-4 w-4 mr-2 text-emerald-600" />
                                Teslim Edildi
                              </DropdownMenuItem>
                              <DropdownMenuSeparator />
                              <DropdownMenuItem
                                onClick={() => handleStatusChange(order.id, "CANCELLED")}
                                disabled={order.status === "CANCELLED"}
                                className="text-destructive focus:text-destructive"
                              >
                                <XCircle className="h-4 w-4 mr-2" />
                                Siparişi İptal Et
                              </DropdownMenuItem>
                            </DropdownMenuContent>
                          </DropdownMenu>
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </div>

        {/* Sayfalama Kontrolleri */}
        {pageInfo && pageInfo.totalPages > 1 && (
          <div className="flex flex-col sm:flex-row items-center justify-between gap-3 pt-2">
            <div className="text-xs text-muted-foreground">
              Toplam <span className="font-medium text-foreground">{pageInfo.totalElements}</span> sipariş arasından{" "}
              <span className="font-medium text-foreground">{page * size + 1}</span> -{" "}
              <span className="font-medium text-foreground">
                {Math.min((page + 1) * size, pageInfo.totalElements)}
              </span>{" "}
              arası gösteriliyor.
            </div>

            <div className="flex items-center gap-2">
              <Select
                value={size.toString()}
                onValueChange={(val) => {
                  setSize(Number(val));
                  setPage(0);
                }}
              >
                <SelectTrigger className="w-24 h-8 text-xs">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="10">10 / sayfa</SelectItem>
                  <SelectItem value="20">20 / sayfa</SelectItem>
                  <SelectItem value="50">50 / sayfa</SelectItem>
                </SelectContent>
              </Select>

              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="h-8 text-xs"
              >
                Önceki
              </Button>
              <div className="text-xs font-medium px-2">
                {page + 1} / {pageInfo.totalPages}
              </div>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.min(pageInfo.totalPages - 1, p + 1))}
                disabled={page >= pageInfo.totalPages - 1}
                className="h-8 text-xs"
              >
                Sonraki
              </Button>
            </div>
          </div>
        )}
      </CardContent>

      {/* Sipariş Detay Dialogu */}
      <Dialog open={!!selectedOrder} onOpenChange={(open) => !open && setSelectedOrder(null)}>
        {selectedOrder && (
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle className="flex items-center justify-between gap-2 text-lg">
                <span>Sipariş Detayı: {selectedOrder.orderNumber}</span>
                <Badge
                  variant="outline"
                  className={`inline-flex items-center gap-1 px-2.5 py-0.5 text-xs font-medium rounded-full ${
                    STATUS_CONFIG[selectedOrder.status]?.colorClass || ""
                  }`}
                >
                  {STATUS_CONFIG[selectedOrder.status]?.icon}
                  {STATUS_CONFIG[selectedOrder.status]?.label || selectedOrder.status}
                </Badge>
              </DialogTitle>
              <DialogDescription>
                Oluşturulma Tarihi: {formatDate(selectedOrder.createdAt)}
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-4 py-2">
              {/* Müşteri ve Teslimat Bilgileri */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 p-3 bg-muted/40 rounded-lg text-sm">
                <div>
                  <span className="text-xs text-muted-foreground block">Müşteri E-Posta</span>
                  <span className="font-medium">{selectedOrder.customerEmail}</span>
                </div>
                <div>
                  <span className="text-xs text-muted-foreground block">Müşteri ID</span>
                  <span className="font-mono text-xs">{selectedOrder.userId}</span>
                </div>
                <div className="sm:col-span-2">
                  <span className="text-xs text-muted-foreground block">Teslimat Adresi</span>
                  <span className="font-medium">{selectedOrder.shippingAddress}</span>
                </div>
              </div>

              {/* Kalemler Tablosu */}
              <div>
                <h4 className="text-sm font-semibold mb-2">Sipariş Kalemleri</h4>
                <div className="rounded-md border overflow-hidden">
                  <Table>
                    <TableHeader>
                      <TableRow className="bg-muted/50">
                        <TableHead>Ürün</TableHead>
                        <TableHead>SKU</TableHead>
                        <TableHead className="text-center">Adet</TableHead>
                        <TableHead className="text-right">Birim Fiyat</TableHead>
                        <TableHead className="text-right">Ara Toplam</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {selectedOrder.items?.map((item) => (
                        <TableRow key={item.id}>
                          <TableCell className="font-medium text-sm">{item.productName}</TableCell>
                          <TableCell className="font-mono text-xs text-muted-foreground">{item.sku}</TableCell>
                          <TableCell className="text-center text-sm">{item.quantity}</TableCell>
                          <TableCell className="text-right text-sm">{formatCurrency(item.unitPrice)}</TableCell>
                          <TableCell className="text-right font-medium text-sm">
                            {formatCurrency(item.subtotal)}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              </div>

              {/* Toplam Tutar */}
              <div className="flex justify-between items-center p-3 border rounded-lg bg-card">
                <span className="font-semibold text-sm">Toplam Sipariş Tutarı</span>
                <span className="text-lg font-bold text-primary">
                  {formatCurrency(selectedOrder.totalAmount)}
                </span>
              </div>
            </div>
          </DialogContent>
        )}
      </Dialog>
    </Card>
  );
}
