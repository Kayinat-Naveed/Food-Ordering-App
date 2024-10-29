package com.example.orderfoodprject;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orderfoodprject.Common.Common;
import com.example.orderfoodprject.Database.Database;
import com.example.orderfoodprject.Model.Order;
import com.example.orderfoodprject.Model.Request;
import com.example.orderfoodprject.Model.Token;
import com.example.orderfoodprject.viewHolder.CartAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.BreakIterator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class cartFood extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference reference;

    TextView txtTotalPrice;
    Button btnPlace;

    List<Order> cart = new ArrayList<>();

    CartAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_food);

        database= FirebaseDatabase.getInstance();
        reference = database.getReference("Requests");

        recyclerView = findViewById(R.id.listCart);
        Log.d("myTag","recyclerView");
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice = findViewById(R.id.total);
        btnPlace = findViewById(R.id.placeOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cart.size() > 0 ){

                    showAlertDialog();
                }
                else{
                    Toast.makeText(cartFood.this, "Your cart is empty !!!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        loadListFood();

    }

    private void showAlertDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(cartFood.this);
        alertDialog.setTitle("One More Step!");
        alertDialog.setMessage("Enter Your Address: ");

        final EditText editAddress = new EditText(cartFood.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editAddress.setLayoutParams(lp);
        alertDialog.setView(editAddress);//Add edit Text to alert dialog
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Create new Request
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        editAddress.getText().toString(),
                        txtTotalPrice.getText().toString(),
                        cart
                );

                //Submit to Firebase
                //We will using System.CurrentMilli to key

                String order_number = String.valueOf((System.currentTimeMillis()));
                reference.child(order_number).setValue(request);
                //Delete Cart
                new Database(getBaseContext()).cleanCart();

                sendNotificationOrder(order_number);


//                Toast.makeText(cartFood.this, "Thank you , Order Placed", Toast.LENGTH_SHORT).show();
//                finish();
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();

    }

    private void sendNotificationOrder(String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("isServerToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for (DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                   Token serverToken = postSnapShot.getValue(Token.class);
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadListFood() {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //Calculate total price
        int total = 0;
        for (Order order : cart){
            total += (Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        }
        Locale locale = new Locale("en", "PK");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        txtTotalPrice.setText(fmt.format(total));
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());

        return true;
    }

    private void deleteCart(int position) {
        //remove item from List<Order> by position
        cart.remove(position);
        //delete all old data from SQLite
        new Database(this).cleanCart();
        //update new data from List<Order> to SQLite
        for (Order item:cart)
            new Database(this).addToCart(item);
        //Refresh
        loadListFood();
    }
}
