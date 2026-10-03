"use client"

import { cn } from "cn"
import { Command } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
    Field,
    FieldDescription, FieldError,
    FieldGroup,
    FieldLabel,
} from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import Link from "next/link";
import {Controller, useForm} from "react-hook-form";
import {Spinner} from "@/components/ui/spinner";
import {resetPasswordSchema, type ResetPasswordSchema} from "@/types/user";
import {zodResolver} from "@hookform/resolvers/zod";
import {useResetPassword} from "@/hooks/auth/use-reset-password";

interface ResetPasswordFormProps extends React.ComponentProps<"div"> {
    token?: string
}


export  function ResetPasswordForm({token, className, ...props}: ResetPasswordFormProps) {

    const form = useForm<ResetPasswordSchema>({
        resolver: zodResolver(resetPasswordSchema),
        defaultValues: {newPassword: "",reNewPassword: "",token: token}
    })

    const {mutate:resetPassword,isError,isPending,error} = useResetPassword()

    const handleResetPassword = (values:ResetPasswordSchema) => {
        resetPassword(values)
    }

    return (
        <div className={cn("flex flex-col gap-6", className)} {...props}>
            <form onSubmit={form.handleSubmit(handleResetPassword)}>
                <FieldGroup>
                    <div className="flex flex-col items-center gap-2 text-center">
                        <Link href="/" className="flex flex-col items-center gap-2 font-medium">
                            <div className="flex size-8 items-center justify-center rounded-md">
                                <Command className="size-6" />
                            </div>
                            <span className="sr-only">Vestra.</span>
                        </Link>
                        <h1 className="text-xl font-bold">Şifremi yenile</h1>
                        <FieldDescription>Hesabın için yeni bir şifre belirle.</FieldDescription>
                    </div>
                    <Controller
                        name="newPassword"
                        control={form.control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={field.name}>Şifre</FieldLabel>
                                <Input{...field} type={"password"} id={field.name} aria-invalid={fieldState.invalid} placeholder="******" autoComplete="off"/>
                                {fieldState.invalid ?
                                    (<FieldError errors={[fieldState.error]} />):
                                    <FieldDescription>Şifre minimum 6 karakter olmalıdır</FieldDescription>
                                }

                            </Field>
                        )}
                    />
                    <Controller
                        name="reNewPassword"
                        control={form.control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={field.name}>Şifre(Tekrar)</FieldLabel>
                                <Input{...field} type={"password"} id={field.name} aria-invalid={fieldState.invalid} placeholder="******" autoComplete="off"/>
                                {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                            </Field>
                        )}
                    />
                    {isError && <FieldError>{error.message}</FieldError>}
                    <Field>
                        <Button disabled={isPending} type="submit">
                            {isPending? <Spinner/>:'' }
                            Şifremi değiştir
                        </Button>
                    </Field>
                </FieldGroup>
            </form>
        </div>
    )
}