# 🍔 FoodNow — Ứng dụng Đặt Đồ Ăn

> Dự án môn học **Android Development** — Ứng dụng đặt đồ ăn đơn giản trên nền tảng Android.

---

## 📋 Mục lục

- [Giới thiệu](#-giới-thiệu)
- [Tính năng](#-tính-năng)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Cài đặt & Cấu hình](#-cài-đặt--cấu-hình)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Kiến trúc MVVM](#-kiến-trúc-mvvm)
- [Build & Chạy](#-build--chạy)
- [Ảnh chụp màn hình](#-ảnh-chụp-màn-hình)
- [Thành viên nhóm](#-thành-viên-nhóm)

---

## 📖 Giới thiệu

**FoodNow** là ứng dụng Android cho phép người dùng:
- Duyệt danh sách quán ăn và thực đơn
- Thêm món vào giỏ hàng và đặt hàng
- Quản lý đơn hàng, yêu thích và thông tin cá nhân

Ứng dụng được xây dựng theo kiến trúc **MVVM** (Model – View – ViewModel), sử dụng **Firebase** làm backend và **Cloudinary** để lưu trữ hình ảnh.

---

## ✨ Tính năng

| Tính năng | Mô tả |
|-----------|-------|
| 🔐 **Đăng nhập / Đăng ký** | Xác thực người dùng qua Firebase Authentication |
| 🏠 **Trang chủ** | Hiển thị danh mục món ăn (ngang) + danh sách quán ăn (dọc) |
| 🔍 **Tìm kiếm** | Tìm kiếm quán ăn theo tên (local filtering) |
| 🏪 **Chi tiết quán** | Xem thông tin quán + thực đơn món ăn |
| 🛒 **Giỏ hàng** | Thêm/xóa/sửa số lượng món — chỉ từ 1 quán tại 1 thời điểm |
| 💳 **Đặt hàng** | Nhập địa chỉ, ghi chú → tạo đơn hàng trên Firestore |
| 📦 **Đơn hàng** | Xem lịch sử đơn hàng đã đặt |
| ❤️ **Yêu thích** | Lưu quán/món yêu thích, xóa khi không cần |
| 👤 **Tài khoản** | Xem/sửa thông tin cá nhân, đăng xuất |
| ☁️ **Upload ảnh** | Tích hợp Cloudinary cho upload ảnh đại diện |

---

## 🛠 Công nghệ sử dụng

| Thành phần | Công nghệ |
|------------|-----------|
| Ngôn ngữ | **Java 11** |
| Giao diện | **XML Views** (không dùng Jetpack Compose) |
| Kiến trúc | **MVVM** (ViewModel + LiveData) |
| Backend | **Firebase** (Authentication + Cloud Firestore) |
| Hình ảnh (hiển thị) | **Glide 4.16.0** |
| Hình ảnh (lưu trữ) | **Cloudinary Android SDK 3.0.2** |
| UI Components | RecyclerView, CardView, ViewPager2, Material Design 3 |
| Build tool | **Gradle (Kotlin DSL)** |
| Min SDK | **24** (Android 7.0) |
| Target SDK | **35** (Android 15) |

---

## 💻 Yêu cầu hệ thống

- **Android Studio** Hedgehog (2023.1.1) trở lên
- **JDK 11** trở lên
- **Android SDK 35**
- Tài khoản **Firebase** (đã tạo project)
- Tài khoản **Cloudinary** (miễn phí)
- Thiết bị/emulator chạy **Android 7.0+** (API 24+)

---

## 🔧 Cài đặt & Cấu hình

### 1. Clone dự án

```bash
git clone <repository-url>
cd project-nhom9
```

### 2. Cấu hình Firebase

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Tạo project mới (hoặc dùng project có sẵn)
3. Thêm ứng dụng Android với package name: `com.example.foodnow`
4. Tải file `google-services.json` và đặt vào thư mục `app/`
5. Bật **Authentication** → phương thức **Email/Password**
6. Tạo **Cloud Firestore** database
7. Tạo các collection theo schema tại [`.github/db.md`](.github/db.md):
   - `Users`, `Stores`, `Foods`, `Categories`, `Orders`, `Favorites`

### 3. Cấu hình Cloudinary

1. Đăng ký tại [cloudinary.com](https://cloudinary.com/)
2. Lấy **Cloud Name** từ Dashboard
3. Tạo **Upload Preset** (unsigned) với tên: `foodnow_unsigned`
4. Cập nhật `local.properties` (không commit file này):

```properties
cloudinary.cloud_name=YOUR_CLOUD_NAME
cloudinary.upload_preset=YOUR_UNSIGNED_UPLOAD_PRESET
```

5. App sẽ đọc cấu hình từ:
   - `local.properties` (`cloudinary.cloud_name`, `cloudinary.upload_preset`)
   - hoặc Gradle properties (`CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_UPLOAD_PRESET`)
   - nếu thiếu sẽ dùng giá trị mặc định trong `app/build.gradle.kts`

### 4. Mở dự án trong Android Studio

1. **File → Open** → chọn thư mục `project-nhom9`
2. Chờ Gradle sync hoàn tất
3. Kiểm tra `google-services.json` đã có trong `app/`

---

## 📁 Cấu trúc dự án

```
project-nhom9/
├── .github/
│   ├── copilot-instructions.md    ← Hướng dẫn cho AI assistant
│   └── db.md                      ← Schema Firestore
├── app/
│   ├── build.gradle.kts           ← Dependencies
│   ├── google-services.json       ← Firebase config (không commit)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/foodnow/
│       │   ├── MainActivity.java           ← Navigation chính (4 tab)
│       │   ├── models/                     ← 7 data classes
│       │   │   ├── User.java
│       │   │   ├── Store.java
│       │   │   ├── Food.java
│       │   │   ├── Category.java
│       │   │   ├── Order.java              ← chứa inner class OrderItem
│       │   │   ├── CartItem.java           ← local-only, không lưu Firestore
│       │   │   └── Favorite.java
│       │   ├── repositories/               ← 7 Firebase data access
│       │   │   ├── AuthRepository.java
│       │   │   ├── StoreRepository.java
│       │   │   ├── CategoryRepository.java
│       │   │   ├── FoodRepository.java
│       │   │   ├── OrderRepository.java
│       │   │   ├── FavoriteRepository.java
│       │   │   └── UserRepository.java
│       │   ├── viewmodels/                 ← 7 ViewModels + LiveData
│       │   │   ├── AuthViewModel.java
│       │   │   ├── HomeViewModel.java
│       │   │   ├── StoreDetailViewModel.java
│       │   │   ├── CheckoutViewModel.java
│       │   │   ├── OrdersViewModel.java
│       │   │   ├── FavoritesViewModel.java
│       │   │   └── ProfileViewModel.java
│       │   ├── activities/                 ← 4 Activities
│       │   │   ├── LoginActivity.java
│       │   │   ├── RegisterActivity.java
│       │   │   ├── StoreDetailActivity.java
│       │   │   └── CheckoutActivity.java
│       │   ├── fragments/                  ← 5 Fragments
│       │   │   ├── HomeFragment.java
│       │   │   ├── CartFragment.java
│       │   │   ├── OrdersFragment.java
│       │   │   ├── FavoritesFragment.java
│       │   │   └── ProfileFragment.java
│       │   ├── adapters/                   ← 6 RecyclerView Adapters
│       │   │   ├── StoreAdapter.java
│       │   │   ├── CategoryAdapter.java
│       │   │   ├── FoodAdapter.java
│       │   │   ├── CartAdapter.java
│       │   │   ├── OrderAdapter.java
│       │   │   └── FavoriteAdapter.java
│       │   └── utils/                      ← Utilities
│       │       ├── CartManager.java        ← Singleton giỏ hàng
│       │       └── CloudinaryHelper.java   ← Upload ảnh
│       └── res/
│           ├── layout/                     ← 16 XML layouts
│           │   ├── activity_main.xml
│           │   ├── activity_login.xml
│           │   ├── activity_register.xml
│           │   ├── activity_store_detail.xml
│           │   ├── activity_checkout.xml
│           │   ├── fragment_home.xml
│           │   ├── fragment_cart.xml
│           │   ├── fragment_orders.xml
│           │   ├── fragment_favorites.xml
│           │   ├── fragment_profile.xml
│           │   ├── item_store.xml
│           │   ├── item_category.xml
│           │   ├── item_food.xml
│           │   ├── item_cart.xml
│           │   ├── item_order.xml
│           │   └── item_favorite.xml
│           ├── menu/
│           │   └── bottom_nav_menu.xml     ← 4 tab navigation
│           └── values/
│               ├── strings.xml
│               └── themes.xml
├── build.gradle.kts                        ← Root Gradle
├── settings.gradle.kts                     ← Project settings
└── gradle/                                 ← Gradle wrapper
```

**Tổng cộng: 40 file Java · 16 layouts · 1 menu · 74+ files**

---

## 🏗 Kiến trúc MVVM

```
┌─────────────────────────────────────────────────────────┐
│                        VIEW                             │
│  Activities / Fragments / Adapters                      │
│  (XML Layouts + Java UI logic)                          │
│                                                         │
│  • Hiển thị dữ liệu từ ViewModel qua LiveData          │
│  • Gửi user actions đến ViewModel                       │
│  • KHÔNG truy cập trực tiếp Firebase                    │
└────────────────────┬────────────────────────────────────┘
                     │ observe LiveData / call methods
┌────────────────────▼────────────────────────────────────┐
│                    VIEWMODEL                            │
│  AuthViewModel, HomeViewModel, StoreDetailViewModel,    │
│  CheckoutViewModel, OrdersViewModel, FavoritesViewModel,│
│  ProfileViewModel                                       │
│                                                         │
│  • Giữ LiveData cho UI observe                          │
│  • Gọi Repository để lấy/ghi dữ liệu                  │
│  • Xử lý logic nghiệp vụ đơn giản                     │
└────────────────────┬────────────────────────────────────┘
                     │ call methods
┌────────────────────▼────────────────────────────────────┐
│                   REPOSITORY                            │
│  AuthRepo, StoreRepo, CategoryRepo, FoodRepo,          │
│  OrderRepo, FavoriteRepo, UserRepo                      │
│                                                         │
│  • Tương tác trực tiếp với Firebase Auth / Firestore    │
│  • Trả về LiveData hoặc Task                           │
│  • Snapshot listeners cho dữ liệu real-time            │
└────────────────────┬────────────────────────────────────┘
                     │ read/write
┌────────────────────▼────────────────────────────────────┐
│                   DATA SOURCE                           │
│  Firebase Auth  │  Cloud Firestore  │  Cloudinary       │
│  (đăng nhập)    │  (dữ liệu)       │  (hình ảnh)       │
└─────────────────────────────────────────────────────────┘
```

> 📖 Xem chi tiết kiến trúc tại [`ARCHITECTURE.md`](ARCHITECTURE.md)

---

## 🚀 Build & Chạy

### Build debug APK
```bash
.\gradlew.bat assembleDebug
```

### Chạy unit tests
```bash
.\gradlew.bat testDebugUnitTest
```

### Chạy instrumentation tests (cần thiết bị/emulator)
```bash
.\gradlew.bat connectedDebugAndroidTest
```

### Chạy lint kiểm tra code
```bash
.\gradlew.bat lintDebug
```

### Chạy 1 test cụ thể
```bash
# Unit test
.\gradlew.bat testDebugUnitTest --tests "com.example.foodnow.ExampleUnitTest"

# Instrumentation test
.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.foodnow.ExampleInstrumentedTest#useAppContext
```

---

## 📸 Ảnh chụp màn hình

<!-- Thêm ảnh chụp màn hình ứng dụng tại đây -->

| Đăng nhập | Trang chủ | Chi tiết quán |
|:---------:|:---------:|:-------------:|
| <!-- ![Login](screenshots/login.png) --> | <!-- ![Home](screenshots/home.png) --> | <!-- ![Store](screenshots/store.png) --> |

| Giỏ hàng | Đặt hàng | Đơn hàng |
|:---------:|:---------:|:--------:|
| <!-- ![Cart](screenshots/cart.png) --> | <!-- ![Checkout](screenshots/checkout.png) --> | <!-- ![Orders](screenshots/orders.png) --> |

---

## 📚 Tài liệu liên quan

- [`ARCHITECTURE.md`](ARCHITECTURE.md) — Kiến trúc kỹ thuật chi tiết
- [`.github/db.md`](.github/db.md) — Schema cơ sở dữ liệu Firestore
- [`.github/copilot-instructions.md`](.github/copilot-instructions.md) — Hướng dẫn cho AI assistant

---

## 👥 Thành viên nhóm

<!-- Cập nhật thông tin thành viên nhóm -->

| STT | Họ và tên | MSSV | Vai trò |
|:---:|-----------|------|---------|
| 1 | | | |
| 2 | | | |
| 3 | | | |

---

## 📄 License

Dự án phục vụ mục đích học tập tại trường đại học.
