import { createSlice, nanoid } from "@reduxjs/toolkit";

const initialState = {
  toasts: [],
  isCartDrawerOpen: false,
  isAuthModalOpen: false,
  /** الوجهة المطلوب الانتقال لها تلقائياً بعد نجاح تسجيل الدخول من داخل المودال (مثلاً "/checkout"). */
  authModalRedirectTo: null,
};

const uiSlice = createSlice({
  name: "ui",
  initialState,
  reducers: {
    showToast: {
      reducer(state, action) {
        state.toasts.push(action.payload);
      },
      prepare(message, type = "info") {
        return { payload: { id: nanoid(), message, type } };
      },
    },
    dismissToast(state, action) {
      state.toasts = state.toasts.filter((toast) => toast.id !== action.payload);
    },
    openCartDrawer(state) {
      state.isCartDrawerOpen = true;
    },
    closeCartDrawer(state) {
      state.isCartDrawerOpen = false;
    },
    openAuthModal(state, action) {
      state.isAuthModalOpen = true;
      state.authModalRedirectTo = action.payload?.redirectTo ?? null;
    },
    closeAuthModal(state) {
      state.isAuthModalOpen = false;
      state.authModalRedirectTo = null;
    },
  },
});

export const {
  showToast,
  dismissToast,
  openCartDrawer,
  closeCartDrawer,
  openAuthModal,
  closeAuthModal,
} = uiSlice.actions;
export default uiSlice.reducer;
