package com.monowar.firebasechatapp.Test;

/**
 * Created by The Game on 12/11/2016.
 */

public interface BasePresenter<V> {
    void onCreatePresenter();

    void onResumePresenter();

    void onPausePresenter();

    void onDestroyPresenter();
}
