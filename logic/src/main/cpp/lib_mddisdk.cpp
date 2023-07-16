#include <jni.h>
#include <string>
#include "android/bitmap.h"
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/types_c.h>


using namespace cv;
using namespace std;


void bitmapToMat(JNIEnv *env, jobject bitmap, Mat &dst, jboolean needUnPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void *pixels = 0;

    try {
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                  info.format == ANDROID_BITMAP_FORMAT_RGB_565);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        dst.create(info.height, info.width, CV_8UC4);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if (needUnPremultiplyAlpha) cvtColor(tmp, dst, COLOR_mRGBA2RGBA);
            else tmp.copyTo(dst);
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, dst, COLOR_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception &e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return;
    }
}

void matToBitmap(JNIEnv *env, Mat src, jobject bitmap, jboolean needPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void *pixels = 0;

    try {
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                  info.format == ANDROID_BITMAP_FORMAT_RGB_565);
        CV_Assert(src.dims == 2 && info.height == (uint32_t) src.rows &&
                  info.width == (uint32_t) src.cols);
        CV_Assert(src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if (src.type() == CV_8UC1) {
                cvtColor(src, tmp, COLOR_GRAY2RGBA);
            } else if (src.type() == CV_8UC3) {
                cvtColor(src, tmp, COLOR_RGB2RGBA);
            } else if (src.type() == CV_8UC4) {
                if (needPremultiplyAlpha) cvtColor(src, tmp, COLOR_RGBA2mRGBA);
                else src.copyTo(tmp);
            }
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if (src.type() == CV_8UC1) {
                cvtColor(src, tmp, COLOR_GRAY2BGR565);
            } else if (src.type() == CV_8UC3) {
                cvtColor(src, tmp, COLOR_RGB2BGR565);
            } else if (src.type() == CV_8UC4) {
                cvtColor(src, tmp, COLOR_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception &e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return;
    }
}


extern "C" JNIEXPORT void JNICALL
Java_com_dynamicelement_sdk_android_mddiclient_MddiParameters_cvtGrayscaledBitmap(JNIEnv *env, jclass p_this,
                                                      jobject bitmapIn, jobject bitmapOut) {
    Mat src;
    Mat dst;
    bitmapToMat(env, bitmapIn, src, false);
    cvtColor(src, dst, COLOR_BGR2GRAY);
    matToBitmap(env, dst, bitmapOut, false);
}

extern "C" JNIEXPORT void JNICALL
Java_com_dynamicelement_sdk_android_mddiclient_MddiParameters_cvtResizedBitmap(JNIEnv *env, jclass p_this,
                                                   jobject bitmapIn, jobject bitmapOut) {
    Mat src;
    Mat out;
    Mat dst;
    bitmapToMat(env, bitmapOut, out, false);
    bitmapToMat(env, bitmapIn, src, false);
    resize(src, dst, Size(out.size().width, out.size().height), 0, 0, INTER_CUBIC);
    matToBitmap(env, dst, bitmapOut, false);
}


extern "C" JNIEXPORT void JNICALL
Java_com_dynamicelement_sdk_android_mddiclient_MddiParameters_cvtResizedGrayscaledBitmap(JNIEnv *env, jclass p_this,
                                                             jobject bitmapIn,
                                                             jobject bitmapOut) {
    Mat src;
    Mat out;
    Mat dst;
    bitmapToMat(env, bitmapOut, out, false);
    bitmapToMat(env, bitmapIn, src, false);
    cvtColor(src, src, COLOR_BGR2GRAY);
    resize(src, dst, Size(out.size().width, out.size().height), 0, 0, INTER_CUBIC);
    matToBitmap(env, dst, bitmapOut, false);
}


extern "C" JNIEXPORT void JNICALL
Java_com_dynamicelement_sdk_android_mddiclient_MddiParameters_cvtResizedGrayscaledBitmapFromFile(JNIEnv *env,
                                                                     jclass p_this,
                                                                     jstring filePath,
                                                                     jobject bitmapOut) {
    Mat src;
    Mat out;
    Mat dst;
    bitmapToMat(env, bitmapOut, out, false);
    const char *path = env->GetStringUTFChars(filePath, 0);
    src = imread(path, IMREAD_GRAYSCALE);
    resize(src, dst, Size(out.size().width, out.size().height), 0, 0, INTER_CUBIC);
    matToBitmap(env, dst, bitmapOut, false);
}



extern "C" JNIEXPORT void JNICALL
Java_com_dynamicelement_sdk_android_mddiclient_MddiParameters_cvtBarcodeBitmap1(JNIEnv *env, jclass p_this,
                                                    jobject bitmapIn, jobject bitmapOut,
                                                    jfloat sigma) {
    Mat src;
    Mat out;
    Mat dst;
    bitmapToMat(env, bitmapIn, src, false);
    bitmapToMat(env, bitmapOut, out, false);
    cvtColor(src, src, COLOR_BGR2GRAY);
    threshold(src, src, 0, 255.0, THRESH_BINARY + THRESH_OTSU);
    resize(src, src, Size(out.size().width, out.size().height), 0, 0, INTER_CUBIC);
    GaussianBlur(src, dst, Size(5, 5), sigma);
    matToBitmap(env, dst, bitmapOut, false);
}

extern "C" JNIEXPORT void JNICALL
Java_com_dynamicelement_sdk_android_mddiclient_MddiParameters_cvtBarcodeBitmap2(JNIEnv *env, jclass p_this,
                                                    jobject bitmapIn, jobject bitmapOut,
                                                    jfloat sigma) {
    Mat src;
    Mat out;
    Mat dst;
    Mat element = getStructuringElement(MORPH_ELLIPSE, Size(3, 3));
    bitmapToMat(env, bitmapOut, out, false);
    bitmapToMat(env, bitmapIn, src, false);
    cvtColor(src, src, COLOR_BGR2GRAY);
    erode(src, src, getStructuringElement(MORPH_ELLIPSE, Size(3, 3)), Point(-1, -1), 1);
    morphologyEx(src, src, MORPH_CLOSE, element);
    threshold(src, src, 0, 255.0, THRESH_BINARY + THRESH_OTSU);
    dilate(src, src, getStructuringElement(MORPH_ELLIPSE, Size(3, 3)), Point(-1, -1), 1);
    GaussianBlur(src, src, Size(), sigma);
    resize(src, dst, Size(out.size().width, out.size().height), 0, 0, INTER_CUBIC);
    matToBitmap(env, dst, bitmapOut, false);
}


extern "C" JNIEXPORT double JNICALL
Java_com_dynamicelement_sdk_android_mddiclient_MddiParameters_getImageVariance(JNIEnv *env, jclass p_this,
                                                jobject bitmapIn, jobject bitmapOut) {
    Mat src;
    Mat out;
    Mat dst;
    Mat laplacian;
    Scalar mean, stdDev;
    Mat euc1;
    bitmapToMat(env, bitmapOut, out, false);
    bitmapToMat(env, bitmapIn, src, false);
    cvtColor(src, src, COLOR_BGR2GRAY);
    resize(src, src, Size(out.size().width, out.size().height), 0, 0, INTER_CUBIC);
    src.convertTo(laplacian, CV_8UC1);
    Laplacian(src, laplacian, CV_8U);
    matToBitmap(env, laplacian, bitmapOut, false);
    meanStdDev(laplacian, mean, stdDev, cv::Mat());
    return stdDev.val[0] * stdDev.val[0];
}