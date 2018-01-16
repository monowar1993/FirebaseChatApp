package com.monowar.firebasechatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSettingsActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;

    private RelativeLayout layoutRoot;
    private CircleImageView ivProfileImage;
    private TextView tvDisplayName, tvStatus;
    private Button buttonChangeImage, buttonChangeStatus;

    private ProgressDialog progressDialog;

    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorageReference;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        layoutRoot = (RelativeLayout) findViewById(R.id.layout_root);
        ivProfileImage = (CircleImageView) findViewById(R.id.iv_profile_image);
        tvDisplayName = (TextView) findViewById(R.id.tv_display_name);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        buttonChangeImage = (Button) findViewById(R.id.button_change_image);
        buttonChangeStatus = (Button) findViewById(R.id.button_change_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = mCurrentUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mDatabase.keepSynced(true);

        mStorageReference = FirebaseStorage.getInstance().getReference();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("AccountSettingsActivity", dataSnapshot.toString());
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.no_avatar).error(R.drawable.no_avatar)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(ivProfileImage, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                //Try again online if cache failed
                                Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.no_avatar).error(R.drawable.no_avatar)
                                        .into(ivProfileImage);
                            }
                        });

                tvDisplayName.setText(name);
                tvStatus.setText(status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showSnackBar(databaseError.getMessage());
            }
        });

        buttonChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

                /*
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AccountSettingsActivity.this);
                        */
            }
        });

        buttonChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String statusValue = tvStatus.getText().toString();
                Intent changeStatusIntent = new Intent(AccountSettingsActivity.this, ChangeStatusActivity.class);
                changeStatusIntent.putExtra("EXTRA_STATUS_VALUE", statusValue);
                startActivity(changeStatusIntent);
            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            Log.e("AccountSettingsActivity", "ImageUri " + imageUri);
            CropImage.activity(imageUri)
                    .setActivityTitle(getString(R.string.title_activity_crop_image))
                    .setMinCropWindowSize(500, 500)
                    .setAspectRatio(1, 1)
                    .start(AccountSettingsActivity.this);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                progressDialog = new ProgressDialog(AccountSettingsActivity.this);
                progressDialog.setMessage(getString(R.string.please_wait));
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();
                Log.e("AccountSettingsActivity", "ImageUri After Crop " + resultUri);

                File imageFile = new File(resultUri.getPath());
                Bitmap thumbImage;
                try {
                    thumbImage = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(imageFile);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumbImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumbImageData = baos.toByteArray();
                    final StorageReference thumbImageRef = mStorageReference.child("profile_images").child("thumbs").child(uid + "_dp.jpg");

                    StorageReference imageRef = mStorageReference.child("profile_images").child(uid + "_dp.jpg");
                    imageRef.putFile(resultUri).addOnCompleteListener(AccountSettingsActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                final String imageDownloadURL = task.getResult().getDownloadUrl().toString();

                                UploadTask uploadTask = thumbImageRef.putBytes(thumbImageData);
                                uploadTask.addOnCompleteListener(AccountSettingsActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                        if (thumb_task.isSuccessful()) {
                                            String thumbImageDownloadURL = thumb_task.getResult().getDownloadUrl().toString();

                                            Map<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("image", imageDownloadURL);
                                            hashMap.put("thumb_image", thumbImageDownloadURL);

                                            mDatabase.updateChildren(hashMap).addOnCompleteListener(AccountSettingsActivity.this, new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        if (progressDialog != null && progressDialog.isShowing()) {
                                                            progressDialog.dismiss();
                                                        }
                                                    } else {
                                                        if (progressDialog != null && progressDialog.isShowing()) {
                                                            progressDialog.dismiss();
                                                        }
                                                        showSnackBar(task.getException().getMessage());
                                                    }
                                                }
                                            });
                                        } else {
                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }
                                            showSnackBar(thumb_task.getException().getMessage());
                                        }
                                    }
                                });
                            } else {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                showSnackBar(task.getException().getMessage());
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                showSnackBar(error.getMessage());
            }
        }
    }

    private void showSnackBar(String message) {
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
}
