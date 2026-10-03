import { z } from "zod";

export interface ProductVariantAttribute {
  id?: string;
  attributeName: string;
  attributeValue: string;
}

export interface ProductVariant {
  id?: string;
  price: number;
  sku: string;
  attributes?: ProductVariantAttribute[] | null;
}

export interface ProductCategoryInfo {
  id?: string;
  name: string;
  slug: string;
}

export interface ProductListItem {
  id: string;
  name: string;
  description?: string;
  imageUrl?: string;
  slug?: string;
  category?: ProductCategoryInfo | null;
  variants?: ProductVariant[] | null;
}

export interface CreateVariantAttributePayload {
  attributeName: string;
  attributeValue: string;
}

export interface CreateVariantPayload {
  price: number;
  sku: string;
  initialStock: number;
  attributes: CreateVariantAttributePayload[];
}

export interface CreateProductPayload {
  name: string;
  slug: string;
  description?: string;
  imageUrl?: string;
  categoryId: string;
  variants: CreateVariantPayload[];
}

export const createVariantAttributeSchema = z.object({
  attributeName: z.string().min(1, "Özellik adı zorunludur (Örn: Beden, Renk)"),
  attributeValue: z.string().min(1, "Özellik değeri zorunludur (Örn: L, Mavi)"),
});

export const createVariantSchema = z
  .object({
    price: z.number().positive("Fiyat 0'dan büyük olmalıdır."),
    sku: z.string().min(1, "SKU barkod kodu zorunludur."),
    initialStock: z
      .number()
      .int("Stok tam sayı olmalıdır.")
      .min(0, "Başlangıç stoğu en az 0 olmalıdır."),
    attributes: z.array(createVariantAttributeSchema),
  })
  .superRefine((data, ctx) => {
    const seen = new Set<string>();
    data.attributes.forEach((attr, idx) => {
      const name = attr.attributeName.trim().toLowerCase();
      if (!name) return;
      if (seen.has(name)) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: `"${attr.attributeName}" özelliği bu varyantta zaten tanımlı. Farklı renk/beden için "Varyant Ekle" kullanınız.`,
          path: ["attributes", idx, "attributeName"],
        });
      } else {
        seen.add(name);
      }
    });
  });

export const createProductSchema = z
  .object({
    name: z.string().min(1, "Ürün adı zorunludur."),
    slug: z
      .string()
      .min(1, "Slug zorunludur.")
      .regex(
        /^[a-z0-9]+(?:-[a-z0-9]+)*$/,
        "Slug sadece küçük harf, rakam ve tire (-) içerebilir (Örn: apple-iphone-15)."
      ),
    description: z.string().optional(),
    imageUrl: z.string().optional(),
    categoryId: z.string().min(1, "Lütfen bir kategori seçiniz."),
    variants: z
      .array(createVariantSchema)
      .min(1, "En az bir varyant (fiyat ve stok) tanımlamalısınız."),
  })
  .superRefine((data, ctx) => {
    const seenSkus = new Set<string>();
    data.variants.forEach((v, idx) => {
      const sku = v.sku.trim().toLowerCase();
      if (!sku) return;
      if (seenSkus.has(sku)) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: `"${v.sku}" SKU kodu başka bir varyantta kullanılmış.`,
          path: ["variants", idx, "sku"],
        });
      } else {
        seenSkus.add(sku);
      }
    });
  });

export type CreateProductFormValues = z.infer<typeof createProductSchema>;
export type CreateVariantFormValues = z.infer<typeof createVariantSchema>;
export type CreateVariantAttributeFormValues = z.infer<
  typeof createVariantAttributeSchema
>;

export interface UpdateVariantAttributePayload {
  attributeName: string;
  attributeValue: string;
}

export interface UpdateVariantPayload {
  id?: string;
  price: number;
  sku: string;
  initialStock?: number;
  attributes: UpdateVariantAttributePayload[];
}

export interface UpdateProductPayload {
  name: string;
  slug: string;
  description?: string;
  imageUrl?: string;
  categoryId: string;
  variants: UpdateVariantPayload[];
}

export const updateVariantAttributeSchema = createVariantAttributeSchema;

export const updateVariantSchema = z
  .object({
    id: z.string().optional(),
    price: z.number().positive("Fiyat 0'dan büyük olmalıdır."),
    sku: z.string().min(1, "SKU barkod kodu zorunludur."),
    initialStock: z
      .number()
      .int("Stok tam sayı olmalıdır.")
      .min(0, "Başlangıç stoğu en az 0 olmalıdır.")
      .optional(),
    attributes: z.array(updateVariantAttributeSchema),
  })
  .superRefine((data, ctx) => {
    const seen = new Set<string>();
    data.attributes.forEach((attr, idx) => {
      const name = attr.attributeName.trim().toLowerCase();
      if (!name) return;
      if (seen.has(name)) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: `"${attr.attributeName}" özelliği bu varyantta zaten tanımlı.`,
          path: ["attributes", idx, "attributeName"],
        });
      } else {
        seen.add(name);
      }
    });
  });

export const updateProductSchema = z
  .object({
    name: z.string().min(1, "Ürün adı zorunludur."),
    slug: z
      .string()
      .min(1, "Slug zorunludur.")
      .regex(
        /^[a-z0-9]+(?:-[a-z0-9]+)*$/,
        "Slug sadece küçük harf, rakam ve tire (-) içerebilir (Örn: apple-iphone-15)."
      ),
    description: z.string().optional(),
    imageUrl: z.string().optional(),
    categoryId: z.string().min(1, "Lütfen bir kategori seçiniz."),
    variants: z
      .array(updateVariantSchema)
      .min(1, "En az bir varyant tanımlamalısınız."),
  })
  .superRefine((data, ctx) => {
    const seenSkus = new Set<string>();
    data.variants.forEach((v, idx) => {
      const sku = v.sku.trim().toLowerCase();
      if (!sku) return;
      if (seenSkus.has(sku)) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: `"${v.sku}" SKU kodu başka bir varyantta kullanılmış.`,
          path: ["variants", idx, "sku"],
        });
      } else {
        seenSkus.add(sku);
      }
    });
  });

export type UpdateProductFormValues = z.infer<typeof updateProductSchema>;
export type UpdateVariantFormValues = z.infer<typeof updateVariantSchema>;


