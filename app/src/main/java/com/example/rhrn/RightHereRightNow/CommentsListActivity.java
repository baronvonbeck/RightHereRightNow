package com.example.rhrn.RightHereRightNow;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rhrn.RightHereRightNow.firebase_entry.Comments;
import com.example.rhrn.RightHereRightNow.firebase_entry.Event;
import com.example.rhrn.RightHereRightNow.firebase_entry.Post;
import com.example.rhrn.RightHereRightNow.firebase_entry.User;
import com.example.rhrn.RightHereRightNow.util.CircleTransform;
import com.firebase.client.ServerValue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.example.rhrn.RightHereRightNow.NotificationFragment.app;
import static com.example.rhrn.RightHereRightNow.R.id.comment_options;
import static com.example.rhrn.RightHereRightNow.R.id.view;
import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * Created by NatSand on 4/25/17.
 */

public class CommentsListActivity extends FragmentActivity {

    private Button newComment;
    private Button backButton;
    private Button postButton;
    private CheckBox anon;
    private EditText content;
    private ListView mListView;
    private ArrayList<Comments> mComments;
    private commentsAdapter mAdapter;
    public App mApp;

    String postID;
    String type;





    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postID = getIntent().getStringExtra("postID");
        type = getIntent().getStringExtra("type");
        setContentView(R.layout.comment_list);

        mApp = (App) getApplicationContext();
        mComments = new ArrayList<>();
        mListView = (ListView) findViewById(R.id.comment_list_view);
        mAdapter = new commentsAdapter(getBaseContext(), mComments, type);
        mListView.setAdapter(mAdapter);
        anon = (CheckBox) findViewById(R.id.comment_anonymous_check);
        content = (EditText) findViewById(R.id.Comment_content);

        postButton = (Button) findViewById(R.id.Comment_post_button);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.clear();
                String temp = content.getText().toString();
                temp = temp.trim();
                if (temp.length() > 0) {
                    Comments comment = createComment(FirebaseAuth.getInstance().getCurrentUser().getUid(), postID, temp, 0, null, anon.isChecked());
                    if(type.equals("Event"))
                        Event.changeCount("comments", postID, true);
                    else if(type.equals("Post"))
                        Post.changeCount("comments", postID, true);
                        getComments(postID, true);
                } else {
                    Toast.makeText(getApplicationContext(), "Please Enter Comment", Toast.LENGTH_SHORT).show();
                }
            }
        });
        backButton = (Button) findViewById(R.id.comment_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getComments(postID, true);
    }


    public void getComments(String postID, boolean first) {
        Toast.makeText(getApplicationContext(), "Got here", Toast.LENGTH_LONG).show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Comments").child(postID);
        ref.orderByChild("timestamp_create");
        if (first) {
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                        Comments comment = commentSnapshot.getValue(Comments.class);
                        mComments.add(comment);
                        mAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

        } else {
            ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Comments comment = dataSnapshot.getValue(Comments.class);
                    mAdapter.add(comment);
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {}

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }


    }

    public Comments createComment(String userID, String postID, String Content, int Order, String responseID, boolean anon) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference rootreference = FirebaseDatabase.getInstance().getReference("Comments").child(postID);
        DatabaseReference reference;
        if (Order == 0) {
            reference = rootRef.child("comment").push();
        } else {
            reference = rootRef.child("comment").child(postID).child(responseID).push();
        }
        String key = reference.getKey();

        DatabaseReference createdComment = FirebaseDatabase.getInstance().getReference("Comments").child(postID).child(key);
        Comments Result = new Comments(userID, key, Content, postID, Order, 0, 0, anon, ServerValue.TIMESTAMP);
        createdComment.setValue(Result);
        createdComment.child("timestamp_create").setValue(ServerValue.TIMESTAMP);

        return Result;
    }

    public static class commentsAdapter extends ArrayAdapter<Comments> {
        final String currUsr = FirebaseAuth.getInstance().getCurrentUser().getUid();
        int commentDeleted = 0;
        String type;

        commentsAdapter(Context context, ArrayList<Comments> commentses, final String Type) {
            super(context, R.layout.comment_post_display, R.id.comment_text, commentses);
            type = Type;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            final Comments comment = getItem(position);
            TextView commentText = (TextView) convertView.findViewById(R.id.comment_text);
            final TextView displayName = (TextView) convertView.findViewById(R.id.comment_simp_user_name);
            final TextView handle = (TextView) convertView.findViewById(R.id.comment_simp_user_handle);
            final ImageView miniProfilePicture = (ImageView) convertView.findViewById(R.id.comment_simp_user_image);
            final ImageView commentOptions = (ImageView) convertView.findViewById(R.id.comment_options);

            if (comment.isAnon == true) {
                displayName.setText("Anonymous");
                handle.setText("");
                Picasso.with(getContext()).load(R.drawable.happy).transform(new CircleTransform()).into(miniProfilePicture);
                miniProfilePicture.setClickable(false);
                displayName.setClickable(false);
                handle.setClickable(false);
            } else {
                try {
                    User.requestUser(comment.ownerID.toString(), "auth", new User.UserReceivedListener() {
                        @Override
                        public void onUserReceived(User... users) {
                            User usr = users[0];

                            displayName.setText(usr.DisplayName);
                            handle.setText(usr.handle);
                            try {
                                if (usr.ProfilePicture != null)
                                    //Convert the URL to aa Bitmap using function, then set the profile picture
                                    Picasso.with(getContext()).load(usr.ProfilePicture).transform(new CircleTransform()).into(miniProfilePicture);
                                else
                                    Picasso.with(getContext()).load(R.drawable.happy).transform(new CircleTransform()).into(miniProfilePicture);
                            } catch (Exception e) {
                            }
                        }
                    });
                } catch (Exception e) {
                }
            }

            commentText.setText(comment.content);

            if(!comment.isAnon) {
                displayName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ViewUserActivity.class);
                        intent.putExtra("otherUserID", comment.ownerID);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                    }
                });
                miniProfilePicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ViewUserActivity.class);
                        intent.putExtra("otherUserID", comment.ownerID);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                    }
                });
            }

            final View view = convertView;
            commentOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMenu(view, comment.ownerID, comment.responseID, comment.commentID, type);
                }
            });

            return convertView;
        }


        public void popupMenu(View view, final String ownerID, final String responseID, final String commentID, final String type) {
            ImageView options = (ImageView) view.findViewById(R.id.comment_options);
            options = (ImageView) view.findViewById(R.id.comment_options);
            final PopupMenu popup = new PopupMenu(view.getContext(), options);
            popup.getMenuInflater().inflate(R.menu.comment_options, popup.getMenu());
            if (String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getUid()).equals(ownerID))
                popup.getMenu().findItem(R.id.delete_comment).setVisible(true);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    int i = item.getItemId();
                    if (i == R.id.delete_comment) {
                        promptDelete(ownerID, responseID, commentID, type);
                        return true;
                    }
                    if (i == R.id.report_comment) {
                        Toast.makeText(getContext(), "Reporting Comment...", Toast.LENGTH_SHORT).show();
                        reportEvent(ownerID, responseID, commentID, type);
                        return true;
                    } else {
                        return onMenuItemClick(item);
                    }
                }
            });
            popup.show();
        }

        public void promptDelete(final String ownerID, final String responseID, final String commentID, final String type) {
            android.support.v7.app.AlertDialog.Builder dlgAlert = new android.support.v7.app.AlertDialog.Builder(getContext());
            dlgAlert.setMessage("Are you sure you want to unshare this event? This action cannot be undone!");
            dlgAlert.setTitle("Unshare Event?");

            dlgAlert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    commentDeleted = 1;
                    //Perform delete
                    Toast.makeText(getContext(), "Deleting Comment...", Toast.LENGTH_SHORT).show();
                    FirebaseDatabase.getInstance().getReference().child("Comments").child(responseID).child(commentID).removeValue();
                    Toast.makeText(getContext(), "Comment Deleted!", Toast.LENGTH_SHORT).show();
                    if(type.equals("Post")){
                        Post.changeCount("comments", responseID, false);
                    }
                    else if(type.equals("Event")){
                        Event.changeCount("comments", responseID, false);

                    }
                    //TODO: update likes received...
                }
            });

            //if user cancels
            dlgAlert.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });

            dlgAlert.setCancelable(true);
            dlgAlert.create();
            dlgAlert.show();
        }

        public void reportEvent(final String ownerID, final String responseID, final String commentID, final String type) {
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Comments");
            ref.child(responseID).child(commentID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if ((String) dataSnapshot.child("description").getValue() == null) return;
                    else {
                        if (!dataSnapshot.child("numberOfReports").exists())
                            ref.child(responseID).child(commentID).child("numberOfReports").setValue(0);
                        else {
                            long numberOfReports = (long) dataSnapshot.child("numberOfReports").getValue();
                            //parse whitespace
                            String[] content = ((String) dataSnapshot.child("description").getValue()).split("\\s+");
                            if (hasBadWord(content)) {
                                numberOfReports++;
                                ref.child(responseID).child(commentID).child("numberOfReports").setValue(numberOfReports);
                                //TODO: set the amount of reports before a event is deleted
                                if (numberOfReports > 5) {
                                    FirebaseDatabase.getInstance().getReference().child("Comments").child(responseID).child(commentID).removeValue();
                                    if(type.equals("Post")){
                                        Post.changeCount("comments", responseID, false);
                                    }
                                    else if(type.equals("Event")){
                                        Event.changeCount("comments", responseID, false);

                                    }

                                }
                            } //Has bad word
                        }//else number of reports exists
                    }//else event has content
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public boolean hasBadWord(String[] content) {
            for(String c : content) {
                for (String badWord : app.badWords) {
                    c = c.toLowerCase();
                    if (c.contains(badWord)) {
                        Toast.makeText(getContext(), "Comment has been reported.", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
            }
            Toast.makeText(getContext(), "There is nothing to report.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
