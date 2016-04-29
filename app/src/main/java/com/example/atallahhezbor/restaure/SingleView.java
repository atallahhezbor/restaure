package com.example.atallahhezbor.restaure;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class SingleView extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String mCurrentPhotoPath = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        final String name = (String) getIntent().getStringExtra("Restaurant");
        TextView title = (TextView)findViewById(R.id.restaurantName);
        title.setText(name);

        Button pictureButton = (Button) findViewById(R.id.pictureButton);
        pictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveToDB(name);
                // Go back to main activity
                SingleView.this.finish();
            }
        });

        // Check if there is saved data for this restaurant
        ArrayList<String> savedData = readFromDB(name);
        if (savedData.size() > 0) {
            // Set the description
            EditText editText = (EditText)findViewById(R.id.editText);
            String savedDesc = savedData.get(3);
            editText.setText(savedDesc);

            // Set the image file if saved
            String filePath = savedData.get(2);
            ImageView imageView = (ImageView)findViewById(R.id.imageView);
            if (filePath != null)
                setPic(imageView, filePath, 1);
        }
    }

    public ArrayList<String> readFromDB(String name) {
        // Gets the data repository in read mode
        DatabaseHelper mDbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //  Create a new array of values, where column names are the keys
        //  for use in the where clause
        String[] values = {
                name
        };
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                "ID",
                "name",
                "filename",
                "description"
        };



        Cursor cursor = db.query(
                "restaurants",  // The table to query
                projection,                               // The columns to return
                "name=?",                                // The columns for the WHERE clause
                values,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );


        ArrayList<String> result = new ArrayList<>();

        // Construct the result if found
        if (cursor.moveToFirst()) {
            String[] columnNames = cursor.getColumnNames();
            for (String column : columnNames) {
                String value = cursor.getString(
                        cursor.getColumnIndexOrThrow(column)
                );
                result.add(value);
            }
        }

        return result;
    }



    public void saveToDB(String name) {
        // Gets the data repository in write mode
        DatabaseHelper mDbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        EditText editText = (EditText)findViewById(R.id.editText);
        String description = editText.getText().toString();
        values.put("name", name);
        if (mCurrentPhotoPath.length() > 0) {
            List<String> path = Arrays.asList(mCurrentPhotoPath.split("/"));
            String filename = "sdcard/Pictures/" + path.get(path.size() - 1);
            values.put("filename", filename);
        }
            values.put("description", description);

        // Check if the restaurant already exists
        String[] whereArgs = {name};
        String[] columns = {"ID"};
        Cursor c = db.query("restaurants",columns,"name = ?", whereArgs,null, null,null);
        // if so update else insert
        if (c.moveToFirst()) {
            String[] updateWhereArgs = { c.getString(0) };
            db.update("restaurants", values, "ID = ?",updateWhereArgs);
        } else {
            // Insert the new row
            db.insert(
                    "restaurants",
                    null,
                    values);

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = {
                    "ID",
                    "name",
                    "filename",
                    "description"
            };

            // How you want the results sorted in the resulting Cursor
            String sortOrder =
                    "ID" + " DESC";

            Cursor cursor = db.query(
                    "restaurants",  // The table to query
                    projection,                               // The columns to return
                    null,                                // The columns for the WHERE clause
                    null,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    null                                 // The sort order
            );

            //cursor.moveToFirst();
            while (cursor.moveToNext()) {
                String currID = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                );
                Log.i("DBData", currID);
            }
        }
    }

    private void dispatchTakePictureIntent() {


        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i("io", ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.i("uri", Uri.fromFile(photoFile).toString());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            } else {
                Log.i("CAMERA", "null_photo");
            }
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        // Open a file with that name in external storage
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Add the picture to the gallery
            galleryAddPic();
            // Call setPic() to generate the thumbnail
            ImageView mImageView = (ImageView)findViewById(R.id.imageView);
            List<String> path = Arrays.asList(mCurrentPhotoPath.split("/"));
            String filename = "sdcard/Pictures/" + path.get(path.size()-1);
            setPic(mImageView, filename, 1);

        }
    }

    public static int setPic(ImageView mImageView, String filepath, int scale) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        // Get the dimensions of the View
        // TODO: use this to scale the image appropriately
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        // Get the dimensions of the bitmap
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, bmOptions);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scale;

        Bitmap bitmap = BitmapFactory.decodeFile(filepath, bmOptions);
        mImageView.setImageBitmap(bitmap);
        if (bitmap != null) {
            return bitmap.getWidth();
        } else {
            return 0;
        }
    }

}
