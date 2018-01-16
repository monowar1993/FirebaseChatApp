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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.monowar.firebasechatapp.utils.ExtendedEditText;

public class LoginActivity extends AppCompatActivity {

    private CoordinatorLayout layoutRoot;
    private TextInputLayout tilEmail, tilPassword;
    private Button buttonLogin;

    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        layoutRoot = (CoordinatorLayout) findViewById(R.id.layout_root);
        tilEmail = (TextInputLayout) findViewById(R.id.til_email);
        tilPassword = (TextInputLayout) findViewById(R.id.til_password);
        buttonLogin = (Button) findViewById(R.id.button_login);

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.hideSoftKeyboard(view, LoginActivity.this);

                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage(getString(R.string.please_wait));
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                tilEmail.setErrorEnabled(false);
                tilPassword.setErrorEnabled(false);

                String email = tilEmail.getEditText().getText().toString();
                String password = tilPassword.getEditText().getText().toString();

                boolean hasError = false;
                boolean hasEmailError = false;
                boolean hasPasswordError = false;
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

                login(email, password);
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

    private void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            Log.i("LoginActivity", "Device Token: " + deviceToken);
                            mUserDatabase.child(mAuth.getCurrentUser().getUid()).child("device_token").setValue(deviceToken).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            });
                        } else {
                            String error;
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                error = getString(R.string.error_weak_password);
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                error = getString(R.string.error_invalid_email_password);
                            } catch (FirebaseAuthUserCollisionException e) {
                                error = getString(R.string.error_user_exists);
                            } catch (Exception e) {
                                error = task.getException().getMessage();
                            }
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            final Snackbar snackbar = Snackbar.make(layoutRoot, error, Snackbar.LENGTH_LONG);
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
                });
    }
}
