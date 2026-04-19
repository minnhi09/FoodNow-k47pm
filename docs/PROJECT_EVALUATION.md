# Đánh giá dự án FoodNow & Chiến lược phát triển

> Tài liệu đánh giá hiện trạng dự án, chiến lược phát triển Feature-by-Feature, và phân tích khả năng mở rộng.
> Cập nhật: 19/04/2026

---

## 1. Đánh giá hiện trạng dự án

### Tổng quan: ~32-35% hoàn thành

| Thành phần | Trạng thái | Chi tiết |
|---|---|---|
| **Authentication (Login/Register)** | ✅ 100% | Firebase Auth + Firestore, MVVM đầy đủ |
| **User Profile** | ✅ 100% | Xem/sửa thông tin, avatar với Glide |
| **Categories** | ✅ 100% | Model + Repository + Adapter + ViewModel |
| **Home Screen** | ⚠️ 60% | Chỉ có danh mục, thiếu danh sách quán |
| **Stores (Quán ăn)** | 🔴 0% | Chưa có Model, Repository, Adapter, ViewModel |
| **Foods (Món ăn)** | 🔴 0% | Chưa có gì |
| **Cart (Giỏ hàng)** | 🔴 0% | Chỉ có Fragment placeholder |
| **Checkout (Đặt hàng)** | 🔴 0% | Chưa có gì |
| **Orders (Đơn hàng)** | 🔴 0% | Chỉ có Fragment placeholder |
| **Favorites (Yêu thích)** | 🔴 0% | Chỉ có Fragment placeholder |
| **Search** | 🔴 0% | Chưa có gì |

### Thống kê file

| Loại | Đã có | Cần thêm | Tổng |
|---|---|---|---|
| Java files | ~18 | ~22-25 | ~40-43 |
| XML layouts | 6 | ~10 | ~16 |
| Models | 2 (User, Category) | 5 (Store, Food, Order, CartItem, Favorite) | 7 |
| Repositories | 3 | 4 | 7 |
| ViewModels | 3 | 4 | 7 |
| Adapters | 1 | 5-6 | 6-7 |
| Activities | 3 | 2 (StoreDetail, Checkout) | 5 |

### Firestore: 2/6 collections đã implement trong code

- ✅ Users, Categories
- 🔴 Stores, Foods, Orders, Favorites

---

## 2. Chiến lược phát triển: Feature-by-Feature

### Tại sao không chọn "code hết logic trước" hay "thêm Figma trước"?

| Phương án | Ưu điểm | Nhược điểm |
|---|---|---|
| **A: Code logic trước** | Debug dễ | Layout tạm → sửa lại tốn công đôi |
| **B: Figma UI trước** | Nhìn đẹp sớm | Không demo được chức năng |
| **C: Feature-by-Feature ✅** | Không code 2 lần, demo liên tục | Cần kỷ luật theo thứ tự |

### ✅ Phương án đề xuất: Feature-by-Feature

**Nguyên tắc: Mỗi feature = Layout Figma + Model + Repository + ViewModel + Adapter + Fragment/Activity**

Làm xong 1 feature hoàn chỉnh rồi mới chuyển sang feature tiếp. Lý do:

1. **Không phải code 2 lần** — layout đúng từ đầu, logic viết 1 lần
2. **Demo được liên tục** — mỗi sprint xong là có thêm feature chạy được
3. **Dễ chia việc cho nhóm** — mỗi người nhận 1-2 feature
4. **Dễ phát hiện lỗi sớm** — test từng feature ngay khi hoàn thành

---

## 3. Kế hoạch triển khai

### Thứ tự ưu tiên (dựa trên dependency giữa các feature):

#### Phase 1: Nền tảng dữ liệu (PHẢI LÀM TRƯỚC)

- **T1: Models còn thiếu** — Store.java, Food.java, Order.java, CartItem.java, Favorite.java
  - Thêm `role` vào User.java, `storeOwnerId` vào Store.java (chuẩn bị mở rộng)

#### Phase 2: Luồng chính (Core Flow)

- **T2: Store List (Trang chủ)** — Layout Figma + StoreRepository + StoreAdapter + cập nhật HomeFragment
  - _Phụ thuộc: T1_
- **T3: Store Detail + Food List** — Layout Figma + FoodRepository + FoodAdapter + StoreDetailActivity
  - _Phụ thuộc: T1, T2_
- **T4: Cart (Giỏ hàng)** — Layout Figma + CartManager singleton + CartAdapter + CartFragment
  - _Phụ thuộc: T1, T3_
- **T5: Checkout + Order** — Layout Figma + OrderRepository + CheckoutActivity
  - _Phụ thuộc: T1, T4_

#### Phase 3: Tính năng phụ

- **T6: Orders History** — Layout Figma + OrdersViewModel + OrderAdapter + OrdersFragment
  - _Phụ thuộc: T5_
- **T7: Favorites** — Layout Figma + FavoriteRepository + FavoritesViewModel + FavoritesAdapter
  - _Phụ thuộc: T1_

#### Phase 4: Nâng cao

- **T8: Search** — Tìm kiếm quán/món
- **T9: UI Polish** — Banner trang chủ, animations, empty states
- **T10: Image Upload** — Tích hợp CloudinaryHelper vào Profile

### Sơ đồ dependency

```
T1 (Models)
├── T2 (Store List)
│   └── T3 (Store Detail + Food)
│       └── T4 (Cart)
│           └── T5 (Checkout)
│               └── T6 (Orders History)
├── T7 (Favorites)
└── T9 (UI Polish)

T8 (Search)      ← độc lập
T10 (Image Upload) ← độc lập
```

### Gợi ý phân chia nhóm (3-4 người)

| Giai đoạn | Người A | Người B | Người C |
|---|---|---|---|
| Phase 1 | Tất cả cùng tạo Models | | |
| Phase 2 | T2: Store List + Home | T3: StoreDetail + Food | T4: Cart (sau T3) |
| Phase 2 | | | T5: Checkout |
| Phase 3 | T6: Orders History | T7: Favorites | T8: Search |
| Phase 4 | UI Polish | UI Polish | Image Upload |

---

## 4. 🔮 Phân tích khả năng mở rộng

### 4.1 Đánh giá kiến trúc hiện tại

| Yếu tố kiến trúc | Hiện trạng | Ảnh hưởng tới mở rộng |
|---|---|---|
| **MVVM pattern** | ✅ Tốt | Dễ thêm ViewModel/Repository mới cho feature mới |
| **Firebase Auth** | ✅ Tốt | Hỗ trợ Custom Claims cho phân quyền |
| **Firestore** | ✅ Tốt | Thêm collection mới rất dễ, có Security Rules |
| **Single-module app** | ⚠️ Hạn chế | 1 app cho cả khách/nhà hàng/tài xế sẽ phình to |
| **Không có DI** | ⚠️ Hạn chế | Repository tạo bằng `new` → khó mock test, nhưng chấp nhận được |
| **Không có role field** | 🔴 Thiếu | User model chưa có trường `role` → cần thêm |
| **Không có FCM** | 🔴 Thiếu | Chưa có push notification → cần cho driver/store |
| **Không có Room DB** | ⚠️ Thiếu | Không có offline cache → cần nếu muốn hoạt động offline |

### 4.2 Phân tích từng tính năng mở rộng

---

#### 🔸 A. Chức năng quảng cáo (Ads/Promotions)

**Mức độ phức tạp: THẤP (⭐⭐)**

**Cần thêm gì:**

```
Firestore collections mới:
  Banners/
    {bannerId}/
      imageUrl   : "https://..."
      title      : "Giảm 50% Pizza"
      storeId    : "store001"    ← click vào banner → đến quán
      isActive   : true
      startDate  : Timestamp
      endDate    : Timestamp

  Promotions/
    {promoId}/
      code       : "SALE50"
      discount   : 50000         ← giảm cố định VND
      minOrder   : 100000        ← đơn tối thiểu
      storeId    : "store001"    ← hoặc null = áp dụng tất cả
      isActive   : true

Code mới:
  models/Banner.java, Promotion.java
  repositories/BannerRepository.java, PromotionRepository.java
  adapters/BannerAdapter.java (ViewPager2)

Code sửa:
  HomeFragment.java → thêm ViewPager2 hiển thị banner (đã có dependency sẵn!)
  CheckoutActivity.java → thêm ô nhập mã giảm giá
```

**Dùng lại được:** ~80% code hiện tại (ViewPager2, CircleIndicator, Glide đều đã có sẵn trong build.gradle.kts)

---

#### 🔸 B. Phân quyền nhà hàng (Store Owner Dashboard)

**Mức độ phức tạp: TRUNG BÌNH (⭐⭐⭐)**

**Cần thêm gì:**

```
Firestore:
  Users/{userId}/role : "customer" | "store_owner" | "driver"  ← THÊM FIELD

Code mới cần tạo:
  activities/
    StoreOwnerMainActivity.java     ← Dashboard riêng cho store owner
    ManageFoodActivity.java         ← CRUD món ăn
    ManageOrderActivity.java        ← Xử lý đơn hàng (chấp nhận/từ chối)
  fragments/
    StoreOrdersFragment.java        ← Đơn hàng đến
    StoreMenuFragment.java          ← Quản lý thực đơn
    StoreStatsFragment.java         ← Thống kê doanh thu
  viewmodels/
    StoreOwnerViewModel.java
  repositories/
    StoreManagementRepository.java

Layouts: ~6-8 file XML mới
```

**Dùng lại được:** ~60% code hiện tại (User, Store, Food models, AuthRepository, MVVM pattern)

**Thay đổi code hiện có:**

- `User.java` → thêm `String role`
- `AuthRepository.register()` → thêm param `role`
- `LoginActivity.java` → check role → navigate đúng Activity
- `bottom_nav_menu.xml` → tạo thêm 1 menu riêng cho store owner

---

#### 🔸 C. Phân quyền tài xế (Driver App)

**Mức độ phức tạp: CAO (⭐⭐⭐⭐⭐)**

**Cần thêm gì:**

```
Dependencies mới:
  - Google Maps SDK / Google Play Services Location
  - Firebase Cloud Messaging (FCM) cho push notification

Firestore collections mới:
  Deliveries/
    {deliveryId}/
      orderId      : "order001"
      driverId     : "driver001"
      status       : "Đang lấy hàng" | "Đang giao" | "Hoàn thành"
      pickupLocation : GeoPoint
      dropoffLocation : GeoPoint
      currentLocation : GeoPoint  ← real-time update

Code mới cần tạo:
  activities/
    DriverMainActivity.java        ← Dashboard tài xế
    DeliveryMapActivity.java       ← Bản đồ giao hàng (Google Maps)
  fragments/
    AvailableOrdersFragment.java   ← Đơn hàng chờ nhận
    ActiveDeliveryFragment.java    ← Đơn đang giao
    DriverProfileFragment.java     ← Thông tin tài xế
  services/
    LocationTrackingService.java   ← Background service cập nhật vị trí
    FCMService.java                ← Nhận push notification
  models/
    Delivery.java
  repositories/
    DeliveryRepository.java
  viewmodels/
    DriverViewModel.java

Layouts: ~8-10 file XML mới
```

**Dùng lại được:** ~30% code hiện tại (Auth, Order model, MVVM pattern)

**Tại sao phức tạp:**

1. **Google Maps SDK** — dependency mới, cần API key, học cách dùng MapView
2. **Real-time location** — cần FusedLocationProvider + background service
3. **Push notification (FCM)** — cần setup FCM server key, tạo FCMService
4. **GeoPoint trong Firestore** — kiểu dữ liệu mới chưa dùng trong project
5. **Background Service** — cần xử lý lifecycle, battery optimization

**Đề xuất:** Nên cân nhắc **tách thành app Android riêng**, dùng chung Firestore.

---

### 4.3 Tổng hợp mức độ phức tạp

| Tính năng mở rộng | Độ phức tạp | Dùng lại code cũ | Cần thêm SDK mới |
|---|---|---|---|
| **Quảng cáo/Banner** | ⭐⭐ Thấp | ~80% | Không |
| **Phân quyền nhà hàng** | ⭐⭐⭐ Trung bình | ~60% | Không |
| **Phân quyền tài xế** | ⭐⭐⭐⭐⭐ Cao | ~30% | Maps, FCM, Location |

### 4.4 Kết luận

**KHÔNG quá phức tạp, nhưng có điều kiện.**

**✅ Kiến trúc hiện tại CÓ khả năng mở rộng tốt:**

1. **MVVM pattern** cho phép thêm feature mới mà **không phá code cũ**
2. **Firestore** rất linh hoạt — thêm collection mới không ảnh hưởng collection cũ
3. **Repository pattern** giúp tách biệt data layer — thêm repository mới rất dễ
4. **Dependencies đã chuẩn bị sẵn** — ViewPager2, CircleIndicator, Glide đều đã có

**🔴 Điều CÓ THỂ phức tạp:**

1. **Driver app** cần SDK mới (Maps, FCM, Location) → nên tách app riêng
2. **Nếu 3 role dùng chung 1 app** → conditional navigation phức tạp, app size lớn
3. **Firestore Security Rules** — cần viết rules phân quyền

---

### 4.5 Chiến lược mở rộng đề xuất

```
Kế hoạch hiện tại (10 tasks)
        │
        │ Hoàn thành MVP cho customer
        ▼
    Phase 5A: Quảng cáo/Banner (DỄ — làm ngay trong app hiện tại)
        │
        ▼
    Phase 5B: Store Owner Dashboard (TRUNG BÌNH — thêm Activity set mới)
        │
        ▼
    Phase 5C: Driver App (KHÓ — nên tách project riêng, dùng chung Firestore)
```

**Cách tổ chức nếu mở rộng thành 3 role:**

```
Phương án 1: 1 app, 3 navigation flow (đơn giản hơn cho university project)
  └── Login → check role → 3 Activity khác nhau (CustomerMain, StoreOwnerMain, DriverMain)

Phương án 2: 3 app riêng, chung Firestore (chuyên nghiệp hơn)
  ├── FoodNow (customer)       ← app hiện tại
  ├── FoodNow Store (owner)    ← app mới
  └── FoodNow Driver (driver)  ← app mới
```

**Đề xuất cho university project:** Phương án 1 (1 app) vì đơn giản hơn và dễ demo.

---

## 5. Hành động chuẩn bị mở rộng (làm trong kế hoạch hiện tại)

> Những thay đổi nhỏ giúp mở rộng DỄ HƠN sau này:

| # | Hành động | Thêm vào Task | Effort thêm |
|---|---|---|---|
| 1 | Thêm `String role` vào `User.java` | T1 (Models) | +2 phút |
| 2 | Thêm `userData.put("role", "customer")` vào `AuthRepository.register()` | T1 (Models) | +1 phút |
| 3 | Tách `navigateByRole()` trong LoginActivity | T1 (Models) | +5 phút |
| 4 | Thêm `String storeOwnerId` vào `Store.java` | T1 (Models) | +2 phút |
| 5 | Comment TODO ở những chỗ cần mở rộng | Mọi task | +0 |

**Tổng effort thêm: ~10 phút**, nhưng giúp tiết kiệm hàng giờ khi mở rộng sau.

### Code mẫu chuẩn bị:

```java
// User.java — thêm 1 field
private String role; // "customer" (default), "store_owner", "driver"
public String getRole() { return role; }
public void setRole(String role) { this.role = role; }
```

```java
// AuthRepository.register() — thêm 1 dòng
userData.put("role", "customer"); // default role
```

```java
// LoginActivity — tách method navigation
private void navigateByRole(String role) {
    // TODO: Mở rộng thêm role khác
    // if ("store_owner".equals(role)) → StoreOwnerMainActivity
    // if ("driver".equals(role)) → DriverMainActivity
    startActivity(new Intent(this, MainActivity.class));
    finish();
}
```
