package com.goldtek.recognitionsvr;

/**
 * Created by darwinhu on 2017/12/18.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CClientSocket implements Runnable{

    String TAG = "CClientSocket";
    protected static int BUFFSIZE = 10 * 1024;
    protected Socket clientSocket = null;
    protected String serverText   = "";
    public boolean      isClosed    = false;


    /**
     * Client Info
     * <cmd></cmd><name></name><id></id>
     */
    public int      m_nID;
    public String   m_szName;
    public String   m_szCmd = "";
    public String   m_szClientID;

    private Handler mHandler;
    private DataInputStream mInput;

    private MyTimerTask myTask = new MyTimerTask();
    private Timer myTimer = new Timer();
    private long lastRecvTime;
    private int nTimer = 0;
    private String m_szImageLoginRes = "";

    public CClientSocket(Socket clientSocket, int nID, Handler handler) {
        this.clientSocket = clientSocket;
        this.m_nID   = nID;

        this.mHandler = handler;
        String ip=(((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/","");
        callback_msg(Common.MSGTYPE.INFO, "*** New Client " + ip + " ***");
    }

    class MyTimerTask extends TimerTask {
        public void run() {
//            long checktime = System.currentTimeMillis();
//            int total = Integer.parseInt(String.valueOf((checktime - lastRecvTime)));
//            if(total >= Common.KEEP_IDLE_SOCKET ) {
//                Log.w(TAG, "Socket Idle Time: " + String.valueOf((checktime - lastRecvTime)) + " ms");
//                if (m_szCmd.compareTo(Common.COMMAND.REG) == 0) {
//                    // Register the rest image
//                    ExecuteRegister();
//                }
//                Stop();
//                this.cancel();
//            }

            if(nTimer >= Common.COUNT_TIMES){
                Log.d(TAG, "nTimer :" + String.valueOf(nTimer));
                if (m_szCmd.compareTo(Common.COMMAND.REG) == 0) {
                    // Register the rest image
                    ExecuteRegister();
                }
                Stop();
                this.cancel();
            }
            nTimer++;
        }
    }

    public void run() {
        myTimer.schedule(myTask, 1000, Common.TIMER_PERIOD);
        try {
            byte[] inputData = null;
            boolean bCheckHeader = false, bCheckRegPath = false, bCheckLoginPath = false;
            String fullPath = "";

            DataPacket oPckt = null;

            int nPos = 0, nImageSize = 0;
            int nLastRemain = 0;
            byte[] remainData = null;

            mInput = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

            //boolean bStartTraining = false, bStartCompare = false;
            int nRecvImage = 0;
            byte[] byteArrayLogin;
            int ntmp = 0;
            while(clientSocket.isConnected()){
                try{
                    inputData = new byte[BUFFSIZE];
                    int nRecvSize = mInput.read(inputData);
                    if(nRecvSize == -1)
                        break;
                    else{
                        nTimer = 0;
                        //lastRecvTime = System.currentTimeMillis();
                        //mOutput = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
                        String szRecv = new String(inputData);
                        // Authorization
                        if(szRecv.contains(oPckt.INFO_START) && szRecv.contains(oPckt.INFO_END)) {
                            // Client Make Connection for the first time
                            String szMessage = Common.readByteToString(inputData);
                            if (szMessage.compareTo("") != 0) {
                                m_szCmd = Common.getTagValue(szMessage, Common.PROTOCOL.CMD);
                                m_szName = Common.getTagValue(szMessage, Common.PROTOCOL.NAME);
                                m_szClientID = Common.getTagValue(szMessage, Common.PROTOCOL.ID);
                                callback_msg(Common.MSGTYPE.INFO,  szMessage);

                                // Send the ack
                                //byte[] bytes = Common.RES_YES.getBytes();
                                String szResponse = "";
                                if (m_szCmd.compareTo(Common.COMMAND.REG) == 0)
                                    szResponse = Common.ComposeResponse(Common.COMMAND.REG, Common.RES_YES);
                                if (m_szCmd.compareTo(Common.COMMAND.LOGIN) == 0)
                                    szResponse = Common.ComposeResponse(Common.COMMAND.LOGIN, Common.RES_YES);
                                Sending(szResponse.getBytes());
                                Log.d(TAG, "--> " + szResponse);
                            }
                        }
                        else{
                            if(ntmp % 100==0 && ntmp!=0)
                                Log.d(TAG, "RecvSize: " +String.valueOf(nRecvSize));
                            ntmp++;

                            if (bCheckRegPath == false) {
                                bCheckRegPath = true;
                                if (m_szCmd.compareTo(Common.COMMAND.REG) == 0) {
                                    fullPath = Common.createFolderIfNotExisting(Common.TRAINING_PATH, String.valueOf(m_szClientID));
                                }
                                else if (m_szCmd.compareTo(Common.COMMAND.LOGIN) == 0) {
                                    fullPath = Common.COMPARE_PATH;
                                }
                                Common.deleteRecursive(new File(fullPath));
                            }
                            if(!bCheckHeader){
                                // region CheckHeader
                                if(oPckt == null)
                                    oPckt = new DataPacket();

                                nPos = 0;
                                nImageSize = 0;

                                // Append remain data from last receive
                                if(nLastRemain > oPckt.HEADERSIZE) {
                                    // last remain data bigger than HEADER size
                                    System.arraycopy(remainData, 0, oPckt.m_Header, 0, oPckt.HEADERSIZE);
                                    oPckt.m_baos.write(remainData, oPckt.HEADERSIZE, (nLastRemain - oPckt.HEADERSIZE));
                                    nPos += (nLastRemain - oPckt.HEADERSIZE);

                                    oPckt.m_baos.write(inputData, 0, nRecvSize);
                                    nPos += nRecvSize;
                                    nLastRemain = 0;
                                }
                                else {
                                    // If the last packet smaller than HEADER size
                                    if(nLastRemain != 0)
                                        System.arraycopy(remainData, 0, oPckt.m_Header, 0, nLastRemain);

                                    // Parsing the HEADER
                                    System.arraycopy(inputData, 0, oPckt.m_Header, nLastRemain, (oPckt.HEADERSIZE - nLastRemain));
                                    int nLength = nRecvSize -  (oPckt.HEADERSIZE - nLastRemain);
                                    oPckt.m_baos.write(inputData, (oPckt.HEADERSIZE - nLastRemain), nLength);
                                    nPos += nLength;
                                    nLastRemain = 0;
                                }

                                String szHeader = Common.readByteToString(oPckt.m_Header);
                                if(szHeader.startsWith(DataPacket.HEADER_START) && szHeader.endsWith(DataPacket.HEADER_END)){
                                    Log.i(TAG, "<--" +  szHeader);
                                    bCheckHeader = true;
                                    oPckt._ID = Common.getTagValue(szHeader, DataPacket.TAG_ID );
                                    oPckt._Name = Common.getTagValue(szHeader, DataPacket.TAG_NAME);
                                    oPckt._Size = Common.getTagValue(szHeader, DataPacket.TAG_SIZE);
                                    nImageSize = Integer.parseInt(oPckt._Size);
                                }
                                else {
                                    Log.e(TAG, "No Contain Header");
                                    bCheckHeader = false;
                                    nPos = nImageSize = nLastRemain = 0;
                                    oPckt = null;
                                    String szResponse = Common.ComposeResponse("", Common.RES_NO);
                                    Sending(szResponse.getBytes());
                                }

                                // endregion
                            }
                            else {
                                // region Receiving Image Data
                                if ((nPos + nRecvSize) > nImageSize) {
                                    // Receive the last packet
                                    int nLastData = nImageSize - nPos;
                                    oPckt.m_baos.write(inputData, 0, nLastData);
                                    nPos += nLastData;

                                    nLastRemain = nRecvSize - nLastData;
                                    if(nLastRemain > 0) {
                                        remainData = new byte[nLastRemain];
                                        System.arraycopy(inputData, nLastData, remainData, 0, nLastRemain);
                                        Log.d(TAG, "Last remain data: " + String.valueOf(nLastRemain));
                                    }
                                } else {
                                    oPckt.m_baos.write(inputData, 0, nRecvSize);
                                    nPos += nRecvSize;
                                }
                                // endregion

                                // region Saving Image
                                if(nPos == nImageSize ){
                                    String szResponse = "";
                                    boolean bSaveImage = SaveImage(oPckt.m_baos, fullPath, oPckt._Name);
                                    Log.d(TAG, "Received for one image: " + String.valueOf(ntmp));
                                    ntmp = 0;
                                    if(bSaveImage) {
                                        szResponse = Common.ComposeResponse(oPckt._Name, Common.RES_YES);
                                        callback_msg(Common.MSGTYPE.INFO, String.valueOf(nRecvImage) +
                                                ", " + oPckt._Name +
                                                ", " + String.valueOf((nImageSize/1024)) + " KB");
                                        Log.i(TAG, "Save Image SUCCEED " + szResponse);
                                        nRecvImage++;
                                    }else {
                                        szResponse = Common.ComposeResponse(oPckt._Name, Common.RES_NO);
                                        Log.e(TAG, "Save Image FAILED " + szResponse);
                                    }
                                    // Initialize for new Image
                                    bCheckHeader = false;
                                    nPos = nImageSize = 0;
                                    oPckt = null;

                                    if (m_szCmd.compareTo(Common.COMMAND.REG) == 0) {
                                        Sending(szResponse.getBytes());
                                        if(nRecvImage == Common.REGISTER_PIC){
                                            ExecuteRegister();
                                            nRecvImage = 0;
                                        }
                                    }
                                    else if (m_szCmd.compareTo(Common.COMMAND.LOGIN) == 0) {
                                        if (bSaveImage) {
                                            if (nRecvImage == Common.LOGIN_PIC) {
                                                String szTemp = ExecuteLogin();
                                                Sending(szResponse.getBytes());
                                                Sending(szTemp.getBytes());
                                                nRecvImage = 0;
                                            }
                                        } else
                                            Sending(szResponse.getBytes());
                                    }
                                }
                                // endregion


                            }

                        } //  end else saving image
                    } // if have data to read

                }
                catch (Exception exception){
                    exception.printStackTrace();
                    Log.e(TAG, "While exception" + exception.getLocalizedMessage());
                }
            }  // end while
            Log.d(TAG, "--- End While ---");
            //-------------------------------------------------------------------------------------

            //mOutput.close();
            mInput.close();

        } catch (IOException e) {
            //report exception somewhere.
            Log.e(TAG, e.getLocalizedMessage());
        }
        finally {
            Stop();
        }
    }


    private void ExecuteRegister(){
        Log.d(TAG, "*** REGISTER ***");
        callback_msg(Common.MSGTYPE.REGISTER_DONE, "ExecuteRegister...");
        String szMsg = "";
        if(Common.m_oTraining.Training())
            szMsg = Common.ComposeResponse(Common.MSGTYPE.REGISTER_DONE, Common.RES_YES);
        else
            szMsg = Common.ComposeResponse(Common.MSGTYPE.REGISTER_DONE, Common.RES_NO);
        callback_msg(Common.MSGTYPE.REGISTER_DONE, "Sending: " + szMsg);
        Sending(szMsg.getBytes());
    }

    private String ExecuteLogin(){
        Log.d(TAG, "*** LOGIN ***");
        callback_msg(Common.MSGTYPE.REGISTER_DONE, "ExecuteLogin...");
        String szResult = Common.m_oRecog.Recognition();
        String szMsg = Common.ComposeResponse(Common.MSGTYPE.LOGIN_DONE, szResult);
        callback_msg(Common.MSGTYPE.LOGIN_DONE, "Sending: " + szMsg);
        return szMsg;
    }

    private boolean Sending(byte[] packet){
        boolean ret = false;
        try {
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
            if(clientSocket.isConnected()){
                output.write(packet);
                //Log.d(TAG, "Sending Packet ... " + String.valueOf(packet.length));
                ret = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getLocalizedMessage());
            ret = false;
        }
        return ret;
    }

    public void Stop(){
        callback_msg(Common.MSGTYPE.INFO, "****** Connection Close ******");
        this.isClosed = true;
        try {
            if(clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean isReadable(Socket socket, InputStream input) {
        if (!(input instanceof BufferedInputStream)) {
            return true; // Optimistic.
        }
        BufferedInputStream bufferedInputStream = (BufferedInputStream) input;
        try {
            int readTimeout = socket.getSoTimeout();
            try {
                socket.setSoTimeout(1);
                bufferedInputStream.mark(1);
                if (bufferedInputStream.read() == -1) {
                    return false; // Stream is exhausted; socket is closed.
                }
                bufferedInputStream.reset();
                return true;
            } finally {
                socket.setSoTimeout(readTimeout);
            }
        } catch (SocketTimeoutException ignored) {
            return true; // Read timed out; socket is good.
        } catch (IOException e) {
            return false; // Couldn't read; socket is closed.
        }
    }

    private void callback_msg(String msgtype, String szMsg){
        if(szMsg != null) {
            String szCompose = "[" + String.valueOf(m_nID) + "]" + szMsg;
            Log.d(TAG, szCompose);
            if (mHandler != null) {
                Bundle b = new Bundle();
                b.putString(Common.Hndl_MSGTYPE, msgtype);
                b.putString(Common.Hndl_MSG, szCompose);
                Message mMsg = mHandler.obtainMessage();
                mMsg.setData(b);
                mHandler.sendMessage(mMsg);
            } else {
                Log.e(TAG, "Message or Handler is NULL");
            }
        }
    }

    public boolean SaveImage(ByteArrayOutputStream baos, String fullPath, String szName){
        boolean ret = false;
        byte[] byteArray = baos.toByteArray();
        Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray,
                0,
                byteArray.length);
        OutputStream fOut = null;
        if(compressedBitmap == null) {
            Log.e(TAG, "compressedBitmap is NULL");
            return false;
        }

        Bitmap bmpCropped = Common.m_oDetect.Detection(compressedBitmap);
        if(bmpCropped == null){
            Log.e(TAG, "Image Face not Detected");
            callback_msg(Common.MSGTYPE.INFO, "Image Face not Detected");
            ret = false;
        }
        else {
            // Set Bitmap to compare
            if (m_szCmd.compareTo(Common.COMMAND.LOGIN) == 0) {
                Common.m_oRecog.setBitmap(bmpCropped);
            }

            File file = new File(fullPath, szName + ".png");
            try {
                file.createNewFile();
                fOut = new FileOutputStream(file);
                bmpCropped.compress(Bitmap.CompressFormat.PNG, 0, fOut);

                fOut.flush();
                fOut.close();
                ret = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }




}