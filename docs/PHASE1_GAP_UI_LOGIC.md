# Phase 1 - Chot gap UI/logic (Orders, Cart, Favorites)

## 1) Scope da chot

- UI files:
  - `app/src/main/res/layout/fragment_orders.xml`
  - `app/src/main/res/layout/fragment_cart.xml`
  - `app/src/main/res/layout/fragment_favorites.xml`
- Fragment files:
  - `app/src/main/java/com/example/foodnow/fragments/OrdersFragment.java`
  - `app/src/main/java/com/example/foodnow/fragments/CartFragment.java`
  - `app/src/main/java/com/example/foodnow/fragments/FavoritesFragment.java`

Muc tieu Phase 1: khoa baseline, xac dinh ro phan thieu UI/logic truoc khi vao phase thiet ke lai.

## 2) Baseline hien tai (da xac nhan)

### Orders
- `fragment_orders.xml`: chi co `RecyclerView` + `TextView` empty state.
- `OrdersFragment`: bind data vao list, toggle empty state, chua co filter/tracking/detail.

### Cart
- `fragment_cart.xml`: chi co ten quan, list, subtotal text, nut "Thanh toan".
- `CartFragment`: doc `CartManager`, cap nhat tong tien va empty state, mo `CheckoutActivity`.

### Favorites
- `fragment_favorites.xml`: chi co `RecyclerView` + `TextView` empty state.
- `FavoritesFragment`: load danh sach, xoa favorite, toggle empty state.

## 3) Gap UI can dong

| Man hinh | Gap UI chinh | Anh huong |
|---|---|---|
| Orders | Thieu header tong don + tab loc (Tat ca/Dang xu ly/Hoan thanh), thieu card item theo mockup (status chip, ETA, CTA Theo doi/Chi tiet). | Nguoi dung khong loc nhanh, khong follow don.
| Orders | Chua co giao dien theo doi don hang rieng (timeline trang thai). | Flow don hang bi dut doan sau list.
| Cart | Thieu header "Gio hang cua ban" + so mon, thieu card dia chi, voucher, payment, footer dat hang sticky. | Trai nghiem checkout khong giong mockup, khoi thong tin thanh toan thieu.
| Cart | Item card chua co style remove icon/chu thich/stepper nhu design. | Nhat quan UI kem, kho thao tac nhanh.
| Favorites | Thieu header "Yeu thich" + count, thieu tab loc (Tat ca/Quan an/Mon an). | Khong phan loai du lieu theo loai.
| Favorites | Item card chua co status chip type, icon tim bo goc tren, nut them gio cho mon an. | Chua phu hop use-case "favorite food -> add cart".

## 4) Gap logic can dong

| File | Gap logic | Muc do |
|---|---|---|
| `OrdersFragment.java` | Chua co filter theo status tren UI (Tat ca/Dang xu ly/Hoan thanh). | P0 |
| `OrdersFragment.java` | Chua co callback click item/CTA de mo man "Theo doi" va "Chi tiet". | P0 |
| `OrdersFragment.java` | Chua co loading/error state khi Firestore fail (hien tai im lang). | P1 |
| `CartFragment.java` | Chua refresh UI o `onResume()`; sau khi dat hang thanh cong va quay lai co nguy co hien thi stale subtotal/list. | P0 |
| `CartFragment.java` | Chua co logic tong hop item count cho header va badge dong. | P0 |
| `FavoritesFragment.java` | Chua co logic loc `type` (store/food) theo tab. | P0 |
| `FavoritesFragment.java` | Chua co item click navigation (mo quyen quan/mon) va action them gio cho type food. | P1 |
| `FavoritesFragment.java` | Chua co loading/error state khi query Favorites loi. | P1 |

## 5) Definition of Done cho Phase 1

- [ ] Co tai lieu gap duoc chot va review trong team (file nay).
- [ ] Co danh sach task UI + logic tach theo P0/P1, khong mo ho.
- [ ] Scope gioi han trong 6 file muc tieu va cac dependency lien quan duoc ghi chu ro.
- [ ] Co acceptance criteria cho tung man (Orders/Cart/Favorites) de vao implementation phase tiep theo.

## 6) Backlog de bat dau implementation

### P0 (lam truoc)
1. Orders: them bo loc status tren UI va logic filter trong fragment.
2. Orders: mo duoc man "Theo doi" va "Chi tiet" tu item/CTA.
3. Cart: them refresh `onResume()` de dong bo UI sau checkout.
4. Cart: cap nhat item count va tong tien theo style moi.
5. Favorites: them tab loc Tat ca/Quan an/Mon an + filter local theo `Favorite.type`.

### P1 (lam sau P0)
1. Orders/Favorites: bo sung loading + error state than thien.
2. Favorites: item click -> dieu huong den man chi tiet tuong ung.
3. Favorites(food): action "+" them vao gio (khi da co map du lieu mon).
4. Hoan thien polish empty state theo nguyen tac UI chung.

## 7) Ghi chu dependency ngoai scope 6 file

- De lam duoc CTA Theo doi/Chi tiet can mo rong `item_order.xml` + `OrderAdapter`.
- De lam them gio tu Favorites(food) can mo rong `item_favorite.xml` + `FavoriteAdapter` va map du lieu du tu `Favorite`.
- De co badge dong can chinh `MainActivity.setupCartBadge(...)` theo `CartManager.getItemCount()`.

