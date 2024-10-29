package com.example.orderfoodprject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.orderfoodprject.Common.Common;
import com.example.orderfoodprject.Model.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Registration extends AppCompatActivity {

    Button account;
    EditText name,phone,mail,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        account = findViewById(R.id.account);

        name= findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        mail = findViewById(R.id.mail);
        password = findViewById(R.id.password);

        FirebaseApp.initializeApp(this);


        //Intialize firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("Users");

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())){

                final ProgressDialog mDialog = new ProgressDialog(Registration.this);
                mDialog.setMessage("Please Waiting...");
                mDialog.show();

                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //check if user already exist

                        if (dataSnapshot.child(phone.getText().toString()).exists()){
                            mDialog.dismiss();
                            Toast.makeText(Registration.this, "Phone number already register", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            mDialog.dismiss();
                            User user = new User(name.getText().toString(),mail.getText().toString(),password.getText().toString());
                            table_user.child(phone.getText().toString()).setValue(user);
                            Toast.makeText(Registration.this, "Sign Up Successfully!!", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
                else {
                    Toast.makeText(Registration.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });
    }
}
