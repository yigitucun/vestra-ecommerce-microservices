"use client"
import { cn } from "cn"

import { Button } from "@/components/ui/button"
import {
    Field,
    FieldDescription, FieldError,
    FieldGroup,
    FieldLabel,
    FieldSeparator,
} from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { useForm,Controller } from "react-hook-form"
import {registerSchema, RegisterSchema} from "@/types/user"
import {zodResolver} from "@hookform/resolvers/zod";
import {GoogleLoginButton} from "@/components/auth/google-login-button";
import Link from "next/link";
import {Spinner} from "@/components/ui/spinner";
import {useRegister} from "@/hooks/auth/use-register";

export function SignupForm({className, ...props}: React.ComponentProps<"form">) {

    const form = useForm<RegisterSchema>({
        resolver: zodResolver(registerSchema),
        defaultValues: {email: "",password: "",firstName: "",lastName: ""}
    })

    const {mutate:register,isPending,error} = useRegister()
    const handleRegister = (values:RegisterSchema) => {
        register(values)
    }

    return (
        <form className={cn("flex flex-col gap-6", className)} {...props} onSubmit={form.handleSubmit(handleRegister)}>
            <FieldGroup>
                <div className="flex flex-col items-center gap-1 text-center">
                    <h1 className="text-2xl font-bold">Hesap oluştur</h1>
                    <p className="text-sm text-balance text-muted-foreground">Başlamak için bilgilerini gir.</p>
                </div>
                <FieldGroup className="grid grid-cols-2 ">
                    <Controller
                        name="firstName"
                        control={form.control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={field.name}>Ad</FieldLabel>
                                <Input{...field} id={field.name} aria-invalid={fieldState.invalid} placeholder="Michael" autoComplete="off"/>
                                {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                            </Field>
                        )}
                    />
                    <Controller
                        name="lastName"
                        control={form.control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={field.name}>Soyad</FieldLabel>
                                <Input{...field} id={field.name} aria-invalid={fieldState.invalid} placeholder="Jackson" autoComplete="off"/>
                                {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                            </Field>
                        )}
                    />
                </FieldGroup>

                <Controller
                    name="email"
                    control={form.control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor={field.name}>E-posta</FieldLabel>
                            <Input{...field} id={field.name} aria-invalid={fieldState.invalid} placeholder="m@example.com" autoComplete="off"/>
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />
                <Controller
                    name="password"
                    control={form.control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor={field.name}>Şifre</FieldLabel>
                            <Input{...field} id={field.name} aria-invalid={fieldState.invalid} placeholder="******" autoComplete="off" type={"password"}/>
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />
                {error && (
                    <FieldError>{error.message}</FieldError>
                )}
                <Field>
                    <Button disabled={isPending} type="submit">
                        {isPending?<Spinner/>:''}
                        Kayıt ol
                    </Button>
                </Field>
                <FieldSeparator>Veya</FieldSeparator>
                <Field>
                    <GoogleLoginButton/>
                    <FieldDescription className="text-center">
                        Zaten hesabın var mı?{" "}
                        <Link href="/auth/login" className="underline underline-offset-4">Giriş yap</Link>
                    </FieldDescription>
                </Field>
            </FieldGroup>
        </form>
    )
}