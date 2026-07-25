import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import axios from "axios";
import { setSessionFromRefresh } from "../features/auth/authSlice";
import { setAccessToken } from "../api/axiosClient";
import { loadCart } from "../features/cart/cartSlice";
import { loadWishlist } from "../features/wishlist/wishlistSlice";
import { getOrCreateGuestSessionId } from "../utils/guestSession";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

/**
 * تحاول استعادة جلسة المستخدم عند فتح التطبيق لأول مرة عبر refresh token المخزَّن بالكوكي،
 * وتضمن وجود معرّف جلسة زائر للسلة إن لم يكن هناك مستخدم مسجَّل دخوله.
 * @returns {boolean} isReady - true بعد اكتمال محاولة استعادة الجلسة الأولية (تُستخدم لإخفاء شاشة التحميل)
 */
export default function useSessionBootstrap() {
  const dispatch = useDispatch();
  const user = useSelector((state) => state.auth.user);
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    getOrCreateGuestSessionId();

    axios
      .post(`${BASE_URL}/auth/refresh`, {}, { withCredentials: true })
      .then((response) => {
        setAccessToken(response.data.accessToken);
        dispatch(setSessionFromRefresh(response.data));
      })
      .catch(() => {
        // لا توجد جلسة سابقة صالحة، يبقى المستخدم زائراً
      })
      .finally(() => {
        dispatch(loadCart());
        setIsReady(true);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (user) {
      dispatch(loadWishlist());
      dispatch(loadCart());
    }
  }, [user, dispatch]);

  return isReady;
}
