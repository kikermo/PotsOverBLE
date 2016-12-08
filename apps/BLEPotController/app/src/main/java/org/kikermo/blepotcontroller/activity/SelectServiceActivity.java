package org.kikermo.blepotcontroller.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.kikermo.blepotcontroller.R;

public class SelectServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_service);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}
