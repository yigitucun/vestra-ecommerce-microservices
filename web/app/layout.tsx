import type { Metadata } from 'next';
import { Geist_Mono, Inter } from "next/font/google"

import "./globals.css"
import { ThemeProvider } from "@/components/providers/theme-provider"
import { cn } from "@/lib/utils";
import QueryProvider from "@/components/providers/query-provider";
import {Toaster} from "@/components/ui/toast";

const inter = Inter({subsets:['latin'],variable:'--font-sans'})

const fontMono = Geist_Mono({
  subsets: ["latin"],
  variable: "--font-mono",
})

export const metadata:Metadata = {
    title: {
        default: "Vestra",
        template: `%s - Vestra`,

    }
}

export default function RootLayout({ children, }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en" suppressHydrationWarning className={cn("antialiased", fontMono.variable, "font-sans", inter.variable)}>
      <body>
      <QueryProvider>
        <ThemeProvider>
            {children}
            <Toaster/>
        </ThemeProvider>
      </QueryProvider>
      </body>
    </html>
  )
}
