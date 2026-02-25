# 🏗 Kiến trúc FoodNow — Tài liệu Kỹ thuật Chi tiết

> Tài liệu mô tả kiến trúc MVVM, luồng dữ liệu, và thiết kế hệ thống của ứng dụng FoodNow.

---

## Mục lục

- [1. Tổng quan kiến trúc MVVM](#1-tổng-quan-kiến-trúc-mvvm)
- [2. Luồng dữ liệu (Data Flow)](#2-luồng-dữ-liệu-data-flow)
- [3. Layer chi tiết](#3-layer-chi-tiết)
  - [3.1 Models](#31-models)
  - [3.2 Repositories](#32-repositories)
  - [3.3 ViewModels](#33-viewmodels)
  - [3.4 Views (Activities & Fragments)](#34-views-activities--fragments)
  - [3.5 Adapters](#35-adapters)
  - [3.6 Utilities](#36-utilities)
- [4. Navigation Flow](#4-navigation-flow)
- [5. Quy trình hoạt động chính](#5-quy-trình-hoạt-động-chính)
- [6. Cơ sở dữ liệu Firestore](#6-cơ-sở-dữ-liệu-firestore)
- [7. Quản lý Dependencies](#7-quản-lý-dependencies)
- [8. Conventions & Standards](#8-conventions--standards)

---

## 1. Tổng quan kiến trúc MVVM

Ứng dụng tuân theo mô hình **MVVM (Model – View – ViewModel)** — phù hợp với Android Architecture Components và dễ hiểu cho dự án đại học.

```
┌───────────────────────────────────────────────────────────────┐
│                                                               │
│   ┌─────────┐    ┌────────────┐    ┌────────────┐            │
│   │  VIEW   │◄───│ VIEWMODEL  │◄───│ REPOSITORY │◄── Firebase│
│   │         │───►│            │───►│            │──► Firestore│
│   └─────────┘    └────────────┘    └────────────┘            │
│   Activities     ViewModel          Firebase calls            │
│   Fragments      LiveData           Snapshot listeners        │
│   Adapters       Business logic     Data transformation       │
│                                                               │
└───────────────────────────────────────────────────────────────┘
```

### Nguyên tắc thiết kế

| Nguyên tắc | Mô tả |
|-------------|-------|
| **Đơn giản** | Không dùng Dependency Injection (Hilt/Dagger), không RxJava |
| **Repository trực tiếp** | ViewModel tạo Repository bằng `new`, không qua DI framework |
| **LiveData** | Dữ liệu reactive từ Repository → ViewModel → View |
| **Single Activity (chính)** | `MainActivity` chứa 4 Fragment qua BottomNavigationView |
| **Firebase native** | Dùng SDK Firebase trực tiếp, không abstraction layer phức tạp |

---

## 2. Luồng dữ liệu (Data Flow)

### 2.1 Đọc dữ liệu (Read)

```
Firestore Collection
        │
        ▼
  SnapshotListener (real-time)
        │
        ▼
  Repository: chuyển documents → Java objects
        │
        ▼
  MutableLiveData.setValue()
        │
        ▼
  ViewModel: expose LiveData (getter)
        │
        ▼
  Fragment/Activity: observe(LiveData) → cập nhật UI
        │
        ▼
  Adapter.notifyDataSetChanged() → RecyclerView render
```

### 2.2 Ghi dữ liệu (Write)

```
User nhấn nút (ví dụ: "Đặt hàng")
        │
        ▼
  Fragment/Activity: gọi ViewModel.method()
        │
        ▼
  ViewModel: tạo data object, gọi Repository.method()
        │
        ▼
  Repository: Firestore .add() / .set() / .update()
        │
        ▼
  Task<T>: onSuccess → ViewModel cập nhật LiveData
           onFailure → ViewModel set error LiveData
        │
        ▼
  Fragment/Activity: observe kết quả → hiện Toast/navigate
```

### 2.3 Xác thực (Authentication)

```
User nhập email + password
        │
        ▼
  AuthViewModel.login() / register()
        │
        ▼
  AuthRepository → FirebaseAuth.signInWithEmailAndPassword()
        │                        .createUserWithEmailAndPassword()
        ▼
  Task<AuthResult>: onSuccess → userLiveData.setValue(FirebaseUser)
                    onFailure → errorLiveData.setValue(message)
        │
        ▼
  LoginActivity observe → navigate to MainActivity
```

---

## 3. Layer chi tiết

### 3.1 Models

> Package: `com.example.foodnow.models`

Mỗi model tương ứng với 1 Firestore collection (trừ `CartItem`). Tất cả đều cần:
- **Empty constructor** (bắt buộc cho Firestore deserialization)
- **Getters/Setters** cho mọi field

| Class | Firestore Collection | Fields chính | Ghi chú |
|-------|---------------------|-------------|---------|
| `User` | `Users` | id, email, name, phone, address, imageUrl, createdAt (Timestamp) | createdAt dùng Firestore Timestamp |
| `Store` | `Stores` | id, name, description, address, phone, imageUrl, rating (float), deliveryTime, deliveryFee (long), isOpen (boolean) | deliveryFee tính bằng VND |
| `Food` | `Foods` | id, title, description, price (long), imageUrl, rating (float), storeId, categoryId, isAvailable (boolean) | price tính bằng VND |
| `Category` | `Categories` | id, name, imageUrl | Dùng cho filter danh mục |
| `Order` | `Orders` | id, userId, storeId, storeName, address, paymentMethod, note, subtotal, deliveryFee, total (long), status, createdAt, items (List\<OrderItem\>) | Chứa inner class `OrderItem` |
| `Order.OrderItem` | *(embedded)* | foodId, title, price (long), quantity (int), imageUrl | Embedded trong Order.items |
| `CartItem` | *(không lưu DB)* | foodId, title, price, quantity, imageUrl, storeId, storeName | Local-only, quản lý bởi CartManager |
| `Favorite` | `Favorites` | id, userId, type ("store"/"food"), itemId, name, imageUrl | type phân biệt quán vs món |

#### Kiểu dữ liệu đặc biệt

- **Giá tiền**: dùng `long` (không phải `double`) vì VND không có phần thập phân. Ví dụ: `55000` = 55.000₫
- **Timestamp**: dùng `com.google.firebase.Timestamp` cho createdAt
- **CartItem.getTotalPrice()**: trả về `price * quantity` — logic tính toán duy nhất trong models

---

### 3.2 Repositories

> Package: `com.example.foodnow.repositories`

Repository là lớp trung gian giữa ViewModel và Firebase. Mỗi repository chịu trách nhiệm 1 collection.

| Class | Collection | Pattern | Methods |
|-------|-----------|---------|---------|
| `AuthRepository` | Users + Auth | Task-based | `login()`, `register()`, `getCurrentUser()`, `logout()` |
| `StoreRepository` | Stores | Snapshot Listener → LiveData | `getAllStores()` |
| `CategoryRepository` | Categories | Snapshot Listener → LiveData | `getAllCategories()` |
| `FoodRepository` | Foods | Snapshot Listener → LiveData | `getFoodsByStore(storeId)` |
| `OrderRepository` | Orders | Task-based | `createOrder(order)` |
| `FavoriteRepository` | Favorites | Snapshot Listener + Task | `getFavorites()`, `addFavorite()`, `removeFavorite()` |
| `UserRepository` | Users | Snapshot Listener + Task | `getCurrentUser()`, `updateUser()` |

#### Patterns sử dụng

**1. Snapshot Listener → LiveData (đọc real-time)**
```java
public LiveData<List<Store>> getAllStores() {
    MutableLiveData<List<Store>> liveData = new MutableLiveData<>();
    db.collection("Stores").addSnapshotListener((snapshot, error) -> {
        List<Store> stores = new ArrayList<>();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Store store = doc.toObject(Store.class);
            store.setId(doc.getId());
            stores.add(store);
        }
        liveData.setValue(stores);
    });
    return liveData;
}
```

**2. Task-based (ghi dữ liệu)**
```java
public Task<DocumentReference> createOrder(Order order) {
    return db.collection("Orders").add(order);
}
```

**3. Filtered query (lọc theo field)**
```java
public LiveData<List<Food>> getFoodsByStore(String storeId) {
    // ...
    db.collection("Foods")
      .whereEqualTo("storeId", storeId)
      .addSnapshotListener(...);
}
```

---

### 3.3 ViewModels

> Package: `com.example.foodnow.viewmodels`

ViewModel giữ dữ liệu sống sót qua configuration changes (xoay màn hình) và expose LiveData cho View observe.

| ViewModel | Repository | LiveData exposed | Methods |
|-----------|-----------|-----------------|---------|
| `AuthViewModel` | AuthRepository | `userLiveData` (FirebaseUser), `errorLiveData` (String), `loadingLiveData` (Boolean) | `login()`, `register()`, `logout()` |
| `HomeViewModel` | StoreRepository, CategoryRepository | `stores` (List\<Store\>), `categories` (List\<Category\>) | `getStores()`, `getCategories()` |
| `StoreDetailViewModel` | FoodRepository | `foods` (List\<Food\>) | `getFoods(storeId)` |
| `CheckoutViewModel` | OrderRepository | `orderSuccess` (Boolean), `errorLiveData` (String), `loadingLiveData` (Boolean) | `placeOrder(order)` |
| `OrdersViewModel` | *(direct Firestore)* | `ordersLiveData` (List\<Order\>) | `getOrders()` |
| `FavoritesViewModel` | FavoriteRepository | `favorites` (List\<Favorite\>) | `getFavorites()`, `removeFavorite()` |
| `ProfileViewModel` | UserRepository | `user` (User) | `getUser()`, `updateUser()` |

#### Cách tạo ViewModel trong View

```java
// Trong Activity
AuthViewModel viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

// Trong Fragment
HomeViewModel viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
```

> **Lưu ý:** Repository được tạo trực tiếp trong constructor của ViewModel bằng `new`. Không dùng factory pattern hay DI framework.

---

### 3.4 Views (Activities & Fragments)

> Package: `com.example.foodnow.activities` và `com.example.foodnow.fragments`

#### Activities

| Activity | Layout | ViewModel | Chức năng |
|----------|--------|-----------|-----------|
| `LoginActivity` | `activity_login` | AuthViewModel | Đăng nhập, auto-redirect nếu đã đăng nhập |
| `RegisterActivity` | `activity_register` | AuthViewModel | Đăng ký tài khoản mới |
| `StoreDetailActivity` | `activity_store_detail` | StoreDetailViewModel | Xem quán + thực đơn, thêm vào giỏ hàng |
| `CheckoutActivity` | `activity_checkout` | CheckoutViewModel | Nhập thông tin giao hàng, đặt đơn |
| `MainActivity` | `activity_main` | *(không)* | Container cho BottomNavigationView + 4 Fragments |

#### Fragments (được host trong MainActivity)

| Fragment | Layout | ViewModel | Tab |
|----------|--------|-----------|-----|
| `HomeFragment` | `fragment_home` | HomeViewModel | Trang chủ |
| `CartFragment` | `fragment_cart` | *(dùng CartManager)* | *(trong tab Trang chủ hoặc riêng)* |
| `OrdersFragment` | `fragment_orders` | OrdersViewModel | Đơn hàng |
| `FavoritesFragment` | `fragment_favorites` | FavoritesViewModel | Yêu thích |
| `ProfileFragment` | `fragment_profile` | ProfileViewModel + AuthViewModel | Tài khoản |

---

### 3.5 Adapters

> Package: `com.example.foodnow.adapters`

Tất cả adapter kế thừa `RecyclerView.Adapter<ViewHolder>` và sử dụng **interface callback** cho sự kiện click.

| Adapter | Model | Layout | Click Interface | Formatting |
|---------|-------|--------|----------------|------------|
| `StoreAdapter` | Store | `item_store` | `OnStoreClickListener.onStoreClick(Store)` | ⭐ rating |
| `CategoryAdapter` | Category | `item_category` | `OnCategoryClickListener.onCategoryClick(Category)` | — |
| `FoodAdapter` | Food | `item_food` | `OnFoodClickListener.onFoodClick(Food)` | VND currency |
| `CartAdapter` | CartItem | `item_cart` | `OnCartItemChangeListener.onQuantityChanged()` | VND currency, +/− buttons |
| `OrderAdapter` | Order | `item_order` | *(không)* | VND currency, "X món" |
| `FavoriteAdapter` | Favorite | `item_favorite` | `OnFavoriteRemoveListener.onRemove(Favorite)` | "Quán ăn"/"Món ăn" type |

#### Currency formatting (VND)

```java
NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
String priceText = formatter.format(price) + "đ";
// Ví dụ: 55000 → "55.000đ"
```

#### Image loading (Glide)

```java
Glide.with(context)
     .load(store.getImageUrl())
     .into(holder.imgStore);
```

---

### 3.6 Utilities

> Package: `com.example.foodnow.utils`

#### CartManager (Singleton)

Quản lý giỏ hàng **trong bộ nhớ** (không lưu Firestore). Giỏ hàng chỉ chứa món từ **1 quán duy nhất** tại 1 thời điểm.

```
┌─────────────────────────────────────────┐
│            CartManager                  │
│         (Singleton Pattern)             │
├─────────────────────────────────────────┤
│ - instance: CartManager                 │
│ - items: List<CartItem>                 │
│ - currentStoreId: String                │
│ - currentStoreName: String              │
├─────────────────────────────────────────┤
│ + getInstance(): CartManager            │
│ + addItem(CartItem): void               │
│ + removeItem(foodId): void              │
│ + isFromDifferentStore(storeId): boolean│
│ + clearCart(): void                     │
│ + getItems(): List<CartItem>            │
│ + getSubtotal(): long                   │
│ + getItemCount(): int                   │
│ + getCurrentStoreId(): String           │
│ + getCurrentStoreName(): String         │
└─────────────────────────────────────────┘
```

**Logic đổi quán:**
1. User thêm món từ quán B khi giỏ có món quán A
2. `isFromDifferentStore("storeB")` → `true`
3. Hiện `AlertDialog`: "Giỏ hàng đang có món từ quán khác. Xóa giỏ hàng?"
4. User xác nhận → `clearCart()` → `addItem(newItem)`

**Logic addItem:**
- Nếu `foodId` đã có trong giỏ → tăng `quantity` lên 1
- Nếu chưa có → thêm CartItem mới
- Cập nhật `currentStoreId` và `currentStoreName`

**Logic removeItem:**
- Nếu `quantity > 1` → giảm `quantity` đi 1
- Nếu `quantity == 1` → xóa item khỏi giỏ
- Nếu giỏ trống → reset `currentStoreId` và `currentStoreName`

#### CloudinaryHelper

Upload ảnh lên Cloudinary bằng **unsigned upload preset**.

```
┌────────────────────────────────────┐
│        CloudinaryHelper            │
├────────────────────────────────────┤
│ + init(Context): void              │
│ + uploadImage(Uri, Callback): void │
└────────────────────────────────────┘
         │
         ▼
   Cloudinary CDN
   (unsigned preset: "foodnow_unsigned")
```

**Cấu hình cần thiết:**
- `cloud_name`: lấy từ Cloudinary Dashboard
- Upload preset: tạo unsigned preset tên `foodnow_unsigned`
- Không cần `api_key` hay `api_secret` (unsigned upload)

---

## 4. Navigation Flow

### 4.1 Sơ đồ tổng quan

```
                    ┌──────────────┐
                    │  App Launch   │
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │ LoginActivity │
                    │              │◄─────────── Logout
                    └──┬───────┬───┘
                       │       │
            đã login   │       │ chưa có tài khoản
                       │       │
                       │  ┌────▼──────────┐
                       │  │RegisterActivity│
                       │  └────┬──────────┘
                       │       │ đăng ký thành công
                       ▼       ▼
                ┌──────────────────┐
                │   MainActivity    │
                │ (BottomNavView)  │
                └──┬──┬──┬──┬─────┘
                   │  │  │  │
        ┌──────────┘  │  │  └──────────┐
        │             │  │             │
  ┌─────▼─────┐ ┌────▼──▼───┐ ┌──────▼──────┐ ┌──────▼───────┐
  │   Home    │ │  Orders   │ │ Favorites  │ │  Profile     │
  │ Fragment  │ │ Fragment  │ │ Fragment   │ │ Fragment     │
  └─────┬─────┘ └───────────┘ └────────────┘ └──────────────┘
        │
        │ click quán
  ┌─────▼───────────┐
  │StoreDetailActivity│
  │ (xem thực đơn)   │
  └─────┬─────────────┘
        │ thêm vào giỏ → CartManager
        │
  ┌─────▼─────┐
  │   Cart    │
  │ Fragment  │
  └─────┬─────┘
        │ click "Thanh toán"
  ┌─────▼──────────┐
  │CheckoutActivity │
  │ (đặt hàng)     │
  └─────────────────┘
```

### 4.2 Bottom Navigation (4 tab)

```xml
<!-- res/menu/bottom_nav_menu.xml -->
nav_home       → HomeFragment       (Trang chủ)
nav_orders     → OrdersFragment     (Đơn hàng)
nav_favorites  → FavoritesFragment  (Yêu thích)
nav_profile    → ProfileFragment    (Tài khoản)
```

### 4.3 Intent Data truyền giữa màn hình

| Từ → Đến | Key | Type | Mô tả |
|-----------|-----|------|-------|
| HomeFragment → StoreDetailActivity | `storeId` | String | ID quán ăn |
| | `storeName` | String | Tên quán |
| | `storeDescription` | String | Mô tả quán |
| | `storeImage` | String | URL ảnh quán |
| | `storeRating` | float | Điểm đánh giá |
| | `storeDeliveryTime` | String | Thời gian giao |
| CartFragment → CheckoutActivity | *(không truyền Intent)* | — | Dùng CartManager singleton |

---

## 5. Quy trình hoạt động chính

### 5.1 Đăng nhập

```
1. User mở app → LoginActivity hiện ra
2. Kiểm tra FirebaseAuth.getCurrentUser()
   ├── != null → chuyển thẳng đến MainActivity
   └── == null → hiện form đăng nhập
3. User nhập email + password → nhấn "Đăng nhập"
4. AuthViewModel.login(email, password)
5. AuthRepository → FirebaseAuth.signInWithEmailAndPassword()
6. Thành công → navigate to MainActivity (FLAG_ACTIVITY_NEW_TASK | CLEAR_TASK)
   Thất bại → hiện Toast lỗi
```

### 5.2 Đăng ký

```
1. User nhấn "Đăng ký" từ LoginActivity → RegisterActivity
2. User nhập: tên, SĐT, email, password → nhấn "Đăng ký"
3. AuthViewModel.register(email, password, name, phone)
4. AuthRepository:
   a. FirebaseAuth.createUserWithEmailAndPassword()
   b. Thành công → tạo document trong "Users" collection với thông tin user
5. Thành công → navigate to MainActivity
   Thất bại → hiện Toast lỗi
```

### 5.3 Xem quán + thêm món vào giỏ

```
1. HomeFragment hiển thị danh sách quán (từ HomeViewModel → StoreRepository)
2. User click quán → Intent đến StoreDetailActivity (truyền storeId, name, ...)
3. StoreDetailActivity load thực đơn (StoreDetailViewModel → FoodRepository.getFoodsByStore())
4. User nhấn "Thêm" trên món ăn
5. Kiểm tra CartManager.isFromDifferentStore(storeId)
   ├── false → CartManager.addItem(cartItem)
   └── true → AlertDialog "Xóa giỏ hàng cũ?"
                ├── Xác nhận → clearCart() → addItem()
                └── Hủy → không làm gì
6. Toast "Đã thêm vào giỏ hàng"
```

### 5.4 Đặt hàng

```
1. CartFragment hiển thị items từ CartManager
2. User nhấn "Thanh toán" → CheckoutActivity
3. CheckoutActivity hiển thị:
   - Danh sách items + tổng tiền
   - Ô nhập địa chỉ, ghi chú
4. User nhấn "Đặt hàng"
5. Tạo Order object:
   - Convert List<CartItem> → List<OrderItem>
   - Set userId, storeId, storeName, address, note
   - Set subtotal, deliveryFee (15000), total
   - Set status = "Đang xử lý", createdAt = now
6. CheckoutViewModel.placeOrder(order)
7. OrderRepository.createOrder() → Firestore .add()
8. Thành công → CartManager.clearCart() → finish() → về CartFragment
   Thất bại → Toast lỗi
```

### 5.5 Đăng xuất

```
1. ProfileFragment → nhấn "Đăng xuất"
2. AuthViewModel.logout()
3. AuthRepository → FirebaseAuth.signOut()
4. Navigate to LoginActivity (FLAG_ACTIVITY_NEW_TASK | CLEAR_TASK)
   → xóa hết back stack
```

---

## 6. Cơ sở dữ liệu Firestore

### 6.1 Sơ đồ tổng thể

```
Firestore Database
│
├── 👤 Users/{userId}           ← thông tin người dùng
├── 🏪 Stores/{storeId}         ← danh sách quán ăn
├── 🍔 Foods/{foodId}           ← danh sách món ăn
├── 📂 Categories/{categoryId}  ← danh mục (Pizza, Burger, ...)
├── 📦 Orders/{orderId}         ← đơn hàng
└── ❤️ Favorites/{favoriteId}   ← quán/món yêu thích
```

### 6.2 Quan hệ giữa Collections

```
Users ──────────────────────┐
  │                         │
  ├── tạo ──→ Orders        │
  │             │           │
  │             └── chứa ──→ Foods
  │                          │
  └── yêu thích ──→ Favorites│
                             │
Stores ──────────────────────┘
  │
  └── có ──→ Foods ──→ Categories
```

### 6.3 Trạng thái đơn hàng

```
"Đang xử lý"  →  "Đang giao"  →  "Hoàn thành"
                                    ↑
                   "Đã hủy" ────────┘ (chỉ từ "Đang xử lý")
```

### 6.4 Collection Names (QUAN TRỌNG)

Tên collection trong Firestore **viết hoa chữ đầu** (PascalCase):

| Java constant | Firestore | Lưu ý |
|---------------|-----------|-------|
| `"Users"` | `Users` | Document ID = Firebase Auth UID |
| `"Stores"` | `Stores` | Document ID = auto-generated |
| `"Foods"` | `Foods` | `storeId` tham chiếu Stores |
| `"Categories"` | `Categories` | Document ID = auto-generated |
| `"Orders"` | `Orders` | `userId` tham chiếu Users |
| `"Favorites"` | `Favorites` | `userId` + `itemId` tham chiếu |

> 📖 Schema chi tiết: xem [`.github/db.md`](../.github/db.md)

---

## 7. Quản lý Dependencies

### 7.1 Danh sách dependencies

| Library | Version | Mục đích |
|---------|---------|----------|
| Firebase Auth | *(BOM managed)* | Xác thực người dùng |
| Firebase Firestore | *(BOM managed)* | Cơ sở dữ liệu |
| Glide | 4.16.0 | Tải & cache hình ảnh |
| Cloudinary Android | 3.0.2 | Upload hình ảnh |
| RecyclerView | 1.3.2 | Danh sách cuộn |
| CardView | 1.0.0 | Card UI component |
| ViewPager2 | 1.0.0 | Banner/slider |
| CircleIndicator | 2.1.6 | Indicator cho ViewPager |
| ViewModel | *(lifecycle)* | MVVM ViewModel |
| LiveData | *(lifecycle)* | Observable data holder |
| Material Design 3 | *(libs.material)* | UI theme & components |

### 7.2 Phong cách khai báo

Dự án dùng **2 phong cách** khai báo dependency:

```kotlin
// Phong cách 1: Version Catalog (cho core Android deps)
implementation(libs.appcompat)
implementation(libs.material)
implementation(libs.activity)

// Phong cách 2: Explicit coordinates (cho third-party)
implementation("com.github.bumptech.glide:glide:4.16.0")
implementation("com.cloudinary:cloudinary-android:3.0.2")
implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
```

> **Convention:** Khi thêm dependency mới, theo phong cách đã có trong file `build.gradle.kts`.

---

## 8. Conventions & Standards

### 8.1 Naming Conventions

| Thành phần | Pattern | Ví dụ |
|------------|---------|-------|
| Activity | `XxxActivity` | `LoginActivity`, `CheckoutActivity` |
| Fragment | `XxxFragment` | `HomeFragment`, `CartFragment` |
| ViewModel | `XxxViewModel` | `AuthViewModel`, `HomeViewModel` |
| Repository | `XxxRepository` | `AuthRepository`, `StoreRepository` |
| Adapter | `XxxAdapter` | `StoreAdapter`, `FoodAdapter` |
| Model | `Xxx` (noun) | `Store`, `Food`, `Order` |
| Layout (Activity) | `activity_xxx.xml` | `activity_login.xml` |
| Layout (Fragment) | `fragment_xxx.xml` | `fragment_home.xml` |
| Layout (Item) | `item_xxx.xml` | `item_store.xml`, `item_food.xml` |
| Menu | `xxx_menu.xml` | `bottom_nav_menu.xml` |
| Nav ID | `nav_xxx` | `nav_home`, `nav_orders` |

### 8.2 Coding Standards

1. **Ngôn ngữ UI:** Tiếng Việt (labels, Toast messages, comments)
2. **Ngôn ngữ code:** Tiếng Anh (class names, method names, variable names)
3. **Model fields:** phải match exactly với Firestore field names
4. **Empty constructor:** bắt buộc cho mọi Firestore model
5. **Giá tiền:** dùng `long` (VND, không có phần thập phân)
6. **Currency format:** `NumberFormat.getInstance(new Locale("vi", "VN"))` + "đ"
7. **Image loading:** luôn dùng Glide, không dùng Picasso hay thủ công
8. **LiveData:** ViewModel expose `LiveData<T>`, giữ `MutableLiveData<T>` là private
9. **No DI:** Repository tạo bằng `new` trong ViewModel constructor

### 8.3 File mới cần tuân thủ

Khi tạo thêm tính năng mới, tuân theo pattern:

```
1. Tạo Model (nếu cần) → models/NewModel.java
2. Tạo Repository           → repositories/NewRepository.java
3. Tạo ViewModel             → viewmodels/NewViewModel.java
4. Tạo Layout XML            → res/layout/activity_new.xml hoặc fragment_new.xml
5. Tạo Activity/Fragment     → activities/NewActivity.java hoặc fragments/NewFragment.java
6. Tạo Adapter (nếu có list) → adapters/NewAdapter.java + res/layout/item_new.xml
7. Đăng ký Activity          → AndroidManifest.xml
```

---

## Phụ lục: Bảng tổng hợp tất cả Classes

| # | Package | Class | Loại | Mô tả |
|---|---------|-------|------|-------|
| 1 | *(root)* | `MainActivity` | Activity | Container chính, BottomNavigationView |
| 2 | models | `User` | Model | Thông tin người dùng |
| 3 | models | `Store` | Model | Thông tin quán ăn |
| 4 | models | `Food` | Model | Thông tin món ăn |
| 5 | models | `Category` | Model | Danh mục món |
| 6 | models | `Order` | Model | Đơn hàng (chứa OrderItem) |
| 7 | models | `Order.OrderItem` | Inner class | Chi tiết món trong đơn |
| 8 | models | `CartItem` | Model | Món trong giỏ hàng (local) |
| 9 | models | `Favorite` | Model | Quán/món yêu thích |
| 10 | repositories | `AuthRepository` | Repository | Firebase Auth + tạo user |
| 11 | repositories | `StoreRepository` | Repository | CRUD quán ăn |
| 12 | repositories | `CategoryRepository` | Repository | Đọc danh mục |
| 13 | repositories | `FoodRepository` | Repository | Đọc món theo quán |
| 14 | repositories | `OrderRepository` | Repository | Tạo đơn hàng |
| 15 | repositories | `FavoriteRepository` | Repository | CRUD yêu thích |
| 16 | repositories | `UserRepository` | Repository | Đọc/sửa user profile |
| 17 | viewmodels | `AuthViewModel` | ViewModel | Xác thực (login/register/logout) |
| 18 | viewmodels | `HomeViewModel` | ViewModel | Stores + Categories |
| 19 | viewmodels | `StoreDetailViewModel` | ViewModel | Foods theo store |
| 20 | viewmodels | `CheckoutViewModel` | ViewModel | Đặt hàng |
| 21 | viewmodels | `OrdersViewModel` | ViewModel | Lịch sử đơn hàng |
| 22 | viewmodels | `FavoritesViewModel` | ViewModel | Danh sách yêu thích |
| 23 | viewmodels | `ProfileViewModel` | ViewModel | Thông tin cá nhân |
| 24 | activities | `LoginActivity` | Activity | Đăng nhập |
| 25 | activities | `RegisterActivity` | Activity | Đăng ký |
| 26 | activities | `StoreDetailActivity` | Activity | Chi tiết quán + thực đơn |
| 27 | activities | `CheckoutActivity` | Activity | Thanh toán |
| 28 | fragments | `HomeFragment` | Fragment | Trang chủ (search + categories + stores) |
| 29 | fragments | `CartFragment` | Fragment | Giỏ hàng |
| 30 | fragments | `OrdersFragment` | Fragment | Tab đơn hàng |
| 31 | fragments | `FavoritesFragment` | Fragment | Tab yêu thích |
| 32 | fragments | `ProfileFragment` | Fragment | Tab tài khoản |
| 33 | adapters | `StoreAdapter` | Adapter | Hiển thị quán ăn |
| 34 | adapters | `CategoryAdapter` | Adapter | Hiển thị danh mục |
| 35 | adapters | `FoodAdapter` | Adapter | Hiển thị món ăn |
| 36 | adapters | `CartAdapter` | Adapter | Hiển thị giỏ hàng (+/−) |
| 37 | adapters | `OrderAdapter` | Adapter | Hiển thị đơn hàng |
| 38 | adapters | `FavoriteAdapter` | Adapter | Hiển thị yêu thích |
| 39 | utils | `CartManager` | Utility | Singleton giỏ hàng (in-memory) |
| 40 | utils | `CloudinaryHelper` | Utility | Upload ảnh lên Cloudinary |
