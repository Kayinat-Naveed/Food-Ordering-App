package com.example.orderfoodprject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orderfoodprject.Common.Common;
import com.example.orderfoodprject.Model.Category;
import com.example.orderfoodprject.Model.Request;
import com.example.orderfoodprject.Model.foodmenu;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class OrderStatus extends AppCompatActivity {


    FirebaseDatabase firebase;
    DatabaseReference requests;

    public ListView listView;
    FirebaseListAdapter<Request> adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        firebase = FirebaseDatabase.getInstance();
        requests = firebase.getReference();
        listView = findViewById(R.id.listOrder);

        if (Common.currentUser.getPhone() != null) {
            loadOrders(Common.currentUser.getPhone());
        } else if (getIntent() == null){
            loadOrders(getIntent().getStringExtra("userPhone"));
        }
    }

    private void loadOrders(String phone) {
        Query query = FirebaseDatabase.getInstance().getReference("Requests").orderByChild("phone").equalTo(phone);
        Log.d("Tag","msg");

        FirebaseListOptions<Request> options = new FirebaseListOptions.Builder<Request>().setLayout(R.layout.order_layout)
                .setQuery(query, Request.class).build();
        adapter = new FirebaseListAdapter<Request>(options) {
            @Override
            protected void populateView(View v, Request model, int position) {
                Log.d("Tag","msg");

                TextView txtOrderAddress = v.findViewById(R.id.order_address);
                TextView txtOrderId = v.findViewById(R.id.order_id);
                TextView txtOrderStatus = v.findViewById(R.id.order_status);
                TextView txtOrderPhone = v.findViewById(R.id.order_phone);

                txtOrderId.setText(getRef(position).getKey());
                txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                txtOrderPhone.setText(model.getPhone());
                txtOrderAddress.setText(model.getAddress());


            }
        };
        listView.setAdapter(adapter);
    }



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
