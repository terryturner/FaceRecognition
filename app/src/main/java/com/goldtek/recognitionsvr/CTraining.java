package com.goldtek.recognitionsvr;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.goldtek.libfacerecognition.Helpers.FileHelper;
import com.goldtek.libfacerecognition.Helpers.MatName;
import com.goldtek.libfacerecognition.Helpers.PreferencesHelper;
import com.goldtek.libfacerecognition.PreProcessor.PreProcessorFactory;
import com.goldtek.libfacerecognition.Recognition.Recognition;
import com.goldtek.libfacerecognition.Recognition.RecognitionFactory;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.net.Socket;
import java.util.List;

/**
 * Created by darwinhu on 2017/12/28.
 */

//public class CTraining implements Runnable{
public class CTraining {
    private static final String TAG = "CTraining";

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    private Handler mHandler = null;
    private Context m_contx = null;

    protected boolean m_bRunning = false;

    private PreProcessorFactory m_ppF = null;
    private PreferencesHelper m_preferencesHelper = null;
    private String m_algorithm = "";

    public CTraining(Context m_contx) {
        this.m_contx = m_contx;
        m_bRunning = false;

        m_ppF = new PreProcessorFactory(m_contx);
        m_preferencesHelper = new PreferencesHelper(m_contx);
        m_algorithm = m_preferencesHelper.getClassificationMethod();

    }

    public synchronized boolean Training() {
        if(m_ppF == null) {
            Log.d(TAG, "*** Initialize ERROR ***");
            return false;
        }

        m_bRunning = true;
        Log.d(TAG, "*** Training START ***");

        boolean ret = false;

        Log.i(TAG, "[Algorithm] " + m_algorithm);
        FileHelper fileHelper = new FileHelper();
        fileHelper.createDataFolderIfNotExsiting();
        final File[] persons = fileHelper.getTrainingList();
        if (persons.length > 0) {
            Recognition rec = RecognitionFactory.getRecognitionAlgorithm(m_contx, Recognition.TRAINING, m_algorithm);
            for (File person : persons) {
                if (person.isDirectory()) {
                    File[] files = person.listFiles();
                    int counter = 1;
                    for (File file : files) {
                        if (FileHelper.isFileAnImage(file)) {
                            Mat imgRgb = Imgcodecs.imread(file.getAbsolutePath());
                            Imgproc.cvtColor(imgRgb, imgRgb, Imgproc.COLOR_BGRA2RGBA);
                            Mat processedImage = new Mat();
                            imgRgb.copyTo(processedImage);
                            List<Mat> images = m_ppF.getProcessedImage(processedImage, PreProcessorFactory.PreprocessingMode.RECOGNITION);
                            if (images == null || images.size() > 1) {
                                // More than 1 face detected --> cannot use this file for training
                                continue;
                            } else {
                                processedImage = images.get(0);
                            }
                            if (processedImage.empty()) {
                                continue;
                            }
                            // The last token is the name --> Folder name = Person name
                            String[] tokens = file.getParent().split("/");
                            final String name = tokens[tokens.length - 1];

                            MatName m = new MatName("processedImage", processedImage);
                            fileHelper.saveMatToImage(m, FileHelper.DATA_PATH);

                            rec.addImage(processedImage, name, false);

//                                      fileHelper.saveCroppedImage(imgRgb, ppF, file, name, counter);

                            // Update screen to show the progress
                            final int counterPost = counter;
                            final int filesLength = files.length;
                            Log.d(TAG, "Image " + counterPost + " of " + filesLength + " from " + name + " imported.");
                            counter++;
                        }
                    }
                }
            }

            if (rec.train()) {
                Log.i(TAG, "Training successful");
                ret = true;
            } else {
                Log.i(TAG, "Training failed");
                ret = false;
            }

        }
        m_bRunning = false;
        Log.d(TAG, "*** Training STOP ***");
        return ret;

    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    private synchronized boolean isRunning() {
        return this.m_bRunning;
    }

//    public synchronized void stop(){
//        Log.d(TAG, "Training Stop");
//        mHandler.removeCallbacks(this);
//    }

//    @Override
//    public void run() {
//        synchronized (mRunLock) {
//            m_bRunning = true;
//            Log.d(TAG, "*** Training Start ***");
//        }
//
//        PreProcessorFactory ppF = new PreProcessorFactory(m_contx);
//        PreferencesHelper preferencesHelper = new PreferencesHelper(m_contx);
//        String algorithm = preferencesHelper.getClassificationMethod();
//        Log.i(TAG, "[Algorithm] " + algorithm);
//        FileHelper fileHelper = new FileHelper();
//        fileHelper.createDataFolderIfNotExsiting();
//        final File[] persons = fileHelper.getTrainingList();
//        if (persons.length > 0) {
//            Recognition rec = RecognitionFactory.getRecognitionAlgorithm(m_contx, Recognition.TRAINING, algorithm);
//            for (File person : persons) {
//                if (person.isDirectory()){
//                    File[] files = person.listFiles();
//                    int counter = 1;
//                    for (File file : files) {
//                        if (FileHelper.isFileAnImage(file)){
//                            Mat imgRgb = Imgcodecs.imread(file.getAbsolutePath());
//                            Imgproc.cvtColor(imgRgb, imgRgb, Imgproc.COLOR_BGRA2RGBA);
//                            Mat processedImage = new Mat();
//                            imgRgb.copyTo(processedImage);
//                            List<Mat> images = ppF.getProcessedImage(processedImage, PreProcessorFactory.PreprocessingMode.RECOGNITION);
//                            if (images == null || images.size() > 1) {
//                                // More than 1 face detected --> cannot use this file for training
//                                continue;
//                            } else {
//                                processedImage = images.get(0);
//                            }
//                            if (processedImage.empty()) {
//                                continue;
//                            }
//                            // The last token is the name --> Folder name = Person name
//                            String[] tokens = file.getParent().split("/");
//                            final String name = tokens[tokens.length - 1];
//
//                            MatName m = new MatName("processedImage", processedImage);
//                            fileHelper.saveMatToImage(m, FileHelper.DATA_PATH);
//
//                            rec.addImage(processedImage, name, false);
//
////                                      fileHelper.saveCroppedImage(imgRgb, ppF, file, name, counter);
//
//                            // Update screen to show the progress
//                            final int counterPost = counter;
//                            final int filesLength = files.length;
//                            Log.d(TAG, "Image " + counterPost + " of " + filesLength + " from " + name + " imported.");
//                            counter++;
//                        }
//                    }
//                }
//            }
//
//            if (rec.train()) {
//                Log.i(TAG, "Training successful");
//            } else {
//                Log.i(TAG,  "Training failed");
//            }
//
//        } else {
//            Thread.currentThread().interrupt();
//        }
//
//        synchronized (mRunLock) {
//            m_bRunning = false;
//            Log.d(TAG, "*** Training STOP ***");
//        }
//    }


}
