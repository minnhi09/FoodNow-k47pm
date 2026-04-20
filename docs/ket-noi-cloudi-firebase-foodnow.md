# Huong dan A-Z cho nguoi moi: Ket noi Cloudinary + Firebase + Android (FoodNow)

> Muc tieu: sau tai lieu nay, ban hieu va tu lam duoc luong:
> **chon anh tu dien thoai -> upload len Cloudinary -> lay URL -> luu URL vao Firestore -> hien thi lai tren app**.

---

## 1. Tong quan de hieu nhanh

Trong du an nay:

1. **Cloudinary** chi dung de luu file anh.
2. **Firebase Firestore** chi luu du lieu text/number, trong do co truong `imageUrl`.
3. **Android app** la noi nguoi dung chon anh va goi 2 buoc tren.

Luong du lieu:

1. Nguoi dung chon anh trong app.
2. App upload anh len Cloudinary (unsigned preset).
3. Cloudinary tra ve `secure_url`.
4. App ghi `secure_url` vao Firestore (`Users`, `Stores`, `Foods`).
5. Glide load lai anh tu URL do de hien thi.

---

## 2. Chuan bi tai khoan va du an

### 2.1 Firebase

1. Tao Firebase project.
2. Tao Android app voi package `com.example.foodnow`.
3. Tai `google-services.json` dat vao thu muc `app/`.
4. Bat **Authentication -> Email/Password**.
5. Tao Firestore Database.
6. Tao collection co ban: `Users`, `Stores`, `Foods`.

### 2.2 Cloudinary

1. Tao tai khoan Cloudinary.
2. Lay `Cloud Name` trong Dashboard.
3. Vao **Settings -> Upload** tao Upload Preset.
4. Preset phai bat **Unsigned**.
5. Vi du preset: `foodnow_unsigned`.

> Neu preset khong unsigned, upload tu app se that bai.

---

## 3. Cau hinh Android project

### 3.1 Dependency

Trong `app/build.gradle.kts` can co:

```kotlin
implementation("com.cloudinary:cloudinary-android:3.0.2")
implementation("com.google.firebase:firebase-firestore")
implementation("com.github.bumptech.glide:glide:4.16.0")
```

### 3.2 BuildConfig cho Cloudinary

Trong `app/build.gradle.kts`, du an nay dang dung:

```kotlin
buildFeatures {
    buildConfig = true
}
```

va:

```kotlin
buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"$cloudinaryCloudName\"")
buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"$cloudinaryUploadPreset\"")
```

### 3.3 local.properties

Trong `local.properties` (file local, khong commit), them:

```properties
cloudinary.cloud_name=dwtvqd3nu
cloudinary.upload_preset=foodnow_unsigned
```

Neu khong them, project hien tai van co fallback mac dinh trong Gradle.

---

## 4. CloudinaryHelper (lop dung chung)

File: `app/src/main/java/com/example/foodnow/utils/CloudinaryHelper.java`

Nhiem vu:

1. Khoi tao Cloudinary 1 lan (`init`).
2. Upload anh tu `Uri`.
3. Tra callback `onStart`, `onSuccess`, `onError`.
4. Dinh nghia folder:
   - `foodnow/profiles`
   - `foodnow/stores`
   - `foodnow/foods`

Phan quan trong nhat voi unsigned preset:

```java
MediaManager.get()
    .upload(imageUri)
    .unsigned(UPLOAD_PRESET)
    .option("folder", targetFolder)
```

> Neu dung sai kieu request, upload co the that bai du da nhap dung preset.

---

## 5. Firebase phan luu URL anh

### 5.1 Avatar user

`ProfileFragment` upload thanh cong -> lay `secure_url` -> goi:

```java
updates.put("imageUrl", secureUrl);
profileViewModel.updateUser(updates);
```

### 5.2 Tranh loi `NOT_FOUND`

Neu document `Users/{uid}` chua ton tai, `update(...)` se loi.
Du an da sua trong `UserRepository`:

```java
set(updates, SetOptions.merge())
```

Nhu vay:

1. Chua co doc -> tu tao doc moi.
2. Da co doc -> merge field, khong mat du lieu cu.

### 5.3 Store/Food

Man hinh `ImageAdminActivity`:

1. Load danh sach tu `Stores` hoac `Foods`.
2. Chon item.
3. Upload len Cloudinary.
4. Update `imageUrl` cho document da chon.

---

## 6. Firestore Rules de test dung

Vao Firebase Console -> Firestore -> Rules.

Mau rule de test theo app hien tai:

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

> Neu khong dung rule phu hop, ban se gap `PERMISSION_DENIED`.

---

## 7. Quy trinh test A-Z tren may that

### Test avatar

1. Dang nhap app.
2. Vao tab **Tai khoan**.
3. Bam **Doi anh dai dien**.
4. Chon anh trong gallery.
5. Kiem tra:
   - Toast thanh cong.
   - Avatar doi ngay tren UI.
   - Firestore `Users/{uid}.imageUrl` co URL moi.
   - Cloudinary Media Library co anh trong `foodnow/profiles`.

### Test Store/Food

1. Vao **Tai khoan** -> **Quan tri anh Store/Food**.
2. Chon loai `Store` hoac `Food`.
3. Chon item can cap nhat.
4. Chon anh.
5. Bam upload.
6. Kiem tra Firestore (`Stores`/`Foods`) va Cloudinary folder tuong ung.

---

## 8. Cac loi hay gap va cach xu ly

### Loi 1: `defaultConfig contains custom BuildConfig fields, but the feature is disabled`

Nguyen nhan: dung `buildConfigField(...)` nhung tat buildConfig generation.

Cach sua:

```kotlin
buildFeatures {
    buildConfig = true
}
```

### Loi 2: `PERMISSION_DENIED`

Nguyen nhan: Firestore Rules chua cho phep user hien tai ghi vao document.

Cach sua: cap nhat Rules dung voi luong app.

### Loi 3: `NOT_FOUND: No document to update`

Nguyen nhan: document chua ton tai ma dung `.update(...)`.

Cach sua: dung `.set(data, SetOptions.merge())`.

### Loi 4: Upload fail tu Cloudinary

Kiem tra:

1. Preset co bat **Unsigned** chua.
2. Ten preset co dung exact text chua.
3. Cloud name dung chua.
4. App dang goi `.unsigned(UPLOAD_PRESET)` chua.

---

## 9. Best practice bao mat (quan trong)

1. Khong hardcode secret trong app.
2. Android app chi nen dung unsigned upload hoac upload qua backend cua ban.
3. Restrict unsigned preset (folder, format, kich thuoc, rate limit) trong Cloudinary.
4. Voi production nghiem tuc, uu tien luong **signed upload qua server**.
5. Firestore Rules phai theo role (user/admin), khong de write mo rong lau dai.

---

## 10. Checklist nhanh de xac nhan "da ket noi thanh cong"

1. App upload anh khong bao loi.
2. Cloudinary co file moi.
3. Firestore co `imageUrl` moi.
4. App load duoc anh tu URL do sau khi mo lai man hinh.

Neu ca 4 dieu tren dung, ban da ket noi Cloudinary + Firebase + Android thanh cong.

