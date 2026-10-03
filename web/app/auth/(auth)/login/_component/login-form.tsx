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
import {loginSchema,type LoginSchema} from "@/types/user"
import {zodResolver} from "@hookform/resolvers/zod";
import {useLogin} from "@/hooks/auth/use-login";
import {GoogleLoginButton} from "@/components/auth/google-login-button";
import Link from "next/link";
import {Spinner} from "@/components/ui/spinner";

export function LoginForm({className, ...props}: React.ComponentProps<"form">) {

    const form = useForm<LoginSchema>({
        resolver: zodResolver(loginSchema),
        defaultValues: {email: "",password: ""}
    })
    const {mutate:login,isPending,error} = useLogin()
    const handleLogin = (values:LoginSchema) => {
        login(values)
    }

    return (
        <form className={cn("flex flex-col gap-6", className)} {...props} onSubmit={form.handleSubmit(handleLogin)}>
            <FieldGroup>
                <div className="flex flex-col items-center gap-1 text-center">
                    <h1 className="text-2xl font-bold">Hesabına giriş yap</h1>
                    <p className="text-sm text-balance text-muted-foreground">Devam etmek için bilgilerini gir.</p>
                </div>
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
                            <div className={"flex items-center justify-between"}>
                                <FieldLabel htmlFor={field.name}>Şifre</FieldLabel>
                                <Link className={"text-sm hover:underline"} href={"/auth/forgot-password"}>Şifreni mi unuttun?</Link>
                            </div>
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
                        Giriş yap
                    </Button>
                </Field>
                <FieldSeparator>Veya</FieldSeparator>
                <Field>
                    <GoogleLoginButton/>
                    <FieldDescription className="text-center">
                        Hala hesabın yok mu?{" "}
                        <Link href="/auth/signup" className="underline underline-offset-4">Kayıt ol</Link>
                    </FieldDescription>
                </Field>
            </FieldGroup>
        </form>
    )
}