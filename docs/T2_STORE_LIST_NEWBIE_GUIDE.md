# Huong dan newbie: Tu code tinh nang T2 - Store List tren Home

> Muc tieu: tu tay xay dung tinh nang hien thi danh sach quan an tren trang chu theo MVVM + Firestore.
>  
> Ban se lam duoc: `Model -> Repository -> ViewModel -> Adapter -> Layout -> Fragment`.

---

## 1. Ban se xay gi?

Sau khi xong, `HomeFragment` se co:
1. Danh muc (da co san, hien thi ngang).
2. Danh sach quan an (ban tu them, hien thi doc).
3. Click vao quan: tam thoi hien Toast (de test luong click).

Day la tinh nang tiep theo hop ly nhat vi:
- Thuoc **Core Flow** (sau T1).
- De nhin thay ket qua ngay tren UI.
- Giup ban hieu du moi lop trong MVVM.

---

## 2. Kien thuc can co truoc khi bat dau

1. Da sync project thanh cong trong Android Studio.
2. Firestore da co collection `Stores`.
3. Ban da hieu co ban ve RecyclerView.
4. Ban chap nhan lam them 1 buoc tien de nho: tao `Store.java` (vi du an chua co model nay).

---

## 3. Danh sach file can tao/sua

### Tao moi
- `app/src/main/java/com/example/foodnow/models/Store.java`
- `app/src/main/java/com/example/foodnow/repositories/StoreRepository.java`
- `app/src/main/java/com/example/foodnow/adapters/StoreAdapter.java`
- `app/src/main/res/layout/item_store.xml`

### Sua
- `app/src/main/java/com/example/foodnow/viewmodels/HomeViewModel.java`
- `app/src/main/java/com/example/foodnow/fragments/HomeFragment.java`
- `app/src/main/res/layout/fragment_home.xml`

---

## 4. Lam tung buoc (chi tiet cho newbie)

## Buoc 0 - Tao nhanh branch rieng

```bash
git checkout -b feature/t2-store-list
```

Neu code loi, ban co the bo branch nay ma khong anh huong code chinh.

---

## Buoc 1 - Tao model `Store.java`

**Muc tieu:** map document Firestore `Stores` <-> object Java.

Trong `Store.java`, tao cac field:
- `id`
- `name`
- `description`
- `address`
- `phone`
- `imageUrl`
- `rating` (float)
- `deliveryTime`
- `deliveryFee` (long)
- `isOpen` (boolean)
- `storeOwnerId` (String, de mo rong sau nay)

**Bat buoc:**
1. Co **constructor rong** (`public Store() {}`) cho Firestore.
2. Co getter/setter day du.

> Meo: copy style code tu `User.java` va `Category.java` de dong bo convention.

---

## Buoc 2 - Tao `StoreRepository.java`

**Muc tieu:** doc du lieu `Stores` tu Firestore bang real-time listener.

Pattern giong `CategoryRepository`:
1. `FirebaseFirestore db = FirebaseFirestore.getInstance();`
2. Method: `LiveData<List<Store>> getAllStores()`
3. Trong `addSnapshotListener`:
   - Neu `error != null || snapshots == null` -> return
   - Tao `List<Store> list = new ArrayList<>();`
   - Loop documents:
     - `Store store = doc.toObject(Store.class);`
     - `store.setId(doc.getId());`
     - `list.add(store);`
   - `liveData.setValue(list);`

> Dung cung convention dat ten collection: `"Stores"` (chu S hoa, giong db.md).

---

## Buoc 3 - Tao `item_store.xml`

**Muc tieu:** 1 item hien thi quan an trong danh sach doc.

Goi y bo cuc don gian (de de debug):
1. `CardView` ngoai cung, margin 8dp.
2. Ben trong dung `LinearLayout` ngang.
3. Ben trai: `ImageView` (id: `img_store`, kich thuoc ~80dp).
4. Ben phai: cot text:
   - `TextView` ten quan (`tv_store_name`)
   - `TextView` dia chi (`tv_store_address`)
   - `TextView` rating + fee (`tv_store_meta`)

Khong can dep ngay. Uu tien dung id ro rang.

---

## Buoc 4 - Tao `StoreAdapter.java`

**Muc tieu:** bind `List<Store>` vao RecyclerView.

Lam theo khuon `CategoryAdapter`:
1. Constructor nhan:
   - `Context context`
   - `List<Store> storeList`
   - `OnStoreClickListener listener`
2. Interface:
   - `void onStoreClick(Store store);`
3. `onCreateViewHolder`: inflate `R.layout.item_store`
4. `onBindViewHolder`:
   - set ten, dia chi
   - set text meta (rating, phi ship)
   - Glide load `imageUrl` neu co
   - `itemView.setOnClickListener(...)` goi callback
5. `getItemCount`: tra ve `storeList.size()`

> Meo dinh dang phi ship:
>- Ban co the hien thi tam `deliveryFee + "đ"` truoc.
>- Sau nay toi uu bang `NumberFormat` theo locale `vi-VN`.

---

## Buoc 5 - Sua `HomeViewModel.java`

Hien tai HomeViewModel chi co categories.

Can them:
1. Import `Store`, `StoreRepository`.
2. Field:
   - `private final LiveData<List<Store>> stores;`
3. Trong constructor:
   - `StoreRepository storeRepo = new StoreRepository();`
   - `stores = storeRepo.getAllStores();`
4. Getter:
   - `public LiveData<List<Store>> getStores() { return stores; }`

> Nho: giu nguyen `getCategories()` da co, khong xoa.

---

## Buoc 6 - Sua `fragment_home.xml`

Hien tai layout moi co title "Danh muc" + `rv_categories`.

Can them:
1. `TextView` title thu 2 (vi du: `tv_store_title`, text = "Quan an gan ban")
2. `RecyclerView` doc cho stores (id: `rv_stores`)

Goi y:
- Dat `rv_stores` ben duoi danh muc.
- Neu man hinh bi tran, boc toan bo trong `NestedScrollView` hoac dung 1 layout hop ly de cuon.
- Cach de nhat cho ban dau:
  - title danh muc
  - rv_categories (wrap_content)
  - title stores
  - rv_stores (0dp + constraint bottom parent)

---

## Buoc 7 - Sua `HomeFragment.java`

Hien tai HomeFragment chi setup danh muc.

Can them:
1. Bien moi:
   - `RecyclerView rvStores;`
   - `StoreAdapter storeAdapter;`
   - `List<Store> storeList = new ArrayList<>();`
2. `findViewById` cho `rv_stores`.
3. Khoi tao `StoreAdapter`:
   - callback click -> Toast ten quan de test
4. Set `LayoutManager` doc cho `rvStores`.
5. Observe `homeViewModel.getStores()`:
   - clear list
   - addAll
   - notifyDataSetChanged

**Checklist nhanh cho HomeFragment:**
- [ ] Import dung `Store` va `StoreAdapter`
- [ ] Co `new ViewModelProvider(this).get(HomeViewModel.class)`
- [ ] Observe ca `getCategories()` va `getStores()`
- [ ] Khong xoa logic categories cu

---

## Buoc 8 - Chuan bi du lieu Firestore de test

Vao Firebase Console -> Firestore -> collection `Stores`, tao 2-3 document:

```json
{
  "name": "Pho Ha Noi",
  "description": "Pho truyen thong",
  "address": "45 Nguyen Chi Thanh",
  "phone": "0911111111",
  "imageUrl": "https://images.unsplash.com/photo-1544025162-d76694265947",
  "rating": 4.8,
  "deliveryTime": "15 phut",
  "deliveryFee": 15000,
  "isOpen": true,
  "storeOwnerId": ""
}
```

Neu field sai kieu (vi du `rating` la string), app de loi map data.

---

## Buoc 9 - Chay app va tu kiem tra

Chay lai project:

```bash
.\gradlew.bat assembleDebug
```

Mo app:
1. Dang nhap.
2. Vao tab Trang chu.
3. Kiem tra:
   - Danh muc van hien thi.
   - Danh sach quan hien thi dung du lieu Firestore.
   - Bam item quan -> hien Toast.

---

## 5. Loi thuong gap va cach sua nhanh

1. **Khong hien store nao**
   - Kiem tra collection ten dung `"Stores"` chua.
   - Kiem tra Firestore rules cho phep read.
   - Kiem tra field map dung kieu (rating float, deliveryFee number).

2. **Crash do NullPointer**
   - Kiem tra `findViewById(R.id.rv_stores)` co id dung trong XML.
   - Kiem tra da `setAdapter` cho `rvStores` chua.

3. **Adapter khong update**
   - Kiem tra co `notifyDataSetChanged()` sau `addAll()` chua.
   - Kiem tra observe dang dung `getViewLifecycleOwner()`.

4. **Anh khong hien**
   - URL anh sai/het han.
   - Tam thay bang placeholder de test logic.

---

## 6. Tieu chi hoan thanh T2 (Definition of Done)

- [ ] Tao du `Store.java` + getter/setter + empty constructor.
- [ ] Co `StoreRepository.getAllStores()` tra ve LiveData.
- [ ] Co `StoreAdapter` + `item_store.xml`.
- [ ] `HomeViewModel` expose `getStores()`.
- [ ] `HomeFragment` hien thi danh sach stores doc.
- [ ] Click store co phan hoi (Toast la du cho T2).
- [ ] Build debug thanh cong.

---

## 7. Lam tiep gi sau khi xong T2?

Neu T2 on dinh, ban chuyen ngay sang **T3 - Store Detail + Food List**:
1. Tao `Food.java`
2. Tao `FoodRepository.getFoodsByStore(storeId)`
3. Tao `StoreDetailActivity`
4. Tu click store (T2) -> mo StoreDetailActivity (T3)

Luc do ban se co luong hoan chinh dau tien:
**Home (Store list) -> Store detail (Food list)**.

