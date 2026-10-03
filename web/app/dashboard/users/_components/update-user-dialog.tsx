"use client";

import * as React from "react";
import { useEffect } from "react";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from "@/components/ui/select";
import {
  Field,
  FieldLabel,
  FieldError,
  FieldDescription,
} from "@/components/ui/field";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
import { toast } from "@/components/ui/toast";
import { useUpdateUser } from "@/hooks/admin/use-admin-users";
import {
  updateUserSchema,
  UpdateUserFormValues,
  UpdateUserPayload,
  AdminUserListItem,
} from "@/types/user";
import { UserPen, AlertCircle, Shield, User, Mail, Lock } from "lucide-react";

interface UpdateUserDialogProps {
  user: AdminUserListItem | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function UpdateUserDialog({
  user,
  open,
  onOpenChange,
}: UpdateUserDialogProps) {
  const updateUserMutation = useUpdateUser();

  const form = useForm<UpdateUserFormValues>({
    resolver: zodResolver(updateUserSchema),
    defaultValues: {
      firstName: "",
      lastName: "",
      email: "",
      role: "CUSTOMER",
      isActive: true,
      password: "",
    },
  });

  useEffect(() => {
    if (user && open) {
      form.reset({
        firstName: user.firstName || "",
        lastName: user.lastName || "",
        email: user.email || "",
        role: (user.role === "ADMIN" ? "ADMIN" : "CUSTOMER") as "ADMIN" | "CUSTOMER",
        isActive: Boolean(user.isActive),
        password: "",
      });
    }
  }, [user, open, form]);

  const onSubmit = (values: UpdateUserFormValues) => {
    if (!user) return;

    const payload: UpdateUserPayload = {
      firstName: values.firstName.trim(),
      lastName: values.lastName.trim(),
      email: values.email.trim().toLowerCase(),
      role: values.role,
      isActive: values.isActive,
      password: values.password?.trim() ? values.password.trim() : undefined,
    };

    updateUserMutation.mutate(
      { id: user.id, payload },
      {
        onSuccess: () => {
          toast.add({
            title: "Başarılı",
            description: `"${payload.firstName} ${payload.lastName}" kullanıcısı başarıyla güncellendi.`,
          });
          onOpenChange(false);
        },
        onError: (err: unknown) => {
          toast.add({
            title: "Hata",
            description:
              (err as Error)?.message || "Kullanıcı güncellenirken bir hata oluştu.",
          });
        },
      }
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[540px]">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <div className="flex size-9 items-center justify-center rounded-lg bg-primary/10 text-primary">
              <UserPen className="size-5" />
            </div>
            <div>
              <DialogTitle className="text-base font-semibold">
                Kullanıcıyı Düzenle
              </DialogTitle>
              <DialogDescription className="text-xs">
                Kullanıcı bilgilerini, rolünü, erişim durumunu veya şifresini güncelleyin.
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        {updateUserMutation.isError && (
          <div className="flex items-center gap-2 rounded-lg border border-destructive/30 bg-destructive/10 p-3 text-destructive text-xs">
            <AlertCircle className="size-4 shrink-0" />
            <span>
              {updateUserMutation.error instanceof Error
                ? updateUserMutation.error.message
                : "Kullanıcı güncellenirken bir hata oluştu."}
            </span>
          </div>
        )}

        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          {/* Ad & Soyad */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Controller
              name="firstName"
              control={form.control}
              render={({ field, fieldState }) => (
                <Field data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor={field.name} className="text-xs flex items-center gap-1">
                    <User className="size-3 text-muted-foreground" />
                    Ad *
                  </FieldLabel>
                  <Input
                    {...field}
                    id={field.name}
                    placeholder="Ad"
                    className="h-9 text-xs"
                    aria-invalid={fieldState.invalid}
                  />
                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} className="text-xs" />
                  )}
                </Field>
              )}
            />

            <Controller
              name="lastName"
              control={form.control}
              render={({ field, fieldState }) => (
                <Field data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor={field.name} className="text-xs flex items-center gap-1">
                    <User className="size-3 text-muted-foreground" />
                    Soyad *
                  </FieldLabel>
                  <Input
                    {...field}
                    id={field.name}
                    placeholder="Soyad"
                    className="h-9 text-xs"
                    aria-invalid={fieldState.invalid}
                  />
                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} className="text-xs" />
                  )}
                </Field>
              )}
            />
          </div>

          {/* E-posta */}
          <Controller
            name="email"
            control={form.control}
            render={({ field, fieldState }) => (
              <Field data-invalid={fieldState.invalid}>
                <FieldLabel htmlFor={field.name} className="text-xs flex items-center gap-1">
                  <Mail className="size-3 text-muted-foreground" />
                  E-posta Adresi *
                </FieldLabel>
                <Input
                  {...field}
                  id={field.name}
                  type="email"
                  placeholder="ornek@vestra.com"
                  className="h-9 text-xs font-mono"
                  aria-invalid={fieldState.invalid}
                />
                {fieldState.invalid && (
                  <FieldError errors={[fieldState.error]} className="text-xs" />
                )}
              </Field>
            )}
          />

          {/* Rol & Hesap Durumu */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Controller
              name="role"
              control={form.control}
              render={({ field, fieldState }) => (
                <Field data-invalid={fieldState.invalid}>
                  <FieldLabel className="text-xs flex items-center gap-1">
                    <Shield className="size-3 text-muted-foreground" />
                    Kullanıcı Rolü *
                  </FieldLabel>
                  <Select
                    value={field.value}
                    onValueChange={(val) => field.onChange(val as "ADMIN" | "CUSTOMER")}
                  >
                    <SelectTrigger className="w-full h-9 text-xs">
                      <SelectValue placeholder="Rol seçiniz..." />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="CUSTOMER" className="text-xs">
                        Müşteri (CUSTOMER)
                      </SelectItem>
                      <SelectItem value="ADMIN" className="text-xs">
                        Yönetici (ADMIN)
                      </SelectItem>
                    </SelectContent>
                  </Select>
                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} className="text-xs" />
                  )}
                </Field>
              )}
            />

            <Controller
              name="isActive"
              control={form.control}
              render={({ field, fieldState }) => (
                <Field data-invalid={fieldState.invalid}>
                  <FieldLabel className="text-xs flex items-center gap-1">
                    Hesap Durumu *
                  </FieldLabel>
                  <Select
                    value={field.value ? "true" : "false"}
                    onValueChange={(val) => field.onChange(val === "true")}
                  >
                    <SelectTrigger className="w-full h-9 text-xs">
                      <SelectValue placeholder="Durum seçiniz..." />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="true" className="text-xs">
                        Aktif (Giriş Yapabilir)
                      </SelectItem>
                      <SelectItem value="false" className="text-xs">
                        Pasif (Girişi Engelli)
                      </SelectItem>
                    </SelectContent>
                  </Select>
                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} className="text-xs" />
                  )}
                </Field>
              )}
            />
          </div>

          {/* Yeni Şifre (Opsiyonel) */}
          <Controller
            name="password"
            control={form.control}
            render={({ field, fieldState }) => (
              <Field data-invalid={fieldState.invalid}>
                <FieldLabel htmlFor={field.name} className="text-xs flex items-center gap-1">
                  <Lock className="size-3 text-muted-foreground" />
                  Yeni Şifre Belirle (Opsiyonel)
                </FieldLabel>
                <Input
                  {...field}
                  id={field.name}
                  type="password"
                  placeholder="Değiştirmek istemiyorsanız boş bırakınız"
                  className="h-9 text-xs"
                  aria-invalid={fieldState.invalid}
                />
                {fieldState.invalid ? (
                  <FieldError errors={[fieldState.error]} className="text-xs" />
                ) : (
                  <FieldDescription className="text-[11px]">
                    Kullanıcının şifresini değiştirmek istiyorsanız en az 6 karakter giriniz.
                  </FieldDescription>
                )}
              </Field>
            )}
          />

          <DialogFooter className="gap-2 sm:gap-2.5 pt-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => onOpenChange(false)}
              disabled={updateUserMutation.isPending}
            >
              Vazgeç
            </Button>
            <Button
              type="submit"
              size="sm"
              disabled={updateUserMutation.isPending}
              className="gap-1.5"
            >
              {updateUserMutation.isPending && (
                <Spinner className="size-3.5" />
              )}
              {updateUserMutation.isPending ? "Kaydediliyor..." : "Değişiklikleri Kaydet"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
