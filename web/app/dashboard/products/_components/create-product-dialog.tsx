"use client";

import * as React from "react";
import { useState } from "react";
import { useForm, useFieldArray, Controller } from "react-hook-form";
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
import { Badge } from "@/components/ui/badge";
import { Spinner } from "@/components/ui/spinner";
import { toast } from "@/components/ui/toast";
import { useAdminCategories } from "@/hooks/admin/use-admin-categories";
import { useCreateProduct } from "@/hooks/admin/use-admin-products";
import {
  createProductSchema,
  CreateProductFormValues,
  CreateProductPayload,
} from "@/types/product";
import { slugify } from "@/types/category";
import {
  Plus,
  PackagePlus,
  Trash2,
  Layers,
  AlertCircle,
  Tag,
  Barcode,
  Coins,
  Warehouse,
} from "lucide-react";
import { cn } from "@/lib/utils";

interface CreateProductDialogProps {
  trigger?: React.ReactNode;
}

export function CreateProductDialog({ trigger }: CreateProductDialogProps) {
  const [open, setOpen] = useState(false);
  const [isSlugCustomized, setIsSlugCustomized] = useState(false);

  const { data: categories = [] } = useAdminCategories();
  const createProductMutation = useCreateProduct();

  const form = useForm<CreateProductFormValues>({
    resolver: zodResolver(createProductSchema),
    defaultValues: {
      name: "",
      slug: "",
      description: "",
      imageUrl: "",
      categoryId: "",
      variants: [
        {
          sku: "",
          price: 0,
          initialStock: 0,
          attributes: [],
        },
      ],
    },
  });

  const {
    fields: variantFields,
    append: appendVariant,
    remove: removeVariant,
  } = useFieldArray({
    control: form.control,
    name: "variants",
  });

  const handleNameChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    onChange: (...event: unknown[]) => void
  ) => {
    const newName = e.target.value;
    onChange(newName);

    if (!isSlugCustomized) {
      const generatedSlug = slugify(newName);
      form.setValue("slug", generatedSlug, { shouldValidate: true });

      // İlk varyantın SKU'su boşsa otomatik öneri sun
      const currentVariants = form.getValues("variants");
      if (currentVariants.length === 1 && !currentVariants[0]?.sku) {
        form.setValue(
          "variants.0.sku",
          `${generatedSlug.toUpperCase().slice(0, 8)}-001`
        );
      }
    }
  };

  const handleSlugChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    onChange: (...event: unknown[]) => void
  ) => {
    setIsSlugCustomized(true);
    onChange(slugify(e.target.value));
  };

  const handleAddVariant = () => {
    const currentVariants = form.getValues("variants");
    const count = currentVariants.length + 1;
    const baseSlug = form.getValues("slug") || "PRD";
    const skuPrefix = baseSlug.toUpperCase().slice(0, 8);

    appendVariant({
      sku: `${skuPrefix}-00${count}`,
      price: currentVariants[0]?.price || 0,
      initialStock: 10,
      attributes: [],
    });
  };

  const onSubmit = (values: CreateProductFormValues) => {
    const payload: CreateProductPayload = {
      name: values.name.trim(),
      slug: values.slug.trim(),
      description: values.description?.trim() || undefined,
      imageUrl: values.imageUrl?.trim() || undefined,
      categoryId: values.categoryId,
      variants: values.variants.map((v) => ({
        sku: v.sku.trim(),
        price: Number(v.price),
        initialStock: Number(v.initialStock),
        attributes: (v.attributes || [])
          .filter(
            (attr) => attr.attributeName?.trim() && attr.attributeValue?.trim()
          )
          .map((attr) => ({
            attributeName: attr.attributeName.trim(),
            attributeValue: attr.attributeValue.trim(),
          })),
      })),
    };

    createProductMutation.mutate(payload, {
      onSuccess: () => {
        toast.add({
          title: "Başarılı",
          description: `"${payload.name}" ürünü ve varyantları başarıyla oluşturuldu.`,
        });
        form.reset({
          name: "",
          slug: "",
          description: "",
          imageUrl: "",
          categoryId: "",
          variants: [
            {
              sku: "",
              price: 0,
              initialStock: 0,
              attributes: [],
            },
          ],
        });
        setIsSlugCustomized(false);
        setOpen(false);
      },
      onError: (err: unknown) => {
        toast.add({
          title: "Hata",
          description:
            (err as Error)?.message || "Ürün kaydedilirken bir hata oluştu.",
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
              <span>Yeni Ürün Ekle</span>
            </Button>
          )
        }
      />
      <DialogContent className="sm:max-w-[700px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <div className="flex size-9 items-center justify-center rounded-lg bg-primary/10 text-primary">
              <PackagePlus className="size-5" />
            </div>
            <div>
              <DialogTitle className="text-base font-semibold">
                Yeni Ürün Ekle
              </DialogTitle>
              <DialogDescription className="text-xs">
                Kataloğunuza yeni bir ürün, fiyat ve başlangıç stoklu varyantlar ekleyin.
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        {createProductMutation.isError && (
          <div className="flex items-center gap-2 rounded-lg border border-destructive/30 bg-destructive/10 p-3 text-destructive text-xs">
            <AlertCircle className="size-4 shrink-0" />
            <span>
              {(createProductMutation.error as Error)?.message ||
                "Ürün kaydedilirken bir sorun oluştu."}
            </span>
          </div>
        )}

        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6 pt-2">
          {/* 1. Bölüm: Genel Ürün Bilgileri */}
          <div className="space-y-4">
            <div className="flex items-center gap-2 pb-1 border-b">
              <Tag className="size-4 text-primary" />
              <h3 className="font-semibold text-xs tracking-wider uppercase text-foreground">
                Genel Bilgiler
              </h3>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {/* Ürün Adı */}
              <Controller
                name="name"
                control={form.control}
                render={({ field, fieldState }) => (
                  <Field data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor={field.name}>Ürün Adı *</FieldLabel>
                    <Input
                      {...field}
                      id={field.name}
                      placeholder="Örn: Oversize Pamuklu T-Shirt"
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

              {/* URL Yolu (Slug) */}
              <Controller
                name="slug"
                control={form.control}
                render={({ field, fieldState }) => (
                  <Field data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor={field.name}>URL Yolu (Slug) *</FieldLabel>
                    <Input
                      {...field}
                      id={field.name}
                      placeholder="oversize-pamuklu-tshirt"
                      autoComplete="off"
                      onChange={(e) => handleSlugChange(e, field.onChange)}
                      aria-invalid={fieldState.invalid}
                      className="font-mono text-xs"
                    />
                    {fieldState.invalid && (
                      <FieldError errors={[fieldState.error]} />
                    )}
                  </Field>
                )}
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {/* Kategori Seçimi */}
              <Controller
                name="categoryId"
                control={form.control}
                render={({ field, fieldState }) => (
                  <Field data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor={field.name}>Kategori *</FieldLabel>
                    <Select
                      value={field.value}
                      onValueChange={(val) => field.onChange(val)}
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue placeholder="Kategori seçiniz" />
                      </SelectTrigger>
                      <SelectContent>
                        {categories.map((c) => (
                          <SelectItem key={c.id} value={c.id}>
                            {c.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {fieldState.invalid && (
                      <FieldError errors={[fieldState.error]} />
                    )}
                  </Field>
                )}
              />

              {/* Görsel URL */}
              <Controller
                name="imageUrl"
                control={form.control}
                render={({ field, fieldState }) => (
                  <Field data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor={field.name}>Görsel URL</FieldLabel>
                    <Input
                      {...field}
                      id={field.name}
                      placeholder="https://example.com/image.jpg"
                      autoComplete="off"
                      value={field.value || ""}
                    />
                    {fieldState.invalid && (
                      <FieldError errors={[fieldState.error]} />
                    )}
                  </Field>
                )}
              />
            </div>

            {/* Açıklama */}
            <Controller
              name="description"
              control={form.control}
              render={({ field, fieldState }) => (
                <Field data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor={field.name}>Ürün Açıklaması</FieldLabel>
                  <Textarea
                    {...field}
                    id={field.name}
                    placeholder="Ürün detayları, kumaş özellikleri veya teknik bilgiler..."
                    rows={2}
                    value={field.value || ""}
                  />
                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} />
                  )}
                </Field>
              )}
            />
          </div>

          {/* 2. Bölüm: Varyantlar, Fiyat ve Başlangıç Stoğu */}
          <div className="space-y-4">
            <div className="flex items-center justify-between pb-1 border-b">
              <div className="flex items-center gap-2">
                <Layers className="size-4 text-primary" />
                <h3 className="font-semibold text-xs tracking-wider uppercase text-foreground">
                  Varyantlar, Fiyat & Başlangıç Stoğu
                </h3>
              </div>
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={handleAddVariant}
                className="h-7 text-xs gap-1"
              >
                <Plus className="size-3" />
                Varyant Ekle
              </Button>
            </div>

            <p className="text-xs text-muted-foreground">
              Her varyant için SKU, satış fiyatı ve item-service'e iletilecek başlangıç stok miktarını giriniz.
            </p>

            {form.formState.errors.variants?.root && (
              <p className="text-xs text-destructive font-medium">
                {form.formState.errors.variants.root.message}
              </p>
            )}

            <div className="space-y-4">
              {variantFields.map((fieldItem, index) => (
                <VariantCard
                  key={fieldItem.id}
                  index={index}
                  control={form.control}
                  canRemove={variantFields.length > 1}
                  onRemove={() => removeVariant(index)}
                />
              ))}
            </div>
          </div>

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
              disabled={createProductMutation.isPending}
            >
              Vazgeç
            </Button>
            <Button
              type="submit"
              size="sm"
              disabled={createProductMutation.isPending}
              className="gap-1.5"
            >
              {createProductMutation.isPending && (
                <Spinner className="size-3.5" />
              )}
              {createProductMutation.isPending ? "Kaydediliyor..." : "Ürünü Oluştur"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

interface VariantCardProps {
  index: number;
  control: any;
  canRemove: boolean;
  onRemove: () => void;
}

function VariantCard({ index, control, canRemove, onRemove }: VariantCardProps) {
  const {
    fields: attributeFields,
    append: appendAttribute,
    remove: removeAttribute,
  } = useFieldArray({
    control,
    name: `variants.${index}.attributes`,
  });

  return (
    <div className="rounded-xl border bg-muted/20 p-3.5 space-y-3 transition-colors hover:border-primary/40">
      <div className="flex items-center justify-between">
        <Badge variant="outline" className="text-xs font-semibold gap-1">
          <Barcode className="size-3 text-muted-foreground" />
          Varyant #{index + 1}
        </Badge>
        {canRemove && (
          <Button
            type="button"
            variant="ghost"
            size="icon-xs"
            onClick={onRemove}
            className="text-destructive hover:bg-destructive/10"
            title="Varyantı kaldır"
          >
            <Trash2 className="size-3.5" />
          </Button>
        )}
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        {/* SKU */}
        <Controller
          name={`variants.${index}.sku`}
          control={control}
          render={({ field, fieldState }) => (
            <Field data-invalid={fieldState.invalid}>
              <FieldLabel htmlFor={field.name} className="text-xs">
                SKU Barkod Kodu *
              </FieldLabel>
              <Input
                {...field}
                id={field.name}
                placeholder="Örn: TSHIRT-BLK-S"
                className="h-8 text-xs font-mono"
                autoComplete="off"
                aria-invalid={fieldState.invalid}
              />
              {fieldState.invalid && (
                <FieldError errors={[fieldState.error]} className="text-[11px]" />
              )}
            </Field>
          )}
        />

        {/* Fiyat */}
        <Controller
          name={`variants.${index}.price`}
          control={control}
          render={({ field, fieldState }) => (
            <Field data-invalid={fieldState.invalid}>
              <FieldLabel htmlFor={field.name} className="text-xs flex items-center gap-1">
                <Coins className="size-3 text-muted-foreground" />
                Fiyat (₺) *
              </FieldLabel>
              <Input
                {...field}
                id={field.name}
                type="number"
                step="0.01"
                min="0"
                placeholder="249.90"
                className="h-8 text-xs"
                onChange={(e) => field.onChange(parseFloat(e.target.value) || 0)}
                aria-invalid={fieldState.invalid}
              />
              {fieldState.invalid && (
                <FieldError errors={[fieldState.error]} className="text-[11px]" />
              )}
            </Field>
          )}
        />

        {/* Başlangıç Stoğu */}
        <Controller
          name={`variants.${index}.initialStock`}
          control={control}
          render={({ field, fieldState }) => (
            <Field data-invalid={fieldState.invalid}>
              <FieldLabel htmlFor={field.name} className="text-xs flex items-center gap-1">
                <Warehouse className="size-3 text-muted-foreground" />
                Başlangıç Stoğu *
              </FieldLabel>
              <Input
                {...field}
                id={field.name}
                type="number"
                min="0"
                step="1"
                placeholder="50"
                className="h-8 text-xs"
                onChange={(e) => field.onChange(parseInt(e.target.value, 10) || 0)}
                aria-invalid={fieldState.invalid}
              />
              {fieldState.invalid && (
                <FieldError errors={[fieldState.error]} className="text-[11px]" />
              )}
            </Field>
          )}
        />
      </div>

      {/* Varyant Özellikleri (Renk, Beden vb.) */}
      <div className="pt-2 border-t border-dashed">
        <div className="flex items-start justify-between gap-2 mb-2">
          <div>
            <span className="text-[11px] font-medium text-foreground">
              Varyant Nitelikleri (Opsiyonel: Beden, Renk, Materyal vb.)
            </span>
            <p className="text-[10px] text-muted-foreground mt-0.5">
              Her varyant için aynı özellik (örn. Renk) yalnızca bir kez eklenebilir. Farklı renk veya bedenler için lütfen yukarıdan yeni bir varyant ekleyiniz.
            </p>
          </div>
          <Button
            type="button"
            variant="ghost"
            size="xs"
            onClick={() => appendAttribute({ attributeName: "", attributeValue: "" })}
            className="h-6 text-[11px] gap-1 text-primary shrink-0"
          >
            <Plus className="size-3" />
            Özellik Ekle
          </Button>
        </div>

        {attributeFields.length > 0 && (
          <div className="space-y-2 mt-2">
            {attributeFields.map((attrItem, attrIndex) => (
              <div key={attrItem.id} className="space-y-1">
                <div className="flex items-start gap-2">
                  <Controller
                    name={`variants.${index}.attributes.${attrIndex}.attributeName`}
                    control={control}
                    render={({ field, fieldState }) => (
                      <div className="flex-1">
                        <Input
                          {...field}
                          placeholder="Özellik (Örn: Renk)"
                          className={cn(
                            "h-7 text-xs",
                            fieldState.invalid && "border-destructive focus-visible:ring-destructive"
                          )}
                        />
                        {fieldState.error && (
                          <p className="text-[10px] text-destructive mt-0.5 pl-0.5">
                            {fieldState.error.message}
                          </p>
                        )}
                      </div>
                    )}
                  />
                  <Controller
                    name={`variants.${index}.attributes.${attrIndex}.attributeValue`}
                    control={control}
                    render={({ field, fieldState }) => (
                      <div className="flex-1">
                        <Input
                          {...field}
                          placeholder="Değer (Örn: Kırmızı)"
                          className={cn(
                            "h-7 text-xs",
                            fieldState.invalid && "border-destructive focus-visible:ring-destructive"
                          )}
                        />
                        {fieldState.error && (
                          <p className="text-[10px] text-destructive mt-0.5 pl-0.5">
                            {fieldState.error.message}
                          </p>
                        )}
                      </div>
                    )}
                  />
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon-xs"
                    onClick={() => removeAttribute(attrIndex)}
                    className="text-muted-foreground hover:text-destructive shrink-0 mt-0.5"
                    title="Özelliği sil"
                  >
                    <Trash2 className="size-3" />
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
