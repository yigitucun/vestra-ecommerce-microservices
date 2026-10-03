"use client";

import * as React from "react";
import { useState } from "react";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  DialogTrigger,
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
  useCreateCategory,
} from "@/hooks/admin/use-admin-categories";
import {
  createCategorySchema,
  CreateCategorySchema,
  slugify,
  CreateCategoryPayload,
} from "@/types/category";
import { Plus, FolderPlus, AlertCircle } from "lucide-react";

interface CreateCategoryDialogProps {
  trigger?: React.ReactNode;
}

export function CreateCategoryDialog({ trigger }: CreateCategoryDialogProps) {
  const [open, setOpen] = useState(false);
  const [isSlugCustomized, setIsSlugCustomized] = useState(false);

  const { data: categories = [] } = useAdminCategories();
  const createCategoryMutation = useCreateCategory();

  const form = useForm<CreateCategorySchema>({
    resolver: zodResolver(createCategorySchema),
    defaultValues: {
      name: "",
      slug: "",
      description: "",
      parentId: "none",
    },
  });

  const handleNameChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    onChange: (...event: unknown[]) => void
  ) => {
    const newName = e.target.value;
    onChange(newName);

    // Slug kullanıcı tarafından manuel değiştirilmediyse otomatik üret
    if (!isSlugCustomized) {
      form.setValue("slug", slugify(newName), { shouldValidate: true });
    }
  };

  const handleSlugChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    onChange: (...event: unknown[]) => void
  ) => {
    setIsSlugCustomized(true);
    onChange(slugify(e.target.value));
  };

  const onSubmit = (values: CreateCategorySchema) => {
    const payload: CreateCategoryPayload = {
      name: values.name.trim(),
      slug: values.slug.trim(),
      description: values.description?.trim() || undefined,
      parentId:
        values.parentId && values.parentId !== "none"
          ? values.parentId
          : undefined,
    };

    createCategoryMutation.mutate(payload, {
      onSuccess: () => {
        toast.add({
          title: "Başarılı",
          description: `"${payload.name}" kategorisi başarıyla oluşturuldu.`,
        });
        form.reset({
          name: "",
          slug: "",
          description: "",
          parentId: "none",
        });
        setIsSlugCustomized(false);
        setOpen(false);
      },
      onError: (err: unknown) => {
        toast.add({
          title: "Hata",
          description:
            (err as Error)?.message || "Kategori oluşturulurken bir hata oluştu.",
        });
      },
    });
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger
        render={
          trigger ? (
            (trigger as React.ReactElement)
          ) : (
            <Button size="sm" className="gap-1.5 h-8">
              <Plus className="size-4" />
              Yeni Kategori Ekle
            </Button>
          )
        }
      />
      <DialogContent className="sm:max-w-[480px]">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <div className="flex size-9 items-center justify-center rounded-lg bg-primary/10 text-primary">
              <FolderPlus className="size-5" />
            </div>
            <div>
              <DialogTitle className="text-base font-semibold">
                Yeni Kategori Ekle
              </DialogTitle>
              <DialogDescription className="text-xs">
                Kataloğunuz için yeni bir ana veya alt kategori tanımlayın.
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        {createCategoryMutation.isError && (
          <div className="flex items-center gap-2 rounded-lg border border-destructive/30 bg-destructive/10 p-3 text-destructive text-xs">
            <AlertCircle className="size-4 shrink-0" />
            <span>
              {(createCategoryMutation.error as Error)?.message ||
                "Kategori kaydedilirken bir sorun oluştu."}
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
                      {categories.map((c) => (
                        <SelectItem key={c.id} value={c.id}>
                          {c.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FieldDescription className="text-[11px]">
                    Eğer bu kategori başka bir kategorinin altındaysa seçebilirsiniz.
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
              onClick={() => {
                form.reset();
                setIsSlugCustomized(false);
                setOpen(false);
              }}
              disabled={createCategoryMutation.isPending}
            >
              Vazgeç
            </Button>
            <Button
              type="submit"
              size="sm"
              disabled={createCategoryMutation.isPending}
              className="gap-1.5"
            >
              {createCategoryMutation.isPending && <Spinner className="size-3.5" />}
              {createCategoryMutation.isPending ? "Kaydediliyor..." : "Kategori Ekle"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
