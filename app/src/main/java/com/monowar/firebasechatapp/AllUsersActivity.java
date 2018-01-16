package com.monowar.firebasechatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.monowar.firebasechatapp.model.Users;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        //mUsersDatabase.keepSynced(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.single_item_all_users,
                UsersViewHolder.class,
                mUsersDatabase
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, final Users model, int position) {
                viewHolder.setProfileImage(AllUsersActivity.this, model.getThumb_image());
                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());

                final String userId = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent userProfileIntent = new Intent(AllUsersActivity.this, UserProfileActivity.class);
                                userProfileIntent.putExtra("EXTRA_USER_ID", userId);
                                startActivity(userProfileIntent);
                            }
                        }, 200);
                    }
                });
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class UsersViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private CircleImageView ivProfileImage;
        private TextView tvDisplayName, tvStatus;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            ivProfileImage = (CircleImageView) itemView.findViewById(R.id.iv_profile_image);
            tvDisplayName = (TextView) itemView.findViewById(R.id.tv_display_name);
            tvStatus = (TextView) itemView.findViewById(R.id.tv_status);
        }

        private void setProfileImage(final Context context, final String image) {
            Picasso.with(context).load(image).placeholder(R.drawable.no_avatar).error(R.drawable.no_avatar)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(ivProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            //Try again online if cache failed
                            Picasso.with(context).load(image).placeholder(R.drawable.no_avatar).error(R.drawable.no_avatar)
                                    .into(ivProfileImage);
                        }
                    });
        }

        private void setName(String name) {
            tvDisplayName.setText(name);
        }

        private void setStatus(String status) {
            tvStatus.setText(status);
        }
    }
}
