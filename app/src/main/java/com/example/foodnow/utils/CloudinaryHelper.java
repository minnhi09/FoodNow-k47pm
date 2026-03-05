package com.example.foodnow.utils;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class để upload ảnh lên Cloudinary.
 *
 * Trước khi gọi upload, cần khởi tạo 1 lần trong Application hoặc Activity:
 *
 *   Map<String, Object> config = new HashMap<>();
 *   config.put("cloud_name", "YOUR_CLOUD_NAME");
 *   config.put("api_key",    "YOUR_API_KEY");
 *   config.put("api_secret", "YOUR_API_SECRET");
 *   MediaManager.init(context, config);
 */
public class CloudinaryHelper {

    private static boolean initialized = false;

    /**
     * Khởi tạo Cloudinary (gọi 1 lần duy nhất).
     * Thay YOUR_CLOUD_NAME, YOUR_API_KEY, YOUR_API_SECRET bằng thông tin thật.
     */
    public static void init(Context context) {
        if (initialized) return;
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "YOUR_CLOUD_NAME"); // TODO: thay bằng cloud_name thật từ Cloudinary Dashboard
        // Với unsigned upload, không cần api_key/api_secret
        MediaManager.init(context, config);
        initialized = true;
    }

    /**
     * Upload ảnh lên Cloudinary.
     * @param uri       URI của ảnh (từ gallery picker)
     * @param callback  callback nhận kết quả
     */
    public static void uploadImage(Uri uri, UploadCallback callback) {
        MediaManager.get().upload(uri)
                .unsigned("foodnow_unsigned") // preset name trên Cloudinary
                .callback(callback)
                .dispatch();
    }
}
