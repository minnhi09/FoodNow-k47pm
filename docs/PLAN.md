# Đánh giá dự án FoodNow & Chiến lược phát triển

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

## 2. Phân tích: Code tính năng trước vs. Thêm giao diện Figma trước

### ✅ Phương án C (ĐỀ XUẤT): Làm xen kẽ theo từng feature — "Feature-by-Feature"

**Nguyên tắc: Mỗi feature = Layout Figma + Model + Repository + ViewModel + Adapter + Fragment/Activity**

Làm xong 1 feature hoàn chỉnh rồi mới chuyển sang feature tiếp. Lý do:
1. **Không phải code 2 lần** — layout đúng từ đầu, logic viết 1 lần
2. **Demo được liên tục** — mỗi sprint xong là có thêm feature chạy được
3. **Dễ chia việc cho nhóm** — mỗi người nhận 1-2 feature
4. **Dễ phát hiện lỗi sớm** — test từng feature ngay khi hoàn thành

---

## 3. Kế hoạch triển khai theo Feature-by-Feature

### Thứ tự ưu tiên (dựa trên dependency giữa các feature):

#### Phase 1: Nền tảng dữ liệu (PHẢI LÀM TRƯỚC)
- **T1: Models còn thiếu** — Store.java, Food.java, Order.java, CartItem.java, Favorite.java

#### Phase 2: Luồng chính (Core Flow)
- **T2: Store List (Trang chủ)** — Layout Figma + StoreRepository + StoreAdapter + cập nhật HomeFragment
- **T3: Store Detail + Food List** — Layout Figma + FoodRepository + FoodAdapter + StoreDetailActivity
- **T4: Cart (Giỏ hàng)** — Layout Figma + CartManager singleton + CartAdapter + CartFragment
- **T5: Checkout + Order** — Layout Figma + OrderRepository + CheckoutActivity

#### Phase 3: Tính năng phụ
- **T6: Orders History** — Layout Figma + OrdersViewModel + OrderAdapter + OrdersFragment
- **T7: Favorites** — Layout Figma + FavoriteRepository + FavoritesViewModel + FavoritesAdapter

#### Phase 4: Nâng cao
- **T8: Search** — Tìm kiếm quán/món
- **T9: UI Polish** — Banner trang chủ, animations, empty states
- **T10: Image Upload** — Tích hợp CloudinaryHelper vào Profile

---

## 4. 🔮 Phân tích khả năng mở rộng sau khi hoàn thành kế hoạch hiện tại

### 4.1 Đánh giá kiến trúc hiện tại cho việc mở rộng

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

#### 🔸 A. Phân quyền nhà hàng (Store Owner Dashboard)

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

**Những gì DÙNG LẠI được từ code hiện tại:**
- ✅ User model (chỉ thêm field `role`)
- ✅ AuthRepository (giữ nguyên login/register, thêm check role)
- ✅ Store model, Food model (giữ nguyên)
- ✅ OrderRepository (mở rộng thêm method cho store owner)
- ✅ Toàn bộ MVVM pattern, Firestore integration

**Thay đổi code hiện có:**
- `User.java` → thêm `String role` (field + getter/setter)
- `AuthRepository.register()` → thêm param `role`, lưu vào Firestore
- `LoginActivity.java` → sau login, check role → navigate đến đúng MainActivity hoặc StoreOwnerMainActivity
- `bottom_nav_menu.xml` → tạo thêm 1 menu riêng cho store owner

**Đánh giá:** Khá dễ vì MVVM pattern cho phép thêm ViewModel/Repository mới độc lập. Phần khó nhất là **conditional navigation** sau login (đọc role → chuyển đúng Activity).

---

#### 🔸 B. Phân quyền tài xế (Driver App)

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

**Những gì DÙNG LẠI được:**
- ✅ User model (thêm role = "driver")
- ✅ AuthRepository, AuthViewModel
- ✅ Order model (đọc thông tin đơn hàng)
- ✅ MVVM pattern, Firebase infrastructure

**Tại sao phức tạp:**
1. **Google Maps SDK** — dependency mới, cần API key, học cách dùng MapView
2. **Real-time location** — cần FusedLocationProvider + background service
3. **Push notification (FCM)** — cần setup FCM server key, tạo FCMService
4. **GeoPoint trong Firestore** — kiểu dữ liệu mới chưa dùng trong project
5. **Background Service** — cần xử lý lifecycle, battery optimization

**Đánh giá:** Đây là feature PHỨC TẠP NHẤT vì cần nhiều SDK mới (Maps, Location, FCM) và kiến thức Android nâng cao (Service, foreground notification). Nên cân nhắc **tách thành app riêng** cho driver.

---

#### 🔸 C. Chức năng quảng cáo (Ads/Promotions)

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

**Những gì DÙNG LẠI được:**
- ✅ Toàn bộ MVVM infrastructure
- ✅ ViewPager2 dependency (đã có trong build.gradle.kts!)
- ✅ CircleIndicator dependency (đã có!)
- ✅ Glide (load ảnh banner)
- ✅ HomeFragment (chỉ thêm ViewPager2 vào layout)

**Đánh giá:** Đây là feature DỄ NHẤT để mở rộng. Build.gradle.kts **đã có sẵn** ViewPager2 + CircleIndicator. Chỉ cần thêm 2-3 model mới + 1 adapter + sửa HomeFragment.

---

### 4.3 Tổng hợp: Mức độ phức tạp khi mở rộng

| Tính năng mở rộng | Độ phức tạp | Dùng lại code cũ | Cần thêm SDK mới | Thời gian ước lượng |
|---|---|---|---|---|
| **Quảng cáo/Banner** | ⭐⭐ Thấp | ~80% | Không | Ngắn |
| **Phân quyền nhà hàng** | ⭐⭐⭐ Trung bình | ~60% | Không | Trung bình |
| **Phân quyền tài xế** | ⭐⭐⭐⭐⭐ Cao | ~30% | Maps, FCM, Location | Dài |

### 4.4 Kết luận: Có phức tạp không?

**Trả lời ngắn: KHÔNG quá phức tạp, nhưng có điều kiện.**

**✅ Tin tốt — Kiến trúc hiện tại CÓ khả năng mở rộng tốt:**
1. **MVVM pattern** cho phép thêm feature mới mà **không phá code cũ** — chỉ cần tạo thêm ViewModel/Repository/Fragment mới
2. **Firestore** rất linh hoạt — thêm collection mới không ảnh hưởng collection cũ
3. **Repository pattern** giúp tách biệt data layer — thêm repository mới rất dễ
4. **Dependencies đã chuẩn bị sẵn** — ViewPager2, CircleIndicator, Glide đều đã có

**⚠️ Điều cần LÀM NGAY trong kế hoạch hiện tại để mở rộng dễ hơn sau này:**

1. **Thêm field `role` vào User model ngay từ Phase 1** (gần như 0 effort thêm)
   ```java
   // User.java — thêm 1 field
   private String role; // "customer" (default), "store_owner", "driver"
   ```
   
2. **Thêm field `role` khi register** (sửa 1 dòng trong AuthRepository)
   ```java
   userData.put("role", "customer"); // default role
   ```

3. **Tách logic navigation** trong LoginActivity thành method riêng
   ```java
   private void navigateByRole(String role) {
       // Sau này dễ thêm: if "store_owner" → StoreOwnerMainActivity
   }
   ```

**🔴 Điều CÓ THỂ phức tạp:**
1. **Driver app** cần SDK mới (Maps, FCM, Location) → nên xem xét tách thành app Android riêng
2. **Nếu 3 role dùng chung 1 app** → conditional navigation phức tạp, app size lớn
3. **Firestore Security Rules** — cần viết rules phân quyền (hiện có thể chưa có)

---

### 4.5 Khuyến nghị chiến lược mở rộng

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

## 5. Hành động cần làm NGAY để chuẩn bị mở rộng

> Những thay đổi nhỏ trong kế hoạch 10 tasks hiện tại, giúp mở rộng DỄ HƠN sau này:

| # | Hành động | Thêm vào Task | Effort thêm |
|---|---|---|---|
| 1 | Thêm `String role` vào `User.java` | T1 (Models) | +2 phút |
| 2 | Thêm `userData.put("role", "customer")` vào `AuthRepository.register()` | T1 (Models) | +1 phút |
| 3 | Tách `navigateByRole()` trong LoginActivity | T1 (Models) | +5 phút |
| 4 | Thêm `String storeOwnerId` vào `Store.java` | T1 (Models) | +2 phút |
| 5 | Comment TODO ở những chỗ cần mở rộng | Mọi task | +0 |

**Tổng effort thêm: ~10 phút**, nhưng giúp tiết kiệm hàng giờ khi mở rộng sau.

---

## 6. Plan cho yeu cau moi: Huong dan newbie de tu code

### Problem
Nguoi dung muon tu code de hieu sau, can 1 file markdown huong dan chi tiet cho "tinh nang tiep theo" trong plan.

### Feature duoc chon
**T2 - Store List tren Home** (kem buoc tien de nho: tao `Store.java`).

### Ly do chon T2
1. Day la feature core, co ket qua UI ro rang, de hoc nhat cho newbie.
2. Di qua day du luong MVVM (Model -> Repository -> ViewModel -> Adapter -> Fragment/Layout).
3. Tao nen cho T3 (Store Detail + Food List).

### Todo cho deliverable nay
1. Viet file huong dan cho nguoi moi, theo tung buoc rat cu the.
2. Bao gom file can tao/sua, code flow, checklist, loi thuong gap.
3. Dat file trong `docs/` de user doc va tu implement.

### Dau ra
- `docs/T2_STORE_LIST_NEWBIE_GUIDE.md`

### Notes
- Tai lieu uu tien "de tu code", khong copy day du code final.
- Giu dung conventions cua du an (Java + XML + MVVM + Firestore).

---

## 7. Plan cho yeu cau moi: Tao giao dien Trang chu giong Figma

### Problem
Can tao lai UI Trang chu theo 2 anh `trang-chu-1.png` va `trang-chu-2.png`.

### Scope da chot voi user
1. Tao UI HomeFragment giong bo cuc trong anh.
2. Doi bottom navigation thanh 5 tab giong anh (co tab `Gio hang`).
3. Phan `Mon an de xuat` dung du lieu mock cung de khop UI truoc.

### Cac thanh phan UI can co sau khi xong
1. Header mau cam (dia chi giao den + icon gio hang co badge) + o tim kiem.
2. Card `Don hang cua ban` nhu mockup.
3. Danh muc ngang theo style icon o nen rounded.
4. Section `Quan an pho bien` co row title + `Xem tat ca`.
5. Danh sach quan dang doc (tai su dung StoreAdapter + item_store, style theo Figma).
6. Section `Mon an de xuat` dang luoi 2 cot (RecyclerView grid) bang mock data.
7. Bottom nav 5 tab: Trang chu / Gio hang / Don hang / Yeu thich / Tai khoan.

### File du kien thay doi
- `app/src/main/res/layout/fragment_home.xml`
- `app/src/main/java/com/example/foodnow/fragments/HomeFragment.java`
- `app/src/main/java/com/example/foodnow/adapters/CategoryAdapter.java` (neu can doi style bind)
- `app/src/main/res/layout/item_category.xml`
- `app/src/main/java/com/example/foodnow/adapters/StoreAdapter.java`
- `app/src/main/res/layout/item_store.xml`
- `app/src/main/res/menu/bottom_nav_menu.xml`
- `app/src/main/java/com/example/foodnow/MainActivity.java`
- `app/src/main/res/layout/activity_main.xml` (style bottom nav)
- `app/src/main/res/values/colors.xml` + tao color selectors cho nav/icon/text
- `app/src/main/res/drawable/*` (shape/bg/icon theo UI moi)

### File du kien tao moi
- `app/src/main/java/com/example/foodnow/models/RecommendedFood.java` (UI model local)
- `app/src/main/java/com/example/foodnow/adapters/RecommendedFoodAdapter.java`
- `app/src/main/res/layout/item_recommended_food.xml`
- Drawable support cho badge/chip/card bo goc/plus button/section icon (neu thieu)

### Ke hoach implementation
1. **Dung skeleton layout Home moi**: dung NestedScrollView + cac block theo thu tu screenshot.
2. **Hoan thien section data hien co**:
   - Category list (ngang) style lai theo mockup.
   - Store list (doc) style lai item theo mockup.
3. **Them section Mon an de xuat**:
   - Tao model + adapter + item layout.
   - Dua mock data tai HomeFragment.
   - Render bang RecyclerView GridLayoutManager(2 cot), nested scroll off.
4. **Cap nhat bottom nav 5 tab**:
   - Them `nav_cart` vao menu.
   - Noi `nav_cart` -> `CartFragment` trong MainActivity.
   - Chinh mau active/inactive + label/icon size cho giong Figma.
5. **Polish va consistency**:
   - Chinh spacing, corner radius, text hierarchy, icon tint.
   - Dam bao home van load du lieu categories/stores tu ViewModel nhu hien tai.
6. **Build/lint/test de xac nhan tich hop on dinh**.

### Decisions quan trong
1. Chua implement business logic moi cho cart/order trong request nay; chi can UI home + dieu huong tab.
2. `Mon an de xuat` dung mock data local, khong doi schema Firestore.
3. Giu code MVVM hien co cho Categories/Stores, khong dua logic UI vao Repository.

### Progress cap nhat
- [x] Dung lai `fragment_home.xml` theo bo cuc Figma: header, search, order card, section quán và món đề xuất.
- [x] Cap nhat style `item_category.xml` + `item_store.xml` va adapter tuong ung.
- [x] Them `RecommendedFood` + `RecommendedFoodAdapter` + `item_recommended_food.xml` va mock data trong `HomeFragment`.
- [x] Doi bottom nav thanh 5 tab (co `Giỏ hàng`), cap nhat `MainActivity` va badge cart.
- [x] Bo sung color/drawable/icon resources phuc vu giao dien moi.
- [x] Build/test/lint pass sau khi thay doi.

---

## 8. Plan cho yeu cau moi: Ket noi Cloudinary de app co anh

### Problem
Can ket noi Cloudinary theo cach **unsigned upload preset** de co anh cho 3 nhom: `Profile`, `Stores`, `Foods`.

### Hien trang code
1. Da co dependency Cloudinary trong `app/build.gradle.kts`.
2. Da co `CloudinaryHelper.java`, nhung:
   - chua thay `YOUR_CLOUD_NAME`,
   - chua duoc goi `init(...)` o diem khoi dong app,
   - chua duoc noi vao UI upload (Profile/Store/Food).
3. `ProfileFragment` moi hien `imageUrl` bang Glide, chua co pick anh + upload.
4. Luong tao/sua Store-Food chua hoan thien (chua co StoreDetail/Checkout/quan ly mon).

### Muc tieu
1. Co anh hien thi ngay trong app (Users/Stores/Foods deu co `imageUrl` hop le).
2. Co luong upload anh trong app cho Profile truoc.
3. Chuan bi san helper tai su dung cho Store/Food khi UI quan ly du lieu duoc mo rong.

### Approach tong quan (2 duong song song)
1. **Nhanh de "co anh ngay":** upload anh len Cloudinary Dashboard -> copy URL -> ghi vao Firestore (`imageUrl`).
2. **Dung trong app:** pick anh tu may -> upload Cloudinary unsigned -> nhan `secure_url` -> update Firestore.

### Ke hoach chi tiet

#### Phase A - Cloudinary setup dung chuan
1. Tao unsigned preset `foodnow_unsigned` tren Cloudinary (folder `foodnow/*`, gioi hanh format jpg/png/webp, gioi hanh size).
2. Sua `CloudinaryHelper.init(...)`:
   - dung cloud name that,
   - bo comment nham ve `api_key/api_secret` (unsigned khong can key/secret tren mobile).
3. Goi `CloudinaryHelper.init(getApplicationContext())` 1 lan tai `MainActivity` (hoac `Application` class neu ban them sau).

#### Phase B - Co anh ngay cho app hien tai
1. Upload lo anh mau tren Cloudinary Dashboard.
2. Lay `secure_url`.
3. Ghi vao Firestore:
   - `Users/{uid}.imageUrl`
   - `Stores/{storeId}.imageUrl`
   - `Foods/{foodId}.imageUrl`
4. Ket qua:
   - Home (store image) va Profile (avatar) hien anh ngay ma khong doi den luong upload UI.

#### Phase C - Upload avatar trong Profile (implement truoc)
1. `fragment_profile.xml`: them nut/overlay "doi anh" tren avatar.
2. `ProfileFragment.java`:
   - dung `ActivityResultLauncher<String>` voi `GetContent` (`image/*`) de chon anh.
   - goi `CloudinaryHelper.uploadImage(uri, callback)`.
   - trong `onSuccess`, lay `secure_url` va goi `profileViewModel.updateUser(...)` set `imageUrl`.
   - them loading state + thong bao loi ro rang.
3. Kiem tra avatar tu Glide cap nhat real-time qua snapshot listener.

#### Phase D - Chuan hoa de mo rong Store/Food
1. Tao helper dung chung (vi du `ImageUploadCoordinator`) tra ve callback URL, de Profile/Store/Food tai su dung.
2. Khi co man tao/sua Store-Food (T3+):
   - chon anh -> upload -> save `imageUrl` vao document `Stores`/`Foods`.
3. Tranh duplicate code upload callback giua cac man hinh.

### File du kien thay doi
- `app/src/main/java/com/example/foodnow/utils/CloudinaryHelper.java`
- `app/src/main/java/com/example/foodnow/MainActivity.java` (hoac them `FoodNowApplication.java` + manifest)
- `app/src/main/res/layout/fragment_profile.xml`
- `app/src/main/java/com/example/foodnow/fragments/ProfileFragment.java`
- (tuong lai) man tao/sua Store/Food khi T3/T4 mo rong

### Security notes (unsigned)
1. Khong hardcode `api_secret` trong app mobile.
2. Preset unsigned phai bi khoa chat:
   - folder co dinh,
   - loai file hop le,
   - gioi han kich thuoc,
   - neu can thi moderation/manual approve.
3. Neu sau nay production, can chuyen signed upload qua backend.

### Definition of Done
1. Cloudinary init thanh cong, upload callback nhan `secure_url`.
2. Profile co the chon anh va luu avatar URL vao Firestore.
3. Store/Food co `imageUrl` hop le nen UI hien anh on dinh.
4. Khong co secret Cloudinary nhay cam trong source code.
