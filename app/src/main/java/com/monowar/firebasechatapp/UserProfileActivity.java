package com.monowar.firebasechatapp;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";

    private RelativeLayout layoutRoot;
    private ImageView ivProfileImage;
    private TextView tvDisplayName, tvStatus, tvFriendsCount;
    private Button buttonSendFriendRequest, buttonDecline;

    private ProgressDialog progressDialog;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase, mUserDatabase, mFriendRequestsDatabase, mFriendsDatabase;

    private int friendsCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        layoutRoot = (RelativeLayout) findViewById(R.id.layout_root);
        ivProfileImage = (ImageView) findViewById(R.id.iv_profile_image);
        tvDisplayName = (TextView) findViewById(R.id.tv_display_name);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        tvFriendsCount = (TextView) findViewById(R.id.tv_friends_count);
        buttonSendFriendRequest = (Button) findViewById(R.id.button_send_friend_request);
        buttonDecline = (Button) findViewById(R.id.button_decline_friend_request);

        progressDialog = new ProgressDialog(UserProfileActivity.this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        friendsCurrentState = App.NOT_FRIEND;

        final String userId = getIntent().getStringExtra("EXTRA_USER_ID");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mFriendRequestsDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_requests");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
//        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("UserProfileActivity", dataSnapshot.toString());
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                Picasso.with(getApplicationContext())
                        .load(image)
                        .placeholder(R.drawable.no_avatar)
                        .error(R.drawable.no_avatar)
                        .into(ivProfileImage);

                tvDisplayName.setText(name);
                tvStatus.setText(status);

                mFriendRequestsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(userId)) {
                            String request_type = dataSnapshot.child(userId).child("request_type").getValue().toString();
                            if (request_type.equalsIgnoreCase("received")) {
                                Log.e(TAG, "received");
                                friendsCurrentState = App.FRIEND_REQUEST_RECEIVED;
                                buttonSendFriendRequest.setText(R.string.accept_friend_request);
                                buttonDecline.setVisibility(View.VISIBLE);
                            } else if (request_type.equalsIgnoreCase("sent")) {
                                Log.e(TAG, "sent");
                                friendsCurrentState = App.FRIEND_REQUEST_SENT;
                                buttonSendFriendRequest.setText(R.string.cancel_friend_request);
                                buttonDecline.setVisibility(View.GONE);
                            }

                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        } else {
                            mFriendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(userId)) {
                                        Log.e(TAG, "friends");
                                        friendsCurrentState = App.FRIENDS;
                                        buttonSendFriendRequest.setText(R.string.unfriend);
                                        buttonDecline.setVisibility(View.GONE);
                                    }

                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                    showSnackBarError(databaseError.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        showSnackBarError(databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                showSnackBarError(databaseError.getMessage());
            }
        });

//        buttonSendFriendRequest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                buttonSendFriendRequest.setEnabled(false);
//                buttonSendFriendRequest.setAlpha((float) 0.2);
//
//                if (friendsCurrentState == App.NOT_FRIEND) {
//                    progressDialog = new ProgressDialog(UserProfileActivity.this);
//                    progressDialog.setMessage(getString(R.string.please_wait));
//                    progressDialog.setCanceledOnTouchOutside(false);
//                    progressDialog.show();
//
//                    mFriendRequestsDatabase.child(mCurrentUser.getUid()).child(userId).child("request_type").setValue("sent").addOnCompleteListener(UserProfileActivity.this, new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()) {
//                                mFriendRequestsDatabase.child(userId).child(mCurrentUser.getUid()).child("request_type").setValue("received").addOnCompleteListener(UserProfileActivity.this, new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
//                                            HashMap<String, String> notificationData = new HashMap<String, String>();
//                                            notificationData.put("from", mCurrentUser.getUid());
//                                            notificationData.put("type", "friend_request");
//                                            mNotificationDatabase.child(userId).push().setValue(notificationData).addOnCompleteListener(UserProfileActivity.this, new OnCompleteListener<Void>() {
//                                                @Override
//                                                public void onComplete(@NonNull Task<Void> task) {
//                                                    if (task.isSuccessful()) {
//                                                        friendsCurrentState = App.FRIEND_REQUEST_SENT;
//                                                        buttonSendFriendRequest.setText(R.string.cancel_friend_request);
//                                                        buttonDecline.setVisibility(View.GONE);
//
//                                                        showSnackBarSuccess(getString(R.string.friend_request_sent));
//                                                    } else {
//                                                        showSnackBarError(task.getException().getMessage());
//                                                    }
//                                                }
//                                            });
//                                        } else {
//                                            showSnackBarError(task.getException().getMessage());
//                                        }
//                                    }
//                                });
//                            } else {
//                                showSnackBarError(task.getException().getMessage());
//                            }
//                        }
//                    });
//
//                    buttonSendFriendRequest.setEnabled(true);
//                    buttonSendFriendRequest.setAlpha((float) 1);
//
//                    if (progressDialog != null && progressDialog.isShowing()) {
//                        progressDialog.dismiss();
//                    }
//                } else if (friendsCurrentState == App.FRIEND_REQUEST_SENT) {
//                    progressDialog = new ProgressDialog(UserProfileActivity.this);
//                    progressDialog.setMessage(getString(R.string.please_wait));
//                    progressDialog.setCanceledOnTouchOutside(false);
//                    progressDialog.show();
//
//                    mFriendRequestsDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnCompleteListener(UserProfileActivity.this, new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()) {
//                                mFriendRequestsDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(UserProfileActivity.this, new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
//                                            friendsCurrentState = App.NOT_FRIEND;
//                                            buttonSendFriendRequest.setText(R.string.send_friend_request);
//                                            buttonDecline.setVisibility(View.GONE);
//
//                                            showSnackBarSuccess(getString(R.string.friend_request_canceled));
//                                        } else {
//                                            showSnackBarError(task.getException().getMessage());
//                                        }
//                                    }
//                                });
//                            } else {
//                                showSnackBarError(task.getException().getMessage());
//                            }
//                        }
//                    });
//
//                    buttonSendFriendRequest.setEnabled(true);
//                    buttonSendFriendRequest.setAlpha((float) 1);
//
//                    if (progressDialog != null && progressDialog.isShowing()) {
//                        progressDialog.dismiss();
//                    }
//                } else if (friendsCurrentState == App.FRIEND_REQUEST_RECEIVED) {
//                    progressDialog = new ProgressDialog(UserProfileActivity.this);
//                    progressDialog.setMessage(getString(R.string.please_wait));
//                    progressDialog.setCanceledOnTouchOutside(false);
//                    progressDialog.show();
//
//                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
//                    mFriendsDatabase.child(mCurrentUser.getUid()).child(userId).setValue(currentDate).addOnCompleteListener(UserProfileActivity.this, new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()) {
//                                mFriendsDatabase.child(userId).child(mCurrentUser.getUid()).setValue(currentDate).addOnCompleteListener(UserProfileActivity.this, new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
//                                            mFriendRequestsDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnCompleteListener(UserProfileActivity.this, new OnCompleteListener<Void>() {
//                                                @Override
//                                                public void onComplete(@NonNull Task<Void> task) {
//                                                    if (task.isSuccessful()) {
//                                                        mFriendRequestsDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(UserProfileActivity.this, new OnCompleteListener<Void>() {
//                                                            @Override
//                                                            public void onComplete(@NonNull Task<Void> task) {
//                                                                if (task.isSuccessful()) {
//                                                                    friendsCurrentState = App.FRIENDS;
//                                                                    buttonSendFriendRequest.setText(R.string.unfriend);
//                                                                    buttonDecline.setVisibility(View.GONE);
//
//                                                                    showSnackBarSuccess(getString(R.string.friend_request_accepted));
//                                                                } else {
//                                                                    showSnackBarError(task.getException().getMessage());
//                                                                }
//                                                            }
//                                                        });
//                                                    } else {
//                                                        showSnackBarError(task.getException().getMessage());
//                                                    }
//                                                }
//                                            });
//                                        } else {
//                                            showSnackBarError(task.getException().getMessage());
//                                        }
//                                    }
//                                });
//                            } else {
//                                showSnackBarError(task.getException().getMessage());
//                            }
//                        }
//                    });
//
//                    buttonSendFriendRequest.setEnabled(true);
//                    buttonSendFriendRequest.setAlpha((float) 1);
//
//                    if (progressDialog != null && progressDialog.isShowing()) {
//                        progressDialog.dismiss();
//                    }
//                } else if (friendsCurrentState == App.FRIENDS) {
//                    progressDialog = new ProgressDialog(UserProfileActivity.this);
//                    progressDialog.setMessage(getString(R.string.please_wait));
//                    progressDialog.setCanceledOnTouchOutside(false);
//                    progressDialog.show();
//
//                    mFriendsDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnCompleteListener(UserProfileActivity.this, new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()) {
//                                mFriendsDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(UserProfileActivity.this, new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
//                                            friendsCurrentState = App.NOT_FRIEND;
//                                            buttonSendFriendRequest.setText(R.string.send_friend_request);
//                                            buttonDecline.setVisibility(View.GONE);
//
//                                            showSnackBarSuccess(getString(R.string.unfriend));
//                                        } else {
//                                            showSnackBarError(task.getException().getMessage());
//                                        }
//                                    }
//                                });
//                            } else {
//                                showSnackBarError(task.getException().getMessage());
//                            }
//                        }
//                    });
//
//                    buttonSendFriendRequest.setEnabled(true);
//                    buttonSendFriendRequest.setAlpha((float) 1);
//
//                    if (progressDialog != null && progressDialog.isShowing()) {
//                        progressDialog.dismiss();
//                    }
//                }
//            }
//        });

        buttonSendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSendFriendRequest.setEnabled(false);
                buttonSendFriendRequest.setAlpha((float) 0.2);

                if (friendsCurrentState == App.NOT_FRIEND) {
                    progressDialog = new ProgressDialog(UserProfileActivity.this);
                    progressDialog.setMessage(getString(R.string.please_wait));
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    DatabaseReference notificationDatabase = mDatabase.child("Notifications").child(userId).push();
                    String notificationId = notificationDatabase.getKey();

                    HashMap<String, String> notificationData = new HashMap<String, String>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "friend_request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_requests/" + mCurrentUser.getUid() + "/" + userId + "/request_type", "sent");
                    requestMap.put("Friend_requests/" + userId + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    requestMap.put("Notifications/" + userId + "/" + notificationId, notificationData);

                    mDatabase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                friendsCurrentState = App.FRIEND_REQUEST_SENT;
                                buttonSendFriendRequest.setText(R.string.cancel_friend_request);
                                buttonDecline.setVisibility(View.GONE);

                                showSnackBarSuccess(getString(R.string.friend_request_sent));
                            } else {
                                showSnackBarError(databaseError.getMessage());
                            }
                        }
                    });

                    buttonSendFriendRequest.setEnabled(true);
                    buttonSendFriendRequest.setAlpha((float) 1);

                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                } else if (friendsCurrentState == App.FRIEND_REQUEST_SENT) {
                    progressDialog = new ProgressDialog(UserProfileActivity.this);
                    progressDialog.setMessage(getString(R.string.please_wait));
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    mFriendRequestsDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnCompleteListener(UserProfileActivity.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mFriendRequestsDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(UserProfileActivity.this, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            friendsCurrentState = App.NOT_FRIEND;
                                            buttonSendFriendRequest.setText(R.string.send_friend_request);
                                            buttonDecline.setVisibility(View.GONE);

                                            showSnackBarSuccess(getString(R.string.friend_request_canceled));
                                        } else {
                                            showSnackBarError(task.getException().getMessage());
                                        }
                                    }
                                });
                            } else {
                                showSnackBarError(task.getException().getMessage());
                            }
                        }
                    });

                    buttonSendFriendRequest.setEnabled(true);
                    buttonSendFriendRequest.setAlpha((float) 1);

                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                } else if (friendsCurrentState == App.FRIEND_REQUEST_RECEIVED) {
                    progressDialog = new ProgressDialog(UserProfileActivity.this);
                    progressDialog.setMessage(getString(R.string.please_wait));
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + userId + "/date", currentDate);
                    friendsMap.put("Friends/" + userId + "/" + mCurrentUser.getUid() + "/date", currentDate);
                    friendsMap.put("Friend_requests/" + mCurrentUser.getUid() + "/" + userId, null);
                    friendsMap.put("Friend_requests/" + userId + "/" + mCurrentUser.getUid(), null);

                    mDatabase.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                friendsCurrentState = App.FRIENDS;
                                buttonSendFriendRequest.setText(R.string.unfriend);
                                buttonDecline.setVisibility(View.GONE);

                                showSnackBarSuccess(getString(R.string.friend_request_accepted));
                            } else {
                                showSnackBarError(databaseError.getMessage());
                            }
                        }
                    });

                    buttonSendFriendRequest.setEnabled(true);
                    buttonSendFriendRequest.setAlpha((float) 1);

                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                } else if (friendsCurrentState == App.FRIENDS) {
                    progressDialog = new ProgressDialog(UserProfileActivity.this);
                    progressDialog.setMessage(getString(R.string.please_wait));
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    Map unfriendsMap = new HashMap();
                    unfriendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + userId, null);
                    unfriendsMap.put("Friends/" + userId + "/" + mCurrentUser.getUid(), null);

                    mDatabase.updateChildren(unfriendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                friendsCurrentState = App.NOT_FRIEND;
                                buttonSendFriendRequest.setText(R.string.send_friend_request);
                                buttonDecline.setVisibility(View.GONE);

                                showSnackBarSuccess(getString(R.string.unfriend));
                            } else {
                                showSnackBarError(databaseError.getMessage());
                            }
                        }
                    });

                    buttonSendFriendRequest.setEnabled(true);
                    buttonSendFriendRequest.setAlpha((float) 1);

                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }
        });

//        buttonDecline.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                progressDialog = new ProgressDialog(UserProfileActivity.this);
//                progressDialog.setMessage(getString(R.string.please_wait));
//                progressDialog.setCanceledOnTouchOutside(false);
//                progressDialog.show();
//
//                mFriendRequestsDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnCompleteListener(UserProfileActivity.this, new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            mFriendRequestsDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(UserProfileActivity.this, new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (task.isSuccessful()) {
//                                        friendsCurrentState = App.NOT_FRIEND;
//                                        buttonSendFriendRequest.setText(R.string.send_friend_request);
//                                        buttonDecline.setVisibility(View.GONE);
//
//                                        showSnackBarSuccess(getString(R.string.friend_request_canceled));
//                                    } else {
//                                        showSnackBarError(task.getException().getMessage());
//                                    }
//                                }
//                            });
//                        } else {
//                            showSnackBarError(task.getException().getMessage());
//                        }
//                    }
//                });
//
//                buttonSendFriendRequest.setEnabled(true);
//                buttonSendFriendRequest.setAlpha((float) 1);
//
//                if (progressDialog != null && progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//            }
//        });

        buttonDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSendFriendRequest.setEnabled(false);
                buttonDecline.setEnabled(false);
                buttonDecline.setAlpha((float) 0.2);

                progressDialog = new ProgressDialog(UserProfileActivity.this);
                progressDialog.setMessage(getString(R.string.please_wait));
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Map declineRequestMap = new HashMap();
                declineRequestMap.put("Friend_requests/" + mCurrentUser.getUid() + "/" + userId, null);
                declineRequestMap.put("Friend_requests/" + userId + "/" + mCurrentUser.getUid(), null);

                mDatabase.updateChildren(declineRequestMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            friendsCurrentState = App.NOT_FRIEND;
                            buttonSendFriendRequest.setText(R.string.send_friend_request);
                            buttonDecline.setVisibility(View.GONE);

                            showSnackBarSuccess(getString(R.string.friend_request_canceled));
                        } else {
                            showSnackBarError(databaseError.getMessage());
                        }
                    }
                });

                buttonSendFriendRequest.setEnabled(true);
                buttonDecline.setEnabled(true);
                buttonDecline.setAlpha((float) 1);

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void showSnackBarError(String message) {
        final Snackbar snackbar = Snackbar.make(layoutRoot, message, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        snackbar.setAction(R.string.dismiss, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }

    private void showSnackBarSuccess(String message) {
        final Snackbar snackbar = Snackbar.make(layoutRoot, message, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        snackbar.setAction(R.string.dismiss, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.GREEN);
        snackbar.show();
    }
}
