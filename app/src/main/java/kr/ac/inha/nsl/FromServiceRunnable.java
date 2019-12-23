package kr.ac.inha.nsl;

import java.util.Arrays;

public abstract class FromServiceRunnable implements Runnable {
    protected FromServiceRunnable(Object... args) {
        this.args = Arrays.copyOf(args, args.length);
    }

    public Object[] args;
}
