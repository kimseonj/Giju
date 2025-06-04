import { getCategories } from "@/lib/category";
import Link from "next/link";
import Image from "next/image";

export const dynamic = "force-dynamic";

export default async function CategoriesPage() {
  const categories = (await getCategories()) as any[];

  // 원하는 순서대로 섹션을 만듦
  const sectionOrder = ["탁주", "청주", "증류주", "약주", "과실주", "기타"];

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">카테고리</h1>
      {/* 전체 섹션 */}
      <div className="mb-10">
        {/* <h2 className="text-xl font-semibold mb-2">전체</h2> */}
        <ul className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {categories.map((cat) => (
            <li key={cat.id}>
              <Link
                href={`/category/${cat.id}`}
                className="block relative w-full h-[40px] text-center text-lg text-white font-inter"
              >
                <Image
                  className="absolute left-0 top-0 w-full h-full z-0"
                  src="/box.svg"
                  alt="박스 배경"
                  width={400}
                  height={40}
                  style={{ objectFit: "contain" }}
                />
                <b className="absolute top-[50%] left-[50%] transform -translate-x-1/2 -translate-y-1/2 z-20">
                  {cat.name}
                </b>
              </Link>
            </li>
          ))}
        </ul>
      </div>
      {/* 개별 섹션 */}
      {/* <div className="space-y-8">
        {sectionOrder.map((section) => {
          const cats = categories.filter((cat) => cat.name === section);
          if (cats.length === 0) return null;
          return (
            <div key={section}>
              <h2 className="text-xl font-semibold mb-2">{section}</h2>
              <ul className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {cats.map((cat) => (
                  <li key={cat.id}>
                    <Link
                      href={`/category/${cat.id}`}
                      className="block border rounded p-4 text-lg font-medium bg-white shadow hover:bg-orange-50 transition"
                    >
                      {cat.name}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          );
        })}
      </div> */}
    </div>
  );
}
