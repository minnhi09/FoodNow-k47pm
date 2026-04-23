# 📋 Tổng hợp chức năng Thành viên 2 — Trang chủ & Quán ăn & Thực đơn

> Tài liệu này mô tả chi tiết toàn bộ phần code mà Thành viên 2 (TV2) chịu trách nhiệm thực hiện trong dự án FoodNow.

---

## Mục lục

1. [Tổng quan phân công](#1-tổng-quan-phân-công)
2. [Model: Store.java](#2-model-storejava)
3. [Model: Food.java](#3-model-foodjava)
4. [Repository: StoreRepository.java](#4-repository-storerepositoryjava)
5. [Repository: FoodRepository.java](#5-repository-foodrepositoryjava)
6. [ViewModel: HomeViewModel.java](#6-viewmodel-homevmjava)
7. [ViewModel: StoreDetailViewModel.java](#7-viewmodel-storedetailvmjava)
8. [Adapter: StoreAdapter.java](#8-adapter-storeadapterjava)
9. [Adapter: FoodAdapter.java](#9-adapter-foodadapterjava)
10. [Adapter: RecommendedFoodAdapter.java](#10-adapter-recommendedfoodadapterjava)
11. [Fragment: HomeFragment.java](#11-fragment-homefragmentjava)
12. [Activity: StoreDetailActivity.java](#12-activity-storedetailactivityjava)
13. [Activity: FoodDetailActivity.java](#13-activity-fooddetailactivityjava)
14. [Sơ đồ luồng dữ liệu](#14-sơ-đồ-luồng-dữ-liệu)
15. [Điểm kết nối với TV3](#15-điểm-kết-nối-với-tv3)

---

## 1. Tổng quan phân công

TV2 phụ trách **luồng xem sản phẩm** — từ trang chủ, danh sách quán ăn, đến chi tiết từng món ăn.

| Layer | File |
|-------|------|
| **Models** | `Store.java`, `Food.java` |
| **Repositories** | `StoreRepository.java`, `FoodRepository.java` |
| **ViewModels** | `HomeViewModel.java`, `StoreDetailViewModel.java` |
| **Fragment** | `HomeFragment.java` |
| **Activities** | `StoreDetailActivity.java`, `FoodDetailActivity.java` |
| **Adapters** | `StoreAdapter.java`, `FoodAdapter.java`, `RecommendedFoodAdapter.java` |
| **Layouts** | `fragment_home.xml`, `activity_store_detail.xml`, `activity_food_detail.xml`, `item_store.xml`, `item_food.xml`, `item_recommended_food.xml` |

**Kỹ năng học được:** RecyclerView, Snapshot Listener real-time Firestore, truyền dữ liệu qua Intent, Glide load ảnh, tìm kiếm & lọc danh sách.

---

## 2. Model: `Store.java`

**Vị trí:** `app/src/main/java/com/example/foodnow/models/Store.java`

### Mục đích
Đây là class đại diện cho dữ liệu một **quán ăn** trong Firestore. Mỗi đối tượng `Store` tương ứng với một document trong collection `Stores`.

### Các trường dữ liệu

| Field | Kiểu | Mô tả |
|-------|------|-------|
| `id` | `String` | ID document Firestore (gán thủ công sau khi đọc) |
| `name` | `String` | Tên quán ăn |
| `description` | `String` | Mô tả ngắn về quán |
| `address` | `String` | Địa chỉ quán |
| `phone` | `String` | Số điện thoại |
| `imageUrl` | `String` | URL ảnh bìa quán (lưu trên Cloudinary) |
| `rating` | `float` | Điểm đánh giá (ví dụ: 4.8) |
| `deliveryTime` | `String` | Thời gian giao hàng (ví dụ: "20-30 phút") |
| `deliveryFee` | `long` | Phí giao hàng (đơn vị: đồng) |
| `isOpen` | `boolean` | Quán đang mở hay đóng cửa |
| `storeOwnerId` | `String` | UID của chủ quán (liên kết với User) |
| `categoryId` | `String` | ID danh mục (dùng để lọc theo loại quán) |

### Lưu ý quan trọng
```java
// Bắt buộc phải có constructor rỗng — Firestore dùng để deserialize
public Store() {}
```
Nếu không có constructor rỗng, Firestore sẽ **báo lỗi** khi đọc dữ liệu bằng `.toObject(Store.class)`.

---

## 3. Model: `Food.java`

**Vị trí:** `app/src/main/java/com/example/foodnow/models/Food.java`

### Mục đích
Đại diện cho một **món ăn** trong thực đơn của quán. Mỗi `Food` thuộc về một `Store` cụ thể (qua `storeId`).

### Các trường dữ liệu

| Field | Kiểu | Mô tả |
|-------|------|-------|
| `id` | `String` | ID document Firestore |
| `title` | `String` | Tên món ăn |
| `description` | `String` | Mô tả chi tiết món |
| `price` | `long` | Giá tiền (đơn vị: đồng) |
| `imageUrl` | `String` | URL ảnh món ăn |
| `rating` | `float` | Điểm đánh giá |
| `storeId` | `String` | ID quán chứa món này (dùng để query) |
| `categoryId` | `String` | Loại món (dùng để phân loại) |
| `isAvailable` | `boolean` | Món đang có hay hết |

### Lưu ý về giá tiền
Dùng kiểu `long` (không phải `double`) để tránh lỗi làm tròn số thập phân khi lưu trữ. Khi hiển thị, dùng `NumberFormat` với `Locale("vi", "VN")`:
```java
NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
tvPrice.setText(nf.format(food.getPrice()) + "đ"); // ví dụ: "65.000đ"
```

---

## 4. Repository: `StoreRepository.java`

**Vị trí:** `app/src/main/java/com/example/foodnow/repositories/StoreRepository.java`

### Mục đích
Lớp truy xuất dữ liệu quán ăn từ **Firestore**. Tất cả logic kết nối Firebase đều nằm ở đây, Fragment và ViewModel không gọi Firestore trực tiếp.

### Các method

#### `getAllStores()` — Lấy toàn bộ quán, lắng nghe real-time
```java
public LiveData<List<Store>> getAllStores() {
    MutableLiveData<List<Store>> liveData = new MutableLiveData<>();
    db.collection("Stores")
        .addSnapshotListener((snapshots, error) -> {
            if (error != null || snapshots == null) return;
            List<Store> list = new ArrayList<>();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Store store = doc.toObject(Store.class);
                if (store != null) {
                    store.setId(doc.getId()); // ⚠️ quan trọng: ID không tự gán
                    list.add(store);
                }
            }
            liveData.setValue(list);
        });
    return liveData;
}
```
- Dùng **`addSnapshotListener`** thay vì `.get()` để nhận **cập nhật real-time** — khi admin thêm/sửa quán trên Firestore, app tự cập nhật mà không cần refresh.
- **Lý do `store.setId(doc.getId())`:** Firestore không tự điền field `id` vào object, phải gán thủ công từ document ID.

#### `getStoreById(String storeId)` — Lấy 1 quán theo ID
Dùng cho trường hợp cần reload thông tin quán theo thời gian thực.

#### `updateStore(String storeId, Store store)` — Cập nhật quán
Trả về `Task<Void>` để biết khi nào ghi xong (dành cho Store Owner role).

---

## 5. Repository: `FoodRepository.java`

**Vị trí:** `app/src/main/java/com/example/foodnow/repositories/FoodRepository.java`

### Mục đích
Truy xuất danh sách món ăn từ Firestore, hỗ trợ CRUD đầy đủ (dành cho Store Owner quản lý thực đơn).

### Các method

#### `getFoodsByStore(String storeId)` — Lấy món theo quán
```java
db.collection("Foods")
    .whereEqualTo("storeId", storeId)  // lọc theo quán
    .addSnapshotListener(...);
```
Dùng `.whereEqualTo("storeId", ...)` để chỉ lấy món ăn của **đúng quán đang xem**, không kéo toàn bộ collection.

#### `addFood(Food food)` — Thêm món mới
```java
// Dùng Map để tránh lưu field "id" vào Firestore
Map<String, Object> data = foodToMap(food);
return db.collection("Foods").document().set(data);
```
Firestore tự tạo document ID ngẫu nhiên.

#### `updateFood(Food food)` — Cập nhật món ăn
#### `deleteFood(String foodId)` — Xóa món ăn

---

## 6. ViewModel: `HomeViewModel.java`

**Vị trí:** `app/src/main/java/com/example/foodnow/viewmodels/HomeViewModel.java`

### Mục đích
Cung cấp dữ liệu **danh mục** và **quán ăn** cho `HomeFragment`, đảm bảo dữ liệu sống sót qua screen rotation (đặc tính của ViewModel).

```java
public class HomeViewModel extends ViewModel {
    private final LiveData<List<Category>> categories;
    private final LiveData<List<Store>> stores;

    public HomeViewModel() {
        categories = new CategoryRepository().getAllCategories();
        stores     = new StoreRepository().getAllStores();
    }

    public LiveData<List<Category>> getCategories() { return categories; }
    public LiveData<List<Store>> getStores()        { return stores; }
}
```

### Tại sao cần ViewModel?
- Nếu đặt `getAllStores()` trực tiếp trong Fragment, mỗi lần xoay màn hình sẽ tạo ra một listener Firestore mới → **rò rỉ bộ nhớ**.
- ViewModel chỉ tạo repository **một lần** và dữ liệu tồn tại qua configuration change.

---

## 7. ViewModel: `StoreDetailViewModel.java`

**Vị trí:** `app/src/main/java/com/example/foodnow/viewmodels/StoreDetailViewModel.java`

### Mục đích
Cung cấp danh sách **món ăn** cho `StoreDetailActivity`, với lazy init để tránh query lại khi activity không thay đổi.

```java
public LiveData<List<Food>> getFoods(String storeId) {
    if (foods == null) {              // chỉ query Firestore lần đầu
        FoodRepository repo = new FoodRepository();
        foods = repo.getFoodsByStore(storeId);
    }
    return foods;
}
```

---

## 8. Adapter: `StoreAdapter.java`

**Vị trí:** `app/src/main/java/com/example/foodnow/adapters/StoreAdapter.java`

### Mục đích
Hiển thị danh sách quán ăn dưới dạng **RecyclerView** theo chiều dọc. Mỗi item dùng layout `item_store.xml`.

### Cấu trúc
```
StoreAdapter
├── Constructor(context, storeList, OnStoreClickListener)
├── onCreateViewHolder()  → inflate item_store.xml
├── onBindViewHolder()    → gán text + ảnh + click listener
├── ViewHolder            → img_store, tv_store_name, tv_store_rating, ...
└── Helper methods        → safeText(), getRatingText(), getDistanceText()
```

### Interface callback
```java
public interface OnStoreClickListener {
    void onStoreClick(Store store);
}
```
Fragment truyền lambda khi khởi tạo adapter:
```java
storeAdapter = new StoreAdapter(requireContext(), storeList,
    store -> {
        Intent intent = new Intent(requireContext(), StoreDetailActivity.class);
        intent.putExtra("storeId", store.getId());
        // ... các thông tin khác
        startActivity(intent);
    }
);
```

### Views trong item_store.xml

| View ID | Loại | Nội dung |
|---------|------|----------|
| `img_store` | ImageView | Ảnh quán (load bằng Glide) |
| `tv_store_name` | TextView | Tên quán |
| `tv_store_country` | TextView | Địa chỉ |
| `tv_store_time` | TextView | Thời gian giao hàng |
| `tv_store_distance` | TextView | Khoảng cách (mock data) |
| `tv_store_rating` | TextView | Điểm đánh giá |
| `tv_store_badge` | TextView | Badge "Nổi bật" (chỉ item đầu) |

---

## 9. Adapter: `FoodAdapter.java`

**Vị trí:** `app/src/main/java/com/example/foodnow/adapters/FoodAdapter.java`

### Mục đích
Hiển thị danh sách món ăn trong `StoreDetailActivity`. Hỗ trợ **2 loại click**:
1. **Click vào card** → mở `FoodDetailActivity`
2. **Click nút "+"** → thêm vào giỏ hàng (callback cho TV3)

### Hai interface callback
```java
// Click vào card món → mở FoodDetailActivity
public interface OnFoodClickListener {
    void onFoodClick(Food food);
}

// Click nút + → thêm vào giỏ (TV3 sẽ implement CartManager)
public interface OnAddToCartListener {
    void onAddToCart(Food food);
}
```

### Views trong item_food.xml

| View ID | Loại | Nội dung |
|---------|------|----------|
| `img_food` | ImageView | Ảnh món ăn |
| `tv_food_title` | TextView | Tên món |
| `tv_food_description` | TextView | Mô tả ngắn |
| `tv_food_price` | TextView | Giá (định dạng VND) |
| `tv_food_rating` | TextView | Sao đánh giá |
| `btn_add_cart` | TextView | Nút "+" thêm vào giỏ |

---

## 10. Adapter: `RecommendedFoodAdapter.java`

**Vị trí:** `app/src/main/java/com/example/foodnow/adapters/RecommendedFoodAdapter.java`

### Mục đích
Hiển thị section **"Món gợi ý"** ở trang chủ dưới dạng lưới 2 cột (`GridLayoutManager`). Dữ liệu hiện tại là mock data tĩnh, chưa kết nối Firestore.

### Model `RecommendedFood`
```java
// Các trường: name, storeName, price, rating, imageUrl, isPopular
RecommendedFood("Phở Bò Đặc Biệt", "Phở Hà Nội", 65000, 4.8f, "url...", true)
```

---

## 11. Fragment: `HomeFragment.java`

**Vị trí:** `app/src/main/java/com/example/foodnow/fragments/HomeFragment.java`

### Mục đích
Đây là **tab Trang chủ** — màn hình trung tâm của app. HomeFragment là file phức tạp nhất của TV2, tích hợp 4 chức năng chính:

---

### 11.1 Tìm kiếm quán ăn (Search)

Người dùng gõ vào ô `EditText` (`et_search`), danh sách quán tự lọc theo thời gian thực:

```java
etSearch.addTextChangedListener(new TextWatcher() {
    @Override
    public void onTextChanged(CharSequence s, ...) {
        filterStores(s.toString().trim());
    }
    ...
});
```

**Cơ chế:** Tìm kiếm theo `name` hoặc `description` của store (không phân biệt hoa/thường):
```java
boolean searchMatch = query.isEmpty()
    || store.getName().toLowerCase().contains(lower)
    || store.getDescription().toLowerCase().contains(lower);
```

---

### 11.2 Lọc theo danh mục (Category Filter)

Người dùng nhấn chip danh mục → chỉ hiển thị quán thuộc loại đó:

```java
categoryAdapter = new CategoryAdapter(requireContext(), categoryList, category -> {
    String clickedId = category.getId();
    // Nhấn "Tất cả" hoặc nhấn lại chip đang chọn → bỏ lọc
    if ("all".equals(clickedId) || clickedId.equals(selectedCategoryId)) {
        selectedCategoryId = "";
    } else {
        selectedCategoryId = clickedId;
    }
    categoryAdapter.setSelectedCategory(selectedCategoryId);
    filterStores(etSearch.getText().toString().trim());
});
```

**Kết hợp search + filter (AND logic):**
```java
private void filterStores(String query) {
    for (Store store : allStoreList) {
        boolean categoryMatch = selectedCategoryId.isEmpty()
                || selectedCategoryId.equals(store.getCategoryId());
        boolean searchMatch = query.isEmpty()
                || store.getName().toLowerCase().contains(query.toLowerCase())
                || store.getDescription().toLowerCase().contains(query.toLowerCase());

        if (categoryMatch && searchMatch) {
            storeList.add(store);
        }
    }
}
```

**Hai danh sách riêng:**
- `allStoreList` — danh sách đầy đủ từ Firestore, **không bao giờ bị xóa**
- `storeList` — danh sách đang hiển thị sau khi lọc

---

### 11.3 Danh sách quán ăn

Hiển thị `RecyclerView` dọc với `StoreAdapter`. Khi nhấn vào quán → mở `StoreDetailActivity` và truyền đầy đủ thông tin qua Intent:

```java
Intent intent = new Intent(requireContext(), StoreDetailActivity.class);
intent.putExtra("storeId",          store.getId());
intent.putExtra("storeName",        store.getName());
intent.putExtra("storeImage",       store.getImageUrl());
intent.putExtra("storeRating",      store.getRating());
intent.putExtra("storeDeliveryTime",store.getDeliveryTime());
intent.putExtra("storeDeliveryFee", store.getDeliveryFee());
startActivity(intent);
```

---

### 11.4 Món gợi ý

Hiển thị `RecyclerView` lưới 2 cột với `RecommendedFoodAdapter`. Dữ liệu mock từ `seedMockRecommendedFoods()` — 4 món ăn phổ biến có ảnh từ Unsplash.

---

### 11.5 Observe data từ ViewModel

```java
private void observeData() {
    // Quan sát danh mục → cập nhật chip filter
    homeViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
        categoryList.clear();
        categoryList.add(new Category("all", "Tất cả", "")); // mục đầu tiên luôn là "Tất cả"
        categoryList.addAll(categories);
        categoryAdapter.notifyDataSetChanged();
    });

    // Quan sát quán ăn → lưu vào allStoreList, rồi áp filter
    homeViewModel.getStores().observe(getViewLifecycleOwner(), stores -> {
        allStoreList.clear();
        allStoreList.addAll(stores != null && !stores.isEmpty() ? stores : getMockStores());
        filterStores(etSearch.getText().toString().trim()); // giữ từ khóa đang nhập
    });
}
```

---

## 12. Activity: `StoreDetailActivity.java`

**Vị trí:** `app/src/main/java/com/example/foodnow/activities/StoreDetailActivity.java`

### Mục đích
Màn hình **chi tiết quán ăn** — hiển thị thông tin quán ở header và danh sách món ăn bên dưới.

### Luồng hoạt động

```
HomeFragment nhấn quán → Intent với storeId, storeName, storeImage, storeRating, ...
    → StoreDetailActivity.onCreate()
        ① Nhận dữ liệu từ Intent extras
        ② Hiển thị header: ảnh quán, tên, rating, thời gian, phí ship
        ③ Khởi tạo FoodAdapter với 2 callback (add cart + click detail)
        ④ Observe ViewModel → load danh sách món từ Firestore
        ⑤ Click món → mở FoodDetailActivity
        ⑥ Nút back → finish()
```

### Truyền dữ liệu xuống FoodDetailActivity
```java
foodAdapter.setOnFoodClickListener(food -> {
    Intent intent = new Intent(this, FoodDetailActivity.class);
    intent.putExtra("foodId",           food.getId());
    intent.putExtra("foodTitle",        food.getTitle());
    intent.putExtra("foodDescription",  food.getDescription());
    intent.putExtra("foodPrice",        food.getPrice());
    intent.putExtra("foodImageUrl",     food.getImageUrl());
    intent.putExtra("foodRating",       food.getRating());
    intent.putExtra("storeName",        storeName);
    intent.putExtra("storeDeliveryTime",storeTime);
    intent.putExtra("storeDeliveryFee", storeDeliveryFee);
    startActivity(intent);
});
```

### Định dạng phí giao hàng
```java
private String formatDeliveryFee(long fee) {
    if (fee <= 0) return "Miễn phí ship";
    NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    return "Ship " + nf.format(fee) + "đ"; // → "Ship 15.000đ"
}
```

---

## 13. Activity: `FoodDetailActivity.java`

**Vị trí:** `app/src/main/java/com/example/foodnow/activities/FoodDetailActivity.java`

### Mục đích
Màn hình **chi tiết món ăn** — hiển thị đầy đủ thông tin món, cho phép chọn số lượng, thêm giỏ hàng, và xem đánh giá.

### Các chức năng chính

#### ① Hiển thị thông tin món
Nhận dữ liệu từ Intent extras và hiển thị: ảnh lớn, tên, rating, số đánh giá, giá, mô tả, tên quán, thời gian giao hàng. Badge "Phổ biến" tự động hiện khi `rating >= 4.5`.

#### ② Điều khiển số lượng [−][n][+]
```java
btnDecrease.setOnClickListener(v -> {
    if (quantity > 1) {       // không cho giảm xuống dưới 1
        quantity--;
        tvQuantity.setText(String.valueOf(quantity));
        updateOrderButtonText(); // cập nhật giá trên nút "Đặt"
    }
});

btnIncrease.setOnClickListener(v -> {
    quantity++;
    tvQuantity.setText(String.valueOf(quantity));
    updateOrderButtonText();
});

private void updateOrderButtonText() {
    long total = unitPrice * quantity;
    btnOrder.setText("Đặt · " + currencyFormatter.format(total) + "đ");
    // Ví dụ: "Đặt · 130.000đ" khi chọn 2 phở 65.000đ
}
```

#### ③ Nút Yêu thích (toggle UI)
```java
btnFavorite.setOnClickListener(v -> {
    isFavorite = !isFavorite;
    ivFavoriteIcon.setImageResource(
        isFavorite ? android.R.drawable.btn_star_big_on
                   : android.R.drawable.btn_star_big_off);
    // Đổi màu icon theo trạng thái
});
```
> Hiện tại chỉ thay đổi UI, chưa lưu Firestore (TV3 sẽ kết nối `FavoriteRepository`).

#### ④ Nút Giỏ hàng & Đặt hàng
Hiện tại hiển thị Toast thông báo. Chờ TV3 hoàn thành `CartManager` để kết nối thật sự.

#### ⑤ Đánh giá mock (Reviews)
```java
List<Review> reviews = new ArrayList<>();
reviews.add(new Review("Nguyễn Văn A", "2 ngày trước", 5,
        "Món ăn rất ngon, phục vụ nhanh chóng. Sẽ ủng hộ thêm!", 12));
// ...
rvReviews.setAdapter(new ReviewAdapter(this, reviews));
```
Dùng `rvReviews.setNestedScrollingEnabled(false)` để RecyclerView cuộn cùng với ScrollView cha thay vì cuộn độc lập.

---

## 14. Sơ đồ luồng dữ liệu

```
Firestore (collection "Stores")
         │  addSnapshotListener (real-time)
         ▼
  StoreRepository.getAllStores()
         │  LiveData<List<Store>>
         ▼
  HomeViewModel.getStores()
         │  observe()
         ▼
  HomeFragment
    ├── allStoreList (lưu toàn bộ)
    ├── filterStores(query) ◄── etSearch (TextWatcher)
    │       └── storeList (đang hiển thị)      ◄── selectedCategoryId (chip click)
    └── StoreAdapter → item_store.xml
              │  click
              ▼
    Intent → StoreDetailActivity
                │  storeId
                ▼
    StoreDetailViewModel.getFoods(storeId)
                │  LiveData<List<Food>>
                ▼
    FoodAdapter → item_food.xml
              │  click card
              ▼
    Intent → FoodDetailActivity
              │  hiển thị chi tiết + chọn số lượng
              ▼
         [Chờ TV3: CartManager]
```

---

## 15. Điểm kết nối với TV3

TV2 và TV3 chia sẻ điểm kết nối tại **nút thêm vào giỏ hàng**. Hiện tại TV2 để placeholder Toast:

```java
// Trong StoreDetailActivity — TODO khi TV3 hoàn thành CartManager
foodAdapter = new FoodAdapter(this, foodList, food -> {
    // CartManager.getInstance().addItem(food);  ← TV3 sẽ làm điều này
    Toast.makeText(this, "Đã thêm: " + food.getTitle(), Toast.LENGTH_SHORT).show();
});
```

Khi TV3 hoàn thành `CartManager.java`, TV2 cần:
1. Thay Toast bằng `CartManager.getInstance().addItem(food, storeId, storeName)`
2. Xử lý trường hợp người dùng thêm món từ quán khác (hiện CartManager chỉ giữ món từ 1 quán)

---

*Tài liệu được tổng hợp từ source code tại commit hiện tại. Cập nhật nếu có thay đổi lớn trong logic.*
