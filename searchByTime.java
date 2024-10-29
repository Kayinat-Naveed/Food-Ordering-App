package com.example.recipeapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.recipeapp.Model.recipe;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.jaredrummler.materialspinner.MaterialSpinner;

public class searchByTime extends AppCompatActivity {

    MaterialSpinner spinner;
    ListView listView;

    DatabaseReference reference;
    FirebaseListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_by_time);

        listView = findViewById(R.id.listview);
        reference = FirebaseDatabase.getInstance().getReference();
        spinner = findViewById(R.id.spinner);
        spinner.setHint("Select Time");
        spinner.setItems("30 minutes","55 minutes","1 hour","1 hour 10 mint","1 hour 20 mint","1 hour 30 mint","1 hour 40 mint");


        loadFood();


    }

    private void loadFood() {

        Query query = FirebaseDatabase.getInstance().getReference("Recipe");
        FirebaseListOptions<recipe> options = new FirebaseListOptions.Builder<recipe>()
                .setLayout(R.layout.menu)
                .setQuery(query, recipe.class).build();
        adapter = new FirebaseListAdapter(options) {
            @Override
            protected void populateView(View v, Object model, final int position) {

                TextView foodname = v.findViewById(R.id.foodname);
                ImageView foodimage = v.findViewById(R.id.foodimage);
                final recipe foodItems = (recipe) model;

                String name = this.getRef(position).getKey();
                foodname.setText(name);
                //Picasso.with(search.this).load(foodItems.getImage().toString()).into(foodimage);

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Intent intent = new Intent(menuList.this, Details.class);
//                        intent.putExtra("foodId", adapter.getRef(position).getKey());
//                        startActivity(intent);
                    }
                });
            }
        };
        listView.setAdapter(adapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.startListening();

    }


}
