package com.monowar.firebasechatapp.Test;

import android.app.Activity;
import android.content.Context;

/**
 * Created by The Game on 12/11/2016.
 */

public interface BaseView {
    Context getContext();
    Activity getActivity();

    void showLoader();

    void hideLoader();

}
