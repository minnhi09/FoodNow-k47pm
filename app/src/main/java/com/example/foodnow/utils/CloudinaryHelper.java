package com.example.foodnow.utils;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.foodnow.BuildConfig;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {

    private static final String CLOUD_NAME = BuildConfig.CLOUDINARY_CLOUD_NAME;
    private static final String UPLOAD_PRESET = BuildConfig.CLOUDINARY_UPLOAD_PRESET;
    private static final String ROOT_FOLDER = "foodnow";
    public static final String FOLDER_PROFILES = "profiles";
    public static final String FOLDER_STORES = "stores";
    public static final String FOLDER_FOODS = "foods";
    private static boolean isInitialized = false;

    // ① Init — chỉ gọi 1 lần khi app khởi động
    public static void init(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context không hợp lệ");
        }
        if (isInitialized) return;
        validateConfig();

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", CLOUD_NAME.trim());
        // KHÔNG để api_key và api_secret ở đây — unsigned upload không cần
        MediaManager.init(context.getApplicationContext(), config);
        isInitialized = true;
    }

    // ② Upload ảnh từ Uri — dùng cho Profile, Store, Food
    public static void uploadImage(Context context, Uri imageUri,
                                   String folder, OnUploadCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback không hợp lệ");
        }
        if (context == null) {
            callback.onError("Context không hợp lệ");
            return;
        }
        if (imageUri == null) {
            callback.onError("Ảnh không hợp lệ");
            return;
        }
        if (isBlank(folder)) {
            callback.onError("Folder upload không hợp lệ");
            return;
        }
        if (!isInitialized) {
            init(context.getApplicationContext());
        }

        String targetFolder = ROOT_FOLDER + "/" + folder.trim();

        MediaManager.get()
                .upload(imageUri)
                .unsigned(UPLOAD_PRESET)
                .option("folder", targetFolder)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        callback.onStart();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Object secureUrl = resultData.get("secure_url");
                        if (secureUrl instanceof String && !((String) secureUrl).trim().isEmpty()) {
                            callback.onSuccess((String) secureUrl);
                            return;
                        }
                        callback.onError("Cloudinary không trả về secure_url");
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        if (error != null && error.getDescription() != null && !error.getDescription().trim().isEmpty()) {
                            callback.onError("[" + error.getCode() + "] " + error.getDescription());
                            return;
                        }
                        callback.onError("Upload ảnh thất bại");
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch(context);
    }

    private static void validateConfig() {
        if (isBlank(CLOUD_NAME) || isBlank(UPLOAD_PRESET)) {
            throw new IllegalStateException("Thiếu cấu hình Cloudinary. Kiểm tra CLOUDINARY_CLOUD_NAME và CLOUDINARY_UPLOAD_PRESET.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // ③ Interface callback dùng chung cho mọi màn hình
    public interface OnUploadCallback {
        void onStart();
        void onSuccess(String secureUrl);
        void onError(String errorMessage);
    }
}
