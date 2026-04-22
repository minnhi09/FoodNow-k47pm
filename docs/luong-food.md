# 📖 Hướng dẫn newbie: Luồng Food — Quán ăn & Thực đơn (TV2)

> **Mục tiêu:** Giúp bạn hiểu từng dòng code trong luồng **Trang chủ → Chi tiết quán → Danh sách món ăn**.
> Sau khi đọc xong, bạn sẽ biết dữ liệu đi từ Firestore lên màn hình như thế nào, và tại sao code lại viết theo cách đó.

---

## Mục lục

1. [Luồng này làm gì?](#1-luồng-này-làm-gì)
2. [Sơ đồ dữ liệu (Data Flow)](#2-sơ-đồ-dữ-liệu-data-flow)
3. [Danh sách file trong luồng này](#3-danh-sách-file-trong-luồng-này)
4. [Giải thích từng file](#4-giải-thích-từng-file)
   - [Food.java — Model](#41-foodjava--model)
   - [FoodRepository.java — Lấy dữ liệu từ Firestore](#42-foodrepositoryjava--lấy-dữ-liệu-từ-firestore)
   - [StoreDetailViewModel.java — Trung gian](#43-storedetailviewmodeljava--trung-gian)
   - [item_food.xml — Giao diện 1 món ăn](#44-item_foodxml--giao-diện-1-món-ăn)
   - [FoodAdapter.java — Ghép dữ liệu vào giao diện](#45-foodadapterjava--ghép-dữ-liệu-vào-giao-diện)
   - [activity_store_detail.xml — Màn hình chi tiết quán](#46-activity_store_detailxml--màn-hình-chi-tiết-quán)
   - [StoreDetailActivity.java — Điều phối tất cả](#47-storedetailactivityjava--điều-phối-tất-cả)
   - [HomeFragment.java — Điểm khởi đầu](#48-homefragmentjava--điểm-khởi-đầu)
5. [Hiểu kiến trúc MVVM qua ví dụ thực tế](#5-hiểu-kiến-trúc-mvvm-qua-ví-dụ-thực-tế)
6. [Những khái niệm quan trọng cần nhớ](#6-những-khái-niệm-quan-trọng-cần-nhớ)
7. [Lỗi thường gặp & cách sửa](#7-lỗi-thường-gặp--cách-sửa)
8. [Checklist tự kiểm tra](#8-checklist-tự-kiểm-tra)

---

## 1. Luồng này làm gì?

Người dùng sẽ đi qua 2 màn hình:

```
[HomeFragment] ──nhấn vào quán──► [StoreDetailActivity]
   Danh sách quán                    Ảnh quán + thông tin
   (đã có sẵn)                       + danh sách món ăn
```

**Dữ liệu đến từ đâu?**
- Danh sách quán: Firestore → collection `Stores`
- Danh sách món ăn: Firestore → collection `Foods` (lọc theo `storeId`)

---

## 2. Sơ đồ dữ liệu (Data Flow)

Đây là sơ đồ quan trọng nhất. Hãy đọc từ trái sang phải:

```
FIRESTORE                REPOSITORY           VIEWMODEL           UI
──────────────────────────────────────────────────────────────────────
collection "Foods"
  {foodId}:            FoodRepository       StoreDetailViewModel
    title: "Phở bò"  ──getFoodsByStore()──► getFoods(storeId) ──► StoreDetailActivity
    price: 55000        LiveData<List>        LiveData<List>          │
    storeId: "abc"      (real-time)           (lazy init)             ▼
    ...                                                          FoodAdapter
                                                                   │
                                                                   ▼
                                                              item_food.xml
                                                              (hiển thị ra màn hình)
```

**Giải thích đơn giản:**
1. `FoodRepository` kết nối Firestore, nhận dữ liệu thô
2. `StoreDetailViewModel` giữ dữ liệu đó dưới dạng `LiveData` (sống sót khi xoay màn hình)
3. `StoreDetailActivity` quan sát (`observe`) LiveData, khi có dữ liệu mới → cập nhật UI
4. `FoodAdapter` nhận `List<Food>` → ghép vào `item_food.xml` → hiển thị ra màn hình

---

## 3. Danh sách file trong luồng này

```
app/src/main/java/com/example/foodnow/
├── models/
│   └── Food.java                        ← 1. Khuôn mẫu 1 món ăn
├── repositories/
│   └── FoodRepository.java              ← 2. Đọc Firestore
├── viewmodels/
│   └── StoreDetailViewModel.java        ← 3. Giữ dữ liệu
├── adapters/
│   └── FoodAdapter.java                 ← 5. Ghép dữ liệu vào layout
├── activities/
│   └── StoreDetailActivity.java         ← 7. Màn hình chi tiết quán
└── fragments/
    └── HomeFragment.java (đã sửa)       ← 8. Điểm bắt đầu luồng

app/src/main/res/layout/
├── item_food.xml                        ← 4. Giao diện 1 món ăn
└── activity_store_detail.xml            ← 6. Giao diện màn hình chi tiết
```

---

## 4. Giải thích từng file

### 4.1 `Food.java` — Model

**Đường dẫn:** `models/Food.java`

**Mục đích:** Đây là "khuôn mẫu" (template) cho 1 món ăn. Firestore trả về dữ liệu thô (JSON), Java cần 1 class để chứa dữ liệu đó.

**Firestore document trông như thế này:**
```json
{
  "title": "Phở bò tái",
  "description": "Phở bò truyền thống",
  "price": 55000,
  "imageUrl": "https://...",
  "rating": 4.7,
  "storeId": "abc123",
  "categoryId": "xyz456",
  "isAvailable": true
}
```

**Class Java tương ứng:**
```java
public class Food {
    private String id;           // lấy từ doc.getId() — không có trong Firestore document
    private String title;        // "Phở bò tái"
    private String description;  // "Phở bò truyền thống"
    private long price;          // 55000 (dùng long, không dùng int để tránh tràn số)
    private String imageUrl;     // URL ảnh
    private float rating;        // 4.7
    private String storeId;      // "abc123" — dùng để tìm món theo quán
    private String categoryId;   // "xyz456"
    private boolean isAvailable; // true/false
```

**⚠️ Quy tắc bắt buộc — Constructor rỗng:**
```java
// BẮT BUỘC! Nếu không có dòng này, app sẽ CRASH khi đọc Firestore
public Food() {}
```
> **Tại sao?** Firestore dùng cơ chế gọi là "deserialization": nhận JSON → tạo object Java.
> Cơ chế này cần gọi `new Food()` trước, rồi mới set từng field.
> Nếu không có constructor rỗng → Firestore không tạo được object → crash.

---

### 4.2 `FoodRepository.java` — Lấy dữ liệu từ Firestore

**Đường dẫn:** `repositories/FoodRepository.java`

**Mục đích:** Là lớp DUY NHẤT được phép "chạm" vào Firestore. ViewModel và Activity không được gọi Firestore trực tiếp.

```java
public class FoodRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<Food>> getFoodsByStore(String storeId) {
        MutableLiveData<List<Food>> liveData = new MutableLiveData<>();

        db.collection("Foods")                    // (1) Vào collection "Foods"
                .whereEqualTo("storeId", storeId) // (2) Lọc: chỉ lấy món của quán này
                .addSnapshotListener((snapshots, error) -> { // (3) Lắng nghe real-time
                    if (error != null || snapshots == null) return; // (4) Bỏ qua nếu lỗi

                    List<Food> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Food food = doc.toObject(Food.class); // (5) JSON → Java object
                        if (food != null) {
                            food.setId(doc.getId()); // (6) Gán ID từ document
                            list.add(food);
                        }
                    }
                    liveData.setValue(list); // (7) Thông báo dữ liệu đã sẵn sàng
                });

        return liveData;
    }
}
```

**Giải thích từng bước:**

| Bước | Code | Ý nghĩa |
|------|------|---------|
| (1) | `.collection("Foods")` | Chọn bảng "Foods" trong Firestore |
| (2) | `.whereEqualTo("storeId", storeId)` | WHERE storeId = 'abc123' (giống SQL) |
| (3) | `.addSnapshotListener(...)` | Tự động gọi lại mỗi khi dữ liệu thay đổi |
| (4) | `if (error != null) return` | Nếu lỗi → không làm gì |
| (5) | `doc.toObject(Food.class)` | Chuyển JSON document → object Food |
| (6) | `food.setId(doc.getId())` | ID không nằm trong document, phải gán thêm |
| (7) | `liveData.setValue(list)` | "Tôi có dữ liệu rồi, ai đang chờ thì nhận đi" |

**`LiveData` là gì?**
> Hãy tưởng tượng `LiveData` như một **bảng thông báo**. Repository ghi thông tin lên bảng. Activity/Fragment đứng xem bảng đó và tự động cập nhật khi có thông tin mới. Không cần gọi đi gọi lại.

---

### 4.3 `StoreDetailViewModel.java` — Trung gian

**Đường dẫn:** `viewmodels/StoreDetailViewModel.java`

**Mục đích:** Giữ dữ liệu khi người dùng xoay màn hình, làm cầu nối giữa Repository và Activity.

```java
public class StoreDetailViewModel extends ViewModel {

    private LiveData<List<Food>> foods; // Lưu kết quả query

    public LiveData<List<Food>> getFoods(String storeId) {
        if (foods == null) {             // Lần đầu gọi → mới query Firestore
            FoodRepository repo = new FoodRepository();
            foods = repo.getFoodsByStore(storeId);
        }
        return foods; // Lần sau gọi → trả luôn kết quả cũ (không query lại)
    }
}
```

**Tại sao cần `if (foods == null)`?**
> Nếu người dùng xoay màn hình, `StoreDetailActivity` bị tạo lại từ đầu, nhưng `ViewModel` **không bị tạo lại**. Nhờ check `null`, app chỉ query Firestore 1 lần dù xoay màn hình bao nhiêu lần.

**Tại sao không gọi Repository trực tiếp trong Activity?**
> Nếu Activity bị destroy (xoay màn hình, điện thoại thấp RAM...) → dữ liệu mất. ViewModel sống lâu hơn Activity → dữ liệu an toàn.

---

### 4.4 `item_food.xml` — Giao diện 1 món ăn

**Đường dẫn:** `res/layout/item_food.xml`

**Mục đích:** Định nghĩa giao diện của 1 item trong danh sách món ăn. `FoodAdapter` sẽ "bơm" dữ liệu vào file XML này.

**Cấu trúc layout:**
```
MaterialCardView (card trắng có viền)
└── LinearLayout (ngang)
    ├── FrameLayout (96x96dp) ← Ảnh món ăn
    │   ├── ImageView (img_food)
    │   └── LinearLayout (chip rating góc dưới)
    │       ├── ImageView (icon sao)
    │       └── TextView (tv_food_rating) "4.8"
    └── LinearLayout (dọc) ← Thông tin
        ├── TextView (tv_food_title) "Phở bò tái"
        ├── TextView (tv_food_description) "Phở bò truyền thống"
        └── LinearLayout (ngang)
            ├── TextView (tv_food_price) "55.000đ"
            └── TextView (btn_add_cart) "+"
```

**Các ID quan trọng cần nhớ:**

| ID | Loại | Mô tả |
|----|------|-------|
| `img_food` | ImageView | Ảnh món ăn |
| `tv_food_title` | TextView | Tên món |
| `tv_food_description` | TextView | Mô tả |
| `tv_food_price` | TextView | Giá tiền |
| `tv_food_rating` | TextView | Rating |
| `btn_add_cart` | TextView | Nút "+" thêm vào giỏ |

> **Tại sao `btn_add_cart` là `TextView` chứ không phải `Button`?**
> Vì `TextView` có thể style tự do hơn (bo tròn, màu cam...). Về mặt chức năng, `setOnClickListener` dùng được trên bất kỳ View nào.

---

### 4.5 `FoodAdapter.java` — Ghép dữ liệu vào giao diện

**Đường dẫn:** `adapters/FoodAdapter.java`

**Mục đích:** `RecyclerView` không tự hiển thị dữ liệu. Nó cần `Adapter` làm nhiệm vụ: "với item ở vị trí số 3, hãy lấy `Food` thứ 3 trong list và đặt vào `item_food.xml`".

**Cấu trúc adapter:**
```java
public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {

    // 1. Interface — định nghĩa sự kiện click
    public interface OnAddToCartListener {
        void onAddToCart(Food food);
    }

    // 2. Constructor — nhận dữ liệu từ bên ngoài
    public FoodAdapter(Context context, List<Food> foodList, OnAddToCartListener listener) { ... }

    // 3. onCreateViewHolder — "tạo 1 cái khung (ViewHolder) từ item_food.xml"
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new ViewHolder(view);
    }

    // 4. onBindViewHolder — "đổ dữ liệu vào khung ở vị trí position"
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Food food = foodList.get(position);
        holder.tvTitle.setText(food.getTitle());
        holder.tvPrice.setText(formatPrice(food.getPrice())); // "55.000đ"
        // ... set các view khác
        holder.btnAddCart.setOnClickListener(v -> listener.onAddToCart(food));
    }

    // 5. getItemCount — "list này có bao nhiêu item?"
    @Override
    public int getItemCount() { return foodList.size(); }

    // 6. ViewHolder — giữ tham chiếu đến từng view để không phải tìm lại mỗi lần
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvTitle, tvDescription, tvPrice, tvRating, btnAddCart;

        public ViewHolder(View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.img_food);
            tvTitle = itemView.findViewById(R.id.tv_food_title);
            // ...
        }
    }
}
```

**`ViewHolder` là gì và tại sao cần nó?**
> Mỗi lần scroll, `RecyclerView` gọi `onBindViewHolder`. Nếu mỗi lần đó gọi `findViewById()` → rất chậm (phải tìm view trong toàn bộ cây XML).
> `ViewHolder` lưu sẵn tham chiếu đến từng view → tìm 1 lần, dùng mãi mãi → scroll mượt hơn.

**Format giá tiền VND:**
```java
// Input: 55000  →  Output: "55.000đ"
NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
String price = nf.format(food.getPrice()) + "đ";
```

---

### 4.6 `activity_store_detail.xml` — Màn hình chi tiết quán

**Đường dẫn:** `res/layout/activity_store_detail.xml`

**Mục đích:** Định nghĩa toàn bộ giao diện màn hình chi tiết quán.

**Cấu trúc layout:**
```
CoordinatorLayout (layout gốc — hỗ trợ scroll phức tạp)
├── AppBarLayout (phần header cố định trên cùng)
│   ├── CollapsingToolbarLayout (ảnh quán có thể thu nhỏ khi scroll)
│   │   ├── ImageView (img_store_detail) — ảnh bìa quán
│   │   └── ImageView (btn_back) — nút quay lại
│   ├── LinearLayout — thông tin quán
│   │   ├── TextView (tv_store_detail_name) — tên quán
│   │   └── LinearLayout — rating + thời gian + phí ship
│   │       ├── TextView (tv_store_detail_rating)
│   │       ├── TextView (tv_store_detail_time)
│   │       └── TextView (tv_store_detail_fee)
│   └── LinearLayout — tiêu đề "Thực đơn"
└── RecyclerView (rv_foods) — danh sách món ăn
    (cuộn bên dưới AppBarLayout nhờ app:layout_behavior)
```

**`CoordinatorLayout` là gì?**
> Cho phép các view "phối hợp" với nhau khi scroll. Ví dụ: khi scroll list món ăn lên → header ảnh quán tự động thu nhỏ lại. Nếu dùng `LinearLayout` thông thường, header sẽ đứng yên che mất màn hình.

---

### 4.7 `StoreDetailActivity.java` — Điều phối tất cả

**Đường dẫn:** `activities/StoreDetailActivity.java`

**Mục đích:** Kết nối tất cả các thành phần lại. Đây là "bộ não" của màn hình chi tiết quán.

```java
public class StoreDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_detail); // ① Gắn layout

        // ② Ánh xạ các view (tìm view theo ID)
        imgStore = findViewById(R.id.img_store_detail);
        tvName = findViewById(R.id.tv_store_detail_name);
        // ...

        // ③ Nhận dữ liệu từ HomeFragment (truyền qua Intent)
        String storeId = getIntent().getStringExtra("storeId");
        String storeName = getIntent().getStringExtra("storeName");
        // ...

        // ④ Hiển thị thông tin quán lên header
        tvName.setText(storeName);
        Glide.with(this).load(storeImage).into(imgStore); // load ảnh
        // ...

        // ⑤ Setup RecyclerView danh sách món
        foodAdapter = new FoodAdapter(this, foodList, food -> {
            // Khi nhấn nút "+": tạm thời Toast (sẽ kết nối CartManager sau)
            Toast.makeText(this, "Đã thêm: " + food.getTitle(), Toast.LENGTH_SHORT).show();
        });
        rvFoods.setLayoutManager(new LinearLayoutManager(this));
        rvFoods.setAdapter(foodAdapter);

        // ⑥ Lấy ViewModel và bắt đầu lắng nghe dữ liệu
        viewModel = new ViewModelProvider(this).get(StoreDetailViewModel.class);
        viewModel.getFoods(storeId).observe(this, foods -> {
            // Mỗi khi Firestore có dữ liệu mới → chạy vào đây
            foodList.clear();
            foodList.addAll(foods);
            foodAdapter.notifyDataSetChanged(); // "RecyclerView ơi, vẽ lại đi!"
        });
    }
}
```

**`Intent` là gì?**
> `Intent` là "phong bì" để truyền dữ liệu giữa các màn hình.
> HomeFragment bỏ dữ liệu vào: `intent.putExtra("storeId", "abc123")`
> StoreDetailActivity mở phong bì ra: `getIntent().getStringExtra("storeId")`

**`observe` là gì?**
> Đăng ký lắng nghe LiveData. Mỗi khi dữ liệu thay đổi (Firestore cập nhật, v.v.), đoạn code trong `observe` sẽ tự động chạy. Không cần gọi lại thủ công.

---

### 4.8 `HomeFragment.java` — Điểm khởi đầu

**Phần đã được sửa** trong `setupStoreRecyclerView()`:

```java
// TRƯỚC: chỉ Toast
store -> Toast.makeText(..., "Quán: " + store.getName(), ...).show()

// SAU: mở StoreDetailActivity với đầy đủ dữ liệu
store -> {
    Intent intent = new Intent(requireContext(), StoreDetailActivity.class);
    intent.putExtra("storeId", store.getId());           // ID để query Foods
    intent.putExtra("storeName", store.getName());        // Tên quán
    intent.putExtra("storeImage", store.getImageUrl());  // Ảnh bìa
    intent.putExtra("storeRating", store.getRating());   // Rating
    intent.putExtra("storeDeliveryTime", store.getDeliveryTime()); // Thời gian
    intent.putExtra("storeDeliveryFee", store.getDeliveryFee());   // Phí ship
    startActivity(intent); // Mở màn hình mới
}
```

> **Tại sao truyền nhiều thứ như vậy?**
> Để hiển thị header ngay lập tức mà không cần query Firestore lần thứ 2 chỉ để lấy tên quán. Dữ liệu quán đã có sẵn từ HomeFragment → truyền thẳng qua Intent.

---

## 5. Hiểu kiến trúc MVVM qua ví dụ thực tế

Hãy theo dõi hành trình của 1 món ăn từ Firestore → màn hình:

```
Bước 1: StoreDetailActivity khởi động
        └─► viewModel.getFoods("store-abc")

Bước 2: StoreDetailViewModel kiểm tra
        └─► foods == null → gọi FoodRepository lần đầu

Bước 3: FoodRepository kết nối Firestore
        └─► db.collection("Foods").whereEqualTo("storeId","store-abc")
            .addSnapshotListener(...)

Bước 4: Firestore trả về 3 món ăn
        └─► FoodRepository chuyển JSON → List<Food>
            └─► liveData.setValue(list) → "tôi có dữ liệu rồi!"

Bước 5: StoreDetailActivity nhận thông báo (đang observe)
        └─► foodList.clear() + foodList.addAll(foods)
            └─► foodAdapter.notifyDataSetChanged()

Bước 6: FoodAdapter vẽ 3 item lên RecyclerView
        └─► onBindViewHolder() × 3
            └─► set text, load ảnh, set click listener

Bước 7: Màn hình hiển thị 3 món ăn ✅
```

---

## 6. Những khái niệm quan trọng cần nhớ

| Khái niệm | Giải thích đơn giản | File ví dụ |
|-----------|--------------------|-----------:|
| **Model** | Khuôn mẫu dữ liệu (1 món ăn có những gì?) | `Food.java` |
| **Repository** | Nơi DUY NHẤT đọc/ghi Firestore | `FoodRepository.java` |
| **ViewModel** | Giữ dữ liệu an toàn, sống qua xoay màn hình | `StoreDetailViewModel.java` |
| **LiveData** | Dữ liệu "biết tự thông báo" khi thay đổi | trả về bởi `getFoods()` |
| **Adapter** | Cầu nối giữa List dữ liệu và RecyclerView | `FoodAdapter.java` |
| **ViewHolder** | Cache tham chiếu đến view để tránh `findViewById` lặp | trong `FoodAdapter` |
| **Intent** | Phong bì truyền dữ liệu giữa màn hình | trong `HomeFragment` |
| **observe** | Đăng ký nhận thông báo khi LiveData thay đổi | trong `StoreDetailActivity` |

---

## 7. Lỗi thường gặp & cách sửa

### ❌ App crash ngay khi mở StoreDetailActivity
**Nguyên nhân thường gặp:** `NullPointerException` do `storeId` là null
```java
// Kiểm tra storeId trước khi dùng
if (storeId != null && !storeId.isEmpty()) {
    viewModel.getFoods(storeId).observe(...);
}
```

### ❌ Danh sách món ăn trống hoàn toàn
**Kiểm tra theo thứ tự:**
1. Vào Firebase Console → Firestore → collection `Foods` → có document nào không?
2. Field `storeId` trong Firestore có đúng với `storeId` được truyền qua Intent không?
3. Tên collection đúng chưa? Phải là `"Foods"` (chữ F hoa)

### ❌ Crash với thông báo: `no-arg constructor`
**Nguyên nhân:** `Food.java` thiếu constructor rỗng
```java
// Thêm dòng này vào Food.java
public Food() {}
```

### ❌ Ảnh quán/món không hiển thị (chỉ thấy placeholder)
**Nguyên nhân:** URL ảnh sai, hoặc thiếu permission INTERNET
- Kiểm tra `AndroidManifest.xml` có dòng: `<uses-permission android:name="android.permission.INTERNET" />`
- Kiểm tra URL trong Firestore còn valid không

### ❌ `notifyDataSetChanged()` không cập nhật UI
**Nguyên nhân:** Đang set list mới cho adapter thay vì `clear()` + `addAll()`
```java
// ❌ SAI: gán list mới — adapter vẫn giữ tham chiếu list cũ
foodAdapter = new FoodAdapter(this, foods, ...);

// ✅ ĐÚNG: sửa đúng list mà adapter đang giữ
foodList.clear();
foodList.addAll(foods);
foodAdapter.notifyDataSetChanged();
```

### ❌ Nhấn nút back không về HomeFragment
**Nguyên nhân:** Chưa đăng ký `StoreDetailActivity` trong `AndroidManifest.xml`
```xml
<!-- Thêm vào AndroidManifest.xml trong thẻ <application> -->
<activity android:name=".activities.StoreDetailActivity" />
```

---

## 8. Checklist tự kiểm tra

Sau khi đọc xong, bạn có thể tự kiểm tra hiểu bài bằng cách trả lời:

- [ ] Tại sao `Food.java` cần constructor rỗng?
- [ ] `FoodRepository` lọc món theo quán bằng câu lệnh nào?
- [ ] Tại sao cần `ViewModel`? Nếu không có ViewModel thì sao?
- [ ] `LiveData.observe()` hoạt động như thế nào?
- [ ] `ViewHolder` trong Adapter giải quyết vấn đề gì?
- [ ] Dữ liệu quán được truyền từ `HomeFragment` sang `StoreDetailActivity` bằng cách nào?
- [ ] Khi Firestore có dữ liệu mới, đoạn code nào trong Activity tự động chạy?
- [ ] Tại sao `if (foods == null)` trong ViewModel lại quan trọng?

---

> 📝 **Bước tiếp theo sau khi hiểu luồng này:**
> Tích hợp nút "Thêm vào giỏ" với `CartManager` (TV3 làm) và kết nối vào `CartFragment`.
> Xem file: `utils/CartManager.java` và `fragments/CartFragment.java`
