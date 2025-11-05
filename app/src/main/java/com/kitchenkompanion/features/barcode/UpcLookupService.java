package com.kitchenkompanion.features.barcode;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Service for looking up product information by barcode using OpenFoodFacts API.
 * 
 * OpenFoodFacts is a free, open database of food products from around the world.
 * API Documentation: https://wiki.openfoodfacts.org/API
 * 
 * No API key required!
 * 
 * Usage:
 * UpcLookupService.lookupProduct("0123456789", new LookupCallback() {
 *     public void onSuccess(ProductInfo product) { ... }
 *     public void onNotFound() { ... }
 *     public void onError(String error) { ... }
 * });
 */
public class UpcLookupService {
    
    private static final String TAG = "UpcLookupService";
    private static final String BASE_URL = "https://world.openfoodfacts.org/api/v0/product/";
    private static final Gson gson = new Gson();
    
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();
    
    public interface LookupCallback {
        void onSuccess(ProductInfo product);
        void onNotFound();
        void onError(String error);
    }
    
    /**
     * Lookup product by barcode (UPC, EAN, etc.)
     * 
     * @param barcode The barcode to lookup
     * @param callback Callback for results
     */
    public static void lookupProduct(String barcode, LookupCallback callback) {
        String url = BASE_URL + barcode + ".json";
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "KitchenKompanion - Android")
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network request failed", e);
                callback.onError("Network error: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("HTTP " + response.code());
                    return;
                }
                
                try {
                    String jsonStr = response.body().string();
                    JsonObject json = gson.fromJson(jsonStr, JsonObject.class);
                    
                    int status = json.get("status").getAsInt();
                    
                    if (status == 0) {
                        // Product not found
                        callback.onNotFound();
                        return;
                    }
                    
                    // Extract product info
                    JsonObject productJson = json.getAsJsonObject("product");
                    ProductInfo product = new ProductInfo();
                    product.barcode = barcode;
                    
                    // Product name
                    if (productJson.has("product_name") && !productJson.get("product_name").isJsonNull()) {
                        product.name = productJson.get("product_name").getAsString();
                    }
                    
                    // Brand
                    if (productJson.has("brands") && !productJson.get("brands").isJsonNull()) {
                        product.brand = productJson.get("brands").getAsString();
                    }
                    
                    // Quantity
                    if (productJson.has("quantity") && !productJson.get("quantity").isJsonNull()) {
                        product.quantity = productJson.get("quantity").getAsString();
                    }
                    
                    // Image URL
                    if (productJson.has("image_url") && !productJson.get("image_url").isJsonNull()) {
                        product.imageUrl = productJson.get("image_url").getAsString();
                    }
                    
                    // Nutrition data
                    if (productJson.has("nutriments") && !productJson.get("nutriments").isJsonNull()) {
                        JsonObject nutriments = productJson.getAsJsonObject("nutriments");
                        product.nutritionJson = gson.toJson(nutriments);
                    }
                    
                    // Use brand + name if available
                    if (product.name == null || product.name.isEmpty()) {
                        if (product.brand != null && !product.brand.isEmpty()) {
                            product.name = product.brand;
                        } else {
                            product.name = "Product " + barcode;
                        }
                    } else if (product.brand != null && !product.brand.isEmpty()) {
                        product.name = product.brand + " " + product.name;
                    }
                    
                    callback.onSuccess(product);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                    callback.onError("Parse error: " + e.getMessage());
                }
            }
        });
    }
}






