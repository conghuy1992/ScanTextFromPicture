package notes.conghuy.com.scantextfrompicture;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    ImageView iv, iv_2;
    String TAG = "MainActivity";
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tv = (TextView) findViewById(R.id.tv);
        iv = (ImageView) findViewById(R.id.iv);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.img_1);
//        iv.setImageBitmap(bitmap);

        sendTest(bitmap);
    }

    private void sendTest(final Bitmap bitmap) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                Frame imageFrame = new Frame.Builder()
                        .setBitmap(bitmap)                 // your image bitmap
                        .build();

                String imageText = "";
                SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
                if (textBlocks == null) Log.d(TAG, "textBlocks null");
                else Log.d(TAG, "textBlocks:" + textBlocks.size());
                for (int i = 0; i < textBlocks.size(); i++) {
                    TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                    imageText += textBlock.getValue() + "\n";                   // return string
                    Log.d(TAG, "imageText:" + imageText);
                }
                handler.obtainMessage(MSG_UPDATE_DISPLAY, imageText).sendToTarget();
            }
        }).start();
    }

    private final static int MSG_UPDATE_DISPLAY = 2;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_UPDATE_DISPLAY:
                    // TODO
                    final String text = (String) msg.obj; // get contents
                    tv.setText(text);
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    private final int MY_PERMISSIONS_REQUEST_CODE = 1;

    public boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public void setPermissions() {
        String[] requestPermission = new String[]{
                android.Manifest.permission.READ_EXTERNAL_STORAGE
        };
        ActivityCompat.requestPermissions(this, requestPermission, MY_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != MY_PERMISSIONS_REQUEST_CODE) {
            return;
        }
        boolean isGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
                break;
            }
        }
        if (isGranted) {
            choosePicture();
        } else {

        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
//            takePicture();
            if (checkPermissions()) choosePicture();
            else setPermissions();
        }

        return super.onOptionsItemSelected(item);
    }

    private final int PICK_IMAGE_REQUEST = 2;

    public void choosePicture() {
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_IMAGE_REQUEST);
    }

    public String dateToString(Date date, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }

    public static int REQUEST_IMAGE = 3;
    File destination;

    void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                intent.putExtra("android.intent.extras.CAMERA_FACING", 0);
        String name = dateToString(new Date(), "yyyy-MM-dd-hh-mm-ss");
        destination = new File(Environment.getExternalStorageDirectory(), name + ".jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    public Bitmap bitMapRotate(int exifToDegrees, Bitmap bmPhotoResult) {
        //Create object of new Matrix.
        Matrix matrix = new Matrix();
        //set image rotation value to 90 degrees in matrix.
        matrix.postRotate(exifToDegrees);
//        matrix.postScale(0.5f, 0.5f);
        int newWidth = bmPhotoResult.getWidth();
        int newHeight = bmPhotoResult.getHeight();
        //Create bitmap with new values.
        Bitmap bMapRotate = Bitmap.createBitmap(bmPhotoResult, 0, 0, newWidth, newHeight, matrix, true);
        return bMapRotate;
    }

    public static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    public Bitmap createScaleBitmap(String pathFile, int reqSize) {
        try {
            // Get the dimensions of the View
            int targetW = reqSize;
            int targetH = reqSize;

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pathFile, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            Bitmap bmResult = BitmapFactory.decodeFile(pathFile, bmOptions);
            ExifInterface exifReader = new ExifInterface(pathFile);
            int exifOrientation = exifReader.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            int exifToDegrees = exifToDegrees(exifOrientation);

            return bitMapRotate(exifToDegrees, bmResult);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
//                FileInputStream in = new FileInputStream(destination);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 10;
                String imagePath = destination.getAbsolutePath();
//                Bitmap bmp = BitmapFactory.decodeStream(in, null, options);
                Bitmap bmp = createScaleBitmap(imagePath, 100);
                iv.setImageBitmap(bmp);
                sendTest(bmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            Log.d(TAG, "uri:" + uri);
            String path = getRealPathFromURI(this, uri);
            Log.d(TAG, "path:" + path);
            if (path != null && path.length() > 0 && !path.startsWith("http")) {

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    sendTest(bitmap);
                    iv.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
//                Const.showMsg(getActivity(), R.string.can_not_get_path_picture);
            }
        } else {
            Log.e(TAG, "Request cancelled");
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
