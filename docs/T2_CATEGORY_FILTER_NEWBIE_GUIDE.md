# T2 - Hướng dẫn luồng code: Lọc quán ăn theo danh mục

> Tài liệu dành cho người mới tham gia dự án FoodNow. Giải thích **toàn bộ** cơ chế lọc danh mục (category filter) từ XML → Adapter → Fragment.

---

## 1. Tính năng này làm gì?

Khi người dùng nhấn vào một chip danh mục (Phở, Pizza, …), danh sách quán ăn bên dưới sẽ thu hẹp lại chỉ còn những quán thuộc danh mục đó. Chip đang chọn đổi màu nền thành cam. Nhấn lại chip hoặc nhấn "Tất cả" → hiển thị lại tất cả quán.

```
┌────────────────────────────────────────────────┐
│  [🍜 Tất cả]  [🍲 Phở]  [🍕 Pizza]  [🍵 Trà]  │  ← rv_categories
│                                                │
│  ┌──────────────────────────────────────────┐  │
│  │ 🍜 Phở Hà Nội          ⭐ 4.8  20 phút  │  │  ← chỉ hiện quán
│  └──────────────────────────────────────────┘  │    có categoryId = "pho"
└────────────────────────────────────────────────┘
```

**Kết hợp với search:** Người dùng có thể vừa lọc danh mục VÀ vừa gõ từ khóa — cả hai filter hoạt động đồng thời.

---

## 2. Các file liên quan

```
app/src/main/
├── java/com/example/foodnow/
│   ├── models/
│   │   └── Store.java                  ← thêm field categoryId
│   ├── adapters/
│   │   └── CategoryAdapter.java        ← thêm selected state
│   └── fragments/
│       └── HomeFragment.java           ← thêm selectedCategoryId, sửa filterStores()
└── res/
    ├── layout/
    │   └── item_category.xml           ← thêm id cho FrameLayout icon
    └── drawable/
        ├── bg_home_category_icon.xml          ← nền mặc định (cam nhạt)
        └── bg_home_category_icon_selected.xml ← nền khi chọn (cam đậm)  ← MỚI
```

---

## 3. Nền tảng: Store cần biết nó thuộc danh mục nào

### Vấn đề

`CategoryAdapter` hiển thị chip danh mục. `StoreAdapter` hiển thị quán ăn. Hai cái này không liên kết với nhau trước đây.

### Giải pháp

Thêm field `categoryId` vào `Store.java`:

```java
// Store.java
private String categoryId; // ví dụ: "pho", "pizza", "dessert"

public String getCategoryId() { return categoryId; }
public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
```

**Trong Firestore**, mỗi document trong collection `Stores` cần có field `categoryId` trùng với `id` của document trong collection `Categories`.

**Ví dụ:**
```
Stores/
  store001/
    name: "Phở Hà Nội"
    categoryId: "pho"      ← trỏ đến Categories/pho

Categories/
  pho/
    name: "Phở"
    imageUrl: "..."
```

---

## 4. Lưu trạng thái chip đang chọn — selectedCategoryId

### Trong HomeFragment.java

```java
// Biến trạng thái — nhớ danh mục đang được chọn
private String selectedCategoryId = ""; // "" = không lọc (Tất cả)
```

**Quy ước:**
| Giá trị `selectedCategoryId` | Ý nghĩa |
|---|---|
| `""` (chuỗi rỗng) | Không lọc — hiển thị tất cả quán |
| `"pho"` | Chỉ hiển thị quán có `categoryId = "pho"` |
| `"pizza"` | Chỉ hiển thị quán có `categoryId = "pizza"` |

---

## 5. Xử lý click danh mục

```java
// HomeFragment.java — setupCategoryRecyclerView()
categoryAdapter = new CategoryAdapter(
    requireContext(),
    categoryList,
    category -> {
        String clickedId = category.getId();

        // Click "Tất cả" HOẶC click lại chip đang active → bỏ chọn
        if ("all".equals(clickedId) || clickedId.equals(selectedCategoryId)) {
            selectedCategoryId = "";
        } else {
            selectedCategoryId = clickedId;
        }

        // Báo cho adapter đổi màu chip
        categoryAdapter.setSelectedCategory(selectedCategoryId);

        // Lọc lại danh sách (giữ nguyên từ khóa search)
        filterStores(etSearch.getText().toString().trim());
    }
);
```

**Bảng logic click:**

| Đang chọn | Người dùng click | Kết quả |
|---|---|---|
| `""` (Tất cả) | "Phở" | `selectedCategoryId = "pho"` |
| `"pho"` | "Phở" (click lại) | `selectedCategoryId = ""` (bỏ chọn) |
| `"pho"` | "Pizza" | `selectedCategoryId = "pizza"` |
| `"pizza"` | "Tất cả" | `selectedCategoryId = ""` |

---

## 6. filterStores() — Combined Filter

Đây là hàm cốt lõi. Nó xử lý **cả hai** filter cùng một lúc:

```java
private void filterStores(String query) {
    storeList.clear();
    String lower = query.toLowerCase();

    for (Store store : allStoreList) {
        // ── ĐIỀU KIỆN 1: Category ─────────────────────────
        // Nếu selectedCategoryId rỗng → bỏ qua filter này (chấp nhận tất cả)
        // Nếu có giá trị → chỉ nhận store nào có categoryId trùng khớp
        boolean categoryMatch = selectedCategoryId.isEmpty()
                || selectedCategoryId.equals(store.getCategoryId());

        // ── ĐIỀU KIỆN 2: Search query ─────────────────────
        // Nếu query rỗng → bỏ qua filter này (chấp nhận tất cả)
        // Nếu có query → tìm trong tên hoặc mô tả quán
        boolean searchMatch = query.isEmpty()
                || (store.getName() != null && store.getName().toLowerCase().contains(lower))
                || (store.getDescription() != null && store.getDescription().toLowerCase().contains(lower));

        // Chỉ thêm vào danh sách khi THỎA MÃN CẢ HAI điều kiện (AND)
        if (categoryMatch && searchMatch) {
            storeList.add(store);
        }
    }

    storeAdapter.notifyDataSetChanged();
    tvNoResults.setVisibility(storeList.isEmpty() ? View.VISIBLE : View.GONE);
}
```

### Sơ đồ luồng:

```
allStoreList (tất cả quán từ Firestore)
      │
      ├─ for each store:
      │     │
      │     ├─ categoryMatch? ── selectedCategoryId rỗng? → YES
      │     │                 └─ store.categoryId == selectedCategoryId? → YES/NO
      │     │
      │     ├─ searchMatch?   ── query rỗng? → YES
      │     │                 └─ name.contains(lower)? → YES/NO
      │     │                 └─ description.contains(lower)? → YES/NO
      │     │
      │     └─ categoryMatch AND searchMatch? → thêm vào storeList
      │
storeList (kết quả lọc) → hiển thị trên RecyclerView
```

### Ví dụ cụ thể:

**Chọn "Phở" + gõ "Hà":**
| Quán | categoryId | Tên | categoryMatch | searchMatch | Kết quả |
|---|---|---|---|---|---|
| Phở Hà Nội | pho | Phở Hà Nội | ✅ | ✅ (chứa "hà") | **Hiển thị** |
| Phở Sài Gòn | pho | Phở Sài Gòn | ✅ | ❌ | Ẩn |
| Pizza Hà Nội | pizza | Pizza Hà Nội | ❌ | ✅ | Ẩn |
| Trà sữa Đài Loan | dessert | Trà sữa | ❌ | ❌ | Ẩn |

---

## 7. CategoryAdapter — Visual selected state

### Vấn đề cũ

`CategoryAdapter` không biết chip nào đang được chọn → không thể đổi màu.

### Giải pháp: thêm state + method

```java
// CategoryAdapter.java
private String selectedCategoryId = ""; // trạng thái nội bộ của adapter

// Gọi từ bên ngoài để cập nhật
public void setSelectedCategory(String categoryId) {
    this.selectedCategoryId = categoryId == null ? "" : categoryId;
    notifyDataSetChanged(); // refresh toàn bộ list
}
```

### Áp dụng trong onBindViewHolder:

```java
@Override
public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
    Category category = categoryList.get(position);
    holder.tvName.setText(category.getName());
    holder.imgCategory.setImageResource(getCategoryIconRes(category.getName()));

    // Kiểm tra chip này có đang được chọn không
    boolean isSelected = category.getId() != null
            && category.getId().equals(selectedCategoryId);

    // Đổi màu nền: cam đậm khi chọn, cam nhạt khi không
    holder.iconContainer.setBackgroundResource(
            isSelected ? R.drawable.bg_home_category_icon_selected
                       : R.drawable.bg_home_category_icon);

    // Đổi màu icon: trắng khi chọn, cam khi không
    holder.imgCategory.setColorFilter(context.getColor(
            isSelected ? R.color.white : R.color.home_primary_orange));

    // Đổi màu chữ: cam khi chọn, đen khi không
    holder.tvName.setTextColor(context.getColor(
            isSelected ? R.color.home_primary_orange : R.color.home_text_primary));

    holder.itemView.setOnClickListener(v -> {
        if (listener != null) listener.onCategoryClick(category);
    });
}
```

---

## 8. Drawables — hai trạng thái nền chip

### `bg_home_category_icon.xml` (không chọn — cam nhạt)

```xml
<shape android:shape="rectangle">
    <solid android:color="@color/home_chip_bg" />  <!-- #F5E5D7 -->
    <corners android:radius="16dp" />
</shape>
```

### `bg_home_category_icon_selected.xml` (đang chọn — cam đậm)

```xml
<shape android:shape="rectangle">
    <solid android:color="@color/home_primary_orange" />  <!-- #D46E1F -->
    <corners android:radius="16dp" />
</shape>
```

**Kết quả trực quan:**

```
Không chọn:                    Đang chọn:
┌──────────────┐               ┌──────────────┐
│  [🍲 cam]    │               │  [🍲 trắng]  │  ← nền cam đậm
│   Phở        │               │   Phở        │  ← text cam
└──────────────┘               └──────────────┘
```

---

## 9. item_category.xml — thêm id cho FrameLayout

Để `CategoryAdapter` có thể truy cập FrameLayout (container của icon) và đổi background, cần đặt `id`:

```xml
<FrameLayout
    android:id="@+id/frame_category_icon"   ← THÊM CÁI NÀY
    android:layout_width="64dp"
    android:layout_height="64dp"
    android:background="@drawable/bg_home_category_icon">
```

Trong ViewHolder:
```java
public static class CategoryViewHolder extends RecyclerView.ViewHolder {
    FrameLayout iconContainer;   ← THÊM
    ImageView imgCategory;
    TextView tvName;

    public CategoryViewHolder(@NonNull View itemView) {
        super(itemView);
        iconContainer = itemView.findViewById(R.id.frame_category_icon);  ← THÊM
        imgCategory = itemView.findViewById(R.id.img_category);
        tvName = itemView.findViewById(R.id.tv_category_name);
    }
}
```

---

## 10. Luồng tổng thể — 8 bước

```
① App khởi động
    → HomeFragment.onViewCreated()
    → setupCategoryRecyclerView() — khởi tạo adapter, chưa có gì chọn
    → observeData() — lắng nghe Firestore

② Firestore trả về danh sách stores
    → allStoreList.clear() + allStoreList.addAll(stores)
    → filterStores("") — hiển thị tất cả

③ Người dùng nhấn chip "Phở"
    → categoryAdapter.onCategoryClick(category) được gọi
    → selectedCategoryId = "pho"
    → categoryAdapter.setSelectedCategory("pho") → notifyDataSetChanged()
        → chip "Phở" đổi nền cam đậm + icon trắng + text cam
    → filterStores(etSearch.getText()) — lọc lại

④ filterStores("pho" filter + "" search)
    → duyệt allStoreList
    → chỉ giữ store có store.getCategoryId() == "pho"
    → storeList = [Phở Hà Nội, Phở Sài Gòn]
    → storeAdapter.notifyDataSetChanged() → RecyclerView cập nhật

⑤ Người dùng gõ "hà" vào search box
    → TextWatcher.onTextChanged("hà") → filterStores("hà")
    → categoryMatch: selectedCategoryId="pho" vẫn còn
    → searchMatch: lọc thêm tên chứa "hà"
    → storeList = [Phở Hà Nội]

⑥ Người dùng nhấn "Phở" lần nữa (bỏ chọn)
    → clickedId.equals(selectedCategoryId) → true
    → selectedCategoryId = ""
    → categoryAdapter.setSelectedCategory("") → chip về màu mặc định
    → filterStores("hà") — chỉ còn filter search
    → storeList = tất cả quán có "hà" trong tên

⑦ Người dùng xóa hết text
    → filterStores("") — không filter gì cả
    → storeList = allStoreList → hiển thị tất cả

⑧ Nếu storeList rỗng
    → tvNoResults.setVisibility(VISIBLE) — "Không tìm thấy quán ăn phù hợp 🍽️"
```

---

## 11. Lỗi thường gặp

### 🐛 Lỗi 1: NullPointerException khi click category

**Nguyên nhân:** `category.getId()` trả về `null` (Firestore không có field `id`).

**Lý do thực sự:** `id` của document Firestore phải được set thủ công sau khi deserialize:
```java
// CategoryRepository.java
Category category = doc.toObject(Category.class);
category.setId(doc.getId()); // ← bắt buộc
```

**Dấu hiệu:** App crash khi click chip, LogCat hiện `NullPointerException at CategoryAdapter`.

---

### 🐛 Lỗi 2: Filter không hoạt động với data thật từ Firestore

**Nguyên nhân:** Stores trong Firestore không có field `categoryId`, hoặc giá trị không khớp với `id` của category.

**Cách kiểm tra:** Vào Firebase Console → Stores → kiểm tra document, tìm field `categoryId`. Phải trùng với document ID trong collection `Categories`.

---

### 🐛 Lỗi 3: Chip không đổi màu khi click

**Nguyên nhân 1:** Quên thêm `android:id="@+id/frame_category_icon"` vào `item_category.xml` → `iconContainer` là `null` → NPE hoặc không đổi.

**Nguyên nhân 2:** `notifyDataSetChanged()` bên trong `setSelectedCategory()` bị thiếu → adapter không redraw.

---

### 🐛 Lỗi 4: Sau khi chọn category, search không hoạt động

**Nguyên nhân:** `filterStores()` không được gọi từ `TextWatcher` nữa (đã thay thế bằng code khác).

**Kiểm tra:** `setupSearch()` phải gọi `filterStores(s.toString().trim())` trong `onTextChanged`.

---

### 🐛 Lỗi 5: Chọn "Tất cả" không reset chip đang chọn

**Nguyên nhân:** Kiểm tra `"all".equals(clickedId)` phải đúng với `id` của item "Tất cả". Trong `observeData()`, item "Tất cả" được thêm với `id = "all"`:

```java
categoryList.add(new Category("all", "Tất cả", ""));
```

Đảm bảo trùng khớp với điều kiện trong click handler.

---

## 12. Câu hỏi tự kiểm tra

**Q1:** `filterStores()` được gọi từ những nơi nào? (3 nơi)
> A: (1) `TextWatcher.onTextChanged()`, (2) category click handler, (3) `observeData()` sau khi Firestore cập nhật.

**Q2:** Tại sao phải dùng `allStoreList` thay vì lọc thẳng trên `storeList`?
> A: Vì `storeList` đã bị lọc rồi — nếu lọc lại trên nó, những item bị ẩn sẽ không bao giờ xuất hiện trở lại. `allStoreList` luôn giữ đủ tất cả.

**Q3:** Điều gì xảy ra nếu `store.getCategoryId()` trả về `null`?
> A: `selectedCategoryId.equals(null)` → `false` → store không thỏa `categoryMatch` → bị ẩn. Để fix: kiểm tra `store.getCategoryId() != null` trước khi `.equals()`.

**Q4:** Tại sao cần `notifyDataSetChanged()` trong `setSelectedCategory()`?
> A: Vì RecyclerView không tự biết data thay đổi. `notifyDataSetChanged()` báo adapter "redraw tất cả item", từ đó `onBindViewHolder()` chạy lại và áp màu mới.

**Q5:** Làm sao để search và category filter hoạt động cùng nhau?
> A: Vì `filterStores()` dùng `AND` — `if (categoryMatch && searchMatch)`. Cả hai điều kiện đều phải `true` thì store mới xuất hiện.

**Q6:** Tại sao icon color phải set mỗi lần `onBindViewHolder` thay vì set một lần trong XML?
> A: Vì RecyclerView tái sử dụng ViewHolder (view recycling). Nếu chỉ set trong XML, item cũ sẽ mang trạng thái của item trước → màu sắc sai.

**Q7:** Làm sao thêm danh mục mới vào app?
> A: (1) Thêm document vào Firestore collection `Categories`. (2) Thêm icon tương ứng trong `getCategoryIconRes()` của `CategoryAdapter`. (3) Đảm bảo stores có `categoryId` trùng với id document vừa thêm.
