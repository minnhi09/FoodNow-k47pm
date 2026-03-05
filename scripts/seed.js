/**
 * Seed dữ liệu mẫu vào Firestore cho FoodNow.
 *
 * Cách chạy:
 *   1. Đặt file serviceAccountKey.json vào thư mục scripts/
 *   2. cd scripts && node seed.js
 *
 * Script sẽ tạo dữ liệu cho 3 collections: Categories, Stores, Foods.
 * Các collection Users, Orders, Favorites sẽ do app tạo khi sử dụng.
 */

const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const db = admin.firestore();

// ═══════════════════════════════════════════
// DỮ LIỆU MẪU
// ═══════════════════════════════════════════

const categories = [
  { name: "Phở & Bún", imageUrl: "" },
  { name: "Cơm", imageUrl: "" },
  { name: "Pizza & Burger", imageUrl: "" },
  { name: "Trà sữa & Nước", imageUrl: "" },
  { name: "Bánh mì", imageUrl: "" },
  { name: "Lẩu & Nướng", imageUrl: "" },
];

const stores = [
  {
    name: "Phở Hà Nội",
    description: "Phở bò truyền thống Hà Nội, nước dùng ninh xương 12 tiếng",
    address: "45 Nguyễn Chí Thanh, Đà Lạt",
    phone: "0911111111",
    imageUrl: "",
    rating: 4.8,
    deliveryTime: "15 phút",
    deliveryFee: 15000,
    isOpen: true,
  },
  {
    name: "Cơm Tấm Sài Gòn",
    description: "Cơm tấm sườn bì chả đúng vị Sài Gòn",
    address: "12 Phan Đình Phùng, Đà Lạt",
    phone: "0922222222",
    imageUrl: "",
    rating: 4.5,
    deliveryTime: "20 phút",
    deliveryFee: 10000,
    isOpen: true,
  },
  {
    name: "Pizza House",
    description: "Pizza & Burger phong cách Ý, đế bánh mỏng giòn",
    address: "78 Trần Phú, Đà Lạt",
    phone: "0933333333",
    imageUrl: "",
    rating: 4.6,
    deliveryTime: "25 phút",
    deliveryFee: 20000,
    isOpen: true,
  },
  {
    name: "Trà Sữa ToCoToCo",
    description: "Trà sữa tươi, topping phong phú",
    address: "56 Bùi Thị Xuân, Đà Lạt",
    phone: "0944444444",
    imageUrl: "",
    rating: 4.3,
    deliveryTime: "10 phút",
    deliveryFee: 10000,
    isOpen: true,
  },
  {
    name: "Lẩu Bò Năm Sao",
    description: "Lẩu bò nhúng dấm, nướng BBQ Hàn Quốc",
    address: "99 Hai Bà Trưng, Đà Lạt",
    phone: "0955555555",
    imageUrl: "",
    rating: 4.7,
    deliveryTime: "30 phút",
    deliveryFee: 15000,
    isOpen: true,
  },
];

// Foods sẽ được tạo sau khi có storeId và categoryId thực tế
// Mỗi entry chứa storeIndex và categoryIndex để liên kết
const foodsTemplate = [
  // ── Phở Hà Nội (store 0) ──
  {
    title: "Phở bò tái",
    description: "Phở bò tái truyền thống, nước dùng đậm đà",
    price: 55000,
    imageUrl: "",
    rating: 4.9,
    storeIndex: 0,
    categoryIndex: 0, // Phở & Bún
    isAvailable: true,
  },
  {
    title: "Phở bò chín",
    description: "Phở bò chín mềm, thơm ngon",
    price: 55000,
    imageUrl: "",
    rating: 4.7,
    storeIndex: 0,
    categoryIndex: 0,
    isAvailable: true,
  },
  {
    title: "Bún bò Huế",
    description: "Bún bò Huế cay nồng đặc trưng",
    price: 60000,
    imageUrl: "",
    rating: 4.8,
    storeIndex: 0,
    categoryIndex: 0,
    isAvailable: true,
  },

  // ── Cơm Tấm Sài Gòn (store 1) ──
  {
    title: "Cơm tấm sườn bì chả",
    description: "Sườn nướng than, bì giòn, chả trứng",
    price: 45000,
    imageUrl: "",
    rating: 4.6,
    storeIndex: 1,
    categoryIndex: 1, // Cơm
    isAvailable: true,
  },
  {
    title: "Cơm tấm sườn trứng",
    description: "Sườn nướng kèm trứng ốp la",
    price: 40000,
    imageUrl: "",
    rating: 4.4,
    storeIndex: 1,
    categoryIndex: 1,
    isAvailable: true,
  },
  {
    title: "Cơm chiên Dương Châu",
    description: "Cơm chiên với tôm, lạp xưởng, trứng",
    price: 50000,
    imageUrl: "",
    rating: 4.5,
    storeIndex: 1,
    categoryIndex: 1,
    isAvailable: true,
  },
  {
    title: "Cơm gà xối mỡ",
    description: "Gà chiên giòn, cơm tấm dẻo thơm",
    price: 50000,
    imageUrl: "",
    rating: 4.3,
    storeIndex: 1,
    categoryIndex: 1,
    isAvailable: true,
  },

  // ── Pizza House (store 2) ──
  {
    title: "Pizza Margherita",
    description: "Pizza cà chua, phô mai mozzarella, húng quế",
    price: 120000,
    imageUrl: "",
    rating: 4.7,
    storeIndex: 2,
    categoryIndex: 2, // Pizza & Burger
    isAvailable: true,
  },
  {
    title: "Pizza Hải Sản",
    description: "Tôm, mực, sò điệp trên đế giòn",
    price: 150000,
    imageUrl: "",
    rating: 4.8,
    storeIndex: 2,
    categoryIndex: 2,
    isAvailable: true,
  },
  {
    title: "Burger Bò Phô Mai",
    description: "Bò Úc, phô mai cheddar, rau tươi",
    price: 75000,
    imageUrl: "",
    rating: 4.5,
    storeIndex: 2,
    categoryIndex: 2,
    isAvailable: true,
  },
  {
    title: "Khoai tây chiên",
    description: "Khoai tây chiên giòn, sốt cà chua",
    price: 35000,
    imageUrl: "",
    rating: 4.2,
    storeIndex: 2,
    categoryIndex: 2,
    isAvailable: true,
  },

  // ── Trà Sữa ToCoToCo (store 3) ──
  {
    title: "Trà sữa trân châu đường đen",
    description: "Trà sữa tươi, trân châu đường đen dẻo mềm",
    price: 35000,
    imageUrl: "",
    rating: 4.5,
    storeIndex: 3,
    categoryIndex: 3, // Trà sữa & Nước
    isAvailable: true,
  },
  {
    title: "Trà đào cam sả",
    description: "Trà đào tươi, cam vắt, sả thơm",
    price: 30000,
    imageUrl: "",
    rating: 4.6,
    storeIndex: 3,
    categoryIndex: 3,
    isAvailable: true,
  },
  {
    title: "Matcha đá xay",
    description: "Matcha Nhật Bản xay đá, kem whipping",
    price: 45000,
    imageUrl: "",
    rating: 4.4,
    storeIndex: 3,
    categoryIndex: 3,
    isAvailable: true,
  },
  {
    title: "Sinh tố bơ",
    description: "Bơ sáp Đắk Lắk xay nhuyễn, béo ngậy",
    price: 40000,
    imageUrl: "",
    rating: 4.3,
    storeIndex: 3,
    categoryIndex: 3,
    isAvailable: true,
  },

  // ── Lẩu Bò Năm Sao (store 4) ──
  {
    title: "Lẩu bò nhúng dấm",
    description: "Bò Mỹ thái lát mỏng, nước dùng chua ngọt",
    price: 199000,
    imageUrl: "",
    rating: 4.9,
    storeIndex: 4,
    categoryIndex: 5, // Lẩu & Nướng
    isAvailable: true,
  },
  {
    title: "Lẩu Thái Tom Yum",
    description: "Nước dùng chua cay kiểu Thái, hải sản tươi",
    price: 219000,
    imageUrl: "",
    rating: 4.8,
    storeIndex: 4,
    categoryIndex: 5,
    isAvailable: true,
  },
  {
    title: "Nướng BBQ tổng hợp",
    description: "Bò, heo, gà ướp sốt BBQ Hàn Quốc",
    price: 179000,
    imageUrl: "",
    rating: 4.7,
    storeIndex: 4,
    categoryIndex: 5,
    isAvailable: true,
  },
];

// ═══════════════════════════════════════════
// LOGIC SEED
// ═══════════════════════════════════════════

async function seed() {
  console.log("🚀 Bắt đầu seed dữ liệu...\n");

  // 1. Seed Categories
  console.log("📂 Đang tạo Categories...");
  const categoryIds = [];
  for (const cat of categories) {
    const ref = await db.collection("Categories").add(cat);
    categoryIds.push(ref.id);
    console.log(`   ✅ ${cat.name} → ${ref.id}`);
  }

  // 2. Seed Stores
  console.log("\n🏪 Đang tạo Stores...");
  const storeIds = [];
  for (const store of stores) {
    const ref = await db.collection("Stores").add(store);
    storeIds.push(ref.id);
    console.log(`   ✅ ${store.name} → ${ref.id}`);
  }

  // 3. Seed Foods (liên kết storeId + categoryId)
  console.log("\n🍔 Đang tạo Foods...");
  for (const food of foodsTemplate) {
    const { storeIndex, categoryIndex, ...foodData } = food;
    foodData.storeId = storeIds[storeIndex];
    foodData.categoryId = categoryIds[categoryIndex];

    const ref = await db.collection("Foods").add(foodData);
    console.log(`   ✅ ${foodData.title} (${stores[storeIndex].name}) → ${ref.id}`);
  }

  console.log("\n════════════════════════════════════════");
  console.log("✅ Seed hoàn tất!");
  console.log(`   📂 ${categories.length} Categories`);
  console.log(`   🏪 ${stores.length} Stores`);
  console.log(`   🍔 ${foodsTemplate.length} Foods`);
  console.log("════════════════════════════════════════\n");
  console.log("💡 Nhớ thay imageUrl bằng URL ảnh thật trên Firebase Console hoặc chạy script update riêng.");

  process.exit(0);
}

seed().catch((err) => {
  console.error("❌ Lỗi seed:", err);
  process.exit(1);
});
