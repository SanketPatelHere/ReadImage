package com.example.readimage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.skyhope.textrecognizerlibrary.TextScanner;
import com.skyhope.textrecognizerlibrary.callback.TextExtractCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;


public class MainActivity extends AppCompatActivity {
    Button buttonGallery, buttonCamera;
    TextView recognizeText;
    ImageView captureImage;
    String currentPhotoPath;
    Uri photoURI;
    Bitmap bitmap;
    public static final int REQUEST_FOR_IMAGE_FROM_GALLERY = 101;
    public static final int REQUEST_FOR_IMAGE_FROM_CAMERA = 102;
    //private com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay<OcrGraphic> graphicOverlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonGallery = findViewById(R.id.button_gallery);
        buttonCamera = findViewById(R.id.button_camera);
        recognizeText = findViewById(R.id.text);
        captureImage = findViewById(R.id.imageView);

        buttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dexter.withActivity(MainActivity.this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        dispatchTakePictureIntent();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

            }
        });
    }

    private void openGallery()
    {
        Intent pi = new Intent(Intent.ACTION_PICK);
        pi.setType("image/*");
        startActivityForResult(pi, REQUEST_FOR_IMAGE_FROM_GALLERY);
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri mImageFileUri = null;
        Uri uri = null;
        if(requestCode==REQUEST_FOR_IMAGE_FROM_CAMERA)
        {
            try {
                    uri = Uri.parse(currentPhotoPath);
                    ///storage/emulated/0/Android/data/com.example.readimage/files/Pictures/JPEG_20200213_155830_6801677513600166301.jpg
                    Log.i("My inside uri = ",uri+"");
                    bitmap = (Bitmap) data.getExtras().get("data");
                    captureImage.setImageBitmap(bitmap);

                }
                catch (Exception e) {
                    Log.i("My Error = ","imageFile = "+e.getMessage());

                }
        }
        else if(requestCode==REQUEST_FOR_IMAGE_FROM_GALLERY)
        {
            try
            {
                uri = data.getData();
                Log.i("My inside uri = ",uri+"");

                 bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                captureImage.setImageBitmap(bitmap);
            }
            catch (Exception e)
            {
                Log.i("My Error = ","in gallery image load  = "+e.getMessage());
            }
        }

        try
        {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                }
            }, 2000);
            Log.i("My inside uri2 = ",uri+"");



            List<String> result = new ArrayList<>();
            String imageText = "";
            TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
            /////////not use = for read gujarati text = ocr library
            //textRecognizer.setProcessor(new OcrDetectorProcessor(graphicOverlay));
            if (!textRecognizer.isOperational()) {
                Log.i("My TextRecognizer ", "is not operational");
                textRecognizer.release();
                recognizeText.setVisibility(View.VISIBLE);
                recognizeText.setText(imageText+"");
                Log.i("My result = ",result+"");

                //return result;
            }
            Frame imageFrame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
            for (int i = 0; i < textBlocks.size(); i++) {
                TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                imageText = imageText + textBlock.getValue();
                result.add(imageText+"");
            }
            textRecognizer.release();
            //recognizeText.setText(result+"");
            recognizeText.setVisibility(View.VISIBLE);
            if(imageText=="")
            {
                recognizeText.setText("Sorry, Text not capture from Image. Try again!");
            }
            else
            {
                recognizeText.setText(imageText+"");
            }
            Log.i("My result = ",result+"");



           /*TextScanner.getInstance(getApplicationContext()).init().load(uri)
        .getCallback(new TextExtractCallback() {
            @Override
            public void onGetExtractText(List<String> textList) {
                final StringBuilder text = new StringBuilder();
                for(String s : textList)
                {
                    text.append(s).append("\n");
                    Log.i("My text = ",text+"");
                }
                recognizeText.post(new Runnable() {
                    @Override
                    public void run() {
                        recognizeText.setText(text.toString());
                        Log.i("My recognizeText = ",recognizeText+"");

                    }
                });
            }
        });*/


        }
        catch (Exception e)
        {
            Log.i("My Error = ","bitmap = "+e.getMessage());
        }
    }





    private File createImageFile2() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile2();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                 photoURI = FileProvider.getUriForFile(getApplicationContext(),
                        "com.example.readimage",
                        photoFile);
                Log.i("My Uri = ",photoURI+"");
                takePictureIntent.putExtra("data", photoURI);
                takePictureIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                startActivityForResult(takePictureIntent, REQUEST_FOR_IMAGE_FROM_CAMERA);
            }
        }
    }

}
