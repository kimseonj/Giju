"use client";

import { useState, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { searchDrinks } from "@/lib/drink";
import Link from "next/link";
import Image from "next/image";

interface Drink {
  id: number;
  name: string;
  price: number;
  alcoholContent: number;
  volume: number;
  region: string;
  category: {
    id: number;
    name: string;
  };
  thumbnailUrl: string;
}

interface DrinkSearchResponse {
  content: Drink[];
}

export default function SearchPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const keywordParam = searchParams.get("keyword") || "";

  const [keyword, setKeyword] = useState(keywordParam);
  const [drinks, setDrinks] = useState<Drink[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 쿼리스트링이 바뀌면 자동 검색
  useEffect(() => {
    if (!keywordParam) {
      setDrinks([]);
      setKeyword("");
      return;
    }
    setKeyword(keywordParam);
    setLoading(true);
    setError(null);
    searchDrinks({ type: "name", keyword: keywordParam })
      .then((response) => {
        setDrinks((response as DrinkSearchResponse).content || []);
      })
      .catch(() => setError("검색에 실패했습니다."))
      .finally(() => setLoading(false));
  }, [keywordParam]);

  // 검색 submit 시 쿼리스트링 갱신
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (keyword.trim()) {
      router.push(`/search?keyword=${encodeURIComponent(keyword.trim())}`);
    }
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">술 이름으로 검색</h1>
      <form
        onSubmit={handleSearch}
        className="relative flex items-center w-[328px] h-[38px] mb-8 mx-auto"
        style={{ maxWidth: 328 }}
      >
        {/* 검색창 배경 SVG */}
        <Image
          src="/search-bg.svg"
          alt="검색창 배경"
          fill
          className="absolute left-0 top-0 w-full h-full z-0"
        />
        {/* 왼쪽 placeholder SVG (input 맨 왼쪽에 겹치게) */}
        {keyword === "" && (
          <span className="absolute left-3 top-1/2 -translate-y-1/2 z-10 pointer-events-none">
            <Image
              src="/search-deco.svg"
              alt="검색 데코"
              width={170}
              height={28}
            />
          </span>
        )}
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          // placeholder="술 이름을 입력하세요"
          className="pl-4 pr-10 py-2 bg-transparent border-none text-white w-full h-full relative z-10 focus:outline-none"
          style={{ background: "transparent" }}
        />
        {/* 돋보기 아이콘 */}
        <button
          type="submit"
          className="absolute right-2 top-1/2 -translate-y-1/2 z-10"
          disabled={loading || !keyword.trim()}
        >
          <Image src="/search-icon.svg" alt="검색" width={22} height={22} />
        </button>
      </form>
      {loading && <div className="text-center">로딩 중...</div>}
      {error && <div className="text-center text-red-500">{error}</div>}
      {!loading && !error && drinks.length === 0 && keywordParam && (
        <div className="text-center text-gray-500">검색 결과가 없습니다.</div>
      )}
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
    </div>
  );
}
