package com.example.atallahhezbor.restaure;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class Memories extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memories);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ArrayList<Restaurant> restaurants = readFromDB();
        Log.i("restaurants", restaurants.toString());
        GridLayout gridLayout = (GridLayout) findViewById(R.id.gridLayout);
        for (final Restaurant r : restaurants) {
            String filepath = r.getFilepath();
            RelativeLayout linearLayout = new RelativeLayout(this);
            ImageView imageView = new ImageView(this);
            imageView.setPadding(10,10,10,10);
            int imageWidth = SingleView.setPic(imageView, filepath, 8);
            Log.i("width", Integer.toString(imageWidth));
            TextView textView = new TextView(this);
            textView.setText(r.getName());
            textView.setWidth(imageWidth);
            textView.setPadding(10, 10, 10, 10);
            linearLayout.addView(imageView);
            linearLayout.addView(textView);
            gridLayout.addView(linearLayout);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(Memories.this, SingleView.class);
                    intent.putExtra("Restaurant", r.getName());
                    startActivity(intent);
                }
            });

        }
    }

    // Get's all user's records from the database
    public ArrayList<Restaurant> readFromDB() {
        // Gets the data repository in read mode
        DatabaseHelper mDbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //  Create a new array of values, where column names are the keys
        //  for use in the where clause

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                "name",
                "filename",
                "description"
        };



        Cursor cursor = db.query(
                "restaurants",  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );


        ArrayList<Restaurant> result = new ArrayList<>();

        // Construct the result if found
        while (cursor.moveToNext()) {
            ArrayList<String> fields = new ArrayList<>();
            String[] columnNames = cursor.getColumnNames();
            for (String column : columnNames) {
                String value = cursor.getString(
                        cursor.getColumnIndexOrThrow(column)
                );
                fields.add(value);
            }
            result.add(new Restaurant(fields));
        }

        return result;
    }


}
