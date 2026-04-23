# Kế hoạch Mở rộng Role: `store_owner` (Chủ Nhà Hàng)

> **Mục tiêu:** Thêm vai trò `store_owner` vào FoodNow, cho phép chủ nhà hàng đăng nhập và quản lý quán, thực đơn, xem đơn hàng — tách biệt hoàn toàn với trải nghiệm của khách hàng.

---

## 1. Tổng quan 3 vai trò

```
┌──────────────┬─────────────────────────────────────────────────────┐
│    Role      │  Mô tả                                               │
├──────────────┼─────────────────────────────────────────────────────┤
│ customer     │ Khách hàng: xem quán, xem món, đặt hàng, yêu thích  │
│ store_owner  │ Chủ quán: quản lý quán + thực đơn + xem đơn hàng   │
│ admin        │ Quản trị viên: quản lý ảnh Cloudinary, toàn quyền   │
└──────────────┴─────────────────────────────────────────────────────┘
```

### Bảng quyền chi tiết

| Chức năng                     | customer | store_owner | admin |
|-------------------------------|:--------:|:-----------:|:-----:|
| Xem danh sách quán ăn         | ✅       | ❌          | ✅    |
| Xem chi tiết quán / món ăn    | ✅       | ❌          | ✅    |
| Tìm kiếm + lọc quán           | ✅       | ❌          | ✅    |
| Đặt hàng                      | ✅       | ❌          | ✅    |
| Yêu thích quán/món             | ✅       | ❌          | ✅    |
| **Xem thông tin quán của mình** | ❌      | ✅          | ❌    |
| **Chỉnh sửa thông tin quán**   | ❌       | ✅          | ❌    |
| **Thêm / sửa / xóa món ăn**   | ❌       | ✅          | ❌    |
| **Xem đơn hàng đến quán**     | ❌       | ✅          | ❌    |
| **Cập nhật trạng thái đơn**   | ❌       | ✅ (phase 2)| ❌    |
| Quản lý ảnh Cloudinary        | ❌       | ❌          | ✅    |
| Quản lý profile cá nhân       | ✅       | ✅          | ✅    |

---

## 2. Thay đổi Database (Firestore)

### Collection `Users` — thêm 2 field mới

```
Users/
  {userId}/
    email        : "owner@gmail.com"
    name         : "Nguyễn Văn Chủ"
    phone        : "0901234567"
    address      : "..."
    imageUrl     : "https://..."
    role         : "store_owner"       ← thêm giá trị mới
    storeId      : "store_abc123"      ← ← FIELD MỚI — liên kết tới Stores/{storeId}
    createdAt    : Timestamp
```

> **Cách gán:** Admin vào Firestore Console → collection `Users` → tìm tài khoản chủ quán → sửa:
> - `role` = `"store_owner"`
> - `storeId` = `"<id của quán trong Stores collection>"`

### Collection `Stores` — không đổi

Giữ nguyên schema hiện tại. Field `storeId` trong User trỏ tới document ID của Stores.

---

## 3. Kiến trúc điều hướng sau Login

```
LoginActivity
    │
    ▼ (1) Firebase Auth login thành công
    │
    ▼ (2) Fetch Firestore Users/{uid} → lấy User object
    │
    ├── role = "customer"    ──→  MainActivity      (Trang chủ / Đơn hàng / Yêu thích / Tài khoản)
    ├── role = "admin"       ──→  MainActivity      (+ nút Quản trị ảnh trong Profile)
    └── role = "store_owner" ──→  StoreOwnerActivity (Dashboard / Thực đơn / Đơn hàng / Hồ sơ)
```

### Tại sao cần fetch Firestore sau Auth?
Firebase Auth chỉ trả về `FirebaseUser` (email, uid) — không chứa `role`. Cần thêm bước
đọc `Users/{uid}` để lấy role + storeId trước khi navigate.

---

## 4. Cấu trúc thư mục (code mới)

```
com/example/foodnow/
├── activities/
│   ├── StoreOwnerActivity.java       ← Shell + BottomNav cho chủ quán
│   └── AddEditFoodActivity.java      ← Form thêm/sửa 1 món ăn
│
├── fragments/
│   ├── StoreOwnerDashboardFragment.java  ← Màn hình dashboard quán
│   ├── ManageFoodsFragment.java          ← Danh sách + CRUD món ăn
│   └── StoreOrdersFragment.java          ← Đơn hàng đến quán (placeholder → real)
│
└── viewmodels/
    └── StoreOwnerViewModel.java      ← LiveData: store info + foods + CRUD

res/
├── layout/
│   ├── activity_store_owner.xml
│   ├── fragment_store_owner_dashboard.xml
│   ├── fragment_manage_foods.xml
│   ├── activity_add_edit_food.xml
│   └── item_manage_food.xml          ← item trong danh sách quản lý
└── menu/
    └── store_owner_nav_menu.xml      ← 4 tab bottom nav
```

---

## 5. Chi tiết từng màn hình

### 5.1 StoreOwnerActivity (shell)

**Chức năng:** Container chính cho chủ quán, tương tự `MainActivity` nhưng có tab khác.

```
┌─────────────────────────────┐
│  [Tiêu đề tab hiện tại]     │  ← Toolbar
├─────────────────────────────┤
│                             │
│    [Fragment container]     │  ← fragment_container
│                             │
├─────────────────────────────┤
│ 🏪Dashboard │ 🍔Thực đơn │ 📦Đơn hàng │ 👤Hồ sơ │
└─────────────────────────────┘
```

```java
// Logic routing tab
R.id.nav_dashboard  → StoreOwnerDashboardFragment
R.id.nav_foods      → ManageFoodsFragment
R.id.nav_orders     → StoreOrdersFragment
R.id.nav_profile    → ProfileFragment  // Dùng lại, không tạo mới
```

---

### 5.2 StoreOwnerDashboardFragment (Dashboard)

**Chức năng:** Tổng quan thông tin quán. Chủ quán nhìn vào biết ngay tình trạng.

```
┌─────────────────────────────┐
│  [Ảnh quán banner 200dp]    │
│  [Nút đổi ảnh quán]  [admin]│
├─────────────────────────────┤
│ 🏪 Phở Hà Nội               │  ← tên quán (bold, 22sp)
│ ⭐ 4.8  ·  20-30 phút       │  ← rating + thời gian
│ 📍 45 Nguyễn Chí Thanh      │  ← địa chỉ
├─────────────────────────────┤
│ [Card Thống kê nhanh]       │
│  🍔 12 món  │  📦 5 đơn hôm nay │
├─────────────────────────────┤
│ [Nút "Sửa thông tin quán"]  │  ← mở form edit store
│ [Nút "Xem đơn hàng"]        │  ← navigate sang tab Đơn hàng
└─────────────────────────────┘
```

**Logic code:**
```java
// Nhận storeId từ Intent (được truyền khi StoreOwnerActivity khởi tạo)
String storeId = requireActivity().getIntent().getStringExtra("storeId");
viewModel.loadStore(storeId).observe(...);
viewModel.loadFoodCount(storeId).observe(...);  // đếm số món
```

---

### 5.3 ManageFoodsFragment (Quản lý thực đơn)

**Chức năng:** Danh sách tất cả món của quán. Thêm / sửa / xóa.

```
┌─────────────────────────────┐
│ 🔍 Tìm món...               │  ← search trong thực đơn
├─────────────────────────────┤
│ [item] 🖼 Phở Bò Đặc Biệt  │
│        65.000đ  ⭐4.8  ✏️🗑  │
│ [item] 🖼 Bún Bò Huế        │
│        55.000đ  ⭐4.6  ✏️🗑  │
│ ...                         │
├─────────────────────────────┤
│           [+] Thêm món mới  │  ← FAB hoặc Button
└─────────────────────────────┘
```

**Logic code:**
- Load foods từ `storeId` (real-time listener — FoodRepository đã có)
- Click ✏️ → `startActivityForResult(AddEditFoodActivity, foodId=...)`
- Click 🗑 → `AlertDialog xác nhận` → `viewModel.deleteFood(foodId)`
- Click FAB → `startActivityForResult(AddEditFoodActivity, foodId=null)`

---

### 5.4 AddEditFoodActivity (Form thêm/sửa)

**Chức năng:** Form nhập/sửa thông tin 1 món ăn.

```
┌─────────────────────────────┐
│ ← [Thêm món / Sửa món]     │  ← Toolbar với title động
├─────────────────────────────┤
│ [Ảnh món ăn - click để đổi]│  ← Cloudinary upload
│ Tên món *                   │
│ [________________________] │
│ Mô tả                       │
│ [________________________] │
│ Giá (đ) *                   │
│ [________________________] │
│ Phân loại                   │
│ [Dropdown - categoryId]     │  ← Spinner từ Categories
│                             │
│ [ ] Còn hàng (isAvailable)  │  ← CheckBox
│                             │
│    [Hủy]      [Lưu]         │
└─────────────────────────────┘
```

**Logic code (2 chế độ):**
```java
// Nhận intent extra
String foodId = getIntent().getStringExtra("foodId");  // null = Add mode

if (foodId != null) {
    // Edit mode: fetch food từ Firestore → pre-fill form
    setTitle("Sửa món ăn");
    viewModel.getFood(foodId).observe(this, food -> fillForm(food));
} else {
    // Add mode: form trống
    setTitle("Thêm món mới");
}

// Khi bấm Lưu:
Food food = buildFoodFromForm();
food.setStoreId(storeId);  // storeId từ SharedPreferences / Intent
if (foodId != null) {
    viewModel.updateFood(foodId, food);
} else {
    viewModel.addFood(food);
}
```

---

### 5.5 StoreOrdersFragment (Đơn hàng đến quán)

**Phase 1 (placeholder đẹp):**
```
┌─────────────────────────────┐
│  📦 Đơn hàng đến quán       │
│                             │
│  [Icon lớn]                 │
│  Tính năng đang phát triển  │
│  TV3 sẽ hoàn thiện sớm      │
└─────────────────────────────┘
```

**Phase 2 (khi TV3 có Orders):**
- Query `Orders` where `storeId == myStoreId`
- Hiển thị list đơn hàng theo status
- Nút "Xác nhận" / "Đang giao" / "Hoàn thành"

---

## 6. StoreOwnerViewModel — Logic trung tâm

```java
public class StoreOwnerViewModel extends ViewModel {

    private final StoreRepository storeRepo = new StoreRepository();
    private final FoodRepository foodRepo = new FoodRepository();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Load thông tin quán theo storeId
    public LiveData<Store> loadStore(String storeId) { ... }

    // Load danh sách món theo storeId (real-time)
    public LiveData<List<Food>> loadFoods(String storeId) { ... }

    // Thêm món mới vào Firestore
    public Task<DocumentReference> addFood(Food food) {
        return db.collection("Foods").add(food);
    }

    // Sửa món ăn
    public Task<Void> updateFood(String foodId, Map<String, Object> updates) {
        return db.collection("Foods").document(foodId).update(updates);
    }

    // Xóa món ăn
    public Task<Void> deleteFood(String foodId) {
        return db.collection("Foods").document(foodId).delete();
    }
}
```

---

## 7. Thay đổi AuthViewModel + LoginActivity

### AuthViewModel — thêm userProfileLiveData

```java
// Sau khi login thành công, fetch profile từ Firestore
private final MutableLiveData<User> userProfileLiveData = new MutableLiveData<>();

public void login(String email, String password) {
    authRepository.login(email, password)
        .addOnSuccessListener(result -> {
            userLiveData.setValue(result.getUser());
            // Fetch thêm Firestore profile để lấy role
            String uid = result.getUser().getUid();
            authRepository.getUserProfile(uid)
                .addOnSuccessListener(user -> userProfileLiveData.setValue(user));
        });
}
```

### LoginActivity — route theo role

```java
authViewModel.getUserProfileLiveData().observe(this, user -> {
    if (user == null) return;
    if ("store_owner".equals(user.getRole())) {
        Intent intent = new Intent(this, StoreOwnerActivity.class);
        intent.putExtra("storeId", user.getStoreId());  // truyền storeId sang
        startActivity(intent);
    } else {
        startActivity(new Intent(this, MainActivity.class));
    }
    finish();
});
```

---

## 8. Thứ tự thực hiện đề xuất

```
Phase 1 — Routing (bắt buộc, làm trước)
  ① User.java — thêm storeId
  ② AuthRepository — getUserProfile()
  ③ AuthViewModel — userProfileLiveData
  ④ LoginActivity — observe + route by role
  ⑤ StoreOwnerActivity — shell + bottom nav

Phase 2 — Dashboard
  ⑥ StoreOwnerDashboardFragment + layout
  ⑦ StoreOwnerViewModel (loadStore, loadFoods)

Phase 3 — Quản lý thực đơn
  ⑧ ManageFoodsFragment + item_manage_food.xml
  ⑨ AddEditFoodActivity + layout + Categories Spinner

Phase 4 — Hoàn thiện
  ⑩ StoreOrdersFragment placeholder
  ⑪ AndroidManifest (đăng ký activity mới)
  ⑫ Build + verify
  ⑬ Hướng dẫn newbie
```

---

## 9. Điểm cần lưu ý khi code

### 9.1 Loading state khi fetch role

```
LoginActivity:
  [1] User bấm Đăng nhập
  [2] Firebase Auth (async ~500ms) → thành công
  [3] Firestore fetch profile (async ~300ms) → lấy role
  [4] Navigate

→ Cần hiển thị ProgressBar từ bước [1] đến bước [4].
```

### 9.2 Truyền storeId xuyên suốt StoreOwnerActivity

`storeId` cần được truyền từ LoginActivity → StoreOwnerActivity → tất cả Fragments.

```java
// Cách đơn giản nhất cho university project:
// Đọc từ Intent của Activity trong Fragment
String storeId = requireActivity().getIntent().getStringExtra("storeId");
```

### 9.3 Firestore Security Rules (tham khảo, không bắt buộc trong môi trường test)

```
// Chủ quán chỉ sửa được store và foods của mình
match /Stores/{storeId} {
  allow write: if request.auth != null
    && get(/databases/$(database)/documents/Users/$(request.auth.uid)).data.storeId == storeId;
}
```

### 9.4 Backward-compatible — customer + admin không đổi

- `MainActivity`, `HomeFragment`, `StoreDetailActivity`, `FoodDetailActivity` → giữ nguyên 100%.
- `AuthViewModel.getUserLiveData()` → vẫn hoạt động như cũ (dùng cho auto-login check).

---

## 10. Chức năng đề xuất thêm (roadmap tương lai)

| Chức năng | Ưu tiên | Ghi chú |
|-----------|---------|---------|
| Toggle `isOpen` (mở/đóng quán) | ⭐⭐⭐ | Switch 1 dòng trong Dashboard |
| Đổi ảnh quán (Cloudinary) | ⭐⭐⭐ | Tái dùng CloudinaryHelper |
| Thống kê đơn hàng theo ngày | ⭐⭐ | Cần Orders từ TV3 |
| Cập nhật trạng thái đơn hàng | ⭐⭐⭐ | `"Đang xử lý" → "Đang giao" → "Hoàn thành"` |
| Thêm nhiều ảnh cho món ăn | ⭐ | Cần thay đổi model Food |
| Giờ mở cửa (openTime/closeTime) | ⭐ | Thêm field vào Store model |
| Thông báo đơn hàng mới | ⭐ | FCM Push Notification |

---

## 11. Sơ đồ tổng thể hệ thống sau khi có store_owner

```
Firebase Auth
    └── uid
         │
         ▼
    Firestore Users/{uid}
         │   role = "customer" ────────────→ MainActivity
         │   role = "store_owner" ──────────→ StoreOwnerActivity
         │   role = "admin" ────────────────→ MainActivity (+ admin features)
         │
         │   storeId (chỉ store_owner có)
              │
              ▼
         Firestore Stores/{storeId}
              └── Foods (where storeId == myStoreId)
              └── Orders (where storeId == myStoreId) [TV3]
```

---

*Tài liệu này được tạo bởi TV2 — FoodNow Project*
*Phiên bản: 1.0 — 2026-04-22*
