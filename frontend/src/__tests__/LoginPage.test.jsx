import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { configureStore } from "@reduxjs/toolkit";
import { Provider } from "react-redux";
import { MemoryRouter } from "react-router-dom";
import authReducer from "../features/auth/authSlice";
import cartReducer from "../features/cart/cartSlice";
import wishlistReducer from "../features/wishlist/wishlistSlice";
import uiReducer from "../features/ui/uiSlice";
import LoginPage from "../pages/LoginPage";
import * as authApi from "../api/authApi";

jest.mock("../api/authApi");
jest.mock("../api/cartApi", () => ({ fetchCart: jest.fn(() => Promise.resolve({ id: 1, items: [], totalAmount: 0 })) }));

function renderWithProviders(ui) {
  const store = configureStore({
    reducer: { auth: authReducer, cart: cartReducer, wishlist: wishlistReducer, ui: uiReducer },
  });
  render(
    <Provider store={store}>
      <MemoryRouter>{ui}</MemoryRouter>
    </Provider>
  );
  return store;
}

describe("LoginPage", () => {
  test("يعرض خطأ عند فشل تسجيل الدخول من السيرفر", async () => {
    authApi.login.mockRejectedValue({ response: { data: { error: "بيانات الدخول غير صحيحة" } } });
    const store = renderWithProviders(<LoginPage />);

    await userEvent.type(screen.getByLabelText("البريد الإلكتروني"), "wrong@example.com");
    await userEvent.type(screen.getByLabelText("كلمة المرور"), "WrongPassword1");
    await userEvent.click(screen.getByRole("button", { name: "تسجيل الدخول" }));

    await waitFor(() => expect(screen.getByText("بيانات الدخول غير صحيحة")).toBeInTheDocument());
    expect(store.getState().auth.user).toBeNull();
  });

  test("يسجّل بيانات المستخدم بالمتجر عند نجاح تسجيل الدخول", async () => {
    authApi.login.mockResolvedValue({
      accessToken: "fake-token",
      user: { id: 1, fullName: "أحمد علي", email: "ahmed@example.com", role: "CUSTOMER" },
    });
    const store = renderWithProviders(<LoginPage />);

    await userEvent.type(screen.getByLabelText("البريد الإلكتروني"), "ahmed@example.com");
    await userEvent.type(screen.getByLabelText("كلمة المرور"), "Password123");
    await userEvent.click(screen.getByRole("button", { name: "تسجيل الدخول" }));

    await waitFor(() => expect(store.getState().auth.user?.email).toBe("ahmed@example.com"));
  });
});
