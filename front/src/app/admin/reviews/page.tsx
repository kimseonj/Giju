"use client";

import { useState } from "react";
import {
  Search,
  Filter,
  Star,
  Trash2,
  ChevronLeft,
  ChevronRight,
  ArrowUpDown,
  Eye,
} from "lucide-react";

// 리뷰 타입 정의
interface Review {
  id: number;
  productName: string;
  userName: string;
  rating: number;
  content: string;
  date: string;
  status: "승인" | "대기중" | "신고됨";
}

export default function ReviewsPage() {
  // 리뷰 목록 (실제로는 API에서 가져올 것)
  const [reviews, setReviews] = useState<Review[]>([
    {
      id: 1,
      productName: "경기도 막걸리",
      userName: "김민수",
      rating: 5,
      content: "정말 맛있는 막걸리입니다. 향이 좋고 목넘김이 부드러워요.",
      date: "2025-05-10",
      status: "승인",
    },
    {
      id: 2,
      productName: "전통 동동주",
      userName: "이지은",
      rating: 4,
      content: "달콤한 맛이 일품이에요. 다음에도 구매할 예정입니다.",
      date: "2025-05-09",
      status: "승인",
    },
    {
      id: 3,
      productName: "제주 오메기술",
      userName: "박준호",
      rating: 3,
      content: "평범한 맛이었습니다. 특별한 느낌은 없었어요.",
      date: "2025-05-08",
      status: "승인",
    },
    {
      id: 4,
      productName: "안동 소주",
      userName: "최유진",
      rating: 5,
      content: "향이 독특하고 맛이 깊어요. 선물용으로도 좋을 것 같습니다.",
      date: "2025-05-07",
      status: "승인",
    },
    {
      id: 5,
      productName: "복분자주",
      userName: "정승환",
      rating: 2,
      content: "생각보다 맛이 없었어요. 너무 달고 향이 인공적입니다.",
      date: "2025-05-06",
      status: "신고됨",
    },
    {
      id: 6,
      productName: "매실주",
      userName: "강지원",
      rating: 4,
      content: "상큼한 맛이 좋아요. 도수도 적당해서 부담없이 마실 수 있습니다.",
      date: "2025-05-05",
      status: "승인",
    },
    {
      id: 7,
      productName: "청주",
      userName: "윤서연",
      rating: 5,
      content: "깔끔한 맛이 일품입니다. 음식과 함께 먹으면 더 맛있어요.",
      date: "2025-05-04",
      status: "대기중",
    },
  ]);

  const [searchTerm, setSearchTerm] = useState("");
  const [selectedStatus, setSelectedStatus] = useState<string | null>(null);

  // 리뷰 삭제 핸들러
  const handleDeleteReview = (id: number) => {
    if (window.confirm("정말로 이 리뷰를 삭제하시겠습니까?")) {
      setReviews(reviews.filter((review) => review.id !== id));
    }
  };

  // 필터링된 리뷰 목록
  const filteredReviews = reviews.filter((review) => {
    const matchesSearch =
      review.productName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      review.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      review.content.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = selectedStatus
      ? review.status === selectedStatus
      : true;
    return matchesSearch && matchesStatus;
  });

  // 별점 렌더링 함수
  const renderStars = (rating: number) => {
    return Array(5)
      .fill(0)
      .map((_, i) => (
        <Star
          key={i}
          className={`w-4 h-4 ${
            i < rating ? "text-yellow-400 fill-yellow-400" : "text-gray-300"
          }`}
        />
      ));
  };

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-2xl font-bold mb-2">리뷰 관리</h1>
        <p className="text-gray-500">
          고객 리뷰를 관리하고 부적절한 리뷰를 삭제하세요.
        </p>
      </div>

      {/* 검색 및 필터 */}
      <div className="bg-white rounded-lg shadow mb-6">
        <div className="p-6 flex flex-col md:flex-row gap-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
            <input
              type="text"
              placeholder="상품명, 작성자, 내용 검색..."
              className="pl-10 pr-4 py-2 border rounded-md w-full focus:outline-none focus:ring-2 focus:ring-orange-500"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <div className="relative">
            <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
            <select
              className="pl-10 pr-8 py-2 border rounded-md appearance-none bg-white focus:outline-none focus:ring-2 focus:ring-orange-500"
              value={selectedStatus || ""}
              onChange={(e) => setSelectedStatus(e.target.value || null)}
            >
              <option value="">모든 상태</option>
              <option value="승인">승인</option>
              <option value="대기중">대기중</option>
              <option value="신고됨">신고됨</option>
            </select>
          </div>
        </div>
      </div>

      {/* 리뷰 목록 테이블 */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-gray-50 text-left text-gray-600 text-sm">
                <th className="px-6 py-3 font-medium">
                  <div className="flex items-center">
                    ID
                    <ArrowUpDown className="w-4 h-4 ml-1" />
                  </div>
                </th>
                <th className="px-6 py-3 font-medium">상품명</th>
                <th className="px-6 py-3 font-medium">작성자</th>
                <th className="px-6 py-3 font-medium">
                  <div className="flex items-center">
                    별점
                    <ArrowUpDown className="w-4 h-4 ml-1" />
                  </div>
                </th>
                <th className="px-6 py-3 font-medium">내용</th>
                <th className="px-6 py-3 font-medium">
                  <div className="flex items-center">
                    작성일
                    <ArrowUpDown className="w-4 h-4 ml-1" />
                  </div>
                </th>
                <th className="px-6 py-3 font-medium">상태</th>
                <th className="px-6 py-3 font-medium">관리</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filteredReviews.map((review) => (
                <tr key={review.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-sm">{review.id}</td>
                  <td className="px-6 py-4">
                    <div className="font-medium">{review.productName}</div>
                  </td>
                  <td className="px-6 py-4 text-sm">{review.userName}</td>
                  <td className="px-6 py-4">
                    <div className="flex">{renderStars(review.rating)}</div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="truncate max-w-xs">{review.content}</div>
                  </td>
                  <td className="px-6 py-4 text-sm">{review.date}</td>
                  <td className="px-6 py-4">
                    <span
                      className={`px-2 py-1 rounded-full text-xs ${
                        review.status === "승인"
                          ? "bg-green-100 text-green-800"
                          : review.status === "대기중"
                          ? "bg-yellow-100 text-yellow-800"
                          : "bg-red-100 text-red-800"
                      }`}
                    >
                      {review.status}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex space-x-2">
                      <button
                        className="p-1 text-blue-600 hover:text-blue-800 rounded-full hover:bg-blue-100"
                        title="리뷰 상세 보기"
                      >
                        <Eye className="w-5 h-5" />
                      </button>
                      <button
                        onClick={() => handleDeleteReview(review.id)}
                        className="p-1 text-red-600 hover:text-red-800 rounded-full hover:bg-red-100"
                        title="리뷰 삭제"
                      >
                        <Trash2 className="w-5 h-5" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* 페이지네이션 */}
        <div className="px-6 py-4 flex items-center justify-between border-t">
          <div className="text-sm text-gray-500">
            총 <span className="font-medium">{filteredReviews.length}</span>개
            리뷰
          </div>
          <div className="flex space-x-2">
            <button className="p-2 rounded-md border hover:bg-gray-50">
              <ChevronLeft className="w-5 h-5" />
            </button>
            <button className="px-3 py-1 rounded-md bg-orange-600 text-white">
              1
            </button>
            <button className="px-3 py-1 rounded-md hover:bg-gray-50">2</button>
            <button className="p-2 rounded-md border hover:bg-gray-50">
              <ChevronRight className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
