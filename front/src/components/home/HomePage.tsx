"use client";

import KoreaMap from "../map/KoreaMap";
import Image from "next/image";

export default function HomePage() {
  return (
    <div
      className="w-full min-h-screen bg-cover bg-center bg-no-repeat"
      style={{ backgroundImage: "url('/test.svg')" }}
    >
      {/* Hero 영역 */}
      <section className="w-full flex flex-col items-center justify-center min-h-[600px] pt-24">
        <div className="w-full max-w-[520px] md:max-w-[700px] mx-auto">
          <KoreaMap />
        </div>
      </section>

      {/* 뱃지 랭킹 상단 타이틀 영역 */}
      <section className="w-full flex justify-center py-8">
        <div className="w-full max-w-[1200px] relative h-[107px] text-left text-[21px] text-darkslategray font-inter">
          {/* 블러 배경: 중앙 콘텐츠만 덮도록 */}
          {/* <div className="absolute left-[18%] top-0 w-[64%] h-full z-0">
            <Image
              src="/blur-bg-1.svg"
              alt="블러 배경"
              fill
              className="object-cover opacity-80"
            />
          </div> */}
          {/* 상단 구분선 (5px) */}
          <div
            className="absolute top-0 left-0 w-full z-10"
            style={{ borderTop: "5px solid #0E2E40" }}
          />
          {/* 하단 구분선 (0.75px) */}
          <div
            className="absolute bottom-0 left-0 w-full z-10"
            style={{ borderTop: "0.75px solid #0E2E40" }}
          />
          {/* 타이틀 텍스트 */}
          <div className="absolute font-pretendard text-[21px] text-main top-[38.71%] left-[14.02%] z-20">
            각 도 주류별 구매뱃지 보유자 랭킹
          </div>
          {/* 왼쪽 장식 - 피그마 기준 최대한 동일하게 조정 */}
          {/* <Image
            className="absolute z-20"
            style={{
              top: "38px",
              left: "18px",
              maxWidth: "none",
              maxHeight: "none",
            }}
            width={80}
            height={40}
            alt="뱃지 랭킹 타이틀 장식"
            src="/뱃지 랭킹_타이틀.svg"
          /> */}
          <div
            className="absolute z-20 font-jj font-extrabold text-[28px] text-main"
            style={{
              top: "38px",
              left: "18px",
              letterSpacing: "0.02em",
            }}
          >
            지역별 랭킹
          </div>
        </div>
      </section>

      {/* 뱃지 랭킹 지도 영역
      <section className="w-full flex justify-center py-8">
        <div className="w-full max-w-[1200px] relative h-[513.1px]">
          <Image
            className="absolute"
            style={{
              top: "0%",
              left: "0%",
              width: "90.77%",
              height: "100%",
              right: "9.23%",
              maxWidth: "100%",
              maxHeight: "100%",
            }}
            src="/map.svg"
            alt="지도"
            width={1061}
            height={513}
          />
        </div>
      </section>

      {/* 전라남도 SVG 영역 
      <section className="w-full flex justify-center py-8">
        <div className="w-full max-w-[1200px] relative h-[134px]">
          <Image
            className="absolute"
            style={{
              top: "0%",
              left: "0%",
              width: "100%",
              height: "100%",
              maxWidth: "100%",
              maxHeight: "100%",
            }}
            src="/전라남도.svg"
            alt="전라남도"
            width={400}
            height={134}
          />
        </div>
      </section>

      {/* 지역명 SVG 영역 *
      <section className="w-full flex justify-center py-4">
        <div className="w-full max-w-[1200px] relative h-[29.8px]">
          <Image
            className="absolute"
            style={{
              top: "0%",
              left: "0%",
              width: "100%",
              height: "100%",
              maxWidth: "100%",
              maxHeight: "100%",
            }}
            src="/지역명.svg"
            alt="전라남도 지역명"
            width={200}
            height={29.8}
          />
        </div>
      </section> */}

      {/* 술 이름/주종 박스 전체 구현 (구분선 제거) */}
      <section className="w-full flex flex-col items-center gap-2 py-2">
        {/* 대통대잎술 */}
        <div className="w-full max-w-[400px] relative h-10 text-left text-lg text-white font-inter">
          <Image
            className="absolute left-0 top-0 w-full h-full z-0"
            src="/search-bg.svg"
            alt="박스 배경"
            fill
            style={{ objectFit: "cover" }}
          />
          <b className="absolute top-[19.65%] left-[10.07%] z-20">대통대잎술</b>
          <div className="absolute top-[25.7%] left-[80.96%] text-base text-lightyellow text-right z-20">
            약주
          </div>
        </div>
        {/* 대통대잎술 */}
        <div className="w-full max-w-[400px] relative h-10 text-left text-lg text-white font-inter">
          <Image
            className="absolute left-0 top-0 w-full h-full z-0"
            src="/search-bg.svg"
            alt="박스 배경"
            fill
            style={{ objectFit: "cover" }}
          />
          <b className="absolute top-[19.65%] left-[10.07%] z-20">대통대잎술</b>
          <div className="absolute top-[25.7%] left-[80.96%] text-base text-lightyellow text-right z-20">
            약주
          </div>
        </div>
        {/* 대통대잎술 */}
        <div className="w-full max-w-[400px] relative h-10 text-left text-lg text-white font-inter">
          <Image
            className="absolute left-0 top-0 w-full h-full z-0"
            src="/search-bg.svg"
            alt="박스 배경"
            fill
            style={{ objectFit: "cover" }}
          />
          <b className="absolute top-[19.65%] left-[10.07%] z-20">대통대잎술</b>
          <div className="absolute top-[25.7%] left-[80.96%] text-base text-lightyellow text-right z-20">
            약주
          </div>
        </div>
        {/* 대통대잎술 */}
        <div className="w-full max-w-[400px] relative h-10 text-left text-lg text-white font-inter">
          <Image
            className="absolute left-0 top-0 w-full h-full z-0"
            src="/search-bg.svg"
            alt="박스 배경"
            fill
            style={{ objectFit: "cover" }}
          />
          <b className="absolute top-[19.65%] left-[10.07%] z-20">대통대잎술</b>
          <div className="absolute top-[25.7%] left-[80.96%] text-base text-lightyellow text-right z-20">
            약주
          </div>
        </div>
        {/* 대통대잎술 */}
        <div className="w-full max-w-[400px] relative h-10 text-left text-lg text-white font-inter">
          <Image
            className="absolute left-0 top-0 w-full h-full z-0"
            src="/search-bg.svg"
            alt="박스 배경"
            fill
            style={{ objectFit: "cover" }}
          />
          <b className="absolute top-[19.65%] left-[10.07%] z-20">대통대잎술</b>
          <div className="absolute top-[25.7%] left-[80.96%] text-base text-lightyellow text-right z-20">
            약주
          </div>
        </div>
        {/* 대통대잎술 */}
        <div className="w-full max-w-[400px] relative h-10 text-left text-lg text-white font-inter">
          <Image
            className="absolute left-0 top-0 w-full h-full z-0"
            src="/search-bg.svg"
            alt="박스 배경"
            fill
            style={{ objectFit: "cover" }}
          />
          <b className="absolute top-[19.65%] left-[10.07%] z-20">대통대잎술</b>
          <div className="absolute top-[25.7%] left-[80.96%] text-base text-lightyellow text-right z-20">
            약주
          </div>
        </div>
        {/* 대통대잎술 */}
        <div className="w-full max-w-[400px] relative h-10 text-left text-lg text-white font-inter">
          <Image
            className="absolute left-0 top-0 w-full h-full z-0"
            src="/search-bg.svg"
            alt="박스 배경"
            fill
            style={{ objectFit: "cover" }}
          />
          <b className="absolute top-[19.65%] left-[10.07%] z-20">대통대잎술</b>
          <div className="absolute top-[25.7%] left-[80.96%] text-base text-lightyellow text-right z-20">
            약주
          </div>
        </div>
        {/* 대통대잎술 */}
        <div className="w-full max-w-[400px] relative h-10 text-left text-lg text-white font-inter">
          <Image
            className="absolute left-0 top-0 w-full h-full z-0"
            src="/search-bg.svg"
            alt="박스 배경"
            fill
            style={{ objectFit: "cover" }}
          />
          <b className="absolute top-[19.65%] left-[10.07%] z-20">대통대잎술</b>
          <div className="absolute top-[25.7%] left-[80.96%] text-base text-lightyellow text-right z-20">
            약주
          </div>
        </div>
        {/* 매실 막걸리 */}
        <div className="w-full max-w-[400px] relative h-10 text-left text-lg text-white font-inter">
          <Image
            className="absolute left-0 top-0 w-full h-full z-0"
            src="/search-bg.svg"
            alt="박스 배경"
            fill
            style={{ objectFit: "cover" }}
          />
          <b className="absolute top-[19.65%] left-[10.07%] z-20">
            매실 막걸리
          </b>
          <div className="absolute top-[25.7%] left-[68.07%] text-base text-lightyellow text-right z-20">
            살균탁주
          </div>
        </div>
        {/* 백운 복분자주 */}
        <div className="w-full max-w-[400px] relative h-10 text-left text-lg text-white font-inter">
          <Image
            className="absolute left-0 top-0 w-full h-full z-0"
            src="/search-bg.svg"
            alt="박스 배경"
            fill
            style={{ objectFit: "cover" }}
          />
          <b className="absolute top-[19.65%] left-[10.07%] z-20">
            백운 복분자주
          </b>
          <div className="absolute top-[25.7%] left-[68.07%] text-base text-lightyellow text-right z-20">
            기타주류
          </div>
        </div>
        {/* 명품 진도홍주 */}
        <div className="w-full max-w-[400px] relative h-10 text-left text-lg text-white font-inter">
          <Image
            className="absolute left-0 top-0 w-full h-full z-0"
            src="/search-bg.svg"
            alt="박스 배경"
            fill
            style={{ objectFit: "cover" }}
          />
          <b className="absolute top-[19.65%] left-[8.61%] z-20">
            명품 진도홍주
          </b>
          <div className="absolute top-[26.22%] left-[64.45%] text-base text-lightyellow text-right z-20">
            일반증류주
          </div>
        </div>
        {/* 백운 복분자 와인 */}
        <div className="w-full max-w-[400px] relative h-10 text-left text-lg text-white font-inter">
          <Image
            className="absolute left-0 top-0 w-full h-full z-0"
            src="/search-bg.svg"
            alt="박스 배경"
            fill
            style={{ objectFit: "cover" }}
          />
          <b className="absolute top-[19.65%] left-[8.61%] z-20">
            백운 복분자 와인
          </b>
          <div className="absolute top-[25.7%] left-[69.52%] text-base text-lightyellow text-right z-20">
            기타주류
          </div>
        </div>
        {/* 병영소주 */}
        <div className="w-full max-w-[400px] relative h-10 text-left text-lg text-white font-inter">
          <Image
            className="absolute left-0 top-0 w-full h-full z-0"
            src="/search-bg.svg"
            alt="박스 배경"
            fill
            style={{ objectFit: "cover" }}
          />
          <b className="absolute top-[19.65%] left-[10.07%] z-20">병영소주</b>
          <div className="absolute top-[25.7%] left-[61.78%] text-base text-lightyellow text-right z-20">
            증류식 소주
          </div>
        </div>
      </section>
    </div>
  );
}
