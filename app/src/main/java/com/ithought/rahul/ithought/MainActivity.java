package com.ithought.rahul.ithought;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int MAX_LENGTH = 40;
    public DrawerLayout mDrawerLayout;
    public ActionBarDrawerToggle mDrawerToggle;
    public String[] mDrawableListItem;
    private RecyclerView mPostsList;
    boolean mLoved = false;
    String Uid;
    FirebaseAuth mAuth;
    private DatabaseReference mRefLoves,mRef,mLoveNode,mRefPosts;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public Query postsNode,likedPosts;
    ProgressDialog dialog;
    TextView emptyView;
    private LinearLayoutManager mLayoutManager;
    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        emptyView = (TextView)findViewById(R.id.empty_view);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);
        mDrawableListItem = getResources().getStringArray(R.array.drawer_list);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                myToolbar,
                R.string.app_name,
                R.string.app_name
        ){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });


        dialog=new ProgressDialog(this);
        dialog.setMessage("please wait while the content is loading...");
        dialog.show();
        dialog.setCancelable(false);
        mRef=FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        postsNode = FirebaseDatabase.getInstance().getReference().child("posts");
        mPostsList = (RecyclerView)findViewById(R.id.posts_list);

        mLayoutManager = new LinearLayoutManager(MainActivity.this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mPostsList.setLayoutManager(mLayoutManager);

        Uid = mAuth.getCurrentUser().getUid();


        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){

                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                    finish();

                }
            }
        };


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddPost.class);
                startActivity(intent);
            }
        });



        //getting the reference of the liked posts here in likedPosts and then putting the value in postsNode for retrieving liked posts
        DatabaseReference Node = FirebaseDatabase.getInstance().getReference().child("loves");
        final String likesUid = "likes/"+Uid;

        Node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                likedPosts = dataSnapshot.getRef().orderByChild(likesUid).equalTo("thank you for your love");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setTheScreen();



    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.myAccount:
                startActivity(new Intent(MainActivity.this,AboutMe.class));
                break;

            case R.id.random:
                postsNode = FirebaseDatabase.getInstance().getReference().child("loves").orderByChild("randomId");
                setTheScreen();
                Toast.makeText(MainActivity.this,"Posts are randomly sorted",Toast.LENGTH_SHORT).show();
                break;


            case android.R.id.home:
                if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                }else{
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }
                break;

            case R.id.about:
                startActivity(new Intent(MainActivity.this,AboutApp.class));
                break;

            case R.id.sign_out:
                mAuth.signOut();
                break;

            case R.id.myPosts:
                postsNode = FirebaseDatabase.getInstance().getReference().child("posts").orderByChild("uid").equalTo(Uid);
                postsNode.keepSynced(true);
                setTheScreen();
                Toast.makeText(MainActivity.this,"you selected your posts",Toast.LENGTH_SHORT).show();
                break;

            case R.id.likedPosts:

                postsNode = likedPosts;
                setTheScreen();
                Toast.makeText(MainActivity.this,"you selection is Liked Pages",Toast.LENGTH_SHORT).show();
                break;


            default:
                return super.onOptionsItemSelected(item);


        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }




    public void setTheScreen(){
       FirebaseRecyclerAdapter<Post,PostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post,PostViewHolder>(
               Post.class,
               R.layout.post_display_blueprint,
               PostViewHolder.class,
               postsNode
       ) {
           @Override
           public void onDataChanged() {
               if(dialog != null && dialog.isShowing()){
                   dialog.dismiss();
               }
           }

           @Override
           protected void populateViewHolder(final PostViewHolder viewHolder, Post model, int position) {
               final String key = getRef(position).getKey();
               mRefPosts = FirebaseDatabase.getInstance().getReference().child("posts").child(key);
               mLoveNode = mRef.child("loves");
               mRefLoves = mRef.child("loves").child(key);
               viewHolder.setTitle(model.getTitle());
               viewHolder.setLoves(mRefLoves);
               viewHolder.setLoveButton(Uid,key);
               viewHolder.setImage(getApplicationContext(),model.getImageUrl());


               mRefPosts.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {

                       String isAnonymous = dataSnapshot.child("anonymous").getValue(String.class);
                       if (isAnonymous != null && isAnonymous.equals("true")) {
                           viewHolder.anonymousButton.setVisibility(View.VISIBLE);
                       }

                   }
                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });

               viewHolder.heartButton.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {

                       mLoved = true;

                       mLoveNode.addValueEventListener(new ValueEventListener() {
                           @Override
                           public void onDataChange(DataSnapshot dataSnapshot) {

                               if(mLoved){
                                   if(dataSnapshot.child(key).child("likes").hasChild(Uid)){

                                       mLoveNode.child(key).child("likes").child(Uid).removeValue();
                                       mLoved = false;
                                   }else {
                                       mRefPosts = FirebaseDatabase.getInstance().getReference().child("posts").child(key);
                                       mRefPosts.addValueEventListener(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(DataSnapshot dataSnapshot) {
                                               mLoveNode.child(key).child("title").setValue(dataSnapshot.child("title").getValue(String.class));
                                               mLoveNode.child(key).child("content").setValue(dataSnapshot.child("content").getValue(String.class));
                                               mLoveNode.child(key).child("ImageUrl").setValue(dataSnapshot.child("ImageUrl").getValue(String.class));
                                               mLoveNode.child(key).child("uid").setValue(dataSnapshot.child("uid").getValue(String.class));
                                               mLoveNode.child(key).child("name").setValue(dataSnapshot.child("name").getValue(String.class));
                                               mLoveNode.child(key).child("email").setValue(dataSnapshot.child("email").getValue(String.class));
                                               mLoveNode.child(key).child("randomId").setValue(randomize());
                                               mLoveNode.child(key).child("category").setValue(dataSnapshot.child("category").getValue(String.class));
                                           }
                                           @Override
                                           public void onCancelled(DatabaseError databaseError) {                                           }
                                       });
                                       mLoveNode.child(key).child("likes").child(Uid).setValue("thank you for your love");
                                       mLoved = false;
                                   }
                               }

                           }

                           @Override
                           public void onCancelled(DatabaseError databaseError) {

                           }
                       });

                   }
               });


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(MainActivity.this,PostDisplayPageActivity.class);
                        intent.putExtra("Key",key);
                        startActivity(intent);

                    }
                });
           }
       };
       mPostsList.setAdapter(firebaseRecyclerAdapter);

        postsNode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    emptyView.setVisibility(View.VISIBLE);
                }else{
                    emptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        firebaseRecyclerAdapter.notifyDataSetChanged();

   }

    public static class PostViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageButton heartButton,anonymousButton;
        DatabaseReference mLoveNode;

        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            heartButton = (ImageButton)mView.findViewById(R.id.heart_button);
            anonymousButton = (ImageButton)mView.findViewById(R.id.anonymous_button);
            mLoveNode = FirebaseDatabase.getInstance().getReference().child("loves");
        }

        public void setLoves(DatabaseReference mRef){

            final TextView loveCount_textview = (TextView) mView.findViewById(R.id.love_count);
            mRef.child("likes").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long countLove = dataSnapshot.getChildrenCount();
                    String count = String.valueOf(countLove);
                    loveCount_textview.setText(count);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

        }

        public void setLoveButton(final String Uid, final String key){

            mLoveNode.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.child(key).child("likes").hasChild(Uid)){
                        heartButton.setBackgroundResource(R.drawable.heart_filled);
                    }else{
                        heartButton.setBackgroundResource(R.drawable.heart);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setTitle(String title) {
            TextView title_textview = (TextView) mView.findViewById(R.id.title_textview);
            title_textview.setText(title);
        }

        public void setImage(Context ctx,String imageUrl){
            ImageView post_image = (ImageView)mView.findViewById(R.id.blog_image);
            Picasso.with(ctx).load(imageUrl).fit().centerCrop().into(post_image);
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
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mPostsList.getLayoutManager().onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null)
        {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            mPostsList.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }
}
