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
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogMedia,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
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
import {
  useAdminProducts,
  useDeleteProduct,
} from "@/hooks/admin/use-admin-products";
import { ProductListItem } from "@/types/product";
import {
  Search,
  X,
  RefreshCw,
  MoreHorizontal,
  Copy,
  Check,
  Package,
  Layers,
  Trash2,
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  AlertCircle,
  Tag,
  Barcode,
  Plus,
  Pencil,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { CreateProductDialog } from "./create-product-dialog";
import { UpdateProductDialog } from "./update-product-dialog";

export function ProductsDataTable() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(15);
  const [searchTerm, setSearchTerm] = useState("");
  const [copiedId, setCopiedId] = useState<string | null>(null);

  // Güncelleme modalı durumu
  const [productToEdit, setProductToEdit] = useState<ProductListItem | null>(null);

  // Silme modalı durumu
  const [productToDelete, setProductToDelete] = useState<ProductListItem | null>(
    null
  );

  const { data, isPending, isFetching, isError, error, refetch } = useAdminProducts({
    page,
    size,
  });

  const deleteProductMutation = useDeleteProduct();

  const allProducts: ProductListItem[] = data?.content ?? [];
  const totalElements = data?.page?.totalElements ?? data?.totalElements ?? 0;
  const totalPages = data?.page?.totalPages ?? data?.totalPages ?? 0;

  // İstemci tarafı arama filtresi (Backend doğrudan getAll() döndürdüğünde veya mevcut sayfada arama için)
  const filteredProducts = useMemo(() => {
    if (!searchTerm.trim()) return allProducts;
    const term = searchTerm.toLowerCase().trim();
    return allProducts.filter((p) => {
      const nameMatch = p.name?.toLowerCase().includes(term);
      const descMatch = p.description?.toLowerCase().includes(term);
      const catMatch = p.category?.name?.toLowerCase().includes(term);
      const skuMatch = p.variants?.some((v) =>
        v.sku?.toLowerCase().includes(term)
      );
      return nameMatch || descMatch || catMatch || skuMatch;
    });
  }, [allProducts, searchTerm]);

  const from = totalElements === 0 ? 0 : page * size + 1;
  const to = Math.min((page + 1) * size, totalElements);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("tr-TR", {
      style: "currency",
      currency: "TRY",
      minimumFractionDigits: 2,
    }).format(amount);
  };

  const getPriceDisplay = (product: ProductListItem) => {
    const variants = product.variants ?? [];
    if (variants.length === 0) return "-";

    const prices = variants
      .map((v) => Number(v.price))
      .filter((p) => !isNaN(p));

    if (prices.length === 0) return "-";
    const minPrice = Math.min(...prices);
    const maxPrice = Math.max(...prices);

    if (minPrice === maxPrice) {
      return formatCurrency(minPrice);
    }
    return `${formatCurrency(minPrice)} - ${formatCurrency(maxPrice)}`;
  };

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
      // Fallback
    }
  };

  const handleDeleteConfirm = () => {
    if (!productToDelete) return;
    deleteProductMutation.mutate(productToDelete.id, {
      onSuccess: () => {
        toast.add({
          title: "Silindi",
          description: `"${productToDelete.name}" ürünü başarıyla silindi.`,
        });
        setProductToDelete(null);
      },
      onError: (err: unknown) => {
        toast.add({
          title: "Hata",
          description:
            (err as Error)?.message || "Ürün silinirken bir sorun oluştu.",
        });
      },
    });
  };

  return (
    <>
      <Card className="w-full">
        <CardHeader className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4">
          <div>
            <div className="flex items-center gap-2">
              <CardTitle className="text-xl font-bold flex items-center gap-2">
                <Package className="size-5 text-primary" />
                Ürün Yönetimi
              </CardTitle>
              {!isPending && (
                <Badge variant="secondary" className="text-xs font-normal">
                  {totalElements} Ürün
                </Badge>
              )}
            </div>
            <CardDescription className="mt-1">
              Kataloğunuzdaki tüm ürünleri listeleyin, fiyat ve varyantlarını inceleyin.
            </CardDescription>
          </div>

          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => refetch()}
              disabled={isFetching}
              className="h-8 gap-1.5"
              title="Yenile"
            >
              <RefreshCw className={cn("size-3.5", isFetching && "animate-spin")} />
              <span className="hidden sm:inline">Yenile</span>
            </Button>
            <CreateProductDialog />
          </div>
        </CardHeader>

        <CardContent className="space-y-4">
          {/* Filtre ve Arama Alanı */}
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
            <div className="relative flex-1 max-w-sm">
              <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 size-4 text-muted-foreground pointer-events-none" />
              <Input
                placeholder="Ürün adı, kategori veya SKU ile ara..."
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value);
                }}
                className="pl-8 pr-8 h-9"
              />
              {searchTerm && (
                <button
                  type="button"
                  onClick={() => setSearchTerm("")}
                  className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                  title="Aramayı temizle"
                >
                  <X className="size-4" />
                </button>
              )}
            </div>

            <div className="flex items-center gap-2 self-end sm:self-auto">
              <span className="text-xs text-muted-foreground whitespace-nowrap">
                Sayfa başı:
              </span>
              <Select
                value={String(size)}
                onValueChange={(val) => {
                  if (val) {
                    setSize(Number(val));
                    setPage(0);
                  }
                }}
              >
                <SelectTrigger size="sm" className="w-[110px] h-9">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="10">10 ürün</SelectItem>
                  <SelectItem value="15">15 ürün</SelectItem>
                  <SelectItem value="25">25 ürün</SelectItem>
                  <SelectItem value="50">50 ürün</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Hata Durumu */}
          {isError && (
            <div className="flex items-center justify-between rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-destructive">
              <div className="flex items-center gap-3">
                <AlertCircle className="size-5 shrink-0" />
                <div>
                  <p className="font-semibold text-sm">Ürünler yüklenemedi</p>
                  <p className="text-xs opacity-90">
                    {(error as Error)?.message ||
                      "Ürün verileri alınırken bir sorun oluştu. Lütfen bağlantınızı kontrol edin."}
                  </p>
                </div>
              </div>
              <Button
                variant="destructive"
                size="sm"
                onClick={() => refetch()}
                className="shrink-0 text-xs"
              >
                Tekrar Dene
              </Button>
            </div>
          )}

          {/* Tablo */}
          <div className="rounded-lg border bg-card overflow-hidden">
            <Table>
              <TableHeader className="bg-muted/40">
                <TableRow>
                  <TableHead className="w-[280px]">Ürün</TableHead>
                  <TableHead className="w-[140px]">Kategori</TableHead>
                  <TableHead className="w-[140px]">Fiyat</TableHead>
                  <TableHead className="w-[160px]">Varyant / SKU</TableHead>
                  <TableHead className="w-[150px]">Ürün ID</TableHead>
                  <TableHead className="w-[70px] text-right">İşlemler</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isPending ? (
                  // Yüklenme Durumu (Skeleton)
                  Array.from({ length: 6 }).map((_, i) => (
                    <TableRow key={i}>
                      <TableCell>
                        <div className="flex items-center gap-3">
                          <Skeleton className="size-10 rounded-lg shrink-0" />
                          <div className="space-y-1.5">
                            <Skeleton className="h-4 w-36" />
                            <Skeleton className="h-3 w-48" />
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Skeleton className="h-5 w-20 rounded-full" />
                      </TableCell>
                      <TableCell>
                        <Skeleton className="h-4 w-20" />
                      </TableCell>
                      <TableCell>
                        <Skeleton className="h-5 w-24 rounded-md" />
                      </TableCell>
                      <TableCell>
                        <Skeleton className="h-4 w-24" />
                      </TableCell>
                      <TableCell className="text-right">
                        <Skeleton className="size-8 rounded-md ml-auto" />
                      </TableCell>
                    </TableRow>
                  ))
                ) : filteredProducts.length === 0 ? (
                  // Boş Durum
                  <TableRow>
                    <TableCell colSpan={6} className="h-48 text-center">
                      <div className="flex flex-col items-center justify-center gap-2">
                        <div className="rounded-full bg-muted p-3">
                          <Package className="size-6 text-muted-foreground" />
                        </div>
                        <p className="font-medium text-sm">Ürün bulunamadı</p>
                        <p className="text-xs text-muted-foreground max-w-sm">
                          {searchTerm
                            ? `"${searchTerm}" aramasına uygun hiçbir ürün bulunamadı.`
                            : "Henüz kayıtlı bir ürün bulunmuyor."}
                        </p>
                        {searchTerm ? (
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setSearchTerm("")}
                            className="mt-2 text-xs"
                          >
                            Aramayı Temizle
                          </Button>
                        ) : (
                          <CreateProductDialog
                            trigger={
                              <Button size="sm" className="mt-2 text-xs gap-1.5">
                                <Plus className="size-3.5" />
                                İlk Ürünü Ekle
                              </Button>
                            }
                          />
                        )}
                      </div>
                    </TableCell>
                  </TableRow>
                ) : (
                  // Veri Satırları
                  filteredProducts.map((product) => {
                    const isIdCopied = copiedId === product.id;
                    const variants = product.variants ?? [];
                    const firstSku = variants[0]?.sku;

                    return (
                      <TableRow
                        key={product.id}
                        className="hover:bg-muted/40 transition-colors"
                      >
                        {/* Ürün Görsel & İsim */}
                        <TableCell>
                          <div className="flex items-center gap-3">
                            <div className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary border border-border/50">
                              <Package className="size-5" />
                            </div>
                            <div className="flex flex-col min-w-0">
                              <span className="font-medium text-foreground truncate text-sm">
                                {product.name}
                              </span>
                              {product.description ? (
                                <span className="text-xs text-muted-foreground truncate max-w-xs">
                                  {product.description}
                                </span>
                              ) : product.slug ? (
                                <span className="text-xs text-muted-foreground/70 font-mono truncate">
                                  /{product.slug}
                                </span>
                              ) : null}
                            </div>
                          </div>
                        </TableCell>

                        {/* Kategori */}
                        <TableCell>
                          {product.category?.name ? (
                            <Badge variant="secondary" className="gap-1 text-xs">
                              <Tag className="size-3" />
                              {product.category.name}
                            </Badge>
                          ) : (
                            <span className="text-xs text-muted-foreground">-</span>
                          )}
                        </TableCell>

                        {/* Fiyat */}
                        <TableCell>
                          <span className="font-semibold text-sm text-foreground">
                            {getPriceDisplay(product)}
                          </span>
                        </TableCell>

                        {/* Varyant / SKU */}
                        <TableCell>
                          <div className="flex flex-col gap-1">
                            {variants.length > 0 ? (
                              <>
                                <span className="inline-flex items-center gap-1 text-xs font-medium text-foreground">
                                  <Layers className="size-3 text-muted-foreground" />
                                  {variants.length} Varyant
                                </span>
                                {firstSku && (
                                  <span className="font-mono text-[11px] text-muted-foreground inline-flex items-center gap-1">
                                    <Barcode className="size-3" />
                                    {firstSku}
                                  </span>
                                )}
                              </>
                            ) : (
                              <span className="text-xs text-muted-foreground">
                                Varyant yok
                              </span>
                            )}
                          </div>
                        </TableCell>

                        {/* Ürün ID */}
                        <TableCell>
                          <div className="flex items-center gap-1.5">
                            <span className="font-mono text-xs text-muted-foreground">
                              {product.id
                                ? `${product.id.slice(0, 8)}...${product.id.slice(-4)}`
                                : "-"}
                            </span>
                            {product.id && (
                              <Button
                                variant="ghost"
                                size="icon-xs"
                                onClick={() => handleCopy(product.id, "Ürün ID")}
                                title="ID Kopyala"
                                className="text-muted-foreground hover:text-foreground"
                              >
                                {isIdCopied ? (
                                  <Check className="size-3 text-emerald-500" />
                                ) : (
                                  <Copy className="size-3" />
                                )}
                              </Button>
                            )}
                          </div>
                        </TableCell>

                        {/* İşlemler Menüsü */}
                        <TableCell className="text-right">
                          <DropdownMenu>
                            <DropdownMenuTrigger
                              render={
                                <Button
                                  variant="ghost"
                                  size="icon-sm"
                                  className="text-muted-foreground hover:text-foreground"
                                />
                              }
                            >
                              <MoreHorizontal className="size-4" />
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="w-48">
                              <DropdownMenuGroup>
                                <DropdownMenuLabel className="text-xs">
                                  Ürün İşlemleri
                                </DropdownMenuLabel>
                                <DropdownMenuItem
                                  onClick={() => handleCopy(product.id, "Ürün ID")}
                                  className="gap-2 cursor-pointer"
                                >
                                  <Copy className="size-3.5" />
                                  ID Kopyala
                                </DropdownMenuItem>
                                {firstSku && (
                                  <DropdownMenuItem
                                    onClick={() => handleCopy(firstSku, "SKU")}
                                    className="gap-2 cursor-pointer"
                                  >
                                    <Barcode className="size-3.5" />
                                    SKU Kopyala
                                  </DropdownMenuItem>
                                )}
                                <DropdownMenuItem
                                  onClick={() => setProductToEdit(product)}
                                  className="gap-2 cursor-pointer"
                                >
                                  <Pencil className="size-3.5" />
                                  Düzenle
                                </DropdownMenuItem>
                              </DropdownMenuGroup>
                              <DropdownMenuSeparator />
                              <DropdownMenuGroup>
                                <DropdownMenuItem variant={"destructive"} onClick={() => setProductToDelete(product)} className="cursor-pointer">
                                  <Trash2 className="size-3.5" />
                                  Ürünü Sil
                                </DropdownMenuItem>
                              </DropdownMenuGroup>
                            </DropdownMenuContent>
                          </DropdownMenu>
                        </TableCell>
                      </TableRow>
                    );
                  })
                )}
              </TableBody>
            </Table>
          </div>

          {/* Sayfalama (Pagination) Alt Barı */}
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-2">
            <p className="text-xs text-muted-foreground order-2 sm:order-1">
              Toplam <span className="font-medium text-foreground">{totalElements}</span> üründen{" "}
              <span className="font-medium text-foreground">{from}</span> -{" "}
              <span className="font-medium text-foreground">{to}</span> arası gösteriliyor.
            </p>

            <div className="flex items-center gap-1.5 order-1 sm:order-2">
              <Button
                variant="outline"
                size="icon-sm"
                onClick={() => setPage(0)}
                disabled={page === 0 || isPending || isFetching}
                title="İlk Sayfa"
              >
                <ChevronsLeft className="size-4" />
              </Button>
              <Button
                variant="outline"
                size="icon-sm"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0 || isPending || isFetching}
                title="Önceki Sayfa"
              >
                <ChevronLeft className="size-4" />
              </Button>

              <span className="text-xs font-medium px-3 text-muted-foreground whitespace-nowrap">
                Sayfa <span className="text-foreground">{totalPages > 0 ? page + 1 : 1}</span> /{" "}
                <span className="text-foreground">{totalPages > 0 ? totalPages : 1}</span>
              </span>

              <Button
                variant="outline"
                size="icon-sm"
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1 || totalPages === 0 || isPending || isFetching}
                title="Sonraki Sayfa"
              >
                <ChevronRight className="size-4" />
              </Button>
              <Button
                variant="outline"
                size="icon-sm"
                onClick={() => setPage(Math.max(0, totalPages - 1))}
                disabled={page >= totalPages - 1 || totalPages === 0 || isPending || isFetching}
                title="Son Sayfa"
              >
                <ChevronsRight className="size-4" />
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Silme Onay Modalı */}
      <AlertDialog
        open={!!productToDelete}
        onOpenChange={(open) => {
          if (!open) setProductToDelete(null);
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogMedia className="text-destructive bg-destructive/10">
              <AlertCircle className="size-5" />
            </AlertDialogMedia>
            <AlertDialogTitle>Ürünü Sil</AlertDialogTitle>
            <AlertDialogDescription>
              <strong className="text-foreground font-semibold">
                "{productToDelete?.name}"
              </strong>{" "}
              adlı ürünü silmek istediğinizden emin misiniz? Bu ürüne bağlı tüm varyantlar ve kayıtlar silinecektir.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel
              disabled={deleteProductMutation.isPending}
              onClick={() => setProductToDelete(null)}
            >
              Vazgeç
            </AlertDialogCancel>
            <AlertDialogAction
              variant="destructive"
              onClick={handleDeleteConfirm}
              disabled={deleteProductMutation.isPending}
            >
              {deleteProductMutation.isPending ? "Siliniyor..." : "Evet, Sil"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Ürün Güncelleme Modalı */}
      {productToEdit && (
        <UpdateProductDialog
          product={productToEdit}
          open={!!productToEdit}
          onOpenChange={(open) => {
            if (!open) setProductToEdit(null);
          }}
        />
      )}
    </>
  );
}
