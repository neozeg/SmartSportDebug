package com.mupro.smartsportdebug;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mupro.smartsportdebug.Views.WaveformView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BleActivity implements BleDevice.BleBroadcastReceiver {

    private final static String TAG = "MainActivity";

    private final static int SENDCODE_INTERVAL = 100;
    private final static int UPLOAD_INTERVAL = 5;
    private final static int UPLOAD_TIMEOUT = 2000;

    private final static int AVG_SAMPLE_DURATION = 3000;
    private final static int AVG_SAMPLE_INTERVAL = 20;
    private final static int AVG_SAMPLE_INTERVAL2 = 100;
    private final static int AVG_SAMPLE_LENGTH = 10;

    private final static int OPEN_FILE_DIALOG_ID = 1;
    private final static int REQUEST_CONNECT_DEVICE = 1;
    private final static int STATE_DISCONNECTED = BluetoothProfile.STATE_DISCONNECTED;
    private final static int STATE_CONNECTING = BluetoothProfile.STATE_CONNECTING;
    private final static int STATE_CONNECTED = BluetoothProfile.STATE_CONNECTED;


    //private final static int SENSOR_SET_ZERO_PUSHBAR_SET_ZERO = 0x10;
    //private final static int SENSOR_SET_ZERO_PUSHBAR_SET_MAX = 0x11;
    //private final static int SENSOR_SET_ZERO_ABSWHEEL_SET_ZERO = 0x30;
    //private final static int SENSOR_SET_ZERO_ABSWHEEL_SET_MAX = 0x31;
    //private final static int SENSOR_SET_ZERO_RESBAND_SET_0 = 0x40;
    //private final static int SENSOR_SET_ZERO_RESBAND_SET_1 = 0x41;
    //private final static int SENSOR_SET_ZERO_RESBAND_SET_2 = 0x42;
    private final static int SENSOR_PRESSURE_RESET = 0x10;
    private final static int SENSOR_PRESSURE_CALIBRATION = 0x20;
    private final static int SENSOR_SET_ZERO_GSENSOR = 0x50;
    //private final static int SENSOR_SET_ZERO_GYROSENSOR = 0x60;

    private final static int PACKET_AVAL_DATA_LENGTH = 16;

    private final static int WAVEFORM_DATA_LENGTH = 64;

    private int mConnectState = STATE_DISCONNECTED;
    private String mDeviceAddress,mDeviceName,mDeviceNewName;
    private PowerManager.WakeLock mWakeLock;

    private boolean isWorking = false;

    //View Components

    //private Button mBtnLt,mBtnRt,mBtnUp,mBtnDn,mBtnF1,mBtnF2;
    private WaveformView mWaveformView1;
    private ScrollView mSVDebugMsg,mSVDebugScope;
    private Button mBtnScPress,mBtnScRoll,mBtnSc3d;
    private TextView mTvCh1,mTvCh2,mTvCh3,mTvCh4,mTvCh5,mTvCh6;
    private TextView mTvMsgCh1,mTvMsgCh2,mTvMsgCh3,mTvMsgCh4,mTvMsgCh5,mTvMsgCh6;
    private TextView mTvTxMessage,mTvRxMessage,mTvVersion,mTvDeviceName;
    private Button mBtnDevice;
    //private Button mBtnCmd1,mBtnCmd2,mBtnCmd3,mBtnCmd4,mBtnCmd5,mBtnCmd6,mBtnCmd7,mBtnCmd8;
    private Switch mSwRpt,mSwRpt6xL,mSwRpt6xR,mSwRptAttitude,mSwRptPress;
    private Button mBtnRptFw,mBtnRptDeviceId,mBtnCmdSleep;
    private EditText mEtRefVal;
    private Button mBtnPressReset,mBtnPressCalibrate,mBtnGyroReset;
    private Button mBtnResBP1R,mBtnResBP2R,mBtnResBP3R,mBtnResBP4R,mBtnResBP1L,mBtnResBP2L,mBtnResBP3L,mBtnResBP4L;
    private Button mBtnOTAEnterR,mBtnOTABlankR,mBtnOTAEraseR,mBtnOTAProgramR,mBtnOTAVerifyR,mBtnOTAExitR;
    private Button mBtnOTAEnterL,mBtnOTABlankL,mBtnOTAEraseL,mBtnOTAProgramL,mBtnOTAVerifyL,mBtnOTAExitL;
    //private TextView mTvInfoProp,mTvInfoDevice,mTvInfo0,mTvInfo1,mTvInfo2,mTvInfo3,mTvInfo4,mTvInfo5,mTvInfo6,mTvInfo7,mTvInfo8,mTvInfo9,mTvInfo10,mTvInfoDebug;
    private TextView mTvMsg00,mTvMsg01,mTvMsg10,mTvMsg11,mTvMsg12,mTvMsg20,mTvMsg21,mTvMsg22,mTvMsg30,mTvMsg31,mTvMsg32,mTvMsg40,mTvMsg50,mTvMsg60,mTvInfoDebug;
    private EditText mEtHeight,mEtWeight,mEtInfoReserved,mEtInfoPushbar,mEtInfoAbsWheel,mEtInfoPressMaxL,mEtInfoPressMaxR;
    private EditText mEtUserId[];
    private Button mBtnUserInfoR,mBtnUserInfoW;

    private EditText mEtPairCode;
    private Button mBtnGenCode,mBtnSendPCode;

    private EditText mEtOldBleName,mEtNewBleName;
    private Button mBtnBleRenameSubmit;
    private Button mBtnBleTxPowerSubmit;
    private RadioButton mRBTx0,mRBTx1,mRBTx2,mRBTx3;

    private TextView mTvMsgExt00,mTvMsgExt01,mTvMsgExt10,mTvMsgExt11,mTvMsgExt20,mTvMsgExt21,mTvMsgExt30,mTvMsgExt31;

    //private ViewFlipper mViewFlipperBottom;
    private ViewPager viewPagerBottom;
    private ArrayList<View> pageBottomViews;
    private ViewPager viewPagerTop;
    private ArrayList<View> pageTopViews;

    private Animation mAnimationTx,mAnimationRx,mBlinkAnimation;

    private Dialog mConfirmWriteDialog;
    private int afterConfirmWriteStep=0;
    private final static int CONFIRM_BLE_RENAME = 1;
    private final static int CONFIRM_BLE_TXPOWER = 2;
    private final static int CONFIRM_SENSOR_RESET = 3;
    // private AlertDialog.Builder mConfirmWriteDialog;

    private SharedPreferences mDeviceConfigPref;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String DEVICE_NAME = "device_name";
    private static final String CONNECT_CONFIG = "connect_config";

    private final static int REPORT_TYPE_NORMALMESSAGE = 0;
    private final static int REPORT_TYPE_6AXILSENSOR_LEFT = 2;
    private final static int REPORT_TYPE_6AXILSENSOR_RIGHT = 1;
    private final static int REPORT_TYPE_ATTITUDE = 3;
    private final static int REPORT_TYPE_PRESSURE = 4;
    private final static int REPORT_TYPE_DEVICE_ID = 10;
    private final static int REPORT_TYPE_FIRMWARE = 11;
    private final static int REPORT_TYPE_FIRST_MSG = 12;

    private int reportType  = 0;
    private int buttonReportCnt = 0;
    private boolean isOTARight;
    private long backClickTimeStamp;
    private int backClickCnt;
    private byte[] dataBytes;
    private byte[] pairCode;
    private byte[] pairCodeSub;
    private byte[] pairCodeFb1;
    private byte[] pairCodeFb2;
    private byte[] pairCode2;

    //private short[] waveformData1;
    //private short[] waveformData2;
    //private short[] waveformData3;
    //private short[] waveformData4;
    //private short[] waveformData5;
    //private short[] waveformData6;
    private final static int SCOPE_SEL_PRESSURE = 0;
    private final static int SCOPE_SEL_ATTITUDE = 1;
    private final static int SCOPE_SEL_6AXIS_LEFT = 2;
    private final static int SCOPE_SEL_6AXIS_RIGHT = 3;
    private final static int SCOPE_SEL_ACCEL_LEFT = 4;
    private final static int SCOPE_SEL_ACCEL_RIGHT = 5;
    private final static int SCOPE_SEL_GYRO_LEFT = 6;
    private final static int SCOPE_SEL_GYRO_RIGHT = 7;
    private int mScopeSel=0;
    private short[] pressureZeroDataL = new short[WAVEFORM_DATA_LENGTH];
    private short[] pressureZeroDataR = new short[WAVEFORM_DATA_LENGTH];
    private short[] pressureRawDataL = new short[WAVEFORM_DATA_LENGTH];
    private short[] pressureRawDataR = new short[WAVEFORM_DATA_LENGTH];
    private short[] pressureCalDataL = new short[WAVEFORM_DATA_LENGTH];
    private short[] pressureCalDataR = new short[WAVEFORM_DATA_LENGTH];
    private short[] yawDataL = new short[WAVEFORM_DATA_LENGTH];
    private short[] yawDataR = new short[WAVEFORM_DATA_LENGTH];
    private short[] pitchDataL = new short[WAVEFORM_DATA_LENGTH];
    private short[] pitchDataR = new short[WAVEFORM_DATA_LENGTH];
    private short[] rollDataL = new short[WAVEFORM_DATA_LENGTH];
    private short[] rollDataR = new short[WAVEFORM_DATA_LENGTH];
    private short gDataArrayL[][] = new short[6][WAVEFORM_DATA_LENGTH];
    private short gDataArrayR[][] = new short[6][WAVEFORM_DATA_LENGTH];
    //private short[] axDataL = new short[WAVEFORM_DATA_LENGTH];
    //private short[] ayDataL = new short[WAVEFORM_DATA_LENGTH];
    //private short[] azDataL = new short[WAVEFORM_DATA_LENGTH];
    //private short[] gxDataL = new short[WAVEFORM_DATA_LENGTH];
    //private short[] gyDataL = new short[WAVEFORM_DATA_LENGTH];
    //private short[] gzDataL = new short[WAVEFORM_DATA_LENGTH];
    //private short[] axDataR = new short[WAVEFORM_DATA_LENGTH];
    //private short[] ayDataR = new short[WAVEFORM_DATA_LENGTH];
    //private short[] azDataR = new short[WAVEFORM_DATA_LENGTH];
    //private short[] gxDataR = new short[WAVEFORM_DATA_LENGTH];
    //private short[] gyDataR = new short[WAVEFORM_DATA_LENGTH];
    //private short[] gzDataR = new short[WAVEFORM_DATA_LENGTH];

    private AppCommands appCmd = new AppCommands();

    //info data
    private int valPressureL,valPressureR;
    private int valPressureRawL,valPressureRawR;

    //private int maxAxL,maxAyL,maxAzL,maxGxL,maxGyL,maxGzL;
    //private int maxAxR,maxAyR,maxAzR,maxGxR,maxGyR,maxGzR;
    //private int minAxL,minAyL,minAzL,minGxL,minGyL,minGzL;
    //private int minAxR,minAyR,minAzR,minGxR,minGyR,minGzR;
    //private int valAxL,valAyL,valAzL,valGxL,valGyL,valGzL;
    //private int valAxR,valAyR,valAzR,valGxR,valGyR,valGzR;
    private int valGensorL[] = new int[6];
    private int valGensorR[] = new int[6];
    private int valYawL,valPitchL,valRollL,valYawR,valPitchR,valRollR;

    private int deviceTxPowerSel=2;
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mWakeLock.acquire();
        Log.v(TAG, "mWakeLock.acquire()");
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        stopContinueReportCmd();
        avgSamplingTimer.removeCallbacks(avgSamplingRunnable);
        super.onPause();
        Log.v(TAG, "onPause()");
        if(mWakeLock.isHeld()){
            mWakeLock.release();
            Log.v(TAG, "mWakeLock.release()");
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Log.v(TAG, "onStop()");
        if(mWakeLock.isHeld()){
            mWakeLock.release();
            Log.v(TAG, "mWakeLock.release()");
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.v(TAG,"onDestroy()");
        super.onDestroy();
        //bleApp.manager.bleDevice.disableGattNotification();

        if(bleApp.manager.bleDevice!=null)
        {
            isWorking = false;
            bleApp.manager.bleDevice.disconnectDevice();
            bleApp.manager.bleDevice.unbindService();
            bleApp.manager.bleDevice.unregisterRecevier();
            //bleApp.manager.bleDevice.setBleBroadcastReceiver(null);
            bleApp.manager.bleDevice = null;
        }
        //ReconnectTimer.removeCallbacks(ReconnectRunnable);
        if(mWakeLock.isHeld()){
            mWakeLock.release();
            Log.v(TAG, "mWakeLock.release()");
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        PowerManager pm=(PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,"bright");
        //mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");

        bleApp.manager.isEnabled(MainActivity.this);
        //bleApp.manager.bleDevice.setBleBroadcastReceiver(this);


        loadConnectConfig();

        setupViewComponents();

        if(mDeviceAddress != null){
            reconnectDevice();
        }else{
            setupConnectDialog();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
		/*
		if (id == R.id.action_settings) {

			return true;
		}*/
        if(id == R.id.reconnect){

            setupConnectDialog();

        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode == KeyEvent.KEYCODE_BACK){
            //

            long timeDiff = System.currentTimeMillis() - backClickTimeStamp;
            backClickTimeStamp = System.currentTimeMillis();
            if(timeDiff > 2000){
                backClickCnt = 0;
                Toast.makeText(getApplicationContext(), "Press Back Button Again To Exit", Toast.LENGTH_SHORT).show();
                return true;
            }else{
                backClickCnt ++;
                if(backClickCnt>0){
                    backClickCnt=0;
                    return super.onKeyDown(keyCode, event);
                }else{
                    return true;
                }
            }

        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_CONNECT_DEVICE:
                if(resultCode == Activity.RESULT_OK){
                    //bleApp.manager.bleDevice.connectDevice(data);
                    Log.v(TAG, "connect device ok");
                    bleApp.manager.bleDevice.setBleBroadcastReceiver(this);
                }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent, String uuid) {
        // TODO Auto-generated method stub
        final String action = intent.getAction();
        if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
            //Log.v(TAG, "ACTION_GATT_CONNECTED");
            bleConnected();
        } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
            //Log.v(TAG, "ACTION_GATT_DISCONNECTED");
            bleDisconnected();
        } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            //Log.v(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
            bleServiceDiscovered();
        } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            //Log.v(TAG, "ACTION_DATA_AVAILABLE\n"+uuid);
            bleDataAvailable(intent);
        } else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)){
            //Log.v(TAG, "ACTION_DATA_WRITE");
            bleDataWrite();
        }

    }

    private void bleConnected(){
        //Toast.makeText(getApplicationContext(), "device connected", Toast.LENGTH_SHORT).show();
        mConnectState = STATE_CONNECTING;
        updateConnectionState();

    }
    private void bleDisconnected(){
        Toast.makeText(getApplicationContext(), "Device Disconnected", Toast.LENGTH_SHORT).show();
        Log.v(TAG, "Device Disconnected");
        mConnectState = STATE_DISCONNECTED;
        updateConnectionState();
            if(isWorking){
            reconnectDevice();
        }
    }
    private void bleServiceDiscovered(){
        //Toast.makeText(getApplicationContext(), "service found", Toast.LENGTH_SHORT).show();
        Log.v(TAG, "Service found");
        if(bleApp.manager.bleDevice.setCharacteristicNotification(SampleGattAttributes.SMARTEQUIT_SERVICE,
                SampleGattAttributes.SMARTEQUIT_SERVICE_NOTIFY, true))
        {
            Toast.makeText(getApplicationContext(), "Device Connected", Toast.LENGTH_SHORT).show();
            mConnectState = STATE_CONNECTED;
            updateConnectionState();
            mDeviceAddress = bleApp.manager.bleDevice.getAddress();
            mDeviceName = bleApp.manager.bleDevice.getName();
            mDeviceNewName = DeviceRename.getNewName(mDeviceAddress);
            mTvDeviceName.setText(mDeviceName);
            mEtNewBleName.setText(mDeviceNewName);
            if(mEtUserId!=null)
            if(mEtUserId[0]!=null){
                for(int i=0;i<6;i++){
                    mEtUserId[i].setText(mDeviceNewName.substring(i*2+3,i*2+5));
                }
            }
            saveConnectConfig();


            //sendBleData(appCmd.txCmd1_0);
            reportType = REPORT_TYPE_DEVICE_ID;
            ReportCmdTimer.postDelayed(ReportCmdRunnable,SENDCODE_INTERVAL);
        }
    }
    private void bleDataAvailable(Intent intent){
        byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);

        if(data!=null && data.length > 0){
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            mTvRxMessage.setText(stringBuilder.toString());
            mTvRxMessage.startAnimation(mAnimationRx);
            processData(data);
        }
    }
    private void bleDataWrite(){
    }

    private void updateConnectionState(){
        switch(mConnectState){
            case STATE_DISCONNECTED:
                mBtnDevice.setText("Disconnected");
                mBtnDevice.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
            case STATE_CONNECTING:
                mBtnDevice.setText("Connecting...");
                mBtnDevice.setTextColor(getResources().getColor(android.R.color.darker_gray));
                break;
            case STATE_CONNECTED:
                mBtnDevice.setText("Connected");
                mBtnDevice.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                mTvDeviceName.setText(bleApp.manager.bleDevice.getName());
                break;
        }
    }


    private void saveConnectConfig(){
        mDeviceConfigPref.edit()
                .putString(DEVICE_ADDRESS, mDeviceAddress)
                .putString(DEVICE_NAME,mDeviceName).commit();
    }
    private void loadConnectConfig(){
        mDeviceConfigPref = getSharedPreferences(CONNECT_CONFIG, 0);
        mDeviceAddress = mDeviceConfigPref.getString(DEVICE_ADDRESS, null);
        mDeviceName = mDeviceConfigPref.getString(DEVICE_NAME,"null");
    }
    private void reconnectDevice(){
        if(bleApp.manager.bleDevice!=null){
            isWorking = false;
            bleApp.manager.bleDevice.disconnectDevice();
            bleApp.manager.bleDevice.unbindService();
            bleApp.manager.bleDevice.unregisterRecevier();
            //bleApp.manager.bleDevice.setBleBroadcastReceiver(null);
            bleApp.manager.bleDevice = null;
        }
        bleApp.manager.setBleDevice(mDeviceAddress);
        bleApp.manager.bleDevice.setBleBroadcastReceiver(MainActivity.this);
        //ReconnectTimer.postDelayed(ReconnectRunnable, 10000);
    }

    private void setupConnectDialog(){


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alertDialogBuilder.setTitle("Connect a device");
        alertDialogBuilder.setMessage("Connect a new device?");

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                mConnectState = STATE_DISCONNECTED;
                updateConnectionState();

                if (bleApp.manager.bleDevice != null) {
                    isWorking = false;
                    bleApp.manager.bleDevice.disconnectDevice();
                    bleApp.manager.bleDevice.unbindService();
                    bleApp.manager.bleDevice.unregisterRecevier();
                    //bleApp.manager.bleDevice.setBleBroadcastReceiver(null);
                    bleApp.manager.bleDevice = null;
                }
                //ReconnectTimer.removeCallbacks(ReconnectRunnable);


                Intent intent = new Intent(getApplicationContext(), ScanDeviceActivity.class);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
            }
        });

        if(mConnectState == STATE_DISCONNECTED){
            alertDialogBuilder.setNeutralButton("reconnect device", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    reconnectDevice();
                }
            });
        }

        alertDialogBuilder.create().show();
    }






    private void sendBleData(byte[] bytes){

        byte[] data = new byte[bytes.length+1];
        for(int i=0;i<bytes.length;i++){
            data[i] = bytes[i];
            data[data.length-1]+=bytes[i];
        }
        if(data!=null && data.length > 0){
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            mTvTxMessage.setText(stringBuilder.toString());
            mTvTxMessage.startAnimation(mAnimationTx);
        }
        if(bleApp.manager.bleDevice != null)
            bleApp.manager.bleDevice.writeValue(SampleGattAttributes.SMARTEQUIT_SERVICE,
                    SampleGattAttributes.SMARTEQUIT_SERVICE_WRITE,
                    data);
    }
    private void sendReportCmd(){
        sendBleData(appCmd.txCmd1_1);
    }
    private void startContinueReportCmd(){
        buttonReportCnt = 2;
        switch (reportType){
            case REPORT_TYPE_NORMALMESSAGE:
                sendReportCmd();
                //mBtnCmd1.setTextColor(Color.RED);
                break;
            case REPORT_TYPE_6AXILSENSOR_RIGHT:
                sendBleData(appCmd.txCmdF_2);
                //mBtnCmd4.setTextColor(Color.RED);
                break;
            case REPORT_TYPE_6AXILSENSOR_LEFT:
                sendBleData(appCmd.txCmdF_1);
                //mBtnCmd5.setTextColor(Color.RED);
                break;
            case REPORT_TYPE_ATTITUDE:
                sendBleData(appCmd.txCmdF_3);
                //mBtnCmd6.setTextColor(Color.RED);
                break;
            case REPORT_TYPE_PRESSURE:
                sendBleData(appCmd.txCmd10);
                //mBtnCmd6.setTextColor(Color.RED);
                break;
        }
        ReportCmdTimer.postDelayed(ReportCmdRunnable, SENDCODE_INTERVAL);
    }
    private void stopContinueReportCmd(){
        //if(buttonReportCnt!=0)sendBleData(appCmd.txCmd1_2);
        buttonReportCnt=0;
        ReportCmdTimer.removeCallbacks(ReportCmdRunnable);
        if(mSwRpt!=null)mSwRpt.setChecked(false);
        if(mSwRpt6xL!=null)mSwRpt6xL.setChecked(false);
        if(mSwRpt6xR!=null)mSwRpt6xR.setChecked(false);
        if(mSwRptAttitude!=null)mSwRptAttitude.setChecked(false);
        if(mSwRptPress!=null)mSwRptPress.setChecked(false);
        //if(mBtnCmd1!=null)mBtnCmd1.setTextColor(Color.WHITE);
        //if(mBtnCmd4!=null)mBtnCmd4.setTextColor(Color.WHITE);
        //if(mBtnCmd5!=null)mBtnCmd5.setTextColor(Color.WHITE);
        //if(mBtnCmd6!=null)mBtnCmd6.setTextColor(Color.WHITE);
    }

    private final static int MSG_PROGRESS_CHANGE = 1;
    private final static int MSG_SEND_BLE_DATA = 2;
    private final static int MSG_SEND_BLE_ERR = 3;
    private final static int MSG_SEND_BLE_CANCEL = 4;
    private final static int MSG_SENSOR_SET_ZERO = 5;
    private final static int MSG_SHOW_WAITING_FEEDBACK_DIALOG = 6;
    private final static int MSG_SHOW_NOTICE_DIALOG = 7;
    private final static int MSG_SHOW_CONFIRM_WRITE_DIALOG = 8;
    private final static String  EXTRA_DIALOG_TITLE = "extra.dialog.title";

    private final static String MSG_BLE_BUFFER = "message_ble_buffer";
    private Handler mMsgHandler = new Handler(){
        long leftTime=0;
        long totalTime=0;
        long startTime=0;
        int progress=0;
        int lastProgess=0;
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            //super.handleMessage(msg);
            switch(msg.what){
                case MSG_PROGRESS_CHANGE:
                    progress = msg.arg1;
                    if(progress < 100){
                        /*
                        if(progress==2 && lastProgess==1){
                            startTime = System.currentTimeMillis();
                        }
                        if(startTime!=0 && progress!=lastProgess){
                            totalTime=(System.currentTimeMillis()-startTime)*mProgressDialog.getMax()/progress/1000;
                            leftTime = totalTime * (mProgressDialog.getMax()-progress)/mProgressDialog.getMax();
                            leftTime++;
                            mProgressDialog.setMessage(Long.toString(leftTime)+" second(s) left.");
                        }*/
                        mProgressDialog.setProgress(progress);
                        lastProgess = progress;
                        if(msg.arg2 == MSG_SEND_BLE_DATA)
                            sendBleData(msg.getData().getByteArray(MSG_BLE_BUFFER));
                    }else if(progress>=100){
                        startTime = 0;
                        mProgressDialog.dismiss();
                        if(msg.arg2 == MSG_SEND_BLE_DATA){
                            sendBleData(msg.getData().getByteArray(MSG_BLE_BUFFER));
                            updateDebugInfo("Checksum : 0x" + bytesToString(intToBytesMSB(otaBinChecksum), ""));
                        }
                    }
                    break;
                case MSG_SEND_BLE_DATA:
                    sendBleData(msg.getData().getByteArray(MSG_BLE_BUFFER));
                    break;
                case MSG_SEND_BLE_ERR:
                    mProgressDialog.dismiss();
                    updateDebugInfo("Upload Error!!!");
                    break;
                case MSG_SEND_BLE_CANCEL:
                    mProgressDialog.dismiss();
                    updateDebugInfo("Upload Canceled!!!");
                    break;
                case MSG_SHOW_WAITING_FEEDBACK_DIALOG:
                    String pdTitle = msg.getData().getString(EXTRA_DIALOG_TITLE);
                    mProgressDialog = new ProgressDialog(MainActivity.this);
                    mProgressDialog.setTitle(pdTitle);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.show();
                    break;
                case MSG_SHOW_NOTICE_DIALOG:
                    if(mProgressDialog!=null)mProgressDialog.dismiss();
                    String dTitle = msg.getData().getString(EXTRA_DIALOG_TITLE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(dTitle);
                    builder.setCancelable(true);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    break;
                case MSG_SHOW_CONFIRM_WRITE_DIALOG:
                    mConfirmWriteDialog.show();
                    break;
            }
        }

    };
    private static int AVG_DIFF_TH = 50;
    private int samplePointer;
    private int setZeroType = 0;
    private int setZeroType0 = 0;
    private void startAvgSampling(){
        samplePointer=0;
        avgSamplingTimer.postDelayed(avgSamplingRunnable,AVG_SAMPLE_INTERVAL);
        updateDebugInfo("Start Average Sampling");
    }
    private void stopAvgSamplingGSensor(){
        setZeroType = 0;
        avgSamplingTimer.removeCallbacks(avgSamplingRunnable);
    }
    private void stopAvgSamplingPSensor(){
        setZeroType = 0;
        avgSamplingTimer.removeCallbacks(avgSamplingRunnable);
    }
    private Handler avgSamplingTimer = new Handler();
    private Runnable avgSamplingRunnable = new Runnable() {
        @Override
        public void run() {

            switch(setZeroType) {
                case SENSOR_PRESSURE_RESET:
                    if(samplePointer < AVG_SAMPLE_DURATION/AVG_SAMPLE_INTERVAL2){
                        sendBleData(appCmd.txCmd10);
                        avgSamplingTimer.postDelayed(this, AVG_SAMPLE_INTERVAL2);
                    }else{
                        mProgressDialog.dismiss();
                        stopAvgSamplingPSensor();
                    }
                    break;

                case SENSOR_PRESSURE_CALIBRATION:
                    if(samplePointer < AVG_SAMPLE_DURATION/AVG_SAMPLE_INTERVAL2){
                        sendBleData(appCmd.txCmd10);
                        avgSamplingTimer.postDelayed(this, AVG_SAMPLE_INTERVAL2);
                    }else{
                        mProgressDialog.dismiss();
                        stopAvgSamplingPSensor();
                    }
                    break;
                case SENSOR_SET_ZERO_GSENSOR:
                    if(samplePointer < 300){
                        if(samplePointer%2 == 0)
                            sendBleData(appCmd.txCmdF_1);
                        else
                            sendBleData(appCmd.txCmdF_2);
                        samplePointer++;
                        avgSamplingTimer.postDelayed(this, AVG_SAMPLE_INTERVAL);
                        updateDebugInfo("Initializing Gsensor..."+samplePointer);

                    }else{
                        stopAvgSamplingGSensor();
                        updateDebugInfo("Initialize Gsensor Failed, No response");
                    }
            }
        }
    };

    private Handler ReportCmdTimer = new Handler();
    private Runnable ReportCmdRunnable = new Runnable() {
        @Override
        public void run() {
            switch (reportType){
                case REPORT_TYPE_NORMALMESSAGE:
                    buttonReportCnt = 2;
                    sendReportCmd();
                    ReportCmdTimer.postDelayed(this,SENDCODE_INTERVAL);
                    break;
                case REPORT_TYPE_6AXILSENSOR_RIGHT:
                    buttonReportCnt = 2;
                    sendBleData(appCmd.txCmdF_2);
                    ReportCmdTimer.postDelayed(this,SENDCODE_INTERVAL);
                    break;
                case REPORT_TYPE_6AXILSENSOR_LEFT:
                    buttonReportCnt = 2;
                    sendBleData(appCmd.txCmdF_1);
                    ReportCmdTimer.postDelayed(this,SENDCODE_INTERVAL);
                    break;
                case REPORT_TYPE_ATTITUDE:
                    buttonReportCnt = 2;
                    sendBleData(appCmd.txCmdF_3);
                    ReportCmdTimer.postDelayed(this,SENDCODE_INTERVAL);
                    break;
                case REPORT_TYPE_PRESSURE:
                    buttonReportCnt = 2;
                    sendBleData(appCmd.txCmd10);
                    ReportCmdTimer.postDelayed(this,SENDCODE_INTERVAL);
                    break;
                case REPORT_TYPE_DEVICE_ID:
                    reportType = REPORT_TYPE_FIRMWARE;
                    sendBleData(appCmd.txCmd2);
                    ReportCmdTimer.postDelayed(this,SENDCODE_INTERVAL*5);
                    break;
                case REPORT_TYPE_FIRMWARE:
                    reportType = REPORT_TYPE_FIRST_MSG;
                    sendBleData(appCmd.txCmd3);
                    ReportCmdTimer.postDelayed(this,SENDCODE_INTERVAL*5);
                    break;
                case REPORT_TYPE_FIRST_MSG:
                    sendReportCmd();
                    break;
            }
        }
    };
    private Handler ReconnectTimer = new Handler();

    private Runnable ReconnectRunnable = new Runnable(){

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if(mConnectState != STATE_CONNECTED){
                Log.v(TAG, "Connect failed, Reconnect!");
                reconnectDevice();
            }
        }

    };

    private void disconnectDevice(long delay){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mConnectState = STATE_DISCONNECTED;
                updateConnectionState();
                if(bleApp.manager.bleDevice!=null){
                    isWorking = false;
                    bleApp.manager.bleDevice.disconnectDevice();
                    bleApp.manager.bleDevice.unbindService();
                    bleApp.manager.bleDevice.unregisterRecevier();
                    //bleApp.manager.bleDevice.setBleBroadcastReceiver(null);
                    bleApp.manager.bleDevice = null;
                }
            }
        },delay);
    }

    /////////////////////////////////////////////////////////////
    // view components setup//
    /////////////////////////////////////////////////////////////
    @Override
    protected Dialog onCreateDialog(int id) {
        if(id==OPEN_FILE_DIALOG_ID){
            Map<String, Integer> images = new HashMap<String, Integer>();
            // 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
            images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);	// 根目录图标
            images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);	//返回上一层的图标
            images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);	//文件夹图标
            images.put("wav", R.drawable.filedialog_wavfile);	//wav文件图标
            images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
            Dialog dialog = OpenFileDialog.createDialog(id, this, "Open File", new CallbackBundle() {
                        @Override
                        public void callback(Bundle bundle) {
                            String filepath = bundle.getString("path");
                            //setTitle(filepath); // 把文件路径显示在标题上
                            Log.v(TAG,"path= "+ filepath);
                            processOtaData(filepath);
                        }
                    },
                    ".bin;",
                    images);
            return dialog;
        }
        return null;
    }


    private void setupTopPagesViewComponents(int page){
        Log.v(TAG,"setupTopPagesViewComponents"+page);
        switch (page){
            case 0:
                setupPageMessageViewComponents();
                break;
            case 1:
                setupPageScopeViewComponents();
                break;
        }
    }
    private void setupBottomPagesViewComponents(int page){
        switch(page){
            case 0:
                setupPageBleRenameViewComponents();
                break;
            case 1:
                setupPage1ViewComponents();
                break;
            case 2:
                setupPageSensorResetViewComponents();
                break;
            //case 2:
            //    setupPageResBandViewComponents();
            //    break;
            case 3:
                setupPageUserInfoViewComponents();
                break;
            case 4:
                setupPagePairCodeViewComponents();
                break;
            case 5:
                setupPageOTAViewComponents();
                break;

        }

    }
    private void setupPageScopeViewComponents(){
        mSVDebugScope = (ScrollView) findViewById(R.id.scrollViewWaveform);
        mWaveformView1 = (WaveformView) findViewById(R.id.WaveformView1);
        //mWaveformView1.setZOrderOnTop(true);
        //mWaveformView1.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mBtnScPress = (Button) findViewById(R.id.buttonScopePress);
        mBtnScRoll = (Button) findViewById(R.id.buttonScopeRoll);
        mBtnSc3d = (Button) findViewById(R.id.buttonScope3d);

        mBtnScPress.setOnClickListener(mOCLPageScope);
        mBtnScRoll.setOnClickListener(mOCLPageScope);
        mBtnSc3d.setOnClickListener(mOCLPageScope);

        mTvCh1 = (TextView) findViewById(R.id.tvColorCh1);
        mTvCh2 = (TextView) findViewById(R.id.tvColorCh2);
        mTvCh3 = (TextView) findViewById(R.id.tvColorCh3);
        mTvCh4 = (TextView) findViewById(R.id.tvColorCh4);
        mTvCh5 = (TextView) findViewById(R.id.tvColorCh5);
        mTvCh6 = (TextView) findViewById(R.id.tvColorCh6);
        mTvMsgCh1 = (TextView) findViewById(R.id.tvMsgCh1);
        mTvMsgCh2 = (TextView) findViewById(R.id.tvMsgCh2);
        mTvMsgCh3 = (TextView) findViewById(R.id.tvMsgCh3);
        mTvMsgCh4 = (TextView) findViewById(R.id.tvMsgCh4);
        mTvMsgCh5 = (TextView) findViewById(R.id.tvMsgCh5);
        mTvMsgCh6 = (TextView) findViewById(R.id.tvMsgCh6);
        mTvCh1.setTextColor(Color.BLACK);
        mTvMsgCh1.setTextColor(Color.BLACK);
        mTvCh1.setBackgroundColor(mWaveformView1.getWaveColor(0));
        mTvMsgCh1.setBackgroundColor(mWaveformView1.getWaveColor(0));
        mTvCh2.setTextColor(Color.BLACK);
        mTvMsgCh2.setTextColor(Color.BLACK);
        mTvCh2.setBackgroundColor(mWaveformView1.getWaveColor(1));
        mTvMsgCh2.setBackgroundColor(mWaveformView1.getWaveColor(1));
        mTvCh3.setTextColor(Color.BLACK);
        mTvMsgCh3.setTextColor(Color.BLACK);
        mTvCh3.setBackgroundColor(mWaveformView1.getWaveColor(2));
        mTvMsgCh3.setBackgroundColor(mWaveformView1.getWaveColor(2));
        mTvCh4.setTextColor(Color.BLACK);
        mTvMsgCh4.setTextColor(Color.BLACK);
        mTvCh4.setBackgroundColor(mWaveformView1.getWaveColor(3));
        mTvMsgCh4.setBackgroundColor(mWaveformView1.getWaveColor(3));
        mTvCh5.setTextColor(Color.BLACK);
        mTvMsgCh5.setTextColor(Color.BLACK);
        mTvCh5.setBackgroundColor(mWaveformView1.getWaveColor(4));
        mTvMsgCh5.setBackgroundColor(mWaveformView1.getWaveColor(4));
        mTvCh6.setTextColor(Color.BLACK);
        mTvMsgCh6.setTextColor(Color.BLACK);
        mTvCh6.setBackgroundColor(mWaveformView1.getWaveColor(5));
        mTvMsgCh6.setBackgroundColor(mWaveformView1.getWaveColor(5));
        mTvCh1.setOnClickListener(mOCLPageScope);
        mTvCh2.setOnClickListener(mOCLPageScope);
        mTvCh3.setOnClickListener(mOCLPageScope);
        mTvCh4.setOnClickListener(mOCLPageScope);
        mTvCh5.setOnClickListener(mOCLPageScope);
        mTvCh6.setOnClickListener(mOCLPageScope);
        mTvMsgCh1.setOnClickListener(mOCLPageScope);
        mTvMsgCh2.setOnClickListener(mOCLPageScope);
        mTvMsgCh3.setOnClickListener(mOCLPageScope);
        mTvMsgCh4.setOnClickListener(mOCLPageScope);
        mTvMsgCh5.setOnClickListener(mOCLPageScope);
        mTvMsgCh6.setOnClickListener(mOCLPageScope);
        /**/
        mTvMsgExt00 = (TextView)findViewById(R.id.tvMsgExt00);
        mTvMsgExt01 = (TextView)findViewById(R.id.tvMsgExt01);
        mTvMsgExt10 = (TextView)findViewById(R.id.tvMsgExt10);
        mTvMsgExt11 = (TextView)findViewById(R.id.tvMsgExt11);
        mTvMsgExt20 = (TextView)findViewById(R.id.tvMsgExt20);
        mTvMsgExt21 = (TextView)findViewById(R.id.tvMsgExt21);
        mTvMsgExt30 = (TextView)findViewById(R.id.tvMsgExt30);
        mTvMsgExt31 = (TextView)findViewById(R.id.tvMsgExt31);
    }
    private void switchToDebugMsgExt(){
        viewPagerTop.setCurrentItem(1,true);
        mSVDebugScope.arrowScroll(ScrollView.FOCUS_DOWN);
    }
    private void setupPageMessageViewComponents(){
        mSVDebugMsg = (ScrollView)findViewById(R.id.scrollViewDebugMessage);
        mTvTxMessage = (TextView) findViewById(R.id.tvTxMessage);
        mTvRxMessage = (TextView) findViewById(R.id.tvRxMessage);
        mTvMsg00 = (TextView) findViewById(R.id.tvMsg00);
        mTvMsg01 = (TextView) findViewById(R.id.tvMsg01);
        mTvMsg10 = (TextView) findViewById(R.id.tvMsg10);
        mTvMsg11 = (TextView) findViewById(R.id.tvMsg11);
        mTvMsg12 = (TextView) findViewById(R.id.tvMsg12);
        mTvMsg20 = (TextView) findViewById(R.id.tvMsg20);
        mTvMsg21 = (TextView) findViewById(R.id.tvMsg21);
        mTvMsg22 = (TextView) findViewById(R.id.tvMsg22);
        mTvMsg30 = (TextView) findViewById(R.id.tvMsg30);
        mTvMsg31 = (TextView) findViewById(R.id.tvMsg31);
        mTvMsg32 = (TextView) findViewById(R.id.tvMsg32);
        mTvMsg40 = (TextView) findViewById(R.id.tvMsg40);
        mTvMsg50 = (TextView) findViewById(R.id.tvMsg50);
        mTvMsg60 = (TextView) findViewById(R.id.tvMsg60);
        /*
        mTvInfoProp = (TextView)findViewById(R.id.tvProperty);
        mTvInfoDevice = (TextView)findViewById(R.id.tvDeviceInfo);
        mTvInfo0 = (TextView) findViewById(R.id.tvInfo0 );
        mTvInfo1 = (TextView) findViewById(R.id.tvInfo1 );
        mTvInfo2 = (TextView) findViewById(R.id.tvInfo2 );
        mTvInfo3 = (TextView) findViewById(R.id.tvInfo3 );
        mTvInfo4 = (TextView) findViewById(R.id.tvInfo4 );
        mTvInfo5 = (TextView) findViewById(R.id.tvInfo5 );
        mTvInfo6 = (TextView) findViewById(R.id.tvInfo6 );
        mTvInfo7 = (TextView) findViewById(R.id.tvInfo7 );
        mTvInfo8 = (TextView) findViewById(R.id.tvInfo8 );
        mTvInfo9 = (TextView) findViewById(R.id.tvInfo9 );
        mTvInfo10 = (TextView) findViewById(R.id.tvInfo10 );
        */
        mTvInfoDebug = (TextView) findViewById(R.id.tvInfoDebug );
        clearInfoDisplay();
    }
    private void setupPageMessageExtViewComponents(){
    }
    private void setupPage1ViewComponents(){
        mSwRpt = (Switch)findViewById(R.id.switchReport);
        mSwRpt6xL = (Switch)findViewById(R.id.switchRpt6axisL);
        mSwRpt6xR = (Switch)findViewById(R.id.switchRpt6axisR);
        mSwRptAttitude = (Switch)findViewById(R.id.switchRptAttitude);
        mSwRptPress = (Switch)findViewById(R.id.switchRptPress);
        mBtnRptFw = (Button) findViewById(R.id.buttonRptFw);
        mBtnRptDeviceId = (Button) findViewById(R.id.buttonRpdId);
        mBtnCmdSleep = (Button) findViewById(R.id.buttonCmdSleep);
        mSwRpt.setOnCheckedChangeListener(mOCCLPage1);
        mSwRpt6xL.setOnCheckedChangeListener(mOCCLPage1);
        mSwRpt6xR.setOnCheckedChangeListener(mOCCLPage1);
        mSwRptAttitude.setOnCheckedChangeListener(mOCCLPage1);
        mSwRptPress.setOnCheckedChangeListener(mOCCLPage1);
        mBtnRptFw.setOnClickListener(mOCLPage1);
        mBtnRptDeviceId.setOnClickListener(mOCLPage1);
        mBtnCmdSleep.setOnClickListener(mOCLPage1);
    }
    private void setupPageSensorResetViewComponents() {
        //mBtnPBSet0,mBtnPBSetM,mBtnAWSet0,mBtnAWSetM,mBtnRBSet0,mBtnRBSet1,mBtnRBSet2
        mEtRefVal = (EditText) findViewById(R.id.editTextRefVal);
        mBtnPressReset = (Button) findViewById(R.id.buttonPressReset);
        mBtnPressCalibrate = (Button) findViewById(R.id.buttonPressCal);
        mBtnGyroReset = (Button) findViewById(R.id.buttonGyroReset);
        mBtnPressReset.setOnClickListener(mOCLPageSensorReset);
        mBtnPressCalibrate.setOnClickListener(mOCLPageSensorReset);
        mBtnGyroReset.setOnClickListener(mOCLPageSensorReset);
    }
    private void setupPageResBandViewComponents(){
        mBtnResBP1L = (Button) findViewById(R.id.buttonResBP1L);
        mBtnResBP1R = (Button) findViewById(R.id.buttonResBP1R);
        mBtnResBP2L = (Button) findViewById(R.id.buttonResBP2L);
        mBtnResBP2R = (Button) findViewById(R.id.buttonResBP2R);
        mBtnResBP3L = (Button) findViewById(R.id.buttonResBP3L);
        mBtnResBP3R = (Button) findViewById(R.id.buttonResBP3R);
        mBtnResBP4L = (Button) findViewById(R.id.buttonResBP4L);
        mBtnResBP4R = (Button) findViewById(R.id.buttonResBP4R);
        mBtnResBP1L.setOnClickListener(mOCLPageResBand);
        mBtnResBP1R.setOnClickListener(mOCLPageResBand);
        mBtnResBP2L.setOnClickListener(mOCLPageResBand);
        mBtnResBP2R.setOnClickListener(mOCLPageResBand);
        mBtnResBP3L.setOnClickListener(mOCLPageResBand);
        mBtnResBP3R.setOnClickListener(mOCLPageResBand);
        mBtnResBP4L.setOnClickListener(mOCLPageResBand);
        mBtnResBP4R.setOnClickListener(mOCLPageResBand);

    }

    private void setupPageUserInfoViewComponents(){
        mBtnUserInfoR = (Button) findViewById( R.id.buttonUserInfoR);
        mBtnUserInfoW = (Button) findViewById(R.id.buttonUserInfoW);
        mBtnUserInfoR.setOnClickListener(mOCLPageUserInfo);
        mBtnUserInfoW.setOnClickListener(mOCLPageUserInfo);
        mEtUserId = new EditText[8];
        mEtUserId[0] = (EditText) findViewById(R.id.editTextUserId0);
        mEtUserId[1] = (EditText) findViewById(R.id.editTextUserId1);
        mEtUserId[2] = (EditText) findViewById(R.id.editTextUserId2);
        mEtUserId[3] = (EditText) findViewById(R.id.editTextUserId3);
        mEtUserId[4] = (EditText) findViewById(R.id.editTextUserId4);
        mEtUserId[5] = (EditText) findViewById(R.id.editTextUserId5);
        mEtUserId[6] = (EditText) findViewById(R.id.editTextUserId6);
        mEtUserId[7] = (EditText) findViewById(R.id.editTextUserId7);
        mEtHeight = (EditText)findViewById(R.id.editTextHeight);
        mEtWeight = (EditText)findViewById(R.id.editTextWeight);
        mEtInfoReserved = (EditText) findViewById(R.id.editTextInfoReserved);
        mEtInfoPushbar= (EditText)findViewById(R.id.editTextInfoPushupBar);
        mEtInfoAbsWheel= (EditText)findViewById(R.id.editTextInfoAbsWheel);
        mEtInfoPressMaxL= (EditText)findViewById(R.id.editTextInfoPressL);
        mEtInfoPressMaxR= (EditText)findViewById(R.id.editTextInfoPressR);
        for(int i=0;i<mEtUserId.length;i++){
            final int id = i;
            mEtUserId[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int length = s.toString().length();
                    if(length == 2){
                        //mEtUserId[id].setText(mEtUserId[id].getText().toString().toUpperCase());
                        if(id<mEtUserId.length-1){
                            if(mEtUserId[id].isFocused()){
                                mEtUserId[id].clearFocus();
                                mEtUserId[id+1].requestFocus();
                                mEtUserId[id+1].selectAll();
                            }
                        }
                    }
                }
            });
        }

        if(mDeviceNewName!=null)
            if(mDeviceNewName.length()>6){
                for(int i=0;i<6;i++){
                    mEtUserId[i].setText(mDeviceNewName.substring(i*2+3,i*2+5));
                }
            }
        //mEtUserInfo1.setText(bytesToString(hexStrToBytes(mEtUserInfo1.getText().toString())," "));
        /*
        mEtUserInfo1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) {
                    int length = s.toString().length();
                    if ((length == 2 || (length - 2) % 3 == 0) && length != 0) {
                        mEtUserInfo1.setText(s + " ");
                        mEtUserInfo1.setSelection(mEtUserInfo1.getText().toString().length());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });*/
    }

    private void setupPageOTAViewComponents(){
        mBtnOTAEnterR = (Button) findViewById(R.id.buttonOTAEnterR);
        mBtnOTABlankR = (Button) findViewById(R.id.buttonOTABlankR);
        mBtnOTAEraseR = (Button) findViewById(R.id.buttonOTAEraseR);
        mBtnOTAProgramR = (Button) findViewById(R.id.buttonOTAProgramR);
        mBtnOTAVerifyR = (Button) findViewById(R.id.buttonOTAVerifyR);
        mBtnOTAExitR = (Button) findViewById(R.id.buttonOTAExitR);
        mBtnOTAEnterR.setOnClickListener(mOCLPageOTA);
        mBtnOTABlankR.setOnClickListener(mOCLPageOTA);
        mBtnOTAEraseR.setOnClickListener(mOCLPageOTA);
        mBtnOTAProgramR.setOnClickListener(mOCLPageOTA);
        mBtnOTAVerifyR.setOnClickListener(mOCLPageOTA);
        mBtnOTAExitR.setOnClickListener(mOCLPageOTA);


        mBtnOTAEnterL = (Button) findViewById(R.id.buttonOTAEnterL);
        mBtnOTABlankL = (Button) findViewById(R.id.buttonOTABlankL);
        mBtnOTAEraseL = (Button) findViewById(R.id.buttonOTAEraseL);
        mBtnOTAProgramL = (Button) findViewById(R.id.buttonOTAProgramL);
        mBtnOTAVerifyL = (Button) findViewById(R.id.buttonOTAVerifyL);
        mBtnOTAExitL = (Button) findViewById(R.id.buttonOTAExitL);
        mBtnOTAEnterL.setOnClickListener(mOCLPageOTA);
        mBtnOTABlankL.setOnClickListener(mOCLPageOTA);
        mBtnOTAEraseL.setOnClickListener(mOCLPageOTA);
        mBtnOTAProgramL.setOnClickListener(mOCLPageOTA);
        mBtnOTAVerifyL.setOnClickListener(mOCLPageOTA);
        mBtnOTAExitL.setOnClickListener(mOCLPageOTA);
    }

    private void setupPagePairCodeViewComponents(){
        mBtnGenCode = (Button) findViewById(R.id.buttonGenCode);
        mBtnSendPCode = (Button) findViewById(R.id.buttonSendPCode);
        mBtnGenCode.setOnClickListener(mOCLPagePairing);
        mBtnSendPCode.setOnClickListener(mOCLPagePairing);
        mEtPairCode = (EditText) findViewById(R.id.editTextPCode);
        mEtPairCode.setEnabled(false);
    }

    private void setupPageBleRenameViewComponents(){
        mBtnBleRenameSubmit = (Button) findViewById( R.id.buttonRenameSubmit);
        mBtnBleTxPowerSubmit = (Button) findViewById(R.id.buttonTxPowerChange);
        mBtnBleRenameSubmit.setOnClickListener(mOCLPageBLERename);
        mBtnBleTxPowerSubmit.setOnClickListener(mOCLPageBLERename);
        mEtOldBleName =(EditText)findViewById(R.id.editTextOldBLEName);
        mEtNewBleName = (EditText) findViewById(R.id.editTextNewBLEName);
        mEtOldBleName.setEnabled(false);
        mEtNewBleName.setEnabled(false);
        if (mDeviceNewName != null) {
            if(mDeviceNewName.length()>0)
                mEtNewBleName.setText(mDeviceNewName);
        }else{
            mEtNewBleName.setText("");
        }
        mEtOldBleName.setText(mDeviceName);

        mRBTx0 = (RadioButton) findViewById(R.id.radioButtonTx0);
        mRBTx1 = (RadioButton) findViewById(R.id.radioButtonTx1);
        mRBTx2 = (RadioButton) findViewById(R.id.radioButtonTx2);
        mRBTx3 = (RadioButton) findViewById(R.id.radioButtonTx3);
        mRBTx0.setOnClickListener(mOCLPageBLERename);
        mRBTx1.setOnClickListener(mOCLPageBLERename);
        mRBTx2.setOnClickListener(mOCLPageBLERename);
        mRBTx3.setOnClickListener(mOCLPageBLERename);
        //mRBTx0.setOnCheckedChangeListener();

    }

    private View.OnClickListener mOCLPageScope = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == mBtnScPress.getId()){
                changeScopeDisplayPressure();
            }else if (v.getId() == mBtnScRoll.getId()){
                changeScopeDisplayAttitude();
            }else if (v.getId() == mBtnSc3d.getId()){
                if(mScopeSel==SCOPE_SEL_6AXIS_LEFT){
                    changeScopeDisplay6AxisRight();
                }
                else{
                    changeScopeDisplay6AxisLeft();
                }
            }else if(v.getId() == mTvCh1.getId()){
                mWaveformView1.setWaveEnable(0, !mWaveformView1.isWaveEnabled(0));
            }else if(v.getId() == mTvCh2.getId()){
                mWaveformView1.setWaveEnable(1, !mWaveformView1.isWaveEnabled(1));
            }else if(v.getId() == mTvCh3.getId()){
                mWaveformView1.setWaveEnable(2,!mWaveformView1.isWaveEnabled(2));
            }else if(v.getId() == mTvCh4.getId()){
                mWaveformView1.setWaveEnable(3,!mWaveformView1.isWaveEnabled(3));
            }else if(v.getId() == mTvCh5.getId()){
                mWaveformView1.setWaveEnable(4,!mWaveformView1.isWaveEnabled(4));
            }else if(v.getId() == mTvCh6.getId()){
                mWaveformView1.setWaveEnable(5,!mWaveformView1.isWaveEnabled(5));
            }else if(v.getId() == mTvMsgCh1.getId()){
                mWaveformView1.setWaveEnable(0, !mWaveformView1.isWaveEnabled(0));
            }else if(v.getId() == mTvMsgCh2.getId()){
                mWaveformView1.setWaveEnable(1, !mWaveformView1.isWaveEnabled(1));
            }else if(v.getId() == mTvMsgCh3.getId()){
                mWaveformView1.setWaveEnable(2,!mWaveformView1.isWaveEnabled(2));
            }else if(v.getId() == mTvMsgCh4.getId()){
                mWaveformView1.setWaveEnable(3,!mWaveformView1.isWaveEnabled(3));
            }else if(v.getId() == mTvMsgCh5.getId()){
                mWaveformView1.setWaveEnable(4,!mWaveformView1.isWaveEnabled(4));
            }else if(v.getId() == mTvMsgCh6.getId()){
                mWaveformView1.setWaveEnable(5,!mWaveformView1.isWaveEnabled(5));
            }
        }
    };
    private void changeScopeDisplayPressure(){
        mScopeSel=SCOPE_SEL_PRESSURE;
        mTvCh1.setText("Press\n(L)");
        mTvCh2.setText("Press\n(R)");
        mTvCh3.setText("");
        mTvCh4.setText("");
        mTvCh5.setText("");
        mTvCh6.setText("");
        mWaveformView1.clearDataRange();
        setWaveEnable(2,true);
        playWaveform2Ch(pressureCalDataL, pressureCalDataR);
    }
    private void changeScopeDisplayAttitude(){
        mScopeSel=SCOPE_SEL_ATTITUDE;
        mTvCh1.setText("Yaw(L)");
        mTvCh2.setText("Pitch(L)");
        mTvCh3.setText("Roll(L)");
        mTvCh4.setText("Yaw(R)");
        mTvCh5.setText("Pitch(R)");
        mTvCh6.setText("Roll(R)");
        mWaveformView1.clearDataRange();
        setWaveEnable(6,true);
        playWaveform6Ch(yawDataL,pitchDataL,rollDataL,yawDataR,pitchDataR,rollDataR);

    }
    private void changeScopeDisplay6AxisLeft(){
        mScopeSel=SCOPE_SEL_6AXIS_LEFT;
        mTvCh1.setText("ax(L)");
        mTvCh2.setText("ay(L)");
        mTvCh3.setText("az(L)");
        mTvCh4.setText("gx(L)");
        mTvCh5.setText("gy(L)");
        mTvCh6.setText("gz(L)");
        mWaveformView1.clearDataRange();
        setWaveEnable(6,true);
        playWaveform6Ch(gDataArrayL[0], gDataArrayL[1], gDataArrayL[2], gDataArrayL[3], gDataArrayL[4], gDataArrayL[5]);
    }
    private void changeScopeDisplay6AxisRight(){
        mScopeSel=SCOPE_SEL_6AXIS_RIGHT;
        mTvCh1.setText("ax(R)");
        mTvCh2.setText("ay(R)");
        mTvCh3.setText("az(R)");
        mTvCh4.setText("gx(R)");
        mTvCh5.setText("gy(R)");
        mTvCh6.setText("gz(R)");
        mWaveformView1.clearDataRange();
        setWaveEnable(6,true);
        playWaveform6Ch(gDataArrayR[0], gDataArrayR[1], gDataArrayR[2], gDataArrayR[3], gDataArrayR[4], gDataArrayR[5]);

    }

    private void setWaveEnable(int chnCnt,boolean enable){
        for(int i=0;i<chnCnt;i++)
            mWaveformView1.setWaveEnable(i,enable);
    }
    private CompoundButton.OnCheckedChangeListener mOCCLPage1 = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //boolean checked = isChecked;
            //stopContinueReportCmd();


            ReportCmdTimer.removeCallbacks(ReportCmdRunnable);

            if(buttonView.getId()== mSwRpt.getId()){
                if(isChecked){
                    mSwRpt6xL.setChecked(false);
                    mSwRpt6xR.setChecked(false);
                    mSwRptAttitude.setChecked(false);
                    mSwRptPress.setChecked(false);
                    reportType = REPORT_TYPE_NORMALMESSAGE;
                    startContinueReportCmd();
                }
            }else if(buttonView.getId() == mSwRpt6xL.getId()){
                if(isChecked){
                    mSwRpt.setChecked(false);
                    mSwRpt6xR.setChecked(false);
                    mSwRptAttitude.setChecked(false);
                    mSwRptPress.setChecked(false);
                    reportType = REPORT_TYPE_6AXILSENSOR_LEFT;
                    startContinueReportCmd();
                    changeScopeDisplay6AxisLeft();
                }
            }else if(buttonView.getId() == mSwRpt6xR.getId()){
                if(isChecked){
                    mSwRpt.setChecked(false);
                    mSwRpt6xL.setChecked(false);
                    mSwRptAttitude.setChecked(false);
                    mSwRptPress.setChecked(false);
                    reportType = REPORT_TYPE_6AXILSENSOR_RIGHT;
                    startContinueReportCmd();
                    changeScopeDisplay6AxisRight();
                }
            }else if(buttonView.getId() == mSwRptAttitude.getId()){
                if(isChecked){
                    mSwRpt.setChecked(false);
                    mSwRpt6xL.setChecked(false);
                    mSwRpt6xR.setChecked(false);
                    mSwRptPress.setChecked(false);
                    reportType = REPORT_TYPE_ATTITUDE;
                    startContinueReportCmd();
                    changeScopeDisplayAttitude();
                }
            }else if(buttonView.getId() == mSwRptPress.getId()){
                if(isChecked){
                    mSwRpt.setChecked(false);
                    mSwRpt6xL.setChecked(false);
                    mSwRpt6xR.setChecked(false);
                    mSwRptAttitude.setChecked(false);
                    reportType = REPORT_TYPE_PRESSURE;
                    startContinueReportCmd();
                    changeScopeDisplayPressure();
                    switchToDebugMsgExt();
                }
            }


        }
    };
    private View.OnClickListener mOCLPage1 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopContinueReportCmd();
            if(v.getId()==mBtnDevice.getId()){
                setupConnectDialog();
            }else if(v.getId()==mBtnCmdSleep.getId()){
                sendBleData(appCmd.txCmd5_1);
            }else if(v.getId()==mTvDeviceName.getId()){
                createRenameBleDeviceDialog();
            }else if(v.getId()==mBtnRptFw.getId()){
                sendBleData(appCmd.txCmd3);
            }else if(v.getId()==mBtnRptDeviceId.getId()){
                sendBleData(appCmd.txCmd2);
            }
            /*else if(v.getId()==mBtnCmd1.getId()){
                if(buttonReportCnt<=0){
                    sendBleData(appCmd.txCmd1_0);
                }
                switch(buttonReportCnt){
                    case 0:sendBleData(appCmd.txCmd1_0);
                        buttonReportCnt++;
                        break;
                    case 1:
                        reportType = REPORT_TYPE_NORMALMESSAGE;
                        startContinueReportCmd();
                        buttonReportCnt++;
                        break;
                    default:
                        stopContinueReportCmd();
                        break;
                }
            }else if(v.getId()==mBtnCmd2.getId()){
                sendBleData(appCmd.txCmd2);
            }else if(v.getId()==mBtnCmd3.getId()){
                sendBleData(appCmd.txCmd3);
            }else if(v.getId()==mBtnCmd4.getId()){
                reportType = REPORT_TYPE_6AXILSENSOR_RIGHT;
                if(buttonReportCnt>1)
                    stopContinueReportCmd();
                else {
                    changeScopeDisplay6AxisRight();
                    startContinueReportCmd();
                }
            }else if(v.getId()==mBtnCmd5.getId()) {
                reportType = REPORT_TYPE_6AXILSENSOR_LEFT;
                if(buttonReportCnt>1)
                    stopContinueReportCmd();
                else{
                    changeScopeDisplay6AxisLeft();
                    startContinueReportCmd();
                }
            }else if(v.getId()==mBtnCmd6.getId()) {
                reportType = REPORT_TYPE_ATTITUDE;
                if(buttonReportCnt>1)
                    stopContinueReportCmd();
                else{
                    changeScopeDisplayAttitude();
                    startContinueReportCmd();
                }
            }*/
        }
    };
    private View.OnClickListener mOCLPageSensorReset = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopContinueReportCmd();
            //mBtnPBSet0,mBtnPBSetM,mBtnAWSet0,mBtnAWSetM,mBtnRBSet0,mBtnRBSet1,mBtnRBSet2
            //
            if(v.getId() == mBtnGyroReset.getId()){
                setZeroType = SENSOR_SET_ZERO_GSENSOR;
                setZeroType0 = setZeroType;
                createProgressDialog("Please keep the handles steady");
                startCheckIfGSensorSteady();
                startAvgSampling();
            }else if(v.getId() == mBtnPressReset.getId()){
                setZeroType = SENSOR_PRESSURE_RESET;
                setZeroType0 = setZeroType;
                createProgressDialog("Please keep the handles steady and release your hand from handles");
                startAvgSampling();
            }else if(v.getId() == mBtnPressCalibrate.getId()){
                setZeroType = SENSOR_PRESSURE_CALIBRATION;
                setZeroType0 = setZeroType;
                createProgressDialog("Please keep the handles steady and release your hand from handles");
                startAvgSampling();
            }
        }
    };
    private View.OnClickListener mOCLPageResBand = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopContinueReportCmd();

        }
    };
    private View.OnClickListener mOCLPageUserInfo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopContinueReportCmd();
            if(v.getId()==mBtnUserInfoR.getId()){
                sendBleData(appCmd.txCmd8_r);
            }else if(v.getId()==mBtnUserInfoW.getId()){
                //mEtUserInfo1.setText(bytesToString(hexStrToBytes(mEtUserInfo1.getText().toString())," "));
                byte[] info = new byte[8];// = hexStrToBytes(mEtUserInfo1.getText().toString());
                for(int i =0;i<mEtUserId.length;i++){
                    String str = mEtUserId[i].getText().toString();
                    if(str.equals(""))str="00";
                    byte[] temp = hexStrToBytes(str);
                    info[i] = temp[0];
                    appCmd.txCmd8_w[i+3]=info[i];
                }
                String strHeight = mEtHeight.getText().toString();
                strHeight.replace(" ","");
                if(strHeight.equals(""))strHeight="0";
                int height = Integer.parseInt(strHeight);
                byte[] heightData = intToBytes(height);
                appCmd.txCmd8_w[11]=heightData[0];
                appCmd.txCmd8_w[12]=heightData[1];


                String strWeight = mEtWeight.getText().toString();
                strWeight.replace(" ","");
                if(strWeight.equals(""))strWeight="0";
                int weight = Integer.parseInt(strWeight);
                byte[] weightData = intToBytes(weight);
                appCmd.txCmd8_w[13]=weightData[0];

                String strValReserved = mEtInfoReserved.getText().toString();
                if(strValReserved.equals(""))strValReserved="0";
                int valReserved = Integer.parseInt(strValReserved);
                appCmd.txCmd8_w[14] = (byte) valReserved;


                //sendBleData(appCmd.txCmd8_w);
                dataBytes = new byte[appCmd.txCmd8_w.length];
                System.arraycopy(appCmd.txCmd8_w,0,dataBytes,0,appCmd.txCmd8_w.length);
                afterConfirmWriteStep = 0;
                mConfirmWriteDialog.show();

            }
        }
    };
    private View.OnClickListener mOCLPageOTA = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopContinueReportCmd();
            if(v.getId()==mBtnOTABlankR.getId()){
                sendBleData(appCmd.txCmd6R_Blankcheck);
            }else if(v.getId()==mBtnOTAEraseR.getId()){
                dataBytes = new byte[appCmd.txCmd6R_Erase.length];
                System.arraycopy(appCmd.txCmd6R_Erase,0,dataBytes,0,dataBytes.length);
                afterConfirmWriteStep = 0;
                mConfirmWriteDialog.show();
            }else if(v.getId()==mBtnOTAProgramR.getId()){
                isOTARight=true;
                showDialog(OPEN_FILE_DIALOG_ID);
            }else if(v.getId()==mBtnOTAVerifyR.getId()){
                sendBleData(appCmd.txCmd6R_Checksum);
            }else if(v.getId()==mBtnOTAExitR.getId()){
                sendBleData(appCmd.OTAExit);
            }else if(v.getId()==mBtnOTAEnterR.getId()){
                sendBleData(appCmd.OTAInto);
            }else if(v.getId()==mBtnOTABlankL.getId()){
                sendBleData(appCmd.txCmd6L_Blankcheck);
            }else if(v.getId()==mBtnOTAEraseL.getId()){
                dataBytes = new byte[appCmd.txCmd6L_Erase.length];
                System.arraycopy(appCmd.txCmd6L_Erase,0,dataBytes,0,dataBytes.length);
                afterConfirmWriteStep = 0;
                mConfirmWriteDialog.show();
            }else if(v.getId()==mBtnOTAProgramL.getId()){
                isOTARight=false;
                showDialog(OPEN_FILE_DIALOG_ID);
            }else if(v.getId()==mBtnOTAVerifyL.getId()){
                sendBleData(appCmd.txCmd6L_Checksum);
            }else if(v.getId()==mBtnOTAExitL.getId()){
                sendBleData(appCmd.OTAExit);
            }else if(v.getId()==mBtnOTAEnterL.getId()){
                sendBleData(appCmd.OTAInto);
            }
        }
    };
    private View.OnClickListener mOCLPagePairing = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopContinueReportCmd();
            if(v.getId()==mBtnGenCode.getId()){
                byte[] bytes1 = doubleToBytes(Math.random());
                byte[] bytes2 = doubleToBytes(Math.random());
                byte[] bytes3 = doubleToBytes(Math.random());
                byte[] bytes4 = doubleToBytes(Math.random());
                pairCode = new byte[8];
                pairCodeSub = new byte[8];
                System.arraycopy(bytes1,0,pairCode, 0, 4);
                System.arraycopy(bytes2,0,pairCode,4,4);
                System.arraycopy(bytes3,0,pairCodeSub, 0, 4);
                System.arraycopy(bytes4,0,pairCodeSub,4,4);
                mEtPairCode.setText(bytesToString(pairCode," "));
                mEtPairCode.startAnimation(mBlinkAnimation);
            }else if(v.getId() == mBtnSendPCode.getId()){
                if(pairCode==null || pairCode.length<1)return;
                System.arraycopy(pairCode,0,appCmd.txCmd9,3,pairCode.length);
                System.arraycopy(pairCodeSub,0,appCmd.txCmd9,11,pairCodeSub.length);
                dataBytes = new byte[appCmd.txCmd9.length];
                System.arraycopy(appCmd.txCmd9,0,dataBytes,0,dataBytes.length);
                afterConfirmWriteStep = 0;
                mConfirmWriteDialog.show();
            }
        }
    };

    private View.OnClickListener mOCLPageBLERename = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == mBtnBleRenameSubmit.getId()) {
                if (mDeviceNewName != null) {
                    if(mDeviceNewName.length()>0)
                        mEtNewBleName.setText(mDeviceNewName);
                }else{
                    return;
                }
                renameBleDevice();
                //updateDebugInfo(bytesToString(appCmd.txCmdD, " "));

            } else if (v.getId() == mBtnBleTxPowerSubmit.getId()) {
                /*
                appCmd.txCmdE[3] = (byte)deviceTxPowerSel;
                dataBytes = new byte[appCmd.txCmdE.length];
                System.arraycopy(appCmd.txCmdE,0,dataBytes,0,dataBytes.length);
                afterConfirmWriteStep = CONFIRM_BLE_TXPOWER;
                mConfirmWriteDialog.show();
                */

            } else if (v.getId() == mRBTx0.getId()) {
                deviceTxPowerSel = 0;
                updateRadioButtonTxPower();
            } else if (v.getId() == mRBTx1.getId()) {
                deviceTxPowerSel = 1;
                updateRadioButtonTxPower();
            } else if (v.getId() == mRBTx2.getId()) {
                deviceTxPowerSel = 2;
                updateRadioButtonTxPower();
            } else if (v.getId() == mRBTx3.getId()) {
                deviceTxPowerSel = 3;
                updateRadioButtonTxPower();
            }
        }
    };

    private void updateRadioButtonTxPower(){
        mRBTx0.setChecked(false);
        mRBTx1.setChecked(false);
        mRBTx2.setChecked(false);
        mRBTx3.setChecked(false);
        switch (deviceTxPowerSel){
            case 0:
                mRBTx0.setChecked(true);
                break;
            case 1:
                mRBTx1.setChecked(true);
                break;
            case 2:
                mRBTx2.setChecked(true);
                break;
            case 3:
                mRBTx3.setChecked(true);
                break;
        }
    }


    private void setupViewComponents(){
        mAnimationTx = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim_blink);
        mAnimationRx = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim_blink);
        mBlinkAnimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim_blink);

        LayoutInflater inflater = getLayoutInflater();
        pageBottomViews = new ArrayList<View>();
        pageBottomViews.add(inflater.inflate(R.layout.layout_debug_blerename,null));
        pageBottomViews.add(inflater.inflate(R.layout.layout_debug_report, null));
        pageBottomViews.add(inflater.inflate(R.layout.layout_debug_sensor, null));
        //pageBottomViews.add(inflater.inflate(R.layout.layout_debug_resband_init, null));
        pageBottomViews.add(inflater.inflate(R.layout.layout_debug_userinfo, null));
        pageBottomViews.add(inflater.inflate(R.layout.layout_debug_pairing, null));
        pageBottomViews.add(inflater.inflate(R.layout.layout_debug_ota, null));


        viewPagerBottom = (ViewPager) findViewById(R.id.viewPagerBottom);
        viewPagerBottom.setAdapter(new PagerAdapter() {
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                //return super.instantiateItem(container, position);
                container.addView(pageBottomViews.get(position));
                setupBottomPagesViewComponents(position);
                return pageBottomViews.get(position);
            }

            //???positionλ??????
            @Override
            public void destroyItem(View v, int position, Object arg2) {
                // TODO Auto-generated method stub
                ((ViewPager) v).removeView(pageBottomViews.get(position));

            }

            @Override
            public void finishUpdate(View arg0) {
                // TODO Auto-generated method stub

            }

            //???????????????
            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                return pageBottomViews.size();
            }

            //?????positionλ??????
            @Override
            public Object instantiateItem(View v, int position) {
                // TODO Auto-generated method stub
                ((ViewPager) v).addView(pageBottomViews.get(position));
                setupBottomPagesViewComponents(position);
                Log.d(TAG, "pageView Changed= " + position);
                return pageBottomViews.get(position);
            }

            // ?ж???????????????
            @Override
            public boolean isViewFromObject(View v, Object arg1) {
                // TODO Auto-generated method stub
                return v == arg1;
            }


            @Override
            public void startUpdate(View arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public int getItemPosition(Object object) {
                // TODO Auto-generated method stub
                return super.getItemPosition(object);
            }

            @Override
            public void restoreState(Parcelable arg0, ClassLoader arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public Parcelable saveState() {
                // TODO Auto-generated method stub
                return null;
            }
        });
        viewPagerBottom.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //Log.v(TAG, "onPageSelected(" + position + ")");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPagerBottom.setCurrentItem(1,true);



        pageTopViews = new ArrayList<View>();
        pageTopViews.add(inflater.inflate(R.layout.layout_debug_message, null));
        //pageTopViews.add(inflater.inflate(R.layout.layout_debug_message_ext,null));
        pageTopViews.add(inflater.inflate(R.layout.layout_debug_scope, null));
        viewPagerTop = (ViewPager) findViewById(R.id.viewPagerTop);
        viewPagerTop.setAdapter(new PagerAdapter() {
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(pageTopViews.get(position));
                setupTopPagesViewComponents(position);
                return pageTopViews.get(position);
            }

            @Override
            public int getCount() {
                return pageTopViews.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }


            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                //super.destroyItem(container, position, object);
                container.removeView(pageTopViews.get(position));
            }

            @Override
            public void finishUpdate(View container) {
                super.finishUpdate(container);
            }

            @Override
            public Object instantiateItem(View container, int position) {
                //return super.instantiateItem(container, position);
                ((ViewPager) container).addView(pageTopViews.get(position));
                setupTopPagesViewComponents(position);
                return pageTopViews.get(position);

            }

            @Override
            public void startUpdate(View container) {
                super.startUpdate(container);
            }

            @Override
            public int getItemPosition(Object object) {
                return super.getItemPosition(object);
            }

            @Override
            public Parcelable saveState() {
                return super.saveState();
            }
        });
        viewPagerTop.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        mTvDeviceName = (TextView)findViewById(R.id.tvDeviceName);

        mTvVersion = (TextView) findViewById(R.id.tvVersion);


        //mTvMessage.setTextSize(wm.getDefaultDisplay().getWidth()/20);
        //mTvMessage.setText("--.-g");
        String verName = "";
        try {
            verName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mTvVersion.setText("version: " + verName);
        mBtnDevice = (Button) findViewById(R.id.device_button);
        mTvDeviceName.setOnClickListener(mOCLPage1);
        mBtnDevice.setOnClickListener(mOCLPage1);

        //setupPageMessageViewComponents();


        AlertDialog.Builder confirmWriteBuilder = new AlertDialog.Builder(MainActivity.this);
        //mConfirmWriteDialog = confirmWriteBuilder.create();
        //mConfirmDialog = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.AlertDialogCustom));
        confirmWriteBuilder.setTitle("Warning");
        confirmWriteBuilder.setMessage("Confirm to Write?");
        confirmWriteBuilder.setIcon(android.R.drawable.stat_sys_warning);
        confirmWriteBuilder.setCancelable(true);

        confirmWriteBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendBleData(dataBytes);
                switch(afterConfirmWriteStep){
                    case CONFIRM_BLE_RENAME:
                        afterConfirmWriteStep = 0;
                        disconnectDevice(300);
                        createNoticeDialog("Device Restarted for Renaming");
                        break;
                    case CONFIRM_BLE_TXPOWER:
                        afterConfirmWriteStep = 0;
                        disconnectDevice(300);
                        createNoticeDialog("Device Restarted for Setting Tx power");
                        break;
                    case CONFIRM_SENSOR_RESET:
                        afterConfirmWriteStep = 0;
                        //startSpinnerProgressDialog();
                        Message msg = new Message();
                        msg.what = MSG_SHOW_WAITING_FEEDBACK_DIALOG;
                        mMsgHandler.sendMessage(msg);
                        break;
                    default:
                        afterConfirmWriteStep = 0;
                        break;
                }
            }
        });
        confirmWriteBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        mConfirmWriteDialog = confirmWriteBuilder.create();
        Window dialogWindow = mConfirmWriteDialog.getWindow();
        WindowManager.LayoutParams dialogLp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialogWindow.setAttributes(dialogLp);


        //WindowManager wm = this.getWindowManager();


        updateConnectionState();
    }
    private void renameBleDevice(){

        //String newName = mEtNewBleName.getText().toString();


        byte[] nameData = mDeviceNewName.getBytes();
        appCmd.txCmdD = new byte[nameData.length + 3];
        appCmd.txCmdD[0] = (byte) 0xAA;
        appCmd.txCmdD[1] = (byte) (nameData.length + 2);
        appCmd.txCmdD[2] = (byte) 0x0D;
        System.arraycopy(nameData, 0, appCmd.txCmdD, 3, nameData.length);

        dataBytes = new byte[appCmd.txCmdD.length];
        System.arraycopy(appCmd.txCmdD, 0, dataBytes, 0, dataBytes.length);
        afterConfirmWriteStep = CONFIRM_BLE_RENAME;
        //mConfirmWriteDialog.show();
        Message msg = new Message();
        msg.what = MSG_SHOW_CONFIRM_WRITE_DIALOG;
        mMsgHandler.sendMessage(msg);
    }
    private void createRenameBleDeviceDialog(){
        if (mDeviceNewName == null || mConnectState != STATE_CONNECTED) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Bluetooth Device");
        builder.setMessage("Confirm rename device\nfrom:\t"+mDeviceName+"\nto:\t"+ mDeviceNewName + "\t?");
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                renameBleDevice();
            }
        });
        builder.create().show();
    }
    private void createProgressDialog(String title){
        Message msg = new Message();
        msg.what = MSG_SHOW_WAITING_FEEDBACK_DIALOG;
        Bundle data = new Bundle();
        data.putString(EXTRA_DIALOG_TITLE,title);
        msg.setData(data);
        mMsgHandler.sendMessage(msg);
    }
    private void createNoticeDialog(String title){
        Message msg = new Message();
        msg.what = MSG_SHOW_NOTICE_DIALOG;
        Bundle data = new Bundle();
        data.putString(EXTRA_DIALOG_TITLE,title);
        msg.setData(data);
        mMsgHandler.sendMessage(msg);
    }


    private final void updateDebugInfo(String string){
        mTvInfoDebug.setText(string);
        mTvInfoDebug.startAnimation(mBlinkAnimation);
        mSVDebugMsg.fullScroll(ScrollView.FOCUS_DOWN);
    }
    private void clearInfoDisplay(){
        //mTvMsg00.setText("-");
        //mTvMsg01.setText("-");
        mTvMsg10.setText("-");
        mTvMsg11.setText("-");
        mTvMsg12.setText("-");
        mTvMsg20.setText("-");
        mTvMsg21.setText("-");
        mTvMsg22.setText("-");
        mTvMsg30.setText("-");
        mTvMsg31.setText("-");
        mTvMsg32.setText("-");
    }
    ////////////////////////////////////////////////////////////////////
    //user method
    ///////////////////////////////////////////////////////////////////
    public static long bytesToLong(byte[] bytes,int index){
        long ldata=0;
        for(int i=0; i<8;i++){
            ldata <<=8;
            ldata = ldata + (long)(bytes[index+7-i]&0xff);
        }
        return  ldata;
    }
    public static byte[] longToBytes(long ldata){
        byte[] bytes = new byte[8];
        long temp = ldata;
        for(int i=0; i<bytes.length ;i++){
            bytes[i] = new Long(temp).byteValue();
            temp=temp>>8;
        }
        return bytes;

    }
    public static byte[] floatToBytes(float fdata){
        byte[] bytes = new byte[4];
        int temp = Float.floatToIntBits(fdata);
        for(int i=0;i<bytes.length;i++){
            bytes[i] = new Integer(temp).byteValue();
            temp = temp>>8;
        }
        return bytes;
    }
    public static byte[] doubleToBytes(double ddata){
        byte[] bytes = new byte[8];
        long temp = Double.doubleToLongBits(ddata);
        for(int i=0; i<bytes.length ;i++){
            bytes[i] = new Long(temp).byteValue();
            temp=temp>>8;
        }
        return bytes;
    }
    public static byte[] hexStrToBytes(String hexStr){
       // hexStr.
        String tempStr = hexStr.toLowerCase();
        tempStr = tempStr.replace(" ", "");
        if(tempStr.length()%2 != 0)tempStr=tempStr.toString()+"0";
        int[] chars = new int[tempStr.length()];
        for(int i=0;i<chars.length;i++){
            char tempChar = tempStr.charAt(i);
            if(tempChar>='0' && tempChar<='9')
                chars[i] = (tempChar-'0') ;
            else if(tempChar>='a' && tempChar<='f')
                chars[i] =(tempChar-'a'+10) ;
        }

        byte[] bytes = new byte[tempStr.length()/2];
        for(int i=0;i<bytes.length;i++){
            bytes[i]=(byte)(chars[2*i]<<4);
            bytes[i]+=(byte)(chars[2*i+1]);
        }
        return bytes;
    }
    public static byte[] intToBytes(int n){
        byte[] b = new byte[4];
        for(int i = 0;i < 4 ;i++){
            b[3-i] = (byte)(n >> (24 - i * 8));
        }
        return b;
    }
    public static byte[] intToBytesMSB(int n){
        byte[] b = new byte[4];
        for(int i = 0;i < 4;i++){
            b[i] = (byte)(n >> (24 - i * 8));
        }
        return b;
    }
    public String byteToString(byte data){
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%02X", data));
        return stringBuilder.toString();
    }
    public String bytesToString(byte[] bytes, String divider){
        if(bytes.length < 1 || bytes==null)return null;
        String strDivider = "";
        if(divider != null) strDivider = divider;
        final StringBuilder stringBuilder = new StringBuilder(bytes.length);
        for (int i=0;i<bytes.length;i++)
            stringBuilder.append(String.format("%02X"+strDivider, bytes[i]));
        return stringBuilder.toString();
    }
    private byte[] readFile(String fileName){
        byte[] buf = null;
        try {
            FileInputStream inStream = new FileInputStream(fileName);
            int length = inStream.available();
            buf = new byte[length];
            inStream.read(buf);
            inStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return buf;
    }
    /*
    private byte[] readFile(int resId){
        byte[] buf = new byte[1024*1024];
        int len = 0;
        FileInputStream inStream = null;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        Resources res = getResources();
        AssetFileDescriptor fd = res.openRawResourceFd(resId);
        try {
            inStream = fd.createInputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            while( (len = inStream.read(buf)) != -1){
                outStream.write(buf, 0, len);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] data = outStream.toByteArray();
        //Log.i(TAG, new String(data));
        try {
            inStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            outStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return data;

    }*/

    private byte[] otaBuf;
    private int otaBinChecksum;
    private boolean otaPacketEndOdd;
    private int otaPacketLength;
    private boolean otaFBRet = false;
    private boolean otaThreadEnable = false;
    private ProgressDialog mProgressDialog;
    private void processOtaData(String filepath){
        mProgressDialog = new ProgressDialog( this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle("Upoading data...");
        //mProgressDialog.setMessage(" second(s) left.");
        mProgressDialog.setCancelable(false);
        Window pgWindow = mProgressDialog.getWindow();
        WindowManager.LayoutParams pgLp = pgWindow.getAttributes();
        pgWindow.setGravity(Gravity.BOTTOM);
        pgWindow.setAttributes(pgLp);

        mProgressDialog.setProgress(0);
        mProgressDialog.setMax(100);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                otaThreadEnable = false;
            }
        });
        mProgressDialog.show();

        Log.v(TAG, "path= " + filepath);
        sendBleData(new byte[]{0x1,0x2,0x3});
        otaBuf = readFile(filepath);
        otaBinChecksum=0;
        otaPacketLength = otaBuf.length/PACKET_AVAL_DATA_LENGTH;
        otaPacketEndOdd = otaPacketLength%PACKET_AVAL_DATA_LENGTH !=0;
        //if(otaBuf.length%PACKET_AVAL_DATA_LENGTH != 0)otaPacketLength++;
        for(int i=0;i<otaBuf.length;i++){
            otaBinChecksum += (int)(otaBuf[i] & 0xff);
        }
        mTvMsg40.setText(filepath);
        mTvMsg50.setText(Integer.toString(otaBuf.length) + " Byte(s)");
        //mTvInfo10.setText(Integer.toString(otaPacketLength));
        mTvInfoDebug.setText("Checksum:" + bytesToString(intToBytesMSB(otaBinChecksum), ""));
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buf = new byte[19];
                buf[0] = isOTARight?(byte)0xa5:(byte)0xa9;
                int progress = 0;
                otaThreadEnable = true;
                for(int i=0;i<otaPacketLength;i++){
                    buf[1] = (byte)i;
                    buf[2] = (byte) (i >> 8);
                    System.arraycopy(otaBuf, i * PACKET_AVAL_DATA_LENGTH, buf, 3, PACKET_AVAL_DATA_LENGTH);
                    //sendBleData(buf);

                    otaFBRet = false;

                    Message msg = new Message();
                    progress = 100*i/otaPacketLength;
                    msg.what = MSG_PROGRESS_CHANGE;
                    msg.arg1 = progress;
                    msg.arg2 = MSG_SEND_BLE_DATA;
                    Bundle data = new Bundle();
                    data.putByteArray(MSG_BLE_BUFFER, buf);
                    msg.setData(data);
                    mMsgHandler.sendMessage(msg);
                    Log.d(TAG,bytesToString(buf," "));
                    /*
                    try {
                        Thread.sleep(UPLOAD_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    */
                    //debug
                    //otaFBRet = true;
                    int timeoutCnt = 0;
                    do{
                        try {
                            Thread.sleep(UPLOAD_INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        timeoutCnt++;
                        if(timeoutCnt>UPLOAD_TIMEOUT/UPLOAD_INTERVAL){
                            Message msgErr = new Message();
                            msgErr.what = MSG_SEND_BLE_ERR;
                            mMsgHandler.sendMessage(msgErr);
                            return;
                        }
                        if(!otaThreadEnable){
                            Message msgErr = new Message();
                            msgErr.what = MSG_SEND_BLE_CANCEL;
                            mMsgHandler.sendMessage(msgErr);
                            return;
                        }
                    }while(!otaFBRet);

                }
                if(otaPacketEndOdd) {
                    int oddLength = otaBuf.length%PACKET_AVAL_DATA_LENGTH;
                    //Log.d(TAG, "One more packet and " + oddLength + " bytes");
                    buf[1] = (byte)(otaPacketLength);
                    buf[2] = (byte)((otaPacketLength)>>8);
                    System.arraycopy(otaBuf, otaPacketLength*PACKET_AVAL_DATA_LENGTH, buf, 3, oddLength);
                    for(int i=oddLength+3;i<buf.length;i++)buf[i]=(byte)0xff;
                    Log.d(TAG, bytesToString(buf, " "));
                }
                if(otaPacketLength==0){
                    Log.d(TAG, "OTA bin is too short");
                    buf[1] = 0;//(byte)(otaPacketLength);
                    buf[2] = 0;//(byte)((otaPacketLength)>>8);
                    System.arraycopy(otaBuf, 0,buf,3,otaBuf.length);
                    for(int i=otaBuf.length+3;i<buf.length;i++)buf[i]=(byte)0xff;
                    Log.d(TAG, bytesToString(buf, " "));
                }
                Message msg = new Message();
                progress = 100;
                msg.what = MSG_PROGRESS_CHANGE;
                msg.arg1 = progress;
                msg.arg2 = MSG_SEND_BLE_DATA;
                Bundle data = new Bundle();
                data.putByteArray(MSG_BLE_BUFFER, buf);
                msg.setData(data);
                mMsgHandler.sendMessage(msg);
            }
        }).start();
    }
    private boolean isCheckGSensorSteady = false;
    private int checkSensorLCnt = 0;
    private int checkSensorRCnt = 0;

    private void startCheckIfGSensorSteady(){
        isCheckGSensorSteady = true;
        checkSensorLCnt = 0;
        checkSensorRCnt = 0;
    }
    private void stopCheckIfGSensorSteady(){
        isCheckGSensorSteady = false;
        checkSensorLCnt = 0;
        checkSensorRCnt = 0;
    }
    private boolean checkIfGSensorLSteady(){
        //check stable?
        int window = WAVEFORM_DATA_LENGTH  / 2;
        if(checkSensorLCnt > window){
            for(int i=3;i<6;i++){
                int maxL = gDataArrayL[i][gDataArrayL[i].length - 1];
                int minL = gDataArrayL[i][gDataArrayL[i].length - 1];
                for(int j=0;j<window/2;j++){
                    int valL = gDataArrayL[i][gDataArrayL[i].length - 1 - j];
                    if(maxL < valL)maxL = valL;
                    if(minL > valL)minL = valL;
                }
                if(maxL-minL > AVG_DIFF_TH){
                    Log.v(TAG,"diffL"+i+"="+(maxL-minL));
                    return false;
                }
            }
        }
        checkSensorLCnt ++;
        return true;
    }
    private boolean checkIfGSensorRSteady(){
        //check stable?
        int window = WAVEFORM_DATA_LENGTH  / 2;
        if(checkSensorRCnt > window){
            for(int i=3;i<6;i++){
                int maxR = gDataArrayR[i][gDataArrayR[i].length - 1];
                int minR = gDataArrayR[i][gDataArrayR[i].length - 1];
                for(int j=0;j<window/2;j++){
                    int valR = gDataArrayR[i][gDataArrayR[i].length - 1 - j];
                    if(maxR < valR)maxR = valR;
                    if(minR > valR)minR = valR;
                }
                if(maxR-minR >AVG_DIFF_TH){
                    Log.v(TAG,"diffR"+i+"="+(maxR-minR));
                    return false;
                }
            }
        }
        checkSensorRCnt++;
        return true;
    }

    private boolean checkIfPSensorRSteady(){

        int window = WAVEFORM_DATA_LENGTH;
        if(samplePointer > window){
            int max = pressureRawDataR[pressureRawDataR.length-1];
            int min = max;
            for(int i=0;i<window;i++){
                int val = pressureRawDataR[pressureRawDataR.length-1-i];
                if(max < val)max = val;
                if(min < val)min = val;
            }
            if(max-min > AVG_DIFF_TH){
                Log.v(TAG,"diffP"+"="+(max-min));
                return false;
            }
        }
        samplePointer++;
        return true;
    }
    private boolean processData(byte[] data){
        if(data.length < 4 || data==null ){
            clearInfoDisplay();
            updateDebugInfo("Data Err");
            return false;
        }
        int dataCount = data[1];
        byte dataSum = 0;
        for(int i=0; i< dataCount+1; i++){
            dataSum+=data[i];
        }
        if(dataSum != data[dataCount+1]){
            updateDebugInfo("Data Err");
            clearInfoDisplay();
            return false;
        }
        //updateDebugInfo("");
        int dataType = data[2] & 0xff;
        if(dataType == 1){
            clearInfoDisplay();
            int dataTouch = (int)data[3]& 0x03;
            int handlePosition = (int)data[3] & 0x08;
            int rfDistance = (data[3]>>4) & 0x0f;
            int deviceInfoR = (int)data[4]& 0xf0;
            int deviceInfoL = (int)data[4]& 0x0f;
            int property = (int)data[5]& 0xff;
            int stepWheel =data[6];
            int count = (data[7] & 0xFF) | ((data[8])<<8);
            int rollL = data[9];
            int rollR = data[10];
            valPressureL =((data[11] & 0xFF) | ((data[12])<<8));
            valPressureR =((data[13] & 0xFF) | ((data[14])<<8));
            float pressL = (float)valPressureL * 10 / 1000;
            float pressR = (float)valPressureR * 10 / 1000;
            //pressL = 99.998f;
            //pressR = -999.998f;
            DecimalFormat format = new DecimalFormat("0.000");
            String strPressL = "PressL(Kg): " + format.format(pressL);
            String strPressR = "PressR(Kg): " + format.format(pressR);
            //valPressureRawL =((data[15] & 0xFF) | ((data[16])<<8));
            //valPressureRawR =((data[16] & 0xFF) | ((data[18])<<8));

            appendDataArray(pressureCalDataL, valPressureL);
            appendDataArray(pressureCalDataR, valPressureR);

            int debugData0 = (data[15] & 0xFF) | ((data[16])<<8);
            int debugData1 = (data[17] & 0xFF) | ((data[18])<<8);

            String debugDataStr0 =  new StringBuilder().append(String.format("%02X", data[16])).append(String.format("%02X", data[15])).toString();
            String debugDataStr1 =  new StringBuilder().append(String.format("%02X", data[18])).append(String.format("%02X", data[17])).toString();

            switch(mScopeSel){
                case SCOPE_SEL_PRESSURE:
                    mTvMsgCh1.setText(Integer.toString(pressureCalDataL[pressureCalDataL.length-1]));
                    mTvMsgCh2.setText(Integer.toString(pressureCalDataR[pressureCalDataR.length-1]));
                    playWaveform2Ch(pressureCalDataL,pressureCalDataR);
                    break;

            }
            int textColor = handlePosition==0?getResources().getColor(android.R.color.white) : getResources().getColor(android.R.color.holo_red_light);
            int holdBgColor = getResources().getColor(android.R.color.holo_green_dark);
            int releaseBgColor = getResources().getColor(android.R.color.background_dark);
            switch(dataTouch){
                case 1://mTvMsg10.setText("Touch L");
                    mTvMsg00.setTextColor(textColor);
                    mTvMsg00.setBackgroundColor(holdBgColor);
                    mTvMsg01.setBackgroundColor(releaseBgColor);
                    break;
                case 2://mTvMsg10.setText("Touch R");
                    mTvMsg00.setBackgroundColor(releaseBgColor);
                    mTvMsg01.setTextColor(textColor);
                    mTvMsg01.setBackgroundColor(holdBgColor);
                    break;
                case 3://mTvMsg10.setText("Touch L&R");
                    mTvMsg00.setTextColor(textColor);
                    mTvMsg00.setBackgroundColor(holdBgColor);
                    mTvMsg01.setTextColor(textColor);
                    mTvMsg01.setBackgroundColor(holdBgColor);
                        break;
                default://mTvMsg10.setText("-");
                    mTvMsg00.setBackgroundColor(releaseBgColor);
                    mTvMsg01.setBackgroundColor(releaseBgColor);
                    break;
            }
            switch(deviceInfoL){
                case 0x00:mTvMsg00.setText("No device");break;
                case 0x01:mTvMsg00.setText("PushupBar");break;
                case 0x02:mTvMsg00.setText("JumpRope");break;
                case 0x03:mTvMsg00.setText("AbsWheel");break;
                case 0x04:mTvMsg00.setText("ResistantBand");break;
                default:mTvMsg00.setText("-");break;
            }

            switch(deviceInfoR){
                case 0x00:mTvMsg01.setText("No device");
                    mTvMsg30.setText(strPressL);
                    mTvMsg31.setText(strPressR);
                    break;
                case 0x10:mTvMsg01.setText("PushupBar");
                    mTvMsg30.setText(strPressL);
                    mTvMsg31.setText(strPressR);
                    switch(property){
                        case 1:updateDebugInfo("Near");break;
                        case 2:updateDebugInfo("Medium");break;
                        case 3:updateDebugInfo("Far");break;
                        default:updateDebugInfo("");break;
                    }
                    break;
                case 0x20:mTvMsg01.setText("JumpRope");
                    mTvMsg30.setText("StepL:\t"+valPressureL);
                    mTvMsg31.setText("StepR:\t"+valPressureR);
                    switch(property){
                        case 1:updateDebugInfo("Forward");break;
                        case 2:updateDebugInfo("Reverse");break;
                        case 3:updateDebugInfo("Fwd & Rev");break;
                        default:updateDebugInfo("");break;
                    }
                    break;
                case 0x30:mTvMsg01.setText("AbsWheel");
                    mTvMsg10.setText("StepWheel:"+stepWheel);
                    mTvMsg30.setText(strPressL);
                    mTvMsg31.setText(strPressR);
                    switch(property){
                        case 1:updateDebugInfo("Push Forward,\t Level 1");break;
                        case 2:updateDebugInfo("Push Left,\t Level 1");break;
                        case 3:updateDebugInfo("Push Right,\t Level 1");break;
                        case 4:updateDebugInfo("Push Forward,\t Level 2");break;
                        case 5:updateDebugInfo("Push Left,\t Level 2");break;
                        case 6:updateDebugInfo("Push Right,\t Level 2");break;
                        case 7:updateDebugInfo("Push Forward,\t Level 3");break;
                        case 8:updateDebugInfo("Push Left,\t Level 3");break;
                        case 9:updateDebugInfo("Push Right,\t Level 3");break;
                        //default:updateDebugInfo("-");break;
                    }
                    break;
                case 0x40:mTvMsg01.setText("ResistantBand");
                    mTvMsg30.setText(strPressL);
                    mTvMsg31.setText(strPressR);
                    switch(property){
                        case 1:updateDebugInfo("shoulder press");break;
                        case 2:updateDebugInfo("shoulder flies 1");break;
                        case 3:updateDebugInfo("shoulder flies 2");break;
                        case 4:updateDebugInfo("overhead tricep ext 1");break;
                        case 5:updateDebugInfo("overhead tricep ext 2");break;
                        case 6:updateDebugInfo("bend over tricep ext");break;
                        case 7:updateDebugInfo("7");break;
                        case 8:updateDebugInfo("waiting cycle");break;
                        //default:updateDebugInfo("");break;
                    }
                    break;
                default:mTvMsg01.setText("-");break;
            }
            mTvMsg11.setText(Integer.toString(debugData0)+"("+debugDataStr0+")");
            mTvMsg12.setText(Integer.toString(debugData1)+"("+debugDataStr1+")");
            mTvMsg20.setText("Roll L: " + Integer.toString(rollL));
            mTvMsg21.setText("Roll R: " + Integer.toString((rollR)));
            mTvMsg22.setText("Count: " + Integer.toString((count)));
            //mTvMsg30.setText("pressL: "+Integer.toString(valPressureL));
            //mTvMsg31.setText("pressR: "+Integer.toString(valPressureR));
            mTvMsg32.setText("RF Power:"+Integer.toString(rfDistance));
        }
        if(dataType == 2){//device ID
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (int i=7;i<dataCount+1;i++)
                stringBuilder.append(String.format("%02X", data[i]));
            mTvMsg40.setText("DeviceID: " + stringBuilder.toString());
            mTvMsg40.startAnimation(mBlinkAnimation);
        }
        if(dataType == 3){//Firmware ver. and battey info
            final StringBuilder stringBuilder = new StringBuilder(4);
            for (int i=0;i<4;i++)
                stringBuilder.append(String.format("%02X", data[10-i]));
            mTvMsg50.setText("Firmware ver.: " + stringBuilder.toString());
            mTvMsg50.startAnimation(mBlinkAnimation);
            int voltageR =  (int) ((data[11] & 0xFF) | ((data[12] & 0xFF)<<8));
            int voltageL =  (int) ((data[13] & 0xFF) | ((data[14] & 0xFF)<<8));
            DecimalFormat format = new DecimalFormat("0.000");
            String strVolL = "L: " + format.format((float)voltageL/1000);
            String strVolR = "R: " + format.format((float)voltageR/1000);
            mTvMsg60.setText("Battery info(v): " + strVolL +" \t"+ strVolR);
            mTvMsg60.startAnimation(mBlinkAnimation);
        }
        if(dataType==0x7){//sensor debug
            if(dataCount==3){
                updateDebugInfo(data[3]==0x00?"Sensor Updated":"Sensor Failed");
                createNoticeDialog(data[3]==0x00?"Sensor Updated":"Sensor Failed");
            }
        }
        if(dataType==0x8){//user info
            if(dataCount==8){
                updateDebugInfo(data[8]==0x00?"UserInfo Uploaded":"UserInfo Upload Failed");
                createNoticeDialog(data[8]==0x00?"UserInfo Uploaded":"UserInfo Upload Failed");
            }else{
                byte[] userInfo = new byte[8];
                for(int i = 0;i<userInfo.length;i++){
                    userInfo[i]=data[i+3];
                    mEtUserId[i].setText(byteToString(data[i+3]));
                }
                int height = (int) ((data[11] & 0xFF) | ((data[12] & 0xFF)<<8));
                int weight = (int) data[13] & 0xff;
                int valReserved = (int) data[14] & 0xff;
                int pushbarInfo = (int) data[15] & 0xff;
                int abswheelInfo = (int) data[16] & 0xff;
                int pressMaxL = (int) data[17] & 0xff;
                int pressMaxR = (int) data[18] & 0xff;
                //mEtUserInfo1.setText(bytesToString(userInfo, " "));
                mEtHeight.setText(Integer.toString(height));
                mEtWeight.setText(Integer.toString(weight));
                mEtInfoReserved.setText(Integer.toString(valReserved));
                mEtInfoPushbar.setText(Integer.toString(pushbarInfo));
                mEtInfoAbsWheel.setText(Integer.toString(abswheelInfo));
                mEtInfoPressMaxL.setText(Integer.toString(pressMaxL));
                mEtInfoPressMaxR.setText(Integer.toString(pressMaxR));
                //mEtUserInfo1.startAnimation(mBlinkAnimation);
            }
        }
        if(dataType==0x09){//pairing code return
            updateDebugInfo("PairCode returned");
            pairCodeFb1 = new byte[8];
            pairCodeFb2 = new byte[8];
            System.arraycopy(data,3,pairCodeFb1,0,pairCodeFb1.length);
            //System.arraycopy(data,11,pairCodeFb2,0,pairCodeFb2.length);
            long lData = bytesToLong(pairCodeFb1,0)*3+3;
            pairCode2 = longToBytes(lData);
            System.arraycopy(pairCode2,0,appCmd.txCmdA,3,pairCode2.length);
            System.arraycopy(pairCodeSub,0,appCmd.txCmdA,11,pairCodeSub.length);
            sendBleData(appCmd.txCmdA);
            mEtPairCode.setText(bytesToString(pairCodeFb1," ")+" "+bytesToString(pairCodeFb2," "));
        }
        if(dataType==0x0a) {//pairing code return
            updateDebugInfo(data[8]==0x00?"Pairing Done":"Pairing Failed");
            createNoticeDialog(data[8]==0x00?"Pairing Done":"Pairing Failed");
        }
        if(dataType==0x0b){//sensor set Zero
            mProgressDialog.dismiss();
            String sResult = (data[8]==0)?"Done":"Failed";
            String sSensorSetType = "";
            switch(data[7]&0xf0){
                case 0x30:
                    sSensorSetType = "G Sensor ";
                    break;
            }
            switch(setZeroType0){
                case SENSOR_PRESSURE_CALIBRATION:
                case SENSOR_PRESSURE_RESET:
                    mSwRptPress.setChecked(true);
                    break;
            }
            setZeroType0 = 0;
            String result = sSensorSetType + "Initialize " + sResult;
            mTvInfoDebug.setText(result);
            createNoticeDialog(result);
        }
        if(dataType==0x06){
            switch(data[3]){
                case 'c'://checksum
                    if(data[4]==0x0)updateDebugInfo("checksum ok.");
                    else updateDebugInfo("verify failed");
                    break;
                case 'e'://erase
                    if(data[4]==0x0)updateDebugInfo("OTA erase done.");
                    else updateDebugInfo("OTA erase failed");
                    break;
                case 'p'://program
                    if(data[4]==0x0)otaFBRet=true;
                    else updateDebugInfo("OTA erase failed");
                    break;
                case 0x00:
                    updateDebugInfo("Entered/Exit OTA");
                    break;
                case 0x01:
                    updateDebugInfo("Enter/Exit OTA failed");
                    break;
            }
        }
        if (dataType == 0x0f){
            //clearInfoDisplay();
            switch (data[3]){
                case 0x01:
                    for(int i = 0;i<valGensorL.length;i++){
                        valGensorL[i] = ((data[4+i*2] & 0xFF) | ((data[5+i*2])<<8));
                        appendDataArray(gDataArrayL[i],valGensorL[i]);
                    }

                    mTvMsgCh1.setText(""+valGensorL[0]);
                    mTvMsgCh2.setText(""+valGensorL[1]);
                    mTvMsgCh3.setText(""+valGensorL[2]);
                    mTvMsgCh4.setText(""+valGensorL[3]);
                    mTvMsgCh5.setText(""+valGensorL[4]);
                    mTvMsgCh6.setText(""+valGensorL[5]);
                    if(isCheckGSensorSteady){
                        if(!checkIfGSensorLSteady()){
                            stopAvgSamplingGSensor();
                            stopCheckIfGSensorSteady();
                            createNoticeDialog("Please keep the handle steady and try again");
                            updateDebugInfo("Please keep the Handle Steady");
                        }else{
                            if(checkSensorLCnt>WAVEFORM_DATA_LENGTH){
                                stopAvgSamplingGSensor();
                                stopCheckIfGSensorSteady();
                                mProgressDialog.dismiss();
                                sendGSensorInitCommand();
                            }
                        }
                    }
                    break;
                case 0x02:
                    for(int i = 0;i<valGensorR.length;i++){
                        valGensorR[i] = ((data[4+i*2] & 0xFF) | ((data[5+i*2])<<8));
                        appendDataArray(gDataArrayR[i],valGensorR[i]);
                    }
                    mTvMsgCh1.setText(""+valGensorR[0]);
                    mTvMsgCh2.setText(""+valGensorR[1]);
                    mTvMsgCh3.setText(""+valGensorR[2]);
                    mTvMsgCh4.setText(""+valGensorR[3]);
                    mTvMsgCh5.setText(""+valGensorR[4]);
                    mTvMsgCh6.setText(""+valGensorR[5]);
                    if(isCheckGSensorSteady){
                        if(!checkIfGSensorRSteady()){
                            stopAvgSamplingGSensor();
                            stopCheckIfGSensorSteady();
                            createNoticeDialog("Please keep the handle steady and try again");
                            updateDebugInfo("Please keep the Handles Steady");
                        }else{
                            if(checkSensorRCnt>WAVEFORM_DATA_LENGTH){
                                stopAvgSamplingGSensor();
                                stopCheckIfGSensorSteady();
                                mProgressDialog.dismiss();
                                sendGSensorInitCommand();
                            }
                        }
                    }
                    break;
                case 0x03:
                    valYawL = ((data[4] & 0xFF) | ((data[5])<<8));
                    valPitchL = ((data[6] & 0xFF) | ((data[7])<<8));
                    valRollL = ((data[8] & 0xFF) | ((data[9])<<8));
                    valYawR = ((data[10] & 0xFF) | ((data[11])<<8));
                    valPitchR = ((data[12] & 0xFF) | ((data[13])<<8));
                    valRollR = ((data[14] & 0xFF) | ((data[15])<<8));
                    appendDataArray(yawDataL,valYawL);
                    appendDataArray(yawDataR,valYawR);
                    appendDataArray(pitchDataL,valPitchL);
                    appendDataArray(pitchDataR,valPitchR);
                    appendDataArray(rollDataL,valRollL);
                    appendDataArray(rollDataR,valRollR);
                    mTvMsgCh1.setText(""+valYawL);
                    mTvMsgCh2.setText(""+valPitchL);
                    mTvMsgCh3.setText(""+valRollL);
                    mTvMsgCh4.setText(""+valYawR);
                    mTvMsgCh5.setText(""+valPitchR);
                    mTvMsgCh6.setText(""+valRollR);
                    break;
            }
            switch (mScopeSel){
                case SCOPE_SEL_ATTITUDE:
                    playWaveform6Ch(yawDataL,pitchDataL,rollDataL,yawDataR,pitchDataR,rollDataR);
                    break;
                case SCOPE_SEL_6AXIS_RIGHT:
                    playWaveform6Ch(gDataArrayR[0], gDataArrayR[1], gDataArrayR[2], gDataArrayR[3], gDataArrayR[4], gDataArrayR[5]);
                    break;
                case SCOPE_SEL_6AXIS_LEFT:
                    playWaveform6Ch(gDataArrayL[0], gDataArrayL[1], gDataArrayL[2], gDataArrayL[3], gDataArrayL[4], gDataArrayL[5]);
                    break;
            }
        }
        if (dataType == 0x10){
            int[] pressData = new int[8];
            for(int i=0;i<pressData.length;i++){
                pressData[i]= ((data[i*2+3] & 0xFF) | ((data[i*2+4])<<8));
            }

            appendDataArray(pressureRawDataL, pressData[0]);
            appendDataArray(pressureRawDataR, pressData[1]);
            appendDataArray(pressureZeroDataL,pressData[2]);
            appendDataArray(pressureZeroDataR,pressData[3]);
            appendDataArray(pressureCalDataL, pressData[4]);
            appendDataArray(pressureCalDataR, pressData[5]);
            float pressL = (float)pressData[4] * 10 / 1000;
            float pressR = (float)pressData[5] * 10 / 1000;
            //pressL = 99.998f;
            //pressR = -999.998f;
            DecimalFormat format = new DecimalFormat("0.000");
            String strPressL = "PressL(Kg): " + format.format(pressL);
            String strPressR = "PressR(Kg): " + format.format(pressR);
            mTvMsg30.setText(strPressL);
            mTvMsg31.setText(strPressR);
            mTvMsgCh1.setText(Integer.toString(pressureCalDataL[pressureCalDataL.length-1]));
            mTvMsgCh2.setText(Integer.toString(pressureCalDataR[pressureCalDataR.length-1]));
            mTvMsgExt00.setText("p-left:\t"+Integer.toString(pressData[0]));
            mTvMsgExt01.setText("p-right:\t"+Integer.toString(pressData[1]));
            mTvMsgExt10.setText("pl-zero:\t"+Integer.toString(pressData[2]));
            mTvMsgExt11.setText("pr-zero:\t"+Integer.toString(pressData[3]));
            mTvMsgExt20.setText("pl-cal:\t"+Integer.toString(pressData[4]));
            mTvMsgExt21.setText("pr-cal:\t"+Integer.toString(pressData[5]));
            mTvMsgExt30.setText("pl:\t"+Integer.toString(pressData[6]));
            mTvMsgExt31.setText("pr:\t"+Integer.toString(pressData[7]));

            switch(setZeroType){
                case SENSOR_PRESSURE_RESET:
                    if(!checkIfPSensorRSteady()){
                        stopAvgSamplingPSensor();
                        createNoticeDialog("Please keep the handle steady and try again");
                    }else{
                        if(samplePointer > 1500/AVG_SAMPLE_INTERVAL2){
                            stopAvgSamplingPSensor();
                            mProgressDialog.dismiss();
                            sendPSensorInitCommand();
                            //createNoticeDialog("Please keep the handle steady and try again");
                        }
                    }
                    break;
                case SENSOR_PRESSURE_CALIBRATION:
                    if(!checkIfPSensorRSteady()){
                        stopAvgSamplingPSensor();
                        createNoticeDialog("Please keep the handle steady and try again");
                    }else{
                        if(samplePointer > 1500/AVG_SAMPLE_INTERVAL2){
                            stopAvgSamplingPSensor();
                            mProgressDialog.dismiss();
                            sendPSensorCalCommand();
                            //createNoticeDialog("Please keep the handle steady and try again");
                        }
                    }
                    break;
            }

            switch(mScopeSel){
                case SCOPE_SEL_PRESSURE:
                    playWaveform2Ch(pressureCalDataL,pressureCalDataR);
                    break;

            }

        }
        if (dataType == 0xff) {
            updateDebugInfo("!!!ERROR!!!");
        }
        return true;
    }

    private void pressureCalbration(){


    }

    private void sendGSensorInitCommand(){
        createProgressDialog("Wait Handles Feedback");
        appCmd.txCmdB_3[7] = 0x33;
        sendBleData(appCmd.txCmdB_3);
    }
    private void sendPSensorCalCommand(){
        createProgressDialog("Wait Handles Feedback");
        int sumL = 0;
        int sumR = 0;
        for(int i=0;i<AVG_SAMPLE_LENGTH;i++){
            sumL += pressureZeroDataL[pressureZeroDataL.length-1-AVG_SAMPLE_LENGTH+i];
            sumR += pressureZeroDataR[pressureZeroDataR.length-1-AVG_SAMPLE_LENGTH+i];
        }
        int avgL = sumL / AVG_SAMPLE_LENGTH;
        int avgR = sumR / AVG_SAMPLE_LENGTH;
        float f1 = 1.0f;
        float f2 = 1.0f;

        int input = Integer.parseInt(mEtRefVal.getText().toString());
        f1 = (float) input /10;
        f2 = (float) input /10;
        f1 = f1/(float)avgL;
        f2 = f2/(float)avgR;
        byte[] pressCalLByte = floatToBytes(f1);
        byte[] pressCalRByte = floatToBytes(f2);
        appCmd.txCmdB_2[7] = 0x23;
        appCmd.txCmdB_2[8] = pressCalRByte[0];
        appCmd.txCmdB_2[9] = pressCalRByte[1];
        appCmd.txCmdB_2[10] = pressCalRByte[2];
        appCmd.txCmdB_2[11] = pressCalRByte[3];
        appCmd.txCmdB_2[12] = pressCalLByte[0];
        appCmd.txCmdB_2[13] = pressCalLByte[1];
        appCmd.txCmdB_2[14] = pressCalLByte[2];
        appCmd.txCmdB_2[15] = pressCalLByte[3];
        sendBleData(appCmd.txCmdB_2);



    }
    private void sendPSensorInitCommand(){
        createProgressDialog("Wait Handles Feedback");
        int sumL = 0;
        int sumR = 0;
        for(int i=0;i<AVG_SAMPLE_LENGTH;i++){
            sumL += pressureRawDataL[pressureRawDataL.length-1-AVG_SAMPLE_LENGTH+i];
            sumR += pressureRawDataR[pressureRawDataR.length-1-AVG_SAMPLE_LENGTH+i];
        }
        int avgL = sumL / AVG_SAMPLE_LENGTH;
        int avgR = sumR / AVG_SAMPLE_LENGTH;
        byte[] avgLbyte = intToBytes(avgL);
        byte[] avgRbyte = intToBytes(avgR);
        appCmd.txCmdB_1[7] = 0x13;
        appCmd.txCmdB_1[8] = avgRbyte[0];
        appCmd.txCmdB_1[9] = avgRbyte[1];
        appCmd.txCmdB_1[12] = avgLbyte[0];
        appCmd.txCmdB_1[13] = avgLbyte[1];
        sendBleData(appCmd.txCmdB_1);
    }



    private void appendDataArray(short[] dataArray,short data){
        System.arraycopy(dataArray,1,dataArray,0,dataArray.length-1);
        dataArray[dataArray.length-1] = data;
    }
    private void appendDataArray(short[] dataArray,int data){
        System.arraycopy(dataArray,1,dataArray,0,dataArray.length-1);
        dataArray[dataArray.length-1] = (short)data;
    }
    private void appendDataArray(short[] dataArray,byte[] data,int start){
        short sData = (short) ((data[start] & 0xFF) | ((data[start+1] & 0xFF)<<8));
        System.arraycopy(dataArray,1,dataArray,0,dataArray.length-1);
        dataArray[dataArray.length-1] = sData;
    }

    void playWaveform(short[] dataArray){
        if(mWaveformView1!=null)
            mWaveformView1.setDataBuffer(dataArray);
    }

    void playWaveform2Ch(short[] dataArray1,short[] dataArray2){
        if (mWaveformView1!=null)
            mWaveformView1.setDataBuffer2Ch(dataArray1, dataArray2, WAVEFORM_DATA_LENGTH);
    }

    void playWaveform3Ch(short[] dataArray1,short[] dataArray2,short[] dataArray3){
        if (mWaveformView1!=null)
            mWaveformView1.setDataBuffer3Ch(dataArray1, dataArray2, dataArray3, WAVEFORM_DATA_LENGTH);
    }
    void playWaveform6Ch(short[] dataArray1,short[] dataArray2,short[] dataArray3,short[] dataArray4,short[] dataArray5,short[] dataArray6){
        if (mWaveformView1!=null)
            mWaveformView1.setDataBuffer6Ch(dataArray1, dataArray2, dataArray3, dataArray4, dataArray5, dataArray6, WAVEFORM_DATA_LENGTH);
    }
}