/**
 * تُرجع معرّف جلسة الزائر الحالي من التخزين المحلي، أو تُنشئ واحداً جديداً إذا لم يوجد.
 * يُستخدم لربط سلة تسوق بزائر غير مسجَّل دخوله.
 * @returns {string} معرّف جلسة الزائر
 */
export function getOrCreateGuestSessionId() {
  let id = localStorage.getItem("guestSessionId");
  if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem("guestSessionId", id);
  }
  return id;
}

/**
 * تحذف معرّف جلسة الزائر من التخزين المحلي بعد تسجيل الدخول ودمج السلة.
 */
export function clearGuestSessionId() {
  localStorage.removeItem("guestSessionId");
}
