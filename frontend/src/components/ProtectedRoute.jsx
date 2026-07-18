import { Navigate, useLocation } from "react-router-dom";
import { useSelector } from "react-redux";

/**
 * يمنع الوصول لصفحة معيّنة إلا لمستخدم مسجَّل دخوله، ويحوّله لصفحة الدخول غير ذلك.
 */
export default function ProtectedRoute({ children }) {
  const user = useSelector((state) => state.auth.user);
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  return children;
}
