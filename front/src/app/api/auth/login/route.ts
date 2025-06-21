import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    console.log("로그인 요청 데이터:", body);

    const response = await fetch(`/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });

    const responseText = await response.text();
    let data;
    try {
      data = JSON.parse(responseText);
    } catch (error) {
      console.error("JSON 파싱 실패:", error);
      console.error("응답 본문:", responseText);
      return NextResponse.json(
        { message: "서버 응답을 처리할 수 없습니다." },
        { status: 500 }
      );
    }

    // 로그인 실패 처리
    if (!response.ok) {
      const errorMessage =
        data?.error?.message || data?.message || "로그인 실패";
      return NextResponse.json(
        { message: errorMessage },
        { status: response.status }
      );
    }

    // 로그인 성공 처리
    return NextResponse.json({
      message: data.message || "로그인 성공",
      user: data.user,
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
    });
  } catch (error) {
    console.error("로그인 처리 중 오류 발생:", error);
    return NextResponse.json(
      { message: "로그인 처리 중 오류가 발생했습니다." },
      { status: 500 }
    );
  }
}
