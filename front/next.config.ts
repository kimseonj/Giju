import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  images: {
    domains: [
      "giju-bubble.s3.ap-northeast-2.amazonaws.com",
      // 필요시 다른 도메인도 추가
    ],
  },
};

export default nextConfig;
