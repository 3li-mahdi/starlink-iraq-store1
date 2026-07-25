import { useEffect, useRef, useState } from "react";
import { Canvas } from "@react-three/fiber";

/**
 * يؤجّل إنشاء مشهد Three.js حتى يصبح العنصر مرئياً فعلياً بالشاشة (عبر IntersectionObserver)،
 * لتفادي تحميل عشرات المشاهد ثلاثية الأبعاد دفعة واحدة عند وجود منتجات كثيرة بالصفحة.
 *
 * @param {React.ReactNode} children - محتوى المشهد (عناصر Three.js) الذي يُعرض فقط عند الظهور
 * @param {object} canvasProps - أي خصائص إضافية تُمرَّر مباشرة لعنصر Canvas
 */
export default function LazyCanvas({ children, style, fallback = null, ...canvasProps }) {
  const containerRef = useRef(null);
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    if (isVisible || !containerRef.current) {
      return undefined;
    }
    if (typeof IntersectionObserver === "undefined") {
      setIsVisible(true);
      return undefined;
    }
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting) {
          setIsVisible(true);
          observer.disconnect();
        }
      },
      { rootMargin: "150px" }
    );
    observer.observe(containerRef.current);
    return () => observer.disconnect();
  }, [isVisible]);

  return (
    <div ref={containerRef} style={style}>
      {isVisible ? (
        <Canvas dpr={[1, 1.5]} gl={{ antialias: true, alpha: true }} {...canvasProps}>
          {children}
        </Canvas>
      ) : (
        fallback
      )}
    </div>
  );
}
