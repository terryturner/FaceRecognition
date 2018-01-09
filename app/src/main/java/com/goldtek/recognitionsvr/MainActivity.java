package com.goldtek.recognitionsvr;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends Activity {

    String TAG = "MainActivity";
    TextView infoIp;
    TextView infoMsg;
    Context m_contx;

    ScrollView scrollview1;

    TCPServer m_server = null;

    private MainHandler mHandler = new MainHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_contx = this;
        Thread.setDefaultUncaughtExceptionHandler(new CrashLog(Common.m_szRoot));

        infoIp = (TextView) findViewById(R.id.infoip);
        infoMsg = (TextView) findViewById(R.id.msg);
        scrollview1 = (ScrollView) findViewById(R.id.scrollview1);
        Common.GetEnvPath(m_contx);

        String szInfo = "Listen Server: " + Common.getIpAddress() + ":" + String.valueOf(Common.PORT);
        infoIp.setText(szInfo);
        Common.LOGSvr.debug("##############################################################");
        Common.LOGSvr.debug(szInfo);

        m_server = new TCPServer(Common.PORT, mHandler);
        new Thread(m_server).start();
        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(m_server == null){
            m_server = new TCPServer(Common.PORT, mHandler);
            new Thread(m_server).start();
        }

        if(Common.m_oTraining == null){
            Common.m_oTraining = new CTraining(m_contx);
        }
        if(Common.m_oRecog == null) {
            Common.m_oRecog = new CRecognition(m_contx);
        }

        if (Common.m_oDetect == null) {
            Common.m_oDetect = new CDetection(m_contx);
        }
//            // Test Recognition
//            String filepath = Common.COMPARE_PATH + "/Fred_4.png";
//            File imagefile = new File(filepath);
//            FileInputStream fis = null;
//            try {
//                fis = new FileInputStream(imagefile);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//
//            Bitmap bmp = BitmapFactory.decodeStream(fis);
//            Common.m_oRecog.setBitmap(bmp);
//            Common.m_oRecog.Recognition();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "Stopping Server");
        if(m_server != null) {
            m_server.stop();
            m_server = null;
        }
        if(Common.m_oTraining != null){
            Common.m_oTraining = null;
        }
        if(Common.m_oRecog != null){
            Common.m_oRecog = null;
        }
        if(Common.m_oDetect != null){
            Common.m_oDetect = null;
        }
    }

    /***
     * Handler Callback for receive messages
     */
    private static class MainHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        SimpleDateFormat dateformat = new SimpleDateFormat("MM/dd HH:mm:ss");

        public MainHandler(MainActivity activity) {

            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            String TYPE = msg.getData().getString(Common.Hndl_MSGTYPE, "");
            String szMsg = "";
            if(TYPE.compareTo(Common.MSGTYPE.INFO) == 0){
                szMsg = msg.getData().getString(Common.Hndl_MSG, "");
            }
            else if(TYPE.compareTo(Common.MSGTYPE.REGISTER) == 0 || TYPE.compareTo(Common.MSGTYPE.REGISTER_DONE) == 0){
                szMsg = msg.getData().getString(Common.Hndl_MSG, "");

            }
            else if(TYPE.compareTo(Common.MSGTYPE.LOGIN) == 0 || TYPE.compareTo(Common.MSGTYPE.LOGIN_DONE) == 0){
                szMsg = msg.getData().getString(Common.Hndl_MSG, "");
            }

            if(activity!= null && activity.infoMsg != null){
                activity.infoMsg.append(dateformat.format(Calendar.getInstance().getTime()) + " " + szMsg + "\n");

                int nY_Pos = activity.infoMsg.getBottom(); // getBottom(); X_pos  getLeft(); getRight();
                // scroll to top of hEdit
                activity.scrollview1.scrollTo(0,nY_Pos);
            }

            Common.LOGSvr.debug(szMsg);
        }
    }




//    public void OnReceiveMsg(String msg) {
//        final String str = msg;
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                infoMsg.setText(str);
//            }
//        });
//    }


}
