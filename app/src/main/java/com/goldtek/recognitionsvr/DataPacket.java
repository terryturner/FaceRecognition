package com.goldtek.recognitionsvr;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Created by darwinhu on 2017/12/20.
 */

public class DataPacket {

    private String TAG = "DataPacket";
    //INFO
    public final static String INFO_START = "<Info>";
    public final static String INFO_END = "</Info>";

    //HEADER
    public final static String HEADER_START = "<GOLDTEK>";
    public final static String HEADER_END = "</GOLDTEK>";

    // <GOLDTEK><id></id><name></name><size></size></GOLDTEK>
    public final static String TAG_ID = "id";
    public final static String TAG_NAME = "name";
    public final static String TAG_SIZE = "size";

    public String _Name = "";
    public String _ID = "";
    public String _Size = "";

    public final static int HEADERSIZE = 512;

    public byte[] m_Header = new byte[HEADERSIZE];
    public ByteArrayOutputStream m_baos = null;
    private byte[] m_data = null;

    public DataPacket(){
        m_baos = new ByteArrayOutputStream();
    }

    /**
     * For Composing Packet
     * @param szName
     * @param bmp
     */
    public DataPacket(int nID, String szName, Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 0, baos);
        byte[] byteArray = baos.toByteArray();
        int sizeImage = byteArray.length;

        String szHEADER = ComposeHeader(szName, nID, sizeImage);

        System.arraycopy(szHEADER.getBytes(), 0, m_Header, 0, szHEADER.getBytes().length);
        Log.d(TAG, "ImageSize: " + String.valueOf(sizeImage) + "HeaderSize: " + szHEADER.getBytes().length);

        // [DATA] Protocol [HEADER][IMAGE...]
        int TOTAL = HEADERSIZE + sizeImage;
        m_data = new byte[TOTAL];
        System.arraycopy(m_Header, 0, m_data, 0, HEADERSIZE);
        System.arraycopy(byteArray, 0, m_data, HEADERSIZE, sizeImage);
    }

    public static String ComposeHeader(String szName, int nID, int nImageSize){
        return "<GOLDTEK><size>" + String.valueOf(nImageSize) + "</size>" +
                "<id>"+ String.valueOf(nID) + "</id>" +
                "<name>" + String.valueOf(szName) + "</name></GOLDTEK>";
    }

    public byte[] getM_data(){
        return m_data;
    }

}
