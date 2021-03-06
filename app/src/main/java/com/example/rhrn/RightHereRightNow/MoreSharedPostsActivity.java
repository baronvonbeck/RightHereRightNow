package com.example.rhrn.RightHereRightNow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.rhrn.RightHereRightNow.firebase_entry.Post;
import com.example.rhrn.RightHereRightNow.firebase_entry.Shares;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MoreSharedPostsActivity extends AppCompatActivity {
    public ImageButton backButton, options;
    public TextView postTitle;
    public ListView postList;
    public ArrayList<Post> postArrayList;
    public SharingAdapters.SharedPostAdapter postAdapter;
    public ProgressBar loadMorePosts;
    public int loadPosts = 0;
    public int scrollCount = 0;
    public boolean isOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_shared_posts);
        backButton = (ImageButton) findViewById(R.id.back_button);
        postTitle = (TextView) findViewById(R.id.profile_name_chat);
        postList = (ListView) findViewById(R.id.user_all_shared_posts);
        postArrayList = new ArrayList<>();
        isOwner = FirebaseAuth.getInstance().getCurrentUser().getUid().equals(getIntent().getStringExtra("userKey"));
        options = (ImageButton) findViewById(R.id.profile_app_bar_options);
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu();
            }
        });
        //postAdapter = new NotificationFragment.PostAdapter(MoreSharedPostsActivity.this, postArrayList);
        //postList.setAdapter(postAdapter);
        //loadMorePosts = (ProgressBar) LayoutInflater.from(this).inflate(R.layout.progress_bar, null);
        //findViewById(R.id.load_more_posts);
        View mProgressBarFooter = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.progress_bar, null, false);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        postList.addFooterView(mProgressBarFooter);
        postList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (findViewById(R.id.load_more_posts).isShown())
                    loadPosts = 0;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (loadPosts == 0) {
                    loadPosts = 1;
                    scrollCount++;
                    postArrayList = new ArrayList<Post>();
                    try {
                        getUserPosts(scrollCount * 25);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //postAdapter.notifyDataSetChanged();
                }
            }
        });


    }

    public void getUserPosts(final int n) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Shares").child(getIntent().getStringExtra("userKey"));
        ref.orderByChild("type").equalTo("Post").limitToLast(n).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Shares share = dataSnapshot1.getValue(Shares.class);
                   final String key = dataSnapshot1.getKey();
                    final String id = share.id;

                    FirebaseDatabase.getInstance().getReference().child("Post")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(id)){
                                        Post.requestPost(id, "auth", new Post.PostReceivedListener() {
                                            @Override
                                            public void onPostReceived(Post... posts) {
                                                android.util.Log.e("nat", posts[0].ownerID);
                                                postArrayList.add(posts[0]);
                                                android.util.Log.e("nat", String.valueOf(postArrayList.size()));
                                                postAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                    else{
                                        FirebaseDatabase.getInstance().getReference().child("Shares").child(getIntent()
                                                .getStringExtra("userKey")).child(key).removeValue(); // delete if has been deleted.
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                }

                    try{postTitle.setText(postArrayList.get(0).DisplayName + "'s Posts");}catch (Exception e){}
                    postAdapter = new SharingAdapters.SharedPostAdapter(MoreSharedPostsActivity.this, postArrayList, isOwner);
                    android.util.Log.e("nat", String.valueOf(postAdapter.getCount()));
                    postList.setAdapter(postAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void popupMenu() {
        PopupMenu popup = new PopupMenu(MoreSharedPostsActivity.this, options);
        popup.getMenuInflater().inflate(R.menu.other_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.logout) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear all activities above it
                    startActivity(intent);
                    finish();
                    return true;
                } else {
                    return onMenuItemClick(item);
                }
            }
        });
        popup.show();
    }
}
