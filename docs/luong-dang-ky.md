# 🔐 Giải thích chi tiết luồng Đăng ký tài khoản — FoodNow

> Tài liệu này giải thích **từng bước, từng dòng code** trong luồng đăng ký user mới — từ lúc user mở app đến khi dữ liệu được ghi lên Firebase.

---

## 📋 Mục lục

1. [Tổng quan luồng](#1-tổng-quan-luồng)
2. [Sơ đồ kiến trúc MVVM](#2-sơ-đồ-kiến-trúc-mvvm)
3. [Thứ tự đọc code](#3-thứ-tự-đọc-code)
4. [Bước 1: App khởi động — AndroidManifest.xml](#4-bước-1-app-khởi-động--androidmanifestxml)
5. [Bước 2: Giao diện đăng nhập — LoginActivity](#5-bước-2-giao-diện-đăng-nhập--loginactivity)
6. [Bước 3: Chuyển sang màn hình đăng ký](#6-bước-3-chuyển-sang-màn-hình-đăng-ký)
7. [Bước 4: Giao diện đăng ký — activity_register.xml](#7-bước-4-giao-diện-đăng-ký--activity_registerxml)
8. [Bước 5: Logic đăng ký — RegisterActivity.java](#8-bước-5-logic-đăng-ký--registeractivityjava)
9. [Bước 6: ViewModel trung gian — AuthViewModel.java](#9-bước-6-viewmodel-trung-gian--authviewmodeljava)
10. [Bước 7: Truy cập Firebase — AuthRepository.java](#10-bước-7-truy-cập-firebase--authrepositoryjava)
11. [Bước 8: Model dữ liệu — User.java](#11-bước-8-model-dữ-liệu--userjava)
12. [Bước 9: Dữ liệu trên Firestore](#12-bước-9-dữ-liệu-trên-firestore)
13. [Bước 10: Sau khi đăng ký thành công — vào MainActivity](#13-bước-10-sau-khi-đăng-ký-thành-công--vào-mainactivity)
14. [Sơ đồ luồng dữ liệu chi tiết](#14-sơ-đồ-luồng-dữ-liệu-chi-tiết)
15. [Giải thích các khái niệm quan trọng](#15-giải-thích-các-khái-niệm-quan-trọng)
16. [Bảng tóm tắt tất cả file liên quan](#16-bảng-tóm-tắt-tất-cả-file-liên-quan)

---

## 1. Tổng quan luồng

Khi user đăng ký tài khoản lần đầu, dữ liệu chảy qua **4 tầng** theo kiến trúc MVVM:

```
[UI: RegisterActivity]
       ↓ gọi
[ViewModel: AuthViewModel]
       ↓ gọi
[Repository: AuthRepository]
       ↓ gọi
[Firebase: Auth + Firestore]
```

**Hai thao tác xảy ra liên tiếp:**
1. **Firebase Authentication** — tạo tài khoản xác thực (email + password) → nhận được `uid`
2. **Cloud Firestore** — ghi thông tin user (name, phone, email...) vào collection `Users/{uid}`

---

## 2. Sơ đồ kiến trúc MVVM

```
┌─────────────────────────────────────────────────────────────┐
│                      VIEW LAYER (UI)                        │
│                                                             │
│  activity_register.xml ← giao diện người dùng nhìn thấy    │
│  RegisterActivity.java ← xử lý sự kiện, ánh xạ view       │
│                                                             │
│  - Thu thập dữ liệu từ EditText                            │
│  - Gọi ViewModel khi user bấm nút                          │
│  - Quan sát LiveData để cập nhật UI                         │
└──────────────────────┬──────────────────────────────────────┘
                       │ gọi authViewModel.register(...)
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   VIEWMODEL LAYER                           │
│                                                             │
│  AuthViewModel.java                                         │
│                                                             │
│  - Nhận yêu cầu từ View                                    │
│  - Chuyển tiếp xuống Repository                             │
│  - Quản lý trạng thái (loading, error, user) qua LiveData  │
│  - KHÔNG biết về Activity/Fragment cụ thể                   │
└──────────────────────┬──────────────────────────────────────┘
                       │ gọi authRepository.register(...)
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                  REPOSITORY LAYER                           │
│                                                             │
│  AuthRepository.java                                        │
│                                                             │
│  - Giao tiếp trực tiếp với Firebase SDK                     │
│  - Tạo user trên Firebase Auth                              │
│  - Ghi dữ liệu vào Firestore                               │
│  - Trả về Task<AuthResult> cho ViewModel                    │
└──────────────────────┬──────────────────────────────────────┘
                       │ gọi Firebase SDK
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                  FIREBASE (BACKEND)                         │
│                                                             │
│  Firebase Authentication — quản lý tài khoản đăng nhập      │
│  Cloud Firestore — database NoSQL lưu thông tin user        │
│                                                             │
│  Collection: Users/{uid}                                    │
│    ├── email      : "abc@gmail.com"                         │
│    ├── name       : "Nguyễn Văn A"                          │
│    ├── phone      : "0901234567"                            │
│    ├── address    : ""                                      │
│    ├── imageUrl   : ""                                      │
│    └── createdAt  : Timestamp                               │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Thứ tự đọc code

Đọc theo thứ tự này để hiểu luồng **từ ngoài vào trong** (UI → Backend):

| Bước | File | Lý do |
|------|------|-------|
| 1 | `AndroidManifest.xml` | Biết app mở màn hình nào đầu tiên |
| 2 | `activity_login.xml` | Giao diện đăng nhập — nơi user bấm "Đăng ký" |
| 3 | `LoginActivity.java` | Code chuyển sang RegisterActivity |
| 4 | `activity_register.xml` | Giao diện đăng ký — user nhập thông tin |
| 5 | `RegisterActivity.java` | Xử lý nút bấm, validate, gọi ViewModel |
| 6 | `AuthViewModel.java` | Trung gian — quản lý loading/error/user |
| 7 | `AuthRepository.java` | ⭐ **Trọng tâm** — nơi data thật sự ghi lên Firebase |
| 8 | `User.java` | Model dữ liệu — cấu trúc user trong Java |
| 9 | `.github/db.md` | Schema Firestore — cấu trúc trên database |

---

## 4. Bước 1: App khởi động — AndroidManifest.xml

📁 **File:** `app/src/main/AndroidManifest.xml`

```xml
<activity
    android:name=".activities.LoginActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

### Giải thích:

- **`android.intent.action.MAIN`** + **`android.intent.category.LAUNCHER`**: Đánh dấu `LoginActivity` là **màn hình đầu tiên** khi user mở app.
- Khi app khởi động → Android tạo `LoginActivity` → gọi `onCreate()`.
- Đây là **điểm bắt đầu** (entry point) của toàn bộ luồng.

### Tại sao LoginActivity là launcher?
Vì app cần kiểm tra xem user đã đăng nhập hay chưa trước khi cho vào trang chính.

---

## 5. Bước 2: Giao diện đăng nhập — LoginActivity

📁 **File:** `app/src/main/java/com/example/foodnow/activities/LoginActivity.java`

### 5.1. Khởi tạo ViewModel (dòng 32)

```java
authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
```

- **`ViewModelProvider(this)`**: Tạo/lấy ViewModel gắn với lifecycle của Activity này.
- **`.get(AuthViewModel.class)`**: Lấy instance của `AuthViewModel`. Nếu chưa có thì tạo mới, nếu đã có (ví dụ xoay màn hình) thì dùng lại.
- ViewModel sống lâu hơn Activity — khi xoay màn hình, Activity bị destroy nhưng ViewModel vẫn còn.

### 5.2. Kiểm tra đã đăng nhập (dòng 42-45)

```java
if (authViewModel.getUserLiveData().getValue() != null) {
    goToMain();
    return;
}
```

- Nếu user **đã đăng nhập** từ lần trước (Firebase Auth lưu session tự động) → chuyển thẳng vào `MainActivity`.
- **`getValue()`**: Lấy giá trị hiện tại của LiveData (không cần chờ observe).
- Nếu `null` → user chưa đăng nhập → ở lại màn login.

### 5.3. Nút chuyển sang đăng ký (dòng 77-79)

```java
tvGoRegister.setOnClickListener(v -> {
    startActivity(new Intent(this, RegisterActivity.class));
});
```

- Khi user bấm text "Chưa có tài khoản? Đăng ký" → mở `RegisterActivity`.
- **`startActivity(Intent)`**: Cơ chế Android để chuyển từ Activity này sang Activity khác.
- `LoginActivity` không bị đóng (vẫn nằm trong back stack) → nếu user bấm back sẽ quay lại.

---

## 6. Bước 3: Chuyển sang màn hình đăng ký

```
LoginActivity ──(bấm "Đăng ký")──→ RegisterActivity
```

Android tạo `RegisterActivity` mới, gọi `onCreate()`, và hiển thị giao diện đăng ký.

---

## 7. Bước 4: Giao diện đăng ký — activity_register.xml

📁 **File:** `app/src/main/res/layout/activity_register.xml`

### Cấu trúc layout:

```
LinearLayout (dọc, căn giữa, padding 32dp)
  ├── TextView "Tạo tài khoản"              ← Tiêu đề
  ├── TextInputLayout > TextInputEditText    ← Họ tên    (id: et_name)
  ├── TextInputLayout > TextInputEditText    ← SĐT       (id: et_phone)
  ├── TextInputLayout > TextInputEditText    ← Email      (id: et_email)
  ├── TextInputLayout > TextInputEditText    ← Mật khẩu  (id: et_password)
  ├── MaterialButton "Đăng ký"              ← Nút đăng ký (id: btn_register)
  ├── ProgressBar                            ← Loading    (id: progress_bar)
  └── TextView "Đã có tài khoản? Đăng nhập" ← Quay lại   (id: tv_go_login)
```

### Giải thích từng thành phần:

| ID | Loại | Mục đích |
|----|-------|----------|
| `et_name` | `TextInputEditText` | User nhập họ tên, `inputType="textPersonName"` — bàn phím hiện chữ viết hoa |
| `et_phone` | `TextInputEditText` | Nhập SĐT, `inputType="phone"` — bàn phím hiện số |
| `et_email` | `TextInputEditText` | Nhập email, `inputType="textEmailAddress"` — bàn phím có `@` |
| `et_password` | `TextInputEditText` | Nhập mật khẩu, `inputType="textPassword"` — hiển thị dấu ••• |
| `btn_register` | `MaterialButton` | Bấm để gửi yêu cầu đăng ký |
| `progress_bar` | `ProgressBar` | Hiện vòng tròn loading khi đang gửi request, mặc định ẩn (`visibility="gone"`) |
| `tv_go_login` | `TextView` | Bấm để quay lại `LoginActivity` |

### TextInputLayout là gì?

```xml
<com.google.android.material.textfield.TextInputLayout>
    <com.google.android.material.textfield.TextInputEditText ... />
</com.google.android.material.textfield.TextInputLayout>
```

- `TextInputLayout` là **container** bọc quanh `TextInputEditText`.
- Nó cung cấp: label nổi (floating hint), animation khi focus, hiển thị lỗi.
- Thuộc thư viện Material Design của Google.

---

## 8. Bước 5: Logic đăng ký — RegisterActivity.java

📁 **File:** `app/src/main/java/com/example/foodnow/activities/RegisterActivity.java`

Đây là file quan trọng nhất ở tầng UI. Hãy đọc kỹ từng phần:

### 8.1. Khai báo biến (dòng 21-24)

```java
private AuthViewModel authViewModel;
private TextInputEditText etName, etPhone, etEmail, etPassword;
private MaterialButton btnRegister;
private ProgressBar progressBar;
```

- **`authViewModel`**: Đối tượng ViewModel — cầu nối giữa UI và logic nghiệp vụ.
- **`etName, etPhone, etEmail, etPassword`**: Các ô nhập liệu tương ứng.
- **`btnRegister`**: Nút đăng ký.
- **`progressBar`**: Thanh loading.

### 8.2. onCreate() — Khởi tạo (dòng 26-79)

#### a) Set layout (dòng 29)
```java
setContentView(R.layout.activity_register);
```
- Nạp file `activity_register.xml` làm giao diện.
- `R.layout.activity_register` là ID tự động tạo bởi Android build system.

#### b) Khởi tạo ViewModel (dòng 32)
```java
authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
```
- Giống như trong `LoginActivity` — tạo hoặc lấy lại `AuthViewModel`.

#### c) Ánh xạ view (dòng 35-41)
```java
etName      = findViewById(R.id.et_name);
etPhone     = findViewById(R.id.et_phone);
etEmail     = findViewById(R.id.et_email);
etPassword  = findViewById(R.id.et_password);
btnRegister = findViewById(R.id.btn_register);
progressBar = findViewById(R.id.progress_bar);
TextView tvGoLogin = findViewById(R.id.tv_go_login);
```
- **`findViewById(R.id.xxx)`**: Tìm view trong layout XML theo ID.
- Sau bước này, biến Java trỏ đến đúng widget trên màn hình.

### 8.3. Observe LiveData — Quan sát trạng thái (dòng 44-61)

Đây là phần **cốt lõi của MVVM** — UI không tự xử lý logic mà chỉ **quan sát** (observe) dữ liệu từ ViewModel.

#### a) Quan sát user (dòng 44-50)
```java
authViewModel.getUserLiveData().observe(this, user -> {
    if (user != null) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
});
```

**Giải thích từng dòng:**
- **`getUserLiveData()`**: Trả về `LiveData<FirebaseUser>` — đối tượng có thể được quan sát.
- **`.observe(this, user -> { ... })`**: Đăng ký lắng nghe. Mỗi khi giá trị `userLiveData` thay đổi → callback này được gọi.
  - `this` (tham số 1): LifecycleOwner — khi Activity bị destroy, tự động hủy lắng nghe (tránh memory leak).
  - `user -> { ... }` (tham số 2): Lambda chạy khi dữ liệu thay đổi.
- **`if (user != null)`**: Khi đăng ký thành công → ViewModel set `userLiveData` = FirebaseUser → observer được trigger.
- **`FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`**: Xóa toàn bộ back stack (LoginActivity, RegisterActivity) → user không thể bấm back quay lại login.

**Luồng:** Đăng ký thành công → ViewModel set user → Observer chạy → Chuyển sang MainActivity

#### b) Quan sát lỗi (dòng 52-56)
```java
authViewModel.getErrorLiveData().observe(this, error -> {
    if (error != null) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }
});
```

- Khi đăng ký thất bại (email đã tồn tại, mật khẩu yếu...) → ViewModel set `errorLiveData` = message lỗi.
- Observer nhận message → hiện Toast cho user.

#### c) Quan sát loading (dòng 58-61)
```java
authViewModel.getLoadingLiveData().observe(this, isLoading -> {
    progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    btnRegister.setEnabled(!isLoading);
});
```

- **`isLoading = true`**: Đang gửi request → hiện ProgressBar + vô hiệu hóa nút (tránh bấm nhiều lần).
- **`isLoading = false`**: Request xong → ẩn ProgressBar + bật lại nút.

### 8.4. Xử lý bấm nút đăng ký (dòng 64-75) ⭐

```java
btnRegister.setOnClickListener(v -> {
    String name     = etName.getText().toString().trim();
    String phone    = etPhone.getText().toString().trim();
    String email    = etEmail.getText().toString().trim();
    String password = etPassword.getText().toString().trim();

    if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
        Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
        return;
    }
    authViewModel.register(email, password, name, phone);
});
```

**Giải thích từng dòng:**

1. **`setOnClickListener(v -> { ... })`**: Đăng ký callback cho sự kiện bấm nút.

2. **`etName.getText().toString().trim()`**:
   - `getText()` → lấy text hiện tại trong EditText (kiểu `Editable`)
   - `.toString()` → chuyển thành `String`
   - `.trim()` → bỏ khoảng trắng đầu cuối

3. **Validate (kiểm tra dữ liệu)**:
   - Chỉ kiểm tra: name, email, password không rỗng.
   - Phone không bắt buộc (không check `phone.isEmpty()`).
   - Nếu thiếu → hiện Toast cảnh báo + `return` (thoát, không gọi tiếp).

4. **`authViewModel.register(email, password, name, phone)`**:
   - ⭐ **ĐÂY LÀ DÒNG QUAN TRỌNG NHẤT** — chuyển dữ liệu từ UI xuống ViewModel.
   - Sau dòng này, UI không làm gì thêm — chỉ chờ LiveData thay đổi.

### 8.5. Quay lại đăng nhập (dòng 78)
```java
tvGoLogin.setOnClickListener(v -> finish());
```
- **`finish()`**: Đóng Activity hiện tại → quay lại Activity trước đó (LoginActivity).

---

## 9. Bước 6: ViewModel trung gian — AuthViewModel.java

📁 **File:** `app/src/main/java/com/example/foodnow/viewmodels/AuthViewModel.java`

### 9.1. Khai báo biến (dòng 13-16)

```java
private final AuthRepository authRepository;

private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
```

**Giải thích:**

- **`AuthRepository authRepository`**: Đối tượng Repository — nơi thực sự giao tiếp với Firebase.
- **`MutableLiveData<FirebaseUser> userLiveData`**: 
  - Kiểu `MutableLiveData` = LiveData có thể thay đổi giá trị (gọi `setValue()`).
  - Chứa `FirebaseUser` — đối tượng user từ Firebase Auth.
  - Khi `setValue()` được gọi → tất cả observer (ở RegisterActivity) được thông báo.
- **`MutableLiveData<String> errorLiveData`**: Chứa message lỗi (nếu có).
- **`MutableLiveData<Boolean> loadingLiveData`**: Trạng thái loading. Giá trị mặc định `false`.

### 9.2. Constructor (dòng 18-24)

```java
public AuthViewModel() {
    authRepository = new AuthRepository();
    if (authRepository.getCurrentUser() != null) {
        userLiveData.setValue(authRepository.getCurrentUser());
    }
}
```

- Tạo `AuthRepository` trực tiếp (không dùng Dependency Injection — phù hợp project đơn giản).
- Nếu user đã đăng nhập trước đó → set vào `userLiveData` ngay → observers biết ngay.

### 9.3. Getter — Expose LiveData (dòng 26-28)

```java
public LiveData<FirebaseUser> getUserLiveData()  { return userLiveData; }
public LiveData<String> getErrorLiveData()       { return errorLiveData; }
public LiveData<Boolean> getLoadingLiveData()    { return loadingLiveData; }
```

- Trả về `LiveData` (không phải `MutableLiveData`) → bên ngoài chỉ có thể **đọc**, không thể **thay đổi** giá trị.
- Đây là nguyên tắc **encapsulation** trong MVVM.

### 9.4. Phương thức register() (dòng 45-56) ⭐

```java
public void register(String email, String password, String name, String phone) {
    loadingLiveData.setValue(true);                          // ① Bật loading
    authRepository.register(email, password, name, phone)    // ② Gọi Repository
            .addOnSuccessListener(result -> {                 // ③ Thành công
                loadingLiveData.setValue(false);
                userLiveData.setValue(result.getUser());
            })
            .addOnFailureListener(e -> {                      // ④ Thất bại
                loadingLiveData.setValue(false);
                errorLiveData.setValue(e.getMessage());
            });
}
```

**Giải thích từng bước:**

| Bước | Dòng code | Ý nghĩa |
|------|-----------|----------|
| ① | `loadingLiveData.setValue(true)` | Bật trạng thái loading → UI hiện ProgressBar |
| ② | `authRepository.register(...)` | Gọi xuống Repository — Repository trả về `Task<AuthResult>` |
| ③ | `.addOnSuccessListener(result -> { ... })` | Khi đăng ký **thành công**: tắt loading + set user vào LiveData → UI nhận được user → chuyển màn hình |
| ④ | `.addOnFailureListener(e -> { ... })` | Khi đăng ký **thất bại**: tắt loading + set error vào LiveData → UI hiện Toast lỗi |

**`result.getUser()`** trả về `FirebaseUser` chứa:
- `getUid()` — ID duy nhất trên Firebase
- `getEmail()` — Email đã đăng ký
- Và nhiều thông tin khác...

### Vai trò của ViewModel:

```
RegisterActivity ──(gọi)──→ AuthViewModel ──(gọi)──→ AuthRepository
                   ←─(observe LiveData)──┘
```

- ViewModel **KHÔNG biết** về RegisterActivity (không import, không giữ reference).
- Nó chỉ cập nhật LiveData → Activity tự phản ứng thông qua Observer pattern.
- Lợi ích: khi xoay màn hình, ViewModel sống sót, dữ liệu không mất.

---

## 10. Bước 7: Truy cập Firebase — AuthRepository.java ⭐⭐⭐

📁 **File:** `app/src/main/java/com/example/foodnow/repositories/AuthRepository.java`

**ĐÂY LÀ FILE QUAN TRỌNG NHẤT** — nơi dữ liệu thật sự được ghi lên Firebase.

### 10.1. Khai báo và Constructor (dòng 12-20)

```java
public class AuthRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();
    }
```

- **`FirebaseAuth.getInstance()`**: Lấy singleton của Firebase Authentication SDK.
  - Dùng để tạo/xóa tài khoản, đăng nhập/đăng xuất.
  - Quản lý session user (tự lưu vào SharedPreferences → lần sau mở app không cần đăng nhập lại).
  
- **`FirebaseFirestore.getInstance()`**: Lấy singleton của Cloud Firestore SDK.
  - Dùng để đọc/ghi dữ liệu lên database trên cloud.
  - NoSQL database: dữ liệu lưu dạng documents trong collections.

### 10.2. Phương thức register() (dòng 28-46) ⭐⭐⭐

```java
public Task<AuthResult> register(String email, String password, String name, String phone) {
    return auth.createUserWithEmailAndPassword(email, password)    // BƯỚC A
            .addOnSuccessListener(result -> {                       // BƯỚC B
                FirebaseUser firebaseUser = result.getUser();
                if (firebaseUser != null) {
                    Map<String, Object> userData = new HashMap<>(); // BƯỚC C
                    userData.put("email", email);
                    userData.put("name", name);
                    userData.put("phone", phone);
                    userData.put("address", "");
                    userData.put("imageUrl", "");
                    userData.put("createdAt", com.google.firebase.Timestamp.now());

                    db.collection("Users")                          // BƯỚC D
                            .document(firebaseUser.getUid())
                            .set(userData);
                }
            });
}
```

### Giải thích CHI TIẾT từng bước:

---

#### BƯỚC A: Tạo tài khoản Firebase Auth

```java
auth.createUserWithEmailAndPassword(email, password)
```

- Gửi request đến **Firebase Authentication server**.
- Firebase Auth sẽ:
  1. Kiểm tra email chưa tồn tại.
  2. Kiểm tra password >= 6 ký tự (quy tắc mặc định của Firebase).
  3. Tạo tài khoản mới với một **UID duy nhất** (ví dụ: `"abc123XyZ456"`).
  4. Tự động đăng nhập user vào session hiện tại.
- Trả về: `Task<AuthResult>` — một "lời hứa" (promise) sẽ có kết quả trong tương lai.
- **Lưu ý**: Bước này chỉ tạo thông tin xác thực (email + password). Các thông tin khác (name, phone) Firebase Auth **KHÔNG lưu** → cần Firestore.

**Nếu thất bại** (email đã tồn tại, mật khẩu yếu, mất mạng...):
- Task chuyển sang trạng thái failure.
- Callback `addOnFailureListener` ở ViewModel sẽ được gọi.
- User nhận được Toast lỗi.

---

#### BƯỚC B: Callback khi tạo Auth thành công

```java
.addOnSuccessListener(result -> {
    FirebaseUser firebaseUser = result.getUser();
    if (firebaseUser != null) {
```

- **`addOnSuccessListener`**: Callback chạy khi Task hoàn thành thành công.
- **`result`** có kiểu `AuthResult` — chứa thông tin kết quả đăng ký.
- **`result.getUser()`**: Lấy `FirebaseUser` — đại diện cho user vừa tạo.
- **`if (firebaseUser != null)`**: Kiểm tra an toàn (luôn nên check null).

---

#### BƯỚC C: Chuẩn bị dữ liệu cho Firestore

```java
Map<String, Object> userData = new HashMap<>();
userData.put("email", email);
userData.put("name", name);
userData.put("phone", phone);
userData.put("address", "");
userData.put("imageUrl", "");
userData.put("createdAt", com.google.firebase.Timestamp.now());
```

**Giải thích:**

- **`HashMap<String, Object>`**: Cấu trúc key-value dùng để ghi lên Firestore.
  - Key = tên field (String)
  - Value = giá trị (Object — có thể là String, Number, Timestamp...)

- **Các field được ghi:**

| Field | Giá trị | Giải thích |
|-------|---------|------------|
| `email` | Từ tham số truyền vào | Email user vừa nhập (ví dụ: `"abc@gmail.com"`) |
| `name` | Từ tham số truyền vào | Họ tên (ví dụ: `"Nguyễn Văn A"`) |
| `phone` | Từ tham số truyền vào | SĐT (có thể rỗng nếu user không nhập) |
| `address` | `""` (chuỗi rỗng) | Địa chỉ — mặc định rỗng, user cập nhật sau ở trang Profile |
| `imageUrl` | `""` (chuỗi rỗng) | URL ảnh đại diện — mặc định rỗng, user upload sau ở trang Profile |
| `createdAt` | `Timestamp.now()` | Thời điểm tạo tài khoản — dùng Firestore Timestamp (chính xác đến nanosecond) |

**Tại sao dùng `Map` mà không dùng `User` object?**
- Firestore SDK có thể nhận `Map<String, Object>` hoặc POJO (User.java).
- Dùng `Map` ở đây vì đơn giản hơn — không cần tạo constructor cho trường hợp đăng ký (thiếu `id`).
- Khi **đọc** dữ liệu từ Firestore thì mới dùng `User.java` (xem phần UserRepository).

---

#### BƯỚC D: Ghi dữ liệu lên Firestore

```java
db.collection("Users")
        .document(firebaseUser.getUid())
        .set(userData);
```

**Giải thích từng phương thức:**

1. **`db.collection("Users")`**:
   - Truy cập collection `Users` trong Firestore.
   - Nếu collection chưa tồn tại → Firestore tự tạo khi ghi document đầu tiên.

2. **`.document(firebaseUser.getUid())`**:
   - Chỉ định document ID = UID từ Firebase Auth.
   - Ví dụ: nếu UID = `"abc123XyZ456"` → document path = `Users/abc123XyZ456`.
   - **Tại sao dùng UID làm document ID?** → Để dễ truy vấn: khi cần lấy thông tin user, chỉ cần `db.collection("Users").document(uid)` — không cần query.

3. **`.set(userData)`**:
   - Ghi toàn bộ `userData` map vào document.
   - Nếu document chưa tồn tại → tạo mới.
   - Nếu document đã tồn tại → ghi đè toàn bộ.
   - Trả về `Task<Void>` nhưng ở đây **không xử lý kết quả** (fire-and-forget).

### Kết quả trên Firestore:

Sau bước D, trên Firebase Console bạn sẽ thấy:

```
Firestore Database
└── Users (collection)
    └── abc123XyZ456 (document — ID = UID)
        ├── email     : "abc@gmail.com"
        ├── name      : "Nguyễn Văn A"
        ├── phone     : "0901234567"
        ├── address   : ""
        ├── imageUrl  : ""
        └── createdAt : October 15, 2024 at 10:30:00 AM UTC+7
```

### 10.3. Return type: Task<AuthResult>

```java
public Task<AuthResult> register(...) {
    return auth.createUserWithEmailAndPassword(...)
            .addOnSuccessListener(...);
}
```

- Phương thức trả về chính `Task` ban đầu (do `addOnSuccessListener` trả về cùng Task).
- ViewModel nhận Task này để gắn thêm `addOnSuccessListener` (set user) và `addOnFailureListener` (set error).
- **Một Task có thể có nhiều listener** — tất cả đều được gọi.

### 10.4. Timeline của toàn bộ phương thức:

```
Thời gian ──────────────────────────────────────────────→

1) auth.createUserWithEmailAndPassword()
   ───────[ gửi request đến Firebase Auth ]─────→ ✅ Auth tạo xong
                                                      │
2) addOnSuccessListener (AuthRepository)               │
   ← chạy callback ←──────────────────────────────────┘
   │
   ├── Chuẩn bị userData (HashMap)
   │
   └── db.collection("Users").document(uid).set(userData)
       ───────[ gửi request đến Firestore ]──────→ ✅ Data ghi xong
   
3) addOnSuccessListener (AuthViewModel)
   ← chạy callback (cùng lúc hoặc sau Repository listener) ←─┘
   │
   ├── loadingLiveData.setValue(false)
   └── userLiveData.setValue(result.getUser())
       │
       └──→ Observer ở RegisterActivity chạy
            │
            └──→ startActivity(MainActivity)    🎉 XONG!
```

**Lưu ý quan trọng:** Bước ghi Firestore (2) và bước set LiveData (3) chạy **gần như đồng thời** — vì cả hai đều là listener của cùng một Task. Trong thực tế, listener ở Repository chạy trước (vì đăng ký trước), nhưng việc ghi Firestore là **bất đồng bộ** — nên có thể user đã vào MainActivity trước khi Firestore ghi xong. Đây là **chấp nhận được** vì:
- Auth session đã sẵn sàng → user đã đăng nhập.
- Firestore ghi gần như tức thì (thường < 200ms).
- Nếu mất mạng, Firestore SDK có **offline persistence** → sẽ ghi khi có mạng lại.

---

## 11. Bước 8: Model dữ liệu — User.java

📁 **File:** `app/src/main/java/com/example/foodnow/models/User.java`

### 11.1. Cấu trúc class

```java
public class User {

    private String id;
    private String email;
    private String name;
    private String phone;
    private String address;
    private String imageUrl;
    private Timestamp createdAt;

    // Constructor rỗng — bắt buộc cho Firestore
    public User() {}

    public User(String id, String email, String name, String phone,
                String address, String imageUrl) { ... }

    // Getters + Setters cho tất cả field
}
```

### 11.2. Tại sao cần empty constructor?

```java
public User() {}
```

- Firestore SDK dùng **reflection** để tạo object khi đọc data.
- Quy trình: `new User()` → gọi setter cho từng field → trả về object hoàn chỉnh.
- **Nếu thiếu empty constructor** → crash runtime: `Could not deserialize object`.

### 11.3. Mapping giữa Java field và Firestore field

Firestore SDK tự động mapping theo **tên field** (hoặc tên getter/setter):

| Java field | Firestore field | Kiểu dữ liệu |
|-----------|-----------------|---------------|
| `id` | (không lưu — là document ID) | `String` |
| `email` | `email` | `String` |
| `name` | `name` | `String` |
| `phone` | `phone` | `String` |
| `address` | `address` | `String` |
| `imageUrl` | `imageUrl` | `String` |
| `createdAt` | `createdAt` | `Timestamp` |

**Lưu ý:** `id` không tự lưu trong document. Khi đọc, ta phải gán thủ công:
```java
User user = snapshot.toObject(User.class);
user.setId(snapshot.getId());  // Gán document ID vào field id
```

### 11.4. User.java chỉ dùng khi ĐỌC, không dùng khi GHI

- **Khi đăng ký (GHI)**: Dùng `Map<String, Object>` → trực tiếp hơn.
- **Khi hiển thị profile (ĐỌC)**: Dùng `User.java` → `snapshot.toObject(User.class)`.

---

## 12. Bước 9: Dữ liệu trên Firestore

### 12.1. Cấu trúc database

```
Firestore Database
│
├── Users (collection)
│   ├── uid_001 (document)
│   │   ├── email     : "user1@gmail.com"
│   │   ├── name      : "Nguyễn Văn A"
│   │   ├── phone     : "0901234567"
│   │   ├── address   : ""
│   │   ├── imageUrl  : ""
│   │   └── createdAt : Timestamp
│   │
│   ├── uid_002 (document)
│   │   └── ...
│   └── ...
```

### 12.2. Phân biệt Firebase Auth vs Firestore

| | Firebase Auth | Cloud Firestore |
|---|---|---|
| **Mục đích** | Xác thực (authentication) | Lưu trữ dữ liệu (database) |
| **Lưu gì?** | Email, password (đã hash), UID | Name, phone, address, imageUrl, createdAt |
| **Truy cập** | `FirebaseAuth.getInstance()` | `FirebaseFirestore.getInstance()` |
| **Console** | Firebase Console → Authentication | Firebase Console → Firestore Database |
| **Khi đăng ký** | Tạo 1 entry trong Auth | Tạo 1 document trong collection `Users` |

**Tại sao cần cả hai?**
- Firebase Auth chỉ lưu email + password — không lưu thông tin profile (name, phone, address...).
- Firestore lưu toàn bộ thông tin phụ trợ — sử dụng UID từ Auth làm key liên kết.

---

## 13. Bước 10: Sau khi đăng ký thành công — vào MainActivity

📁 **File:** `app/src/main/java/com/example/foodnow/MainActivity.java`

### 13.1. Chuyển màn hình (từ RegisterActivity)

```java
// Trong RegisterActivity — observer userLiveData:
Intent intent = new Intent(this, MainActivity.class);
intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
startActivity(intent);
```

- `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`:
  - Xóa toàn bộ back stack (LoginActivity + RegisterActivity).
  - User bấm back → thoát app (không quay lại login).

### 13.2. MainActivity khởi tạo

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    CloudinaryHelper.init(getApplicationContext());  // Khởi tạo Cloudinary
    setContentView(R.layout.activity_main);
    // ... setup BottomNavigationView với 5 tab
    if (savedInstanceState == null) {
        loadFragment(new HomeFragment());             // Mặc định: Trang chủ
    }
}
```

- Từ đây user có thể vào tab **Tài khoản** (ProfileFragment) để:
  - Xem/sửa thông tin cá nhân (name, phone, address).
  - Upload ảnh đại diện (dùng Cloudinary).

---

## 14. Sơ đồ luồng dữ liệu chi tiết

```
┌───────────────────── USER ACTION ─────────────────────┐
│  User điền: name, phone, email, password               │
│  User bấm nút "Đăng ký"                                │
└──────────────────────┬────────────────────────────────┘
                       │
        ┌──────────────▼──────────────┐
        │     RegisterActivity        │
        │ ① Validate dữ liệu         │
        │   - name, email, password   │
        │     không được rỗng         │
        │ ② Gọi ViewModel            │
        │   authViewModel.register(   │
        │     email, password,        │
        │     name, phone             │
        │   )                         │
        └──────────────┬──────────────┘
                       │
        ┌──────────────▼──────────────┐
        │      AuthViewModel          │
        │ ③ Set loading = true        │
        │ ④ Gọi Repository           │
        │   authRepository.register(  │
        │     email, password,        │
        │     name, phone             │
        │   )                         │
        └──────────────┬──────────────┘
                       │
        ┌──────────────▼──────────────┐
        │      AuthRepository         │
        │                             │
        │ ⑤ Firebase Auth:            │
        │   createUserWithEmail       │
        │   AndPassword()             │
        │       │                     │
        │       ▼ (thành công)        │
        │ ⑥ Chuẩn bị userData Map:   │
        │   email, name, phone,       │
        │   address="", imageUrl="",  │
        │   createdAt=now()           │
        │       │                     │
        │ ⑦ Firestore:               │
        │   Users/{uid}.set(userData) │
        └──────────────┬──────────────┘
                       │
        ┌──────────────▼──────────────┐
        │    Firebase Cloud           │
        │                             │
        │ ⑧ Auth: lưu email+password │
        │    → cấp UID duy nhất       │
        │                             │
        │ ⑨ Firestore: tạo document  │
        │    Users/{uid} với 6 field  │
        └──────────────┬──────────────┘
                       │
                       │ (callback thành công)
                       ▼
        ┌──────────────────────────────┐
        │      AuthViewModel           │
        │ ⑩ Set loading = false        │
        │ ⑪ Set user = FirebaseUser    │
        │    (trigger Observer)        │
        └──────────────┬───────────────┘
                       │
        ┌──────────────▼──────────────┐
        │    RegisterActivity          │
        │ ⑫ Observer nhận user ≠ null │
        │ ⑬ startActivity(Main)       │
        │    + clear back stack        │
        └──────────────┬──────────────┘
                       │
        ┌──────────────▼──────────────┐
        │      MainActivity           │
        │ ⑭ Hiển thị trang chủ        │
        │    User đã đăng nhập! 🎉    │
        └─────────────────────────────┘
```

---

## 15. Giải thích các khái niệm quan trọng

### 15.1. Task<T> là gì?

```java
Task<AuthResult> task = auth.createUserWithEmailAndPassword(email, password);
```

- `Task<T>` là class của Google Play Services — đại diện cho **thao tác bất đồng bộ**.
- Tương tự `Promise` trong JavaScript hoặc `Future` trong Dart.
- Có thể:
  - `.addOnSuccessListener(result -> { ... })` — callback khi thành công
  - `.addOnFailureListener(e -> { ... })` — callback khi thất bại
  - `.addOnCompleteListener(task -> { ... })` — callback khi hoàn thành (cả thành công lẫn thất bại)

### 15.2. LiveData là gì?

```java
MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
```

- `LiveData` là class của Android Architecture Components.
- Nó **giữ dữ liệu** và **thông báo cho observers** khi dữ liệu thay đổi.
- **Lifecycle-aware**: tự hủy lắng nghe khi Activity/Fragment bị destroy → không memory leak.
- `MutableLiveData`: cho phép gọi `setValue()` / `postValue()`.
- `LiveData` (không mutable): chỉ cho phép `observe()`.

### 15.3. ViewModel là gì?

- Class đặc biệt tồn tại **lâu hơn Activity/Fragment**.
- Khi xoay màn hình: Activity bị destroy → tạo lại, nhưng ViewModel **vẫn sống**.
- Dùng để giữ dữ liệu UI (thông qua LiveData) mà không bị mất khi configuration change.

### 15.4. Repository Pattern

- Lớp trung gian giữa ViewModel và data source (Firebase).
- Lợi ích:
  - ViewModel không biết dữ liệu đến từ đâu (Firebase, local DB, API...).
  - Nếu đổi backend (từ Firebase sang API riêng), chỉ cần sửa Repository.
  - Dễ test.

### 15.5. Firebase Timestamp

```java
userData.put("createdAt", com.google.firebase.Timestamp.now());
```

- `Timestamp.now()` tạo timestamp chính xác tại thời điểm gọi.
- Lưu trên Firestore dạng native Timestamp — có thể sort, query theo thời gian.
- Khác với `System.currentTimeMillis()` — Firestore Timestamp chính xác hơn và hỗ trợ timezone.

---

## 16. Bảng tóm tắt tất cả file liên quan

| File | Đường dẫn | Vai trò trong luồng đăng ký |
|------|-----------|----------------------------|
| AndroidManifest.xml | `app/src/main/AndroidManifest.xml` | Khai báo LoginActivity là launcher |
| activity_login.xml | `app/src/main/res/layout/activity_login.xml` | Giao diện đăng nhập — có link sang đăng ký |
| LoginActivity.java | `app/.../activities/LoginActivity.java` | Màn hình đăng nhập — điều hướng sang RegisterActivity |
| activity_register.xml | `app/src/main/res/layout/activity_register.xml` | Giao diện đăng ký — 4 ô nhập + nút đăng ký |
| **RegisterActivity.java** | `app/.../activities/RegisterActivity.java` | **UI logic**: validate, gọi ViewModel, observe kết quả |
| **AuthViewModel.java** | `app/.../viewmodels/AuthViewModel.java` | **Trung gian**: quản lý loading/error/user LiveData |
| **AuthRepository.java** | `app/.../repositories/AuthRepository.java` | **⭐ Core logic**: gọi Firebase Auth + ghi Firestore |
| User.java | `app/.../models/User.java` | Model dữ liệu — dùng khi đọc từ Firestore |
| db.md | `.github/db.md` | Schema database Firestore |
| MainActivity.java | `app/.../MainActivity.java` | Màn hình chính sau đăng ký thành công |

### Đường dẫn đầy đủ để đọc code (thứ tự):

```
① app/src/main/AndroidManifest.xml
② app/src/main/res/layout/activity_login.xml
③ app/src/main/java/com/example/foodnow/activities/LoginActivity.java
④ app/src/main/res/layout/activity_register.xml
⑤ app/src/main/java/com/example/foodnow/activities/RegisterActivity.java
⑥ app/src/main/java/com/example/foodnow/viewmodels/AuthViewModel.java
⑦ app/src/main/java/com/example/foodnow/repositories/AuthRepository.java  ← ⭐ quan trọng nhất
⑧ app/src/main/java/com/example/foodnow/models/User.java
⑨ .github/db.md
```

---

> 📝 **Ghi chú cuối:** Sau khi đăng ký, user sẽ có:
> - 1 entry trong **Firebase Auth** (email + password + UID).
> - 1 document trong **Firestore** tại `Users/{UID}` (email, name, phone, address, imageUrl, createdAt).
> - Session đăng nhập tự động — lần sau mở app không cần đăng nhập lại.
> - Profile mặc định: address = "", imageUrl = "" — cập nhật sau ở trang Tài khoản.
