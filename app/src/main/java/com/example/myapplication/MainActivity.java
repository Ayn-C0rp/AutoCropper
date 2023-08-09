package com.example.myapplication;
import static com.example.myapplication.UtilityFunctions.getBitmap;

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
import android.provider.ContactsContract;
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
import android.widget.TextView;
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

    //Code To Pick Images
    int PICK_IMAGE_MULTIPLE = 1;


    //int to Store No of Images
    int cout;
    //Array to Store Images
    ArrayList<Uri> mArrayUri;

    //UI Elements
    Button BSelectImage;
    TextView NoSelected;
    Button CropImageButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialization
        setContentView(R.layout.activity_main);
        mArrayUri = new ArrayList<Uri>();
        BSelectImage = findViewById(R.id.BSelectImage);
        CropImageButton = findViewById(R.id.CropButton);
        NoSelected = findViewById(R.id.NoText);
        BSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });
        CropImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){CropImage(mArrayUri, cout);}
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
    @SuppressLint("SetTextI18n")
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && null != data)
        {

            try{
                cout = data.getClipData().getItemCount();
            }
            catch (Exception e)
            {
                cout = 1;
            }
            NoSelected.setText(cout + " images selected");
            for (int i = 0; i < cout; i++)
            {
                Uri imageurl = data.getClipData().getItemAt(i).getUri();
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


    private void CropImage(ArrayList ImageList, int cout) {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));


        }
        Python py = Python.getInstance();
        PyObject pyobj = py.getModule("ScreenShotCropper");


        for (int i = 0; i < cout; i++)
        {

            Uri imageurl = (Uri) ImageList.get(i);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageurl);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String imgString = UtilityFunctions.getStringImage(bitmap);
            Context context = this.getApplicationContext();
            Resources res = context.getResources();
            int Height = bitmap.getHeight();
            Bitmap bp;
            Bitmap Resized;
            String RefString = null;

            File file = new File(imageurl.getPath());//create path from uri
            String[] split = file.getPath().split(":");//split the path.
            String filePath = split[0] + "/" + getFileName(imageurl);
            PyObject obj = null;
            Bitmap Cropped = null;

            switch (Height) {
                case 1520:
                    bp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ref_1520);
                    System.out.println(bp.getWidth());
                    Bitmap resized = Bitmap.createScaledBitmap(bp, 43, 44, true);
                    RefString = UtilityFunctions.getStringImage(resized);
                    obj = pyobj.callAttr("crop", imgString, RefString, filePath);
                    Cropped = UtilityFunctions.getBitmap(obj.toString());
                    SaveImage(Cropped);

                    break;
                case 1920:
                    bp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ref_1080);
                    resized = Bitmap.createScaledBitmap(bp, 82, 81, true);
                    RefString = UtilityFunctions.getStringImage(resized);
                    obj = pyobj.callAttr("crop", imgString, RefString, filePath);
                    Cropped = UtilityFunctions.getBitmap(obj.toString());
                    SaveImage(Cropped);

                    break;


                case 1280:
                    bp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ref_1280);
                    resized = Bitmap.createScaledBitmap(bp, 35, 35, true);
                    RefString = UtilityFunctions.getStringImage(resized);
                    obj = pyobj.callAttr("crop", imgString, RefString, filePath);
                    Cropped = UtilityFunctions.getBitmap(obj.toString());
                    SaveImage(Cropped);

                    break;
                case 1640:
                    bp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ref_1520);
                    System.out.println(bp.getWidth());
                    resized = Bitmap.createScaledBitmap(bp, 43, 44, true);
                    RefString = UtilityFunctions.getStringImage(resized);
                    obj = pyobj.callAttr("crop", imgString, RefString, filePath);
                    Cropped = UtilityFunctions.getBitmap(obj.toString());
                    SaveImage(Cropped);

                    break;
                default:
                    Toast.makeText(getApplicationContext(),"Skipping Over Image, Invalid Format/Unsupported Resolution",Toast.LENGTH_SHORT).show();
                    break;
            }



        }

        Toast.makeText(getApplicationContext(),"Cropping Complete",Toast.LENGTH_SHORT).show();
    }

    void SaveImage(Bitmap finalBitmap) {

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
            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
            //Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
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
}


