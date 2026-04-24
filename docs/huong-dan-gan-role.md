# Hướng dẫn gắn Role cho tài khoản FoodNow

> Dành cho thành viên nhóm cần tạo tài khoản test hoặc thiết lập môi trường demo.

---

## Tổng quan các Role

| Role | Ý nghĩa | Màn hình sau khi đăng nhập |
|---|---|---|
| `customer` | Khách hàng | `MainActivity` (trang chủ, đặt đồ ăn) |
| `store_owner` | Chủ cửa hàng | `StoreOwnerActivity` (quản lý quán) |
| `admin` | Quản trị viên | `MainActivity` (hiện tại dùng chung với customer) |

**Quy tắc:** Field `role` được lưu trong Firestore tại `Users/{uid}.role`. Firebase Auth **không** biết về role — chỉ quản lý email/mật khẩu.

---

## Cách 1 — Tài khoản Customer (mặc định)

Không cần làm gì thêm. Khi đăng ký qua màn hình Register của app, tài khoản tự động được tạo với `role = "customer"`.

```
Mở app → Đăng ký → Điền tên / SĐT / email / mật khẩu → Nhấn Đăng ký
```

Firestore tự tạo document `Users/{uid}` với:
```
role     : "customer"
address  : ""          ← chưa thiết lập địa chỉ
storeId  : (không có field này)
```

---

## Cách 2 — Tài khoản Store Owner

Cần **3 bước** thực hiện trên Firebase Console.

### Bước 1 — Đăng ký tài khoản thông thường

1. Mở app → nhấn **Đăng ký**
2. Điền thông tin → nhấn **Đăng ký**
3. App đưa vào `MainActivity` (role vẫn là `customer`)
4. **Ghi lại email** vừa đăng ký để dùng ở bước sau

### Bước 2 — Tạo document Cửa hàng trong Firestore

1. Mở [Firebase Console](https://console.firebase.google.com) → chọn project **FoodNow**
2. Chọn **Firestore Database** (menu bên trái)
3. Tìm collection **`Stores`** → nhấn **+ Add document**
4. Để **Document ID** tự sinh (Auto-ID) hoặc đặt tên dễ nhớ, ví dụ: `store_test_01`
5. Điền các field sau:

| Field | Type | Giá trị ví dụ |
|---|---|---|
| `name` | string | `Quán Phở Thử Nghiệm` |
| `description` | string | `Quán phở ngon` |
| `address` | string | `123 Nguyễn Huệ, Q1, TP.HCM` |
| `phone` | string | `0909123456` |
| `imageUrl` | string | *(để trống hoặc URL ảnh)* |
| `rating` | number | `0` |
| `deliveryTime` | string | `20 phút` |
| `deliveryFee` | number | `15000` |
| `isOpen` | boolean | `true` |
| `categoryId` | string | *(ID của 1 category, hoặc để trống)* |

6. Nhấn **Save** → ghi lại **Document ID** vừa tạo (ví dụ: `store_test_01`)

### Bước 3 — Gắn role store_owner cho tài khoản

1. Vẫn trong Firestore → mở collection **`Users`**
2. Tìm document có **email** trùng với tài khoản vừa đăng ký ở Bước 1

   > **Mẹo tìm nhanh:** Vào **Authentication** → tab **Users** → tìm email → copy UID → quay lại Firestore → `Users/{UID}`

3. Nhấn vào document → nhấn **Edit document** (biểu tượng bút chì)
4. Sửa field `role`:
   - Click vào giá trị `"customer"` → đổi thành `"store_owner"` → nhấn tick ✓
5. Thêm field `storeId`:
   - Nhấn **+ Add field**
   - Field name: `storeId`
   - Type: **string**
   - Value: Document ID của Stores vừa tạo ở Bước 2 (ví dụ: `store_test_01`)
   - Nhấn **Add**
6. Nhấn **Update**

### Kết quả

Đăng xuất khỏi app → đăng nhập lại bằng tài khoản đó → app tự động chuyển vào **StoreOwnerActivity** với đầy đủ thông tin quán.

---

## Kiểm tra nhanh sau khi thiết lập

| Kiểm tra | Kết quả mong đợi |
|---|---|
| Đăng nhập tài khoản store_owner | Vào màn hình quản lý quán (không vào trang chủ khách hàng) |
| Tab "Quán của tôi" | Hiển thị tên quán, địa chỉ, số điện thoại đã nhập |
| Công tắc Mở/Đóng cửa | Bật/tắt được, cập nhật ngay trên Firestore |
| Tab "Thực đơn" | Danh sách trống (chưa có món) — nhấn "+" để thêm |
| Banner "Chuyển sang Khách hàng" | Nhấn → vào `MainActivity` bình thường |

---

## Lỗi thường gặp

### ❌ App hiện dialog "Tài khoản chưa được liên kết với cửa hàng"

**Nguyên nhân:** Field `storeId` trong Firestore `Users/{uid}` bị thiếu hoặc rỗng.

**Cách sửa:** Thực hiện lại Bước 3 — kiểm tra field `storeId` đúng với Document ID trong collection `Stores`.

---

### ❌ Tab "Quán của tôi" hiển thị tên/địa chỉ mặc định, không cập nhật

**Nguyên nhân:** `storeId` trong Users trỏ tới một document **không tồn tại** trong collection `Stores`.

**Cách sửa:** Mở Firestore → `Stores` → xác nhận Document ID khớp với `storeId` trong `Users/{uid}`.

---

### ❌ Đăng nhập vào MainActivity thay vì StoreOwnerActivity

**Nguyên nhân:** Field `role` chưa được đổi sang `"store_owner"`, hoặc bị nhập sai chính tả.

**Cách sửa:** Kiểm tra `Users/{uid}.role` — phải là chuỗi `store_owner` (chữ thường, có dấu gạch dưới).

---

### ❌ Đăng nhập store_owner rồi nhấn "Chuyển sang Khách hàng" — app vào được nhưng không thấy dữ liệu

**Giải thích:** Tính năng chuyển mode đưa vào `MainActivity` (màn hình khách hàng). Dữ liệu home (cửa hàng gần đây, gợi ý món) cần có dữ liệu mẫu trong Firestore `Stores` và `Foods`.

---

## Thêm dữ liệu mẫu nhanh

Để trang chủ khách hàng có nội dung khi test, cần có ít nhất:

### Tạo Category
Collection `Categories` → Add document:
```
name     : "Phở"
imageUrl : ""
```

### Tạo Food
Collection `Foods` → Add document:
```
title       : "Phở bò tái"
description : "Phở bò truyền thống"
price       : 55000
imageUrl    : ""
rating      : 4.5
storeId     : <ID của Stores vừa tạo>
categoryId  : <ID của Categories vừa tạo>
isAvailable : true
```

---

## Sơ đồ tóm tắt

```
Firebase Auth          Firestore: Users/{uid}        Firestore: Stores/{storeId}
──────────────         ──────────────────────         ───────────────────────────
email                  role: "store_owner"   ──────→  name, address, phone, ...
password               storeId: "store_xyz"           isOpen, rating, ...
uid (auto)             name, phone, address
                       imageUrl, createdAt
```

**Quy tắc vàng:** `Users/{uid}.storeId` PHẢI trùng với một Document ID hợp lệ trong collection `Stores`. Nếu không, app hiện dialog lỗi và đăng xuất.
