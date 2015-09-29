package com.example.arjun.osapitestapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Browser;
import android.provider.CallLog;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
    public void locationClick(View view) {
        testCellLocation();
        testGpsLocation();
    }

    public void smsClick(View view) {
        testReadSms();
        testSendSms();
    }

    public void wifiClick(View view) {
        testWifi();
    }

    public void syncClick (View view) {
        testWriteSyncSettings();
    }

    public void browserClick(View view) {
        testChromeHistory();
        testAllVisitedUrls();
        testAllBookmarks();
    }

    public void callLogsClick(View view) {
        testReadCallLogs();
    }

    public void nfcClick(View view) {
        testNfc();
    }

    public void clearClick(View view) {
        TextView textView = (TextView) findViewById(R.id.textView2);
        textView.setText("");
    }

    public void testAllVisitedUrls() {
        print("----All Visited URLs----");
        Cursor cursor = Browser.getAllVisitedUrls(getContentResolver());
        if (cursor != null) {
            String[] columnNames = cursor.getColumnNames();
            String toReturn = "";
            for (String columnName : columnNames) {
                toReturn += columnName + ", ";
            }
            print("Cursor Column Names: " + toReturn);
        } else {
            print("Cursor is null");
        }
    }

    public void testAllBookmarks() {
        print("----All Bookmarks----");
        Cursor cursor = Browser.getAllBookmarks(getContentResolver());
        if (cursor != null) {
            String[] columnNames = cursor.getColumnNames();
            String toReturn = "";
            for (String columnName : columnNames) {
                toReturn += columnName + ", ";
            }
            print("Cursor Column Names: " + toReturn);
        } else {
            print("Cursor is null");
        }
    }

    public void testNfc() {
        // TODO: implement this
        print("----NFC----");
        print("Not implemented.");
    }

    public void testGpsLocation() {
        // TODO: Implement this
        print("----GPS Location----");
        print("Not implemented."); // remove this
    }

    private void testReadCallLogs() {
        print("----Read Call Logs----");
        Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        if (cursor == null) {
            print("Cursor is null");
        } else if (cursor.moveToFirst()) {
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
        // FIXME: (not urgent) for some reason, only works for SMS_ALL
        print("----testReadSms----");

        // Use these to toggle between which sms set you want to read
        final String SMS_ALL = "content://sms/";
        final String INBOX = "content://sms/inbox";
        final String DRAFT = "content://sms/draft";
        final String SENT = "content://sms/sent";

        Cursor cursor = getContentResolver().query(Uri.parse(SMS_ALL), null, null, null, null);
        if (cursor == null) {
            print("Cursor is null");
        } else if (cursor.moveToFirst()) {
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

    public void testChromeHistory() {
        print("----Chrome History----");
        final Uri CHROME_BOOKMARKS_URI = Uri.parse("content://com.android.chrome.browser/bookmarks");
        String[] proj = new String[] { Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL };
        String sel = Browser.BookmarkColumns.BOOKMARK + " = 0"; // 0 = history, 1 = bookmark
        Cursor mCur = getContentResolver().query(CHROME_BOOKMARKS_URI, proj, sel, null, null);
        if (mCur != null) {
            mCur.moveToFirst();
            String title, url;
            if (mCur.moveToFirst() && mCur.getCount() > 0) {
                title = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
                url = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.URL));
                print("Title: " + title + " Url: " + url);
                mCur.moveToNext();
            }
        } else {
            print("Cursor is null");
        }
    }

    public void testWriteSyncSettings() {
        print("----Update and Check sync settings----");
        ContentResolver resolver = getContentResolver();
        resolver.setMasterSyncAutomatically(true);
        boolean syncSettingTrue = resolver.getMasterSyncAutomatically();
        resolver.setMasterSyncAutomatically(false);
        boolean syncSettingFalse = resolver.getMasterSyncAutomatically();
        if (syncSettingTrue && !syncSettingFalse) {
            print("Sync updated successfully");
        }
        else {
            if (!syncSettingTrue) {
                print("True setting failed");
            }
            if (syncSettingFalse) {
                print("False setting failed");
            }
        }
    }

    public void testWifi() {
        print("----Wifi SSID----");
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        print("Connection Info: " + connectionInfo.getSSID());
    }

    public void testCellLocation() {
        // FIXME: doesn't work, returns no location
        print("----Cell Location----");
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

    public void testSendSms() {
        print("----testSendSms----");
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("5556", null, "Testing sendSms", null, null);
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
