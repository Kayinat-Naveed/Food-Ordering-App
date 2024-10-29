package com.example.orderfoodprject;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orderfoodprject.Common.Common;
import com.example.orderfoodprject.Model.Category;
import com.example.orderfoodprject.Model.Token;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;

public class ListCategory extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView textfullName;

    ListView listView;

    DatabaseReference reference;
    FirebaseListAdapter<Category> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_category);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FirebaseApp.initializeApp(this);

        Paper.init(this);

        //initiate firebase
        reference = FirebaseDatabase.getInstance().getReference();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListCategory.this, cartFood.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set name for user
        View headerView = navigationView.getHeaderView(0);
        textfullName = headerView.findViewById(R.id.textfullName);
        textfullName.setText(Common.currentUser.getName());

        if (Common.isConnectedToInternet(this))
        {
            loadMenu();
        }
        else{
            Toast.makeText(this, "Please check your internet connection ", Toast.LENGTH_SHORT).show();
            return;
        }

        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token,false);//because this token send from client app
        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }

    private void loadMenu() {
        //Load category
        listView = findViewById(R.id.listview);
        Query query = FirebaseDatabase.getInstance().getReference().child("Category");

        FirebaseListOptions<Category> options = new FirebaseListOptions.Builder<Category>()
                .setLayout(R.layout.food_item)
                .setQuery(query, Category.class).build();
        adapter = new FirebaseListAdapter<Category>(options) {
            @Override
            protected void populateView(View v, Category model, int position) {
                TextView foodname = v.findViewById(R.id.food_name);
                ImageView foodimage = v.findViewById(R.id.food_image);
                final Category food = (Category) model;
                foodname.setText(food.getName().toString());
                Picasso.with(ListCategory.this).load(food.getImage().toString()).into(foodimage);


                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(ListCategory.this, menuList.class);
                        //Bcz CategoryId is key, so we just get key of this item
                        intent.putExtra("CId", food.getCategoryId());
                        startActivity(intent);
                        //Toast.makeText(MainActivity.this, "" +CId, Toast.LENGTH_SHORT).show();
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
        getMenuInflater().inflate(R.menu.list_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.refresh){
            //Load category
            listView = findViewById(R.id.listview);
            Query query = FirebaseDatabase.getInstance().getReference().child("Category");
            FirebaseListOptions<Category> options = new FirebaseListOptions.Builder<Category>()
                    .setLayout(R.layout.food_item)
                    .setQuery(query, Category.class).build();
            adapter = new FirebaseListAdapter<Category>(options) {
                @Override
                protected void populateView(View v, Category model, int position) {
                    TextView foodname = v.findViewById(R.id.food_name);
                    ImageView foodimage = v.findViewById(R.id.food_image);
                    final Category food = (Category) model;
                    foodname.setText(food.getName().toString());
                    Picasso.with(ListCategory.this).load(food.getImage().toString()).into(foodimage);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(ListCategory.this, menuList.class);
                            //Bcz CategoryId is key, so we just get key of this item
                            intent.putExtra("CId", food.getCategoryId());
                            startActivity(intent);
                        }
                    });
                }
            };
            listView.setAdapter(adapter);
        }
        adapter.startListening();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            // Handle the camera action
        } else if (id == R.id.nav_cart) {
            Intent cart = new Intent(ListCategory.this,cartFood.class);
            startActivity(cart);

        } else if (id == R.id.nav_orders) {
            Intent order = new Intent(ListCategory.this,OrderStatus.class);
            startActivity(order);

        } else if (id == R.id.nav_LogOut) {

            //Delete Remember User and Password
            Paper.book().destroy();

            //Logout
            Intent logout = new Intent(ListCategory.this,Login.class);
            logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(logout);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
