package com.goldtek.recognitionsvr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.goldtek.libfacerecognition.Helpers.MatName;
import com.goldtek.libfacerecognition.Helpers.MatOperation;
import com.goldtek.libfacerecognition.Helpers.PreferencesHelper;
import com.goldtek.libfacerecognition.PreProcessor.PreProcessorFactory;
import com.goldtek.libfacerecognition.Recognition.Recognition;
import com.goldtek.libfacerecognition.Recognition.RecognitionFactory;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.io.File;
import java.util.List;

/**
 * Created by darwinhu on 2018/1/2.
 */

public class CDetection {
    private static final String TAG = "CDetection";

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    private Context m_contx = null;
    protected boolean m_bRunning = false;

    private PreProcessorFactory m_ppF = null;

    public CDetection(Context m_contx) {
        this.m_contx = m_contx;
        m_bRunning = false;

        m_ppF = new PreProcessorFactory(m_contx);

    }

    public synchronized Bitmap Detection(Bitmap bmp) {
        Bitmap bmpCrop = null;
        if(m_ppF == null) {
            Log.d(TAG, "*** Initialize ERROR ***");
            return null;
        }

        m_bRunning = true;

        Mat imgCopy = new Mat();
        Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, imgCopy);

        // Check that only 1 face is found. Skip if any or more than 1 are found.
        List<Mat> images = m_ppF.getCroppedImage(imgCopy);
        if (images != null && images.size() == 1){
            Mat matCrop = images.get(0);
            if(matCrop != null){
                Rect[] faces = m_ppF.getFacesForRecognition();
                //Only proceed if 1 face has been detected, ignore if 0 or more than 1 face have been detected
                if((faces != null) && (faces.length == 1)){
                    bmpCrop = Bitmap.createBitmap(matCrop.cols(), matCrop.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(matCrop, bmpCrop);
                }
            }
        }

        m_bRunning = false;
        return bmpCrop;
    }

    private synchronized boolean isRunning() {
        return this.m_bRunning;
    }


}
