package com.example.orderfoodprject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orderfoodprject.Common.Common;
import com.example.orderfoodprject.Database.Database;
import com.example.orderfoodprject.Model.foodmenu;
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

public class menuList extends AppCompatActivity {


    String id = "";
    ListView listView;

    DatabaseReference reference;
    FirebaseListAdapter<foodmenu> adapter;

    //search functionality
    FirebaseListAdapter searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //Favorites
    Database localDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_list);

        listView = findViewById(R.id.listview);
        //init Firebase
        reference = FirebaseDatabase.getInstance().getReference();
        //Local DB
        localDb =new Database(this);


        if (getIntent() != null) {
            id = getIntent().getStringExtra("CId");
        }

        if (!id.isEmpty() && id != null) {

            if (Common.isConnectedToInternet(getBaseContext()))
            {
                loadListFood(id);
            }
            else{
                Toast.makeText(this, "Please check your internet connection ", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        //Search

        materialSearchBar = (MaterialSearchBar) findViewById(R.id.search_bar);
        materialSearchBar.setHint("Enter your food");
        //materialSearchBar.setSpeechMode(false);

        loadSuggest();      //Function to load suggestions..from firebase
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

    private void loadListFood(String id) {
        Query query = FirebaseDatabase.getInstance().getReference().child("Food").orderByChild("menuId").equalTo(id);
        FirebaseListOptions<foodmenu> options = new FirebaseListOptions.Builder<foodmenu>()
                .setLayout(R.layout.menu)
                .setQuery(query, foodmenu.class).build();
       adapter = new FirebaseListAdapter<foodmenu>(options) {
           @Override
           protected void populateView(View v, final foodmenu model, final int position) {

               TextView foodname = v.findViewById(R.id.foodname);
               ImageView foodimage = v.findViewById(R.id.foodimage);
               final ImageView fav = v.findViewById(R.id.fav);


               final foodmenu foodItems = (foodmenu) model;
               foodname.setText(foodItems.getName().toString());
               Picasso.with(menuList.this).load(foodItems.getImage().toString()).into(foodimage);

               //Add Favorites
               if (localDb.isFavorite(adapter.getRef(position).getKey())){
                   fav.setImageResource(R.drawable.ic_favorite_black_24dp);
               }

               //click to change state
               fav.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       if (!localDb.isFavorite(adapter.getRef(position).getKey())){
                           localDb.addToFavorites(adapter.getRef(position).getKey());
                           fav.setImageResource(R.drawable.ic_favorite_black_24dp);
                           Toast.makeText(menuList.this, ""+foodItems.getName()+" was added to Favorites", Toast.LENGTH_SHORT).show();
                       }
                       else {
                           localDb.removeFromFavorites(adapter.getRef(position).getKey());
                           fav.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                           Toast.makeText(menuList.this, ""+foodItems.getName()+" was remove from Favorites", Toast.LENGTH_SHORT).show();
                       }
                   }
               });

               v.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       Intent intent = new Intent(menuList.this, Details.class);
                       intent.putExtra("foodId",adapter.getRef(position).getKey());
                       startActivity(intent);
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
        adapter.stopListening();
    }

    private void startSearch(CharSequence text) {
        {
        Log.d("myTag", "start search");

        Query query = FirebaseDatabase.getInstance().getReference().child("Food").orderByChild("name").equalTo(text.toString());
        FirebaseListOptions<foodmenu> listOptions = new FirebaseListOptions.Builder<foodmenu>()
                .setLayout(R.layout.menu)
                .setQuery(query, foodmenu.class).build();
        Log.d("myTag", "adapter");
        searchAdapter = new FirebaseListAdapter(listOptions) {
            @Override
            protected void populateView(View v, Object model, final int position) {

                TextView foodname = v.findViewById(R.id.foodname);
                ImageView foodimage = v.findViewById(R.id.foodimage);
                final foodmenu foodItems = (foodmenu) model;
                foodname.setText(foodItems.getName().toString());
                Picasso.with(menuList.this).load(foodItems.getImage().toString()).into(foodimage);

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(menuList.this, Details.class);
                        intent.putExtra("FoodId", searchAdapter.getRef(position).getKey());
                        startActivity(intent);
                    }
                });
            }
        };
        listView.setAdapter(searchAdapter);

    }
        searchAdapter.startListening();

}

    private void loadSuggest() {

        reference.child("Food").orderByChild("menuId").equalTo(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){

                            foodmenu item = postSnapshot.getValue(foodmenu.class);
                            suggestList.add(item.getName());  //Add name of food to suggest list
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
