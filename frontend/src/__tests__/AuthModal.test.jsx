import { act, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { configureStore } from "@reduxjs/toolkit";
import { Provider } from "react-redux";
import { MemoryRouter } from "react-router-dom";
import authReducer from "../features/auth/authSlice";
import cartReducer from "../features/cart/cartSlice";
import wishlistReducer from "../features/wishlist/wishlistSlice";
import uiReducer, { openAuthModal } from "../features/ui/uiSlice";
import AuthModal from "../components/AuthModal";
import * as authApi from "../api/authApi";

jest.mock("../api/authApi");
jest.mock("../api/cartApi", () => ({ fetchCart: jest.fn(() => Promise.resolve({ id: 1, items: [], totalAmount: 0 })) }));

function renderWithProviders() {
  const store = configureStore({
    reducer: { auth: authReducer, cart: cartReducer, wishlist: wishlistReducer, ui: uiReducer },
  });
  render(
    <Provider store={store}>
      <MemoryRouter>
        <AuthModal />
      </MemoryRouter>
    </Provider>
  );
  return store;
}

describe("AuthModal", () => {
  test("لا يظهر المودال إلا بعد فتحه", () => {
    renderWithProviders();
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  test("يعرض تبويب تسجيل الدخول افتراضياً، ويتبدّل لتبويب حساب جديد عند الضغط عليه", async () => {
    const store = renderWithProviders();
    act(() => store.dispatch(openAuthModal({ redirectTo: "/checkout" })));

    await screen.findByRole("dialog");
    expect(screen.getByLabelText("البريد الإلكتروني")).toBeInTheDocument();

    await userEvent.click(screen.getByRole("button", { name: "حساب جديد" }));
    expect(screen.getByLabelText("الاسم الكامل")).toBeInTheDocument();
  });

  test("نجاح تسجيل الدخول يغلق المودال ويحفظ بيانات المستخدم بالمتجر", async () => {
    authApi.login.mockResolvedValue({
      accessToken: "fake-token",
      user: { id: 1, fullName: "أحمد علي", email: "ahmed@example.com", role: "CUSTOMER" },
    });
    const store = renderWithProviders();
    act(() => store.dispatch(openAuthModal({ redirectTo: "/checkout" })));

    await screen.findByRole("dialog");
    await userEvent.type(screen.getByLabelText("البريد الإلكتروني"), "ahmed@example.com");
    await userEvent.type(screen.getByLabelText("كلمة المرور"), "Password123");
    await userEvent.click(screen.getByRole("button", { name: "تسجيل الدخول" }));

    await waitFor(() => {
      expect(store.getState().auth.user?.email).toBe("ahmed@example.com");
      expect(store.getState().ui.isAuthModalOpen).toBe(false);
    });
  });
});
