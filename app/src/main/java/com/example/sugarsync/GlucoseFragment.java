package com.example.sugarsync;
import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.ImageCaptureException;
import android.content.ContentValues;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageCaptureException;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import android.widget.ImageView;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import android.hardware.camera2.CameraCharacteristics;
import androidx.lifecycle.LifecycleOwner;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.util.Size;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Tesseract;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class GlucoseFragment extends Fragment {

    private PreviewView previewView;
    private Button bCapture;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;

    private ImageView capturedImageView;
    private TextView extractedTextView;

    private Camera camera;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private Size desiredResolution = new Size(1920, 1080);

    private Tesseract tess;

    private boolean isPreviewOpen = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_glucose, container, false);

        previewView = view.findViewById(R.id.previewView);
        bCapture = view.findViewById(R.id.bCapture);

        capturedImageView = view.findViewById(R.id.capturedImageView);

        bCapture.setOnClickListener(v -> {
            if (!isPreviewOpen) {
                // Open the preview when the button is clicked for the first time
                checkCameraPermission();
                previewView.setVisibility(View.VISIBLE);
                capturedImageView.setVisibility(View.GONE);
                isPreviewOpen = true;
            } else {
                // Capture the photo when the button is clicked again
                capturePhoto();
            }
        });
        initTess4J();
        extractedTextView = view.findViewById(R.id.extractedTextView);
        return view;
    }

    private void  initTess4J() {
        tess = Tesseract.getInstance();
        tess.setDatapath(requireContext().getFilesDir().getPath() + "/tessdata");
        tess.setLanguage("eng");
    }

    private void extractTextFromImage(Bitmap bitmap) {
        String extractedText = "";
        try {
            extractedText = tess.doOCR(bitmap);
        } catch (TesseractException e) {
            e.printStackTrace();
        }

        // Display the extracted text in the TextView
        extractedTextView.setText("Extracted Text: " + extractedText);
        extractedTextView.setVisibility(View.VISIBLE);

        // Add the extracted text to Firebase Realtime Database
        //addToFirebase(extractedText);

        // Show a Toast (optional)
        Toast.makeText(requireContext(), "Extracted Text: " + extractedText, Toast.LENGTH_LONG).show();
    }


    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            requestCameraPermission();
        } else {
            // Permission has already been granted
            // Continue with your camera setup
            initializeCamera();
        }
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE
        );
    }

    private void initializeCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Configure the Preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Configure the ImageCapture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        // Set up the camera with the configured use cases
        try {
            cameraProvider.unbindAll();
            Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);

            // Introduce a delay before capturing the photo (optional)
            //new Handler().postDelayed(() -> capturePhoto(), 1000); // 1000 milliseconds delay
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    private void capturePhoto() {
        //long timestamp = System.currentTimeMillis();

        //ContentValues contentValues = new ContentValues();
        // contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
        //contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        if (imageCapture != null) {
            // Define file metadata
            long timestamp = System.currentTimeMillis();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

            // Capture photo
            imageCapture.takePicture(
                    new ImageCapture.OutputFileOptions.Builder(
                            requireContext().getContentResolver(),
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                    ).build(),
                    ContextCompat.getMainExecutor(requireContext()),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            Uri savedUri = outputFileResults.getSavedUri();
                            if (savedUri != null) {
                                // Load and display the image using the savedUri
                                capturedImageView.setImageURI(savedUri);
                                capturedImageView.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(requireContext(), "Photo has been saved successfully.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Toast.makeText(requireContext(), "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        } else {
            Toast.makeText(requireContext(), "Error taking photo: ImageCapture is null", Toast.LENGTH_SHORT).show();
        }
    }




}



