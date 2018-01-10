package com.goldtek.recognitionsvr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.goldtek.libfacerecognition.Helpers.FileHelper;
import com.goldtek.libfacerecognition.Helpers.MatOperation;
import com.goldtek.libfacerecognition.Helpers.PreferencesHelper;
import com.goldtek.libfacerecognition.PreProcessor.PreProcessorFactory;
import com.goldtek.libfacerecognition.Recognition.Recognition;
import com.goldtek.libfacerecognition.Recognition.RecognitionFactory;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.List;
import org.opencv.android.Utils;
import org.opencv.imgproc.Imgproc;

/**
 * Created by darwinhu on 2018/1/2.
 */

public class CRecognition {
    private static final String TAG = "CRecognition";

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    private Context m_contx = null;
    protected boolean m_bRunning = false;

    private PreProcessorFactory m_ppF = null;
    private PreferencesHelper m_preferencesHelper = null;
    private String m_algorithm = "";
    private Recognition m_rec;

    private Bitmap m_bmp = null;
    FileHelper m_fileHelper = new FileHelper();

    public CRecognition(Context m_contx) {
        this.m_contx = m_contx;
        m_bRunning = false;

        m_ppF = new PreProcessorFactory(m_contx);
        m_preferencesHelper = new PreferencesHelper(m_contx);
        m_algorithm = m_preferencesHelper.getClassificationMethod();
    }

    public synchronized void setBitmap(Bitmap bmp){
        this.m_bmp = bmp;
    }

    public synchronized String Recognition() {

        if(m_ppF == null || m_bmp == null || !Common.FolderExist(m_fileHelper.SVM_PATH)) {
            Log.d(TAG, "*** Initialize ERROR ***");
            return Common.RES_UNKNOWN;
        }

        m_bRunning = true;
        Log.d(TAG, "*** Recognition START ***");
        String szRet = Common.RES_UNKNOWN;

        Mat img = new Mat();
        Bitmap bmp32 = m_bmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, img);
        //terry, fix for M7
        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGRA2GRAY);

        List<Mat> images = m_ppF.getProcessedImage(img, PreProcessorFactory.PreprocessingMode.RECOGNITION);
        Rect[] faces = m_ppF.getFacesForRecognition();
        if(images == null || images.size() == 0 || faces == null || faces.length == 0
                || ! (images.size() == faces.length)){
            // skip
            return Common.RES_UNKNOWN;
        } else {
            faces = MatOperation.rotateFaces(img, faces, m_ppF.getAngleForRecognition());
            String label = "";
            for(int i = 0; i<faces.length; i++){
                m_rec = RecognitionFactory.getRecognitionAlgorithm(m_contx, Recognition.RECOGNITION, m_algorithm);
                label = m_rec.recognize(images.get(i), "");
                Log.i(TAG, "Label: " + label);
            }
            szRet = label;
        }

        m_bRunning = false;
        Log.d(TAG, "*** Recognition STOP ***");
        return szRet;
    }

    private synchronized boolean isRunning() {
        return this.m_bRunning;
    }



}
