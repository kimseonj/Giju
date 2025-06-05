"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import {
  Search,
  Plus,
  Filter,
  Edit,
  Trash2,
  ChevronLeft,
  ChevronRight,
  ArrowUpDown,
  RefreshCw,
  Search as SearchIcon,
} from "lucide-react";
import { deleteDrink, getDrink, restoreDrink, searchDrinks } from "@/lib/drink";
import Image from "next/image";

// 상품 타입 정의
interface Product {
  id: number;
  name: string;
  category: string;
  price: string;
  stock: number;
  status: "판매중" | "품절" | "숨김";
  thumbnailUrl: string;
  option?: string;
  salesCount?: number;
  weeklySales?: number;
}

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [selected, setSelected] = useState<number[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [pageNum, setPageNum] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  useEffect(() => {
    async function fetchProducts() {
      try {
        const data: any = await searchDrinks({
          type: "name",
          keyword: searchTerm,
          pageNum,
        });
        setProducts(
          (data.content || data.data || []).map((drink: any) => ({
            id: drink.id,
            name: drink.name,
            category: drink.category?.name || "",
            price: drink.price.toLocaleString() + "원",
            stock: drink.stock,
            status: drink.is_delete ? "숨김" : "판매중",
            thumbnailUrl: drink.thumbnailUrl || "/noimg.png",
            option: drink.option,
            salesCount: drink.salesCount,
            weeklySales: drink.weeklySales,
          }))
        );
        setTotalPages(data.totalPages);
      } catch (e) {
        setProducts([]);
      }
    }
    fetchProducts();
  }, [searchTerm, pageNum]);

  // 상품 삭제 핸들러 (soft delete)
  const handleDeleteProduct = async (id: number) => {
    if (!window.confirm("정말로 이 상품을 숨기시겠습니까?")) return;
    try {
      await deleteDrink(id);
      setProducts(
        products.map((p) => (p.id === id ? { ...p, status: "숨김" } : p))
      );
      alert("숨김 처리되었습니다.");
    } catch (e) {
      alert("숨김 실패");
    }
  };

  // 상품 재판매(복구) 핸들러
  const handleRestoreProduct = async (id: number) => {
    try {
      await restoreDrink(id);
      setProducts(
        products.map((p) => (p.id === id ? { ...p, status: "판매중" } : p))
      );
      alert("재판매 처리되었습니다.");
    } catch (e) {
      alert("재판매 실패");
    }
  };

  // 체크박스 핸들러
  const handleSelect = (id: number) => {
    setSelected((prev) =>
      prev.includes(id) ? prev.filter((sid) => sid !== id) : [...prev, id]
    );
  };
  const handleSelectAll = () => {
    if (selected.length === products.length) {
      setSelected([]);
    } else {
      setSelected(products.map((p) => p.id));
    }
  };

  // 필터링된 상품 목록
  const filteredProducts = products.filter((product) => {
    const matchesSearch = product.name
      .toLowerCase()
      .includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory
      ? product.category === selectedCategory
      : true;
    return matchesSearch && matchesCategory;
  });

  // 카테고리 목록
  const categories = Array.from(
    new Set(products.map((product) => product.category))
  );

  return (
    <div className="w-full px-8 py-8">
      {/* 상단 타이틀/검색/필터/버튼 */}
      <div className="mb-6">
        <div className="font-pretendard text-[21px] font-extrabold text-main mb-4">
          상품관리
        </div>
        <div className="flex items-center w-full border-b border-[#D9D9D9] pb-4 justify-start gap-4">
          <div
            className="relative flex items-center"
            style={{ width: 728, height: 54 }}
          >
            {/* SVG 배경 */}
            <Image
              src="/white-search-bg.svg"
              alt="검색창"
              fill
              className="absolute left-0 top-0 w-full h-full z-0"
            />
            {/* 돋보기 아이콘 */}
            <span className="absolute left-6 top-1/2 -translate-y-1/2 z-10 text-[#B0B8C1]">
              <SearchIcon className="w-7 h-7" />
            </span>
            {/* input */}
            <input
              type="text"
              placeholder="상품명 검색"
              className="relative z-10 font-noh bg-transparent border-none focus:outline-none text-[20px] text-[#333] pl-16 pr-4 h-full flex-1"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              maxLength={40}
              style={{ minWidth: 0 }}
            />
            {/* select */}
            <select
              className="relative z-10 bg-transparent text-[#B18B6C] text-base border-none border-b border-[#B18B6C] min-w-[150px] h-[42px] cursor-pointer font-pretendard font-light ml-3"
              value={selectedCategory || ""}
              onChange={(e) => setSelectedCategory(e.target.value || null)}
              style={{ marginRight: 20 }}
            >
              <option value="">카테고리 필터</option>
              {categories.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </div>
          <Link
            href="/admin/products/new"
            className="relative ml-63 flex items-center justify-center"
            style={{ width: 180, height: 53 }}
          >
            <Image
              src="/addDrinkButton.svg"
              alt="새 상품 등록"
              fill
              className="absolute  left-0 top-0 w-full h-full z-0"
              style={{ objectFit: "cover" }}
            />
            <span className="relative z-10 w-full text-center font-pretendard font-light text-[18px] text-white select-none">
              + 새 상품 등록
            </span>
          </Link>
        </div>
      </div>

      {/* 전체선택/선택삭제/상태필터 */}
      <div className="flex items-center gap-4 mb-4">
        <label className="flex items-center gap-2 cursor-pointer select-none">
          <input
            type="checkbox"
            checked={
              selected.length === filteredProducts.length &&
              filteredProducts.length > 0
            }
            onChange={handleSelectAll}
          />
          <span className="text-sm">전체상품 {filteredProducts.length}개</span>
        </label>
        <button
          className="text-main text-sm hover:underline disabled:text-gray-300"
          disabled={selected.length === 0}
        >
          선택삭제
        </button>
        <span className="text-gray-400 text-sm">|</span>
        <button className="text-main text-sm hover:underline">판매중</button>
        <button className="text-main text-sm hover:underline">숨김</button>
        <button className="text-main text-sm hover:underline">품절</button>
      </div>

      {/* 상품 리스트 헤더 */}
      <div className="w-full border-t border-b border-[#1A3A47] mt-6">
        <div className="flex items-center h-[48px] bg-white text-[#1A3A47] text-[15px] font-semibold border-b border-[#D9D9D9]">
          <div className="flex items-center px-4 w-[320px]">
            <input
              type="checkbox"
              checked={
                selected.length === products.length && products.length > 0
              }
              onChange={handleSelectAll}
              className="w-5 h-5 accent-main mr-2"
            />
            전체상품 {products.length}개
            <span className="ml-3 text-[#B18B6C] font-normal cursor-pointer">
              선택제품 수정/삭제
            </span>
          </div>
          <div className="px-4 flex-1 text-center">상품 판매량</div>
          <div className="px-4 w-[120px] text-center">판매상태</div>
          <div className="px-4 w-[120px] text-center">재고수량</div>
        </div>
        {/* 상품 리스트 */}
        {products.map((product, idx) => (
          <div
            key={product.id}
            className="flex items-center h-[168px] border-b border-[#E5E5E5] bg-white hover:bg-[#F0F6FA]"
          >
            <div className="flex items-center px-4 w-[320px] h-full">
              <input
                type="checkbox"
                checked={selected.includes(product.id)}
                onChange={() => handleSelect(product.id)}
                className="w-5 h-5 accent-main mr-4"
              />
              <img
                src={product.thumbnailUrl}
                alt={product.name}
                className="w-40 h-40 object-cover rounded-lg mr-4"
              />
              <div className="flex flex-col justify-center h-full">
                <div className="text-[21px] text-main font-pretendard font-extrabold leading-tight truncate max-w-[180px]">
                  {product.name}
                </div>
                <div className="text-xs text-[#888] leading-tight truncate max-w-[180px]">
                  {product.option || ""}
                </div>
                <div
                  className="text-xs text-main font-pretendard font-light leading-tight"
                  style={{ whiteSpace: "nowrap" }}
                >
                  판매가 {product.price}
                </div>
              </div>
            </div>
            <div className="px-4 flex-1 text-center text-[15px]">
              {product.salesCount || 0}개<br />
              <span className="text-xs text-[#B0B8C1]">
                1주 {product.weeklySales || 0}개
              </span>
            </div>
            <div className="px-4 w-[120px] text-center font-bold text-[15px]">
              <span
                className={
                  product.status === "판매중"
                    ? "text-[#FF9100]"
                    : product.status === "숨김"
                    ? "text-[#B18B6C]"
                    : "text-[#B0B8C1]"
                }
              >
                {product.status}
              </span>
            </div>
            <div className="px-4 w-[120px] text-center text-[15px]">
              {product.stock === 0 ? (
                <span className="text-[#B0B8C1]">재고없음</span>
              ) : (
                `재고 ${product.stock}개`
              )}
            </div>
          </div>
        ))}
      </div>

      {/* 페이지네이션 */}
      <div className="flex justify-center gap-2 my-6">
        <button
          onClick={() => setPageNum((prev) => Math.max(1, prev - 1))}
          disabled={pageNum === 1}
          className="px-3 py-1 border rounded disabled:opacity-50"
        >
          {"<"}
        </button>
        <span className="px-2 py-1">
          {pageNum} / {totalPages}
        </span>
        <button
          onClick={() => setPageNum((prev) => Math.min(totalPages, prev + 1))}
          disabled={pageNum === totalPages}
          className="px-3 py-1 border rounded disabled:opacity-50"
        >
          {">"}
        </button>
      </div>
    </div>
  );
}
