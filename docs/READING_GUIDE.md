# 📖 Hướng dẫn đọc code FoodNow cho người mới

> Tài liệu này giúp bạn hiểu cách đọc và navigate qua source code của ứng dụng FoodNow một cách có hệ thống.

---

## Mục lục

1. [Tổng quan về app](#1-tổng-quan-về-app)
2. [Công nghệ sử dụng](#2-công-nghệ-sử-dụng)
3. [Cấu trúc thư mục](#3-cấu-trúc-thư-mục)
4. [Kiến trúc MVVM đơn giản](#4-kiến-trúc-mvvm-đơn-giản)
5. [Lộ trình đọc code](#5-lộ-trình-đọc-code)
6. [Chi tiết từng layer](#6-chi-tiết-từng-layer)
7. [Ví dụ thực tế: Luồng hiển thị danh mục](#7-ví-dụ-thực-tế-luồng-hiển-thị-danh-mục)
8. [Tips đọc code hiệu quả](#8-tips-đọc-code-hiệu-quả)

---

## 1. Tổng quan về app

**FoodNow** là ứng dụng đặt đồ ăn trên Android với các tính năng:

- 🔐 Đăng nhập / Đăng ký tài khoản
- 🏠 Trang chủ hiển thị danh mục và quán ăn
- 🍔 Xem chi tiết quán ăn và món ăn
- 🛒 Thêm vào giỏ hàng và đặt hàng
- ❤️ Lưu quán/món yêu thích
- 👤 Quản lý thông tin cá nhân

---

## 2. Công nghệ sử dụng

| Công nghệ | Mục đích |
|-----------|----------|
| **Java** | Ngôn ngữ lập trình chính |
| **XML** | Thiết kế giao diện (layout) |
| **Firebase Auth** | Đăng nhập, đăng ký |
| **Firebase Firestore** | Cơ sở dữ liệu (lưu Users, Stores, Foods...) |
| **Glide** | Tải và hiển thị ảnh |
| **Cloudinary** | Upload ảnh lên cloud |
| **LiveData** | Cập nhật UI tự động khi dữ liệu thay đổi |
| **ViewModel** | Quản lý dữ liệu cho UI |

---

## 3. Cấu trúc thư mục

```
app/src/main/
├── java/com/example/foodnow/
│   ├── MainActivity.java      ← Activity chính, chứa 4 Fragment
│   │
│   ├── activities/            ← Các màn hình riêng lẻ
│   │   ├── LoginActivity.java
│   │   └── RegisterActivity.java
│   │
│   ├── fragments/             ← Các tab trong MainActivity
│   │   ├── HomeFragment.java      ← Tab "Trang chủ"
│   │   ├── OrdersFragment.java    ← Tab "Đơn hàng"
│   │   ├── FavoritesFragment.java ← Tab "Yêu thích"
│   │   └── ProfileFragment.java   ← Tab "Tài khoản"
│   │
│   ├── models/                ← Các class dữ liệu
│   │   ├── User.java
│   │   ├── Category.java
│   │   ├── Store.java
│   │   └── ...
│   │
│   ├── repositories/          ← Lớp truy xuất Firebase
│   │   ├── AuthRepository.java
│   │   ├── CategoryRepository.java
│   │   └── ...
│   │
│   ├── viewmodels/            ← Quản lý logic và dữ liệu
│   │   ├── AuthViewModel.java
│   │   ├── HomeViewModel.java
│   │   └── ...
│   │
│   ├── adapters/              ← Adapter cho RecyclerView
│   │   └── CategoryAdapter.java
│   │
│   └── utils/                 ← Các hàm tiện ích
│       └── CloudinaryHelper.java
│
└── res/
    ├── layout/                ← File XML giao diện
    │   ├── activity_main.xml
    │   ├── fragment_home.xml
    │   ├── item_category.xml
    │   └── ...
    │
    └── menu/
        └── bottom_nav_menu.xml  ← Menu thanh điều hướng
```

---

## 4. Kiến trúc MVVM đơn giản

### MVVM là gì?

**MVVM** = **M**odel - **V**iew - **V**iew**M**odel

Đây là cách tổ chức code để:
- Tách biệt logic xử lý và giao diện
- Dễ bảo trì và mở rộng
- Code sạch hơn

### Sơ đồ tổng quan

```
┌─────────────────────────────────────────────────────────────┐
│                         NGƯỜI DÙNG                          │
│                      (Nhấn nút, vuốt...)                    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      VIEW (Giao diện)                       │
│                                                             │
│   • Activity, Fragment  ← Màn hình                          │
│   • XML Layout          ← Thiết kế giao diện                │
│   • Adapter             ← Hiển thị danh sách                │
│                                                             │
│   Nhiệm vụ: Hiển thị UI, nhận sự kiện từ người dùng         │
└─────────────────────────────────────────────────────────────┘
                              │
           Gọi method         │         Observe LiveData
           (ViewModel.xxx())  │         (dữ liệu thay đổi → UI cập nhật)
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    VIEWMODEL (Bộ não)                       │
│                                                             │
│   • HomeViewModel, AuthViewModel...                         │
│   • Chứa LiveData (dữ liệu reactive)                        │
│                                                             │
│   Nhiệm vụ: Xử lý logic, chuẩn bị dữ liệu cho View          │
└─────────────────────────────────────────────────────────────┘
                              │
           Gọi method         │         Trả về LiveData
           (Repository.xxx()) │         
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                 REPOSITORY (Kho dữ liệu)                    │
│                                                             │
│   • CategoryRepository, StoreRepository...                  │
│   • Kết nối với Firebase Firestore                          │
│                                                             │
│   Nhiệm vụ: Đọc/ghi dữ liệu từ/vào database                 │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    MODEL (Dữ liệu)                          │
│                                                             │
│   • User, Store, Food, Category...                          │
│   • Các class Java đơn giản với getter/setter               │
│                                                             │
│   Nhiệm vụ: Định nghĩa cấu trúc dữ liệu                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   FIREBASE FIRESTORE                        │
│                      (Cloud Database)                       │
└─────────────────────────────────────────────────────────────┘
```

### Ví dụ đơn giản

Khi người dùng mở app và muốn xem danh sách danh mục (Categories):

1. **View** (`HomeFragment`): Hiển thị màn hình, cần dữ liệu danh mục
2. **ViewModel** (`HomeViewModel`): "OK, tôi sẽ lấy dữ liệu từ Repository"
3. **Repository** (`CategoryRepository`): Kết nối Firebase, lấy danh sách Categories
4. **Model** (`Category`): Dữ liệu được chuyển thành đối tượng Category
5. **ViewModel** → **View**: Dữ liệu trả về qua LiveData, UI tự động cập nhật

---

## 5. Lộ trình đọc code

### Bước 1: Hiểu điểm khởi đầu (Entry Point)

Đọc file này trước:

| File | Mô tả |
|------|-------|
| `AndroidManifest.xml` | Xem app khởi động từ Activity nào |

Trong file này, tìm:
```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
```
→ Activity chứa đoạn này sẽ là màn hình đầu tiên (ở đây là `LoginActivity`)

### Bước 2: Đọc luồng đăng nhập

| Thứ tự | File | Mục đích |
|--------|------|----------|
| 1 | `activities/LoginActivity.java` | Màn hình đăng nhập |
| 2 | `viewmodels/AuthViewModel.java` | Xử lý logic đăng nhập |
| 3 | `repositories/AuthRepository.java` | Gọi Firebase Auth |
| 4 | `models/User.java` | Cấu trúc dữ liệu User |

### Bước 3: Đọc luồng trang chủ

| Thứ tự | File | Mục đích |
|--------|------|----------|
| 1 | `MainActivity.java` | Activity chính, chứa 4 Fragment |
| 2 | `fragments/HomeFragment.java` | Tab "Trang chủ" |
| 3 | `viewmodels/HomeViewModel.java` | Logic cho trang chủ |
| 4 | `repositories/CategoryRepository.java` | Lấy danh sách danh mục |
| 5 | `models/Category.java` | Cấu trúc dữ liệu Category |
| 6 | `adapters/CategoryAdapter.java` | Hiển thị danh mục trong RecyclerView |

### Bước 4: Đọc các file layout XML

| File | Mô tả |
|------|-------|
| `res/layout/activity_main.xml` | Giao diện MainActivity |
| `res/layout/fragment_home.xml` | Giao diện trang chủ |
| `res/layout/item_category.xml` | Mỗi item danh mục |

---

## 6. Chi tiết từng layer

### 6.1 Model - Lớp dữ liệu

**Vị trí:** `models/`

**Quy tắc:**
- Phải có **constructor rỗng** (Firestore yêu cầu)
- Phải có **getter và setter** cho mỗi field

**Ví dụ `Category.java`:**

```java
public class Category {

    private String id;
    private String name;
    private String imageUrl;

    // Constructor rỗng — BẮT BUỘC cho Firestore
    public Category() {}

    // Constructor đầy đủ
    public Category(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    // Getter
    public String getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }

    // Setter
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
```

**💡 Tại sao cần constructor rỗng?**

Khi Firestore lấy dữ liệu, nó sẽ:
1. Tạo object bằng constructor rỗng: `new Category()`
2. Gọi setter để gán giá trị từng field

---

### 6.2 Repository - Lớp truy xuất dữ liệu

**Vị trí:** `repositories/`

**Nhiệm vụ:** Kết nối với Firebase Firestore, đọc/ghi dữ liệu

**Ví dụ `CategoryRepository.java`:**

```java
public class CategoryRepository {

    private final FirebaseFirestore db;

    public CategoryRepository() {
        // Lấy instance của Firestore
        db = FirebaseFirestore.getInstance();
    }

    /** Lấy tất cả danh mục từ Firestore */
    public LiveData<List<Category>> getAllCategories() {
        // Tạo LiveData để chứa kết quả
        MutableLiveData<List<Category>> liveData = new MutableLiveData<>();

        // Lắng nghe thay đổi từ collection "Categories"
        db.collection("Categories")
                .addSnapshotListener((snapshots, error) -> {
                    // Nếu có lỗi thì thoát
                    if (error != null || snapshots == null) return;

                    // Chuyển documents thành List<Category>
                    List<Category> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Category category = doc.toObject(Category.class);
                        category.setId(doc.getId());
                        list.add(category);
                    }

                    // Cập nhật LiveData → ViewModel sẽ nhận được
                    liveData.setValue(list);
                });

        return liveData;
    }
}
```

**💡 Giải thích:**

- `addSnapshotListener`: Lắng nghe real-time, khi dữ liệu trên Firestore thay đổi → callback được gọi
- `doc.toObject(Category.class)`: Tự động chuyển document thành object Category
- `liveData.setValue()`: Cập nhật giá trị, tất cả nơi đang observe sẽ nhận được

---

### 6.3 ViewModel - Lớp xử lý logic

**Vị trí:** `viewmodels/`

**Nhiệm vụ:** 
- Chuẩn bị dữ liệu cho View
- Giữ dữ liệu sống sót khi xoay màn hình

**Ví dụ `HomeViewModel.java`:**

```java
public class HomeViewModel extends ViewModel {

    private final LiveData<List<Category>> categories;

    public HomeViewModel() {
        // Tạo Repository và lấy dữ liệu
        CategoryRepository categoryRepo = new CategoryRepository();
        categories = categoryRepo.getAllCategories();
    }

    // Expose LiveData cho View
    public LiveData<List<Category>> getCategories() {
        return categories;
    }
}
```

**💡 Giải thích:**

- Kế thừa `ViewModel` để Android quản lý lifecycle
- Dữ liệu không mất khi xoay màn hình
- View chỉ có thể `observe` LiveData, không thể sửa trực tiếp

---

### 6.4 View - Lớp giao diện

**Vị trí:** `fragments/`, `activities/`

**Nhiệm vụ:**
- Hiển thị UI
- Nhận sự kiện từ người dùng
- Observe LiveData từ ViewModel

**Ví dụ `HomeFragment.java`:**

```java
public class HomeFragment extends Fragment {

    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList = new ArrayList<>();
    private HomeViewModel homeViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. Ánh xạ view từ XML
        rvCategories = view.findViewById(R.id.rv_categories);

        // 2. Setup RecyclerView và Adapter
        categoryAdapter = new CategoryAdapter(getContext(), categoryList, category -> {
            // Callback khi user click vào 1 danh mục
            Toast.makeText(getContext(), "Danh mục: " + category.getName(), 
                           Toast.LENGTH_SHORT).show();
        });
        rvCategories.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // 3. Khởi tạo ViewModel
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // 4. Observe LiveData — khi dữ liệu thay đổi, UI tự động cập nhật
        homeViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryList.clear();
            categoryList.addAll(categories);
            categoryAdapter.notifyDataSetChanged();
        });

        return view;
    }
}
```

**💡 Giải thích:**

- `ViewModelProvider`: Cách lấy ViewModel, Android quản lý lifecycle
- `observe()`: Đăng ký lắng nghe, khi LiveData thay đổi → lambda được gọi
- `getViewLifecycleOwner()`: Tự động hủy observe khi Fragment bị destroy

---

### 6.5 Adapter - Hiển thị danh sách

**Vị trí:** `adapters/`

**Nhiệm vụ:** Kết nối dữ liệu với RecyclerView

**Cấu trúc cơ bản:**

```java
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> categoryList;
    private OnCategoryClickListener listener;

    // Interface để callback khi click
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    // Constructor
    public CategoryAdapter(Context context, List<Category> categoryList,
                           OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    // Tạo ViewHolder (inflate layout)
    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    // Gán dữ liệu vào ViewHolder
    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvName.setText(category.getName());

        // Load ảnh bằng Glide
        Glide.with(context)
                .load(category.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgCategory);

        // Xử lý click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCategoryClick(category);
        });
    }

    // Số lượng item
    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    // ViewHolder - giữ reference đến các view trong item
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCategory;
        TextView tvName;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            imgCategory = itemView.findViewById(R.id.img_category);
            tvName = itemView.findViewById(R.id.tv_category_name);
        }
    }
}
```

---

## 7. Ví dụ thực tế: Luồng hiển thị danh mục

Hãy theo dõi luồng dữ liệu từ database đến màn hình:

```
┌─────────────────────────────────────────────────────────────────────┐
│ BƯỚC 1: App khởi động, HomeFragment được load                       │
│                                                                     │
│   MainActivity.onCreate()                                           │
│       └── loadFragment(new HomeFragment())                          │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│ BƯỚC 2: HomeFragment tạo ViewModel                                  │
│                                                                     │
│   homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class)
│                                                                     │
│   → HomeViewModel constructor được gọi                              │
│   → Tạo CategoryRepository và gọi getAllCategories()                │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│ BƯỚC 3: Repository kết nối Firestore                                │
│                                                                     │
│   db.collection("Categories").addSnapshotListener(...)              │
│                                                                     │
│   → Gửi request đến Firebase                                        │
│   → Trả về LiveData (lúc này chưa có dữ liệu)                       │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│ BƯỚC 4: HomeFragment observe LiveData                               │
│                                                                     │
│   homeViewModel.getCategories().observe(getViewLifecycleOwner(), ...)
│                                                                     │
│   → Đăng ký lắng nghe, chờ dữ liệu về                               │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│ BƯỚC 5: Firestore trả về dữ liệu                                    │
│                                                                     │
│   SnapshotListener callback được gọi                                │
│       └── liveData.setValue(list)                                   │
│                                                                     │
│   → LiveData cập nhật giá trị mới                                   │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│ BƯỚC 6: Observer trong HomeFragment được trigger                    │
│                                                                     │
│   categories -> {                                                   │
│       categoryList.clear();                                         │
│       categoryList.addAll(categories);                              │
│       categoryAdapter.notifyDataSetChanged();                       │
│   }                                                                 │
│                                                                     │
│   → RecyclerView cập nhật, hiển thị danh sách danh mục              │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 8. Tips đọc code hiệu quả

### 💡 Tip 1: Bắt đầu từ UI, đi ngược lại

1. Mở app, nhìn vào màn hình cần tìm hiểu
2. Tìm layout XML tương ứng (trong `res/layout/`)
3. Tìm Activity/Fragment sử dụng layout đó
4. Từ đó tìm ViewModel và Repository

### 💡 Tip 2: Sử dụng Android Studio Search

- **Ctrl + Shift + F**: Tìm kiếm trong toàn bộ project
- **Ctrl + Click**: Nhảy đến định nghĩa của class/method
- **Ctrl + B**: Xem nơi sử dụng (usages)
- **Alt + F7**: Tìm tất cả references

### 💡 Tip 3: Đọc theo luồng dữ liệu

```
User Action → View → ViewModel → Repository → Firebase
                                     ↓
Firebase → Repository → ViewModel → View → UI Update
```

### 💡 Tip 4: Sử dụng Debugger

1. Đặt breakpoint ở dòng bạn muốn dừng
2. Chạy app ở chế độ Debug (Shift + F9)
3. Khi app dừng ở breakpoint, xem giá trị các biến
4. F8 để step qua từng dòng

### 💡 Tip 5: Đọc commit history

```bash
git log --oneline -20
```
Xem các commit gần đây để hiểu code được thêm như thế nào

### 💡 Tip 6: Chú ý các pattern lặp lại

Trong project này, hầu hết các tính năng đều theo pattern:

```
Model → Repository → ViewModel → Fragment → Adapter
```

Khi hiểu một feature (ví dụ: Categories), bạn sẽ dễ dàng hiểu các feature khác (Stores, Foods, Orders...)

---

## 📚 Tài liệu tham khảo thêm

- `ARCHITECTURE.md` - Kiến trúc chi tiết của dự án
- `.github/db.md` - Thiết kế cơ sở dữ liệu Firestore
- [Android Developers - ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [Firebase Firestore Docs](https://firebase.google.com/docs/firestore)

---

> 💬 **Lời khuyên cuối:** Đừng cố hiểu tất cả cùng lúc. Hãy bắt đầu từ một tính năng đơn giản (như hiển thị danh mục), hiểu rõ luồng dữ liệu, rồi mới chuyển sang tính năng phức tạp hơn. Chúc bạn học tập hiệu quả! 🚀
