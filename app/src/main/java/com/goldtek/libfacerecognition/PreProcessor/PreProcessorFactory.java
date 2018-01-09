/* Copyright 2016 Michael Sladoje and Mike Schälchli. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.goldtek.libfacerecognition.PreProcessor;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.goldtek.recognitionsvr.R;
import com.goldtek.libfacerecognition.Helpers.Eyes;
import com.goldtek.libfacerecognition.Helpers.FaceDetection;
import com.goldtek.libfacerecognition.Helpers.PreferencesHelper;
import com.goldtek.libfacerecognition.PreProcessor.BrightnessCorrection.GammaCorrection;
import com.goldtek.libfacerecognition.PreProcessor.Contours.DifferenceOfGaussian;
import com.goldtek.libfacerecognition.PreProcessor.Contours.LocalBinaryPattern;
import com.goldtek.libfacerecognition.PreProcessor.Contours.Masking;
import com.goldtek.libfacerecognition.PreProcessor.ContrastAdjustment.HistogrammEqualization;
import com.goldtek.libfacerecognition.PreProcessor.StandardPostprocessing.Resize;
import com.goldtek.libfacerecognition.PreProcessor.StandardPreprocessing.Crop;
import com.goldtek.libfacerecognition.PreProcessor.StandardPreprocessing.EyeAlignment;
import com.goldtek.libfacerecognition.PreProcessor.StandardPreprocessing.GrayScale;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;

public class PreProcessorFactory {
    private Context context;
    private PreferencesHelper preferencesHelper;
    private Resources resources;
    public enum PreprocessingMode {DETECTION, RECOGNITION};
    private PreProcessor preProcessorRecognition;
    private PreProcessor preProcessorDetection;
    private List<Mat> images;
    public CommandFactory commandFactory;
    private FaceDetection faceDetection;
    private boolean eyeDetectionEnabled;

    public PreProcessorFactory(Context context) {
        this.context = context;
        this.faceDetection = new FaceDetection(context);
        preferencesHelper = new PreferencesHelper(context);
        resources = context.getResources();
        eyeDetectionEnabled = preferencesHelper.getEyeDetectionEnabled();
        commandFactory = new CommandFactory();
        commandFactory.addCommand(resources.getString(R.string.grayscale), new GrayScale());
        commandFactory.addCommand(resources.getString(R.string.eyeAlignment), new EyeAlignment());
        commandFactory.addCommand(resources.getString(R.string.crop), new Crop());
        commandFactory.addCommand(resources.getString(R.string.gammaCorrection), new GammaCorrection(preferencesHelper.getGamma()));
        commandFactory.addCommand(resources.getString(R.string.doG), new DifferenceOfGaussian(preferencesHelper.getSigmas()));
        commandFactory.addCommand(resources.getString(R.string.masking), new Masking());
        commandFactory.addCommand(resources.getString(R.string.histogrammEqualization), new HistogrammEqualization());
        commandFactory.addCommand(resources.getString(R.string.resize), new Resize());
        commandFactory.addCommand(resources.getString(R.string.lbp), new LocalBinaryPattern());
    }

    public List<Mat> getCroppedImage(Mat img){
        preProcessorDetection = new PreProcessor(faceDetection, getCopiedImageList(img), context);
        List<String> preprocessingsDetection = getPreprocessings(PreferencesHelper.Usage.DETECTION);
        images = new ArrayList<Mat>();
        images.add(img);
        preProcessorRecognition = new PreProcessor(faceDetection, images, context);

        try {
            preprocess(preProcessorDetection, preprocessingsDetection);
            preProcessorRecognition.setFaces(PreprocessingMode.RECOGNITION);
            preProcessorRecognition = commandFactory.executeCommand(resources.getString(R.string.crop), preProcessorRecognition);
            if (eyeDetectionEnabled) {
                Eyes[] eyes = preProcessorRecognition.setEyes();
                if (eyes == null || eyes[0] == null){
                    return null;
                }
            }
            preProcessorRecognition.setImages(Resize.preprocessImage(preProcessorRecognition.getImages(), preferencesHelper.getFaceSize()));
        } catch (NullPointerException e){
            Log.d("getCroppedImage", "No face detected");
            return null;
        }
        return preProcessorRecognition.getImages();
    }

    public List<Mat> getProcessedImage(Mat img, PreprocessingMode preprocessingMode) throws NullPointerException {

        preProcessorDetection = new PreProcessor(faceDetection, getCopiedImageList(img), context);

        images = new ArrayList<Mat>();
        images.add(img);
        preProcessorRecognition = new PreProcessor(faceDetection, images, context);

        try {
            preprocess(preProcessorDetection, getPreprocessings(PreferencesHelper.Usage.DETECTION));

            preProcessorDetection.setFaces(preprocessingMode);
            preProcessorRecognition.setFaces(preProcessorDetection.getFaces());
            preProcessorRecognition.setAngle(preProcessorDetection.getAngle());
            preProcessorRecognition = commandFactory.executeCommand(resources.getString(R.string.crop), preProcessorRecognition);

            if (eyeDetectionEnabled) {
                Eyes[] eyes = preProcessorRecognition.setEyes();
                if (eyes == null || eyes[0] == null){
                    return null;
                }
            }

            if (preprocessingMode == PreprocessingMode.RECOGNITION){
                preprocess(preProcessorRecognition, getPreprocessings(PreferencesHelper.Usage.RECOGNITION));
            }

        } catch (NullPointerException e){
            Log.d("getProcessedImage", "No face detected");
            return null;
        }
        if (preprocessingMode == PreprocessingMode.DETECTION){
            return preProcessorDetection.getImages();
        } else {
            return preProcessorRecognition.getImages();
        }
    }

    private List<String> getPreprocessings(PreferencesHelper.Usage usage){
        ArrayList<String> preprocessings = new ArrayList<String>();
        preprocessings.addAll(preferencesHelper.getStandardPreprocessing(usage));
        preprocessings.addAll(preferencesHelper.getBrightnessPreprocessing(usage));
        preprocessings.addAll(preferencesHelper.getContoursPreprocessing(usage));
        preprocessings.addAll(preferencesHelper.getContrastPreprocessing(usage));
        preprocessings.addAll(preferencesHelper.getStandardPostprocessing(usage));
        return preprocessings;
    }

    private void preprocess(PreProcessor preProcessor, List<String> preprocessings){
        for (String name : preprocessings){
            preProcessor = commandFactory.executeCommand(name, preProcessor);
        }
    }

    public Rect[] getFacesForRecognition() {
        if(preProcessorRecognition != null){
            return preProcessorRecognition.getFaces();
        } else {
            return null;
        }
    }

    public Eyes[] getEyesForRecognition() {
        if(preProcessorRecognition != null){
            return preProcessorRecognition.getEyes();
        } else {
            return null;
        }
    }

    private List<Mat> getCopiedImageList(Mat img){
        List<Mat> images = new ArrayList<Mat>();
        Mat imgCopy = new Mat();
        img.copyTo(imgCopy);
        images.add(imgCopy);
        return images;
    }

    public int getAngleForRecognition(){
        return preProcessorRecognition.getAngle();
    }

    public void setCascadeClassifierForFaceDetector(String cascadeAssetName){
        faceDetection.setCascadeClassifierForFaceDetector(context, cascadeAssetName);
    }
}
