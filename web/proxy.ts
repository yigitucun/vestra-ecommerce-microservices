import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

interface JwtPayload {
  sub?: string;
  role?: string;
  exp?: number;
  [key: string]: unknown;
}

function parseJwt(token: string): JwtPayload | null {
  try {
    const parts = token.split(".");
    if (parts.length < 2) return null;
    let base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    while (base64.length % 4 !== 0) {
      base64 += "=";
    }
    const binaryStr = atob(base64);
    const bytes = Uint8Array.from(binaryStr, (c) => c.charCodeAt(0));
    const jsonStr = new TextDecoder().decode(bytes);
    return JSON.parse(jsonStr);
  } catch {
    return null;
  }
}

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  const accessToken = request.cookies.get("access_token")?.value;
  const hasRefreshToken = request.cookies.has("refresh_token");

  const payload = accessToken ? parseJwt(accessToken) : null;
  const now = Math.floor(Date.now() / 1000);
  const isTokenExpired = payload?.exp ? payload.exp <= now : false;
  const hasValidAccessToken = !!accessToken && !isTokenExpired;
  const role = payload?.role;

  // OAuth2 geri dönüş (exchange) rotasını middleware kontrolünden muaf tut
  if (pathname.startsWith("/oauth2")) {
    return NextResponse.next();
  }

  // Dashboard rotaları - Sadece ADMIN rolüne sahip kullanıcılar erişebilir
  if (pathname.startsWith("/dashboard")) {
    if (!hasValidAccessToken && !hasRefreshToken) {
      const loginUrl = new URL("/auth/login", request.url);
      loginUrl.searchParams.set("redirect", pathname);
      const res = NextResponse.redirect(loginUrl);
      res.cookies.delete("is_authenticated");
      return res;
    }

    // Geçerli bir token var ama ADMIN değilse ana sayfaya yönlendir
    if (hasValidAccessToken && role !== "ADMIN") {
      return NextResponse.redirect(new URL("/", request.url));
    }

    return NextResponse.next();
  }

  // Giriş yapılmışken auth sayfalarına (login, signup, forgot-password) girilmesini engelle
  const isAuthGuestRoute =
    pathname.startsWith("/auth/login") ||
    pathname.startsWith("/auth/signup") ||
    pathname.startsWith("/auth/forgot-password");

  if (isAuthGuestRoute) {
    // SADECE geçerli ve süresi dolmamış bir access token varsa yönlendir
    if (hasValidAccessToken) {
      if (role === "ADMIN") {
        return NextResponse.redirect(new URL("/dashboard", request.url));
      }
      return NextResponse.redirect(new URL("/", request.url));
    }

    // Token yoksa veya süresi dolmuşsa login sayfasını aç ve bayat auth bayrağını temizle
    const res = NextResponse.next();
    if (!hasRefreshToken) {
      res.cookies.delete("is_authenticated");
    }
    return res;
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Aşağıdaki yollar haricindeki tüm istekleri yakala:
     * - _next/static (statik dosyalar)
     * - _next/image (resim optimizasyon dosyaları)
     * - favicon.ico
     * - statik dosya uzantıları (.svg, .png, .jpg, vb.)
     */
    "/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)",
  ],
};
