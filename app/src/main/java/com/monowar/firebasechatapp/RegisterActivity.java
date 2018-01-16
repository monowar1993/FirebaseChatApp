package com.monowar.firebasechatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private CoordinatorLayout layoutRoot;
    private TextInputLayout tilDisplayName, tilEmail, tilPassword;
    private Button buttonCreateAccount;

    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        layoutRoot = (CoordinatorLayout) findViewById(R.id.layout_root);
        tilDisplayName = (TextInputLayout) findViewById(R.id.til_display_name);
        tilEmail = (TextInputLayout) findViewById(R.id.til_email);
        tilPassword = (TextInputLayout) findViewById(R.id.til_password);
        buttonCreateAccount = (Button) findViewById(R.id.button_create_account);

        mAuth = FirebaseAuth.getInstance();

        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.hideSoftKeyboard(view, RegisterActivity.this);

                progressDialog = new ProgressDialog(RegisterActivity.this);
                progressDialog.setMessage(getString(R.string.please_wait));
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                tilDisplayName.setErrorEnabled(false);
                tilEmail.setErrorEnabled(false);
                tilPassword.setErrorEnabled(false);

                String displayName = tilDisplayName.getEditText().getText().toString();
                String email = tilEmail.getEditText().getText().toString();
                String password = tilPassword.getEditText().getText().toString();

                boolean hasError = false;
                boolean hasEmailError = false;
                boolean hasPasswordError = false;
                if (TextUtils.isEmpty(displayName)) {
                    tilDisplayName.setError(getString(R.string.error_display_name));
                    hasError = true;
                }
                if (TextUtils.isEmpty(email)) {
                    tilEmail.setError(getString(R.string.error_display_name));
                    hasError = true;
                }
                if (TextUtils.isEmpty(email)) {
                    tilEmail.setError(getString(R.string.error_email));
                    hasEmailError = true;
                    hasError = true;
                }
                if (!hasEmailError && !email.contains("@")) {
                    tilEmail.setError(getString(R.string.error_valid_email));
                    hasError = true;
                }
                if (TextUtils.isEmpty(password)) {
                    tilPassword.setError(getString(R.string.error_password));
                    hasPasswordError = true;
                    hasError = true;
                }
                if (!hasPasswordError && password.length() < 4) {
                    tilPassword.setError(getString(R.string.error_valid_password));
                    hasError = true;
                }
                if (hasError) {
                    progressDialog.dismiss();
                    return;
                }

                registerUser(displayName, email, password);
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

    private void registerUser(final String displayName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser != null) {
                                String uid = currentUser.getUid();
                                mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                Log.d("LoginActivity", "Device Token: " + deviceToken);

                                HashMap<String, String> hashMap = new HashMap<String, String>();
                                hashMap.put("name", displayName);
                                hashMap.put("status", "Hi there, I'm using firebase chat app!!");
                                hashMap.put("image", "default");
                                hashMap.put("thumb_image", "default");
                                hashMap.put("device_token", deviceToken);
                                mDatabase.setValue(hashMap).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }
                                            Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(mainIntent);
                                            finish();
                                        } else {
                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }
                                            showSnackBar(task.getException().getMessage());
                                        }
                                    }
                                });
                            }
                        } else {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            showSnackBar(task.getException().getMessage());
                        }
                    }
                });
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
