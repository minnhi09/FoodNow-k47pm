# Scripts — Seed dữ liệu Firestore

## Yêu cầu
- Node.js ≥ 16
- File `serviceAccountKey.json` từ Firebase Console

## Lấy serviceAccountKey.json

1. Vào [Firebase Console](https://console.firebase.google.com/) → chọn project FoodNow
2. **Project Settings** (⚙️) → tab **Service accounts**
3. Nhấn **Generate new private key** → tải file JSON
4. Đổi tên thành `serviceAccountKey.json` và đặt vào thư mục `scripts/`

> ⚠️ **KHÔNG commit file này lên Git** — đã được thêm vào `.gitignore`

## Chạy seed

```bash
cd scripts
npm install        # cài firebase-admin (chỉ lần đầu)
node seed.js       # chạy seed
```

## Kết quả

Script sẽ tạo:
| Collection   | Số lượng | Ghi chú |
|-------------|----------|---------|
| Categories  | 6        | Phở & Bún, Cơm, Pizza & Burger, ... |
| Stores      | 5        | Quán ăn Việt, đầy đủ thông tin |
| Foods       | 18       | 3-4 món/quán, liên kết storeId + categoryId |

## Thay ảnh

Sau khi seed, `imageUrl` đều là `""`. Để thêm ảnh:
- Vào Firebase Console → Firestore → sửa trực tiếp từng document
- Hoặc upload ảnh lên Cloudinary rồi cập nhật URL

## Xoá dữ liệu cũ

Nếu muốn seed lại từ đầu, vào Firebase Console → Firestore → xoá 3 collections `Categories`, `Stores`, `Foods` rồi chạy lại `node seed.js`.
