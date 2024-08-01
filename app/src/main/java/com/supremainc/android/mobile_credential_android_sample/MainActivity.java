package com.supremainc.android.mobile_credential_android_sample;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supremainc.android.libsupremaac.AcInitListener;
import com.supremainc.android.libsupremaac.AcLogStatusListener;
import com.supremainc.android.libsupremaac.SupremaAc;
import com.supremainc.android.libsupremaac.model.CardData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getSimpleName();

    // Test Site id, you can modify below code
    static String siteId = "test";

    // Full Access : True
    // Custom Access Level : False
    static boolean isFullAccess = true;

    // Test Target Device Serial String
    static String targetSerial = "547834064";

    private SupremaAc Sdk = null;

// UI Control variables //
    private final int REQUEST_CODE_ASK_LOCATION_MULTIPLE_PERMISSIONS = 10;
    private final int REQUEST_CODE_ASK_BACKGROUND_LOCATION_MULTIPLE_PERMISSIONS = 12;
    private final int REQUEST_CODE_ASK_BLUETOOTH_MULTIPLE_PERMISSIONS = 11;
    private Context mContext;
    TextView version;
    TextView bleWait;
    TextView cardActivated;
    TextView hceSupport;
    TextView authType;
    TextView accessLevelHandleResult;
    TextView getAccessLevelResult;

    Button issueCard;
    Button removeCard;
    Button submitButton;
    Button notiClearButton;
    Button showCardBtn;
    Button initBtn;
    Button actionAccessLevelHandleButton;
    Button getAccessLevelButton;
    Button connectToNearestDevice;

    ToggleButton nfcBtn;
    ToggleButton bleBtn;
    EditText offText;
    EditText bleText;
    EditText nfcText;
    EditText allText;

    Spinner listDropDown;

    private boolean mNFCtoggle = false;
    private boolean mBLEtoggle = false;
    private boolean mHasAccessLevel = false;

    private boolean mInitRepeat = true;

    private static String offMessage = "";
    private static String bleMessage = "";
    private static String nfcMessage = "";
    private static String allMessage = "";

    private final int AUTH_STATE_NONE_CARD = -1;
    private final int AUTH_STATE_OFF = 0;
    private final int AUTH_STATE_BLE = 1;
    private final int AUTH_STATE_NFC = 2;
    private final int AUTH_STATE_ALL = 3;
    private final int AUTH_STATE_BLE_WAIT = 4;
    private final int AUTH_STATE_BLE_WAIT_NFC_AVAILIABLE = 6;
    private String[] idList;

    private void sdkInitUsingForegroundOnly(){
        SupremaAc.getInstance().init(mContext, siteId, true);
        SupremaAc.getInstance().setBleBackgroundFlag(false);
        SupremaAc.getInstance().setNFCBackgroundFlag(false);
        // sdk validity grace time
        // default & max: 1 day
        SupremaAc.getInstance().setSDKValidityGraceTime(1000);
    }

    private void sdkInitUsingBackgroundMode(){
        SupremaAc.getInstance().init(mContext, siteId, true,"test", R.drawable.ic_launcher_foreground, "testMessage", new AcInitListener() {
            @Override
            public void initCallback(boolean initResult) {
                SupremaAc.getInstance().setAuthStateNoti("test", R.drawable.ic_launcher_foreground, "testMessage");
                SupremaAc.getInstance().setBleBackgroundFlag(true);
                SupremaAc.getInstance().setNFCBackgroundFlag(true);
                // sdk validity grace time
                // default & max: 1 day
                SupremaAc.getInstance().setSDKValidityGraceTime(1000);
            }
        });
    }

    private void SDKInitialize(){
        Sdk = SupremaAc.getInstance();

        // if you want using Foreground only, using below code
//        sdkInitUsingForegroundOnly();
        // if you want using Foreground and Background, using below code
        sdkInitUsingBackgroundMode();

        Sdk.setAuthStateEventListener(new SupremaAc.AuthStateEventListener() {
            @Override
            public void onReceived(int i) {
                String res = "Prepare: ";
                switch (i) {
                    case AUTH_STATE_NONE_CARD:
                        SupremaAc.getInstance().updateAuthStateNoti("Please download mobile card");
                        break;
                    case AUTH_STATE_OFF:
                        if(offMessage != null && !offMessage.isEmpty()) SupremaAc.getInstance().updateAuthStateNoti(offMessage);
                        else SupremaAc.getInstance().updateAuthStateNoti("Please check NFC/BLE status");
                        break;
                    case AUTH_STATE_BLE:
                        if(bleMessage != null && !bleMessage.isEmpty()) SupremaAc.getInstance().updateAuthStateNoti(bleMessage);
                        else SupremaAc.getInstance().updateAuthStateNoti("Available BLE");
                        res += "BLE ";
                        Log.i(TAG, "you can use BLE");
                        break;
                    case AUTH_STATE_NFC:
                    case AUTH_STATE_BLE_WAIT_NFC_AVAILIABLE:
                        if(nfcMessage != null && !nfcMessage.isEmpty()) SupremaAc.getInstance().updateAuthStateNoti(nfcMessage);
                        else SupremaAc.getInstance().updateAuthStateNoti("Available NFC");
                        res += "+ NFC ";
                        Log.i(TAG, "you can use NFC");
                        break;
                    case AUTH_STATE_ALL:
                        if(allMessage != null && !allMessage.isEmpty()) SupremaAc.getInstance().updateAuthStateNoti(allMessage);
                        else SupremaAc.getInstance().updateAuthStateNoti("Available NFC/BLE");
                        res += "BLE ";
                        Log.i(TAG, "you can use BLE");
                        res += "+ NFC ";
                        Log.i(TAG, "you can use NFC");
                        break;
                    case AUTH_STATE_BLE_WAIT:
                        if(allMessage != null && !allMessage.isEmpty()) SupremaAc.getInstance().updateAuthStateNoti(allMessage);
                        else SupremaAc.getInstance().updateAuthStateNoti("Preparing BLE. wait a few seconds");
                        break;
                }
                final String response = res;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        authType.setText(response);
                    }
                });
            }
        });

        SupremaAc.getInstance().setEventListener(new SupremaAc.BleEventListener() {

            @Override
            public void onGuardTimeStarted(int i) {
                runVibration();
            }

            @Override
            public void onGuardTimeFinished() {
                Log.i(TAG, "scan guard time finished.");
                if (null != bleWait) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bleWait.setTextColor(getResources().getColor(R.color.colorPrimary));
                            bleWait.setText("BLE - Ready");
                        }
                    });
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "Guard Time Done", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            }

            @Override
            public void onError(final int i) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "BLE Auth Result : " + i, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }, new SupremaAc.NfcEventListener() {
            @Override
            public void onSendData(int type, int i) {
                Log.i(TAG, "nfc data send");
            }

            @Override
            public void onError(int i) {
                Log.i(TAG, "failed nfc data send, Reason: " + i);
            }
        });

        SupremaAc.getInstance().setNFCFlag(true);
        SupremaAc.getInstance().setBleFlag(true);

        // Sync Time with Server
        SupremaAc.getInstance().setServerTime(12312399492l);

        SupremaAc.getInstance().loadCard();
        // if has card, start Ble Scan
        if(Sdk.getCardIdList() != null && Sdk.getCardIdList().length > 0){
            Sdk.startBleScan();
        }
        testAppSetup();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;


        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        version = findViewById(R.id.version);
        bleWait = findViewById(R.id.ble_wait);
        cardActivated = findViewById(R.id.card_activated);
        hceSupport = findViewById(R.id.nfc_hce_support);
        authType = findViewById(R.id.authTypeResult);
        accessLevelHandleResult = findViewById(R.id.accesslevel_handle_result);
        getAccessLevelResult = findViewById(R.id.get_accesslevel_result);
        connectToNearestDevice = findViewById(R.id.connectNearestDevice);

        showCardBtn = findViewById(R.id.showCardButton);
        issueCard = findViewById(R.id.issuecard);
        removeCard = findViewById(R.id.removecard);
        nfcBtn= findViewById(R.id.nfcButton);
        bleBtn = findViewById(R.id.bleButton);
        submitButton = findViewById(R.id.button);
        notiClearButton = findViewById(R.id.noti_clear_button);
        initBtn = findViewById(R.id.initBtn);
        actionAccessLevelHandleButton = findViewById(R.id.action_access_level);
        getAccessLevelButton = findViewById(R.id.get_access_level);

        offText = findViewById(R.id.off_text);
        bleText = findViewById(R.id.ble_text);
        nfcText = findViewById(R.id.nfc_text);
        allText = findViewById(R.id.all_text);

        nfcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNFCtoggle = nfcBtn.isChecked();
                SupremaAc.getInstance().setNFCFlag(mNFCtoggle);
                Log.i(TAG, "set NFC !!" + mNFCtoggle);
            }
        });

        bleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBLEtoggle = bleBtn.isChecked();
                SupremaAc.getInstance().setBleFlag(mBLEtoggle);
                Log.i(TAG, "set BLE !!" + mBLEtoggle);
            }
        });

        listDropDown = findViewById(R.id.list_dropdown);
        listDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String currentId = idList[position];
                SupremaAc.getInstance().setUsingCard(currentId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        initBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "init Btn : " + mInitRepeat);
                if(mInitRepeat) {
                    SupremaAc.getInstance().stopService();
                    initBtn.setText("SDK init");
                } else {
                    sdkInitUsingForegroundOnly();
                    initBtn.setText("Stop Service");
                }
                mInitRepeat = !mInitRepeat;
            }
        });



        notiClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SupremaAc.getInstance().clearAuthStateNoti();
            }
        });

        version.setText("version : " + SupremaAc.getInstance().getSDKVersion());

        issueCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stringCard1 = "WwogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAiY2FyZERhdGEiOiB7CiAgICAgICAgICAgICAgICAgICAgImNhcmROdW1iZXIiOiAiZDEzTHZDemRQVXpCbnJsZGErSmZldz09IiwKICAgICAgICAgICAgICAgICAgICJzdGFydERhdGUiOiAiMCIsCiAgICAgICAgICAgICAgICAgICAiZW5kRGF0ZSI6ICIyNTM0MDIzMDA3OTkwMDAiLAogICAgICAgICAgICAgICAgICAgInJlZnJlc2giOiAiMCIsCiAgICAgICAgICAgICAgICAgICAiY2FyZE51bWJlckJpdCI6ICIyNCIsCiAgICAgICAgICAgICAgICAgICAiZW5jVHlwZSI6ICIzIiwKICAgICAgICAgICAgICAgICAgICJmb3JtYXRDb2RlIjogIjAiLAogICAgICAgICAgICAgICAgICAgInNpZ25hdHVyZSI6ICJNRVFDSUMzbG96L282YjBSa296SGJzNHpQQWthWU9QTm1FQUFmaHNlTVR6SkVWMlJBaUF2eks4RkRIL2ZpR0VZVG1vem9sVlA1MDJqYjdCeUViZzdwMGh0WTB6ck13PT0iCiAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgfSwKICAgICAgICAgICAgewogICAgICAgICAgICAgICAgImNhcmREYXRhIjogewogICAgICAgICAgICAgICAgICAgICJjYXJkTnVtYmVyIjogInJqcnFEZWd6MktZbWpZcUpFS0VWYmc9PSIsCiAgICAgICAgICAgICAgICAgICAgInN0YXJ0RGF0ZSI6ICIwIiwKICAgICAgICAgICAgICAgICAgICAiZW5kRGF0ZSI6ICIyNTM0MDIzMDA3OTkwMDAiLAogICAgICAgICAgICAgICAgICAgICJyZWZyZXNoIjogIjAiLAogICAgICAgICAgICAgICAgICAgICJjYXJkTnVtYmVyQml0IjogIjI0IiwKICAgICAgICAgICAgICAgICAgICAiZW5jVHlwZSI6ICIzIiwKICAgICAgICAgICAgICAgICAgICAiZm9ybWF0Q29kZSI6ICIwIiwKICAgICAgICAgICAgICAgICAgICAic2lnbmF0dXJlIjogIk1Dd0NGQ3QvbE54ZnhRYmRyZjkxNWFLR0lab3J2cEF1QWhSTDRQRFgrZDhISVQvNzRRSWNJUmpnMlFzUzBBPT0iCiAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgIH0KICAgICAgICBd";
                HashMap<String, Object> bCard = null;
                try {
                    bCard = ExampleCard.getSubIDCard();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Multi Card
                // if you not regist "cardType" then set up 'REGULAR CARD' automatically
                SupremaAc.getInstance().setCardDataCustom(stringCard1, "0"); // siteID : "0", set up 'REGULAR CARD' automatically
                SupremaAc.getInstance().setCardDataCustom((HashMap<String, Object>) bCard.get("record"), MainActivity.siteId); // 2 : INTELLIGENT

                setCardActivatedStatus(true);
                Log.i(TAG, "setCardData Custom!!!!");
                SupremaAc.getInstance().startBleScan();
                SupremaAc.getInstance().setNFCFlag(mNFCtoggle);
                setListDropDown();
            }
        });

        removeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (String id: idList) {
                    SupremaAc.getInstance().removeAcCard(id);
                }
                setListDropDown();
                setCardActivatedStatus(false);
                Log.i(TAG, "Clear CardData !!!!");
                SupremaAc.getInstance().stopBleScan();

            }
        });

        actionAccessLevelHandleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mHasAccessLevel == false) {
                    // add sample Access Levels
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // set Access Levels
                            ExampleAccessLevel d = new ExampleAccessLevel();
                            HashMap<String, Object> b = null;
                            try {
                                b = (HashMap<String, Object>) d.getBody(2);
                                ArrayList<HashMap<String, Object>> als = getAls(b);
                                HashMap<String, Object> extraIfo = getCardExtraInfo(b);
                                mHasAccessLevel = true;
                                runOnButtonTextUpdate(actionAccessLevelHandleButton, "DEL AccessLevel");
                                runOnTextViewTextUpdate(accessLevelHandleResult, "Succeed save to DB");
                                SupremaAc.getInstance().setAccessLevels(als);
                                SupremaAc.getInstance().setAccessLevelsExtraInfo(siteId, (String)extraIfo.get("cardId"), (long)extraIfo.get("lastModifiedAt"));
                            } catch (Exception e) {
                                e.printStackTrace();
                                mHasAccessLevel = true;
                                runOnTextViewTextUpdate(accessLevelHandleResult, "Failed save to DB");
                                runOnButtonTextUpdate(actionAccessLevelHandleButton, "DEL AccessLevel");
                            }
                        }
                    }).start();
                } else {
                    // remove
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // remove Access Levels
                            try{
                                mHasAccessLevel = false;
                                runOnTextViewTextUpdate(accessLevelHandleResult, "Succeed remove from DB");
                                runOnButtonTextUpdate(actionAccessLevelHandleButton, "SET AccessLevel");
                                SupremaAc.getInstance().removeAccessLevels(siteId);
                                SupremaAc.getInstance().removeAccessLevelsExtraInfo(siteId);
                            } catch (Exception e){
                                mHasAccessLevel = true;
                                runOnTextViewTextUpdate(accessLevelHandleResult, "Failed remove from DB");
                                runOnButtonTextUpdate(actionAccessLevelHandleButton, "DEL AccessLevel");
                            }
                        }
                    }).start();
                }
            }
        });

        getAccessLevelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long accessLevelsLastUpdateTime = SupremaAc.getInstance().getAccessLevelsLastUpdatedAt(siteId);
                String[] asid = SupremaAc.getInstance().getAccessLevelIds(siteId);
                if(asid.length > 0) {
                    String accessLevelIdList = "updateTime : " + accessLevelsLastUpdateTime + ", AccessLevel IDs : ";
                    for (int i = 0; i < asid.length; i++){
                        long accessLevelLastUpdateTime = SupremaAc.getInstance().getAccessLevelLastUpdatedAt(asid[i]);
                        accessLevelIdList += asid[i] + "("+accessLevelLastUpdateTime+"), ";
                        runOnTextViewTextUpdate(getAccessLevelResult, accessLevelIdList);
                    }
                } else {
                    Log.i("ACCESSLEVELS", "Can not found AccessLevels");
                    runOnTextViewTextUpdate(getAccessLevelResult, "Can not found AccessLevels");
                }
            }
        });

        connectToNearestDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // try connect to nearest Device when if found connectable device
                Sdk.connectNearestDevice();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                if(offText.getText() != null) {
                    offMessage = offText.getText().toString();
                }
                if(bleText.getText() != null) {
                    bleMessage = bleText.getText().toString();
                }
                if(nfcText.getText() != null) {
                    nfcMessage = nfcText.getText().toString();
                }
                if(allText.getText() != null) {
                    allMessage = allText.getText().toString();
                }

                Toast.makeText(mContext, "Message change success", Toast.LENGTH_LONG).show();
                offText.setText("");
                nfcText.setText("");
                bleText.setText("");
                allText.setText("");
                }
            });
            }
        });

        nfcBtn.setTextOn("Enable NFC");
        bleBtn.setTextOn("Enable BLE");
        nfcBtn.setTextOff("Disable NFC");
        bleBtn.setTextOff("Disable BLE");

        if(mNFCtoggle) {
            nfcBtn.setChecked(true);
        }

        if(mBLEtoggle) {
            bleBtn.setChecked(true);
        }

        showCardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ShowCardActivity.class);
                startActivity(intent);
            }
        });
        // UI & Button Controller

        SDKInitialize();
    }

    private void runOnButtonTextUpdate(final Button btn, final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btn.setText(msg);
            }
        });
    }

    private void runOnTextViewTextUpdate(final TextView tv, final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setText(msg);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Map<String, Integer> perms = new HashMap<String, Integer>();
        // Fill with results
        for (int i = 0; i < permissions.length; i++)
            perms.put(permissions[i], grantResults[i]);
        switch (requestCode) {
            case REQUEST_CODE_ASK_LOCATION_MULTIPLE_PERMISSIONS: {
                // Check for ACCESS_FINE_LOCATION
                if (!perms.containsValue(PackageManager.PERMISSION_DENIED)) {
                    // All Permissions Granted
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "All Permission GRANTED !! Thank You :)", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                    // start scan after get permission.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        backgroundLocationPermissionCheck();
                    } else {
                        bluetoothPermissionCheck();
                    }
                } else {
                    // Permission Denied
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "One or More Permissions are DENIED Exiting App :(", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                    finish();
                }
            }
            break;
            case REQUEST_CODE_ASK_BACKGROUND_LOCATION_MULTIPLE_PERMISSIONS:
                bluetoothPermissionCheck();
                break;
            case REQUEST_CODE_ASK_BLUETOOTH_MULTIPLE_PERMISSIONS:
                if (!perms.containsValue(PackageManager.PERMISSION_DENIED)) {
                    SupremaAc.getInstance().initBLE();
                    SupremaAc.getInstance().startBleScan();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SupremaAc.getInstance().onPauseEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SupremaAc.getInstance().onResumeEvent();
    }

    private void locationPermissionCheck() {
        final List<String> permissionsList = new ArrayList<String>();
        addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION);
        addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            addPermission(permissionsList, Manifest.permission.FOREGROUND_SERVICE);
        }
        requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                REQUEST_CODE_ASK_LOCATION_MULTIPLE_PERMISSIONS);
    }

    private void backgroundLocationPermissionCheck() {
        final List<String> permissionsList = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            addPermission(permissionsList, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
        requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                REQUEST_CODE_ASK_BACKGROUND_LOCATION_MULTIPLE_PERMISSIONS);
    }

    private void bluetoothPermissionCheck() {
        final List<String> permissionsList = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            addPermission(permissionsList, Manifest.permission.BLUETOOTH_CONNECT);
            addPermission(permissionsList, Manifest.permission.BLUETOOTH_ADVERTISE);
            addPermission(permissionsList, Manifest.permission.BLUETOOTH_SCAN);
        } else {
            addPermission(permissionsList, Manifest.permission.BLUETOOTH);
            addPermission(permissionsList, Manifest.permission.BLUETOOTH_ADMIN);
        }
        requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                REQUEST_CODE_ASK_BLUETOOTH_MULTIPLE_PERMISSIONS);
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setCancelable(true)
                .create()
                .show();
    }


    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {
        permissionsList.add(permission);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != SupremaAc.getInstance()) {
            SupremaAc.getInstance().clear();
            SupremaAc.getInstance().stopBleScan();
            SupremaAc.getInstance().stopService();
        }
    }

    private void runVibration() {
        Log.i(TAG, "run vibration : ##");

        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != bleWait) {
                    bleWait.setTextColor(getResources().getColor(R.color.colorAccent));
                    bleWait.setText("BLE - Waiting");
                }
            }
        });

        final Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(100);
    }

    private void testAppSetup() {
        boolean hce = SupremaAc.getInstance().isSupportedHCE();
        if(hce) {
            hceSupport.setText("Support");
        }
        SupremaAc.getInstance().setVibrationAtCertification(100, 3000);
        mNFCtoggle = SupremaAc.getInstance().getNFCFlag();
        mBLEtoggle = SupremaAc.getInstance().getBleFlag();
        if(mNFCtoggle) {
            nfcBtn.setChecked(true);
        }
        if(mBLEtoggle) {
            bleBtn.setChecked(true);
        }
        locationPermissionCheck();
        setListDropDown();
        setCardActivatedStatus(true);
    }

    private void setListDropDown() {
        if (listDropDown == null) return;
        String[] list = SupremaAc.getInstance().getCardIdList();
        if (list != null) {
            idList = list;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, list);
            listDropDown.setAdapter(adapter);
            String currentId = SupremaAc.getInstance().getUsingCard();
            int i;
            for (i = 0; i < idList.length; i++) {
                if (idList[i].equals(currentId)) break;
            }
            listDropDown.setSelection(i);
        } else {
            idList = null;
            listDropDown.setAdapter(null);
        }
    }

    private void setCardActivatedStatus(boolean available) {
        if (cardActivated != null) {
            cardActivated.setText("activated : " + SupremaAc.getInstance().checkActivated() +
                    ", card : " + SupremaAc.getInstance().checkCard());
            Sdk.setCardActivated(available);
        }
    }

    private ArrayList<HashMap<String, Object>> getAls(HashMap<String, Object> b){
        while(b== null);
        try{
            HashMap<String, Object> record = (HashMap<String, Object>) b.get("record");
            ArrayList<HashMap<String, Object>> als = (ArrayList<HashMap<String, Object>>) record.get("accessLevels");
            return als;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private HashMap<String, Object> getCardExtraInfo(HashMap<String, Object> b){
        while(b== null);
        try{
            HashMap<String, Object> record = (HashMap<String, Object>) b.get("record");
            return record;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

class ExampleCard {
    static HashMap<String, Object> res = null;
    static ArrayList<HashMap<String, Object>> reslist = null;

    static public HashMap<String, Object> getSubIDCard() throws Exception {
        String testString = "";

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> recordObj = null;
        recordObj = mapper.readValue(ExampleCard.CardBody, HashMap.class);
        ExampleCard.res = new HashMap<String, Object>(recordObj);
        Log.i("TESTCARD", CardBody);
        return ExampleCard.res;
    }

    static String CardBody=
            "{\n" +
            "   \"status\":200,\n" +
            "   \"message\":\"success\",\n" +
            "   \"record\":{\n" +
            "      \"credential\":\"W3siY2FyZERhdGEiOnsiZm9ybWF0Q29kZSI6IjAiLCJlbmNUeXBlIjoiMyIsInNpZ25hdHVyZSI6Ik1FVUNJQW03bnVmOGJsKzVkakRXRnpNbURUV0drZ3M2cm9aRitxeEFzMWUydmJycUFpRUFtUUZLajRjelc4VUN3bHMxTFgxU3pTRHJ2Yk1WeTBCOUhuZFpCa0VuMmd3PSIsImVuZERhdGUiOjE2MjEzNTQxNTkwMDAsImNhcmROdW1iZXJCaXQiOjE2LCJyZWZyZXNoIjowLCJzdGFydERhdGUiOjE2MjAzNTQxNTkwMDAsImNhcmROdW1iZXIiOiJybXQyWkl2Y2VYdldIUnloeWRWZHFRPT0ifX0seyJjYXJkRGF0YSI6eyJmb3JtYXRDb2RlIjoiMCIsImVuY1R5cGUiOiIzIiwic2lnbmF0dXJlIjoiTUMwQ0ZRRDVYRllTWmNaRmZDbGthNHVWWXhWWWkydzA0d0lVU1BzOG5SclB6cjJ2OVhKa0dXQWRnUGQ4anZFPSIsImVuZERhdGUiOjE2MjEzNTQxNTkwMDAsImNhcmROdW1iZXJCaXQiOjE2LCJyZWZyZXNoIjowLCJzdGFydERhdGUiOjE2MjAzNTQxNTkwMDAsImNhcmROdW1iZXIiOiJQWXdiRm02TDJkSlhZa2F4d0tzRnlBPT0ifX1d\",\n" +
            "      \"cardType\":\"2\",\n" +
            "      \"cardId\":\"130526\"\n" +
            "   }\n" +
            "}";
}

class ExampleAccessLevel {
    static HashMap<String, Object> res = null;
    static ArrayList<HashMap<String, Object>> reslist = null;

    public HashMap<String, Object> getBody(int testType) throws Exception{
        String testString = "";

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> recordObj = null;
        if(MainActivity.isFullAccess){
            Log.i("BODY", this.FullAccessBody);
            recordObj = mapper.readValue(this.FullAccessBody, HashMap.class);
        } else {
            Log.i("BODY", this.CustomAccessBody);
            recordObj = mapper.readValue(this.CustomAccessBody, HashMap.class);
        }
        ExampleAccessLevel.res = new HashMap<String, Object>(recordObj);
        return ExampleAccessLevel.res;
    }

    static String FullAccessBody=
                    "{\n" +
                    "   \"status\":200,\n" +
                    "   \"message\":\"success\",\n" +
                    "   \"record\":{\n" +
                    "      \"cardId\":\"130526\",\n" +
                    "      \"lastModifiedAt\":1611032847085,\n" +
                    "      \"accessLevels\":[\n" +
                    "         {\n" +
                    "            \"id\":\"1\",\n" +
                    "            \"type\":\"0\",\n" +
                    "            \"lastModifiedAt\":1611032846993,\n" +
                    "            \"siteId\":\"" +MainActivity.siteId+ "\",\n" +
                    "            \"weekLimitation\":0,\n" +
                    "            \"devices\":[\n" +
                    "               {\n" +
                    "                  \"id\":\"2\",\n" +
                    "                  \"serial\":\""+MainActivity.targetSerial+"\",\n" +
                    "                  \"model\":\"XPASS2\",\n" +
                    "                  \"name\":\"5\",\n" +
                    "                  \"timeZone\":\"America/Anchorage\",\n" +
                    "                  \"attribute\":\"1\"\n" +
                    "               }\n" +
                    "            ]\n" +
                    "         }\n" +
                    "      ]\n" +
                    "   }\n" +
                    "}";
    static String CustomAccessBody=
            "{\n" +
                    "   \"status\":200,\n" +
                    "   \"message\":\"success\",\n" +
                    "   \"record\":{\n" +
                    "      \"cardId\":\"1\",\n" +
                    "      \"lastModifiedAt\":1611032847085,\n" +
                    "      \"accessLevels\":[\n" +
                    "         {\n" +
                    "            \"id\":\"1\",\n" +
                    "            \"type\":\"1\",\n" +
                    "            \"lastModifiedAt\":1611032846993,\n" +
                    "            \"siteId\":\"" +MainActivity.siteId+ "\",\n" +
                    "            \"weekLimitation\":0,\n" +
                    "            \"timeZones\":[\n" +
                    "               {\n" +
                    "                  \"timeZone\":\"America/New_York\",\n" +
                    "                  \"dst\":{\n" +
                    "                     \"id\":\"2\",\n" +
                    "                     \"siteId\":\"" +MainActivity.siteId+ "\",\n" +
                    "                     \"start\":{\n" +
                    "                        \"month\":2,\n" +
                    "                        \"week\":1,\n" +
                    "                        \"dayOfWeek\":\"MONDAY\",\n" +
                    "                        \"time\":\"02:00\"\n" +
                    "                     },\n" +
                    "                     \"end\":{\n" +
                    "                        \"month\":8,\n" +
                    "                        \"week\":5,\n" +
                    "                        \"dayOfWeek\":\"THURSDAY\",\n" +
                    "                        \"time\":\"01:00\"\n" +
                    "                     },\n" +
                    "                     \"timeOffsetMinute\":30\n" +
                    "                  }\n" +
                    "               },\n" +
                    "               {\n" +
                    "                  \"timeZone\":\"Asia/Seoul\",\n" +
                    "                  \"dst\":{\n" +
                    "                     \"id\":\"1\",\n" +
                    "                     \"siteId\":\"" +MainActivity.siteId+ "\",\n" +
                    "                     \"start\":{\n" +
                    "                        \"month\":3,\n" +
                    "                        \"week\":3,\n" +
                    "                        \"dayOfWeek\":\"MONDAY\",\n" +
                    "                        \"time\":\"02:00\"\n" +
                    "                     },\n" +
                    "                     \"end\":{\n" +
                    "                        \"month\":9,\n" +
                    "                        \"week\":5,\n" +
                    "                        \"dayOfWeek\":\"THURSDAY\",\n" +
                    "                        \"time\":\"01:00\"\n" +
                    "                     },\n" +
                    "                     \"timeOffsetMinute\":30\n" +
                    "                  }\n" +
                    "               }\n" +
                    "            ],\n" +
                    "            \"devices\":[\n" +
                    "               {\n" +
                    "                  \"id\":\"2\",\n" +
                    "                  \"serial\":\""+MainActivity.targetSerial+"\",\n" +
                    "                  \"model\":\"XPASS2\",\n" +
                    "                  \"name\":\"5\",\n" +
                    "                  \"timeZone\":\"America/Anchorage\",\n" +
                    "                  \"attribute\":\"1\"\n" +
                    "               }\n" +
                    "            ],\n" +
                    "            \"weekSchedules\":[\n" +
                    "               {\n" +
                    "                  \"dayOfWeek\":\"MONDAY\",\n" +
                    "                  \"limitation\":0,\n" +
                    "                  \"times\":[\n" +
                    "                     {\n" +
                    "                        \"st\":\"11:00\",\n" +
                    "                        \"et\":\"15:00\",\n" +
                    "                        \"limitation\":0\n" +
                    "                     }\n" +
                    "                  ]\n" +
                    "               },\n" +
                    "               {\n" +
                    "                  \"dayOfWeek\":\"TUESDAY\",\n" +
                    "                  \"limitation\":0,\n" +
                    "                  \"times\":[\n" +
                    "                     {\n" +
                    "                        \"st\":\"00:00\",\n" +
                    "                        \"et\":\"23:59\",\n" +
                    "                        \"limitation\":0\n" +
                    "                     }\n" +
                    "                  ]\n" +
                    "               },\n" +
                    "               {\n" +
                    "                  \"dayOfWeek\":\"WEDNESDAY\",\n" +
                    "                  \"limitation\":0,\n" +
                    "                  \"times\":[\n" +
                    "                     {\n" +
                    "                        \"st\":\"08:00\",\n" +
                    "                        \"et\":\"11:00\",\n" +
                    "                        \"limitation\":0\n" +
                    "                     },\n" +
                    "                     {\n" +
                    "                        \"st\":\"15:00\",\n" +
                    "                        \"et\":\"18:00\",\n" +
                    "                        \"limitation\":0\n" +
                    "                     }\n" +
                    "                  ]\n" +
                    "               },\n" +
                    "               {\n" +
                    "                  \"dayOfWeek\":\"THURSDAY\",\n" +
                    "                  \"limitation\":0,\n" +
                    "                  \"times\":[\n" +
                    "                     {\n" +
                    "                        \"st\":\"11:00\",\n" +
                    "                        \"et\":\"12:00\",\n" +
                    "                        \"limitation\":0\n" +
                    "                     }\n" +
                    "                  ]\n" +
                    "               },\n" +
                    "               {\n" +
                    "                  \"dayOfWeek\":\"FRIDAY\",\n" +
                    "                  \"limitation\":0,\n" +
                    "                  \"times\":[\n" +
                    "                     {\n" +
                    "                        \"st\":\"08:00\",\n" +
                    "                        \"et\":\"21:00\",\n" +
                    "                        \"limitation\":0\n" +
                    "                     }\n" +
                    "                  ]\n" +
                    "               }\n" +
                    "            ],\n" +
                    "            \"holiday\":{\n" +
                    "               \"id\":\"1\",\n" +
                    "               \"siteId\":\"" +MainActivity.siteId+ "\",\n" +
                    "               \"name\":\"holiday group 1\",\n" +
                    "               \"holidays\":[\n" +
                    "                  {\n" +
                    "                     \"id\":\"1\",\n" +
                    "                     \"name\":\"TestHoliday 1\",\n" +
                    "                     \"startDate\":\"2021-01-05\"\n" +
                    "                  },\n" +
                    "                  {\n" +
                    "                     \"id\":\"3\",\n" +
                    "                     \"name\":\"TestHoliday 2\",\n" +
                    "                     \"startDate\":\"2021-08-15\"\n" +
                    "                  },\n" +
                    "                  {\n" +
                    "                     \"id\":\"2\",\n" +
                    "                     \"name\":\"TestHoliday 3\",\n" +
                    "                     \"startDate\":\"2021-08-25\",\n" +
                    "                     \"endDate\":\"2021-08-28\"\n" +
                    "                  },\n" +
                    "                  {\n" +
                    "                     \"id\":\"5\",\n" +
                    "                     \"name\":\"TestHoliday 4\",\n" +
                    "                     \"startDate\":\"2021-09-06\"\n" +
                    "                  },\n" +
                    "                  {\n" +
                    "                     \"id\":\"4\",\n" +
                    "                     \"name\":\"TestHoliday 5\",\n" +
                    "                     \"startDate\":\"2021-12-25\"\n" +
                    "                  }\n" +
                    "               ]\n" +
                    "            }\n" +
                    "         }\n" +
                    "      ]\n" +
                    "   }\n" +
                    "}";


}
