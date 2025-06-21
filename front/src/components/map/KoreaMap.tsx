"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

const regions = [
  { name: "경기도", code: "gyeonggi", top: "26%", left: "57%" },
  { name: "강원도", code: "gangwon", top: "23%", left: "73%" },
  { name: "충청북도", code: "chungbuk", top: "40%", left: "67%" },
  { name: "충청남도", code: "chungnam", top: "46%", left: "54%" },
  { name: "전라북도", code: "jeonbuk", top: "61%", left: "58%" },
  { name: "전라남도", code: "jeonnam", top: "75%", left: "54%" },
  { name: "경상북도", code: "gyeongbuk", top: "50%", left: "80%" },
  { name: "경상남도", code: "gyeongnam", top: "68%", left: "76%" },
  { name: "제주도", code: "jeju", top: "94.2%", left: "49%" },
];

const regionSvgs = [
  { code: "gyeonggi", top: 22, left: 242 },
  { code: "gangwon", top: 13, left: 253 },
  { code: "chungbuk", top: 23, left: 253 },
  { code: "chungnam", top: 23, left: 253 },
  { code: "jeonbuk", top: 23, left: 253 },
  { code: "jeonnam", top: 23, left: 235 },
  { code: "gyeongbuk", top: 23, left: 253 },
  { code: "gyeongnam", top: 23, left: 253 },
  { code: "jeju", top: 23, left: 250 },
];

export default function KoreaMap() {
  const [hovered, setHovered] = useState<string | null>(null);
  const router = useRouter();

  return (
    <div className="w-full flex justify-center items-center">
      <div className="relative w-full max-w-2xl">
        <img
          src="/images/korea-map.svg"
          alt="한국 지도"
          className="w-full h-auto"
        />
        {regions.map((region) => (
          <button
            key={region.code}
            className="absolute z-10 rounded-full"
            style={{
              top: region.top,
              left: region.left,
              width: 60,
              height: region.code === "gyeongbuk" ? 100 : 40,
              background: "transparent",
              border: "none",
              color: "transparent",
              boxShadow: "none",
              transform: "translate(-50%, -50%)",
              cursor: "pointer",
            }}
            onMouseEnter={() => setHovered(region.code)}
            onMouseLeave={() => setHovered(null)}
            onClick={() => router.push(`/region/${region.code}`)}
            aria-label={region.name}
          ></button>
        ))}
        {regionSvgs.map((svg) =>
          hovered === svg.code ? (
            <img
              key={svg.code}
              src={`/region/${svg.code}.svg`}
              alt={svg.code}
              className="absolute w-full h-auto pointer-events-none"
              style={{
                top: svg.top,
                left: svg.left,
              }}
            />
          ) : null
        )}
      </div>
    </div>
  );
}
