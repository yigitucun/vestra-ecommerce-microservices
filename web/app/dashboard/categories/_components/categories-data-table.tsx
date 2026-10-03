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
  useAdminCategories,
  useDeleteCategory,
} from "@/hooks/admin/use-admin-categories";
import { CategoryListItem, CategoryParent } from "@/types/category";
import {
  Search,
  X,
  RefreshCw,
  MoreHorizontal,
  Copy,
  Check,
  FolderTree,
  Folder,
  Layers,
  Trash2,
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  AlertCircle,
  Hash,
  Plus,
  Pencil,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { CreateCategoryDialog } from "./create-category-dialog";
import { UpdateCategoryDialog } from "./update-category-dialog";

export function CategoriesDataTable() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(15);
  const [searchTerm, setSearchTerm] = useState("");
  const [copiedId, setCopiedId] = useState<string | null>(null);

  // Silme modalı durumu
  const [categoryToDelete, setCategoryToDelete] = useState<CategoryListItem | null>(
    null
  );

  // Güncelleme modalı durumu
  const [categoryToEdit, setCategoryToEdit] = useState<CategoryListItem | null>(
    null
  );

  const { data: allCategories = [], isPending, isFetching, isError, error, refetch } =
    useAdminCategories();

  const deleteCategoryMutation = useDeleteCategory();

  // Arama filtresi
  const filteredCategories = useMemo(() => {
    if (!searchTerm.trim()) return allCategories;
    const term = searchTerm.toLowerCase().trim();
    return allCategories.filter((c) => {
      const nameMatch = c.name?.toLowerCase().includes(term);
      const slugMatch = c.slug?.toLowerCase().includes(term);
      const parentName = getParentName(c.parent)?.toLowerCase();
      const parentMatch = parentName ? parentName.includes(term) : false;
      return nameMatch || slugMatch || parentMatch;
    });
  }, [allCategories, searchTerm]);

  // Sayfalama (Pagination)
  const totalElements = filteredCategories.length;
  const totalPages = Math.ceil(totalElements / size);
  const paginatedCategories = useMemo(() => {
    const start = page * size;
    return filteredCategories.slice(start, start + size);
  }, [filteredCategories, page, size]);

  const from = totalElements === 0 ? 0 : page * size + 1;
  const to = Math.min((page + 1) * size, totalElements);

  function getParentName(parent?: CategoryParent | CategoryParent[] | null): string | null {
    if (!parent) return null;
    if (Array.isArray(parent)) {
      return parent.length > 0 ? parent[0].name : null;
    }
    return parent.name || null;
  }

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
    if (!categoryToDelete) return;
    deleteCategoryMutation.mutate(categoryToDelete.id, {
      onSuccess: () => {
        toast.add({
          title: "Silindi",
          description: `"${categoryToDelete.name}" kategorisi başarıyla silindi.`,
        });
        setCategoryToDelete(null);
      },
      onError: (err: unknown) => {
        toast.add({
          title: "Hata",
          description:
            (err as Error)?.message || "Kategori silinirken bir hata oluştu.",
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
                <FolderTree className="size-5 text-primary" />
                Kategori Yönetimi
              </CardTitle>
              {!isPending && (
                <Badge variant="secondary" className="text-xs font-normal">
                  {totalElements} Kategori
                </Badge>
              )}
            </div>
            <CardDescription className="mt-1">
              Ürün kategorilerini listeleyin, arayın ve hiyerarşik yapılarını yönetin.
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
            <CreateCategoryDialog />
          </div>
        </CardHeader>

        <CardContent className="space-y-4">
          {/* Filtre ve Arama Alanı */}
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
            <div className="relative flex-1 max-w-sm">
              <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 size-4 text-muted-foreground pointer-events-none" />
              <Input
                placeholder="Kategori adı veya slug ile ara..."
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value);
                  setPage(0);
                }}
                className="pl-8 pr-8 h-9"
              />
              {searchTerm && (
                <button
                  type="button"
                  onClick={() => {
                    setSearchTerm("");
                    setPage(0);
                  }}
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
                  <SelectItem value="10">10 kategori</SelectItem>
                  <SelectItem value="15">15 kategori</SelectItem>
                  <SelectItem value="25">25 kategori</SelectItem>
                  <SelectItem value="50">50 kategori</SelectItem>
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
                  <p className="font-semibold text-sm">Kategori verileri yüklenemedi</p>
                  <p className="text-xs opacity-90">
                    {(error as Error)?.message ||
                      "Kategoriler alınırken bir sorun oluştu. Lütfen bağlantınızı kontrol edin."}
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
                  <TableHead className="w-[260px]">Kategori Adı</TableHead>
                  <TableHead className="w-[180px]">Slug</TableHead>
                  <TableHead className="w-[160px]">Üst Kategori</TableHead>
                  <TableHead className="w-[160px]">Kategori ID</TableHead>
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
                          <Skeleton className="size-8 rounded-lg shrink-0" />
                          <div className="space-y-1.5">
                            <Skeleton className="h-4 w-32" />
                            <Skeleton className="h-3 w-48" />
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Skeleton className="h-5 w-24 rounded-md" />
                      </TableCell>
                      <TableCell>
                        <Skeleton className="h-5 w-20 rounded-full" />
                      </TableCell>
                      <TableCell>
                        <Skeleton className="h-4 w-24" />
                      </TableCell>
                      <TableCell className="text-right">
                        <Skeleton className="size-8 rounded-md ml-auto" />
                      </TableCell>
                    </TableRow>
                  ))
                ) : paginatedCategories.length === 0 ? (
                  // Boş Durum
                  <TableRow>
                    <TableCell colSpan={5} className="h-48 text-center">
                      <div className="flex flex-col items-center justify-center gap-2">
                        <div className="rounded-full bg-muted p-3">
                          <Folder className="size-6 text-muted-foreground" />
                        </div>
                        <p className="font-medium text-sm">Kategori bulunamadı</p>
                        <p className="text-xs text-muted-foreground max-w-sm">
                          {searchTerm
                            ? `"${searchTerm}" aramasına uygun hiçbir kategori bulunamadı.`
                            : "Henüz kayıtlı bir kategori bulunmuyor."}
                        </p>
                        {searchTerm ? (
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => {
                              setSearchTerm("");
                              setPage(0);
                            }}
                            className="mt-2 text-xs"
                          >
                            Aramayı Temizle
                          </Button>
                        ) : (
                          <CreateCategoryDialog
                            trigger={
                              <Button size="sm" className="mt-2 text-xs gap-1.5">
                                <Plus className="size-3.5" />
                                İlk Kategoriyi Ekle
                              </Button>
                            }
                          />
                        )}
                      </div>
                    </TableCell>
                  </TableRow>
                ) : (
                  // Veri Satırları
                  paginatedCategories.map((category) => {
                    const isIdCopied = copiedId === category.id;
                    const parentName = getParentName(category.parent);

                    return (
                      <TableRow
                        key={category.id}
                        className="hover:bg-muted/40 transition-colors"
                      >
                        {/* Kategori Adı & Açıklama */}
                        <TableCell>
                          <div className="flex items-center gap-3">
                            <div className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary">
                              <Folder className="size-4" />
                            </div>
                            <div className="flex flex-col min-w-0">
                              <span className="font-medium text-foreground truncate text-sm">
                                {category.name}
                              </span>
                              {category.description && (
                                <span className="text-xs text-muted-foreground truncate max-w-xs">
                                  {category.description}
                                </span>
                              )}
                            </div>
                          </div>
                        </TableCell>

                        {/* Slug */}
                        <TableCell>
                          <span className="inline-flex items-center gap-1 font-mono text-xs text-muted-foreground bg-muted px-2 py-0.5 rounded-md">
                            <Hash className="size-3 text-muted-foreground/70" />
                            {category.slug}
                          </span>
                        </TableCell>

                        {/* Üst Kategori */}
                        <TableCell>
                          {parentName ? (
                            <Badge variant="secondary" className="gap-1 text-xs">
                              <Layers className="size-3" />
                              {parentName}
                            </Badge>
                          ) : (
                            <Badge
                              variant="outline"
                              className="text-xs text-muted-foreground"
                            >
                              Ana Kategori
                            </Badge>
                          )}
                        </TableCell>

                        {/* Kategori ID */}
                        <TableCell>
                          <div className="flex items-center gap-1.5">
                            <span className="font-mono text-xs text-muted-foreground">
                              {category.id
                                ? `${category.id.slice(0, 8)}...${category.id.slice(-4)}`
                                : "-"}
                            </span>
                            {category.id && (
                              <Button
                                variant="ghost"
                                size="icon-xs"
                                onClick={() => handleCopy(category.id, "Kategori ID")}
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
                                  Kategori İşlemleri
                                </DropdownMenuLabel>
                                <DropdownMenuItem
                                  onClick={() => handleCopy(category.id, "Kategori ID")}
                                  className="gap-2 cursor-pointer"
                                >
                                  <Copy className="size-3.5" />
                                  ID Kopyala
                                </DropdownMenuItem>
                                <DropdownMenuItem
                                  onClick={() => handleCopy(category.slug, "Slug")}
                                  className="gap-2 cursor-pointer"
                                >
                                  <Hash className="size-3.5" />
                                  Slug Kopyala
                                </DropdownMenuItem>
                                <DropdownMenuItem
                                  onClick={() => setCategoryToEdit(category)}
                                  className="gap-2 cursor-pointer"
                                >
                                  <Pencil className="size-3.5" />
                                  Düzenle
                                </DropdownMenuItem>
                              </DropdownMenuGroup>
                              <DropdownMenuSeparator />
                              <DropdownMenuGroup>
                                <DropdownMenuItem variant={"destructive"}
                                  onClick={() => setCategoryToDelete(category)}
                                  className="cursor-pointer"
                                >
                                  <Trash2 className="size-3.5" />
                                  Kategoriyi Sil
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
              Toplam <span className="font-medium text-foreground">{totalElements}</span> kategoriden{" "}
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
        open={!!categoryToDelete}
        onOpenChange={(open) => {
          if (!open) setCategoryToDelete(null);
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogMedia className="text-destructive bg-destructive/10">
              <AlertCircle className="size-5" />
            </AlertDialogMedia>
            <AlertDialogTitle>Kategoriyi Sil</AlertDialogTitle>
            <AlertDialogDescription>
              <strong className="text-foreground font-semibold">
                "{categoryToDelete?.name}"
              </strong>{" "}
              adlı kategoriyi silmek istediğinizden emin misiniz? Bu işlem geri
              alınamaz ve alt kategorileri etkileyebilir.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel
              disabled={deleteCategoryMutation.isPending}
              onClick={() => setCategoryToDelete(null)}
            >
              Vazgeç
            </AlertDialogCancel>
            <AlertDialogAction
              variant="destructive"
              onClick={handleDeleteConfirm}
              disabled={deleteCategoryMutation.isPending}
            >
              {deleteCategoryMutation.isPending ? "Siliniyor..." : "Evet, Sil"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Kategori Güncelleme Modalı */}
      <UpdateCategoryDialog
        category={categoryToEdit}
        open={!!categoryToEdit}
        onOpenChange={(open) => {
          if (!open) setCategoryToEdit(null);
        }}
      />
    </>
  );
}
