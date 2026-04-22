# T2_FOOD_DETAIL_NEWBIE_GUIDE.md

> **Mục tiêu:** Giải thích chi tiết luồng code màn hình **Chi tiết món ăn** (`FoodDetailActivity`) cho người mới học Android — từ lúc click vào món ăn cho đến khi màn hình chi tiết hiển thị đầy đủ.

---

## 1. Tổng quan luồng

```
[StoreDetailActivity]
   └── RecyclerView (FoodAdapter)
         └── Click vào card món ──────────────────────────────┐
                                                              ▼
                                                   [FoodDetailActivity]
                                                   ┌─────────────────────┐
                                                   │  Hero image         │
                                                   │  Tên + Rating + Giá │
                                                   │  Mô tả              │
                                                   │  Card nhà hàng      │
                                                   │  Card thông tin món  │
                                                   │  Danh sách đánh giá │
                                                   ├─────────────────────┤
                                                   │ [STICKY BOTTOM BAR] │
                                                   │  [−] [1] [+]        │
                                                   │  [Giỏ hàng] [Đặt]   │
                                                   └─────────────────────┘
```

---

## 2. File được tạo / sửa

| File | Hành động | Ghi chú |
|------|-----------|---------|
| `models/Review.java` | Tạo mới | Data class cho đánh giá mock |
| `res/layout/item_review.xml` | Tạo mới | Layout 1 card đánh giá |
| `adapters/ReviewAdapter.java` | Tạo mới | RecyclerView adapter đánh giá |
| `res/layout/activity_food_detail.xml` | Tạo mới | Layout toàn màn hình chi tiết |
| `activities/FoodDetailActivity.java` | Tạo mới | Logic của màn hình |
| `adapters/FoodAdapter.java` | Sửa | Thêm `OnFoodClickListener` + card click |
| `activities/StoreDetailActivity.java` | Sửa | Implement food click → start Activity |
| `AndroidManifest.xml` | Sửa | Đăng ký FoodDetailActivity |
| `drawable/bg_home_back_circle.xml` | Tạo mới | Nền tròn cho nút back/favorite |

---

## 3. Pattern chính: Sticky Bottom Bar

### Vấn đề
Khi có nội dung cuộn, bottom bar phải **luôn hiển thị ở dưới cùng** dù cuộn tới đâu.

### Giải pháp: `ConstraintLayout` + `NestedScrollView`

```xml
<ConstraintLayout>
    <!-- Vùng cuộn: top=parent, bottom=TOP của bottom_bar -->
    <NestedScrollView
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toTopOf="@id/bottom_bar" />

    <!-- Bottom bar cố định: bottom=parent -->
    <LinearLayout
        android:id="@+id/bottom_bar"
        app:layout_constraintBottom_toBottomOf="parent" />
</ConstraintLayout>
```

**Tại sao hoạt động?**
- `NestedScrollView` bị "ép" kết thúc ngay trên `bottom_bar` → nó không bao giờ chồng lên
- `bottom_bar` luôn dán vào đáy màn hình
- Nội dung trong `NestedScrollView` cuộn tự do bên trong vùng đó

---

## 4. Cách truyền dữ liệu: Intent Extras

### Bên gửi — `StoreDetailActivity.java`

```java
foodAdapter.setOnFoodClickListener(food -> {
    Intent intent = new Intent(this, FoodDetailActivity.class);
    // Dữ liệu món ăn
    intent.putExtra("foodId",          food.getId());
    intent.putExtra("foodTitle",        food.getTitle());
    intent.putExtra("foodDescription",  food.getDescription());
    intent.putExtra("foodPrice",        food.getPrice());       // long
    intent.putExtra("foodImageUrl",     food.getImageUrl());
    intent.putExtra("foodRating",       food.getRating());      // float
    // Dữ liệu quán (StoreDetailActivity đã có sẵn)
    intent.putExtra("storeName",        finalStoreName);
    intent.putExtra("storeDeliveryTime",finalStoreTime);
    intent.putExtra("storeDeliveryFee", finalDeliveryFee);      // long
    startActivity(intent);
});
```

### Bên nhận — `FoodDetailActivity.java`

```java
// Đọc ra từ Intent
String foodTitle = getIntent().getStringExtra("foodTitle");
long   unitPrice = getIntent().getLongExtra("foodPrice", 0L);
float  rating    = getIntent().getFloatExtra("foodRating", 0f);
// ... v.v.
```

> ⚠️ **Lưu ý:** Với `float` và `long` phải dùng `getFloatExtra` / `getLongExtra` (không phải `getStringExtra`) và cần cung cấp giá trị mặc định (tham số thứ 2).

---

## 5. Cách FoodAdapter thêm 2 loại click riêng biệt

### Tình huống
Card món ăn có 2 hành động:
- **Click vào card** → mở chi tiết món
- **Click nút "+"** → thêm vào giỏ hàng

### Giải pháp: 2 Interface riêng

```java
// Interface 1: click card
public interface OnFoodClickListener {
    void onFoodClick(Food food);
}

// Interface 2: click nút +
public interface OnAddToCartListener {
    void onAddToCart(Food food);
}
```

```java
// Trong onBindViewHolder:
holder.itemView.setOnClickListener(v -> {
    if (foodClickListener != null) foodClickListener.onFoodClick(food);
});

holder.btnAddCart.setOnClickListener(v -> {
    if (cartListener != null) cartListener.onAddToCart(food);
});
```

```java
// Trong StoreDetailActivity, gán listener riêng lẻ:
foodAdapter = new FoodAdapter(this, foodList, food -> {
    // Giỏ hàng: Toast tạm
    Toast.makeText(this, "Đã thêm: " + food.getTitle(), Toast.LENGTH_SHORT).show();
});
foodAdapter.setOnFoodClickListener(food -> {
    // Mở FoodDetailActivity
    Intent intent = new Intent(this, FoodDetailActivity.class);
    // ... putExtra ...
    startActivity(intent);
});
```

---

## 6. Logic số lượng (Quantity Counter)

```java
private int quantity = 1;     // Số lượng hiện tại (min = 1)
private long unitPrice = 0L;  // Giá 1 đơn vị

// [−] giảm
btnDecrease.setOnClickListener(v -> {
    if (quantity > 1) {           // Không cho giảm xuống 0
        quantity--;
        tvQuantity.setText(String.valueOf(quantity));
        updateOrderButtonText();  // Cập nhật giá
    }
});

// [+] tăng
btnIncrease.setOnClickListener(v -> {
    quantity++;
    tvQuantity.setText(String.valueOf(quantity));
    updateOrderButtonText();
});

// Cập nhật label nút "Đặt"
private void updateOrderButtonText() {
    long total = unitPrice * quantity;
    btnOrder.setText("Đặt · " + currencyFormatter.format(total) + "đ");
}
```

**Điểm quan trọng:**
- `unitPrice` là giá gốc (lưu riêng, không bao giờ thay đổi)
- `total = unitPrice * quantity` tính lại mỗi lần bấm

---

## 7. Nút yêu thích (Toggle)

```java
private boolean isFavorite = false;

btnFavorite.setOnClickListener(v -> {
    isFavorite = !isFavorite;            // Đổi trạng thái
    ivFavoriteIcon.setImageResource(
        isFavorite ? R.drawable.btn_star_big_on    // ❤️
                   : R.drawable.btn_star_big_off); // 🤍
    // Đổi màu theo trạng thái
    int tintColor = isFavorite ? R.color.home_badge_red : R.color.home_primary_orange;
    ivFavoriteIcon.setColorFilter(getColor(tintColor));
});
```

> ⚠️ **Hiện tại:** Chỉ là UI toggle, không lưu vào Firestore. Tính năng lưu yêu thích thật sẽ cần collection `Favorites` trong Firestore (xem db.md).

---

## 8. Mock Reviews — Tại sao không dùng Firestore?

Firestore hiện không có collection `Reviews` trong schema (db.md). Để màn hình trông đầy đủ, dùng mock data cứng:

```java
private void setupMockReviews() {
    List<Review> reviews = new ArrayList<>();
    reviews.add(new Review(
        "Nguyễn Văn A",    // reviewerName
        "2 ngày trước",    // timeAgo
        5,                 // rating (1-5)
        "Món ăn rất ngon, phục vụ nhanh chóng.",  // comment
        12                 // likes
    ));
    // ... thêm reviews ...

    rvReviews.setLayoutManager(new LinearLayoutManager(this));
    rvReviews.setNestedScrollingEnabled(false);  // ← QUAN TRỌNG
    rvReviews.setAdapter(new ReviewAdapter(this, reviews));
}
```

**Tại sao `nestedScrollingEnabled = false`?**
- `rvReviews` nằm bên trong `NestedScrollView`
- Nếu để `true`, RecyclerView sẽ "tranh" cuộn với NestedScrollView → scroll bị giật
- Khi `false`, RecyclerView expand full height và để NestedScrollView xử lý scroll

---

## 9. ReviewAdapter — Hiển thị sao theo rating

```java
// Mảng 5 ImageView sao
ImageView[] stars = {holder.star1, holder.star2, holder.star3, holder.star4, holder.star5};
int rating = review.getRating();  // Ví dụ: 4

for (int i = 0; i < 5; i++) {
    stars[i].setColorFilter(context.getColor(
        i < rating              // Sao thứ i có được tô màu không?
            ? R.color.home_primary_orange  // Cam = đầy
            : R.color.home_border));       // Xám = trống
}
```

**Ví dụ với rating = 4:**
- i=0: 0 < 4 → cam ⭐
- i=1: 1 < 4 → cam ⭐
- i=2: 2 < 4 → cam ⭐
- i=3: 3 < 4 → cam ⭐
- i=4: 4 < 4 = false → xám ☆

---

## 10. Badge "Phổ biến"

```java
// Trong receiveIntentData()
float foodRating = getIntent().getFloatExtra("foodRating", 0f);
if (foodRating >= 4.5f) {
    tvPopularBadge.setVisibility(View.VISIBLE);
    // layout mặc định là visibility="gone"
}
```

Badge dùng drawable `bg_home_popular_chip` (đã tồn tại từ màn hình Home) — tái dùng lại.

---

## 11. Cách đăng ký Activity mới trong AndroidManifest.xml

```xml
<application ...>
    <!-- ... các activity khác ... -->
    <activity android:name=".activities.StoreDetailActivity" />
    <activity android:name=".activities.FoodDetailActivity" />   ← thêm dòng này
</application>
```

> ❌ **Lỗi thường gặp:** Quên đăng ký activity → app crash với `ActivityNotFoundException` khi gọi `startActivity()`.

---

## 12. Các lỗi thường gặp và cách sửa

### Lỗi 1: `NullPointerException` khi bind View

```
java.lang.NullPointerException: Attempt to invoke virtual method on a null object reference
```

**Nguyên nhân:** `findViewById()` trả về `null` — sai ID hoặc gọi trước `setContentView()`.

**Sửa:** Đảm bảo `bindViews()` được gọi **sau** `setContentView()` và ID trong Java khớp với XML.

---

### Lỗi 2: Số lượng giảm xuống 0 hoặc âm

**Nguyên nhân:** Thiếu guard `if (quantity > 1)`.

```java
// SAI
btnDecrease.setOnClickListener(v -> {
    quantity--;  // Có thể thành 0, -1, ...
});

// ĐÚNG
btnDecrease.setOnClickListener(v -> {
    if (quantity > 1) quantity--;  // Min = 1
});
```

---

### Lỗi 3: Giá trên nút "Đặt" không cập nhật

**Nguyên nhân:** Quên gọi `updateOrderButtonText()` sau khi thay đổi `quantity`.

```java
// SAI
quantity++;
tvQuantity.setText(String.valueOf(quantity));

// ĐÚNG
quantity++;
tvQuantity.setText(String.valueOf(quantity));
updateOrderButtonText();  // ← BẮT BUỘC
```

---

### Lỗi 4: RecyclerView review cuộn bị giật

**Nguyên nhân:** `rvReviews.setNestedScrollingEnabled(true)` (mặc định) khi nằm trong `NestedScrollView`.

**Sửa:**
```java
rvReviews.setNestedScrollingEnabled(false);
```

---

### Lỗi 5: Intent extra float/long trả về 0

**Nguyên nhân:** Dùng sai phương thức getExtra.

```java
// SAI
float rating = Float.parseFloat(getIntent().getStringExtra("foodRating"));

// ĐÚNG
float rating = getIntent().getFloatExtra("foodRating", 0f);
long price   = getIntent().getLongExtra("foodPrice", 0L);
```

---

## 13. Q&A cho newbie

**Q: Tại sao dùng Activity thay vì BottomSheet?**
A: Màn hình chi tiết có nhiều nội dung (hero image lớn, card nhà hàng, card thông tin, danh sách đánh giá). BottomSheet thường dùng cho popup nhỏ gọn. Activity cho phép toàn màn hình và navigation stack đúng.

**Q: Tại sao `unitPrice` lưu riêng thay vì đọc từ `tvPrice.getText()`?**
A: Đọc từ TextView trả về String đã format ("65.000đ") → phải parse lại → dễ bug. Lưu `long unitPrice` là nguồn dữ liệu sạch, không bao giờ nhầm.

**Q: `final` trước `finalStoreName` trong StoreDetailActivity để làm gì?**
A: Lambda trong Java chỉ có thể capture biến `effectively final`. Biến `storeName` có thể bị reassign → phải copy sang `final`:
```java
final String finalStoreName = storeName;
foodAdapter.setOnFoodClickListener(food -> {
    intent.putExtra("storeName", finalStoreName);  // OK
});
```

**Q: Khi nào thì "Thêm vào giỏ" sẽ hoạt động thật?**
A: Khi TV3 tạo `CartManager.java`. Lúc đó chỉ cần thay Toast bằng:
```java
CartManager.getInstance().addItem(food, quantity);
```

**Q: Làm sao để lưu trạng thái yêu thích vào Firestore?**
A: Tạo `FavoriteRepository.java` → gọi `addFavorite(userId, foodId)` trong `btnFavorite.setOnClickListener`. Collection `Favorites` đã có trong schema db.md.

---

## 14. Sơ đồ phụ thuộc file

```
Review.java ──────────────────────────┐
                                       ▼
item_review.xml ──────────────────► ReviewAdapter.java
                                       │
                                       ▼
activity_food_detail.xml ──────────► FoodDetailActivity.java
                                       ▲
FoodAdapter.java (OnFoodClickListener) │
         ▲                             │
StoreDetailActivity.java ─────────────┘

AndroidManifest.xml (đăng ký FoodDetailActivity)
```

---

*Tài liệu này được tạo bởi TV2 — FoodNow Project*
