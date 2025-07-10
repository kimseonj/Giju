import Link from "next/link";
import {
  Package,
  Users,
  MessageSquare,
  Star,
  TrendingUp,
  ShoppingCart,
} from "lucide-react";

export default function AdminDashboard() {
  // 대시보드 통계 데이터 (실제로는 API에서 가져올 것)
  const stats = [
    {
      title: "총 상품",
      value: "124",
      change: "+12%",
      trend: "up",
      icon: <Package className="w-8 h-8 text-blue-500" />,
      href: "/admin/products",
    },
    {
      title: "총 회원",
      value: "3,521",
      change: "+5%",
      trend: "up",
      icon: <Users className="w-8 h-8 text-green-500" />,
      href: "/admin/users",
    },
    {
      title: "미답변 Q&A",
      value: "8",
      change: "-2",
      trend: "down",
      icon: <MessageSquare className="w-8 h-8 text-yellow-500" />,
      href: "/admin/qna",
    },
    {
      title: "신규 리뷰",
      value: "32",
      change: "+8",
      trend: "up",
      icon: <Star className="w-8 h-8 text-purple-500" />,
      href: "/admin/reviews",
    },
  ];

  // 최근 주문 데이터 (실제로는 API에서 가져올 것)
  const recentOrders = [
    {
      id: "ORD-1234",
      customer: "김민수",
      date: "2025-05-11",
      amount: "89,000원",
      status: "배송완료",
    },
    {
      id: "ORD-1233",
      customer: "이지은",
      date: "2025-05-10",
      amount: "145,000원",
      status: "배송중",
    },
    {
      id: "ORD-1232",
      customer: "박준호",
      date: "2025-05-10",
      amount: "52,000원",
      status: "결제완료",
    },
    {
      id: "ORD-1231",
      customer: "최유진",
      date: "2025-05-09",
      amount: "78,000원",
      status: "배송중",
    },
    {
      id: "ORD-1230",
      customer: "정승환",
      date: "2025-05-09",
      amount: "124,000원",
      status: "배송완료",
    },
  ];

  // 인기 상품 데이터 (실제로는 API에서 가져올 것)
  const popularProducts = [
    { id: 1, name: "경기도 막걸리", sales: 128, stock: 45, category: "막걸리" },
    { id: 2, name: "전통 동동주", sales: 95, stock: 32, category: "동동주" },
    { id: 3, name: "제주 오메기술", sales: 87, stock: 18, category: "약주" },
    { id: 4, name: "안동 소주", sales: 76, stock: 23, category: "소주" },
    { id: 5, name: "복분자주", sales: 65, stock: 41, category: "과실주" },
  ];

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-2xl font-bold mb-2">관리자 대시보드</h1>
        <p className="text-gray-500">
          기주 쇼핑몰의 주요 통계와 활동을 확인하세요.
        </p>
      </div>

      {/* 통계 카드 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {stats.map((stat, index) => (
          <Link
            href={stat.href}
            key={index}
            className="bg-white rounded-lg shadow p-6 hover:shadow-md transition-shadow"
          >
            <div className="flex justify-between items-start">
              <div>
                <p className="text-gray-500 text-sm">{stat.title}</p>
                <h3 className="text-2xl font-bold mt-1">{stat.value}</h3>
                <p
                  className={`text-sm mt-2 ${
                    stat.trend === "up" ? "text-green-500" : "text-red-500"
                  }`}
                >
                  {stat.change} {stat.trend === "up" ? "↑" : "↓"}
                </p>
              </div>
              <div className="bg-gray-50 p-3 rounded-lg">{stat.icon}</div>
            </div>
          </Link>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        {/* 최근 주문 */}
        <div className="bg-white rounded-lg shadow">
          <div className="p-6 border-b flex justify-between items-center">
            <div>
              <h2 className="text-lg font-bold">최근 주문</h2>
              <p className="text-gray-500 text-sm">최근 5개 주문 내역</p>
            </div>
            <ShoppingCart className="w-5 h-5 text-gray-400" />
          </div>
          <div className="p-6">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="text-left text-gray-500 text-sm border-b">
                    <th className="pb-3 font-medium">주문번호</th>
                    <th className="pb-3 font-medium">고객명</th>
                    <th className="pb-3 font-medium">날짜</th>
                    <th className="pb-3 font-medium">금액</th>
                    <th className="pb-3 font-medium">상태</th>
                  </tr>
                </thead>
                <tbody>
                  {recentOrders.map((order) => (
                    <tr key={order.id} className="border-b last:border-b-0">
                      <td className="py-3 text-sm">{order.id}</td>
                      <td className="py-3 text-sm">{order.customer}</td>
                      <td className="py-3 text-sm">{order.date}</td>
                      <td className="py-3 text-sm">{order.amount}</td>
                      <td className="py-3 text-sm">
                        <span
                          className={`px-2 py-1 rounded-full text-xs ${
                            order.status === "배송완료"
                              ? "bg-green-100 text-green-800"
                              : order.status === "배송중"
                              ? "bg-blue-100 text-blue-800"
                              : "bg-yellow-100 text-yellow-800"
                          }`}
                        >
                          {order.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="mt-4 text-center">
              <Link
                href="/admin/orders"
                className="text-sm text-orange-600 hover:text-orange-700"
              >
                모든 주문 보기 →
              </Link>
            </div>
          </div>
        </div>

        {/* 인기 상품 */}
        <div className="bg-white rounded-lg shadow">
          <div className="p-6 border-b flex justify-between items-center">
            <div>
              <h2 className="text-lg font-bold">인기 상품</h2>
              <p className="text-gray-500 text-sm">판매량 기준 상위 5개 상품</p>
            </div>
            <TrendingUp className="w-5 h-5 text-gray-400" />
          </div>
          <div className="p-6">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="text-left text-gray-500 text-sm border-b">
                    <th className="pb-3 font-medium">상품명</th>
                    <th className="pb-3 font-medium">판매량</th>
                    <th className="pb-3 font-medium">재고</th>
                    <th className="pb-3 font-medium">카테고리</th>
                  </tr>
                </thead>
                <tbody>
                  {popularProducts.map((product) => (
                    <tr key={product.id} className="border-b last:border-b-0">
                      <td className="py-3 text-sm font-medium">
                        {product.name}
                      </td>
                      <td className="py-3 text-sm">{product.sales}개</td>
                      <td className="py-3 text-sm">{product.stock}개</td>
                      <td className="py-3 text-sm">
                        <span className="px-2 py-1 bg-gray-100 rounded-full text-xs">
                          {product.category}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="mt-4 text-center">
              <Link
                href="/admin/products"
                className="text-sm text-orange-600 hover:text-orange-700"
              >
                모든 상품 보기 →
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
