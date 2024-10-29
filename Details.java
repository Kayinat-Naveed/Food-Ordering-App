package com.example.orderfoodprject;

import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;

import com.example.orderfoodprject.Common.Common;
import com.example.orderfoodprject.Database.Database;
import com.example.orderfoodprject.Model.Order;
import com.example.orderfoodprject.Model.Rating;
import com.example.orderfoodprject.Model.foodmenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Details extends AppCompatActivity implements RatingDialogListener {
    TextView food_name, food_price, food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart,btnRating;
    ElegantNumberButton numberButton;
    RatingBar rating;


    String foodid = "";
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    DatabaseReference ratingTb;

    foodmenu currentFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //Firebase initalization

        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("Food");
        ratingTb = firebaseDatabase.getReference("Rating");

        //intiate view
        numberButton = findViewById(R.id.number_button);
        btnCart = findViewById(R.id.btncart);
        btnRating = findViewById(R.id.btnRating);
        rating = findViewById(R.id.rating);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });


        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addToCart(new Order(
                        foodid,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount()
                ));
                Toast.makeText(Details.this, "Add to cart", Toast.LENGTH_SHORT).show();
            }
        });


        food_description = findViewById(R.id.food_description);
        food_name = findViewById(R.id.food_name);
        food_price = findViewById(R.id.food_price);

        food_image = findViewById(R.id.img_food);

        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapseAppbar);


        if (getIntent() != null) {
            foodid = getIntent().getStringExtra("foodId");
        }

        if (!foodid.isEmpty()){

            if (Common.isConnectedToInternet(getBaseContext())) {

                getDetailFood(foodid);
                getRatingFood(foodid);

            }
            else {
                Toast.makeText(Details.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private void getRatingFood(String foodid) {
        Query foodRating = ratingTb.orderByChild("foodid").equalTo(foodid);
        foodRating.addValueEventListener(new ValueEventListener() {
            int count = 0 , sum = 0;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum+=Integer.parseInt(item.getRateValue());
                    count++;
                }
               if (count != 0){
                   float average =sum/count;
                   rating.setRating(average);
               }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDetailFood(String foodid) {
        reference.child(foodid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(foodmenu.class);

                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(food_image);
                collapsingToolbarLayout.setTitle(currentFood.getName());
                food_price.setText(currentFood.getPrice());
                food_name.setText(currentFood.getName());
                food_description.setText(currentFood.getDescription());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad","Not Good","Quite Ok","Very Good","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this Food")
                .setDescription("Please give your feedback")
                .setTitleTextColor(R.color.colorPrimaryDark)
                .setDescriptionTextColor(R.color.colorPrimaryDark)
                .setHint("Please write your comment here...")
                .setHintTextColor(R.color.colorPrimary)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.MyDialogFadeAnimation)
                .create(Details.this)
                .show();

    }

    @Override
    public void onPositiveButtonClicked(int i, String s) {

        final Rating rating = new Rating(Common.currentUser.getPhone(),foodid,String.valueOf(i),s);

        ratingTb.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(Common.currentUser.getPhone()).exists()){
                    //remove old values
                    ratingTb.child(Common.currentUser.getPhone()).removeValue();
                    //update new values
                    ratingTb.child(Common.currentUser.getPhone()).setValue(rating);
                }
                else {
                    //update new values
                    ratingTb.child(Common.currentUser.getPhone()).setValue(rating);
                }

                Toast.makeText(Details.this, "Thank you for your feedback !!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }



}
