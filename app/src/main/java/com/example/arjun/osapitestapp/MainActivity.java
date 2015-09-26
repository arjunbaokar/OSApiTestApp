package com.example.arjun.osapitestapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Browser;
import android.provider.CallLog;
import android.telecom.TelecomManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
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
        testLocation();
        testSms();
        testWifi();
        testWriteSyncSettings();
        testBrowser();
        testReadSms();
        testReadCallLogs();
    }

    private void testReadCallLogs() {
        Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        if (cursor.moveToFirst()) {
            String data = "";
            for(int idx=0;idx<cursor.getColumnCount();idx++)
            {
                data += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
            }
            print(data);
        } else {
            print("No calls made");
        }
    }

    public void testReadSms() {
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);
        if (cursor.moveToFirst()) {
            String msgData = "";
            for(int idx=0;idx<cursor.getColumnCount();idx++)
            {
                msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
            }
            print(msgData);
        } else {
            print("There are no SMS");
        }
    }

    public void testBrowser() {
        String[] proj = new String[] { Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL };
        String sel = Browser.BookmarkColumns.BOOKMARK + " = 0"; // 0 = history, 1 = bookmark
        Cursor mCur = getContentResolver().query(Browser.BOOKMARKS_URI, proj, sel, null, null);
        mCur.moveToFirst();
        String title, url;
        if (mCur.moveToFirst() && mCur.getCount() > 0) {
            title = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
            url = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.URL));
            print("Title: " + title + " Url: " + url);
            mCur.moveToNext();
        }
    }

    public void testWriteSyncSettings() {
        ContentResolver resolver = getContentResolver();
        resolver.setMasterSyncAutomatically(true); // this does not return anything though
        print("Sync enabled");
    }

    public void testWifi() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        print("Connection Info: " + connectionInfo.getSSID());
    }

    public void testLocation() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getPhoneType() == 0x00000001) {
            GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();
            print("Cid: " + location.getCid());
            print("Lac: " + location.getLac());
        }
        if (tm.getPhoneType() == 0x00000002) {
            CdmaCellLocation location = (CdmaCellLocation) tm.getCellLocation();
            print("BaseStationLatitude: " + location.getBaseStationLatitude());
        }
        if (tm.getPhoneType() == 0x00000003) {
            print("Returns null");
        }
        if (tm.getPhoneType() == 0x00000000) {
            print("Location is none");        }
    }

    public void testSms() {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("5556", null, "Testing sendSms", null, null);
        print("Message Sent");
    }
    public void print(String string) {
        TextView textView = (TextView) findViewById(R.id.textView2);
        textView.append(string + "\n");
    }
    // Gets location
        /*LocationListener mLocationListener = new LocationListener() {
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
        };*/
}
