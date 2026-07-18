# متجر Starlink العراق الإلكتروني

متجر إلكتروني متكامل لبيع منتجات Starlink في العراق (أطباق، راوترات، كيبلات) ومنتجات رقمية
(اشتراكات، أكواد تفعيل، روابط تحميل). Backend منفصل (Spring Boot) وFrontend منفصل (React)،
يتواصلان عبر REST API.

## البنية

```
backend/    Spring Boot 3 + Java 17 + PostgreSQL/MySQL + JWT + Spring Security
frontend/   React 18 + Vite + Redux Toolkit + React Router
```

## تشغيل الـ Backend

```bash
cd backend
cp .env.example .env   # عبّئ القيم الحقيقية (JWT secret، بيانات قاعدة البيانات...)
export $(cat .env | xargs)
mvn spring-boot:run
```

- الـ API يعمل افتراضياً على `http://localhost:8080`
- توثيق Swagger: `http://localhost:8080/swagger-ui.html`
- تشغيل الاختبارات: `mvn test`

## تشغيل الـ Frontend

```bash
cd frontend
cp .env.example .env   # عدّل VITE_API_BASE_URL عند الحاجة
npm install
npm run dev
```

- الموقع يعمل افتراضياً على `http://localhost:5173`
- تشغيل الاختبارات: `npm test`
- بناء نسخة الإنتاج: `npm run build`

## ملاحظات أمنية مهمة قبل النشر

- لا تستخدم القيم الافتراضية بملفات `.env.example` بالإنتاج إطلاقاً (خصوصاً `JWT_SECRET` و
  `DB_ENCRYPTION_SECRET_KEY`) - استخدم قيماً عشوائية طويلة فريدة.
- اضبط `CORS_ALLOWED_ORIGINS` على دومين الفرونت إند الفعلي فقط.
- شغّل خلف HTTPS دائماً بالإنتاج (الكوكي الخاص بـ refresh token يتطلب `Secure`).
- اضبط بيانات SMTP الحقيقية بـ `MAIL_*` لتفعيل رسائل تأكيد الطلب والتفعيل.

## خارج النطاق حالياً

- الدفع الحقيقي عبر ZainCash (البنية جاهزة عبر واجهة `PaymentGateway` للإضافة لاحقاً)
- الاستضافة والنشر الفعلي
- اختبارات E2E (Cypress/Playwright)
