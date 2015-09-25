package com.example.arjun.osapitestapp;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Called when the user touches the button */
    public void handleClick(View view) {
        System.out.println("hello world!\n");
        // Gets location
        LocationListener mLocationListener = new LocationListener() {
            public void onLocationChanged(final Location location) {
                Context context = getApplicationContext();
                CharSequence text = Double.toString(location.getLatitude()) + Double.toString(location.getLongitude());
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, text, duration).show();
            }

            public void onStatusChanged(String string, int num, Bundle bundle) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                CharSequence text = "String: " + string + ", " + "Integer: " + String.valueOf(num)
                        + ", "+ "Bundle: " + bundle;
                Toast.makeText(context, text, duration).show();
            }

            public void onProviderEnabled(String string) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                CharSequence text = "String: " + string;
                Toast.makeText(context, text, duration).show();
            }

            public void onProviderDisabled(String string) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                CharSequence text = "String: " + string;
                Toast.makeText(context, text, duration).show();            }
        };
    }
}
