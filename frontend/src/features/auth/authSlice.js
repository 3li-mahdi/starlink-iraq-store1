import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import * as authApi from "../../api/authApi";
import { setAccessToken } from "../../api/axiosClient";

/**
 * تسجّل مستخدماً جديداً وتخزّن التوكن وبيانات المستخدم بمتجر Redux.
 */
export const registerUser = createAsyncThunk("auth/register", async (payload, { rejectWithValue }) => {
  try {
    const data = await authApi.register(payload);
    setAccessToken(data.accessToken);
    return data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.error || "فشل التسجيل");
  }
});

/**
 * تسجّل دخول مستخدم موجود وتخزّن التوكن وبيانات المستخدم بمتجر Redux.
 */
export const loginUser = createAsyncThunk("auth/login", async (payload, { rejectWithValue }) => {
  try {
    const data = await authApi.login(payload);
    setAccessToken(data.accessToken);
    return data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.error || "فشل تسجيل الدخول");
  }
});

/**
 * تسجّل خروج المستخدم الحالي وتمسح بيانات الجلسة من المتجر.
 */
export const logoutUser = createAsyncThunk("auth/logout", async () => {
  try {
    await authApi.logoutRequest();
  } catch {
    // نتجاهل خطأ الشبكة هنا: يجب أن يخرج المستخدم محلياً في كل الأحوال
  }
});

const initialState = {
  user: null,
  accessToken: null,
  status: "idle", // idle | loading | succeeded | failed
  error: null,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    logout(state) {
      state.user = null;
      state.accessToken = null;
      setAccessToken(null);
    },
    setSessionFromRefresh(state, action) {
      state.accessToken = action.payload.accessToken;
      state.user = action.payload.user;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(registerUser.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })
      .addCase(registerUser.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.user = action.payload.user;
        state.accessToken = action.payload.accessToken;
      })
      .addCase(registerUser.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload;
      })
      .addCase(loginUser.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.user = action.payload.user;
        state.accessToken = action.payload.accessToken;
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload;
      })
      .addCase(logoutUser.fulfilled, (state) => {
        state.user = null;
        state.accessToken = null;
        setAccessToken(null);
      });
  },
});

export const { logout, setSessionFromRefresh } = authSlice.actions;
export default authSlice.reducer;
