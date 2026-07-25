import { Suspense } from "react";
import { Canvas } from "@react-three/fiber";
import { OrbitControls } from "@react-three/drei";
import ProductGeometry from "./ProductGeometry";

/**
 * معاينة ثلاثية الأبعاد كبيرة قابلة للتدوير 360° بالسحب (drag)، تُستخدم داخل مودال تفاصيل المنتج.
 * @param {{product: object}} props - بيانات المنتج المطلوب عرض شكله
 */
export default function Product3DViewer({ product }) {
  return (
    <Canvas camera={{ position: [0, 0, 4.5], fov: 45 }} gl={{ antialias: true, alpha: true }}>
      <ambientLight intensity={0.7} />
      <Suspense fallback={null}>
        <ProductGeometry product={product} />
      </Suspense>
      <OrbitControls
        enableZoom={false}
        enablePan={false}
        autoRotate
        autoRotateSpeed={1.2}
        rotateSpeed={0.6}
      />
    </Canvas>
  );
}
