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
import {type ForgotPasswordSchema, forgotPasswordSchema} from "@/types/user"
import {zodResolver} from "@hookform/resolvers/zod";
import {useForgotPassword} from "@/hooks/auth/use-forgot-password";
import {Spinner} from "@/components/ui/spinner";

export function ForgotPasswordForm({className, ...props}: React.ComponentProps<"div">) {

    const form = useForm<ForgotPasswordSchema>({
        resolver: zodResolver(forgotPasswordSchema),
        defaultValues: {email: ""}
    })
    const {mutate:forgotPassword,isPending,isError,error} = useForgotPassword()

    const handleForgotPassword = (values:ForgotPasswordSchema) => {
        forgotPassword(values)
    }

    return (
        <div className={cn("flex flex-col gap-6", className)} {...props}>
            <form onSubmit={form.handleSubmit(handleForgotPassword)}>
                <FieldGroup>
                    <div className="flex flex-col items-center gap-2 text-center">
                        <Link
                            href="/"
                            className="flex flex-col items-center gap-2 font-medium"
                        >
                            <div className="flex size-8 items-center justify-center rounded-md">
                                <Command className="size-6" />
                            </div>
                            <span className="sr-only">Vestra.</span>
                        </Link>
                        <h1 className="text-xl font-bold">Parolanızı sıfırlayın</h1>
                        <FieldDescription>
                            Bağlantı gönderebilmek için e-posta adresini gir.
                        </FieldDescription>
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
                    {isError && <FieldError>{error.message}</FieldError>}
                    <Field>
                        <Button disabled={isPending || isError} type="submit">
                            {isPending? <Spinner/>:'' }
                            Sıfırlama bağlantısı gönder
                        </Button>
                    </Field>
                </FieldGroup>
            </form>
        </div>
    )
}