package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import  android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;


public class MainActivity extends AppCompatActivity {

    // One Button
    int PICK_IMAGE_MULTIPLE = 1;
    Button BSelectImage;


    // One Preview Image
    ImageView IVPreviewImage;
    PyObject Module;
    PyObject Cropper;
    String str;
    PyObject pyobj;
    // constant to compare
    // the activity result code
    int SELECT_PICTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String str = "";
        PyObject x;
        setContentView(R.layout.activity_main);

        mArrayUri = new ArrayList<Uri>();
        BSelectImage = findViewById(R.id.BSelectImage);
        IVPreviewImage = findViewById(R.id.IVPreviewImage);
        BSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });
    }

    // this function is triggered when
    // the Select Image Button is clicked
    void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        i.setAction(Intent.ACTION_GET_CONTENT);


        startActivityForResult(Intent.createChooser(i, "Select Picture"), PICK_IMAGE_MULTIPLE);
    }

    // this function is triggered when user
    // selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && null != data) {
            if(!Python.isStarted())
            {
                Python.start(new AndroidPlatform(this));



            }
            Python py = Python.getInstance();
            PyObject pyobj = py.getModule("ScreenShotCropper");


            ClipData mClipData = data.getClipData();
            int cout = data.getClipData().getItemCount();
            for (int i = 0; i < cout; i++) {
                Uri imageurl = data.getClipData().getItemAt(i).getUri();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageurl);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String imgString = getStringImage(bitmap);
                Context context = this.getApplicationContext();
                Resources res = context.getResources();
                int Height = bitmap.getHeight();
                Bitmap bp;
                Bitmap Resized;
                String RefString = null;
                switch (Height)
                {
                    case 1520:
                        bp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ref_1520);
                        System.out.println(bp.getWidth());
                        Bitmap resized = Bitmap.createScaledBitmap(bp, 43, 44, true);
                        RefString = getStringImage(resized);

                        break;
                    case 1920:
                        bp = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.ref_1080);
                        resized = Bitmap.createScaledBitmap(bp, 82, 81, true);
                        RefString = getStringImage(resized);
                        break;


                    case 1280:
                        bp = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.ref_1280);
                        resized = Bitmap.createScaledBitmap(bp, 35, 35, true);
                        RefString = getStringImage(resized);
                        break;
                    case 1640:
                        bp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ref_1520);
                        System.out.println(bp.getWidth());
                        resized = Bitmap.createScaledBitmap(bp, 43, 44, true);
                        RefString = getStringImage(resized);
                        break;
                    default:
                        RefString = "0";
                        break;
                }
                File file = new File(imageurl.getPath());//create path from uri
                String[] split = file.getPath().split(":");//split the path.
                String filePath = split[0] + "/" + getFileName(imageurl);
                PyObject obj = pyobj.callAttr("crop", imgString, RefString, filePath);

                Bitmap Cropped = getBitmap(obj.toString());
                //IVPreviewImage.setImageBitmap(Cropped);
                SaveImage(Cropped);
                mArrayUri.add(imageurl);
            }



        }
    }
    @SuppressLint("Range")
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    private String getStringImage(@org.jetbrains.annotations.NotNull Bitmap bitmap){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte [] imageBytes=baos.toByteArray();

        String encodedImage=android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);

        return encodedImage;

    }

    private Bitmap getBitmap(String ImageString)
    {
        byte[] imageBytes = android.util.Base64.decode(ImageString, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
        return bmp;

    }

    private File createImageFile() throws IOException {
        String currentPhotoPath;
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();

        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            // sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
            //     Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
// Tell the media scanner about the new file so that it is
// immediately available to the user.
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }


    ArrayList<Uri> mArrayUri;
}