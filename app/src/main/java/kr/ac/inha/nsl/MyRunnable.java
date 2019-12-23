package kr.ac.inha.nsl;

import android.app.Activity;

import java.util.Arrays;

public abstract class MyRunnable implements Runnable {
    protected MyRunnable(Activity activity, Object... args) {
        this.activity = activity;
        this.args = Arrays.copyOf(args, args.length);
    }

    public void enableTouch() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Tools.enable_touch(activity);
            }
        });
    }

    public Object[] args;
    public Activity activity;
}