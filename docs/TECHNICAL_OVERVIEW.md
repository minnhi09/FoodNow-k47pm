# 📋 Tổng Quan Dự Án & Tài Liệu Kỹ Thuật — FoodNow

> **Cập nhật lần cuối:** Tháng 4 / 2025  
> **Phản ánh hiện trạng thực tế của codebase tại thời điểm viết tài liệu này.**

---

## Mục lục

1. [Tổng quan dự án](#1-tổng-quan-dự-án)
2. [Hiện trạng triển khai](#2-hiện-trạng-triển-khai)
3. [Kiến trúc MVVM](#3-kiến-trúc-mvvm)
4. [Cấu trúc toàn bộ file](#4-cấu-trúc-toàn-bộ-file)
5. [Luồng điều hướng theo loại người dùng](#5-luồng-điều-hướng-theo-loại-người-dùng)
6. [Mô tả chi tiết từng màn hình](#6-mô-tả-chi-tiết-từng-màn-hình)
7. [Cơ sở dữ liệu Firestore](#7-cơ-sở-dữ-liệu-firestore)
8. [Dependencies (build.gradle.kts)](#8-dependencies-buildgradlekts)
9. [Tính năng còn thiếu — Roadmap cho nhóm](#9-tính-năng-còn-thiếu--roadmap-cho-nhóm)
10. [Hướng dẫn cấu hình & build](#10-hướng-dẫn-cấu-hình--build)

---

## 1. Tổng quan dự án

| Mục | Chi tiết |
|-----|---------|
| **Tên ứng dụng** | FoodNow |
| **Mục đích** | Ứng dụng đặt đồ ăn đơn giản — dự án môn học Android Development (đại học) |
| **Package** | `com.example.foodnow` |
| **Ngôn ngữ** | Java 11 |
| **Giao diện** | XML Views (không dùng Jetpack Compose) |
| **Kiến trúc** | MVVM (ViewModel + LiveData) |
| **Backend** | Firebase Authentication + Cloud Firestore |
| **Lưu trữ ảnh** | Cloudinary (upload unsigned) |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 34 (Android 14) |
| **Build tool** | Gradle 8.x với Kotlin DSL (`build.gradle.kts`) |

### Tính năng tổng thể của ứng dụng

Ứng dụng phục vụ **3 loại người dùng** với giao diện riêng:

| Vai trò | Truy cập | Mô tả |
|---------|---------|-------|
| `customer` | `MainActivity` | Xem quán, đặt món, quản lý đơn hàng, yêu thích |
| `store_owner` | `StoreOwnerActivity` | Quản lý quán + thực đơn, xem đơn hàng đến quán |
| `admin` | `MainActivity` + `ImageAdminActivity` | Có thêm quyền upload ảnh cho Store/Food |

---

## 2. Hiện trạng triển khai

### 2.1 Tính năng đã hoàn thành ✅

| # | Tính năng | Activity / Fragment | Ghi chú |
|---|-----------|---------------------|---------|
| 1 | **Đăng nhập** | `LoginActivity` | Firebase Auth, route theo role |
| 2 | **Đăng ký** | `RegisterActivity` | Tạo tài khoản + lưu Firestore Users |
| 3 | **Trang chủ** | `HomeFragment` | Danh mục ngang + quán dọc + gợi ý món |
| 4 | **Tìm kiếm quán** | `HomeFragment` | Lọc local theo tên + mô tả |
| 5 | **Lọc theo danh mục** | `HomeFragment` | Click chip → lọc quán theo categoryId |
| 6 | **Chi tiết quán** | `StoreDetailActivity` | Header quán + danh sách món |
| 7 | **Chi tiết món ăn** | `FoodDetailActivity` | Giá, mô tả, số lượng, đánh giá mock |
| 8 | **Hồ sơ cá nhân** | `ProfileFragment` | Xem thông tin, đăng xuất |
| 9 | **Chỉnh sửa hồ sơ** | `EditProfileActivity` | Sửa tên/SĐT/địa chỉ + upload avatar |
| 10 | **Upload ảnh (Admin)** | `ImageAdminActivity` | Admin upload ảnh cho Store/Food |
| 11 | **Dashboard chủ quán** | `StoreOwnerDashboardFragment` | Xem thông tin quán, bật/tắt mở cửa |
| 12 | **Quản lý thực đơn** | `ManageFoodsFragment` | Danh sách món + CRUD |
| 13 | **Thêm/sửa món ăn** | `AddEditFoodActivity` | Form + upload ảnh Cloudinary |

### 2.2 Đã tạo file nhưng chỉ là Placeholder 🚧

| # | Tính năng | File | Trạng thái |
|---|-----------|------|-----------|
| 1 | **Giỏ hàng** | `CartFragment.java` | Chỉ hiển thị text "đang phát triển" |
| 2 | **Đơn hàng** | `OrdersFragment.java` | Chỉ hiển thị text "đang phát triển" |
| 3 | **Yêu thích** | `FavoritesFragment.java` | Chỉ hiển thị text "đang phát triển" |
| 4 | **Đơn hàng quán** | `StoreOrdersFragment.java` | Chỉ inflate layout rỗng |
| 5 | **Nút "Thêm vào giỏ"** | `FoodDetailActivity.java` | Chỉ Toast, chưa kết nối CartManager |
| 6 | **Nút yêu thích** | `FoodDetailActivity.java` | Toggle UI only, chưa lưu Firestore |
| 7 | **Badge giỏ hàng** | `MainActivity.java` | Hardcoded = 3, chưa dynamic |

### 2.3 Chưa tạo ❌

| # | File cần tạo | Layer | Dùng bởi |
|---|-------------|-------|---------|
| 1 | `CartItem.java` | Model | CartManager, CartAdapter |
| 2 | `Order.java` (+ `OrderItem` inner class) | Model | OrderRepository, CheckoutActivity |
| 3 | `Favorite.java` | Model | FavoriteRepository, FavoritesFragment |
| 4 | `CartManager.java` | Utils / Singleton | CartFragment, FoodDetailActivity |
| 5 | `OrderRepository.java` | Repository | CheckoutViewModel, OrdersViewModel |
| 6 | `FavoriteRepository.java` | Repository | FavoritesViewModel |
| 7 | `CheckoutViewModel.java` | ViewModel | CheckoutActivity |
| 8 | `OrdersViewModel.java` | ViewModel | OrdersFragment |
| 9 | `FavoritesViewModel.java` | ViewModel | FavoritesFragment |
| 10 | `CartAdapter.java` | Adapter | CartFragment |
| 11 | `OrderAdapter.java` | Adapter | OrdersFragment |
| 12 | `FavoriteAdapter.java` | Adapter | FavoritesFragment |
| 13 | `CheckoutActivity.java` | Activity | CartFragment → đặt hàng |
| 14 | Layout `fragment_cart.xml` | Layout | CartFragment |
| 15 | Layout `activity_checkout.xml` | Layout | CheckoutActivity |
| 16 | Layout `fragment_orders.xml` | Layout | OrdersFragment |
| 17 | Layout `fragment_favorites.xml` | Layout | FavoritesFragment |
| 18 | Layout `item_cart.xml` | Layout | CartAdapter |
| 19 | Layout `item_order.xml` | Layout | OrderAdapter |
| 20 | Layout `item_favorite.xml` | Layout | FavoriteAdapter |

---

## 3. Kiến trúc MVVM

### 3.1 Sơ đồ tổng thể

```
┌───────────────────────────────────────────────────────────────────┐
│                            VIEW LAYER                             │
│                                                                   │
│   LoginActivity       MainActivity          StoreOwnerActivity    │
│   RegisterActivity    ├── HomeFragment       ├── DashboardFragment│
│   StoreDetailActivity ├── CartFragment       ├── ManageFoodsFragment│
│   FoodDetailActivity  ├── OrdersFragment     ├── StoreOrdersFragment│
│   EditProfileActivity ├── FavoritesFragment  └── ProfileFragment  │
│   AddEditFoodActivity └── ProfileFragment                         │
│   ImageAdminActivity                                              │
│                                                                   │
│   Adapters: StoreAdapter, FoodAdapter, CategoryAdapter,           │
│            RecommendedFoodAdapter, ManageFoodAdapter, ReviewAdapter│
└──────────────────────────────┬────────────────────────────────────┘
                               │ observe LiveData / call methods
┌──────────────────────────────▼────────────────────────────────────┐
│                         VIEWMODEL LAYER                           │
│                                                                   │
│   AuthViewModel        HomeViewModel         ProfileViewModel     │
│   StoreDetailViewModel StoreOwnerViewModel                        │
│                                                                   │
│   (Chưa tạo: CheckoutViewModel, OrdersViewModel, FavoritesViewModel)│
└──────────────────────────────┬────────────────────────────────────┘
                               │ call methods
┌──────────────────────────────▼────────────────────────────────────┐
│                        REPOSITORY LAYER                           │
│                                                                   │
│   AuthRepository    StoreRepository    FoodRepository             │
│   CategoryRepository  UserRepository                              │
│                                                                   │
│   (Chưa tạo: OrderRepository, FavoriteRepository)                 │
└──────────────────────────────┬────────────────────────────────────┘
                               │ read/write
┌──────────────────────────────▼────────────────────────────────────┐
│                         DATA LAYER                                │
│                                                                   │
│   Firebase Auth     Cloud Firestore       Cloudinary              │
│   (đăng nhập)       (dữ liệu chính)       (ảnh)                  │
└───────────────────────────────────────────────────────────────────┘
```

### 3.2 Nguyên tắc thiết kế

| Nguyên tắc | Cách áp dụng |
|------------|-------------|
| **Không DI framework** | Repository được tạo bằng `new` trực tiếp trong ViewModel |
| **LiveData reactive** | Repository trả về `LiveData` từ Firestore SnapshotListener |
| **Single source of truth** | View chỉ đọc từ ViewModel, không tự gọi Firebase |
| **Cart in-memory** | `CartManager` Singleton (chưa tạo) — không lưu Firestore |
| **Comment tiếng Việt** | Toàn bộ comment trong code là tiếng Việt |
| **Mỗi model có constructor rỗng** | Bắt buộc cho Firestore deserialization |

### 3.3 Luồng dữ liệu đọc (Read)

```
Firestore (collection/document)
        │
        ▼ SnapshotListener (real-time)
Repository
        │ documents → Java objects
        ▼ MutableLiveData.setValue(list)
ViewModel
        │ LiveData (read-only getter)
        ▼ observe(lifecycleOwner, observer)
Fragment/Activity
        │
        ▼ adapter.notifyDataSetChanged()
RecyclerView (UI)
```

### 3.4 Luồng dữ liệu ghi (Write)

```
User nhấn nút
        │
        ▼ gọi viewModel.method(data)
ViewModel
        │ tạo object / validate
        ▼ repository.add/update/delete()
Repository
        │ Firestore .set() / .update() / .delete()
        ▼ Task<Void>
ViewModel
        ├── onSuccess → actionMessage.setValue("Thành công!")
        └── onFailure → actionMessage.setValue("Lỗi: ...")
Fragment/Activity
        └── observe actionMessage → Toast
```

---

## 4. Cấu trúc toàn bộ file

```
project-nhom9/
├── .github/
│   ├── copilot-instructions.md     ← Hướng dẫn cho AI assistant
│   ├── db.md                       ← Schema Firestore đầy đủ
│   ├── guide-tv2-tv3.md            ← Hướng dẫn phân công TV2, TV3
│   ├── plan-01.md                  ← Kế hoạch phát triển ban đầu
│   └── work-division.md            ← Phân công công việc nhóm
│
├── app/
│   ├── build.gradle.kts            ← Dependencies + Cloudinary config
│   ├── google-services.json        ← Firebase config (không commit)
│   └── src/main/
│       ├── AndroidManifest.xml     ← 8 Activity khai báo
│       └── java/com/example/foodnow/
│           │
│           ├── MainActivity.java           ← 5 tab (Home/Cart/Orders/Fav/Profile)
│           │                               ← Badge giỏ hàng (hardcoded=3)
│           │
│           ├── models/                     ← Data classes khớp Firestore
│           │   ├── User.java               ✅ id, email, name, phone, address,
│           │   │                              imageUrl, role, storeId, createdAt
│           │   ├── Store.java              ✅ id, name, description, address,
│           │   │                              phone, imageUrl, rating, deliveryTime,
│           │   │                              deliveryFee, isOpen, storeOwnerId, categoryId
│           │   ├── Food.java               ✅ id, title, description, price,
│           │   │                              imageUrl, rating, storeId, categoryId, isAvailable
│           │   ├── Category.java           ✅ id, name, imageUrl
│           │   ├── Review.java             ✅ Mock only - reviewerName, timeAgo,
│           │   │                              rating, comment, likes (chưa lưu Firestore)
│           │   ├── RecommendedFood.java    ✅ Mock only - name, storeName, price,
│           │   │                              rating, imageUrl, popular
│           │   ├── CartItem.java           ❌ CHƯA TẠO
│           │   ├── Order.java              ❌ CHƯA TẠO (cần inner class OrderItem)
│           │   └── Favorite.java           ❌ CHƯA TẠO
│           │
│           ├── repositories/
│           │   ├── AuthRepository.java     ✅ login, register, logout, getUserProfile
│           │   ├── StoreRepository.java    ✅ getAllStores, getStoreById, updateStore
│           │   ├── FoodRepository.java     ✅ getFoodsByStore, addFood, updateFood, deleteFood
│           │   ├── CategoryRepository.java ✅ getAllCategories
│           │   ├── UserRepository.java     ✅ getUser, updateUser
│           │   ├── OrderRepository.java    ❌ CHƯA TẠO
│           │   └── FavoriteRepository.java ❌ CHƯA TẠO
│           │
│           ├── viewmodels/
│           │   ├── AuthViewModel.java      ✅ login, register, logout, userProfileLiveData
│           │   ├── HomeViewModel.java      ✅ getCategories, getStores
│           │   ├── StoreDetailViewModel.java ✅ getFoods(storeId)
│           │   ├── ProfileViewModel.java   ✅ getUser, updateUser
│           │   ├── StoreOwnerViewModel.java ✅ getStore, getFoods, addFood,
│           │   │                              updateFood, deleteFood, loading, actionMessage
│           │   ├── CheckoutViewModel.java  ❌ CHƯA TẠO
│           │   ├── OrdersViewModel.java    ❌ CHƯA TẠO
│           │   └── FavoritesViewModel.java ❌ CHƯA TẠO
│           │
│           ├── activities/
│           │   ├── LoginActivity.java      ✅ Email/password login, route theo role
│           │   ├── RegisterActivity.java   ✅ Tạo account + lưu Firestore
│           │   ├── StoreDetailActivity.java ✅ Header quán + RecyclerView món ăn
│           │   ├── FoodDetailActivity.java ✅ Chi tiết món, số lượng, reviews mock
│           │   │                              ⚠️ Nút "Thêm vào giỏ" chỉ Toast
│           │   │                              ⚠️ Yêu thích chỉ toggle UI
│           │   ├── EditProfileActivity.java ✅ Sửa tên/SĐT/địa chỉ + upload avatar
│           │   ├── ImageAdminActivity.java  ✅ Upload ảnh cho Store/Food (admin only)
│           │   ├── StoreOwnerActivity.java  ✅ 4 tab cho chủ quán
│           │   ├── AddEditFoodActivity.java ✅ Form thêm/sửa món + upload ảnh
│           │   └── CheckoutActivity.java   ❌ CHƯA TẠO
│           │
│           ├── fragments/
│           │   ├── HomeFragment.java       ✅ Search + category filter + store list
│           │   │                              + recommended foods (mock)
│           │   ├── ProfileFragment.java    ✅ User info, admin check, đăng xuất
│           │   ├── CartFragment.java       🚧 Skeleton ("đang phát triển")
│           │   ├── OrdersFragment.java     🚧 Skeleton ("đang phát triển")
│           │   ├── FavoritesFragment.java  🚧 Skeleton ("đang phát triển")
│           │   ├── StoreOwnerDashboardFragment.java ✅ Store info + toggle mở/đóng
│           │   ├── ManageFoodsFragment.java ✅ CRUD danh sách món + FAB thêm mới
│           │   └── StoreOrdersFragment.java 🚧 Chỉ inflate layout rỗng
│           │
│           ├── adapters/
│           │   ├── StoreAdapter.java       ✅ Danh sách quán ăn (RecyclerView)
│           │   ├── FoodAdapter.java        ✅ Danh sách món ăn (RecyclerView)
│           │   ├── CategoryAdapter.java    ✅ Chip danh mục ngang (RecyclerView)
│           │   ├── RecommendedFoodAdapter.java ✅ Grid 2 cột gợi ý món
│           │   ├── ManageFoodAdapter.java  ✅ Danh sách món cho chủ quán (Sửa/Xóa)
│           │   ├── ReviewAdapter.java      ✅ Đánh giá món ăn (mock data)
│           │   ├── CartAdapter.java        ❌ CHƯA TẠO
│           │   ├── OrderAdapter.java       ❌ CHƯA TẠO
│           │   └── FavoriteAdapter.java    ❌ CHƯA TẠO
│           │
│           └── utils/
│               └── CloudinaryHelper.java   ✅ init, uploadImage, OnUploadCallback
│                                              ❌ CartManager.java CHƯA TẠO
│
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml               ✅ BottomNavigationView + fragment_container
│           │   ├── activity_login.xml              ✅
│           │   ├── activity_register.xml           ✅
│           │   ├── activity_store_detail.xml       ✅
│           │   ├── activity_food_detail.xml        ✅
│           │   ├── activity_edit_profile.xml       ✅
│           │   ├── activity_image_admin.xml        ✅
│           │   ├── activity_store_owner.xml        ✅
│           │   ├── activity_add_edit_food.xml      ✅
│           │   ├── fragment_home.xml               ✅
│           │   ├── fragment_profile.xml            ✅
│           │   ├── fragment_store_owner_dashboard.xml ✅
│           │   ├── fragment_manage_foods.xml       ✅
│           │   ├── fragment_store_orders.xml       🚧 Layout rỗng
│           │   ├── fragment_cart.xml               ❌ CHƯA TẠO (CartFragment dùng code)
│           │   ├── fragment_orders.xml             ❌ CHƯA TẠO
│           │   ├── fragment_favorites.xml          ❌ CHƯA TẠO
│           │   ├── activity_checkout.xml           ❌ CHƯA TẠO
│           │   ├── item_store.xml                  ✅
│           │   ├── item_food.xml                   ✅
│           │   ├── item_category.xml               ✅
│           │   ├── item_recommended_food.xml       ✅
│           │   ├── item_manage_food.xml            ✅
│           │   ├── item_review.xml                 ✅
│           │   ├── item_cart.xml                   ❌ CHƯA TẠO
│           │   ├── item_order.xml                  ❌ CHƯA TẠO
│           │   └── item_favorite.xml               ❌ CHƯA TẠO
│           ├── menu/
│           │   ├── bottom_nav_menu.xml             ✅ 5 tab: home/cart/orders/favorites/profile
│           │   └── bottom_nav_owner_menu.xml       ✅ 4 tab chủ quán
│           └── values/
│               ├── strings.xml
│               ├── colors.xml
│               └── themes.xml
│
├── docs/                           ← Tài liệu dự án
│   ├── TECHNICAL_OVERVIEW.md       ← File này
│   ├── PLAN.md
│   ├── PROJECT_EVALUATION.md
│   ├── SYSTEM_EVALUATION.md
│   └── ...nhiều tài liệu khác
│
├── .github/
├── figma/                          ← File thiết kế Figma
├── bugs/                           ← Bug tracker
├── scripts/                        ← Scripts tiện ích
├── ARCHITECTURE.md                 ← Kiến trúc kỹ thuật (tổng quát)
├── README.md                       ← Hướng dẫn cài đặt & chạy
├── build.gradle.kts                ← Root Gradle
└── settings.gradle.kts
```

**Tổng cộng (hiện tại):**
- **Java files:** ~30 file đã triển khai + nhiều file skeleton/chưa tạo
- **Layouts:** ~20 layout XML
- **Menus:** 2 file menu

---

## 5. Luồng điều hướng theo loại người dùng

### 5.1 Customer (người dùng thông thường)

```
App khởi động
      │
      ▼
LoginActivity
  ├── Chưa có tài khoản → RegisterActivity → LoginActivity
  └── Đăng nhập thành công + role = "customer" hoặc "admin"
            │
            ▼
      MainActivity (BottomNavigationView — 5 tab)
        │
        ├── [Home] HomeFragment
        │     ├── Search quán theo tên
        │     ├── Filter theo danh mục (chip)
        │     ├── Danh sách quán (RecyclerView)
        │     │     └── Click quán → StoreDetailActivity
        │     │                         ├── Xem thực đơn
        │     │                         └── Click món → FoodDetailActivity
        │     │                                           ├── Điều chỉnh số lượng
        │     │                                           ├── Nút "Thêm vào giỏ" (TODO)
        │     │                                           └── Nút "Yêu thích" (TODO)
        │     └── Gợi ý món (mock data)
        │
        ├── [Cart] CartFragment ← 🚧 Placeholder
        │     └── TODO: Danh sách CartItem + nút "Đặt hàng" → CheckoutActivity
        │
        ├── [Orders] OrdersFragment ← 🚧 Placeholder
        │     └── TODO: Lịch sử đơn hàng
        │
        ├── [Favorites] FavoritesFragment ← 🚧 Placeholder
        │     └── TODO: Quán/món yêu thích
        │
        └── [Profile] ProfileFragment
              ├── Xem tên, số điện thoại, avatar
              ├── Click → EditProfileActivity (sửa tên/SĐT/địa chỉ + upload avatar)
              ├── [Admin only] → ImageAdminActivity (upload ảnh Store/Food)
              └── Đăng xuất → LoginActivity
```

### 5.2 Store Owner (chủ quán)

```
LoginActivity
  └── Đăng nhập + role = "store_owner"
            │
            ▼
      StoreOwnerActivity (BottomNavigationView — 4 tab)
        │
        ├── [Dashboard] StoreOwnerDashboardFragment
        │     ├── Xem thông tin quán (ảnh, tên, địa chỉ, SĐT, rating)
        │     └── Toggle Switch → Mở / Đóng cửa (cập nhật Firestore ngay)
        │
        ├── [Thực đơn] ManageFoodsFragment
        │     ├── Danh sách món ăn (RecyclerView)
        │     ├── FAB "+" → AddEditFoodActivity (thêm mới)
        │     ├── Nút "Sửa" → AddEditFoodActivity (chỉnh sửa)
        │     └── Nút "Xóa" → AlertDialog → xóa Firestore
        │
        ├── [Đơn hàng] StoreOrdersFragment ← 🚧 Placeholder
        │     └── TODO: Danh sách đơn hàng đến quán
        │
        └── [Tài khoản] ProfileFragment
              └── Tái sử dụng ProfileFragment của customer
```

### 5.3 Admin

```
Đăng nhập với role = "admin"
      │
      ▼
MainActivity (giống customer)
  └── ProfileFragment
        └── [Admin only] row_admin → ImageAdminActivity
              ├── Chọn loại: Store hoặc Food
              ├── Chọn item từ Spinner (tải từ Firestore)
              ├── Chọn ảnh từ thư viện
              └── Upload → Cloudinary → cập nhật imageUrl trong Firestore
```

---

## 6. Mô tả chi tiết từng màn hình

### 6.1 LoginActivity ✅

| Mục | Chi tiết |
|-----|---------|
| **Layout** | `activity_login.xml` |
| **ViewModel** | `AuthViewModel` |
| **Input** | Email, Password |
| **Actions** | Nút Login, Link "Đăng ký" |
| **Output** | Route đến `MainActivity` (customer/admin) hoặc `StoreOwnerActivity` (store_owner) |
| **Ghi chú** | Nếu đã đăng nhập → bypass LoginActivity; Sau login fetch profile từ Firestore để lấy role |

### 6.2 RegisterActivity ✅

| Mục | Chi tiết |
|-----|---------|
| **Layout** | `activity_register.xml` |
| **ViewModel** | `AuthViewModel` |
| **Input** | Tên, Email, SĐT, Mật khẩu |
| **Output** | Tạo FirebaseAuth account + document trong `Users/` với role="customer" |

### 6.3 HomeFragment ✅

| Mục | Chi tiết |
|-----|---------|
| **Layout** | `fragment_home.xml` |
| **ViewModel** | `HomeViewModel` |
| **Dữ liệu thật** | Categories (Firestore), Stores (Firestore) |
| **Dữ liệu mock** | Recommended Foods (luôn mock, chưa có Firestore) |
| **Fallback** | Nếu Firestore trống → hiện mock stores + mock categories |
| **Search** | Local filter theo `store.name` và `store.description` |
| **Category filter** | Click chip → lọc `store.categoryId`; click "Tất cả" → bỏ lọc |
| **Click quán** | Truyền storeId + storeName + storeImage + storeRating + storeDeliveryTime + storeDeliveryFee qua Intent |

### 6.4 StoreDetailActivity ✅

| Mục | Chi tiết |
|-----|---------|
| **Layout** | `activity_store_detail.xml` |
| **ViewModel** | `StoreDetailViewModel` |
| **Input qua Intent** | storeId, storeName, storeImage, storeRating, storeDeliveryTime, storeDeliveryFee |
| **Output** | RecyclerView danh sách Foods theo storeId |
| **Click món** | Mở `FoodDetailActivity` với foodId + foodTitle + foodDescription + foodPrice + foodImageUrl + foodRating |

### 6.5 FoodDetailActivity ✅ (một phần)

| Mục | Chi tiết |
|-----|---------|
| **Layout** | `activity_food_detail.xml` |
| **Input qua Intent** | foodId, foodTitle, foodDescription, foodPrice, foodImageUrl, foodRating, storeName, storeDeliveryTime, storeDeliveryFee |
| **Chức năng hoàn chỉnh** | Hiển thị thông tin món, điều chỉnh số lượng [−][n][+], cập nhật giá |
| **⚠️ Chưa hoàn chỉnh** | Nút "Thêm vào giỏ" → chỉ Toast; Nút "Đặt ngay" → chỉ Toast; Yêu thích → toggle UI only (không lưu Firestore) |
| **Reviews** | Mock data hardcoded (4 reviews) |

### 6.6 ProfileFragment ✅

| Mục | Chi tiết |
|-----|---------|
| **Layout** | `fragment_profile.xml` |
| **ViewModels** | `ProfileViewModel` (user data), `AuthViewModel` (logout) |
| **Hiển thị** | Avatar, tên, SĐT, các menu row |
| **Admin only** | Row "Quản trị ảnh" hiện khi role = "admin" |
| **Click hành động** | Sửa hồ sơ → `EditProfileActivity`; Đăng xuất → `LoginActivity` |

### 6.7 EditProfileActivity ✅

| Mục | Chi tiết |
|-----|---------|
| **Layout** | `activity_edit_profile.xml` |
| **ViewModel** | `ProfileViewModel` |
| **Input** | Tên, SĐT, Địa chỉ, Avatar (từ gallery) |
| **Upload ảnh** | `CloudinaryHelper.uploadImage` → folder `foodnow/profiles` → lưu URL vào `Users/{uid}.imageUrl` |

### 6.8 StoreOwnerActivity ✅

| Mục | Chi tiết |
|-----|---------|
| **Layout** | `activity_store_owner.xml` |
| **storeId** | Nhận qua Intent từ `LoginActivity` |
| **4 tab** | Dashboard, Thực đơn, Đơn hàng (placeholder), Tài khoản |
| **Shared ViewModel** | `StoreOwnerViewModel` dùng chung cho tất cả Fragment con |

### 6.9 AddEditFoodActivity ✅

| Mục | Chi tiết |
|-----|---------|
| **Layout** | `activity_add_edit_food.xml` |
| **ViewModel** | `StoreOwnerViewModel` |
| **Chế độ THÊM** | Không có `foodId` trong Intent; gọi `viewModel.addFood()` |
| **Chế độ SỬA** | Có `foodId`; điền sẵn form; gọi `viewModel.updateFood()` |
| **Upload ảnh** | `CloudinaryHelper.uploadImage` → folder `foodnow/foods` |

---

## 7. Cơ sở dữ liệu Firestore

### 7.1 Sơ đồ collections

```
Firestore
├── Users/
│   └── {userId}
│       ├── email       : String
│       ├── name        : String
│       ├── phone       : String
│       ├── address     : String
│       ├── imageUrl    : String (Cloudinary URL)
│       ├── role        : String  "customer" | "store_owner" | "admin"
│       ├── storeId     : String  (chỉ có ở role "store_owner")
│       └── createdAt   : Timestamp
│
├── Stores/
│   └── {storeId}
│       ├── name        : String
│       ├── description : String
│       ├── address     : String
│       ├── phone       : String
│       ├── imageUrl    : String (Cloudinary URL)
│       ├── rating      : Float
│       ├── deliveryTime: String  "15-25 phút"
│       ├── deliveryFee : Long    (VND)
│       ├── isOpen      : Boolean
│       ├── storeOwnerId: String  (UID của chủ quán)
│       └── categoryId  : String  (liên kết với Categories)
│
├── Foods/
│   └── {foodId}
│       ├── title       : String
│       ├── description : String
│       ├── price       : Long    (VND)
│       ├── imageUrl    : String (Cloudinary URL)
│       ├── rating      : Float
│       ├── storeId     : String  (liên kết với Stores)
│       ├── categoryId  : String  (liên kết với Categories)
│       └── isAvailable : Boolean
│
├── Categories/
│   └── {categoryId}
│       ├── name        : String  "Pizza", "Phở", "Burger"...
│       └── imageUrl    : String
│
├── Orders/              ← CHƯA DÙNG (chưa triển khai)
│   └── {orderId}
│       ├── userId      : String
│       ├── storeId     : String
│       ├── storeName   : String
│       ├── address     : String
│       ├── paymentMethod: String "Tiền mặt"
│       ├── note        : String
│       ├── subtotal    : Long
│       ├── deliveryFee : Long
│       ├── total       : Long
│       ├── status      : String  "Đang xử lý" | "Đang giao" | "Hoàn thành" | "Đã hủy"
│       ├── createdAt   : Timestamp
│       └── items[]
│           ├── foodId  : String
│           ├── title   : String
│           ├── price   : Long
│           ├── quantity: Integer
│           └── imageUrl: String
│
└── Favorites/           ← CHƯA DÙNG (chưa triển khai)
    └── {favoriteId}
        ├── userId      : String
        ├── type        : String  "store" | "food"
        ├── itemId      : String  (storeId hoặc foodId)
        ├── name        : String
        └── imageUrl    : String
```

### 7.2 Mối liên kết Java Model ↔ Firestore Collection

| Java Model | Firestore Collection | Trạng thái |
|-----------|---------------------|-----------|
| `User.java` | `Users` | ✅ Đầy đủ |
| `Store.java` | `Stores` | ✅ Đầy đủ |
| `Food.java` | `Foods` | ✅ Đầy đủ |
| `Category.java` | `Categories` | ✅ Đầy đủ |
| `Review.java` | *(không có)* | ⚠️ Mock only |
| `RecommendedFood.java` | *(không có)* | ⚠️ Mock only |
| `CartItem.java` | *(in-memory only)* | ❌ Chưa tạo |
| `Order.java` | `Orders` | ❌ Chưa tạo |
| `Favorite.java` | `Favorites` | ❌ Chưa tạo |

### 7.3 Trạng thái đơn hàng (khi triển khai)

```
"Đang xử lý"  ──►  "Đang giao"  ──►  "Hoàn thành"
      │
      └──► "Đã hủy"   (chỉ từ trạng thái "Đang xử lý")
```

---

## 8. Dependencies (build.gradle.kts)

```kotlin
dependencies {
    // UI cơ bản
    implementation(libs.appcompat)
    implementation(libs.material)                     // Material Design 3
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase
    implementation(platform(libs.firebase.bom))       // Firebase BOM
    implementation(libs.firebase.auth)                // Authentication
    implementation("com.google.firebase:firebase-firestore") // Firestore

    // Glide (load ảnh từ URL)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    // RecyclerView + CardView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // ViewPager2 + CircleIndicator (banner)
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("me.relex:circleindicator:2.1.6")

    // MVVM
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.7")

    // Cloudinary (upload ảnh)
    implementation("com.cloudinary:cloudinary-android:3.0.2")
}
```

### Cấu hình Cloudinary (local.properties)

```properties
cloudinary.cloud_name=YOUR_CLOUD_NAME
cloudinary.upload_preset=foodnow_unsigned
```

Giá trị được inject vào `BuildConfig` tại compile time qua `build.gradle.kts`:
```kotlin
buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"$cloudinaryCloudName\"")
buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"$cloudinaryUploadPreset\"")
```

---

## 9. Tính năng còn thiếu — Roadmap cho nhóm

### Phase A — Giỏ hàng (TV3 phụ trách)

> **Ưu tiên cao nhất** — Nhiều tính năng khác phụ thuộc vào CartManager

1. Tạo `CartItem.java` (model local, không lưu Firestore):
   ```java
   // foodId, title, price, quantity, imageUrl, storeId
   ```
2. Tạo `CartManager.java` (Singleton in-memory):
   - `addItem(CartItem)` — nếu khác quán → hỏi xác nhận xóa giỏ cũ
   - `removeItem(String foodId)`
   - `updateQuantity(String foodId, int qty)`
   - `getItems()` → `List<CartItem>`
   - `getTotalPrice()` → `long`
   - `getStoreId()` → `String`
   - `clear()`
3. Tạo layout `fragment_cart.xml` + `item_cart.xml`
4. Tạo `CartAdapter.java`
5. Implement `CartFragment.java` đầy đủ
6. Kết nối `FoodDetailActivity` → `CartManager.addItem()` khi click "Thêm vào giỏ"
7. Dynamic badge giỏ hàng trong `MainActivity`

### Phase B — Đặt hàng (TV3 phụ trách)

1. Tạo `Order.java` + inner class `OrderItem`:
   ```java
   // userId, storeId, storeName, address, paymentMethod, note,
   // subtotal, deliveryFee, total, status, createdAt, List<OrderItem>
   ```
2. Tạo `OrderRepository.java`:
   - `placeOrder(Order order)` → `Task<DocumentReference>`
   - `getOrdersByUser(String userId)` → `LiveData<List<Order>>`
3. Tạo `CheckoutViewModel.java`
4. Tạo `CheckoutActivity.java` + layout `activity_checkout.xml`
5. CartFragment → nút "Đặt hàng" → mở CheckoutActivity

### Phase C — Lịch sử đơn hàng (TV3 phụ trách)

1. Tạo layout `fragment_orders.xml` + `item_order.xml`
2. Tạo `OrderAdapter.java`
3. Tạo `OrdersViewModel.java`
4. Implement `OrdersFragment.java`

### Phase D — Yêu thích (TV3 phụ trách)

1. Tạo `Favorite.java`:
   ```java
   // userId, type ("store"|"food"), itemId, name, imageUrl
   ```
2. Tạo `FavoriteRepository.java`:
   - `addFavorite(Favorite)` → `Task<DocumentReference>`
   - `removeFavorite(String favoriteId)` → `Task<Void>`
   - `getFavoritesByUser(String userId)` → `LiveData<List<Favorite>>`
   - `isFavorite(String userId, String itemId)` → `LiveData<Boolean>`
3. Tạo `FavoritesViewModel.java`
4. Tạo layout `fragment_favorites.xml` + `item_favorite.xml`
5. Tạo `FavoriteAdapter.java`
6. Implement `FavoritesFragment.java`
7. Kết nối nút yêu thích trong `FoodDetailActivity` với `FavoriteRepository`

### Phase E — Đơn hàng quán (TV3/TV2 phụ trách)

1. Implement `StoreOrdersFragment.java` — hiển thị đơn hàng đến quán
2. Tính năng cập nhật trạng thái đơn hàng (chủ quán)

### Phase F — Cải thiện nhỏ

| Mục | Mô tả |
|-----|-------|
| Recommended foods | Kết nối thật từ Firestore thay vì mock data |
| Đánh giá món | Lưu Review lên Firestore nếu cần |
| Số điểm user | `tvPointsSubtitle` đang hardcoded "0 điểm" |

---

## 10. Hướng dẫn cấu hình & build

### Cấu hình Firebase

1. Tạo project tại [Firebase Console](https://console.firebase.google.com/)
2. Thêm Android app với package: `com.example.foodnow`
3. Tải `google-services.json` → đặt vào `app/`
4. Bật **Authentication** → Email/Password
5. Tạo **Cloud Firestore** database
6. Tạo collections: `Users`, `Stores`, `Foods`, `Categories`, `Orders`, `Favorites`

### Cấu hình Cloudinary

1. Đăng ký tại [cloudinary.com](https://cloudinary.com/)
2. Lấy **Cloud Name** từ Dashboard
3. Tạo **Upload Preset** unsigned tên `foodnow_unsigned`
4. Cập nhật `local.properties`:
   ```properties
   cloudinary.cloud_name=YOUR_CLOUD_NAME
   cloudinary.upload_preset=foodnow_unsigned
   ```

### Build commands

```bash
# Build debug APK
.\gradlew.bat assembleDebug

# Chạy unit tests
.\gradlew.bat testDebugUnitTest

# Chạy lint
.\gradlew.bat lintDebug

# Chạy instrumentation tests (cần thiết bị/emulator)
.\gradlew.bat connectedDebugAndroidTest
```

### Test accounts (Firestore Users collection)

Để test đầy đủ các role, cần tạo 3 tài khoản trong Firebase Auth và Firestore:

```
customer:
  email: customer@test.com
  role: "customer"

store_owner:
  email: owner@test.com
  role: "store_owner"
  storeId: "<id của 1 store trong Stores collection>"

admin:
  email: admin@test.com
  role: "admin"
```

---

*Tài liệu này được tạo tự động bằng cách phân tích toàn bộ codebase. Cập nhật khi có thay đổi lớn.*
