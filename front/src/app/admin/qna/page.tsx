"use client";

import { useState } from "react";
import {
  Search,
  Filter,
  MessageSquare,
  ChevronLeft,
  ChevronRight,
  ArrowUpDown,
  MessageSquareText,
  Eye,
} from "lucide-react";

// Q&A 타입 정의
interface QnA {
  id: number;
  productName: string;
  userName: string;
  question: string;
  answer: string | null;
  date: string;
  status: "답변완료" | "미답변" | "비공개";
}

export default function QnAPage() {
  // Q&A 목록 (실제로는 API에서 가져올 것)
  const [qnas, setQnas] = useState<QnA[]>([
    {
      id: 1,
      productName: "경기도 막걸리",
      userName: "김민수",
      question: "유통기한은 얼마나 되나요?",
      answer: "제품 수령 후 냉장 보관 시 약 2주입니다.",
      date: "2025-05-10",
      status: "답변완료",
    },
    {
      id: 2,
      productName: "전통 동동주",
      userName: "이지은",
      question: "알코올 도수가 어떻게 되나요?",
      answer: "6-8% 정도입니다.",
      date: "2025-05-09",
      status: "답변완료",
    },
    {
      id: 3,
      productName: "제주 오메기술",
      userName: "박준호",
      question: "선물용으로 포장 가능한가요?",
      answer: null,
      date: "2025-05-08",
      status: "미답변",
    },
    {
      id: 4,
      productName: "안동 소주",
      userName: "최유진",
      question: "배송은 얼마나 걸리나요?",
      answer: null,
      date: "2025-05-07",
      status: "미답변",
    },
    {
      id: 5,
      productName: "복분자주",
      userName: "정승환",
      question: "미성년자도 구매 가능한가요?",
      answer: "주류 제품은 성인만 구매 가능합니다.",
      date: "2025-05-06",
      status: "답변완료",
    },
    {
      id: 6,
      productName: "매실주",
      userName: "강지원",
      question: "매실 원산지가 어디인가요?",
      answer: null,
      date: "2025-05-05",
      status: "미답변",
    },
    {
      id: 7,
      productName: "청주",
      userName: "윤서연",
      question: "단맛이 강한가요?",
      answer: null,
      date: "2025-05-04",
      status: "미답변",
    },
  ]);

  const [searchTerm, setSearchTerm] = useState("");
  const [selectedStatus, setSelectedStatus] = useState<string | null>(null);

  // 답변 모달 상태
  const [isAnswerModalOpen, setIsAnswerModalOpen] = useState(false);
  const [selectedQnA, setSelectedQnA] = useState<QnA | null>(null);
  const [answerText, setAnswerText] = useState("");

  // 답변 모달 열기
  const openAnswerModal = (qna: QnA) => {
    setSelectedQnA(qna);
    setAnswerText(qna.answer || "");
    setIsAnswerModalOpen(true);
  };

  // 답변 제출 핸들러
  const handleSubmitAnswer = () => {
    if (!selectedQnA) return;

    setQnas(
      qnas.map((qna) =>
        qna.id === selectedQnA.id
          ? { ...qna, answer: answerText, status: "답변완료" as const }
          : qna
      )
    );

    setIsAnswerModalOpen(false);
    setSelectedQnA(null);
    setAnswerText("");
  };

  // 필터링된 Q&A 목록
  const filteredQnAs = qnas.filter((qna) => {
    const matchesSearch =
      qna.productName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      qna.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      qna.question.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = selectedStatus ? qna.status === selectedStatus : true;
    return matchesSearch && matchesStatus;
  });

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-2xl font-bold mb-2">Q&A 관리</h1>
        <p className="text-gray-500">고객 문의에 답변하고 관리하세요.</p>
      </div>

      {/* 검색 및 필터 */}
      <div className="bg-white rounded-lg shadow mb-6">
        <div className="p-6 flex flex-col md:flex-row gap-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
            <input
              type="text"
              placeholder="상품명, 작성자, 질문 검색..."
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
              <option value="답변완료">답변완료</option>
              <option value="미답변">미답변</option>
              <option value="비공개">비공개</option>
            </select>
          </div>
        </div>
      </div>

      {/* Q&A 목록 테이블 */}
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
                <th className="px-6 py-3 font-medium">질문</th>
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
              {filteredQnAs.map((qna) => (
                <tr key={qna.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-sm">{qna.id}</td>
                  <td className="px-6 py-4">
                    <div className="font-medium">{qna.productName}</div>
                  </td>
                  <td className="px-6 py-4 text-sm">{qna.userName}</td>
                  <td className="px-6 py-4">
                    <div className="truncate max-w-xs">{qna.question}</div>
                  </td>
                  <td className="px-6 py-4 text-sm">{qna.date}</td>
                  <td className="px-6 py-4">
                    <span
                      className={`px-2 py-1 rounded-full text-xs ${
                        qna.status === "답변완료"
                          ? "bg-green-100 text-green-800"
                          : qna.status === "미답변"
                          ? "bg-yellow-100 text-yellow-800"
                          : "bg-gray-100 text-gray-800"
                      }`}
                    >
                      {qna.status}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex space-x-2">
                      <button
                        onClick={() => openAnswerModal(qna)}
                        className="p-1 text-blue-600 hover:text-blue-800 rounded-full hover:bg-blue-100"
                        title={
                          qna.status === "답변완료" ? "답변 수정" : "답변 작성"
                        }
                      >
                        {qna.status === "답변완료" ? (
                          <MessageSquareText className="w-5 h-5" />
                        ) : (
                          <MessageSquare className="w-5 h-5" />
                        )}
                      </button>
                      <button
                        className="p-1 text-gray-600 hover:text-gray-800 rounded-full hover:bg-gray-100"
                        title="상세 보기"
                      >
                        <Eye className="w-5 h-5" />
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
            총 <span className="font-medium">{filteredQnAs.length}</span>개 문의
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

      {/* 답변 모달 */}
      {isAnswerModalOpen && selectedQnA && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-lg w-full max-w-2xl mx-4">
            <div className="p-6 border-b">
              <h3 className="text-lg font-bold">Q&A 답변</h3>
            </div>
            <div className="p-6">
              <div className="mb-6">
                <h4 className="text-sm font-medium text-gray-700 mb-2">상품</h4>
                <p>{selectedQnA.productName}</p>
              </div>
              <div className="mb-6">
                <h4 className="text-sm font-medium text-gray-700 mb-2">질문</h4>
                <p className="bg-gray-50 p-3 rounded-md">
                  {selectedQnA.question}
                </p>
              </div>
              <div>
                <h4 className="text-sm font-medium text-gray-700 mb-2">답변</h4>
                <textarea
                  rows={5}
                  className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  placeholder="답변을 입력하세요..."
                  value={answerText}
                  onChange={(e) => setAnswerText(e.target.value)}
                ></textarea>
              </div>
            </div>
            <div className="p-6 border-t flex justify-end space-x-3">
              <button
                onClick={() => setIsAnswerModalOpen(false)}
                className="px-4 py-2 border rounded-md text-gray-700 hover:bg-gray-50"
              >
                취소
              </button>
              <button
                onClick={handleSubmitAnswer}
                className="px-4 py-2 bg-orange-600 text-white rounded-md hover:bg-orange-700"
                disabled={!answerText.trim()}
              >
                답변 등록
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
