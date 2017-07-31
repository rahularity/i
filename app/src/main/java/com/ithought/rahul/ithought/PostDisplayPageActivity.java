package com.ithought.rahul.ithought;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.ui.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Random;

public class PostDisplayPageActivity extends AppCompatActivity {

    private static final int MAX_LENGTH = 20;
    private TextView title,content,loveCount,writerName;
    private ImageButton edit,delete,love;
    private ImageView postImage;
    private DatabaseReference mPostRef,mLoveRef,mRef,userRef;
    private StorageReference mPicReference;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String key = null,Uid,photoUrl,authorUid,profPic,isAnonymous;
    private boolean mLoved = false;
    private ImageButton authorDetails;
    private Dialog dialog,dialogAnonymous,dialogDelete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_post_display_page);



        dialogDelete = new Dialog(this);
        dialogDelete.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDelete.setContentView(R.layout.delete_dialog_layout);
        dialogAnonymous = new Dialog(this);
        dialogAnonymous.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogAnonymous.setContentView(R.layout.anonymous_dialog);
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.user_detail_dialog);


        title = (TextView)findViewById(R.id.title);
        content = (TextView)findViewById(R.id.content);
        writerName = (TextView)findViewById(R.id.writer_name);
        loveCount = (TextView)findViewById(R.id.loves_count);
        edit = (ImageButton)findViewById(R.id.edit_button);
        delete = (ImageButton)findViewById(R.id.delete_button);
        love = (ImageButton)findViewById(R.id.love_button);
        postImage = (ImageView)findViewById(R.id.image_post);
        authorDetails = (ImageButton) findViewById(R.id.info);

        key = getIntent().getStringExtra("Key");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Uid = currentUser.getUid();
        }


        mFirebaseStorage = FirebaseStorage.getInstance();
        mLoveRef = FirebaseDatabase.getInstance().getReference().child("loves");
        mPostRef = FirebaseDatabase.getInstance().getReference().child("posts").child(key);
        mRef = FirebaseDatabase.getInstance().getReference().child("posts");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        mPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                authorUid = dataSnapshot.child("uid").getValue(String.class);
                isAnonymous = dataSnapshot.child("anonymous").getValue(String.class);

                if(isAnonymous.equals("true")){
                    String name = "by someone Anonymous";
                    writerName.setText(name);
                }else{
                    //setting writer name on the post display page
                    DatabaseReference theWriter = userRef.child(authorUid);
                    theWriter.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String name = "by "+ dataSnapshot.child("userName").getValue(String.class);
                            writerName.setText(name);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uid = dataSnapshot.child(key).child("uid").getValue(String.class);
                if (uid != null && uid.equals(Uid)) {
                    photoUrl = dataSnapshot.child(key).child("ImageUrl").getValue(String.class);
                    delete.setVisibility(View.VISIBLE);
                    edit.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });


        love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUnsetLove(mLoveRef,key,Uid);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialogDelete.show();

                TextView cancel,delete;
                delete = (TextView)dialogDelete.findViewById(R.id.delete);
                cancel = (TextView)dialogDelete.findViewById(R.id.cancel);

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPostRef.removeValue();
                        mLoveRef.child(key).removeValue();
                        mPicReference = mFirebaseStorage.getReferenceFromUrl(photoUrl);
                        mPicReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(PostDisplayPageActivity.this,"deleted successfully",Toast.LENGTH_SHORT).show();
                            }
                        });
                        finish();
                        Intent mainActivity = new Intent(PostDisplayPageActivity.this,MainActivity.class);
                        startActivity(mainActivity);
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogDelete.dismiss();
                    }
                });



            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPost();
            }
        });

        showPost();
        setLoveButton(Uid,key);


        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


        authorDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPostAuthorDetail();
            }
        });



        //setting number of likes on the post display page
        DatabaseReference loveReference = mLoveRef.child(key);
        loveReference.child("likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long countLove = dataSnapshot.getChildrenCount();
                String love = String.valueOf(countLove)+" likes";
                loveCount.setText(love);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }

    private void showPostAuthorDetail() {

        if(isAnonymous.equals("false")){
            final TextView website,about,name,email;
            name = (TextView)dialog.findViewById(R.id.name);
            email = (TextView)dialog.findViewById(R.id.email);
            about = (TextView)dialog.findViewById(R.id.about);
            website = (TextView)dialog.findViewById(R.id.website);

            final LinearLayout aboutSection,websiteSection;
            aboutSection = (LinearLayout)dialog.findViewById(R.id.about_section);
            websiteSection = (LinearLayout)dialog.findViewById(R.id.website_section);

            final ImageView profilePic = (ImageView)dialog.findViewById(R.id.profile_pic);

            ImageButton close = (ImageButton)dialog.findViewById(R.id.close);

            DatabaseReference theAuthor = userRef.child(authorUid);
            theAuthor.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    name.setText(dataSnapshot.child("userName").getValue(String.class));
                    email.setText(dataSnapshot.child("userEmail").getValue(String.class));
                    about.setText(dataSnapshot.child("about").getValue(String.class));
                    website.setText(dataSnapshot.child("website").getValue(String.class));
                    profPic = dataSnapshot.child("profilePic").getValue(String.class);

                        if (about.length()!=0){
                            aboutSection.setVisibility(View.VISIBLE);
                        }
                        if (website.length()!=0){
                            websiteSection.setVisibility(View.VISIBLE);
                        }
                        if (profPic.length()!=0){
                            Picasso.with(PostDisplayPageActivity.this).load(profPic).into(profilePic);
                        }
                    }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
            dialog.show();

            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }else {
            ImageButton close = (ImageButton)dialogAnonymous.findViewById(R.id.close);
            dialogAnonymous.show();
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAnonymous.dismiss();
                }
            });

        }


    }

    private void editPost() {

        Intent editorActivity = new Intent(PostDisplayPageActivity.this,AddPost.class).putExtra("Key",key);
        startActivity(editorActivity);

    }

    private void setUnsetLove(final DatabaseReference mLoveRef, final String key, String uid) {

        mLoved = true;

        mLoveRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(mLoved){
                    if(dataSnapshot.child(key).child("likes").hasChild(Uid)){

                        mLoveRef.child(key).child("likes").child(Uid).removeValue();
                        mLoved = false;
                    }else {
                        mPostRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                mLoveRef.child(key).child("title").setValue(dataSnapshot.child("title").getValue(String.class));
                                mLoveRef.child(key).child("content").setValue(dataSnapshot.child("content").getValue(String.class));
                                mLoveRef.child(key).child("ImageUrl").setValue(dataSnapshot.child("ImageUrl").getValue(String.class));
                                mLoveRef.child(key).child("uid").setValue(dataSnapshot.child("uid").getValue(String.class));
                                mLoveRef.child(key).child("name").setValue(dataSnapshot.child("name").getValue(String.class));
                                mLoveRef.child(key).child("email").setValue(dataSnapshot.child("email").getValue(String.class));
                                mLoveRef.child(key).child("category").setValue(dataSnapshot.child("category").getValue(String.class));
                                mLoveRef.child(key).child("randomId").setValue(randomize());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        mLoveRef.child(key).child("likes").child(Uid).setValue("thank you for your love");
                        mLoved = false;
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showPost() {

        mPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String anonymousValue = dataSnapshot.child("anonymous").getValue(String.class);
                if (anonymousValue != null) {
                    if(anonymousValue.equals("true")){
                        title.setText(dataSnapshot.child("title").getValue(String.class));
                        content.setText(dataSnapshot.child("content").getValue(String.class));
                        Picasso.with(PostDisplayPageActivity.this).load(dataSnapshot.child("ImageUrl").getValue(String.class)).into(postImage);

                    }else{
                        title.setText(dataSnapshot.child("title").getValue(String.class));
                        content.setText(dataSnapshot.child("content").getValue(String.class));
                        Picasso.with(PostDisplayPageActivity.this).load(dataSnapshot.child("ImageUrl").getValue(String.class)).fit().into(postImage);
                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void setLoveButton(final String Uid, final String key){

        mLoveRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(key).child("likes").hasChild(Uid)){
                    love.setImageResource(R.drawable.heart_filled);
                }else{
                    love.setImageResource(R.drawable.heart);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    
}
