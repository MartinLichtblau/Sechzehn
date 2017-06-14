package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class MainActivity extends AppCompatActivity  {

    private TextView mTextMessage;
    private GoogleMap mMap;

    /*
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




    }
/*
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
        }

        // Add a marker in Sydney and move the camera
//        LatLng tuDarmstadt = new LatLng(49.877452, 8.654454);
//        mMap.addMarker(new MarkerOptions().position(tuDarmstadt).title("TU Darmstadt"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(tuDarmstadt));
//
//        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
    }*/
}
