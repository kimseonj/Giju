"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { searchDrinks } from "@/lib/drink";
import Link from "next/link";

// 영문 코드 → 한글 지역명 매핑
const regionNameMap: Record<string, string> = {
  gyeonggi: "경기도",
  gangwon: "강원도",
  chungbuk: "충청북도",
  chungnam: "충청남도",
  jeonbuk: "전라북도",
  jeonnam: "전라남도",
  gyeongbuk: "경상북도",
  gyeongnam: "경상남도",
  jeju: "제주도",
};

export default function RegionPage() {
  const { region } = useParams();
  const regionStr = Array.isArray(region) ? region[0] : (region as string);
  const [drinks, setDrinks] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!regionStr) return;
    const keyword = regionNameMap[regionStr] || regionStr;
    searchDrinks({
      type: "region",
      keyword,
    })
      .then((data: any) => {
        setDrinks(data.content || []);
        setLoading(false);
      })
      .catch((err) => {
        setError(
          err instanceof Error
            ? err.message
            : "술 목록을 불러오는데 실패했습니다."
        );
        setLoading(false);
      });
  }, [regionStr]);

  if (!regionStr) return <div>지역이 지정되지 않았습니다.</div>;
  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">로딩 중...</div>
      </div>
    );
  }
  if (error) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center text-red-500">에러: {error}</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex items-center gap-4 mb-6">
        <Link
          href="/"
          className="font-pretendard text-main font-32pt font-extrabold"
        >
          &lt; {regionNameMap[regionStr] || regionStr} 전통주 목록
        </Link>
      </div>
      {drinks.length === 0 ? (
        <div className="text-center text-gray-500 py-8">
          해당 지역의 술이 없습니다.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
          {drinks.map((drink) => (
            <Link
              key={drink.id}
              href={`/drink/${drink.id}`}
              className="block overflow-hidden bg-white hover:shadow-lg transition"
            >
              {drink.thumbnailUrl && (
                <div className="relative h-48">
                  <img
                    src={drink.thumbnailUrl}
                    alt={drink.name}
                    className="w-full h-full object-contain"
                  />
                </div>
              )}
              <div className="p-4">
                <h2 className="text-lg font-pretendard font-18pt font-extrabold mb-2">
                  {drink.name}
                </h2>
                <div className="text-gray-600">
                  <p>지역: {drink.region}</p>
                  <p>도수: {drink.alcoholContent}%</p>
                  <p>용량: {drink.volume}ml</p>
                  <p className="font-pretendard font-18pt font-extrabold text-main mt-2">
                    {drink.price?.toLocaleString()}원
                  </p>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
