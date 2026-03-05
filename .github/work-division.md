

## 👤 Thành viên 1 — Nền tảng \& Xác thực \& Hồ sơ

Đây là người **setup dự án đầu tiên** vì toàn nhóm phụ thuộc vào phần Auth hoàn thành trước.

**Công việc cụ thể:**


| Layer | File cần làm |
| :-- | :-- |
| **Setup** | Firebase config, Cloudinary config, `build.gradle.kts`, `AndroidManifest.xml` |
| **Models** | `User.java`, `Category.java` |
| **Repositories** | `AuthRepository.java`, `UserRepository.java`, `CategoryRepository.java` |
| **ViewModels** | `AuthViewModel.java`, `ProfileViewModel.java` |
| **Activities** | `LoginActivity.java`, `RegisterActivity.java` |
| **Fragment** | `ProfileFragment.java` |
| **Adapter** | `CategoryAdapter.java` |
| **Utils** | `CloudinaryHelper.java` (upload ảnh đại diện) |
| **Layouts** | `activity_login.xml`, `activity_register.xml`, `fragment_profile.xml`, `item_category.xml`, `activity_main.xml`, `bottom_nav_menu.xml` |
| **MainActivity** | Setup `BottomNavigationView` + 4 tab điều hướng |

> ⚙️ **Kỹ năng học được:** Firebase Auth, Firestore CRUD, LiveData cơ bản, Navigation giữa Activity.

***

## 👤 Thành viên 2 — Trang chủ \& Quán ăn \& Thực đơn

Phần này xây dựng **luồng xem sản phẩm** — từ danh sách quán đến chi tiết món ăn.

**Công việc cụ thể:**


| Layer | File cần làm |
| :-- | :-- |
| **Models** | `Store.java`, `Food.java` |
| **Repositories** | `StoreRepository.java`, `FoodRepository.java` |
| **ViewModels** | `HomeViewModel.java`, `StoreDetailViewModel.java` |
| **Activity** | `StoreDetailActivity.java` (xem quán + thực đơn, thêm vào giỏ) |
| **Fragment** | `HomeFragment.java` (search + banner danh mục + danh sách quán) |
| **Adapters** | `StoreAdapter.java`, `FoodAdapter.java` |
| **Layouts** | `fragment_home.xml`, `activity_store_detail.xml`, `item_store.xml`, `item_food.xml` |

> ⚙️ **Kỹ năng học được:** `RecyclerView`, Snapshot Listener real-time, truyền dữ liệu qua `Intent`, Glide load ảnh.

***

## 👤 Thành viên 3 — Giỏ hàng \& Đặt hàng \& Yêu thích

Phần này xử lý **toàn bộ luồng mua hàng** và các tính năng phụ trợ.

**Công việc cụ thể:**


| Layer | File cần làm |
| :-- | :-- |
| **Models** | `CartItem.java`, `Order.java` (+ `OrderItem` inner class), `Favorite.java` |
| **Repositories** | `OrderRepository.java`, `FavoriteRepository.java` |
| **ViewModels** | `CheckoutViewModel.java`, `OrdersViewModel.java`, `FavoritesViewModel.java` |
| **Activity** | `CheckoutActivity.java` (nhập địa chỉ → tạo đơn hàng) |
| **Fragments** | `CartFragment.java`, `OrdersFragment.java`, `FavoritesFragment.java` |
| **Adapters** | `CartAdapter.java` (nút +/−), `OrderAdapter.java`, `FavoriteAdapter.java` |
| **Utils** | `CartManager.java` (Singleton quản lý giỏ hàng in-memory) |
| **Layouts** | `fragment_cart.xml`, `activity_checkout.xml`, `fragment_orders.xml`, `fragment_favorites.xml`, `item_cart.xml`, `item_order.xml`, `item_favorite.xml` |

> ⚙️ **Kỹ năng học được:** Singleton Pattern, Firestore `.add()` ghi dữ liệu, `AlertDialog`, xử lý logic nghiệp vụ phức tạp.

***

## 🗓 Thứ tự thực hiện đề xuất

```
TV1 setup dự án + Auth (cả nhóm phụ thuộc phần này)
              ↓
TV2 + TV3 làm song song sau khi TV1 hoàn thành Auth
```

Quy tắc quan trọng: TV2 và TV3 cần **CartManager** (TV3 làm) để kết nối `StoreDetailActivity` với `CartFragment`, nên TV3 nên ưu tiên viết `CartManager.java` trước.



