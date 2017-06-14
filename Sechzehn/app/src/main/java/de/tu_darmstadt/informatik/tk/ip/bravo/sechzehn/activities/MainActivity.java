package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        if (loggedin == true) {
            startActivity(new Intent(MainActivity.this, BottomTabsActivity.class));
        }
        else {
            start Login/Registration
        }
        */

        startActivity(new Intent(MainActivity.this, BottomTabsActivity.class));
    }
}
