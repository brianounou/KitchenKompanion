package com.kitchenkompanion.features.barcode;

/**
 * Simple product information from barcode lookup.
 */
public class ProductInfo {
    public String barcode;
    public String name;
    public String brand;
    public String quantity;
    public String nutritionJson;
    public String imageUrl;
    
    public ProductInfo() {
    }
    
    public ProductInfo(String barcode, String name) {
        this.barcode = barcode;
        this.name = name;
    }
}






