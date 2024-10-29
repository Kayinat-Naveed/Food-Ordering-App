package com.example.recipeapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.recipeapp.Model.recipe;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class search extends AppCompatActivity {

    ListView listView;

    DatabaseReference reference;
    FirebaseListAdapter adapter;

    //search functionality
    FirebaseListAdapter searchAdapter;

    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        listView = findViewById(R.id.listview);
        reference = FirebaseDatabase.getInstance().getReference();
        loadListFood();

        //Search

        materialSearchBar = (MaterialSearchBar) findViewById(R.id.search_bar);
        materialSearchBar.setHint("Enter your food");

        loadSuggest();
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //when user type their tex we will change suggest list
                List<String> suggest = new ArrayList<String>();
                Log.d("myTag", "Txt Change");
                for (String search : suggestList) {
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //When search bar is closed restore original adapter

                if (!enabled) {
                    Log.d("myTag", "search action");
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

                //When search finished
                //show results od search adapter
                Log.d("myTag", "search");
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
    }

    private void startSearch(CharSequence text) {
        {
            Log.d("myTag", "start search");
            Query query = FirebaseDatabase.getInstance().getReference().child("Recipe").orderByChild("Uid").equalTo(text.toString());
                    //.equalTo(text.toString());
            FirebaseListOptions<recipe> listOptions = new FirebaseListOptions.Builder<recipe>()
                    .setLayout(R.layout.menu)
                    .setQuery(query, recipe.class).build();
            Log.d("myTag", "adapter");
            searchAdapter = new FirebaseListAdapter(listOptions) {
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
            listView.setAdapter(searchAdapter);

        }
        searchAdapter.startListening();

    }

    private void loadSuggest() {

        reference.child("Recipe")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//
//                            recipe item = postSnapshot.getValue(recipe.class);
//                            suggestList.add(item.getName());  //Add name of food to suggest list
                            suggestList.add(postSnapshot.getKey().toString());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    private void loadListFood() {

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
