package com.ithought.rahul.ithought;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Random;

public class AddPost extends AppCompatActivity {

    private static final int MAX_LENGTH = 40;
    private static final int PICK_PHOTO = 0;
    private ImageView post_image;
    private EditText title,content;
    private Spinner category;
    private Button save_normal,save_anonymous;
    private DatabaseReference mRef,newPost,oldPost;
    private StorageReference mPicReference;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private Uri imageUri = null,imageUrl,resultUri=null;
    private ProgressDialog mProgress_save_to_database;
    private String Uid,Name,Email,key=null,image,category_from_spinner;
    private String titleString,contentString;
    private ArrayAdapter<CharSequence> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        key = getIntent().getStringExtra("Key");
        mProgress_save_to_database = new ProgressDialog(this);
        mFirebaseStorage = FirebaseStorage.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child("posts_images").child(randomize());
        Uid = mAuth.getCurrentUser().getUid();
        Name = mAuth.getCurrentUser().getDisplayName();
        Email = mAuth.getCurrentUser().getEmail();
        newPost = mRef.child("posts").push();


        category = (Spinner)findViewById(R.id.category);
        adapter = ArrayAdapter.createFromResource(this,R.array.category_list,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adapter);





        title = (EditText)findViewById(R.id.title_post);
        title.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        content = (EditText)findViewById(R.id.content_post);
        save_anonymous = (Button)findViewById(R.id.anonymous_post);
        save_normal = (Button)findViewById(R.id.yourself_post);
        post_image = (ImageView)findViewById(R.id.post_image);
        post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, PICK_PHOTO);
            }
        });

        content.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK){
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });


        if(key!=null){
            oldPost = mRef.child("posts").child(key);
            oldPost.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    title.setText(dataSnapshot.child("title").getValue(String.class));
                    content.setText(dataSnapshot.child("content").getValue(String.class));
                    image =dataSnapshot.child("ImageUrl").getValue(String.class);
                    int position = adapter.getPosition(dataSnapshot.child("category").getValue(String.class));
                    category.setSelection(position);
                    Picasso.with(AddPost.this).load(image).fit().centerCrop().into(post_image);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }


        save_normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                titleString = title.getText().toString().trim();
                contentString = content.getText().toString().trim();
                category_from_spinner = category.getSelectedItem().toString();

                if(key!=null){
                    if(titleString.length()==0 && contentString.length()!=0 && !category_from_spinner.equals("Select a Category")){
                        Toast.makeText(AddPost.this,"Please add a title to your post",Toast.LENGTH_LONG).show();
                    }else if (titleString.length()!=0 && contentString.length()==0 && !category_from_spinner.equals("Select a Category")){
                        Toast.makeText(AddPost.this,"Please add some content to your post",Toast.LENGTH_LONG).show();
                    }else if (titleString.length()!=0 && contentString.length()!=0 && category_from_spinner.equals("Select a Category")){
                        Toast.makeText(AddPost.this,"Please select a category",Toast.LENGTH_LONG).show();
                    }else if (titleString.length()==0 || contentString.length()==0 || category_from_spinner.equals("Select a Category")){
                        Toast.makeText(AddPost.this,"Please, Don't leave them blank",Toast.LENGTH_LONG).show();
                    }else{
                        mProgress_save_to_database.setMessage("saving data...");
                        mProgress_save_to_database.show();
                        mProgress_save_to_database.setCancelable(false);
                        saveNormally();
                    }
                }else{

                    if(titleString.length()==0 && contentString.length()!=0 && !category_from_spinner.equals("Select a Category")&& resultUri!=null){
                        Toast.makeText(AddPost.this,"Please add a title to your post",Toast.LENGTH_LONG).show();
                    }else if (titleString.length()!=0 && contentString.length()==0 && !category_from_spinner.equals("Select a Category")&& resultUri!=null){
                        Toast.makeText(AddPost.this,"Please add some content to your post",Toast.LENGTH_LONG).show();
                    }else if (titleString.length()!=0 && contentString.length()!=0 && category_from_spinner.equals("Select a Category")&& resultUri!=null){
                        Toast.makeText(AddPost.this,"Please select a category",Toast.LENGTH_LONG).show();
                    }else if (titleString.length()!=0 && contentString.length()!=0 && !category_from_spinner.equals("Select a Category")&& resultUri==null){
                        Toast.makeText(AddPost.this,"Please add a picture to your post",Toast.LENGTH_LONG).show();
                    }else if (titleString.length()==0 || contentString.length()==0 || category_from_spinner.equals("Select a Category")){
                        Toast.makeText(AddPost.this,"Please, Don't leave them blank",Toast.LENGTH_LONG).show();
                    }else{
                        mProgress_save_to_database.setMessage("saving data...");
                        mProgress_save_to_database.show();
                        mProgress_save_to_database.setCancelable(false);
                        saveNormally();
                    }
                }

            }
        });

        save_anonymous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titleString = title.getText().toString().trim();
                contentString = content.getText().toString().trim();
                category_from_spinner = category.getSelectedItem().toString();

                if(key!=null){
                    if(titleString.length()==0 && contentString.length()!=0 && !category_from_spinner.equals("Select a Category")){
                        Toast.makeText(AddPost.this,"Please add a title to your post",Toast.LENGTH_LONG).show();
                    }else if (titleString.length()!=0 && contentString.length()==0 && !category_from_spinner.equals("Select a Category")){
                        Toast.makeText(AddPost.this,"Please add some content to your post",Toast.LENGTH_LONG).show();
                    }else if (titleString.length()!=0 && contentString.length()!=0 && category_from_spinner.equals("Select a Category")){
                        Toast.makeText(AddPost.this,"Please select a category",Toast.LENGTH_LONG).show();
                    }else if (titleString.length()==0 || contentString.length()==0 || category_from_spinner.equals("Select a Category")){
                        Toast.makeText(AddPost.this,"Please, Don't leave them blank",Toast.LENGTH_LONG).show();
                    }else{
                        mProgress_save_to_database.setMessage("saving data...");
                        mProgress_save_to_database.show();
                        mProgress_save_to_database.setCancelable(false);
                        saveAnonymously();
                    }
                }else{

                    if(titleString.length()==0 && contentString.length()!=0 && !category_from_spinner.equals("Select a Category")&& resultUri!=null){
                        Toast.makeText(AddPost.this,"Please add a title to your post",Toast.LENGTH_LONG).show();
                    }else if (titleString.length()!=0 && contentString.length()==0 && !category_from_spinner.equals("Select a Category")&& resultUri!=null){
                        Toast.makeText(AddPost.this,"Please add some content to your post",Toast.LENGTH_LONG).show();
                    }else if (titleString.length()!=0 && contentString.length()!=0 && category_from_spinner.equals("Select a Category")&& resultUri!=null){
                        Toast.makeText(AddPost.this,"Please select a category",Toast.LENGTH_LONG).show();
                    }else if (titleString.length()!=0 && contentString.length()!=0 && !category_from_spinner.equals("Select a Category")&& resultUri==null){
                        Toast.makeText(AddPost.this,"Please add a picture to your post",Toast.LENGTH_LONG).show();
                    }else if (titleString.length()==0 || contentString.length()==0 || category_from_spinner.equals("Select a Category")){
                        Toast.makeText(AddPost.this,"Please, Don't leave them blank",Toast.LENGTH_LONG).show();
                    }else{
                        mProgress_save_to_database.setMessage("saving data...");
                        mProgress_save_to_database.show();
                        mProgress_save_to_database.setCancelable(false);
                        saveAnonymously();
                    }
                }

            }
        });

    }


    private void saveAnonymously() {

        if(key!=null){
            oldPost.child("title").setValue(titleString);
            oldPost.child("content").setValue(contentString);
            oldPost.child("uid").setValue(Uid);
            oldPost.child("name").setValue(Name);
            oldPost.child("email").setValue(Email);
            oldPost.child("anonymous").setValue("true");
            oldPost.child("category").setValue(category_from_spinner);
            oldPost.child("time").setValue(ServerValue.TIMESTAMP);
            if(resultUri!=null){

                mPicReference = mFirebaseStorage.getReferenceFromUrl(image);
                mPicReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {}
                });

                mStorage.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(AddPost.this,"upload done",Toast.LENGTH_LONG).show();
                        imageUrl = taskSnapshot.getDownloadUrl();
                        oldPost.child("ImageUrl").setValue(imageUrl.toString());
                        mProgress_save_to_database.dismiss();

                        finish();
                    }
                });
            }else{
                oldPost.child("ImageUrl").setValue(image);
                finish();
            }
        }else{
            newPost.child("title").setValue(titleString);
            newPost.child("content").setValue(contentString);
            newPost.child("uid").setValue(Uid);
            newPost.child("name").setValue(Name);
            newPost.child("email").setValue(Email);
            newPost.child("anonymous").setValue("true");
            newPost.child("category").setValue(category_from_spinner);
            newPost.child("time").setValue(ServerValue.TIMESTAMP);
            mStorage.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(AddPost.this,"upload done",Toast.LENGTH_LONG).show();
                    imageUrl = taskSnapshot.getDownloadUrl();
                    newPost.child("ImageUrl").setValue(imageUrl.toString());
                    mProgress_save_to_database.dismiss();

                    finish();
                }
            });
        }

    }

    private void saveNormally() {

        if(key!=null){
            oldPost.child("title").setValue(titleString);
            oldPost.child("content").setValue(contentString);
            oldPost.child("uid").setValue(Uid);
            oldPost.child("name").setValue(Name);
            oldPost.child("email").setValue(Email);
            oldPost.child("anonymous").setValue("false");
            oldPost.child("category").setValue(category_from_spinner);
            oldPost.child("time").setValue(ServerValue.TIMESTAMP);

            if(resultUri!=null) {

                mPicReference = mFirebaseStorage.getReferenceFromUrl(image);
                mPicReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {}
                });

                mStorage.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(AddPost.this, "upload done", Toast.LENGTH_LONG).show();
                        imageUrl = taskSnapshot.getDownloadUrl();
                        oldPost.child("ImageUrl").setValue(imageUrl.toString());
                        mProgress_save_to_database.dismiss();

                        finish();
                    }
                });
            }else{
                oldPost.child("ImageUrl").setValue(image);
                finish();
            }

        }else{
            newPost.child("title").setValue(titleString);
            newPost.child("content").setValue(contentString);
            newPost.child("uid").setValue(Uid);
            newPost.child("name").setValue(Name);
            newPost.child("email").setValue(Email);
            newPost.child("anonymous").setValue("false");
            newPost.child("category").setValue(category_from_spinner);
            newPost.child("time").setValue(ServerValue.TIMESTAMP);
            mStorage.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(AddPost.this,"upload done",Toast.LENGTH_LONG).show();
                    imageUrl = taskSnapshot.getDownloadUrl();
                    newPost.child("ImageUrl").setValue(imageUrl.toString());
                    mProgress_save_to_database.dismiss();

                    finish();
                }
            });
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
                    .setAspectRatio(4,3)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                post_image.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
