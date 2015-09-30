package com.example.arjun.osapitestapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.PrintWriter;

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
        testNfcBeam();
    }

    public void tagClick(View view) {
        readNfcTag();
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

    public void testNfcBeam() {
        print("----NFC Beam----");
        if (isExternalStorageReadable() && isExternalStorageWritable()) {
            writeFileToExternalStorage();
            androidBeam();
        } else {
            print("External storage is not readable/writeable. Quitting.");
        }
    }

    private void readNfcTag() {
        //TODO: test this
        print("----NFC Tag----");
        NfcAdapter mAdapter;
        PendingIntent mPendingIntent;

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            print("NFC not supported on device! Quitting.");
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        print("Waiting for NFC Tag detected...");
    }

    @Override
    protected void onNewIntent(Intent intent){
        print("NFC tag detected!");
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        print("NFC tag data: " + tag.toString());
    }

    private void androidBeam() {
        print("Checking Android Beam support...");

        Context context = getApplicationContext();
        NfcAdapter mNfcAdapter;
        FileUriCallback mFileUriCallback;
        PackageManager pacman = context.getPackageManager();

        if (!pacman.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            print("NFC and Android Beam not supported! Quitting.");
        } else if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.JELLY_BEAN_MR1) {
            print("Android Beam not supported! Quitting.");
        } else {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            mFileUriCallback = new FileUriCallback();
            mNfcAdapter.setBeamPushUrisCallback(mFileUriCallback,this);
            print("Android Beam supported! Beaming...");
        }
    }

    private class FileUriCallback implements NfcAdapter.CreateBeamUrisCallback {
        public FileUriCallback() {
        }
        /**
         * Create content URIs as needed to share with another device
         */
        @Override
        public Uri[] createBeamUris(NfcEvent event) {    // send files
            File dir = Environment.getExternalStorageDirectory();
            File file = new File(dir, "test.txt");
            file.setReadable(true, false);    // readable=true, ownerOnly=false
            print("Beam URIs requested. Transferring files...");
            return new Uri[] { Uri.fromFile(file) };
        }
    }

    private void writeFileToExternalStorage() {
        print("Writing test.txt to external storage...");

        File externalDir = Environment.getExternalStorageDirectory();
        File f = new File(externalDir, "test.txt");

        try {
            PrintWriter writer = new PrintWriter(f, "UTF-8");
            writer.println("yoloswaggins");
            writer.close();
        } catch (Exception e) {
            print("Write failed with error: " + e.toString());
        }

        print("Done!");
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public void testGpsLocation() {
        print("----GPS Location----");
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        print("Last known GPS location: " + lastKnownLocation);
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        print("Last known passive location: " + lastKnownLocation);
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
        // FIXME: (not critical) for some reason, only works for SMS_ALL
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

    // Used to parse detected NFC tag from intent
    /*
    private void getTagInfo(Intent intent) {
    Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

String[] techList = tag.getTechList();
for (int i = 0; i < techList.length; i++) {
    if (techList[i].equals(MifareClassic.class.getName())) {

        MifareClassic mifareClassicTag = MifareClassic.get(tag);
        switch (mifareClassicTag.getType()) {
        case MifareClassic.TYPE_CLASSIC:
            //Type Clssic
            break;
        case MifareClassic.TYPE_PLUS:
            //Type Plus
            break;
        case MifareClassic.TYPE_PRO:
            //Type Pro
            break;
        }
    } else if (techList[i].equals(MifareUltralight.class.getName())) {
    //For Mifare Ultralight
        MifareUltralight mifareUlTag = MifareUltralight.get(tag);
        switch (mifareUlTag.getType()) {
        case MifareUltralight.TYPE_ULTRALIGHT:
            break;
        case MifareUltralight.TYPE_ULTRALIGHT_C:

            break;
        }
    } else if (techList[i].equals(IsoDep.class.getName())) {
        // info[1] = "IsoDep";
        IsoDep isoDepTag = IsoDep.get(tag);

    } else if (techList[i].equals(Ndef.class.getName())) {
        Ndef.get(tag);

    } else if (techList[i].equals(NdefFormatable.class.getName())) {

        NdefFormatable ndefFormatableTag = NdefFormatable.get(tag);

    }
}
}
     */
}
