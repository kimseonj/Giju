"use client";

import { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import { getCategories } from "@/lib/category";
import { updateDrink } from "@/lib/drink";

export default function EditProductPage() {
  const router = useRouter();
  const params = useParams();
  const drinkId = Number(params.id);

  const [price, setPrice] = useState("");
  const [stock, setStock] = useState("");
  const [region, setRegion] = useState("");
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [categories, setCategories] = useState<{ id: number; name: string }[]>(
    []
  );
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    getCategories().then((data: any) => setCategories(data as any[]));
    // TODO: 상품 상세 정보 불러와서 setPrice, setStock, setRegion, setCategoryId 등 세팅
    // 예시: fetch(`/api/admin/drink/${drinkId}`) ...
  }, [drinkId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!price || !stock || !region || !categoryId) {
      alert("모든 필수 정보를 입력해주세요.");
      return;
    }
    setLoading(true);
    try {
      await updateDrink(drinkId, {
        price: Number(price),
        stock: Number(stock),
        region,
        categoryId: Number(categoryId),
      });
      alert("수정되었습니다.");
      router.push("/admin/products");
    } catch (e) {
      alert("수정 실패");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">상품 수정</h1>
      <form
        className="max-w-xl mx-auto bg-white p-6 rounded shadow"
        onSubmit={handleSubmit}
      >
        <div className="mb-4">
          <label className="block mb-1 font-medium">가격</label>
          <input
            type="number"
            className="w-full border rounded px-3 py-2"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
          />
        </div>
        <div className="mb-4">
          <label className="block mb-1 font-medium">재고</label>
          <input
            type="number"
            className="w-full border rounded px-3 py-2"
            value={stock}
            onChange={(e) => setStock(e.target.value)}
          />
        </div>
        <div className="mb-4">
          <label className="block mb-1 font-medium">지역</label>
          <input
            type="text"
            className="w-full border rounded px-3 py-2"
            value={region}
            onChange={(e) => setRegion(e.target.value)}
          />
        </div>
        <div className="mb-4">
          <label className="block mb-1 font-medium">카테고리</label>
          <select
            className="w-full border rounded px-3 py-2"
            value={categoryId ?? ""}
            onChange={(e) => setCategoryId(Number(e.target.value))}
          >
            <option value="">카테고리 선택</option>
            {categories.map((cat) => (
              <option key={cat.id} value={cat.id}>
                {cat.name}
              </option>
            ))}
          </select>
        </div>
        <button
          type="submit"
          className="w-full bg-orange-600 text-white py-2 rounded hover:bg-orange-700"
          disabled={loading}
        >
          저장
        </button>
      </form>
    </div>
  );
}
