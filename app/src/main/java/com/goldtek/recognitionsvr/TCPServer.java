package com.goldtek.recognitionsvr;

/**
 * Created by darwinhu on 2017/12/18.
 */
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements Runnable{

    String TAG = "TCPServer";

    protected int          serverPort   = Common.PORT;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;

    private Handler mHandler;

    protected ExecutorService threadPool = Executors.newFixedThreadPool(20);

    List<CClientSocket> m_lstClient = new ArrayList<CClientSocket>();
    static int clientID = 1;

    public TCPServer(int port, Handler handler){
        this.serverPort = port;
        this.mHandler = handler;
    }

    public void run(){
        callback_msg(Common.MSGTYPE.INFO, "###### Server START ######");
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while(! isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
                clientSocket.setReceiveBufferSize(CClientSocket.BUFFSIZE);
            } catch (IOException e) {
                if(isStopped()) {
                    Log.e(TAG,"Server Stopped.");
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }

            // Maintenance List
            CheckClientList();
            // Adding to list
            CClientSocket client = new CClientSocket(clientSocket, clientID, mHandler);
            m_lstClient.add(client);
            clientID++;

            this.threadPool.execute(client);
        }
        this.threadPool.shutdown();

        callback_msg(Common.MSGTYPE.INFO, "###### Server STOP ######");
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        for(CClientSocket client : m_lstClient){
            client.Stop();
        }
        m_lstClient.clear();
        clientID = 1;

        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void CheckClientList(){
        Iterator<CClientSocket> iter = m_lstClient.iterator();
        while(iter.hasNext()){
            CClientSocket client = iter.next();
            if (client.isClosed) {
                iter.remove();
                clientID--;
            }
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 8888", e);
        }
    }


    private void callback_msg(String msgtype, String szMsg){
        if(szMsg != null) {
            Log.d(TAG, szMsg);
            if (mHandler != null) {
                Bundle b = new Bundle();
                b.putString(Common.Hndl_MSGTYPE, msgtype);
                b.putString(Common.Hndl_MSG, szMsg);
                Message mMsg = mHandler.obtainMessage();
                mMsg.setData(b);
                mHandler.sendMessage(mMsg);
            } else {
                Log.e(TAG, "Message or Handler is NULL");
            }
        }
    }


}
