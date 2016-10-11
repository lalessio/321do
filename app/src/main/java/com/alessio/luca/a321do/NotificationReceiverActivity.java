package com.alessio.luca.a321do;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Luca on 11/10/2016.
 */

public class NotificationReceiverActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("lalala","esegue sta classe davvero");
        setContentView(R.layout.result);
        Toast.makeText(this,"aaaaaaaaa",Toast.LENGTH_SHORT).show();
    }
}
