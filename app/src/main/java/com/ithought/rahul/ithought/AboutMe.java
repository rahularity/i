package com.ithought.rahul.ithought;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Random;

public class AboutMe extends AppCompatActivity {

    private static final int MAX_LENGTH = 40;
    private static final int PICK_PHOTO = 0;
    private Uri imageUri = null,resultUri=null;
    String ImageUrl = null;
    private TextView userName,userEmail;
    private EditText aboutYourself,website,instagram;
    private Button done;
    private ImageView profilePic;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private StorageReference mStorage,userStorage;
    private FirebaseStorage mFirebaseStorage;
    private ProgressDialog pd;
    String Uid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);

        mAuth = FirebaseAuth.getInstance();
        Uid = mAuth.getCurrentUser().getUid();
        userName = (TextView)findViewById(R.id.user_name);
        userEmail = (TextView)findViewById(R.id.user_email);
        aboutYourself = (EditText) findViewById(R.id.about_yourself);
        website = (EditText) findViewById(R.id.website);
        instagram = (EditText) findViewById(R.id.instagram);
        profilePic = (ImageView)findViewById(R.id.profile_pic);
        done = (Button)findViewById(R.id.done);

        pd = new ProgressDialog(this);




        mStorage = FirebaseStorage.getInstance().getReference().child("profile_pics").child(randomize());
        userDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(Uid);

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userName.setText(dataSnapshot.child("userName").getValue(String.class));
                userEmail.setText(dataSnapshot.child("userEmail").getValue(String.class));
                ImageUrl = dataSnapshot.child("profilePic").getValue(String.class);
                website.setText(dataSnapshot.child("website").getValue(String.class));
                aboutYourself.setText(dataSnapshot.child("about").getValue(String.class));
                instagram.setText(dataSnapshot.child("instagram").getValue(String.class));

                if(ImageUrl.length()==0){}else{
                    Picasso.with(AboutMe.this).load(ImageUrl).fit().centerCrop().into(profilePic);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userDatabase.keepSynced(true);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("saving profile");
                pd.show();
                savePersonalData();
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, PICK_PHOTO);
            }
        });

    }

    private void savePersonalData() {

        final String about = aboutYourself.getText().toString().trim();
        final String web = website.getText().toString().trim();



        if(resultUri == null){
            userDatabase.child("userName").setValue(userName.getText().toString().trim());
            userDatabase.child("userEmail").setValue(userEmail.getText().toString().trim());
            userDatabase.child("about").setValue(about);
            userDatabase.child("instagram").setValue(instagram.getText().toString().trim());
            userDatabase.child("website").setValue(web);
            pd.dismiss();
            finish();
        }else {
            if(ImageUrl.length()!=0){



                try{
                    userStorage = FirebaseStorage.getInstance().getReferenceFromUrl(ImageUrl);

                }catch(IllegalArgumentException e){
                    userStorage = null;
                }
                if(userStorage!=null){
                    userStorage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AboutMe.this,"previous profile pic deleted from database successfully",Toast.LENGTH_SHORT).show();
                        }
                    });
                }


                mStorage.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri imageUrl = taskSnapshot.getDownloadUrl();
                        userDatabase.child("userName").setValue(userName.getText().toString().trim());
                        userDatabase.child("userEmail").setValue(userEmail.getText().toString().trim());
                        userDatabase.child("about").setValue(about);
                        userDatabase.child("website").setValue(web);
                        userDatabase.child("instagram").setValue(instagram.getText().toString().trim());
                        userDatabase.child("profilePic").setValue(imageUrl.toString());
                        pd.dismiss();
                        finish();
                    }
                });

            }else{
                mStorage.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri imageUrl = taskSnapshot.getDownloadUrl();
                        userDatabase.child("userName").setValue(userName.getText().toString().trim());
                        userDatabase.child("userEmail").setValue(userEmail.getText().toString().trim());
                        userDatabase.child("about").setValue(about);
                        userDatabase.child("website").setValue(web);
                        userDatabase.child("instagram").setValue(instagram.getText().toString().trim());
                        userDatabase.child("profilePic").setValue(imageUrl.toString());
                        pd.dismiss();
                        finish();
                    }
                });
            }

        }



    }

    public static String randomize() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PHOTO && resultCode == RESULT_OK){

            imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                profilePic.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    }

