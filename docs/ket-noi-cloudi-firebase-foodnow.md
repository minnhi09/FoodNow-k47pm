# Kết nối Cloudinary + Firebase + Android (FoodNow)

> **Mục tiêu:** Sau tài liệu này, bạn hiểu và tự làm được luồng:
> **chọn ảnh từ điện thoại → upload lên Cloudinary → lấy URL → lưu URL vào Firestore → hiển thị lại trên app**.

***

## 1. Tổng quan để hiểu nhanh

Trong dự án này:

1. **Cloudinary** chỉ dùng để lưu file ảnh.
2. **Firebase Firestore** chỉ lưu dữ liệu text/number, trong đó có trường `imageUrl`.
3. **Android app** là nơi người dùng chọn ảnh và gọi 2 bước trên.

Luồng dữ liệu:

1. Người dùng chọn ảnh trong app.
2. App upload ảnh lên Cloudinary (unsigned preset).
3. Cloudinary trả về `secure_url`.
4. App ghi `secure_url` vào Firestore (`Users`, `Stores`, `Foods`).
5. Glide load lại ảnh từ URL đó để hiển thị.

***

## 2. Chuẩn bị tài khoản và dự án

### 2.1 Firebase

1. Tạo Firebase project.
2. Tạo Android app với package `com.example.foodnow`.
3. Tải `google-services.json` đặt vào thư mục `app/`.
4. Bật **Authentication → Email/Password**.
5. Tạo Firestore Database.
6. Tạo collection cơ bản: `Users`, `Stores`, `Foods`.

### 2.2 Cloudinary

1. Tạo tài khoản Cloudinary.
2. Lấy `Cloud Name` trong Dashboard.
3. Vào **Settings → Upload** tạo Upload Preset.
4. Preset phải bật **Unsigned**.
5. Ví dụ preset: `foodnow_unsigned`.

> Nếu preset không phải unsigned, upload từ app sẽ thất bại.

***

## 3. Cấu hình Android project

### 3.1 Dependency

Trong `app/build.gradle.kts` cần có:

```kotlin
implementation("com.cloudinary:cloudinary-android:3.0.2")
implementation("com.google.firebase:firebase-firestore")
implementation("com.github.bumptech.glide:glide:4.16.0")
```


### 3.2 BuildConfig cho Cloudinary

Trong `app/build.gradle.kts`, dự án này đang dùng:

```kotlin
buildFeatures {
    buildConfig = true
}
```

và:

```kotlin
buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"$cloudinaryCloudName\"")
buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"$cloudinaryUploadPreset\"")
```


### 3.3 local.properties

Trong `local.properties` (file local, không commit), thêm:

```properties
cloudinary.cloud_name=dwtvqd3nu
cloudinary.upload_preset=foodnow_unsigned
```

Nếu không thêm, project hiện tại vẫn có fallback mặc định trong Gradle.

***

## 4. CloudinaryHelper (lớp dùng chung)

File: `app/src/main/java/com/example/foodnow/utils/CloudinaryHelper.java`

Nhiệm vụ:

1. Khởi tạo Cloudinary 1 lần (`init`).
2. Upload ảnh từ `Uri`.
3. Trả callback `onStart`, `onSuccess`, `onError`.
4. Định nghĩa folder:
    - `foodnow/profiles`
    - `foodnow/stores`
    - `foodnow/foods`

Phần quan trọng nhất với unsigned preset:

```java
MediaManager.get()
    .upload(imageUri)
    .unsigned(UPLOAD_PRESET)
    .option("folder", targetFolder)
```

> Nếu dùng sai kiểu request, upload có thể thất bại dù đã nhập đúng preset.

***

## 5. Firebase — phần lưu URL ảnh

### 5.1 Avatar user

`ProfileFragment` upload thành công → lấy `secure_url` → gọi:

```java
updates.put("imageUrl", secureUrl);
profileViewModel.updateUser(updates);
```


### 5.2 Tránh lỗi `NOT_FOUND`

Nếu document `Users/{uid}` chưa tồn tại, `update(...)` sẽ lỗi.
Dự án đã sửa trong `UserRepository`:

```java
set(updates, SetOptions.merge())
```

Như vậy:

1. Chưa có doc → tự tạo doc mới.
2. Đã có doc → merge field, không mất dữ liệu cũ.

### 5.3 Store/Food

Màn hình `ImageAdminActivity`:

1. Load danh sách từ `Stores` hoặc `Foods`.
2. Chọn item.
3. Upload lên Cloudinary.
4. Update `imageUrl` cho document đã chọn.

***

## 6. Firestore Rules để test đúng

Vào Firebase Console → Firestore → Rules.

Mẫu rule để test theo app hiện tại:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /Users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    match /Stores/{storeId} {
      allow read: if true;
      allow write: if request.auth != null; // test nhanh
    }

    match /Foods/{foodId} {
      allow read: if true;
      allow write: if request.auth != null; // test nhanh
    }
  }
}
```

> Nếu không dùng rule phù hợp, bạn sẽ gặp `PERMISSION_DENIED`.

***

## 7. Quy trình test A-Z trên máy thật

### Test avatar

1. Đăng nhập app.
2. Vào tab **Tài khoản**.
3. Bấm **Đổi ảnh đại diện**.
4. Chọn ảnh trong gallery.
5. Kiểm tra:
    - Toast thành công.
    - Avatar đổi ngay trên UI.
    - Firestore `Users/{uid}.imageUrl` có URL mới.
    - Cloudinary Media Library có ảnh trong `foodnow/profiles`.

### Test Store/Food

1. Vào **Tài khoản** → **Quản trị ảnh Store/Food**.
2. Chọn loại `Store` hoặc `Food`.
3. Chọn item cần cập nhật.
4. Chọn ảnh.
5. Bấm upload.
6. Kiểm tra Firestore (`Stores`/`Foods`) và Cloudinary folder tương ứng.

***

## 8. Các lỗi hay gặp và cách xử lý

### Lỗi 1: `defaultConfig contains custom BuildConfig fields, but the feature is disabled`

Nguyên nhân: dùng `buildConfigField(...)` nhưng tắt buildConfig generation.

Cách sửa:

```kotlin
buildFeatures {
    buildConfig = true
}
```


### Lỗi 2: `PERMISSION_DENIED`

Nguyên nhân: Firestore Rules chưa cho phép user hiện tại ghi vào document.

Cách sửa: cập nhật Rules đúng với luồng app.

### Lỗi 3: `NOT_FOUND: No document to update`

Nguyên nhân: document chưa tồn tại mà dùng `.update(...)`.

Cách sửa: dùng `.set(data, SetOptions.merge())`.

### Lỗi 4: Upload fail từ Cloudinary

Kiểm tra:

1. Preset có bật **Unsigned** chưa.
2. Tên preset có đúng exact text chưa.
3. Cloud name đúng chưa.
4. App đang gọi `.unsigned(UPLOAD_PRESET)` chưa.

***

## 9. Best practice bảo mật (quan trọng)

1. Không hardcode secret trong app.
2. Android app chỉ nên dùng unsigned upload hoặc upload qua backend của bạn.
3. Restrict unsigned preset (folder, format, kích thước, rate limit) trong Cloudinary.
4. Với production nghiêm túc, ưu tiên luồng **signed upload qua server**.
5. Firestore Rules phải theo role (user/admin), không để write mở rộng lâu dài.

***

## 10. Checklist nhanh để xác nhận "đã kết nối thành công"

1. App upload ảnh không báo lỗi.
2. Cloudinary có file mới.
3. Firestore có `imageUrl` mới.
4. App load được ảnh từ URL đó sau khi mở lại màn hình.

Nếu cả 4 điều trên đúng, bạn đã kết nối Cloudinary + Firebase + Android thành công.
