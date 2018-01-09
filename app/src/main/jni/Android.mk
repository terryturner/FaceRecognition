LOCAL_PATH:=$(call my-dir)

#Module jnilibsvm

include $(CLEAR_VARS)

LOCAL_MODULE	:= jnilibsvm
LOCAL_CFLAGS    := -DDEV_NDK=1
LOCAL_SRC_FILES := \
	common.cpp jnilibsvm.cpp \
	libsvm/svm-train.cpp \
	libsvm/svm-predict.cpp \
	libsvm/svm.cpp

LOCAL_LDLIBS	+= -llog -ldl

include $(BUILD_SHARED_LIBRARY)

#include $(CLEAR_VARS)
#LOCAL_MODULE := caffe
#LOCAL_SRC_FILES := libcaffe.so
#include $(PREBUILT_SHARED_LIBRARY)

#include $(CLEAR_VARS)
#LOCAL_MODULE := caffe_jni
#LOCAL_SRC_FILES := libcaffe_jni.so
#include $(PREBUILT_SHARED_LIBRARY)