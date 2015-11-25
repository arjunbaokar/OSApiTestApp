package com.example.arjun.osapitestapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.SystemClock;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.Browser;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    long cameraDispatchTime = -1;
    private Camera mCamera;
    private CameraPreview mCameraPreview;

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
        long startTime = SystemClock.uptimeMillis();
        testCellLocation();
        testGpsLocation();
        print("Time taken: " + (SystemClock.uptimeMillis() - startTime));
    }

    public void smsClick(View view) {
        long startTime = SystemClock.uptimeMillis();
        testReadSms();
        testSendSms();
        print("Time taken: " + (SystemClock.uptimeMillis() - startTime));
    }

    public void wifiClick(View view) {
        long startTime = SystemClock.uptimeMillis();
        testWifi();
        print("Time taken: " + (SystemClock.uptimeMillis() - startTime));
    }

    public void syncClick (View view) {
        long startTime = SystemClock.uptimeMillis();
        testWriteSyncSettings();
        print("Time taken: " + (SystemClock.uptimeMillis() - startTime));
    }

    public void browserClick(View view) {
        long startTime = SystemClock.uptimeMillis();
        testChromeHistory();
        testAllVisitedUrls();
        testAllBookmarks();
        print("Time taken: " + (SystemClock.uptimeMillis() - startTime));
    }

    public void callLogsClick(View view) {
        long startTime = SystemClock.uptimeMillis();
        testReadCallLogs();
        print("Time taken: " + (SystemClock.uptimeMillis() - startTime));
    }

    public void nfcClick(View view) {
        testNfcBeam();
    }

    public void cameraClick(View view) {
//        dispatchTakePictureIntent(); // for intent-based camera 1. spawns new activity.
        camera1TakePicture();
        // TODO: for camera 2
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

    @Override
    public void onResume() {
        super.onResume();

        enableForegroundDispatchSystem();
    }

    @Override
    public void onPause() {
        super.onPause();

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);
    }

    private void enableForegroundDispatchSystem() {

        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        IntentFilter[] intentFilters = new IntentFilter[] {};
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        long timeTaken = SystemClock.uptimeMillis();

        super.onNewIntent(intent);

        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            print("---NFC Tag---");
        }

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            print("ACTION_NDEF_DISCOVERED: NDEF Tag Discovered!");
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    print(msgs[i].toString());
                }
            }
        }

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            print("ACTION_TECH_DISCOVERED: Tech Discovered!");
            print(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG).toString());
        }

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            print("ACTION_TAG_DISCOVERED: Non-NDEF Tag Discovered!");
            print(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG).toString());
        }

        print("onNewIntent() time taken: " + (SystemClock.uptimeMillis() - timeTaken));
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

    public void dispatchTakePictureIntent() {
        print("----Camera1:takePicture----");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraDispatchTime = SystemClock.uptimeMillis();
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void camera1TakePicture() {
        mCamera = getCameraInstance();
        mCameraPreview = new CameraPreview(this, mCamera);
        long startTime = SystemClock.uptimeMillis();
        mCamera.takePicture(null, null, mPicture);
        print("Time Taken: " + (SystemClock.uptimeMillis()-startTime));
    }

    /**
     * Helper method to access the camera returns null if it cannot get the
     * camera or does not exist
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
    }

    @SuppressWarnings("deprecation")
    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
        }
    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            print("Time taken: " + (SystemClock.uptimeMillis()-cameraDispatchTime));
        }
    }

    public void print(String string) {
        TextView textView = (TextView) findViewById(R.id.textView2);
        textView.append(string + "\n");
    }
}
