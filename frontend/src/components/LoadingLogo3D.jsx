import { useRef } from "react";
import { Canvas, useFrame } from "@react-three/fiber";

/**
 * شعار دوّار بسيط ثلاثي الأبعاد يُعرض بشاشة التحميل الأولية.
 */
function SpinningLogo() {
  const ref = useRef(null);
  useFrame((_, delta) => {
    if (ref.current) {
      ref.current.rotation.y += delta * 1.4;
      ref.current.rotation.x += delta * 0.6;
    }
  });

  return (
    <mesh ref={ref}>
      <torusKnotGeometry args={[1, 0.32, 120, 16]} />
      <meshBasicMaterial color="#00e5ff" wireframe />
    </mesh>
  );
}

export default function LoadingLogo3D() {
  return (
    <Canvas camera={{ position: [0, 0, 4.5], fov: 45 }} gl={{ antialias: true, alpha: true }}>
      <SpinningLogo />
    </Canvas>
  );
}
