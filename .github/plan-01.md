# Kế hoạch phát triển FoodNow

## Tổng quan dự án
Ứng dụng đặt đồ ăn đơn giản cho môn Android Development (đại học). Backend sử dụng Firebase (Auth + Firestore), hình ảnh lưu trên Cloudinary, kiến trúc MVVM.

## Phân tích hiện trạng

### Đã có:
- `MainActivity` với `BottomNavigationView` (4 tab, tất cả đang trỏ về `HomeFragment`)
- `HomeFragment` hiển thị danh sách quán với dữ liệu giả (mock)
- `StoreAdapter` cho RecyclerView
- `Store.java` model (thiếu nhiều field so với Firestore schema)
- Dependencies: Firebase Auth, Firestore, Glide, RecyclerView, CardView, ViewPager2, CircleIndicator

### Chưa có:
- Chưa có kiến trúc MVVM (thiếu ViewModel, Repository)
- Chưa có màn hình đăng nhập/đăng ký
- Chưa có các model: User, Food, Category, Order, CartItem, Favorite
- Chưa kết nối thực tế với Firebase Firestore
- Chưa tích hợp Cloudinary
- Chưa có các fragment cho tab Đơn hàng, Yêu thích, Tài khoản

## Cấu trúc package MVVM đề xuất

```
com.example.foodnow/
├── models/          ← Data class khớp Firestore (Store, Food, User, Order, CartItem, Category, Favorite)
├── repositories/    ← Lớp truy cập Firebase (StoreRepository, FoodRepository, AuthRepository,...)
├── viewmodels/      ← ViewModel + LiveData cho từng màn hình
├── fragments/       ← Các Fragment UI
├── adapters/        ← RecyclerView Adapter
├── activities/      ← LoginActivity, RegisterActivity, StoreDetailActivity,...
└── utils/           ← Helper (CloudinaryHelper,...)
```

## Kế hoạch thực hiện

### Phase 1: Nền tảng MVVM + Models
- Thêm dependency `lifecycle-viewmodel` và `lifecycle-livedata` vào `build.gradle.kts`
- Tạo tất cả model class theo db.md:
    - Cập nhật `Store.java` (thêm description, address, phone, imageUrl, deliveryFee, isOpen + constructor rỗng cho Firestore)
    - Tạo `User.java`, `Food.java`, `Category.java`, `Order.java`, `CartItem.java`, `Favorite.java`
- Mỗi model cần constructor rỗng (yêu cầu của Firestore deserialization)

### Phase 2: Authentication (Đăng nhập / Đăng ký)
- Tạo `LoginActivity` + layout `activity_login.xml`
- Tạo `RegisterActivity` + layout `activity_register.xml`
- Tạo `AuthRepository` (đăng nhập/đăng ký bằng Firebase Auth email+password)
- Tạo `AuthViewModel` quản lý trạng thái đăng nhập
- Cập nhật `AndroidManifest.xml`: launcher activity → LoginActivity
- Nếu đã đăng nhập → chuyển thẳng đến MainActivity

### Phase 3: Home screen nâng cấp
- Tạo `StoreRepository` (đọc danh sách Store từ Firestore)
- Tạo `CategoryRepository` (đọc danh sách Category từ Firestore)
- Tạo `HomeViewModel` (LiveData<List<Store>>, LiveData<List<Category>>)
- Tạo `CategoryAdapter` + layout `item_category.xml` (RecyclerView ngang)
- Cập nhật `HomeFragment` → dùng ViewModel thay vì mock data
- Cập nhật `StoreAdapter` theo model Store mới (hiển thị ảnh bằng Glide)
- Cập nhật `fragment_home.xml` thêm RecyclerView ngang cho danh mục

### Phase 4: Chi tiết quán + Danh sách món
- Tạo `StoreDetailActivity` + layout `activity_store_detail.xml`
- Tạo `FoodRepository` (đọc Foods theo storeId từ Firestore)
- Tạo `StoreDetailViewModel`
- Tạo `FoodAdapter` + layout `item_food.xml`
- Khi bấm vào 1 quán ở Home → mở StoreDetailActivity hiển thị danh sách món

### Phase 5: Giỏ hàng (Cart - lưu local, chỉ 1 quán)
- Tạo `CartManager` (Singleton, quản lý List<CartItem> trong bộ nhớ)
- Giỏ hàng chỉ chứa món từ 1 quán tại 1 thời điểm; nếu thêm món từ quán khác → hỏi xác nhận xóa giỏ cũ
- Tạo `CartFragment` + layout `fragment_cart.xml`
- Tạo `CartAdapter` + layout `item_cart.xml`
- Tạo `CartViewModel`
- Thêm nút "Thêm vào giỏ" ở FoodAdapter / StoreDetailActivity
- Thêm icon giỏ hàng trên toolbar hoặc FAB

### Phase 6: Đặt hàng (Checkout + Order)
- Tạo `CheckoutActivity` + layout `activity_checkout.xml`
- Tạo `OrderRepository` (ghi Order lên Firestore)
- Tạo `CheckoutViewModel`
- Từ CartFragment → bấm "Đặt hàng" → CheckoutActivity → xác nhận → lưu Firestore

### Phase 7: Tab Đơn hàng
- Tạo `OrdersFragment` + layout `fragment_orders.xml`
- Tạo `OrderAdapter` + layout `item_order.xml`
- Tạo `OrdersViewModel` (đọc Orders theo userId)
- Cập nhật `MainActivity` → nav_orders trỏ đến `OrdersFragment`

### Phase 8: Tab Yêu thích
- Tạo `FavoritesFragment` + layout `fragment_favorites.xml`
- Tạo `FavoriteRepository` (CRUD Favorites trên Firestore)
- Tạo `FavoritesViewModel`
- Tạo `FavoriteAdapter` + layout `item_favorite.xml`
- Thêm nút yêu thích (trái tim) ở StoreAdapter / FoodAdapter
- Cập nhật `MainActivity` → nav_favorites trỏ đến `FavoritesFragment`

### Phase 9: Tab Tài khoản
- Tạo `ProfileFragment` + layout `fragment_profile.xml`
- Tạo `UserRepository` (đọc/cập nhật User trên Firestore)
- Tạo `ProfileViewModel`
- Hiển thị thông tin user, cho phép sửa, nút đăng xuất
- Cập nhật `MainActivity` → nav_profile trỏ đến `ProfileFragment`

### Phase 10: Tích hợp Cloudinary (load + upload)
- Thêm dependency Cloudinary SDK vào `build.gradle.kts`
- Tạo `CloudinaryHelper` để xây dựng URL ảnh và upload ảnh (ví dụ: avatar user)
- Sử dụng Glide load ảnh từ Cloudinary URL trong các Adapter
- Trong ProfileFragment: cho phép chọn ảnh từ gallery → upload lên Cloudinary → lưu URL vào Firestore

### Phase 11: Tìm kiếm quán/món
- Kết nối `SearchView` sẵn có trong `fragment_home.xml` với logic tìm kiếm
- Lọc danh sách Store/Food theo keyword trên Firestore hoặc local filter
- Hiển thị kết quả tìm kiếm trong cùng RecyclerView hoặc fragment riêng

## Lưu ý quan trọng
- Code đơn giản, dễ hiểu (phù hợp môn học đại học)
- Không dùng Dependency Injection phức tạp (Hilt/Dagger) — tạo Repository trực tiếp trong ViewModel
- Firestore model cần constructor rỗng + getter/setter đầy đủ
- CartItem chỉ lưu local (trong bộ nhớ app), không đẩy lên Firestore
- Comment tiếng Việt theo phong cách hiện tại của dự án
