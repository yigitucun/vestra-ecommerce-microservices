"use client";

import * as React from "react";
import { useState, useEffect } from "react";
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
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Skeleton } from "@/components/ui/skeleton";
import { toast } from "@/components/ui/toast";
import { useAdminUsers } from "@/hooks/admin/use-admin-users";
import { AdminUserListItem } from "@/types/user";
import {
  Search,
  X,
  RefreshCw,
  MoreHorizontal,
  Copy,
  Check,
  Shield,
  User as UserIcon,
  Users as UsersIcon,
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  AlertCircle,
  Mail,
} from "lucide-react";
import { cn } from "@/lib/utils";

export function UsersDataTable() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(15);
  const [searchTerm, setSearchTerm] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState("");
  const [copiedId, setCopiedId] = useState<string | null>(null);

  // Arama girdisini debounce et (350ms)
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(searchTerm);
      setPage(0); // Arama değiştiğinde ilk sayfaya dön
    }, 350);

    return () => clearTimeout(timer);
  }, [searchTerm]);

  const { data, isPending, isFetching, isError, error, refetch } = useAdminUsers({
    page,
    size,
    search: debouncedSearch,
  });

  // Spring Pageable dönüşünü normalize et
  const users: AdminUserListItem[] = data?.content ?? [];
  const totalElements = data?.page?.totalElements ?? data?.totalElements ?? 0;
  const totalPages = data?.page?.totalPages ?? data?.totalPages ?? 0;

  const from = totalElements === 0 ? 0 : page * size + 1;
  const to = Math.min((page + 1) * size, totalElements);

  const getInitials = (firstName?: string, lastName?: string) => {
    const first = firstName?.[0] || "";
    const last = lastName?.[0] || "";
    return (first + last).toUpperCase() || "U";
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

  const formatRole = (role: string) => {
    const normalized = role?.toUpperCase() || "";
    if (normalized.includes("ADMIN")) {
      return (
        <Badge
          variant="default"
          className="bg-indigo-600 hover:bg-indigo-700 text-white gap-1 text-xs"
        >
          <Shield className="size-3" />
          Yönetici
        </Badge>
      );
    }
    if (normalized.includes("USER")) {
      return (
        <Badge variant="secondary" className="gap-1 text-xs">
          <UserIcon className="size-3" />
          Kullanıcı
        </Badge>
      );
    }
    return (
      <Badge variant="outline" className="text-xs">
        {role}
      </Badge>
    );
  };

  return (
    <Card className="w-full">
      <CardHeader className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4">
        <div>
          <div className="flex items-center gap-2">
            <CardTitle className="text-xl font-bold flex items-center gap-2">
              <UsersIcon className="size-5 text-primary" />
              Kullanıcı Yönetimi
            </CardTitle>
            {!isPending && (
              <Badge variant="secondary" className="text-xs font-normal">
                {totalElements} Kullanıcı
              </Badge>
            )}
          </div>
          <CardDescription className="mt-1">
            Sistemdeki tüm kayıtlı kullanıcıları listeleyin, arayın ve yetkilerini görüntüleyin.
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
        </div>
      </CardHeader>

      <CardContent className="space-y-4">
        {/* Filtre ve Arama Alanı */}
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
          <div className="relative flex-1 max-w-sm">
            <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 size-4 text-muted-foreground pointer-events-none" />
            <Input
              placeholder="Ad, soyad veya e-posta ile ara..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
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
                <SelectItem value="10">10 kullanıcı</SelectItem>
                <SelectItem value="15">15 kullanıcı</SelectItem>
                <SelectItem value="25">25 kullanıcı</SelectItem>
                <SelectItem value="50">50 kullanıcı</SelectItem>
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
                <p className="font-semibold text-sm">Kullanıcı verileri yüklenemedi</p>
                <p className="text-xs opacity-90">
                  {(error as Error)?.message ||
                    "Yetkiniz bu listeyi görüntülemek için yetersiz olabilir (Yönetici rolü gereklidir)."}
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
                <TableHead className="w-[260px]">Kullanıcı</TableHead>
                <TableHead className="w-[140px]">Rol</TableHead>
                <TableHead className="w-[110px]">Durum</TableHead>
                <TableHead className="w-[180px]">Kullanıcı ID</TableHead>
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
                        <Skeleton className="size-8 rounded-full shrink-0" />
                        <div className="space-y-1.5">
                          <Skeleton className="h-4 w-28" />
                          <Skeleton className="h-3 w-40" />
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-5 w-20 rounded-full" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-5 w-16 rounded-full" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-4 w-28" />
                    </TableCell>
                    <TableCell className="text-right">
                      <Skeleton className="size-8 rounded-md ml-auto" />
                    </TableCell>
                  </TableRow>
                ))
              ) : users.length === 0 ? (
                // Boş Durum
                <TableRow>
                  <TableCell colSpan={5} className="h-48 text-center">
                    <div className="flex flex-col items-center justify-center gap-2">
                      <div className="rounded-full bg-muted p-3">
                        <UsersIcon className="size-6 text-muted-foreground" />
                      </div>
                      <p className="font-medium text-sm">Kayıtlı kullanıcı bulunamadı</p>
                      <p className="text-xs text-muted-foreground max-w-sm">
                        {debouncedSearch
                          ? `"${debouncedSearch}" aramasına uygun hiçbir kullanıcı bulunamadı.`
                          : "Henüz sisteme kayıtlı bir kullanıcı bulunmuyor."}
                      </p>
                      {debouncedSearch && (
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => setSearchTerm("")}
                          className="mt-2 text-xs"
                        >
                          Aramayı Temizle
                        </Button>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              ) : (
                // Veri Satırları
                users.map((user) => {
                  const fullName = `${user.firstName} ${user.lastName}`;
                  const isIdCopied = copiedId === user.id;

                  return (
                    <TableRow key={user.id} className="hover:bg-muted/40 transition-colors">
                      {/* Kullanıcı Bilgisi */}
                      <TableCell>
                        <div className="flex items-center gap-3">
                          <Avatar size="sm">
                            <AvatarFallback className="text-xs font-medium">
                              {getInitials(user.firstName, user.lastName)}
                            </AvatarFallback>
                          </Avatar>
                          <div className="flex flex-col min-w-0">
                            <span className="font-medium text-foreground truncate text-sm">
                              {fullName}
                            </span>
                            <span className="text-xs text-muted-foreground truncate flex items-center gap-1">
                              <Mail className="size-3 shrink-0" />
                              {user.email}
                            </span>
                          </div>
                        </div>
                      </TableCell>

                      {/* Rol */}
                      <TableCell>{formatRole(user.role)}</TableCell>

                      {/* Durum */}
                      <TableCell>
                        {user.isActive ? (
                          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20">
                            <span className="size-1.5 rounded-full bg-emerald-500 animate-pulse" />
                            Aktif
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-rose-500/10 text-rose-600 dark:text-rose-400 border border-rose-500/20">
                            <span className="size-1.5 rounded-full bg-rose-500" />
                            Pasif
                          </span>
                        )}
                      </TableCell>

                      {/* Kullanıcı ID */}
                      <TableCell>
                        <div className="flex items-center gap-1.5">
                          <span className="font-mono text-xs text-muted-foreground">
                            {user.id ? `${user.id.slice(0, 8)}...${user.id.slice(-4)}` : "-"}
                          </span>
                          {user.id && (
                            <Button
                              variant="ghost"
                              size="icon-xs"
                              onClick={() => handleCopy(user.id, "Kullanıcı ID")}
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
                                Kullanıcı İşlemleri
                              </DropdownMenuLabel>
                              <DropdownMenuItem
                                onClick={() => handleCopy(user.id, "Kullanıcı ID")}
                                className="gap-2 cursor-pointer"
                              >
                                <Copy className="size-3.5" />
                                ID Kopyala
                              </DropdownMenuItem>
                              <DropdownMenuItem
                                onClick={() => handleCopy(user.email, "E-posta")}
                                className="gap-2 cursor-pointer"
                              >
                                <Mail className="size-3.5" />
                                E-posta Kopyala
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
            Toplam <span className="font-medium text-foreground">{totalElements}</span> kullanıcıdan{" "}
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
  );
}
