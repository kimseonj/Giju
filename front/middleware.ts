import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

const protectedRoutes = ["/dashboard"];
const adminRoutes = ["/admin"];

export function middleware(request: NextRequest) {
  const accessToken = request.cookies.get("access_token")?.value;
  const userRole = request.cookies.get("user_role")?.value;
  const { pathname } = request.nextUrl;

  // 관리자 페이지 접근 체크
  if (adminRoutes.some((route) => pathname.startsWith(route))) {
    if (!accessToken) {
      const url = new URL("/login", request.url);
      url.searchParams.set("from", pathname);
      return NextResponse.redirect(url);
    }
    if (userRole !== "admin" && userRole !== "ADMIN") {
      return NextResponse.redirect(new URL("/", request.url));
    }
  }

  // 보호된 경로 접근 시 토큰 없으면 로그인으로
  if (
    protectedRoutes.some((route) => pathname.startsWith(route)) &&
    !accessToken
  ) {
    const url = new URL("/login", request.url);
    url.searchParams.set("from", pathname);
    return NextResponse.redirect(url);
  }

  // 이미 로그인한 사용자가 로그인/회원가입 페이지 접근 시
  if (
    (pathname.startsWith("/login") || pathname.startsWith("/register")) &&
    accessToken
  ) {
    return NextResponse.redirect(new URL("/dashboard", request.url));
  }

  // 관리자 페이지 접근 시도 체크
  if (pathname.startsWith("/admin")) {
    const authCookie = request.cookies.get("auth-storage");

    if (!authCookie?.value) {
      return NextResponse.redirect(new URL("/login", request.url));
    }

    try {
      const userData = JSON.parse(authCookie.value);
      const userRole = userData?.state?.user?.role?.toUpperCase();

      if (userRole !== "ADMIN") {
        return NextResponse.redirect(new URL("/", request.url));
      }
    } catch (error) {
      return NextResponse.redirect(new URL("/login", request.url));
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/dashboard/:path*", "/login", "/register", "/admin/:path*"],
};
