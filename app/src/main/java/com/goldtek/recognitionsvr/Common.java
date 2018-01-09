package com.goldtek.recognitionsvr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;


import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by darwinhu on 2017/12/18.
 */
@SuppressWarnings("WeakerAccess")
public class Common {

    private static final String TAG = "Common";

    public static CTraining m_oTraining = null;
    public static CRecognition m_oRecog = null;
    public static CDetection m_oDetect = null;

    public static final String m_szRoot = "GTRecog";
    /***  Path Information
     * Common.m_szExtAppPath, 	/mnt/sdcard/Android/$PACKAGE/files
     * Common.m_szExtRootPath,	/mnt/sdcard/Android/$PACKAGE/files/$Common.m_szRoot
     * Common.m_szIntAppPath, 	/data/data/$PACKAGE/files
     * Common.m_szExtMntPath, 	/mnt/sdcard or $SECOND_VOLUME_STORAGE
     */
    public static String CRASHLOG = "CrashLog";
    public static String m_szExtAppPath = "/e";
    public static String m_szExtRootPath = "/e";
    public static String m_szIntAppPath = "/e";
    public static String m_szExtMntPath = "/e";
    public static String m_szCrashLogPath = "/e";

    public static String TRAINING_PATH = "/e";
    public static String COMPARE_PATH = "/e";
    /***
     *  Preference variable
     */
    public static final String PREFERENCE = "GT_RECOG";
    public static final String P_EXTROOT_PATH = "EXTROOT_PATH";
    public static final String P_INTAPP_PATH = "INTAPP_PATH";
    public static final String P_EXTAPP_PATH = "EXTAPP_PATH";
    public static final String P_EXTMNT_PATH = "EXTMNT_PATH";

    public static final String LOGLV_DEBUG = "DEBUG";
    public static CLog LOGSvr = null;

    public static int REGISTER_PIC = 10;
    public static int LOGIN_PIC = 1;
    public static int PORT = 6666;

    public static String RES_YES = "1";
    public static String RES_NO = "-1";
    public static String RES_UNKNOWN = "UNKNOWN";

    public static int TIMER_PERIOD = 5 * 1000; // seconds
    public static int KEEP_IDLE_SOCKET = 10 * 60 * 1000; // minutes
    public static int COUNT_TIMES = KEEP_IDLE_SOCKET/TIMER_PERIOD;

    /*** Handler Messages TYPE ***/
    public static String Hndl_MSG             = "MSG";
    public static String Hndl_MSGTYPE         = "MSGTYPE";
    public static class MSGTYPE {
        static final String REGISTER        = "REGISTER";
        static final String REGISTER_DONE   = "REGISTER_DONE";
        static final String LOGIN           = "LOGIN";
        static final String LOGIN_DONE      = "LOGIN_DONE";
        static final String INFO            = "INFO";
    }

    /*** Handler Messages TYPE ***/
    public static class PROTOCOL {
        static final String CMD = "cmd";
        static final String NAME = "name";
        static final String ID = "id";
    }

    /*** Handler Messages TYPE ***/
    public static class COMMAND {
        static final String REG = "REGISTER";
        static final String LOGIN = "LOGIN";
    }

//    /*** Client Receive ***/
//    public enum RECV {
//        REG(0),
//        LOGIN(1);
//
//        private int value;
//        RECV(int value) {
//            this.value = value;
//        }
//        public int val() {
//            return value;
//        }
//
//        public static RECV getKey(int code) {
//            for (RECV e : RECV.values()) {
//                if (code == e.val()) return e;
//            }
//            return RECV.REG;
//        }
//    }

     /**
     * Create Folder
     * @param szExtPath
     * @param szFolderName
     * @return Full path of created folder
     */
    public static String createFolderIfNotExisting(String szExtPath, String szFolderName) {
        String strPath = "";
        if (!szExtPath.isEmpty()) {
            File file;
            strPath = szExtPath + "/" + szFolderName;
            file = new File(strPath);

            if (!file.exists()) {
                boolean status = file.mkdir();
                if (status == true)
                    Log.i(TAG, strPath + " Create Folder OK");
            } else {
                Log.i(TAG, strPath + " File Already Exist");
            }
        }

        return strPath;
    }

//    public static void deleteRecursive(File fileOrDirectory) {
//        if (fileOrDirectory.isDirectory())
//            for (File child : fileOrDirectory.listFiles()) {
//                Log.d(TAG, "Delete files: " + child.getName());
//                deleteRecursive(child);
//            }
//        fileOrDirectory.delete();
//    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                Log.d(TAG, "Delete files: " + child.getName());
                child.delete();
            }
        }
    }

    public static  String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress();
                        Log.i(TAG, inetAddress.getHostAddress());
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    public static void GetEnvPath(Context m_contx) {
        /***  Path Information
         * Common.m_szExtAppPath, 	/mnt/sdcard/Android/$PACKAGE/files
         * Common.m_szExtRootPath,	/mnt/sdcard/Android/$PACKAGE/files/$Common.m_szRoot
         * Common.m_szIntAppPath, 	/data/data/$PACKAGE/files
         * Common.m_szExtMntPath, 	/mnt/sdcard or $SECOND_VOLUME_STORAGE
         */
        Common.m_szExtAppPath = m_contx.getExternalFilesDir(null).toString();//m_contx.getExternalCacheDir().toString();
        Common.m_szExtRootPath = Common.createFolderIfNotExisting(Common.m_szExtAppPath, Common.m_szRoot);
        Common.m_szIntAppPath = m_contx.getFilesDir().toString();
        Common.m_szCrashLogPath = Common.createFolderIfNotExisting(Common.m_szExtAppPath, Common.CRASHLOG);

        Common.TRAINING_PATH = Common.createFolderIfNotExisting(Common.m_szExtAppPath, "training");
        Common.COMPARE_PATH = Common.createFolderIfNotExisting(Common.m_szExtAppPath, "compare");

        String SecondaryStoragesStr = System.getenv("SECOND_VOLUME_STORAGE");
        if (SecondaryStoragesStr != null && !SecondaryStoragesStr.equals("")) {
            Common.m_szExtMntPath = SecondaryStoragesStr;
        } else {
            SecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
            if (SecondaryStoragesStr != null && !SecondaryStoragesStr.equals("")) {
                Common.m_szExtMntPath = SecondaryStoragesStr;
            } else {
                Common.m_szExtMntPath = Environment.getExternalStorageDirectory().getAbsolutePath();    // it should be /mnt/sdcard
            }
        }
//        Log.i(TAG, "m_szExtAppPath: " + Common.m_szExtAppPath);
//        Log.i(TAG, "m_szExtRootPath: " + Common.m_szExtRootPath);
//        Log.i(TAG, "m_szIntAppPath: " + Common.m_szIntAppPath);
//        Log.i(TAG, "m_szExtMntPath: " + Common.m_szExtMntPath);

        //SAVE LAST LOGIN Info to configuration
        SharedPreferences share = m_contx.getSharedPreferences(Common.PREFERENCE, m_contx.MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();

        editor.putString(Common.P_EXTROOT_PATH, Common.m_szExtRootPath);
        editor.putString(Common.P_INTAPP_PATH, Common.m_szIntAppPath);
        editor.putString(Common.P_EXTAPP_PATH, Common.m_szExtAppPath);
        editor.putString(Common.P_EXTMNT_PATH, Common.m_szExtMntPath);

        editor.commit();

        // Just need Initialize Once
        String LOG_FullPathName = Common.m_szExtAppPath + "/JavaLOG";
        CLog oLog = new CLog(LOG_FullPathName, Common.LOGLV_DEBUG);
        LOGSvr = new CLog("RecogSvr");
    }

    public static String getTagValue(String xml, String tagName){
        return xml.split("<"+tagName+">")[1].split("</"+tagName+">")[0];
    }

    public static String readByteToString(byte[] szName){
        return new String(szName).replaceAll("[^\\p{Print}]","");
    }

    public static void writeIntToByte(byte[] data, int offset, int value) {
        data[offset] = (byte)((value >>> 24) & 0xFF);
        data[offset + 1] = (byte)((value >>> 16) & 0xFF);
        data[offset + 2] = (byte)((value >>> 8) & 0xFF);
        data[offset + 3] = (byte)((value >>> 0) & 0xFF);
    }

    public static int readByteToInt(byte[] data, int offset) {
        int ch1 = data[offset] & 0xff;
        int ch2 = data[offset + 1] & 0xff;
        int ch3 = data[offset + 2] & 0xff;
        int ch4 = data[offset + 3] & 0xff;
        return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
    }

    public static String ComposeResponse(String szInfo, String szResult){
        String szMsg = String.format("<GOLDTEK><info>%s</info><result>%s</result></GOLDTEK>",
                szInfo, szResult);
        return szMsg;
    }

    public static boolean FolderExist(String szPath){
        File dir = new File(szPath);
        if(dir.exists() && dir.isDirectory()) {
            return true;
        }
        return false;
    }
}
