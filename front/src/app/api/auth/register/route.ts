import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    console.log("회원가입 요청 데이터:", body);

    // 백엔드 API 호출
    const response = await fetch(`/auth/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const errorData = await response.json();
      console.log("회원가입 에러 응답:", errorData);
      return NextResponse.json(
        { message: errorData.message || "회원가입에 실패했습니다." },
        { status: response.status }
      );
    }

    const data = await response.json();
    console.log("회원가입 성공 응답:", data);
    console.log(response.headers);
    // 응답 생성
    const nextResponse = NextResponse.json({
      message: "success",
      user: data.user,
    });

    // Access Token을 쿠키에 저장 (바디에서 가져옴)
    nextResponse.cookies.set("access_token", data.cookies.accessToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "strict",
      maxAge: 3600, // 1시간
    });

    // Refresh Token을 쿠키에 저장 (바디에서 가져옴)
    nextResponse.cookies.set("refresh_token", data.cookies.refreshToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "strict",
      maxAge: 7 * 24 * 3600, // 7일
    });

    // 사용자 권한을 쿠키에 저장
    nextResponse.cookies.set("user_role", data.user.role, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "strict",
      maxAge: 7 * 24 * 3600, // 7일
    });

    console.log("설정된 쿠키:", {
      access_token: data.cookies.accessToken,
      refresh_token: data.cookies.refreshToken,
      user_role: data.user.role,
    });

    return nextResponse;
  } catch (error) {
    console.error("회원가입 처리 중 오류 발생:", error);
    return NextResponse.json(
      { message: "회원가입 처리 중 오류가 발생했습니다." },
      { status: 500 }
    );
  }
}
