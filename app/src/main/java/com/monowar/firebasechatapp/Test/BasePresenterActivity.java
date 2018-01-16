package com.monowar.firebasechatapp.Test;

/**
 * Created by Mostafa Monowar on 16-Aug-17.
 */

public abstract class BasePresenterActivity <V, T extends BasePresenter> extends BaseActivity {
    protected T presenter;
    protected V view;

    protected abstract T bindPresenter();
}
