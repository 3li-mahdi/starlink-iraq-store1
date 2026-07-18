import { configureStore } from "@reduxjs/toolkit";
import authReducer from "../features/auth/authSlice";
import cartReducer from "../features/cart/cartSlice";
import wishlistReducer from "../features/wishlist/wishlistSlice";
import uiReducer from "../features/ui/uiSlice";
import { injectStore } from "../api/axiosClient";

export const store = configureStore({
  reducer: {
    auth: authReducer,
    cart: cartReducer,
    wishlist: wishlistReducer,
    ui: uiReducer,
  },
});

// نربط axiosClient بالمتجر بعد إنشائه لتفادي الاستيراد الدائري، حتى يقدر
// المتصفح ينفّذ logout تلقائياً عند فشل تجديد التوكن
injectStore(store);
