import type React from "react";
import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/components/common/auth-provider";
import Header from "@/components/common/Header";
import Footer from "@/components/common/Footer";
import Script from "next/script";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "기주 - 전통주 쇼핑몰",
  description: "대한민국 전통주의 아름다움을 전하는 온라인 플랫폼",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  // 쿠키에 있는 refresh 토큰 검사 있으면 access 토큰 재발급 후 스토어에 넣어주기
  // 없으면 스토어에서 access 토큰 검사 -> 없으면 로그인 페이지로 이동
  return (
    <html lang="ko">
      <head>{/* meta, link 등만 이곳에 */}</head>
      <body className={inter.className}>
        {/* Adobe Fonts 스크립트는 Script로 body에 삽입 */}
        <Script id="adobe-fonts" strategy="beforeInteractive">
          {`(function(d) {
            var config = {
              kitId: 'csr0bws',
              scriptTimeout: 3000,
              async: true
            },
            h=d.documentElement,t=setTimeout(function(){h.className=h.className.replace(/\\bwf-loading\\b/g,"")+" wf-inactive";},config.scriptTimeout),
            tk=d.createElement("script"),f=false,s=d.getElementsByTagName("script")[0],a;
            if (!h.className.includes("wf-loading")) h.className+=" wf-loading";
            tk.src='https://use.typekit.net/'+config.kitId+'.js';
            tk.async=true;
            tk.onload=tk.onreadystatechange=function(){
              a=this.readyState;
              if(f||a&&a!="complete"&&a!="loaded")return;
              f=true;clearTimeout(t);
              try{Typekit.load(config)}catch(e){}
            };
            s.parentNode.insertBefore(tk,s)
          })(document);`}
        </Script>
        <AuthProvider>
          <div className="flex min-h-screen flex-col">
            <Header />
            <main className="flex-1">{children}</main>
            <Footer />
          </div>
        </AuthProvider>
      </body>
    </html>
  );
}
