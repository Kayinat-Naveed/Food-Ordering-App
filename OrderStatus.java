package com.example.foodserverside;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.foodserverside.Common.Common;
import com.example.foodserverside.Model.Request;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.jaredrummler.materialspinner.MaterialSpinner;

public class OrderStatus extends AppCompatActivity {

    ListView listView;

    FirebaseDatabase database;
    DatabaseReference reference;

    FirebaseListAdapter<Request> adapter;

    //MaterialSpinner spinner;
    MaterialSpinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Requests");

        listView = findViewById(R.id.listview);

        loadOrder();
    }

    private void loadOrder() {
        Query query = FirebaseDatabase.getInstance().getReference("Requests");
        FirebaseListOptions<Request> options = new FirebaseListOptions.Builder<Request>().setLayout(R.layout.order_layout)
                .setQuery(query, Request.class).build();
        adapter = new FirebaseListAdapter<Request>(options) {
            @Override
            protected void populateView(View v, Request model, final int position) {

                TextView txtOrderAddress = v.findViewById(R.id.order_address);
                TextView txtOrderId = v.findViewById(R.id.order_id);
                TextView txtOrderStatus = v.findViewById(R.id.order_status);
                TextView txtOrderPhone = v.findViewById(R.id.order_phone);

                txtOrderId.setText(getRef(position).getKey());
                txtOrderStatus.setText(convertCodeToStatus(model.getStatus()));
                txtOrderPhone.setText(model.getPhone());
                txtOrderAddress.setText(model.getAddress());
                Log.d("Tag", "show");

                v.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        menu.setHeaderTitle("Select the action");

                        menu.add(0,0,position, Common.UPDATE);
                        menu.add(0,1,position,Common.DELETE);
                    }
                });
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });


            }
        };
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);

    }

    private String convertCodeToStatus(String status) {
        Log.d("Tag", "status");
        if (status.equals("0"))
            return "Placed";
        else if (status.equals("1"))
            return "On The Way";
        else
            return "Shipped";
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
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)){
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }

        else if (item.getTitle().equals(Common.DELETE)){
            deleteOrder(adapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }
    private void deleteOrder(String key) {
        reference.child(key).removeValue();
    }

    private void showUpdateDialog(String key, final Request item) {



        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please choose status");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order_layout,null);

        spinner = view.findViewById(R.id.spinner);
        spinner.setItems("Placed","On my way", "Shipped");

        alertDialog.setView(view);
        final String localkey = key;

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                item.setStatus(String.valueOf(spinner.getSelectedIndex()));
                reference.child(localkey).setValue(item);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }
}
