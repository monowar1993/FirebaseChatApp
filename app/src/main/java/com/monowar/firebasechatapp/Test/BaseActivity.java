package com.monowar.firebasechatapp.Test;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Mostafa Monowar on 16-Aug-17.
 */

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initComponents();
    }

    protected abstract void initComponents();

    public Context getContext() {
        return this;
    }

    public Activity getActivity() {
        return this;
    }

    public void showLoader() {

    }

    public void hideLoader() {

    }
}
