Đây là thiết kế **cơ sở dữ liệu Firestore** đầy đủ cho FoodNow, dựa trên giao diện nhóm đã thiết kế.

***

## Sơ đồ tổng thể

```
Firestore Database
│
├── 👤 Users                ← thông tin người dùng
├── 🏪 Stores               ← danh sách quán ăn
├── 🍔 Foods                ← danh sách món ăn
├── 📂 Categories           ← danh mục (Pizza, Burger,...)
├── 📦 Orders               ← đơn hàng
└── ❤️ Favorites            ← món/quán yêu thích
```


***

## Chi tiết từng Collection

### 👤 `Users`

```
Users/
  {userId}/
    email        : "user@gmail.com"
    name         : "Nguyễn Văn A"
    phone        : "0901234567"
    address      : "123 Trần Phú, Đà Lạt"
    imageUrl     : "https://..."
    role         : "customer"     ← "customer" (mặc định) hoặc "admin"
    createdAt    : Timestamp
```


***

### 🏪 `Stores`

```
Stores/
  {storeId}/
    name         : "Phở Hà Nội"
    description  : "Quán phở ngon nhất Đà Lạt"
    address      : "45 Nguyễn Chí Thanh"
    phone        : "0911111111"
    imageUrl     : "https://..."
    rating       : 4.8
    deliveryTime : "15 phút"
    deliveryFee  : 15000
    isOpen       : true
```


***

### 📂 `Categories`

```
Categories/
  {categoryId}/
    name         : "Pizza"
    imageUrl     : "https://..."
```


***

### 🍔 `Foods`

```
Foods/
  {foodId}/
    title        : "Phở bò tái"
    description  : "Phở bò truyền thống"
    price        : 55000
    imageUrl     : "https://..."
    rating       : 4.7
    storeId      : "abc123"      ← liên kết với Stores
    categoryId   : "xyz456"      ← liên kết với Categories
    isAvailable  : true
```


***

### 📦 `Orders`

```
Orders/
  {orderId}/
    userId        : "user001"
    storeId       : "store001"
    storeName     : "Phở Hà Nội"
    address       : "123 Trần Phú, Đà Lạt"
    paymentMethod : "Tiền mặt"
    note          : "Ít cay"
    subtotal      : 110000
    deliveryFee   : 15000
    total         : 125000
    status        : "Đang xử lý"
    createdAt     : Timestamp

    items : [                    ← mảng các món đã đặt
      {
        foodId    : "food001"
        title     : "Phở bò tái"
        price     : 55000
        quantity  : 2
        imageUrl  : "https://..."
      }
    ]
```


***

### ❤️ `Favorites`

```
Favorites/
  {favoriteId}/
    userId       : "user001"
    type         : "store"       ← "store" hoặc "food"
    itemId       : "store001"    ← id của quán hoặc món
    name         : "Phở Hà Nội"
    imageUrl     : "https://..."
```


***

## Sơ đồ quan hệ

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


***

## Trạng thái đơn hàng (field `status`)

```
"Đang xử lý"  →  "Đang giao"  →  "Hoàn thành"
                                   ↑
                  "Đã hủy" ────────┘ (chỉ từ Đang xử lý)
```


***

## Mối liên kết giữa Model Java và Collection

| Java Model | Firestore Collection |
| :-- | :-- |
| `User.java` | `Users` |
| `Store.java` | `Stores` |
| `Food.java` | `Foods` |
| `Order.java` | `Orders` |
| `CartItem.java` | Lưu tạm trong app (không lên DB) |
| `Favorite.java` | `Favorites` |
