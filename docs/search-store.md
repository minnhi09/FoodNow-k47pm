# Hướng dẫn hiểu code: Tính năng Search Quán Ăn

> **Dành cho:** Thành viên mới chưa quen Android, muốn hiểu cách tính năng tìm kiếm hoạt động.
> **File liên quan:** `HomeFragment.java` · `fragment_home.xml`

---

## 1. Tổng quan — Tính năng làm gì?

Người dùng gõ tên quán vào ô tìm kiếm → danh sách quán ăn phía dưới tự động lọc theo từ khóa — **ngay lập tức, không cần nhấn nút**.

```
User gõ "Phở"
     ↓
TextWatcher phát hiện thay đổi
     ↓
filterStores("Phở") được gọi
     ↓
Lọc allStoreList → chỉ giữ quán có tên chứa "phở"
     ↓
storeList = [Phở Hà Nội, Phở Bò Kho, ...]
     ↓
RecyclerView cập nhật hiển thị
```

**Ưu điểm:** Không cần gọi thêm Firebase — dữ liệu đã có trong bộ nhớ, lọc ngay trên thiết bị → nhanh, không tốn băng thông.

---

## 2. Sơ đồ luồng dữ liệu

```
Firebase Firestore
      │
      │ (realtime snapshot)
      ▼
HomeViewModel.getStores()
      │
      │ LiveData<List<Store>>
      ▼
HomeFragment.observeData()
      │
      ▼
allStoreList ← lưu TOÀN BỘ (không bao giờ xóa)
      │
      │ filterStores(query)
      ▼
storeList   ← chỉ chứa kết quả lọc
      │
      ▼
StoreAdapter → RecyclerView → Màn hình
```

---

## 3. Các file thay đổi

| File | Thay đổi |
|------|----------|
| `res/layout/fragment_home.xml` | `TextView` tĩnh → `EditText` thật; thêm `tv_no_results` |
| `fragments/HomeFragment.java` | Thêm `allStoreList`, `setupSearch()`, `filterStores()` |

---

## 4. Giải thích XML — `fragment_home.xml`

### 4.1 Ô tìm kiếm (trước & sau)

**Trước (UI giả):**
```xml
<!-- Không nhập được, chỉ là ảo giả -->
<TextView
    android:text="Tìm món ăn, quán ăn..."
    android:textColor="@color/home_search_hint" />
```

**Sau (EditText thật):**
```xml
<EditText
    android:id="@+id/et_search"
    android:layout_width="0dp"
    android:layout_weight="1"
    android:background="@null"           ← không hiện viền, hòa vào LinearLayout
    android:hint="Tìm món ăn, quán ăn..."
    android:textColorHint="@color/home_search_hint"
    android:textColor="@color/home_text_primary"
    android:textSize="18sp"
    android:singleLine="true"            ← chỉ 1 dòng, không xuống hàng
    android:imeOptions="actionSearch"    ← bàn phím hiện nút 🔍 thay vì Enter
    android:inputType="text" />
```

> 💡 **`android:background="@null"`**: Xóa nền mặc định của EditText (hình chữ nhật trắng có viền dưới). Nhờ đó EditText "ẩn" vào bên trong `LinearLayout` đã có background riêng (bo tròn, màu kem).

> 💡 **`android:layout_width="0dp"` + `layout_weight="1"`**: EditText chiếm toàn bộ chiều rộng còn lại trong `LinearLayout` ngang (sau icon kính lúp).

### 4.2 Empty state (thông báo không tìm thấy)

```xml
<TextView
    android:id="@+id/tv_no_results"
    android:text="Không tìm thấy quán ăn phù hợp 🍽️"
    android:visibility="gone" />   ← ẩn mặc định
```

> 💡 **`visibility="gone"`**: View không hiển thị VÀ không chiếm không gian (khác với `invisible` — ẩn nhưng vẫn chiếm chỗ). Code Java sẽ chuyển thành `VISIBLE` khi danh sách lọc ra rỗng.

---

## 5. Giải thích Java — `HomeFragment.java`

### 5.1 Dual-list pattern (hai danh sách)

```java
// Danh sách GỐC — lưu toàn bộ store từ Firestore
// Không bao giờ bị xóa trừ khi có data mới từ server
private final List<Store> allStoreList = new ArrayList<>();

// Danh sách HIỂN THỊ — RecyclerView đọc từ đây
// Thay đổi mỗi khi user gõ vào ô search
private final List<Store> storeList = new ArrayList<>();
```

**Tại sao cần 2 danh sách?**

Nếu chỉ dùng 1 danh sách:
- User gõ "Phở" → `storeList` chỉ còn `[Phở Hà Nội]`
- User xóa chữ "Phở" → không còn dữ liệu gốc để phục hồi ❌

Với 2 danh sách:
- `allStoreList` luôn đầy đủ: `[Phở Hà Nội, Cơm Tấm, Pizza House, ...]`
- User xóa hết → `filterStores("")` → `storeList = allStoreList` → phục hồi đủ ✅

---

### 5.2 TextWatcher — lắng nghe từng ký tự

```java
private void setupSearch() {
    etSearch.addTextChangedListener(new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Gọi TRƯỚC KHI text thay đổi — thường để trống
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Gọi MỖI KHI user gõ hoặc xóa ký tự
            filterStores(s.toString().trim());   // trim() bỏ khoảng trắng đầu/cuối
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Gọi SAU KHI text đã thay đổi — thường để trống
        }
    });
}
```

> 💡 **`TextWatcher`** là một *interface* (hợp đồng) — bạn phải implement 3 method của nó. Chỉ cần dùng `onTextChanged`, hai method còn lại để trống.

> 💡 **`s.toString().trim()`**: Chuyển `CharSequence` sang `String` và bỏ khoảng trắng hai đầu. Nếu user nhấn Space rồi gõ "Phở" → `"  Phở"` → `.trim()` → `"Phở"` → tìm đúng.

---

### 5.3 filterStores() — trái tim của search

```java
private void filterStores(String query) {
    storeList.clear();                    // ① Xóa danh sách hiển thị cũ

    if (query.isEmpty()) {
        storeList.addAll(allStoreList);   // ② Query rỗng → hiển thị tất cả
    } else {
        String lower = query.toLowerCase();   // ③ Chuyển query sang chữ thường

        for (Store store : allStoreList) {    // ④ Duyệt từng quán
            boolean nameMatch = store.getName() != null
                    && store.getName().toLowerCase().contains(lower);
            boolean descMatch = store.getDescription() != null
                    && store.getDescription().toLowerCase().contains(lower);

            if (nameMatch || descMatch) {     // ⑤ Khớp tên HOẶC mô tả
                storeList.add(store);
            }
        }
    }

    storeAdapter.notifyDataSetChanged();      // ⑥ Báo RecyclerView cập nhật UI
    tvNoResults.setVisibility(              // ⑦ Hiện/ẩn empty state
            storeList.isEmpty() ? View.VISIBLE : View.GONE);
}
```

**Giải thích từng bước:**

| Bước | Code | Ý nghĩa |
|------|------|---------|
| ① | `storeList.clear()` | Xóa kết quả cũ để không bị trùng |
| ② | `storeList.addAll(allStoreList)` | Nếu không có từ khóa → hiện hết |
| ③ | `query.toLowerCase()` | Tìm kiếm không phân biệt hoa/thường: "PHO" = "pho" = "Phở" |
| ④ | `for (Store store : allStoreList)` | Duyệt toàn bộ danh sách gốc |
| ⑤ | `nameMatch \|\| descMatch` | Khớp tên quán **hoặc** mô tả đều được nhận |
| ⑥ | `notifyDataSetChanged()` | Thông báo RecyclerView "danh sách đã thay đổi, vẽ lại đi" |
| ⑦ | `setVisibility(...)` | Nếu lọc ra rỗng → hiện "Không tìm thấy..." |

> 💡 **`.contains(lower)`**: Kiểm tra chuỗi có chứa chuỗi con không. `"Phở Hà Nội".contains("phở")` → `true` (sau khi `.toLowerCase()`).

> ⚠️ **Tại sao check `!= null` trước `.toLowerCase()`?** Nếu `store.getName()` là `null` (field chưa có trong Firestore) → gọi `.toLowerCase()` sẽ ném `NullPointerException` và crash app. Luôn kiểm tra null trước khi dùng method trên String.

---

### 5.4 observeData() — cập nhật khi Firestore thay đổi

```java
homeViewModel.getStores().observe(getViewLifecycleOwner(), stores -> {
    allStoreList.clear();
    if (stores != null && !stores.isEmpty()) {
        allStoreList.addAll(stores);        // Lưu vào master list
    } else {
        addMockStores();                    // Fallback: dùng data mock
    }
    // Áp lại từ khóa đang nhập (nếu có)
    filterStores(etSearch.getText().toString().trim());
});
```

> 💡 **Tại sao gọi `filterStores()` ở cuối?** Giả sử user đang gõ "Phở" → lúc này Firestore gửi data mới → `allStoreList` được cập nhật → nếu không gọi lại `filterStores`, `storeList` sẽ hiển thị tất cả thay vì chỉ kết quả lọc "Phở". Gọi lại để đảm bảo nhất quán.

---

## 6. Toàn bộ luồng step-by-step

```
① App khởi động, HomeFragment.onViewCreated() chạy

② setupSearch() đăng ký TextWatcher lên et_search
   (chưa có gì, chờ user gõ)

③ observeData() đăng ký lắng nghe Firestore
   → Firestore trả về 5 stores
   → allStoreList = [Phở Hà Nội, Cơm Tấm, Pizza, Trà Sữa, Lẩu Bò]
   → filterStores("") → storeList = allStoreList → hiển thị 5 quán

④ User gõ chữ "P"
   → TextWatcher.onTextChanged("P")
   → filterStores("P")
   → Duyệt allStoreList:
       - "Phở Hà Nội".contains("p") = true ✅
       - "Cơm Tấm Sài Gòn".contains("p") = false ✗
       - "Pizza House".contains("p") = true ✅
       - "Trà Sữa ToCoToCo".contains("p") = false ✗
       - "Lẩu Bò Năm Sao".contains("p") = false ✗
   → storeList = [Phở Hà Nội, Pizza House]
   → notifyDataSetChanged() → RecyclerView hiện 2 quán
   → tvNoResults GONE

⑤ User gõ thêm "h" → "Ph"
   → filterStores("ph")
   → "Pizza House".contains("ph") = false ✗
   → storeList = [Phở Hà Nội]
   → RecyclerView hiện 1 quán

⑥ User gõ "phxxx" (không khớp gì)
   → storeList = []
   → tvNoResults VISIBLE → "Không tìm thấy quán ăn phù hợp 🍽️"

⑦ User xóa hết → ""
   → filterStores("")
   → storeList = allStoreList → hiển thị 5 quán lại
```

---

## 7. Các khái niệm quan trọng

### 7.1 TextWatcher là gì?

`TextWatcher` là một **interface** trong Android để lắng nghe thay đổi của `EditText`. Giống như "tai nghe" được gắn vào ô nhập liệu.

```java
// Interface TextWatcher có 3 method bắt buộc:
public interface TextWatcher {
    void beforeTextChanged(CharSequence s, int start, int count, int after);
    void onTextChanged(CharSequence s, int start, int before, int count);
    void afterTextChanged(Editable s);
}
```

Chúng ta dùng `onTextChanged` — gọi mỗi khi text thay đổi (gõ thêm hoặc xóa).

### 7.2 notifyDataSetChanged() là gì?

`RecyclerView` không tự biết khi `storeList` thay đổi. Phải "thông báo" thủ công:

```
storeList thay đổi → RecyclerView không biết
                  → gọi notifyDataSetChanged()
                  → RecyclerView vẽ lại toàn bộ items
```

### 7.3 View.VISIBLE / View.GONE / View.INVISIBLE

| Hằng số | Hiển thị | Chiếm không gian |
|---------|----------|-----------------|
| `View.VISIBLE` | Có | Có |
| `View.INVISIBLE` | Không | Có |
| `View.GONE` | Không | Không |

> Empty state dùng `GONE` vì khi ẩn không muốn nó chiếm chỗ trong layout.

### 7.4 CharSequence vs String

```java
void onTextChanged(CharSequence s, ...) {
    filterStores(s.toString().trim());
}
```

- `CharSequence`: kiểu dữ liệu cơ bản hơn `String`, dùng nội bộ trong Android
- `.toString()`: chuyển sang `String` thông thường để dùng `.contains()`, `.toLowerCase()`, v.v.

---

## 8. Lỗi thường gặp & cách sửa

### Lỗi 1: App crash khi gõ tìm kiếm
```
NullPointerException at filterStores
```
**Nguyên nhân:** `store.getName()` trả về `null` → gọi `.toLowerCase()` trên null.
**Cách sửa:** Luôn check `!= null` trước:
```java
// Sai ❌
store.getName().toLowerCase().contains(lower)

// Đúng ✅
store.getName() != null && store.getName().toLowerCase().contains(lower)
```

### Lỗi 2: Tìm kiếm không hoạt động, RecyclerView không cập nhật
**Nguyên nhân:** Quên gọi `storeAdapter.notifyDataSetChanged()`.
**Cách sửa:** Đảm bảo gọi sau khi sửa `storeList`.

### Lỗi 3: Xóa hết chữ nhưng không hiện lại tất cả quán
**Nguyên nhân:** Quan sát `addMockStores()` đang thêm vào `storeList` thay vì `allStoreList`.
**Cách sửa:** Fallback mock data phải thêm vào `allStoreList`:
```java
// Sai ❌
storeList.add(buildStore(...));   // thêm vào list hiển thị

// Đúng ✅
allStoreList.add(buildStore(...)); // thêm vào master list
```

### Lỗi 4: EditText bị viền xấu
**Nguyên nhân:** Thiếu `android:background="@null"`.
**Cách sửa:** Thêm `android:background="@null"` vào EditText để xóa nền mặc định.

### Lỗi 5: Bàn phím không hiện nút 🔍
**Nguyên nhân:** Thiếu `android:imeOptions="actionSearch"`.
**Cách sửa:** Thêm `android:imeOptions="actionSearch"` và `android:inputType="text"` vào EditText.

---

## 9. Câu hỏi tự kiểm tra

1. Tại sao cần `allStoreList` riêng biệt thay vì dùng 1 danh sách?
2. `TextWatcher` có bao nhiêu method bắt buộc? Chúng ta dùng method nào?
3. Sự khác biệt giữa `View.GONE` và `View.INVISIBLE` là gì?
4. Tại sao phải check `store.getName() != null` trước khi gọi `.toLowerCase()`?
5. Sau khi sửa `storeList`, phải gọi method nào để RecyclerView cập nhật?
6. Nếu Firestore gửi data mới trong lúc user đang gõ "Phở", kết quả tìm kiếm có bị mất không? Tại sao?
7. `android:background="@null"` trong EditText có tác dụng gì?

<details>
<summary>Đáp án</summary>

1. Vì nếu chỉ có 1 list, khi lọc sẽ mất data gốc, không thể phục hồi khi user xóa từ khóa.
2. 3 methods: `beforeTextChanged`, `onTextChanged`, `afterTextChanged`. Chúng ta dùng `onTextChanged`.
3. `GONE`: ẩn và không chiếm chỗ. `INVISIBLE`: ẩn nhưng vẫn chiếm chỗ trong layout.
4. Vì `null.toLowerCase()` ném `NullPointerException` và crash app.
5. `storeAdapter.notifyDataSetChanged()`.
6. Không bị mất — vì cuối `observeData()` có gọi lại `filterStores(etSearch.getText().toString().trim())`, tự động áp lại từ khóa đang nhập.
7. Xóa nền mặc định (viền dưới và nền trắng) của EditText, để nó hòa vào LinearLayout có background riêng.

</details>

---

*Tài liệu này được tạo bởi TV2 — FoodNow Project*
