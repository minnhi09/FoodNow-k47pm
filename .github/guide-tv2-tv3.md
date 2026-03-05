# 📖 Hướng dẫn code cho Thành viên 2 & Thành viên 3

> Tài liệu này hướng dẫn TV2 và TV3 từng bước tạo file, đặt tên, và code theo đúng kiến trúc MVVM mà TV1 đã setup sẵn.

---

## 📌 Đọc trước khi code

| Tài liệu | Mô tả |
|-----------|-------|
| `ARCHITECTURE.md` | Kiến trúc MVVM chi tiết, code mẫu cho từng layer |
| `.github/db.md` | Schema Firestore (tên collection, fields, kiểu dữ liệu) |
| `.github/work-division.md` | Phân công chi tiết từng thành viên |

---

## 🏗 Cấu trúc package hiện tại

```
app/src/main/java/com/example/foodnow/
├── models/          ← User.java, Category.java (TV1 đã tạo)
├── repositories/    ← AuthRepository, UserRepository, CategoryRepository
├── viewmodels/      ← AuthViewModel, ProfileViewModel, HomeViewModel
├── activities/      ← LoginActivity, RegisterActivity
├── fragments/       ← HomeFragment, ProfileFragment, OrdersFragment*, FavoritesFragment*, CartFragment*
├── adapters/        ← CategoryAdapter
└── utils/           ← CloudinaryHelper

(* = placeholder, cần TV2/TV3 implement lại)
```

---

## 📏 Quy ước đặt tên

### Java files

| Layer | Quy tắc đặt tên | Ví dụ |
|-------|-----------------|-------|
| Model | `<Tên>.java` | `Store.java`, `Food.java` |
| Repository | `<Tên>Repository.java` | `StoreRepository.java` |
| ViewModel | `<Tên>ViewModel.java` | `HomeViewModel.java` |
| Activity | `<Tên>Activity.java` | `StoreDetailActivity.java` |
| Fragment | `<Tên>Fragment.java` | `HomeFragment.java` |
| Adapter | `<Tên>Adapter.java` | `StoreAdapter.java` |

### Layout XML files

| Loại | Quy tắc | Ví dụ |
|------|---------|-------|
| Activity layout | `activity_<tên>.xml` | `activity_store_detail.xml` |
| Fragment layout | `fragment_<tên>.xml` | `fragment_home.xml` |
| RecyclerView item | `item_<tên>.xml` | `item_store.xml`, `item_food.xml` |

### View IDs trong XML

| Loại view | Prefix | Ví dụ |
|-----------|--------|-------|
| TextView | `tv_` | `tv_store_name`, `tv_price` |
| ImageView | `img_` | `img_store`, `img_food` |
| EditText | `et_` | `et_address`, `et_note` |
| Button | `btn_` | `btn_checkout`, `btn_add_cart` |
| RecyclerView | `rv_` | `rv_stores`, `rv_foods` |
| ProgressBar | `progress_bar` | `progress_bar` |
| SearchView | `sv_` | `sv_search` |

---

## 🔤 Quy ước code chung

### 1. Model — Bắt buộc có constructor rỗng + getter/setter

```java
package com.example.foodnow.models;

public class Store {
    private String id;
    private String name;
    // ... các field khác

    // ⚠️ BẮT BUỘC — Firestore cần constructor rỗng để deserialize
    public Store() {}

    // Getter
    public String getId()   { return id; }
    public String getName() { return name; }

    // Setter
    public void setId(String id)     { this.id = id; }
    public void setName(String name) { this.name = name; }
}
```

### 2. Repository — Tham khảo `CategoryRepository.java`

```java
package com.example.foodnow.repositories;

public class StoreRepository {
    private final FirebaseFirestore db;

    public StoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Store>> getAllStores() {
        MutableLiveData<List<Store>> liveData = new MutableLiveData<>();
        db.collection("Stores")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    List<Store> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Store store = doc.toObject(Store.class);
                        store.setId(doc.getId());   // ← quan trọng: set id từ document
                        list.add(store);
                    }
                    liveData.setValue(list);
                });
        return liveData;
    }
}
```

### 3. ViewModel — Tạo Repository bằng `new` trong constructor

```java
package com.example.foodnow.viewmodels;

public class HomeViewModel extends ViewModel {
    private final LiveData<List<Store>> stores;

    public HomeViewModel() {
        StoreRepository repo = new StoreRepository();
        stores = repo.getAllStores();
    }

    public LiveData<List<Store>> getStores() { return stores; }
}
```

### 4. Activity/Fragment — Dùng `ViewModelProvider` để tạo ViewModel

```java
// Trong Activity:
HomeViewModel vm = new ViewModelProvider(this).get(HomeViewModel.class);

// Trong Fragment:
HomeViewModel vm = new ViewModelProvider(this).get(HomeViewModel.class);

// Observe LiveData:
vm.getStores().observe(getViewLifecycleOwner(), stores -> {
    storeList.clear();
    storeList.addAll(stores);
    adapter.notifyDataSetChanged();
});
```

### 5. Adapter — Tham khảo `CategoryAdapter.java`

Mỗi adapter cần:
- Inner interface cho click event
- Constructor nhận `(Context, List<Model>, Listener)`
- `ViewHolder` static inner class
- Load ảnh bằng Glide:

```java
Glide.with(context)
     .load(item.getImageUrl())
     .placeholder(R.mipmap.ic_launcher)
     .into(holder.imgItem);
```

### 6. Format giá tiền VND

```java
NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
String priceText = nf.format(price) + "đ";
// 55000 → "55.000đ"
```

---

## 👤 Thành viên 2 — Trang chủ & Quán ăn & Thực đơn

### Thứ tự thực hiện

```
Bước 1: Models (Store, Food)
    ↓
Bước 2: Repositories (StoreRepository, FoodRepository)
    ↓
Bước 3: Sửa HomeViewModel (thêm Store) + Sửa HomeFragment (thêm Store list + Search)
    ↓
Bước 4: StoreAdapter + item_store.xml
    ↓
Bước 5: Sửa fragment_home.xml (thêm SearchView + RecyclerView Store)
    ↓
Bước 6: StoreDetailViewModel + StoreDetailActivity + activity_store_detail.xml
    ↓
Bước 7: FoodAdapter + item_food.xml
    ↓
Bước 8: Đăng ký StoreDetailActivity trong AndroidManifest.xml
```

### Bước 1: Tạo Models

#### `app/src/main/java/com/example/foodnow/models/Store.java`

Tham khảo schema trong `.github/db.md` → collection `Stores`:

| Field | Kiểu | Ghi chú |
|-------|------|---------|
| id | String | set từ `doc.getId()`, không lưu trong Firestore |
| name | String | |
| description | String | |
| address | String | |
| phone | String | |
| imageUrl | String | |
| rating | float | ví dụ: 4.8 |
| deliveryTime | String | ví dụ: "15 phút" |
| deliveryFee | long | VND, ví dụ: 15000 |
| isOpen | boolean | getter: `isOpen()`, setter: `setOpen(boolean)` |

#### `app/src/main/java/com/example/foodnow/models/Food.java`

Tham khảo schema → collection `Foods`:

| Field | Kiểu | Ghi chú |
|-------|------|---------|
| id | String | |
| title | String | tên món |
| description | String | |
| price | long | VND |
| imageUrl | String | |
| rating | float | |
| storeId | String | liên kết với Store |
| categoryId | String | liên kết với Category |
| isAvailable | boolean | |

### Bước 2: Tạo Repositories

#### `app/src/main/java/com/example/foodnow/repositories/StoreRepository.java`

- `getAllStores()` → `LiveData<List<Store>>` — dùng SnapshotListener trên collection `"Stores"`
- Copy pattern từ `CategoryRepository.java`, đổi tên collection và model

#### `app/src/main/java/com/example/foodnow/repositories/FoodRepository.java`

- `getFoodsByStore(String storeId)` → `LiveData<List<Food>>` — dùng `.whereEqualTo("storeId", storeId)` trước `.addSnapshotListener(...)`

### Bước 3: Sửa HomeViewModel

File hiện tại: `viewmodels/HomeViewModel.java` — chỉ có `categories`.

**Cần thêm:**
- Import và tạo `StoreRepository`
- Thêm `LiveData<List<Store>> stores`
- Thêm getter `getStores()`

### Bước 4: Tạo StoreAdapter + Layout

#### `app/src/main/java/com/example/foodnow/adapters/StoreAdapter.java`

- Interface: `OnStoreClickListener.onStoreClick(Store store)`
- Hiển thị: ảnh, tên, mô tả, rating (⭐), delivery time
- Tham khảo `CategoryAdapter.java` cho cấu trúc

#### `app/src/main/res/layout/item_store.xml`

IDs cần dùng: `img_store`, `tv_store_name`, `tv_store_category`, `tv_store_rating`, `tv_delivery_time`

### Bước 5: Sửa HomeFragment + Layout

**Sửa `fragment_home.xml`** — thêm lại:
- `SearchView` (id: `sv_search`)
- RecyclerView danh sách quán (id: `rv_store_or_food`)

**Sửa `HomeFragment.java`:**
- Thêm `StoreAdapter` + observe `getStores()`
- Thêm SearchView listener để filter danh sách quán theo tên
- Khi click quán → mở `StoreDetailActivity` bằng Intent:

```java
Intent intent = new Intent(getContext(), StoreDetailActivity.class);
intent.putExtra("storeId", store.getId());
intent.putExtra("storeName", store.getName());
intent.putExtra("storeDescription", store.getDescription());
intent.putExtra("storeImage", store.getImageUrl());
intent.putExtra("storeRating", store.getRating());
intent.putExtra("storeDeliveryTime", store.getDeliveryTime());
startActivity(intent);
```

### Bước 6: Tạo StoreDetailActivity

#### `app/src/main/java/com/example/foodnow/activities/StoreDetailActivity.java`

- Nhận Intent extras (storeId, storeName, ...)
- Dùng `StoreDetailViewModel` → `getFoods(storeId)`
- Hiển thị thông tin quán + RecyclerView danh sách món
- Khi click "Thêm vào giỏ" trên FoodAdapter → gọi `CartManager` (TV3 sẽ tạo)

> ⚠️ **Lưu ý:** Chưa kết nối CartManager được nếu TV3 chưa xong. Tạm thời dùng `Toast` thay thế.

#### `app/src/main/java/com/example/foodnow/viewmodels/StoreDetailViewModel.java`

```java
public class StoreDetailViewModel extends ViewModel {
    private LiveData<List<Food>> foods;

    public LiveData<List<Food>> getFoods(String storeId) {
        if (foods == null) {
            FoodRepository repo = new FoodRepository();
            foods = repo.getFoodsByStore(storeId);
        }
        return foods;
    }
}
```

### Bước 7: Tạo FoodAdapter + Layout

#### `app/src/main/java/com/example/foodnow/adapters/FoodAdapter.java`

- Interface: `OnFoodClickListener.onFoodClick(Food food)`
- Hiển thị: ảnh, title, giá (format VND), rating

#### `app/src/main/res/layout/item_food.xml`

IDs gợi ý: `img_food`, `tv_food_title`, `tv_food_price`, `tv_food_rating`, `btn_add_cart`

### Bước 8: Đăng ký Activity trong AndroidManifest

Thêm vào `app/src/main/AndroidManifest.xml` (trong thẻ `<application>`):

```xml
<activity android:name=".activities.StoreDetailActivity" />
```

---

## 👤 Thành viên 3 — Giỏ hàng & Đặt hàng & Yêu thích

### ⚠️ Ưu tiên: Viết `CartManager.java` TRƯỚC vì TV2 cần dùng

### Thứ tự thực hiện

```
Bước 1: Models (CartItem, Order + OrderItem, Favorite)
    ↓
Bước 2: CartManager.java (Singleton) ← ƯU TIÊN NHẤT
    ↓
Bước 3: CartAdapter + item_cart.xml + fragment_cart.xml + Sửa CartFragment
    ↓
Bước 4: OrderRepository + CheckoutViewModel
    ↓
Bước 5: CheckoutActivity + activity_checkout.xml
    ↓
Bước 6: OrdersViewModel + OrderAdapter + item_order.xml + fragment_orders.xml + Sửa OrdersFragment
    ↓
Bước 7: FavoriteRepository + FavoritesViewModel
    ↓
Bước 8: FavoriteAdapter + item_favorite.xml + fragment_favorites.xml + Sửa FavoritesFragment
    ↓
Bước 9: Đăng ký CheckoutActivity trong AndroidManifest.xml
```

### Bước 1: Tạo Models

#### `app/src/main/java/com/example/foodnow/models/CartItem.java`

> **Không lưu Firestore** — chỉ dùng local trong bộ nhớ.

| Field | Kiểu | Ghi chú |
|-------|------|---------|
| foodId | String | |
| title | String | tên món |
| price | long | VND |
| quantity | int | |
| imageUrl | String | |
| storeId | String | để biết món thuộc quán nào |
| storeName | String | |

**Cần thêm method:**
```java
public long getTotalPrice() {
    return price * quantity;
}
```

#### `app/src/main/java/com/example/foodnow/models/Order.java`

Tham khảo `.github/db.md` → collection `Orders`:

| Field | Kiểu |
|-------|------|
| id | String |
| userId | String |
| storeId | String |
| storeName | String |
| address | String |
| paymentMethod | String |
| note | String |
| subtotal | long |
| deliveryFee | long |
| total | long |
| status | String |
| createdAt | `com.google.firebase.Timestamp` |
| items | `List<OrderItem>` |

**Cần tạo inner class `OrderItem`:**
```java
public static class OrderItem {
    private String foodId;
    private String title;
    private long price;
    private int quantity;
    private String imageUrl;

    public OrderItem() {}  // bắt buộc cho Firestore

    public OrderItem(String foodId, String title, long price,
                     int quantity, String imageUrl) { ... }

    // getter + setter
}
```

#### `app/src/main/java/com/example/foodnow/models/Favorite.java`

| Field | Kiểu | Ghi chú |
|-------|------|---------|
| id | String | |
| userId | String | |
| type | String | `"store"` hoặc `"food"` |
| itemId | String | id của quán hoặc món |
| name | String | |
| imageUrl | String | |

### Bước 2: Tạo CartManager (Singleton)

#### `app/src/main/java/com/example/foodnow/utils/CartManager.java`

Tham khảo `ARCHITECTURE.md` mục 3.6 cho đầy đủ thiết kế.

**Cấu trúc chính:**
```java
public class CartManager {
    private static CartManager instance;
    private final List<CartItem> items = new ArrayList<>();
    private String currentStoreId;
    private String currentStoreName;

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    // Methods cần implement:
    // isFromDifferentStore(storeId) → boolean
    // clearCart()
    // addItem(CartItem) — nếu foodId đã có → tăng quantity
    // removeItem(foodId) — quantity > 1 → giảm, == 1 → xóa
    // getItems(), getSubtotal(), getItemCount()
    // getCurrentStoreId(), getCurrentStoreName()
}
```

### Bước 3: Cart UI

#### Tạo `app/src/main/res/layout/fragment_cart.xml`

IDs cần: `rv_cart`, `tv_cart_store_name`, `tv_subtotal`, `tv_cart_empty`, `layout_cart_footer`, `btn_checkout`

#### Tạo `app/src/main/res/layout/item_cart.xml`

IDs gợi ý: `img_cart_food`, `tv_cart_food_name`, `tv_cart_food_price`, `tv_cart_quantity`, `btn_minus`, `btn_plus`

#### Tạo `app/src/main/java/com/example/foodnow/adapters/CartAdapter.java`

- Interface: `OnCartItemChangeListener.onQuantityChanged()`
- Nút `+` → `CartManager.getInstance().addItem(...)` → gọi callback
- Nút `−` → `CartManager.getInstance().removeItem(foodId)` → gọi callback

#### Sửa `CartFragment.java` (hiện là skeleton)

Thay toàn bộ nội dung placeholder bằng logic thực:
- Load items từ `CartManager.getInstance()`
- Setup RecyclerView + CartAdapter
- Nút "Thanh toán" → `startActivity(new Intent(getContext(), CheckoutActivity.class))`

### Bước 4: Order Repository + ViewModel

#### `app/src/main/java/com/example/foodnow/repositories/OrderRepository.java`

```java
public class OrderRepository {
    private final FirebaseFirestore db;

    public OrderRepository() { db = FirebaseFirestore.getInstance(); }

    // Tạo đơn hàng mới
    public Task<DocumentReference> createOrder(Order order) {
        return db.collection("Orders").add(order);
    }

    // Lấy đơn hàng theo userId
    public LiveData<List<Order>> getOrdersByUser(String userId) {
        MutableLiveData<List<Order>> liveData = new MutableLiveData<>();
        db.collection("Orders")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, error) -> {
                    // ... tương tự CategoryRepository
                });
        return liveData;
    }
}
```

#### `app/src/main/java/com/example/foodnow/viewmodels/CheckoutViewModel.java`

- LiveData: `orderSuccess` (Boolean), `errorLiveData` (String), `loadingLiveData` (Boolean)
- Method: `placeOrder(Order order)` → gọi `OrderRepository.createOrder()`

### Bước 5: CheckoutActivity

#### `app/src/main/java/com/example/foodnow/activities/CheckoutActivity.java`

Luồng:
1. Lấy items từ `CartManager.getInstance()`
2. Hiển thị tên quán, subtotal, delivery fee (15000), total
3. Ô nhập: địa chỉ (`et_address`), ghi chú (`et_note`)
4. Nút "Đặt hàng":
   - Convert `List<CartItem>` → `List<Order.OrderItem>`
   - Tạo `Order` object với status = `"Đang xử lý"`, createdAt = `Timestamp.now()`
   - `viewModel.placeOrder(order)`
5. Observe `orderSuccess` → `CartManager.clearCart()` → `finish()`

#### Tạo layout `activity_checkout.xml`

IDs cần: `tv_checkout_store`, `tv_subtotal`, `tv_delivery_fee`, `tv_total`, `et_address`, `et_note`, `btn_place_order`, `progress_bar`

### Bước 6: Orders tab

#### `app/src/main/java/com/example/foodnow/viewmodels/OrdersViewModel.java`

- Dùng `OrderRepository.getOrdersByUser(userId)` với `FirebaseAuth.getInstance().getCurrentUser().getUid()`

#### Tạo `OrderAdapter.java` + `item_order.xml`

- Hiển thị: tên quán, tổng tiền (VND), trạng thái, số món (`items.size() + " món"`)

#### Tạo `fragment_orders.xml`

IDs: `rv_orders`, `tv_orders_empty`

#### Sửa `OrdersFragment.java` (hiện là placeholder)

Thay placeholder bằng logic: ViewModel → observe orders → RecyclerView

### Bước 7: Favorites

#### `app/src/main/java/com/example/foodnow/repositories/FavoriteRepository.java`

```java
// getFavorites(userId) → LiveData<List<Favorite>>  (SnapshotListener + whereEqualTo)
// addFavorite(Favorite) → Task<DocumentReference>   (.add())
// removeFavorite(favoriteId) → Task<Void>           (.delete())
```

#### `app/src/main/java/com/example/foodnow/viewmodels/FavoritesViewModel.java`

- LiveData: `favorites` (List\<Favorite\>)
- Methods: `getFavorites()`, `removeFavorite(id)`

### Bước 8: Favorites UI

#### Tạo `FavoriteAdapter.java` + `item_favorite.xml`

- Interface: `OnFavoriteRemoveListener.onRemove(Favorite fav)`
- Hiển thị type: `"store"` → "Quán ăn", `"food"` → "Món ăn"

#### Tạo `fragment_favorites.xml`

IDs: `rv_favorites`, `tv_favorites_empty`

#### Sửa `FavoritesFragment.java` (hiện là placeholder)

Thay placeholder bằng logic: ViewModel → observe favorites → RecyclerView

### Bước 9: Đăng ký Activity trong AndroidManifest

Thêm vào `app/src/main/AndroidManifest.xml`:

```xml
<activity android:name=".activities.CheckoutActivity" />
```

---

## ⚡ Checklist trước khi push code

- [ ] Tất cả Model có **constructor rỗng** + **getter/setter** đầy đủ
- [ ] Repository dùng đúng tên collection Firestore (`"Stores"`, `"Foods"`, `"Orders"`, `"Favorites"`)
- [ ] ViewModel tạo Repository bằng `new` (không dùng DI)
- [ ] Activity/Fragment dùng `ViewModelProvider` để tạo ViewModel
- [ ] Layout XML dùng đúng ID prefix (`tv_`, `img_`, `btn_`, `rv_`, `et_`)
- [ ] Ảnh load bằng Glide (không dùng cách khác)
- [ ] Giá tiền format VND: `NumberFormat.getInstance(new Locale("vi", "VN"))`
- [ ] Giá tiền dùng kiểu `long` (không phải `double`)
- [ ] Activity mới đã đăng ký trong `AndroidManifest.xml`
- [ ] Build thành công: `.\gradlew.bat assembleDebug`
- [ ] Comment tiếng Việt, UI text tiếng Việt

---

## 🔗 Điểm kết nối giữa TV2 và TV3

| TV2 cần từ TV3 | Mô tả |
|----------------|-------|
| `CartManager.getInstance()` | Gọi trong `StoreDetailActivity` khi user bấm "Thêm vào giỏ" |
| `CartItem` model | Tạo `CartItem` object để truyền vào `CartManager.addItem()` |

**Cách xử lý nếu TV3 chưa xong CartManager:**
- TV2 tạm thời dùng `Toast.makeText(this, "Đã thêm vào giỏ (mock)", ...)` thay cho `CartManager.addItem()`
- Khi TV3 hoàn thành CartManager → TV2 thay Toast bằng code thật

---

## 🛠 Lệnh build & test

```bash
# Build
.\gradlew.bat assembleDebug

# Unit tests
.\gradlew.bat testDebugUnitTest

# Lint
.\gradlew.bat lintDebug
```
