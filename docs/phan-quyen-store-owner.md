# Hướng dẫn luồng code: Phân quyền Store Owner

> 📖 Dành cho: thành viên mới, reviewer, hoặc bất kỳ ai muốn hiểu cách hệ thống phân quyền hoạt động trong FoodNow.

---

## 1. Tổng quan hệ thống vai trò

FoodNow có **3 vai trò người dùng**, lưu trong trường `role` của Firestore `Users/{uid}`:

| Role | Ý nghĩa | Đi đến màn hình |
|---|---|---|
| `customer` | Khách hàng thông thường | `MainActivity` |
| `admin` | Quản trị viên | `MainActivity` (tab đặc biệt) |
| `store_owner` | Chủ nhà hàng | `StoreOwnerActivity` |

---

## 2. Sơ đồ luồng đăng nhập (Login Flow)

```
Người dùng nhập email + password
        ↓
[LoginActivity] btnLogin.onClick
        ↓
authViewModel.login(email, password)
        ↓
[AuthRepository] Firebase Auth.signIn(...)
        ↓  thành công → FirebaseUser (chỉ có uid, email)
[AuthRepository] Firestore.collection("Users").document(uid).get()
        ↓  lấy role + storeId từ Firestore
[AuthViewModel] userProfileLiveData.setValue(user)
        ↓
[LoginActivity] observe userProfileLiveData
        ↓
user.getRole() == "store_owner" ?
  ├── CÓ  → goToStoreOwner(storeId) → StoreOwnerActivity
  └── KHÔNG → goToMain()            → MainActivity
```

**Tại sao cần 2 bước?** Firebase Auth chỉ biết email/mật khẩu. Thông tin role là dữ liệu nghiệp vụ, phải lưu ở Firestore.

---

## 3. Các file liên quan

### 3.1 `models/User.java`
```java
private String role;     // "customer" | "store_owner" | "admin"
private String storeId;  // chỉ có ý nghĩa khi role = "store_owner"
```
Firestore yêu cầu constructor rỗng `public User() {}` để tự động map dữ liệu.

### 3.2 `repositories/AuthRepository.java`
```java
// Bước 1: đăng nhập với Firebase Auth
public Task<AuthResult> login(String email, String password) { ... }

// Bước 2: lấy profile (role, storeId) từ Firestore
public Task<User> getUserProfile(String uid) {
    return db.collection("Users").document(uid).get()
        .continueWith(task -> {
            User user = task.getResult().toObject(User.class);
            user.setId(uid);
            return user;
        });
}
```

### 3.3 `viewmodels/AuthViewModel.java`
```java
// LiveData mới — profile đầy đủ (có role)
private final MutableLiveData<User> userProfileLiveData = new MutableLiveData<>();

// Sau khi Auth thành công, tự động fetch profile
public void login(String email, String password) {
    authRepository.login(...)
        .addOnSuccessListener(result -> {
            authRepository.getUserProfile(uid)
                .addOnSuccessListener(user -> userProfileLiveData.setValue(user));
        });
}

public LiveData<User> getUserProfileLiveData() { return userProfileLiveData; }
```

### 3.4 `activities/LoginActivity.java`
```java
// Quan sát profile → route theo role
authViewModel.getUserProfileLiveData().observe(this, userProfile -> {
    if ("store_owner".equals(userProfile.getRole())) {
        goToStoreOwner(userProfile.getStoreId());
    } else {
        goToMain(); // customer hoặc admin
    }
});
```

---

## 4. StoreOwnerActivity — shell BottomNav

**File:** `activities/StoreOwnerActivity.java`

Tương tự `MainActivity`, đây là Activity chứa BottomNavigationView với 4 tab dành riêng cho chủ nhà hàng:

```
StoreOwnerActivity
├── BottomNav: store_owner_nav_menu.xml
├── Tab "Quán của tôi"  → StoreOwnerDashboardFragment
├── Tab "Thực đơn"      → ManageFoodsFragment
├── Tab "Đơn hàng"      → StoreOrdersFragment (placeholder)
└── Tab "Hồ sơ"         → ProfileFragment (tái dụng)
```

**storeId truyền như thế nào?**
```
LoginActivity → Intent.putExtra("storeId", storeId)
     ↓
StoreOwnerActivity.onCreate() → getIntent().getStringExtra("storeId")
     ↓
Fragment con → ((StoreOwnerActivity) requireActivity()).getStoreId()
```

---

## 5. ViewModel dùng chung: StoreOwnerViewModel

**File:** `viewmodels/StoreOwnerViewModel.java`

ViewModel này được **share** giữa tất cả Fragment trong StoreOwnerActivity nhờ `ViewModelProvider(requireActivity())`:

```java
// Trong bất kỳ Fragment nào:
viewModel = new ViewModelProvider(requireActivity()).get(StoreOwnerViewModel.class);
```

**Tại sao dùng `requireActivity()`?** Vì ViewModel được gắn với lifecycle của Activity, nên khi chuyển tab (Fragment thay đổi) dữ liệu không bị load lại.

```
StoreOwnerViewModel
├── getStore(storeId)   → LiveData<Store>  (real-time Firestore)
├── getFoods(storeId)   → LiveData<List<Food>> (real-time Firestore)
├── addFood(food)       → void (gọi FoodRepository.addFood)
├── updateFood(food)    → void
├── deleteFood(foodId)  → void
├── getActionMessage()  → LiveData<String> (kết quả thao tác)
└── getLoading()        → LiveData<Boolean>
```

---

## 6. StoreOwnerDashboardFragment — thông tin quán

**File:** `fragments/StoreOwnerDashboardFragment.java`  
**Layout:** `fragment_store_owner_dashboard.xml`

```
onViewCreated:
  1. Lấy storeId từ StoreOwnerActivity
  2. Lấy ViewModel từ requireActivity()
  3. observe viewModel.getStore(storeId) → cập nhật UI
  4. Switch "Mở cửa" → gọi StoreRepository.updateStore(storeId, store)
```

---

## 7. ManageFoodsFragment — quản lý thực đơn

**File:** `fragments/ManageFoodsFragment.java`  
**Adapter:** `adapters/ManageFoodAdapter.java`

```
onViewCreated:
  1. observe viewModel.getFoods(storeId) → cập nhật RecyclerView
  2. FAB "+" → startActivity(AddEditFoodActivity, storeId)
  3. Nút "Sửa" (trong adapter) → startActivity(AddEditFoodActivity, storeId + foodId + fields)
  4. Nút "Xóa" (trong adapter) → AlertDialog → viewModel.deleteFood(foodId)
```

**ManageFoodAdapter** nhận 2 callback:
- `OnEditListener.onEdit(food)` — Fragment xử lý → mở AddEditFoodActivity
- `OnDeleteListener.onDelete(food)` — Fragment xử lý → hiện dialog xác nhận

---

## 8. AddEditFoodActivity — form thêm/sửa món

**File:** `activities/AddEditFoodActivity.java`  
**Layout:** `activity_add_edit_food.xml`

### Chế độ THÊM MỚI
```
Intent chỉ có "storeId"
  ↓
foodId = "" (rỗng)
setTitle("Thêm món ăn")
Form trống
```

### Chế độ CHỈNH SỬA
```
Intent có "foodId", "foodTitle", "foodPrice", ... (truyền từ ManageFoodsFragment)
  ↓
foodId = intent.getStringExtra("foodId")  (không rỗng)
setTitle("Sửa món ăn")
Điền sẵn dữ liệu vào form
```

### Luồng upload ảnh
```
btnPickImage → pickImageLauncher.launch("image/*")
  ↓ user chọn ảnh từ gallery
uploadImage(uri)
  ↓
CloudinaryHelper.uploadImage(ctx, uri, "foods", callback)
  ↓ upload xong
uploadedImageUrl = secureUrl
etImageUrl.setText(secureUrl)
Glide.load(secureUrl).into(imgPreview)
```

### Lưu món ăn
```
btnSave.onClick
  ↓ validate (title không rỗng, price là số)
Food food = new Food(title, desc, price, imageUrl, storeId, ...)
  ↓
foodId rỗng? → viewModel.addFood(food)
foodId có?   → food.setId(foodId) → viewModel.updateFood(food)
  ↓
observe actionMessage → "thành công" → finish()
```

---

## 9. FoodRepository — các method CRUD mới

```java
addFood(food)      → db.collection("Foods").document().set(foodToMap(food))
updateFood(food)   → db.collection("Foods").document(food.getId()).set(foodToMap(food))
deleteFood(foodId) → db.collection("Foods").document(foodId).delete()
```

`foodToMap()` chuyển Food thành `Map<String, Object>` để **tránh lưu field `id`** (id là document key, không phải field trong Firestore).

---

## 10. Cách test tính năng

### Cấu hình tài khoản store_owner trong Firestore Console

1. Mở [Firebase Console](https://console.firebase.google.com) → Firestore → collection `Users`
2. Chọn document của tài khoản muốn test
3. Sửa field `role` = `store_owner`
4. Thêm field `storeId` = ID của document trong collection `Stores`
5. Đăng xuất rồi đăng nhập lại → app tự route vào `StoreOwnerActivity`

### Kiểm tra CRUD món ăn

| Thao tác | Cách test |
|---|---|
| Xem thực đơn | Tab "Thực đơn" → danh sách món của quán đó |
| Thêm món | FAB "+" → điền form → nhấn Lưu |
| Sửa món | Nút "Sửa" trong item → form điền sẵn → nhấn Lưu |
| Xóa món | Nút "Xóa" → Dialog xác nhận → nhấn Xóa |
| Mở/đóng quán | Tab "Quán của tôi" → công tắc "Mở cửa" |

---

## 11. Câu hỏi thường gặp

**Q: Tại sao không dùng `FirebaseUser.getDisplayName()` để lấy role?**  
A: Firebase Auth không lưu role, đó là dữ liệu nghiệp vụ. Phải lưu ở Firestore `Users/{uid}.role`.

**Q: Fragment con lấy storeId bằng cách nào?**  
A: Cast activity sang `StoreOwnerActivity` rồi gọi `.getStoreId()`. Không dùng Bundle/argument vì storeId không thay đổi trong toàn bộ phiên đăng nhập.

**Q: ViewModel `StoreOwnerViewModel` không phải là `AndroidViewModel` — sao vẫn dùng được?**  
A: ViewModel thông thường là đủ vì không cần `Context`. Repository tự tạo `FirebaseFirestore.getInstance()` (không cần truyền context).

**Q: Nếu người dùng là `store_owner` nhưng `storeId` rỗng thì sao?**  
A: `StoreOwnerActivity` nhận `storeId = ""`, các query Firestore sẽ không trả về kết quả. Cần đảm bảo mọi `store_owner` đều có `storeId` hợp lệ trong Firestore.
