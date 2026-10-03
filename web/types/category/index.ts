import { z } from "zod";

export interface CategoryParent {
  id?: string;
  name: string;
}

export interface CategoryListItem {
  id: string;
  name: string;
  slug: string;
  description?: string;
  parent?: CategoryParent | CategoryParent[] | null;
}

export interface CreateCategoryPayload {
  name: string;
  slug: string;
  description?: string;
  parentId?: string | null;
}

export interface UpdateCategoryPayload {
  name: string;
  slug: string;
  description?: string;
  parentId?: string | null;
}

export const categorySchema = z.object({
  name: z.string().min(1, "Kategori adı zorunludur."),
  slug: z
    .string()
    .min(1, "Slug alanı zorunludur.")
    .regex(
      /^[a-z0-9]+(?:-[a-z0-9]+)*$/,
      "Slug sadece küçük harf, rakam ve tire (-) içerebilir (Örn: elektronik-urunler)."
    ),
  description: z.string().optional(),
  parentId: z.string().optional().nullable(),
});

export type CategoryFormSchema = z.infer<typeof categorySchema>;
export const createCategorySchema = categorySchema;
export type CreateCategorySchema = CategoryFormSchema;
export const updateCategorySchema = categorySchema;
export type UpdateCategorySchema = CategoryFormSchema;

export function slugify(text: string): string {
  const trMap: Record<string, string> = {
    ç: "c",
    Ç: "c",
    ğ: "g",
    Ğ: "g",
    ı: "i",
    İ: "i",
    ö: "o",
    Ö: "o",
    ş: "s",
    Ş: "s",
    ü: "u",
    Ü: "u",
  };

  return text
    .split("")
    .map((char) => trMap[char] || char)
    .join("")
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9\s-]/g, "")
    .replace(/\s+/g, "-")
    .replace(/-+/g, "-");
}
