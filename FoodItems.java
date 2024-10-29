package com.example.foodserverside;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodserverside.Common.Common;
import com.example.foodserverside.Model.Category;
import com.example.foodserverside.Model.foodmenu;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FoodItems extends AppCompatActivity {

    String id = "";
    ListView listView;

    DatabaseReference reference;
    FirebaseDatabase database;
    FirebaseListAdapter<foodmenu> adapter;

    FirebaseStorage storage;
    StorageReference storageReference;

    foodmenu newFood;

    FloatingActionButton fab;

    EditText edtName,edtDescription,edtPrice,edtDiscount;
    Button btnSelect,btnUpload;

    RelativeLayout rootLayout;

    Uri saveUri;
    private final int PICK_IMAGE_REQUEST = 71;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_items);


        listView = findViewById(R.id.listview);
        fab=findViewById(R.id.fab);
        database = FirebaseDatabase.getInstance();
        reference=database.getReference("Food");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFoodDialog();

            }
        });

        if (getIntent() != null) {
            id = getIntent().getStringExtra("CategoryId");
            Toast.makeText(this, ""+id, Toast.LENGTH_SHORT).show();

        }
        if (!id.isEmpty() && id != null ){
            loadFoodList(id);
        }

    }

    private void showAddFoodDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodItems.this);
        alertDialog.setTitle("Add new Food");

        alertDialog.setMessage("Please Enter All Information");


        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_food_layout,null);

        edtName = add_menu_layout.findViewById(R.id.edtName);
        edtDescription = add_menu_layout.findViewById(R.id.edtDescription);
        edtDiscount = add_menu_layout.findViewById(R.id.edtDiscount);
        edtPrice = add_menu_layout.findViewById(R.id.edtPrice);

        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

        rootLayout = findViewById(R.id.rootLayout);

        //Event for Button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); //select image from gallery and save their URL
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadImage();
            }
        });


        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Here just create new food menu
                if (newFood != null)
                {
                    reference.push().setValue(newFood);
                    Snackbar.make(rootLayout,"New Category"+newFood.getName()+"we added",Snackbar.LENGTH_SHORT).show();

                }


            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();

    }

    private void uploadImage() {
        if (saveUri!=null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);

            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(FoodItems.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    //set value for new category  if image upload and we can get download link
                                    newFood = new foodmenu();
                                    newFood.setName(edtName.getText().toString());
                                    newFood.setDescription(edtDescription.getText().toString());
                                    newFood.setPrice(edtPrice.getText().toString());
                                    newFood.setDiscount(edtDiscount.getText().toString());
                                    newFood.setMenuId(id);
                                    newFood.setImage(uri.toString());


                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(FoodItems.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress =(100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded"+progress+"%");
                        }
                    });

        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
    }


    private void loadFoodList(String id) {
        Query query = reference.orderByChild("menuId").equalTo(id);


        FirebaseListOptions<foodmenu> options = new FirebaseListOptions.Builder<foodmenu>()
                .setLayout(R.layout.menu)
                .setQuery(query, foodmenu.class).build();

       adapter = new FirebaseListAdapter<foodmenu>(options) {
           @Override
           protected void populateView(View v, foodmenu model, final int position) {
               TextView foodname = v.findViewById(R.id.foodname);
               ImageView foodimage = v.findViewById(R.id.foodimage);

               foodname.setText(model.getName().toString());
               Picasso.with(FoodItems.this).load(model.getImage().toString()).into(foodimage);
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
        adapter.notifyDataSetChanged(); //Refresh data if have data changed
        listView.setAdapter(adapter);
    }

    @Override
        protected void onStart () {
            super.onStart();
            adapter.startListening();

        }

        @Override
        protected void onStop () {
            super.onStop();
            adapter.startListening();

        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData() != null)
        {
            saveUri = data.getData();
            btnSelect.setText("Image Selected ! ");

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(item.getTitle().equals(Common.UPDATE))
        {
            showUpdatefoodDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));

        }
        else  if(item.getTitle().equals(Common.DELETE))
        {
            deletefoodCategory(adapter.getRef(item.getOrder()).getKey());

        }
        return super.onContextItemSelected(item);
    }

    private void deletefoodCategory(String key) {
        reference.child(key).removeValue();
        Toast.makeText(this, "Item deleted!!!", Toast.LENGTH_SHORT).show();
    }

    private void showUpdatefoodDialog(final String key, final foodmenu item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodItems.this);
        alertDialog.setTitle("Edit Food");

        alertDialog.setMessage("Please Enter All Information");


        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_food_layout,null);

        edtName = add_menu_layout.findViewById(R.id.edtName);
        edtDescription = add_menu_layout.findViewById(R.id.edtDescription);
        edtDiscount = add_menu_layout.findViewById(R.id.edtDiscount);
        edtPrice = add_menu_layout.findViewById(R.id.edtPrice);

        //set default values
        edtName.setText(item.getName());
        edtDescription.setText(item.getDescription());
        edtDiscount.setText(item.getDiscount());
        edtPrice.setText(item.getPrice());


        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

        rootLayout = findViewById(R.id.rootLayout);

        //Event for Button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); //select image from gallery and save their URL
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeImage(item);
            }
        });


        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Here just create new food menu
                if (item != null)
                {
                    // update information
                    item.setName(edtName.getText().toString());
                    item.setPrice(edtPrice.getText().toString());
                    item.setDiscount(edtDiscount.getText().toString());
                    item.setDescription(edtDescription.getText().toString());

                    reference.child(key).setValue(item);
                    Snackbar.make(rootLayout,"Food"+item.getName()+"we edited",Snackbar.LENGTH_SHORT).show();

                }


            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }

    private void changeImage(final foodmenu item) {
        if (saveUri!=null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);

            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(FoodItems.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    //set value for new category  if image upload and we can get download link
                                    item.setImage(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(FoodItems.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress =(100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded"+progress+"%");
                        }
                    });

        }
    }

}
