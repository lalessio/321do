package com.alessio.luca.b321do;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Luca on 31/10/2016.
 */
public class FaqActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faq_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);
        //i know i know this code is pure trash but the deadline is in 2 days and i still need to fix a lot of things so "faq" you
        TextView q1t = (TextView) findViewById(R.id.q1T);
        TextView q2t = (TextView) findViewById(R.id.q2T);
        TextView q3t = (TextView) findViewById(R.id.q3T);
        TextView q4t = (TextView) findViewById(R.id.q4T);
        TextView q5t = (TextView) findViewById(R.id.q5T);
        TextView q6t = (TextView) findViewById(R.id.q6T);
        TextView q7t = (TextView) findViewById(R.id.q7T);
        TextView q8t = (TextView) findViewById(R.id.q8T);
        TextView q9t = (TextView) findViewById(R.id.q9T);
        TextView credits = (TextView) findViewById(R.id.creditsTitle);

        q1t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView qC = (TextView) findViewById(R.id.q1C);
                if(qC.getVisibility()==View.VISIBLE)
                    qC.setVisibility(View.GONE);
                else
                    qC.setVisibility(View.VISIBLE);
            }
        });q2t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView qC = (TextView) findViewById(R.id.q2C);
                if(qC.getVisibility()==View.VISIBLE)
                    qC.setVisibility(View.GONE);
                else
                    qC.setVisibility(View.VISIBLE);
            }
        });q3t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView qC = (TextView) findViewById(R.id.q3C);
                if(qC.getVisibility()==View.VISIBLE)
                    qC.setVisibility(View.GONE);
                else
                    qC.setVisibility(View.VISIBLE);
            }
        });q4t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView qC = (TextView) findViewById(R.id.q4C);
                if(qC.getVisibility()==View.VISIBLE)
                    qC.setVisibility(View.GONE);
                else
                    qC.setVisibility(View.VISIBLE);
            }
        });q5t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView qC = (TextView) findViewById(R.id.q5C);
                if(qC.getVisibility()==View.VISIBLE)
                    qC.setVisibility(View.GONE);
                else
                    qC.setVisibility(View.VISIBLE);
            }
        });q6t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView qC = (TextView) findViewById(R.id.q6C);
                if(qC.getVisibility()==View.VISIBLE)
                    qC.setVisibility(View.GONE);
                else
                    qC.setVisibility(View.VISIBLE);
            }
        });q7t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView qC = (TextView) findViewById(R.id.q7C);
                if(qC.getVisibility()==View.VISIBLE)
                    qC.setVisibility(View.GONE);
                else
                    qC.setVisibility(View.VISIBLE);
            }
        });q8t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView qC = (TextView) findViewById(R.id.q8C);
                if(qC.getVisibility()==View.VISIBLE)
                    qC.setVisibility(View.GONE);
                else
                    qC.setVisibility(View.VISIBLE);
            }
        });q9t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView qC = (TextView) findViewById(R.id.q9C);
                if(qC.getVisibility()==View.VISIBLE)
                    qC.setVisibility(View.GONE);
                else
                    qC.setVisibility(View.VISIBLE);
            }
        });credits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView qC = (TextView) findViewById(R.id.creditsContent);
                if(qC.getVisibility()==View.VISIBLE)
                    qC.setVisibility(View.GONE);
                else
                    qC.setVisibility(View.VISIBLE);
                TextView qLink = (TextView) findViewById(R.id.donateLink);
                if(qLink.getVisibility()==View.VISIBLE)
                    qLink.setVisibility(View.GONE);
                else
                    qLink.setVisibility(View.VISIBLE);
            }
        });
    }
    @Override
    public void onBackPressed() {
        overridePendingTransition(0,0);
        super.onBackPressed();
    }
}
