package com.example.myapplication;
import static android.content.ContentValues.TAG;
import static com.example.myapplication.UtilityFunctions.getBitmap;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.os.Bundle;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
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
import android.widget.ProgressBar;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class MainActivity extends AppCompatActivity {

    //Code To Pick Images
    int PICK_IMAGE_MULTIPLE = 1;


    //int to Store No of Images
    int cout;
    //Array to Store Images
    ArrayList<Uri> mArrayUri;
    int DailyUses;
    Boolean Premium;
    //UI Elements
    Button BSelectImage;
    TextView NoSelected;
    Button CropImageButton;
    ProgressBar progressbar;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    InterstitialAd mInterstitialAd;

    AdView mAdView;
    AdRequest adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        //Initialization
        setContentView(R.layout.activity_main);
        mArrayUri = new ArrayList<Uri>();
        BSelectImage = findViewById(R.id.BSelectImage);
        CropImageButton = findViewById(R.id.CropButton);
        NoSelected = findViewById(R.id.NoText);
        progressbar = findViewById(R.id.progressBar);
        pref = getSharedPreferences("com.android.app.users", Context.MODE_PRIVATE);
        editor = pref.edit();
        Premium = pref.getBoolean("Premium", false);
        CheckDay();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        adRequest = new AdRequest.Builder().build();
        if (!Premium) {
            mAdView = findViewById(R.id.adView);

            mAdView.loadAd(adRequest);
        }
        InterstitAd();

        if (!Premium) {
            DailyUses = pref.getInt("DailyUses", 2);
        }


        progressbar.setVisibility(View.INVISIBLE);


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
        mArrayUri.clear();
        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        i.setAction(Intent.ACTION_GET_CONTENT);


        startActivityForResult(Intent.createChooser(i, "Select Picture"), PICK_IMAGE_MULTIPLE);
        CheckDay();
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
                if(!Premium && (cout > 100))
                {
                    Toast.makeText(getApplicationContext(),"100 image limit for free users exceeded",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    for (int i = 0; i < cout; i++)
                    {
                        Uri imageurl = data.getClipData().getItemAt(i).getUri();
                        mArrayUri.add(imageurl);
                    }
                }
            }
            catch (Exception e)
            {
                cout = 1;
                Uri imageurl = data.getData();
                mArrayUri.add(imageurl);
            }
            NoSelected.setText(cout + " images selected");


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


    private void CropImage(ArrayList ImageList, int cout)
    {
        InterstitAd();
        if(DailyUses != 0)
        {
            if (!Python.isStarted()) {
                Python.start(new AndroidPlatform(this));


            }
            Python py = Python.getInstance();
            PyObject pyobj = py.getModule("ScreenShotCropper");
            progressbar.setVisibility(View.VISIBLE);
            progressbar.setMax(cout);

            for (int i = 0; i < cout; i++)
            {

                progressbar.setProgress(i);
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
            progressbar.setProgress(0);
            NoSelected.setText("");
            mArrayUri.clear();
            Toast.makeText(getApplicationContext(),"Cropping Complete",Toast.LENGTH_SHORT).show();
            progressbar.setVisibility(View.INVISIBLE);
            DailyUses = DailyUses - 1;
            editor.putInt("DailyUses", DailyUses);
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Daily Limit exceeded, Wait a day or buy premium",Toast.LENGTH_SHORT).show();
        }

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

    private void CheckDay()
    {
        Calendar c = Calendar.getInstance();
        int currentTimeSeconds = c.get(Calendar.SECOND);
        int secondsPreviousDay = pref.getInt("seconds", 0);
        if (secondsPreviousDay != 0){ //means there was an earlier set value from previously entering the activity
            //compare if more than 3600*24 = 86400 (1 day in seconds) had passed
            if (currentTimeSeconds - secondsPreviousDay > 86400){
                editor.putInt("DailyUses", 2);
            }
        }
        else {
            pref.edit().putInt("seconds", currentTimeSeconds).apply();
        }
    }

    private void InterstitAd()
    {
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/3347511713", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });

        try {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                    // Called when a click is recorded for an ad.
                    Log.d(TAG, "Ad was clicked.");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    Log.d(TAG, "Ad dismissed fullscreen content.");
                    mInterstitialAd = null;
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when ad fails to show.
                    Log.e(TAG, "Ad failed to show fullscreen content.");
                    mInterstitialAd = null;
                }

                @Override
                public void onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    Log.d(TAG, "Ad recorded an impression.");
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    Log.d(TAG, "Ad showed fullscreen content.");
                }
            });
        }
        catch (Exception e) {
        }
        if (mInterstitialAd != null) {
            mInterstitialAd.show(MainActivity.this);
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }

    }
}


