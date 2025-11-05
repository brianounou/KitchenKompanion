package com.kitchenkompanion.features.barcode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.kitchenkompanion.R;
import com.kitchenkompanion.databinding.ActivityBarcodeScannerBinding;
import com.kitchenkompanion.features.pantry.AddEditItemActivity;
import com.kitchenkompanion.utils.PermissionUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for scanning barcodes using CameraX and ML Kit.
 * 
 * Features:
 * - Real-time barcode detection
 * - Supports UPC-A, UPC-E, EAN-8, EAN-13
 * - Auto-launches product lookup on detection
 * - Camera permission handling
 * - Flashlight toggle
 * 
 * Usage:
 * startActivityForResult(new Intent(context, BarcodeScannerActivity.class), REQUEST_CODE);
 */
public class BarcodeScannerActivity extends AppCompatActivity {
    
    private static final String TAG = "BarcodeScanner";
    public static final String EXTRA_BARCODE = "barcode";
    
    private ActivityBarcodeScannerBinding binding;
    private ExecutorService cameraExecutor;
    private BarcodeScanner scanner;
    private Camera camera;
    private boolean isScanning = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBarcodeScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.scan_barcode);
        
        cameraExecutor = Executors.newSingleThreadExecutor();
        
        // Initialize ML Kit barcode scanner
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39
                )
                .build();
        scanner = BarcodeScanning.getClient(options);
        
        setupUI();
        
        if (PermissionUtils.hasCameraPermission(this)) {
            startCamera();
        } else {
            PermissionUtils.requestCameraPermission(this);
        }
    }
    
    private void setupUI() {
        binding.flashlightButton.setOnClickListener(v -> toggleFlashlight());
        binding.instructionsText.setText(R.string.barcode_instructions);
    }
    
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
                ProcessCameraProvider.getInstance(this);
        
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    
    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        // Preview
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
        
        // Image analysis for barcode scanning
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        
        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            if (!isScanning) {
                imageProxy.close();
                return;
            }
            
            @androidx.camera.core.ExperimentalGetImage
            android.media.Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.getImageInfo().getRotationDegrees()
                );
                
                scanner.process(image)
                        .addOnSuccessListener(barcodes -> {
                            for (Barcode barcode : barcodes) {
                                if (barcode.getRawValue() != null) {
                                    onBarcodeDetected(barcode.getRawValue());
                                    break;
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Barcode scanning failed", e);
                        })
                        .addOnCompleteListener(task -> imageProxy.close());
            } else {
                imageProxy.close();
            }
        });
        
        // Camera selector
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        
        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis
            );
            
            // Enable flashlight button if camera has flash
            if (camera.getCameraInfo().hasFlashUnit()) {
                binding.flashlightButton.setVisibility(View.VISIBLE);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
            Toast.makeText(this, "Failed to bind camera", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void onBarcodeDetected(String barcode) {
        if (!isScanning) return;
        
        isScanning = false;
        runOnUiThread(() -> {
            binding.instructionsText.setText(String.format(getString(R.string.barcode_found), barcode));
            binding.progressBar.setVisibility(View.VISIBLE);
        });
        
        // Lookup product in OpenFoodFacts
        UpcLookupService.lookupProduct(barcode, new UpcLookupService.LookupCallback() {
            @Override
            public void onSuccess(ProductInfo product) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(BarcodeScannerActivity.this, 
                            R.string.product_found, Toast.LENGTH_SHORT).show();
                    openAddItemWithProduct(barcode, product);
                });
            }
            
            @Override
            public void onNotFound() {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(BarcodeScannerActivity.this, 
                            R.string.product_not_found, Toast.LENGTH_SHORT).show();
                    openAddItemWithBarcode(barcode);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(BarcodeScannerActivity.this, 
                            "Lookup failed: " + error, Toast.LENGTH_SHORT).show();
                    openAddItemWithBarcode(barcode);
                });
            }
        });
    }
    
    private void openAddItemWithProduct(String barcode, ProductInfo product) {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        intent.putExtra(AddEditItemActivity.EXTRA_BARCODE, barcode);
        intent.putExtra(AddEditItemActivity.EXTRA_PRODUCT_NAME, product.name);
        if (product.nutritionJson != null) {
            intent.putExtra(AddEditItemActivity.EXTRA_NUTRITION_JSON, product.nutritionJson);
        }
        startActivity(intent);
        finish();
    }
    
    private void openAddItemWithBarcode(String barcode) {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        intent.putExtra(AddEditItemActivity.EXTRA_BARCODE, barcode);
        startActivity(intent);
        finish();
    }
    
    private void toggleFlashlight() {
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            boolean currentState = camera.getCameraInfo().getTorchState().getValue() == 1;
            camera.getCameraControl().enableTorch(!currentState);
            
            // Update button icon
            binding.flashlightButton.setImageResource(
                    currentState ? R.drawable.ic_flashlight_off : R.drawable.ic_flashlight_on
            );
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PermissionUtils.REQUEST_CAMERA_PERMISSION) {
            if (PermissionUtils.isPermissionGranted(grantResults)) {
                startCamera();
            } else {
                Toast.makeText(this, R.string.error_permission_camera, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        scanner.close();
        binding = null;
    }
}






