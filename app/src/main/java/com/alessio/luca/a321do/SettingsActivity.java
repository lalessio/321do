package com.alessio.luca.a321do;

import android.app.Activity;
import android.os.Bundle;

import java.util.Calendar;

/**
 * Created by Luca on 31/10/2016.
 */
public class SettingsActivity extends Activity {
    public static SortingOrder DEFAULT_ORDER = new SortingOrder(SortingOrder.Order.NONE, SortingOrder.Filter.NONE);
    public static Calendar DEFAULT_DATE = Calendar.getInstance();
    //TODO

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
    }
}
