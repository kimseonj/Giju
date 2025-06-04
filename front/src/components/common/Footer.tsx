import Link from "next/link";
import Image from "next/image";

export default function Footer() {
  return (
    <footer className="w-full relative h-[180px] text-left text-lg text-white font-inter overflow-hidden bg-main">
      {/* 중간 라인 */}
      <div
        className="absolute left-[20.24%] right-[8.89%] top-[44.55%] h-[0.75px] bg-gray"
        style={{ width: "70.87%" }}
      />
      {/* 개인정보처리방침 | 이용약관 */}
      <div className="absolute" style={{ top: "27.34%", left: "20.31%" }}>
        개인정보처리방침 | 이용약관
      </div>
      {/* SNS */}
      <div
        className="absolute text-[var(--color-gray)] text-right"
        style={{ top: "27.34%", left: "80.69%" }}
      >
        SNS | @abcd_kiju
      </div>
      {/* 주소 및 카피라이트 */}
      <div
        className="absolute text-[var(--color-gray)]"
        style={{ width: "32.71%", top: "55.45%", left: "20.1%" }}
      >
        <div>[12345] 광주광역시 00대로 00번길 12-34</div>
        <div className="text-xs mt-2">
          Copyright © 2025 by Domisol and Giju all rights reserved
        </div>
      </div>
      {/* 이용문의 */}
      <div className="absolute" style={{ top: "59.97%", left: "70.97%" }}>
        이용문의 |
      </div>
      <div
        className="absolute font-medium text-right"
        style={{ top: "57.23%", left: "76.51%", fontSize: "25.18px" }}
      >
        062-123-4567
      </div>
      {/* 로고 SVG */}
      <Image
        src="/logo.svg"
        alt="로고"
        width={110}
        height={110}
        className="absolute"
        style={{
          top: "32.33%",
          left: "100px",
          width: "8.5%",
          height: "34%",
          minWidth: "80px",
        }}
      />
    </footer>
  );
}
