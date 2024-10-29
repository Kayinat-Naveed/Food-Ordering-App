package com.example.foodserverside;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.foodserverside.Common.Common;
import com.example.foodserverside.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class signin extends AppCompatActivity {
    EditText edtpassword, edtnumber;
    Button signIn;
    DatabaseReference reference;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        edtpassword = findViewById(R.id.edtpassword);
        edtnumber = findViewById(R.id.edtnumber);

        signIn = findViewById(R.id.SignIn);

        //Intialize firebase
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUser(edtnumber.getText().toString(), edtpassword.getText().toString());
            }
        });

    }

    private void signInUser(String phone, String password) {
        final ProgressDialog mDialog = new ProgressDialog(signin.this);
        mDialog.setMessage("Please Waiting...");
        mDialog.show();

        final String localPhone = phone;
        final String localPassword = password;
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(localPhone).exists()) {
                    mDialog.dismiss();
                    User user = dataSnapshot.child(localPhone).getValue(User.class);
                    user.setPhone(localPhone);
                    if (Boolean.parseBoolean(user.getIsStaff())) {
                        if (user.getPassword().equals(localPassword)) {
                            //login ok
                            Intent login = new Intent(signin.this,Home.class);
                            Common.currentUser = user;
                            startActivity(login);
                            finish();

                        } else
                            Toast.makeText(signin.this, "Wrong Password!", Toast.LENGTH_SHORT).show();

                    } else
                        Toast.makeText(signin.this, "Please login with staff account", Toast.LENGTH_SHORT).show();


                } else {
                    mDialog.dismiss();
                    Toast.makeText(signin.this, "User not exist in database", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
