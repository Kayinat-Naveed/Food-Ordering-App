package com.example.orderfoodprject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.orderfoodprject.Common.Common;
import com.example.orderfoodprject.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class second extends AppCompatActivity {
    Button SignIn, SignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        SignIn = findViewById(R.id.SignIn);
        SignUp = findViewById(R.id.Signup);

        //Init paper
        Paper.init(this);



        SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),Login.class);
                startActivity(i);
            }
        });

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),Registration.class);
                startActivity(i);
            }
        });

        //Check Remember
        String user = Paper.book().read(Common.USER_KEY);
        String pwd = Paper.book().read(Common.PWD_KEY);


        if (user != null && pwd != null){
            if (!user.isEmpty() && !pwd.isEmpty()){
                login(user,pwd);
            }
        }
    }

    private void login(final String phone, final String pwd) {
        if (Common.isConnectedToInternet(getBaseContext())) {

            //Intialize firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference table_user = database.getReference("Users");


            final ProgressDialog mDialog = new ProgressDialog(second.this);
            mDialog.setMessage("Please Waiting...");
            mDialog.show();

            table_user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //check if user does not exist

                    if (dataSnapshot.child(phone).exists()) {
                        // get user information
                        mDialog.dismiss();
                        User user = dataSnapshot.child(phone).getValue(User.class);
                        user.setPhone(phone);//set Phone
                        if (user.getPassword().equals(pwd)){
                            Toast.makeText(second.this, "SignIn Successfull!", Toast.LENGTH_SHORT).show();
                            Intent menuIntent = new Intent(second.this, ListCategory.class);
                            Common.currentUser = user;
                            startActivity(menuIntent);
                            Log.d("myTag", "Execute");
                            finish();


                        } else {
                            Toast.makeText(second.this, "Worng Password!!!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mDialog.dismiss();
                        Toast.makeText(second.this, "User does not exist in database", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else {
            Toast.makeText(second.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
            return;
        }


    }
}
