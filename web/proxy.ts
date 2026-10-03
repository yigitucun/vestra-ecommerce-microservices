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
  const hasAuthFlag = request.cookies.get("is_authenticated")?.value === "true";

  const payload = accessToken ? parseJwt(accessToken) : null;
  const role = payload?.role;

  const isAuthenticated = !!accessToken || hasRefreshToken || hasAuthFlag;

  // OAuth2 geri dönüş (exchange) rotasını middleware kontrolünden muaf tut
  if (pathname.startsWith("/oauth2")) {
    return NextResponse.next();
  }

  // Dashboard rotaları - Sadece ADMIN rolüne sahip kullanıcılar erişebilir
  if (pathname.startsWith("/dashboard")) {
    if (!isAuthenticated) {
      const loginUrl = new URL("/auth/login", request.url);
      loginUrl.searchParams.set("redirect", pathname);
      return NextResponse.redirect(loginUrl);
    }

    // Giriş yapılmış fakat ADMIN yetkisi yoksa (örn. CUSTOMER), dashboard'a erişimi engelle
    if (role !== "ADMIN") {
      return NextResponse.redirect(new URL("/", request.url));
    }

    return NextResponse.next();
  }

  // Giriş yapılmışken auth sayfalarına (login, signup, forgot-password) girilmesini engelle
  const isAuthGuestRoute =
    pathname.startsWith("/auth/login") ||
    pathname.startsWith("/auth/signup") ||
    pathname.startsWith("/auth/forgot-password");

  if (isAuthenticated && isAuthGuestRoute) {
    if (role === "ADMIN") {
      return NextResponse.redirect(new URL("/dashboard", request.url));
    }
    return NextResponse.redirect(new URL("/", request.url));
  }

  // Kök dizin (/) yönlendirmesi
  if (pathname === "/") {
    if (isAuthenticated) {
      if (role === "ADMIN") {
        return NextResponse.redirect(new URL("/dashboard", request.url));
      }
      return NextResponse.next();
    } else {
      return NextResponse.redirect(new URL("/auth/login", request.url));
    }
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
