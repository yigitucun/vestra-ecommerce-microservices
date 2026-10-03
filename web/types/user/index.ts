import {z} from 'zod';

export interface User {
    firstName: string;
    lastName: string;
    email: string;
}

export interface AdminUserListItem {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
    role: string;
    isActive: boolean;
}

export interface SpringPageInfo {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
}

export interface PagedModel<T> {
    content: T[];
    page?: SpringPageInfo;
    totalElements?: number;
    totalPages?: number;
    number?: number;
    size?: number;
}

export const loginSchema = z.object({
    email: z.email("Geçersiz e-posta adresi"),
    password: z.string().min(1,"Geçersiz şifre")
})


export const registerSchema = z.object({
    email: z.email("Geçerli e-posta adresi giriniz."),
    password: z.string().min(6,"Şifre minimum 6 karakter olmalıdır"),
    firstName: z.string().min(1,"Adınız zorunludur."),
    lastName: z.string().min(1,"Soyadınız zorunludur."),
})

export const forgotPasswordSchema = z.object({
    email: z.email("Geçersiz e-posta adresi"),
})

export const resetPasswordSchema = z.object({
    newPassword: z.string().min(6,"Şifre minimum 6 karakter olmalıdır"),
    reNewPassword: z.string().min(6, "Şifre minimum 6 karakter olmalıdır"),
    token: z.string(),

}) .refine((data) => data.newPassword === data.reNewPassword, {
    message: "Şifreler eşleşmiyor",
    path: ["reNewPassword"],
})


export type LoginSchema = z.output<typeof loginSchema>
export type RegisterSchema = z.output<typeof registerSchema>
export type ForgotPasswordSchema = z.output<typeof forgotPasswordSchema>
export type ResetPasswordSchema = z.output<typeof resetPasswordSchema>