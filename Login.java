package com.example.orderfoodprject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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


import io.paperdb.Paper;

public class Login extends AppCompatActivity {
    EditText edtpassword, edtnumber;
    Button signIn;
    com.rey.material.widget.CheckBox chkbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);


        edtpassword = findViewById(R.id.edtpassword);
        edtnumber = findViewById(R.id.edtnumber);

        signIn = findViewById(R.id.SignIn);

        chkbox = (com.rey.material.widget.CheckBox) findViewById(R.id.chkbox);

        //initiate paper
        Paper.init(this);

        //Intialize firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("Users");


        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())) {

                    //SAve user and Password
                    if (chkbox.isChecked()){
                        Paper.book().write(Common.USER_KEY,edtnumber.getText().toString());
                        Paper.book().write(Common.PWD_KEY,edtpassword.getText().toString());

                    }

                    final ProgressDialog mDialog = new ProgressDialog(Login.this);
                    mDialog.setMessage("Please Waiting...");
                    mDialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            //check if user does not exist

                            if (dataSnapshot.child(edtnumber.getText().toString()).exists()) {
                                // get user information
                                mDialog.dismiss();
                                User user = dataSnapshot.child(edtnumber.getText().toString()).getValue(User.class);
                                user.setPhone(edtnumber.getText().toString());//set Phone
                                if (user.getPassword().equals(edtpassword.getText().toString())) {
                                    Toast.makeText(Login.this, "SignIn Successfull!", Toast.LENGTH_SHORT).show();
                                    Intent menuIntent = new Intent(Login.this, ListCategory.class);
                                    Common.currentUser = user;
                                    startActivity(menuIntent);
                                    Log.d("myTag", "Execute");
                                    finish();


                                } else {
                                    Toast.makeText(Login.this, "Worng Password!!!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(Login.this, "User does not exist in database", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Toast.makeText(Login.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}
