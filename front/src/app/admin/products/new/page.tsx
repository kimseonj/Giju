"use client";

import type React from "react";

import { useState, useEffect } from "react";
import Link from "next/link";
import { ArrowLeft, Upload, X } from "lucide-react";
import { createDrink } from "@/lib/drink";
import { getCategories } from "@/lib/category";
import { useRouter } from "next/navigation";

export default function NewProductPage() {
  const router = useRouter();
  const [productImage, setProductImage] = useState<string | null>(null);
  const [thumbnailFile, setThumbnailFile] = useState<File | null>(null);
  const [imageFiles, setImageFiles] = useState<File[]>([]);
  const [name, setName] = useState("");
  const [categories, setCategories] = useState<{ id: number; name: string }[]>(
    []
  );
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [price, setPrice] = useState("");
  const [stock, setStock] = useState("");
  const [alcohol, setAlcohol] = useState("");
  const [volume, setVolume] = useState("");
  const [status, setStatus] = useState("판매중");
  const [region, setRegion] = useState("");
  const [loading, setLoading] = useState(false);

  const MAX_SIZE_MB = 10;

  useEffect(() => {
    getCategories().then((data) => setCategories(data as any[]));
  }, []);

  // 이미지 업로드 핸들러
  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > MAX_SIZE_MB * 1024 * 1024) {
        alert("이미지 파일은 10MB 이하만 업로드 가능합니다.");
        return;
      }
      setThumbnailFile(file);
      setProductImage(URL.createObjectURL(file));
    }
  };

  // 이미지 제거 핸들러
  const handleRemoveImage = () => {
    setProductImage(null);
    setThumbnailFile(null);
  };

  // 여러 장 이미지 업로드 핸들러
  const handleFilesUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files) {
      const validFiles = Array.from(files).filter(
        (file) => file.size <= MAX_SIZE_MB * 1024 * 1024
      );
      if (validFiles.length !== files.length) {
        alert("홍보/상세 이미지는 10MB 이하만 업로드 가능합니다.");
      }
      setImageFiles(validFiles);
    }
  };

  // 폼 제출 핸들러
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!thumbnailFile) {
      alert("썸네일 이미지를 업로드해주세요.");
      return;
    }
    if (
      !name ||
      !categoryId ||
      !price ||
      !stock ||
      !alcohol ||
      !volume ||
      !region
    ) {
      alert("모든 필수 정보를 입력해주세요.");
      return;
    }
    setLoading(true);
    try {
      await createDrink(
        {
          name,
          price: Number(price),
          stock: Number(stock),
          alcoholContent: Number(alcohol),
          volume: Number(volume),
          region,
          categoryId: Number(categoryId),
        },
        thumbnailFile,
        imageFiles
      );
      alert("상품이 등록되었습니다.");
      router.push("/admin/products");
    } catch (e) {
      alert("상품 등록에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="mb-8">
        <Link
          href="/admin/products"
          className="inline-flex items-center font-jj font-extrabold text-[21px] text-main hover:text-gray-900 mb-4"
        >
          <ArrowLeft className="w-6 h-6 mr-2" />
          상품 관리로 돌아가기
        </Link>
      </div>

      <div className="bg-white border-gray-200 max-w-[1200px] mx-auto p-10">
        {/* 상품명 */}
        <div>
          <label className="block font-pretendard text-[21px] font-bold text-main mb-3">
            상품명 작성
          </label>
          <div
            className="relative w-full overflow-hidden"
            style={{ height: "56px" }}
          >
            <img
              src="/new-product-name.svg"
              alt="상품명 입력창"
              className="absolute left-0 top-0 w-full h-full pointer-events-none select-none"
              style={{ width: "100%", height: "56px" }}
              draggable={false}
            />
            <input
              className="absolute left-0 top-0 w-full h-full bg-transparent border-none outline-none font-noh font-bold text-[21px] text-main placeholder:text-sub-dark placeholder:font-noh placeholder:font-bold"
              style={{
                paddingLeft: "24px",
                paddingRight: "80px",
                height: "56px",
                lineHeight: "56px",
              }}
              placeholder="노출 상품명"
              value={name}
              onChange={(e) => setName(e.target.value)}
              maxLength={100}
              autoComplete="off"
            />
            <div
              className="absolute text-xs text-[#B18B6C] font-pretendard pointer-events-none"
              style={{
                right: "24px",
                top: "50%",
                transform: "translateY(-50%)",
              }}
            >
              {name.length}/100
            </div>
          </div>
          <div className="text-left text-[16px] text-sub-dark font-pretendard mt-2 mb-8">
            판매 상품과 직접 관련이 없거나 중복된 정보가 적힌 상품명은 기주에
            의해 변경될 수 있습니다.
          </div>
        </div>
        {/* 제품 정보 등록 */}
        <div>
          <label className="block font-bold font-pretendard text-[21px] text-main mb-6 border-b border-main pb-3">
            제품 정보 등록
          </label>
          <div className="flex gap-8">
            <div className="flex-1 min-w-[180px]">
              <div className="font-pretendard text-[18px] font-bold text-main ml-2 mb-2">
                지역
              </div>
              <div className="relative w-full h-[56px]">
                <img
                  src="/tab.svg"
                  alt="지역선택"
                  className="absolute left-0 top-0 w-full h-full pointer-events-none select-none"
                  style={{ width: "100%", height: "56px" }}
                  draggable={false}
                />
                <select
                  className="absolute left-0 top-0 w-full h-full bg-transparent border-none outline-none px-4 text-[18px] font-light text-white appearance-none"
                  style={{ paddingRight: "32px" }}
                  value={region}
                  onChange={(e) => setRegion(e.target.value)}
                >
                  <option value="">지역 선택</option>
                  <option value="서울">서울</option>
                  <option value="경기도">경기도</option>
                  <option value="강원도">강원도</option>
                  <option value="충청북도">충청북도</option>
                  <option value="충청남도">충청남도</option>
                  <option value="전라북도">전라북도</option>
                  <option value="전라남도">전라남도</option>
                  <option value="경상북도">경상북도</option>
                  <option value="경상남도">경상남도</option>
                  <option value="제주도">제주도</option>
                </select>
              </div>
            </div>
            <div className="flex-1 min-w-[180px]">
              <div className="font-pretendard text-[18px] font-bold text-main ml-2 mb-2">
                수량
              </div>
              <div className="relative w-full h-[56px]">
                <img
                  src="/tab.svg"
                  alt="수량입력"
                  className="absolute left-0 top-0 w-full h-full pointer-events-none select-none"
                  style={{ width: "100%", height: "56px" }}
                  draggable={false}
                />
                <input
                  type="number"
                  className="absolute left-0 top-0 w-full h-full bg-transparent border-none outline-none px-4 text-[18px] font-light text-white placeholder:text-white [appearance:textfield]"
                  style={{ paddingRight: "32px", MozAppearance: "textfield" }}
                  placeholder="재고 수량을 입력하세요"
                  value={stock}
                  onChange={(e) => setStock(e.target.value)}
                />
              </div>
            </div>
            <div className="flex-1 min-w-[180px]">
              <div className="font-pretendard text-[18px] font-bold text-main ml-2 mb-2">
                카테고리
              </div>
              <div className="relative w-full h-[56px]">
                <img
                  src="/tab.svg"
                  alt="카테고리선택"
                  className="absolute left-0 top-0 w-full h-full pointer-events-none select-none"
                  style={{ width: "100%", height: "56px" }}
                  draggable={false}
                />
                <select
                  className="absolute left-0 top-0 w-full h-full bg-transparent border-none outline-none px-4 text-[18px] font-light text-white appearance-none"
                  style={{ paddingRight: "32px" }}
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
            </div>
            <div className="flex-1 min-w-[180px]">
              <div className="font-pretendard text-[18px] font-bold text-main ml-2 mb-2">
                도수
              </div>
              <div className="relative w-full h-[56px]">
                <img
                  src="/tab.svg"
                  alt="도수입력"
                  className="absolute left-0 top-0 w-full h-full pointer-events-none select-none"
                  style={{ width: "100%", height: "56px" }}
                  draggable={false}
                />
                <input
                  type="number"
                  className="absolute left-0 top-0 w-full h-full bg-transparent border-none outline-none px-4 text-[18px] font-light text-white placeholder:text-white [appearance:textfield]"
                  style={{ paddingRight: "32px", MozAppearance: "textfield" }}
                  placeholder="알코올 도수를 입력하세요"
                  value={alcohol}
                  onChange={(e) => setAlcohol(e.target.value)}
                />
              </div>
            </div>
            <div className="flex-1 min-w-[180px]">
              <div className="font-pretendard text-[18px] font-bold text-main ml-2 mb-2">
                용량
              </div>
              <div className="relative w-full h-[56px]">
                <img
                  src="/tab.svg"
                  alt="용량입력"
                  className="absolute left-0 top-0 w-full h-full pointer-events-none select-none"
                  style={{ width: "100%", height: "56px" }}
                  draggable={false}
                />
                <input
                  type="number"
                  className="absolute left-0 top-0 w-full h-full bg-transparent border-none outline-none px-4 text-[18px] font-light text-white placeholder:text-white [appearance:textfield]"
                  style={{ paddingRight: "32px", MozAppearance: "textfield" }}
                  placeholder="용량을 입력하세요"
                  value={volume}
                  onChange={(e) => setVolume(e.target.value)}
                />
              </div>
            </div>
          </div>
        </div>
        {/* 구분선 */}
        <div className="border-t border-gray-200 my-6" />
        {/* 가격 */}
        <div className="flex items-center py-4 gap-4">
          <span className="font-bold font-pretendard text-main text-[18px]">
            가격
          </span>
          <input
            type="number"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            className="w-[140px] border border-white rounded-lg px-4 py-2 text-[18px] font-pretendard text-main focus:outline-main [appearance:textfield]"
            style={{ MozAppearance: "textfield" }}
            placeholder="가격입력"
          />
          <span className="text-[18px] text-gray-500">원</span>
        </div>
        <div className="border-b border-gray-200 my-6" />
        {/* 이미지 등록 */}
        <div>
          <label className="block font-bold font-pretendard text-[21px] text-main mb-6 border-b border-main pb-3">
            제품 이미지 등록
          </label>
          <div className="flex gap-6">
            {/* 대표 이미지 */}
            <div className="flex-1 flex flex-col">
              <div className="font-pretendard font-bold text-[18px] mb-2 text-main text-left w-full">
                대표 이미지
              </div>
              <div className="relative w-full h-64 flex items-center justify-center text-gray-400 overflow-hidden">
                <img
                  src="/image-border.svg"
                  alt="대표 이미지 보더"
                  className="absolute left-0 top-0 w-full h-full pointer-events-none select-none"
                  draggable={false}
                />
                {productImage ? (
                  <>
                    <img
                      src={productImage}
                      alt="대표 이미지"
                      className="h-40 object-contain relative z-10"
                    />
                    <button
                      type="button"
                      className="absolute top-2 right-2 bg-white rounded-full p-1 shadow z-20"
                      onClick={handleRemoveImage}
                      tabIndex={-1}
                    >
                      <X className="w-4 h-4 text-gray-500" />
                    </button>
                  </>
                ) : (
                  <div className="flex flex-col items-center justify-center w-full h-full select-none relative z-10">
                    <div className="relative w-16 h-16 mb-5 flex items-center justify-center">
                      <img
                        src="/upload.svg"
                        alt="이미지 등록"
                        className="w-16 h-16"
                        draggable={false}
                      />
                      <img
                        src="/excla.svg"
                        alt="경고"
                        className="absolute left-1/2 top-1/2 w-6 h-6 -translate-x-1/2 -translate-y-1/2"
                        draggable={false}
                      />
                    </div>
                    <span className="text-[17px] text-[#7B7B7B] font-pretendard mb-3">
                      등록된 이미지가 없습니다
                    </span>
                    <span className="text-[20px] text-main font-bold font-pretendard border-b border-main pb-1 cursor-pointer">
                      이미지 등록
                    </span>
                  </div>
                )}
                <input
                  type="file"
                  accept="image/*"
                  className="absolute inset-0 opacity-0 cursor-pointer z-30"
                  onChange={handleImageUpload}
                  tabIndex={-1}
                />
              </div>
            </div>
            {/* 추가 이미지 */}
            <div className="flex-1 flex flex-col">
              <div className="font-pretendard font-bold text-[18px] mb-2 text-main text-left w-full">
                추가 이미지
              </div>
              <div className="relative w-full h-64 flex items-center justify-center text-gray-400 overflow-hidden">
                <img
                  src="/image-border.svg"
                  alt="추가 이미지 보더"
                  className="absolute left-0 top-0 w-full h-full pointer-events-none select-none"
                  draggable={false}
                />
                {imageFiles.length > 0 ? (
                  <div className="flex gap-2 overflow-x-auto w-full h-full items-center px-2 relative z-10">
                    {imageFiles.map((file, idx) => (
                      <img
                        key={idx}
                        src={URL.createObjectURL(file)}
                        alt={`추가 이미지 ${idx + 1}`}
                        className="h-40 object-contain border bg-white rounded"
                      />
                    ))}
                  </div>
                ) : (
                  <div className="flex flex-col items-center justify-center w-full h-full select-none relative z-10">
                    <div className="relative w-16 h-16 mb-5 flex items-center justify-center">
                      <img
                        src="/upload.svg"
                        alt="이미지 등록"
                        className="w-16 h-16"
                        draggable={false}
                      />
                      <img
                        src="/excla.svg"
                        alt="경고"
                        className="absolute left-1/2 top-1/2 w-6 h-6 -translate-x-1/2 -translate-y-1/2"
                        draggable={false}
                      />
                    </div>
                    <span className="text-[17px] text-[#7B7B7B] font-pretendard mb-3">
                      등록된 이미지가 없습니다
                    </span>
                    <span className="text-[20px] text-main font-bold font-pretendard border-b border-main pb-1 cursor-pointer">
                      이미지 등록
                    </span>
                  </div>
                )}
                <input
                  type="file"
                  accept="image/*"
                  multiple
                  className="absolute inset-0 opacity-0 cursor-pointer z-30"
                  onChange={handleFilesUpload}
                  tabIndex={-1}
                />
              </div>
            </div>
          </div>
          {/* 상세페이지 이미지 */}
          <div className="mt-4">
            <div className="font-pretendard font-bold text-[18px] mb-2 text-main text-left w-full">
              상세페이지 이미지
            </div>
            <div className="relative w-full h-64 flex items-center justify-center text-gray-400 overflow-hidden">
              <img
                src="/page-image.svg"
                alt="상세페이지 이미지 보더"
                className="absolute left-0 top-0 w-full h-full pointer-events-none select-none"
                draggable={false}
              />
              {imageFiles.length > 0 ? (
                <div className="flex gap-2 overflow-x-auto w-full h-full items-center px-2 relative z-10">
                  {imageFiles.map((file, idx) => (
                    <img
                      key={idx}
                      src={URL.createObjectURL(file)}
                      alt={`상세 이미지 ${idx + 1}`}
                      className="h-40 object-contain border bg-white rounded"
                    />
                  ))}
                </div>
              ) : (
                <div className="flex flex-col items-center justify-center w-full h-full select-none relative z-10">
                  <div className="relative w-16 h-16 mb-5 flex items-center justify-center">
                    <img
                      src="/upload.svg"
                      alt="이미지 등록"
                      className="w-16 h-16"
                      draggable={false}
                    />
                    <img
                      src="/excla.svg"
                      alt="경고"
                      className="absolute left-1/2 top-1/2 w-6 h-6 -translate-x-1/2 -translate-y-1/2"
                      draggable={false}
                    />
                  </div>
                  <span className="text-[17px] text-[#7B7B7B] font-pretendard mb-3">
                    등록된 이미지가 없습니다
                  </span>
                  <span className="text-[20px] text-main font-bold font-pretendard border-b border-main pb-1 cursor-pointer">
                    이미지 등록
                  </span>
                </div>
              )}
              <input
                type="file"
                accept="image/*"
                multiple
                className="absolute inset-0 opacity-0 cursor-pointer z-30"
                onChange={handleFilesUpload}
                tabIndex={-1}
              />
            </div>
          </div>
        </div>
        {/* 저장 버튼 */}
        <button
          className="w-full bg-main text-white rounded-lg py-3 mt-8 font-bold text-lg"
          disabled={loading}
        >
          저장하기
        </button>
      </div>
    </div>
  );
}
