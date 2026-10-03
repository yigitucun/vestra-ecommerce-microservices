"use client";

import * as React from "react";
import { useEffect, useState } from "react";
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
  FieldGroup,
  FieldDescription,
} from "@/components/ui/field";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Spinner } from "@/components/ui/spinner";
import { toast } from "@/components/ui/toast";
import {
  useAdminCategories,
  useUpdateCategory,
} from "@/hooks/admin/use-admin-categories";
import {
  updateCategorySchema,
  UpdateCategorySchema,
  slugify,
  CategoryListItem,
  CategoryParent,
  UpdateCategoryPayload,
} from "@/types/category";
import { FolderPen, AlertCircle } from "lucide-react";

interface UpdateCategoryDialogProps {
  category: CategoryListItem | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function UpdateCategoryDialog({
  category,
  open,
  onOpenChange,
}: UpdateCategoryDialogProps) {
  const { data: categories = [] } = useAdminCategories();
  const updateCategoryMutation = useUpdateCategory();
  const [isSlugManuallyChanged, setIsSlugManuallyChanged] = useState(false);

  const getParentId = (
    parent?: CategoryParent | CategoryParent[] | null,
    allCategories: CategoryListItem[] = []
  ): string => {
    if (!parent) return "none";
    const p = Array.isArray(parent) ? parent[0] : parent;
    if (!p) return "none";
    if (p.id) return p.id;
    const match = allCategories.find((c) => c.name === p.name);
    return match ? match.id : "none";
  };

  const form = useForm<UpdateCategorySchema>({
    resolver: zodResolver(updateCategorySchema),
    defaultValues: {
      name: "",
      slug: "",
      description: "",
      parentId: "none",
    },
  });

  useEffect(() => {
    if (category && open) {
      const parentId = getParentId(category.parent, categories);
      form.reset({
        name: category.name || "",
        slug: category.slug || "",
        description: category.description || "",
        parentId,
      });
      setIsSlugManuallyChanged(false);
    }
  }, [category, open, categories]);

  const handleNameChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    onChange: (...event: unknown[]) => void
  ) => {
    const newName = e.target.value;
    onChange(newName);

    // Eğer slug kullanıcı tarafından manuel değiştirilmediyse otomatik güncelle
    if (!isSlugManuallyChanged) {
      form.setValue("slug", slugify(newName), { shouldValidate: true });
    }
  };

  const handleSlugChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    onChange: (...event: unknown[]) => void
  ) => {
    setIsSlugManuallyChanged(true);
    onChange(slugify(e.target.value));
  };

  const onSubmit = (values: UpdateCategorySchema) => {
    if (!category) return;

    const payload: UpdateCategoryPayload = {
      name: values.name.trim(),
      slug: values.slug.trim(),
      description: values.description?.trim() || undefined,
      parentId:
        values.parentId && values.parentId !== "none"
          ? values.parentId
          : undefined,
    };

    updateCategoryMutation.mutate(
      { id: category.id, payload },
      {
        onSuccess: () => {
          toast.add({
            title: "Güncellendi",
            description: `"${payload.name}" kategorisi başarıyla güncellendi.`,
          });
          onOpenChange(false);
        },
        onError: (err: unknown) => {
          toast.add({
            title: "Hata",
            description:
              (err as Error)?.message || "Kategori güncellenirken bir hata oluştu.",
          });
        },
      }
    );
  };

  // Bir kategori kendisinin üst kategorisi olamaz
  const eligibleParentCategories = categories.filter(
    (c) => c.id !== category?.id
  );

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[480px]">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <div className="flex size-9 items-center justify-center rounded-lg bg-primary/10 text-primary">
              <FolderPen className="size-5" />
            </div>
            <div>
              <DialogTitle className="text-base font-semibold">
                Kategoriyi Güncelle
              </DialogTitle>
              <DialogDescription className="text-xs">
                Kategori adı, URL yolu ve üst kategori bilgilerini düzenleyin.
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        {updateCategoryMutation.isError && (
          <div className="flex items-center gap-2 rounded-lg border border-destructive/30 bg-destructive/10 p-3 text-destructive text-xs">
            <AlertCircle className="size-4 shrink-0" />
            <span>
              {(updateCategoryMutation.error as Error)?.message ||
                "Kategori güncellenirken bir sorun oluştu."}
            </span>
          </div>
        )}

        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4 pt-2">
          <FieldGroup className="gap-3.5">
            {/* Kategori Adı */}
            <Controller
              name="name"
              control={form.control}
              render={({ field, fieldState }) => (
                <Field data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor={field.name}>Kategori Adı *</FieldLabel>
                  <Input
                    {...field}
                    id={field.name}
                    placeholder="Örn: Bilgisayar & Tablet"
                    autoComplete="off"
                    onChange={(e) => handleNameChange(e, field.onChange)}
                    aria-invalid={fieldState.invalid}
                  />
                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} />
                  )}
                </Field>
              )}
            />

            {/* Slug */}
            <Controller
              name="slug"
              control={form.control}
              render={({ field, fieldState }) => (
                <Field data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor={field.name}>URL Yolu (Slug) *</FieldLabel>
                  <Input
                    {...field}
                    id={field.name}
                    placeholder="bilgisayar-tablet"
                    autoComplete="off"
                    onChange={(e) => handleSlugChange(e, field.onChange)}
                    aria-invalid={fieldState.invalid}
                    className="font-mono text-xs"
                  />
                  <FieldDescription className="text-[11px]">
                    URL yapısında görüntülenecek benzersiz tanımlayıcıdır.
                  </FieldDescription>
                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} />
                  )}
                </Field>
              )}
            />

            {/* Üst Kategori */}
            <Controller
              name="parentId"
              control={form.control}
              render={({ field }) => (
                <Field>
                  <FieldLabel htmlFor={field.name}>Üst Kategori</FieldLabel>
                  <Select
                    value={field.value || "none"}
                    onValueChange={(val) => field.onChange(val || "none")}
                  >
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Üst kategori seçiniz" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="none">
                        Ana Kategori (Üst Kategori Yok)
                      </SelectItem>
                      {eligibleParentCategories.map((c) => (
                        <SelectItem key={c.id} value={c.id}>
                          {c.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FieldDescription className="text-[11px]">
                    Kategorinin bağlı olduğu üst kategoriyi değiştirebilirsiniz.
                  </FieldDescription>
                </Field>
              )}
            />

            {/* Açıklama */}
            <Controller
              name="description"
              control={form.control}
              render={({ field, fieldState }) => (
                <Field data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor={field.name}>Açıklama</FieldLabel>
                  <Textarea
                    {...field}
                    id={field.name}
                    placeholder="Kategori hakkında kısa bir açıklama..."
                    rows={3}
                    value={field.value || ""}
                  />
                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} />
                  )}
                </Field>
              )}
            />
          </FieldGroup>

          <DialogFooter className="gap-2 sm:gap-2.5 pt-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => onOpenChange(false)}
              disabled={updateCategoryMutation.isPending}
            >
              Vazgeç
            </Button>
            <Button
              type="submit"
              size="sm"
              disabled={updateCategoryMutation.isPending}
              className="gap-1.5"
            >
              {updateCategoryMutation.isPending && (
                <Spinner className="size-3.5" />
              )}
              {updateCategoryMutation.isPending
                ? "Güncelleniyor..."
                : "Değişiklikleri Kaydet"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
