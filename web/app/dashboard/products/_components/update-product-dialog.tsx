"use client";

import * as React from "react";
import { useEffect, useState } from "react";
import { useForm, useFieldArray, Controller } from "react-hook-form";
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
import { Badge } from "@/components/ui/badge";
import { Spinner } from "@/components/ui/spinner";
import { toast } from "@/components/ui/toast";
import { useAdminCategories } from "@/hooks/admin/use-admin-categories";
import { useUpdateProduct } from "@/hooks/admin/use-admin-products";
import {
  updateProductSchema,
  UpdateProductFormValues,
  UpdateProductPayload,
  ProductListItem,
} from "@/types/product";
import { slugify } from "@/types/category";
import {
  Plus,
  Pencil,
  Trash2,
  Layers,
  AlertCircle,
  Tag,
  Barcode,
  Coins,
  Warehouse,
  Sparkles,
} from "lucide-react";
import { cn } from "@/lib/utils";

interface UpdateProductDialogProps {
  product: ProductListItem | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function UpdateProductDialog({
  product,
  open,
  onOpenChange,
}: UpdateProductDialogProps) {
  const { data: categories = [] } = useAdminCategories();
  const updateProductMutation = useUpdateProduct();
  const [isSlugCustomized, setIsSlugCustomized] = useState(false);

  const form = useForm<UpdateProductFormValues>({
    resolver: zodResolver(updateProductSchema),
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

  useEffect(() => {
    if (product && open) {
      // Kategori ID'sini bul: ya doğrudan category.id ya da isim/slug eşleşmesi
      let matchedCategoryId = product.category?.id || "";
      if (!matchedCategoryId && product.category?.name) {
        const found = categories.find(
          (c) =>
            c.name === product.category?.name ||
            c.slug === product.category?.slug
        );
        if (found) matchedCategoryId = found.id;
      }

      const initialVariants =
        product.variants && product.variants.length > 0
          ? product.variants.map((v) => ({
              id: v.id,
              sku: v.sku || "",
              price: Number(v.price) || 0,
              initialStock: 0,
              attributes: (v.attributes || []).map((attr) => ({
                attributeName: attr.attributeName || "",
                attributeValue: attr.attributeValue || "",
              })),
            }))
          : [
              {
                sku: "",
                price: 0,
                initialStock: 0,
                attributes: [],
              },
            ];

      form.reset({
        name: product.name || "",
        slug: product.slug || "",
        description: product.description || "",
        imageUrl: product.imageUrl || "",
        categoryId: matchedCategoryId,
        variants: initialVariants,
      });

      setIsSlugCustomized(false);
    }
  }, [product, open, categories, form]);

  const handleNameChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    onChange: (...event: unknown[]) => void
  ) => {
    const newName = e.target.value;
    onChange(newName);

    if (!isSlugCustomized) {
      const generatedSlug = slugify(newName);
      form.setValue("slug", generatedSlug, { shouldValidate: true });
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

  const onSubmit = (values: UpdateProductFormValues) => {
    if (!product) return;

    const payload: UpdateProductPayload = {
      name: values.name.trim(),
      slug: values.slug.trim(),
      description: values.description?.trim() || undefined,
      imageUrl: values.imageUrl?.trim() || undefined,
      categoryId: values.categoryId,
      variants: values.variants.map((v) => ({
        id: v.id,
        sku: v.sku.trim(),
        price: Number(v.price),
        initialStock: v.id ? undefined : Number(v.initialStock || 0),
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

    updateProductMutation.mutate(
      { id: product.id, payload },
      {
        onSuccess: () => {
          toast.add({
            title: "Başarılı",
            description: `"${payload.name}" ürünü başarıyla güncellendi.`,
          });
          onOpenChange(false);
        },
        onError: (err: unknown) => {
          toast.add({
            title: "Hata",
            description:
              (err as Error)?.message || "Ürün güncellenirken bir hata oluştu.",
          });
        },
      }
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[700px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <div className="flex size-9 items-center justify-center rounded-lg bg-primary/10 text-primary">
              <Pencil className="size-5" />
            </div>
            <div>
              <DialogTitle className="text-base font-semibold">
                Ürünü Düzenle
              </DialogTitle>
              <DialogDescription className="text-xs">
                Ürün bilgilerini, fiyatlarını, varyantlarını ve özelliklerini güncelleyin.
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        {updateProductMutation.isError && (
          <div className="flex items-center gap-2 rounded-lg border border-destructive/30 bg-destructive/10 p-3 text-destructive text-xs">
            <AlertCircle className="size-4 shrink-0" />
            <span>
              {updateProductMutation.error instanceof Error
                ? updateProductMutation.error.message
                : "Ürün güncellenirken bir hata oluştu."}
            </span>
          </div>
        )}

        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5">
          {/* Genel Bilgiler */}
          <div className="space-y-4">
            <h4 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
              <Tag className="size-3.5" />
              Genel Bilgiler
            </h4>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {/* Ürün Adı */}
              <Controller
                name="name"
                control={form.control}
                render={({ field, fieldState }) => (
                  <Field data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor={field.name} className="text-xs">
                      Ürün Adı *
                    </FieldLabel>
                    <Input
                      {...field}
                      id={field.name}
                      placeholder="Örn: Apple iPhone 15"
                      onChange={(e) => handleNameChange(e, field.onChange)}
                      aria-invalid={fieldState.invalid}
                      className="h-9 text-xs"
                    />
                    {fieldState.invalid && (
                      <FieldError errors={[fieldState.error]} className="text-xs" />
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
                    <FieldLabel htmlFor={field.name} className="text-xs">
                      Slug (URL Benzersiz Tanımlayıcı) *
                    </FieldLabel>
                    <Input
                      {...field}
                      id={field.name}
                      placeholder="apple-iphone-15"
                      onChange={(e) => handleSlugChange(e, field.onChange)}
                      aria-invalid={fieldState.invalid}
                      className="h-9 text-xs font-mono"
                    />
                    {fieldState.invalid ? (
                      <FieldError errors={[fieldState.error]} className="text-xs" />
                    ) : (
                      <FieldDescription className="text-[11px]">
                        URL dostu benzersiz kimlik.
                      </FieldDescription>
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
                    <FieldLabel className="text-xs">Kategori *</FieldLabel>
                    <Select
                      value={field.value}
                      onValueChange={(val) => field.onChange(val ?? "")}
                    >
                      <SelectTrigger className="w-full h-9 text-xs">
                        <SelectValue placeholder="Kategori seçiniz..." />
                      </SelectTrigger>
                      <SelectContent>
                        {categories.map((cat) => (
                          <SelectItem key={cat.id} value={cat.id} className="text-xs">
                            {cat.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {fieldState.invalid && (
                      <FieldError errors={[fieldState.error]} className="text-xs" />
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
                    <FieldLabel htmlFor={field.name} className="text-xs">
                      Ürün Görsel URL
                    </FieldLabel>
                    <Input
                      {...field}
                      id={field.name}
                      placeholder="https://example.com/images/urun.jpg"
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

            {/* Açıklama */}
            <Controller
              name="description"
              control={form.control}
              render={({ field, fieldState }) => (
                <Field data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor={field.name} className="text-xs">
                    Ürün Açıklaması
                  </FieldLabel>
                  <Textarea
                    {...field}
                    id={field.name}
                    placeholder="Ürün hakkında detaylı bilgiler..."
                    rows={2}
                    className="text-xs resize-none"
                    aria-invalid={fieldState.invalid}
                  />
                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} className="text-xs" />
                  )}
                </Field>
              )}
            />
          </div>

          {/* Varyant Yönetimi */}
          <div className="space-y-4 pt-3 border-t">
            <div className="flex items-center justify-between">
              <div>
                <h4 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
                  <Layers className="size-3.5" />
                  Varyantlar & Fiyatlandırma
                </h4>
                <p className="text-[11px] text-muted-foreground mt-0.5">
                  Mevcut varyantların fiyat ve özelliklerini düzenleyebilir veya yeni varyant ekleyebilirsiniz.
                </p>
              </div>
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={handleAddVariant}
                className="h-8 gap-1 text-xs"
              >
                <Plus className="size-3.5" />
                Varyant Ekle
              </Button>
            </div>

            {form.formState.errors.variants?.root && (
              <p className="text-xs text-destructive font-medium">
                {form.formState.errors.variants.root.message}
              </p>
            )}

            <div className="space-y-4">
              {variantFields.map((fieldItem, index) => (
                <VariantEditCard
                  key={fieldItem.id}
                  index={index}
                  control={form.control}
                  canRemove={variantFields.length > 1}
                  onRemove={() => removeVariant(index)}
                  form={form}
                />
              ))}
            </div>
          </div>

          <DialogFooter className="gap-2 sm:gap-2.5 pt-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => onOpenChange(false)}
              disabled={updateProductMutation.isPending}
            >
              Vazgeç
            </Button>
            <Button
              type="submit"
              size="sm"
              disabled={updateProductMutation.isPending}
              className="gap-1.5"
            >
              {updateProductMutation.isPending && (
                <Spinner className="size-3.5" />
              )}
              {updateProductMutation.isPending ? "Kaydediliyor..." : "Değişiklikleri Kaydet"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

interface VariantEditCardProps {
  index: number;
  control: any;
  canRemove: boolean;
  onRemove: () => void;
  form: any;
}

function VariantEditCard({
  index,
  control,
  canRemove,
  onRemove,
  form,
}: VariantEditCardProps) {
  const {
    fields: attributeFields,
    append: appendAttribute,
    remove: removeAttribute,
  } = useFieldArray({
    control,
    name: `variants.${index}.attributes`,
  });

  const variantId = form.watch(`variants.${index}.id`);
  const isExistingVariant = !!variantId;

  const handleGenerateSku = () => {
    const slug = form.getValues("slug") || "PRD";
    const prefix = slug.toUpperCase().slice(0, 8);
    const randomSuffix = Math.floor(100 + Math.random() * 900);
    form.setValue(`variants.${index}.sku`, `${prefix}-${randomSuffix}`, {
      shouldValidate: true,
    });
  };

  return (
    <div className="rounded-lg border bg-card/60 p-3.5 space-y-3 relative group">
      <div className="flex items-center justify-between pb-1 border-b">
        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold">
            Varyant #{index + 1}
          </span>
          {isExistingVariant ? (
            <Badge variant="outline" className="text-[10px] py-0 h-4.5 text-muted-foreground font-normal">
              Mevcut Varyant
            </Badge>
          ) : (
            <Badge variant="secondary" className="text-[10px] py-0 h-4.5 bg-primary/10 text-primary font-normal">
              Yeni Varyant
            </Badge>
          )}
        </div>

        {canRemove && (
          <Button
            type="button"
            variant="ghost"
            size="icon-xs"
            onClick={onRemove}
            className="text-muted-foreground hover:text-destructive size-6"
            title="Varyantı kaldır"
          >
            <Trash2 className="size-3.5" />
          </Button>
        )}
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        {/* SKU Kodu */}
        <Controller
          name={`variants.${index}.sku`}
          control={control}
          render={({ field, fieldState }) => (
            <Field data-invalid={fieldState.invalid}>
              <div className="flex items-center justify-between">
                <FieldLabel htmlFor={field.name} className="text-xs flex items-center gap-1">
                  <Barcode className="size-3 text-muted-foreground" />
                  SKU / Barkod *
                </FieldLabel>
                {!isExistingVariant && (
                  <button
                    type="button"
                    onClick={handleGenerateSku}
                    className="text-[10px] text-primary hover:underline flex items-center gap-0.5"
                  >
                    <Sparkles className="size-2.5" />
                    Öneri
                  </button>
                )}
              </div>
              <Input
                {...field}
                id={field.name}
                placeholder="Örn: IPH15-128-BLK"
                className="h-8 text-xs font-mono"
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

        {/* Başlangıç Stoğu (Sadece yeni varyantlar için) */}
        {!isExistingVariant ? (
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
        ) : (
          <div className="flex flex-col justify-center">
            <span className="text-[11px] font-medium text-muted-foreground flex items-center gap-1">
              <Warehouse className="size-3" />
              Stok Durumu
            </span>
            <p className="text-[11px] text-muted-foreground/80 mt-1">
              item-service envanteri üzerinden yönetilmektedir.
            </p>
          </div>
        )}
      </div>

      {/* Varyant Özellikleri (Renk, Beden vb.) */}
      <div className="pt-2 border-t border-dashed">
        <div className="flex items-start justify-between gap-2 mb-2">
          <div>
            <span className="text-[11px] font-medium text-foreground">
              Varyant Nitelikleri (Opsiyonel: Beden, Renk vb.)
            </span>
            <p className="text-[10px] text-muted-foreground mt-0.5">
              Her varyant için aynı özellik (örn. Renk) yalnızca bir kez eklenebilir.
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
                          placeholder="Değer (Örn: Siyah)"
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
