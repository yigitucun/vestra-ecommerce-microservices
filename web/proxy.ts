
import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  const hasAccessToken = request.cookies.has("access_token");
  const hasRefreshToken = request.cookies.has("refresh_token");
  const hasAuthFlag = request.cookies.get("is_authenticated")?.value === "true";

  const isAuthenticated = hasAccessToken || hasRefreshToken || hasAuthFlag;

  // OAuth2 geri dönüş (exchange) rotasını middleware kontrolünden muaf tut
  if (pathname.startsWith("/oauth2")) {
    return NextResponse.next();
  }

  // Dashboard rotaları - oturum açılmamışsa login sayfasına yönlendir
  if (pathname.startsWith("/dashboard")) {
    if (!isAuthenticated) {
      const loginUrl = new URL("/auth/login", request.url);
      loginUrl.searchParams.set("redirect", pathname);
      return NextResponse.redirect(loginUrl);
    }
    return NextResponse.next();
  }

  // Giriş yapılmışken auth sayfalarına (login, signup, forgot-password) girilmesini engelle
  const isAuthGuestRoute =
    pathname.startsWith("/auth/login") ||
    pathname.startsWith("/auth/signup") ||
    pathname.startsWith("/auth/forgot-password");

  if (isAuthenticated && isAuthGuestRoute) {
    return NextResponse.redirect(new URL("/dashboard", request.url));
  }

  // Kök dizin (/) yönlendirmesi
  if (pathname === "/") {
    if (isAuthenticated) {
      return NextResponse.redirect(new URL("/dashboard", request.url));
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
