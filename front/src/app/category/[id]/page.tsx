"use client";

import { useEffect, useState } from "react";
import { searchDrinks } from "@/lib/drink";
import Link from "next/link";
import { useParams } from "next/navigation";

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

export default function CategoryDetailPage() {
  const params = useParams();
  const id = params.id as string;
  const [drinks, setDrinks] = useState<Drink[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchDrinks() {
      try {
        const categoryId = parseInt(id, 10);

        if (isNaN(categoryId)) {
          throw new Error("유효하지 않은 카테고리 ID입니다.");
        }

        console.log("검색 파라미터:", {
          type: "category",
          keyword: categoryId.toString(),
        });

        const response = (await searchDrinks({
          type: "category",
          keyword: categoryId.toString(),
        })) as DrinkSearchResponse;

        console.log("API 응답:", response);

        setDrinks(response.content);
      } catch (err) {
        console.error("API 에러:", err);
        setError(
          err instanceof Error
            ? err.message
            : "술 목록을 불러오는데 실패했습니다."
        );
      } finally {
        setLoading(false);
      }
    }

    fetchDrinks();
  }, [id]);

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
          href="/categories"
          className="text-orange-500 hover:text-orange-600"
        >
          ← 카테고리 목록
        </Link>
        <h1 className="text-2xl font-bold">
          {drinks[0]?.category?.name || "카테고리"} 목록
        </h1>
      </div>

      {drinks.length === 0 ? (
        <div className="text-center text-gray-500 py-8">
          해당 카테고리의 술이 없습니다.
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
