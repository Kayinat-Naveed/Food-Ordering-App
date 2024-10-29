package com.example.recipeapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class select extends AppCompatActivity {

    Button search, ingredients , time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        search = findViewById(R.id.search);
        ingredients = findViewById(R.id.ingredients);
        time = findViewById(R.id.time);


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent keyword = new Intent(select.this,search.class);
                startActivity(keyword);
            }
        });

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent keyword = new Intent(select.this,searchByTime.class);
                startActivity(keyword);
            }
        });
    }

}
