package com.app.bmarroc.yamba;

import android.util.Log;

public class YambaClientException extends Exception {
    private String error;
    public YambaClientException(String error) {
        this.error = error;
    }
    public void printStackTrace() {
        Log.d("ERROR: ", this.error);
    }
}
