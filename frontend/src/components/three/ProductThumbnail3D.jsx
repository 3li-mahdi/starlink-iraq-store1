import { useRef } from "react";
import { useFrame } from "@react-three/fiber";
import LazyCanvas from "./LazyCanvas";
import ProductGeometry from "./ProductGeometry";

/**
 * يدوّر شكل المنتج الهندسي تلقائياً وبسرعة خفيفة داخل بطاقة المنتج.
 */
function RotatingModel({ product }) {
  const groupRef = useRef(null);

  useFrame((_, delta) => {
    if (groupRef.current) {
      groupRef.current.rotation.y += delta * 0.5;
      groupRef.current.rotation.x += delta * 0.15;
    }
  });

  return <ProductGeometry product={product} groupRef={groupRef} />;
}

/**
 * معاينة ثلاثية الأبعاد مصغّرة تُعرض داخل بطاقة المنتج بشبكة المنتجات، تُحمَّل فقط عند ظهورها بالشاشة.
 * @param {{product: object}} props - بيانات المنتج المطلوب عرض شكله
 */
export default function ProductThumbnail3D({ product }) {
  return (
    <LazyCanvas
      style={{ width: "100%", height: "100%" }}
      camera={{ position: [0, 0, 4.2], fov: 45 }}
      fallback={<div className="skeleton" style={{ width: "100%", height: "100%" }} />}
    >
      <ambientLight intensity={0.6} />
      <RotatingModel product={product} />
    </LazyCanvas>
  );
}
