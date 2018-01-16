package com.monowar.firebasechatapp.Test;

import java.lang.ref.WeakReference;

/**
 * Created by Mostafa Monowar on 16-Aug-17.
 */

public class TestPresenter implements BasePresenter<TestView> {

    private WeakReference<TestView> view;

    private TestPresenter (TestView view) {
        this.view = new WeakReference<>(view);
    }

    @Override
    public void onCreatePresenter() {

    }

    @Override
    public void onResumePresenter() {

    }

    @Override
    public void onPausePresenter() {

    }

    @Override
    public void onDestroyPresenter() {

    }

    public void onClick() {
        view.get().Test();
        view.get().getContext();
    }
}
