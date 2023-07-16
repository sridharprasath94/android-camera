package com.dynamicelement.sdk.android.mddiclient;

import static com.dynamicelement.sdk.android.mddiclient.MddiVariables.MIN_HEIGHT;
import static com.dynamicelement.sdk.android.mddiclient.MddiVariables.MIN_WIDTH;
import static com.dynamicelement.sdk.android.misc.InstanceType.IVF;
import static com.dynamicelement.sdk.android.misc.MddiConstants.VERSION_ID;
import static java.lang.String.format;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;

import com.dynamicelement.mddi.CollectionId;
import com.dynamicelement.mddi.Image;
import com.dynamicelement.mddi.RequestCollection;
import com.dynamicelement.mddi.RequestTenantCollection;
import com.dynamicelement.mddi.StreamImage;
import com.dynamicelement.mddi.TenantCollectionId;
import com.dynamicelement.sdk.android.collection.CollectionInfo;
import com.dynamicelement.sdk.android.misc.InstanceType;

import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;

/**
 * MDDI parameters
 */
public class MddiParameters {

    /**
     * Converts the input bitmap to grayscaled bitmap.
     *
     * @param bitmapIn  is the input bitmap.
     * @param bitmapOut is the output bitmap.
     */
    private static native void cvtGrayscaledBitmap(Bitmap bitmapIn, Bitmap bitmapOut);

    /**
     * Converts the input bitmap to resized bitmap.
     *
     * @param bitmapIn  is the input bitmap.
     * @param bitmapOut is the output bitmap.
     */
    private static native void cvtResizedBitmap(Bitmap bitmapIn, Bitmap bitmapOut);

    /**
     * Converts the input bitmap to resized and grayscaled bitmap.
     *
     * @param bitmapIn  is the input bitmap.
     * @param bitmapOut is the output bitmap.
     */
    private static native void cvtResizedGrayscaledBitmap(Bitmap bitmapIn, Bitmap bitmapOut);

    /**
     * Converts the image file to resized and grayscaled bitmap.
     *
     * @param filePath  is the file path for the image.(in .jpg,.png,.bmp formats).
     * @param bitmapOut is the output bitmap.
     */
    private static native void cvtResizedGrayscaledBitmapFromFile(String filePath,
                                                                  Bitmap bitmapOut);

    /**
     * Converts the input bitmap to barcode bitmap.(Method 1)
     *
     * @param bitmapIn  is the input bitmap.
     * @param bitmapOut is the output bitmap.
     * @param sigma     is the blur value of the output bitmap.
     */
    private static native void cvtBarcodeBitmap1(Bitmap bitmapIn, Bitmap bitmapOut, float sigma);

    /**
     * Converts the input bitmap to barcode bitmap.(Method 2)
     *
     * @param bitmapIn  is the input bitmap.
     * @param bitmapOut is the output bitmap.
     * @param sigma     is the blur value of the output bitmap.
     */
    private static native void cvtBarcodeBitmap2(Bitmap bitmapIn, Bitmap bitmapOut, float sigma);

    /**
     * Gets the image variance value from input bitmap.
     *
     * @param bitmapIn  is the input bitmap.
     * @param bitmapOut is the output bitmap.
     * @return Image Variance.
     */
    private static native double getImageVariance(Bitmap bitmapIn, Bitmap bitmapOut);

    /**
     * Build the Collection ID for ping, get sample API.
     *
     * @param cid         is the cid of the MDDI request.
     * @param sampleImage is set as 'true' if sample image is needed in the MDDI response.
     *                    Otherwise 'false'.
     * @param otherInfo   is set as 'true' if other info related to the cid is needed in the MDDI
     *                    response. Otherwise 'false'.
     * @return CollectionId for ping, get sample API.
     */
    public static CollectionId buildCollectionID(String cid,
                                                 boolean sampleImage,
                                                 boolean otherInfo,
                                                 String tenantId) {
        requireNonNull(cid);
        return CollectionId.newBuilder().setCid(cid).setTimestamp(Long.toString(System.currentTimeMillis())).
                setSampleImgFlag(sampleImage).setSampleImgPropertiesFlag(true).setSampleImgPixelsFlag(sampleImage).
                setVersionId(VERSION_ID).setOwnerAddressFlag(otherInfo).
                setOwnerIdFlag(otherInfo).setOwnerNameFlag(otherInfo).setOwnerOtherInformationFlag(otherInfo)
                .setTenantId(tenantId)
                .build();
    }

    /**
     * Build the Tenant Collection ID for ping, get sample API for default EC2 instance( with sample image).
     *
     * @param cid         is the cid of the MDDI request.
     * @param sampleImage is set as 'true' if sample image is needed in the MDDI response.
     *                    Otherwise 'false'.
     * @param otherInfo   is set as 'true' if other info related to the cid is needed in the MDDI
     *                    response. Otherwise 'false'.
     * @return TenantCollectionId for ping, get sample API.
     */
    public static TenantCollectionId buildCollectionIDDefaultEC2(String cid,
                                                                 boolean sampleImage,
                                                                 boolean otherInfo,
                                                                 String tenantId) {
        requireNonNull(cid);
        return TenantCollectionId.newBuilder().setCid(cid).setTimestamp(Long.toString(System.currentTimeMillis())).
                setSampleImgFlag(sampleImage).setSampleImgPropertiesFlag(true).setSampleImgPixelsFlag(sampleImage).
                setVersionId(VERSION_ID).setOwnerAddressFlag(otherInfo).
                setOwnerIdFlag(otherInfo).setOwnerNameFlag(otherInfo)
                .setTenantId(tenantId)
                .build();
    }

    /**
     * Build the Collection Request for Create Collection API.
     *
     * @param collectionInfo is the collection info of the MDDI request needed to create the
     *                       collection.
     * @param cid            is the cid of the MDDI request.
     * @param sampleImage    is the sample image of the MDDI request needed to create the
     *                       collection.
     * @return RequestCollection for create collection API.
     */
    public static RequestCollection buildCollectionRequest(CollectionInfo collectionInfo,
                                                           String cid,
                                                           Image sampleImage,
                                                           String tenantId) {
        requireNonNull(collectionInfo, cid, sampleImage);
        return RequestCollection.newBuilder().setImage(sampleImage).setTimestamp(Long.toString(System.currentTimeMillis())).
                setVersionId(VERSION_ID).setCollectionName(collectionInfo.getName()).setCollectionVersionId(collectionInfo.getVersionId()).
                setCollectionOwnerId(collectionInfo.getOwnerId()).setCollectionDescriptionShort(collectionInfo.getShortDescription()).
                setCollectionDescriptionLong(collectionInfo.getLongDescription()).setCid(cid).setTenantId(tenantId).build();
    }

    /**
     * Build the Collection Request for Create Collection API for default EC2 instance.
     *
     * @param collectionInfo is the collection info of the MDDI request needed to create the
     *                       collection.
     * @param cid            is the cid of the MDDI request.
     * @param sampleImage    is the sample image of the MDDI request needed to create the
     *                       collection.
     * @return RequestCollection for create collection API.
     */
    public static RequestTenantCollection buildCollectionRequestDefaultEC2(CollectionInfo collectionInfo,
                                                                           String cid,
                                                                           Image sampleImage,
                                                                           String tenantId) {
        requireNonNull(collectionInfo, cid, sampleImage);
        return RequestTenantCollection.newBuilder().setImage(sampleImage).setTimestamp(Long.toString(System.currentTimeMillis())).
                setVersionId(VERSION_ID).setCollectionName(collectionInfo.getName()).setCollectionVersionId(collectionInfo.getVersionId()).
                setCollectionOwnerId(collectionInfo.getOwnerId()).setTenantId(tenantId).setCid(cid).build();
    }

    /**
     * Build bitmap from the MDDI Image.
     *
     * @param image  is the MDDI image received from the MDDI backend.
     * @param width  is the width of the output bitmap.
     * @param height is the height of the output bitmap.
     * @param config is the configuration of the output bitmap.
     * @return Bitmap.
     */
    public static Bitmap buildBitmapFromMddiImage(Image image,
                                                  int width,
                                                  int height,
                                                  Bitmap.Config config) {
        requireNonNull(image, config);
        // Convert list<Integer> of pixels to integer array of pixels
        int[] pixelsIntArray =
                image.getImagePixelsList().stream().mapToInt(Integer::intValue).toArray();
        return buildBitmapFromIntArray(pixelsIntArray, width, height, config);
    }

    /**
     * Build the bitmap from the given pixels with given width and height.
     *
     * @param pixelsList is the list of pixels(List<Integer>).
     * @param width      is the width of the output bitmap.
     * @param height     is the height of the output bitmap.
     * @param config     is the configuration of the output bitmap.
     * @return Bitmap.
     */
    public static Bitmap buildBitmapFromIntegerList(List<Integer> pixelsList,
                                                    int width,
                                                    int height,
                                                    Bitmap.Config config) {
        requireNonNull(pixelsList, config);
        // Integer array for storing the received pixels
        int[] pixelsIntArray = toIntArray(pixelsList);
        return buildBitmapFromIntArray(pixelsIntArray, width, height, config);
    }

    /**
     * Build the bitmap from the given pixels with given width and height.
     *
     * @param pixelsIntArray is the list of pixels(nt[])
     * @param width          is the width of the output bitmap.
     * @param height         is the height of the output bitmap.
     * @param config         is the configuration of the output bitmap.
     * @return Bitmap.
     */
    private static Bitmap buildBitmapFromIntArray(int[] pixelsIntArray,
                                                  int width,
                                                  int height,
                                                  Bitmap.Config config) {
        requireNonNull(config);
        Bitmap newBitmap;
        // Initialise the bitmap with corresponding configuration,width and height
        newBitmap = Bitmap.createBitmap(width, height, config);
        // Set the integer array of pixels to construct this bitmap
        newBitmap.setPixels(pixelsIntArray, 0, width, 0, 0, width, height);
        // For grayscale image
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int colorPixel = newBitmap.getPixel(x, y);
                   /*Getting a single intensity value(for each pixel) from the server..But our
                   default colour format is
                    expecting the pixels in a format [Red,Green,Blue]. By default, it takes this
                    single value and assign into the
                     blue pixel[0,0,Blue]... For producing a black and white image, we want also
                     Red and Green pixels...So we take
                     that blue pixel and assign into the R and G of the image.

                     For example, if we get a value of 155 for a single pixel, by default is is
                     putting the pixels as [0,0,155]..So we
                     are converting into [155,155,155] format so that we get a correct grayscaled
                      image.*/
                int R = Color.blue(colorPixel);
                int G = Color.blue(colorPixel);
                int B = Color.blue(colorPixel);
                // Replace the colour from the given colour to the target colour.
                newBitmap.setPixel(x, y, Color.rgb(R, G, B));
            }
        }
        // Returns the grayscale bitmap.
        return newBitmap;
    }

    /**
     * Convert List<Integer> to int[].
     *
     * @param list is the list of pixel values.
     * @return int[].
     */
    private static int[] toIntArray(List<Integer> list) {
        requireNonNull(list);
        int[] ret = new int[list.size()];
        for (int i = 0; i < ret.length; i++)
            ret[i] = list.get(i);
        return ret;
    }

    /**
     * Get every 3rd pixel and create a new List<Integer>.
     *
     * @param pixelsList is the list of pixel values.
     * @return Revised List<Integer>.
     */
    public static List<Integer> getRevisedList(List<Integer> pixelsList) {
        List<Integer> pixelsListRevised = new ArrayList<>();
        for (int i = 0; i < pixelsList.size(); i = i + 3) {
            pixelsListRevised.add(pixelsList.get(i));
        }
        pixelsList = pixelsListRevised;
        return pixelsList;
    }

    /**
     * Create resized bitmap from a given bitmap.
     *
     * @param bitmap is the input bitmap.
     * @param width  is the width of the output bitmap.
     * @param height is the height of the output bitmap.
     * @param config is the configuration of the output bitmap.
     * @return Resized Bitmap.
     */
    public static Bitmap createResizedBitmap(Bitmap bitmap,
                                             int width,
                                             int height,
                                             Bitmap.Config config) {

        requireNonNull(bitmap);
        System.loadLibrary("lib_mddisdk");
        Bitmap resized = Bitmap.createBitmap(width, height, config);
        cvtResizedGrayscaledBitmap(bitmap, resized);
        return resized;
    }

    /**
     * Create resized bitmap from a image file.
     *
     * @param filePath is the file path of the image(in .jpg,.png,.bmp formats).
     * @param width    is the width of the output bitmap.
     * @param height   is the height of the output bitmap.
     * @param config   is the configuration of the output bitmap.
     * @return Resized Bitmap.
     */
    public static Bitmap createResizedBitmapFromFile(String filePath,
                                                     int width,
                                                     int height,
                                                     Bitmap.Config config) {

        System.loadLibrary("lib_mddisdk");
        Bitmap resized = Bitmap.createBitmap(width, height, config);
        cvtResizedGrayscaledBitmapFromFile(filePath, resized);
        return resized;
    }

    /**
     * Find the sharpness of the image using laplacian calculation from openCV
     *
     * @param bitmap is the input bitmap.
     * @param width  is the width of the output bitmap.
     * @param height is the height of the output bitmap.
     * @param config is the configuration of the output bitmap.
     * @return Image Variance(Sharpness value).
     */
    public static double getImageVariance(Bitmap bitmap,
                                          int width,
                                          int height,
                                          Bitmap.Config config) {

        requireNonNull(bitmap);
        System.loadLibrary("lib_mddisdk");
        Bitmap resized = Bitmap.createBitmap(width, height, config);
        return getImageVariance(bitmap, resized);
    }

    /**
     * Get the bytes from the bitmap.
     *
     * @param bitmap is the input bitmap.
     * @return byte[].
     */
    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        requireNonNull(bitmap);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * Get the bytes from the scaled bitmap.
     *
     * @param bitmap is the input bitmap.
     * @param width  is the width of the output bitmap.
     * @param height is the height of the output bitmap.
     * @return byte[].
     */
    public static byte[] getScaledBytesFromBitmap(Bitmap bitmap, int width, int height) {
        requireNonNull(bitmap);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        return getBytesFromBitmap(scaledBitmap);
    }

    /**
     * Crop the center of the source bitmap with given width and height.
     *
     * @param bitmap is the input bitmap.
     * @param width  is the width of the output bitmap.
     * @param height is the height of the output bitmap.
     * @return Center cropped bitmap.
     */
    public static Bitmap centerCropBitmap(Bitmap bitmap, int width, int height) {
        requireNonNull(bitmap);
        Matrix matrix = new Matrix();
        // Scaling factor of 1
        matrix.postScale(1f, 1f);
        // Crop the bitmap only if the source bitmap is bigger than the new bitmap
        if (bitmap.getWidth() >= width && bitmap.getHeight() >= height) {
            // Starting point of new bitmap in X-axis
            int xStart = (bitmap.getWidth() - width) / 2;
            // Starting point of new bitmap in Y axis
            int yStart = (bitmap.getHeight() - height) / 2;
            // Return the new bitmap
            return Bitmap.createBitmap(bitmap, xStart, yStart, width, height, matrix, true);
        }
        // Otherwise return the original bitmap itself
        else {
            return bitmap;
        }
    }

    /**
     * Build the barcode bitmap from the given bytes and the blur value
     */
    protected static Bitmap buildBarcodeBitmapFromBytes(byte[] bytes, int blurValue) {
        System.loadLibrary("lib_mddisdk");
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int resizeWidth;
        int resizeHeight;

        if (bitmap.getWidth() > 1000 && bitmap.getHeight() > 1000) {
            resizeWidth = bitmap.getWidth() / 4;
            resizeHeight = bitmap.getHeight() / 4;
        } else {
            resizeWidth = bitmap.getWidth() / 2;
            resizeHeight = bitmap.getHeight() / 2;
        }

        Bitmap barcodeBitmap = Bitmap.createBitmap(resizeWidth, resizeHeight, bitmap.getConfig());
        cvtBarcodeBitmap2(bitmap, barcodeBitmap, blurValue);

        int borderWidth = resizeWidth / 3;
        int borderHeight = resizeHeight / 3;
        // To maintain the ratio 4:3
        while (borderHeight % 4 != 0) {
            borderHeight++;
            borderWidth = borderHeight * 3 / 4;
        }

        // Add the white border around the bitmap - It helps to identify the barcodes easily.
        Bitmap bitmapWithBorder = Bitmap.createBitmap(barcodeBitmap.getWidth() + borderWidth * 2,
                barcodeBitmap.getHeight() + borderHeight * 2, barcodeBitmap.getConfig());
        Canvas canvas = new Canvas(bitmapWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(barcodeBitmap, borderWidth, borderHeight, null);
        // Returns the white border bitmap.
        return bitmapWithBorder;
    }

    /**
     * Build the barcode bitmap from the given bytes and the blur value
     */
    protected static List<Integer> getResizedWidthAndHeight(Bitmap bitmap) {
        System.loadLibrary("lib_mddisdk");
        List<Integer> getResizedCoordinates = new ArrayList<>(2);
        int resizeWidth;
        int resizeHeight;
        if (bitmap.getWidth() > 1000 && bitmap.getHeight() > 1000) {
            resizeWidth = bitmap.getWidth() / 4;
            resizeHeight = bitmap.getHeight() / 4;
        } else {
            resizeWidth = bitmap.getWidth() / 2;
            resizeHeight = bitmap.getHeight() / 2;
        }
        getResizedCoordinates.add(resizeWidth);
        getResizedCoordinates.add(resizeHeight);

        return getResizedCoordinates;
    }

    /**
     * Build the MDDI Image from Bitmap.
     *
     * @param bitmap is the input bitmap.
     * @param sno    is the sno of the output MDDI image.
     * @return Mddi image.
     */
    public static Image buildImageFromBitmap(Bitmap bitmap,
                                             String sno) {
        requireNonNull(bitmap, sno);
        List<Integer> pixelsList;
        int[] pixels = new int[MIN_WIDTH * MIN_HEIGHT];
        Image image;
        Bitmap resizedBitmap;
        // Convert the coloured bitmap to grayscaled bitmap
        resizedBitmap = createResizedBitmap(bitmap, MIN_WIDTH, MIN_HEIGHT, Bitmap.Config.ARGB_8888);
        // Get the imagePixels from the Bitmap
        resizedBitmap.getPixels(pixels, 0, MIN_WIDTH, 0, 0, MIN_WIDTH, MIN_HEIGHT);
        pixelsList = new ArrayList<>(pixels.length);
        // Add the pixels to the pixelsList
        for (int j : pixels) {
            pixelsList.add(j);
        }
        // Build the Mddi image using the imagePixels from the Bitmap
        image = Image.newBuilder().addAllImagePixels(pixelsList).setImageName(sno).setImageWidth(MIN_WIDTH).
                setImageHeight(MIN_HEIGHT).setSno(sno).setImageFormat(0).setImageExt("jpeg").build();
        // Return the image
        return image;
    }

    /**
     * Build the Mddi stream image from image file, cid, sno etc.
     *
     * @param filePath     is the file path of the image(in .jpg,.png,.bmp formats).
     * @param imageName    is the image name of the output Mddi stream image.
     * @param cid          is the cid of the output Mddi stream image.
     * @param sno          is the sno of the output Mddi stream image.
     * @param instanceType is the instance type of the Mddi backend.
     * @return Mddi StreamImage.
     */
    public static StreamImage getStreamImageFromFile(String filePath,
                                                     String imageName,
                                                     String cid,
                                                     String sno,
                                                     InstanceType instanceType,
                                                     String tenantId) {
        requireNonNull(filePath, cid, sno);
        System.loadLibrary("lib_mddisdk");
        Bitmap resized = Bitmap.createBitmap(MIN_WIDTH, MIN_HEIGHT, Bitmap.Config.ARGB_8888);
        cvtResizedGrayscaledBitmapFromFile(filePath, resized);
        return getStreamImage(resized, cid, sno, imageName, instanceType, tenantId);
    }

    /**
     * Build the Mddi stream image from bitmap, cid, sno etc.
     *
     * @param bitmap       is the input bitmap(Can be of any 4:3 resolution).
     * @param cid          is the cid of the output Mddi stream image.
     * @param sno          is the sno of the output Mddi stream image.
     * @param instanceType is the instance type of the Mddi backend.
     * @return Mddi StreamImage.
     */
    public static StreamImage getStreamImageFromBitmap(Bitmap bitmap,
                                                       String cid,
                                                       String sno,
                                                       InstanceType instanceType,
                                                       String tenantId) {
        requireNonNull(bitmap, cid, sno);
        System.loadLibrary("lib_mddisdk");
        byte[] bytes = getBytesFromBitmap(bitmap);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap resized = Bitmap.createBitmap(MIN_WIDTH, MIN_HEIGHT, Bitmap.Config.ARGB_8888);
        cvtResizedGrayscaledBitmap(decodedBitmap, resized);
        return getStreamImage(resized, cid, sno, null, instanceType, tenantId);
    }

    public static Bitmap convertBitmapToMddiSpecs(Bitmap bitmap, int minWidth, int minHeight) {
        System.loadLibrary("lib_mddisdk");
        byte[] bytes = getBytesFromBitmap(bitmap);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap resized = Bitmap.createBitmap(minWidth, minHeight, Bitmap.Config.ARGB_8888);
        cvtResizedGrayscaledBitmap(decodedBitmap, resized);
        return resized;
    }

    /**
     * Build the Mddi stream image from resized bitmap.
     *
     * @param resizedBitmap is the resized bitmap(640*480 resolution).
     * @param cid           is the cid of the output Mddi stream image.
     * @param sno           is the sno of the output Mddi stream image.
     * @param imageName     is the image name of the output Mddi stream image.
     * @param instanceType  is the instance type of the Mddi backend.
     * @return Mddi StreamImage.
     */
    private static StreamImage getStreamImage(Bitmap resizedBitmap,
                                              String cid,
                                              String sno,
                                              String imageName,
                                              InstanceType instanceType,
                                              String tenantId) {
        requireNonNull(resizedBitmap, cid, sno, instanceType);
        List<Integer> pixelsList;
        int[] pixels = new int[MIN_WIDTH * MIN_HEIGHT];
        Image image;
        StreamImage streamImage;
        // Get the imagePixels from the resizedBitmap
        resizedBitmap.getPixels(pixels, 0, MIN_WIDTH, 0, 0, MIN_WIDTH, MIN_HEIGHT);
        pixelsList = new ArrayList<>(pixels.length);
        // Add the pixels to the pixelsList
        for (int j : pixels) {
            pixelsList.add(j);
        }
        if (instanceType == IVF) {
            // Build the Mddi image using the image pixels from the Bitmap
            image = Image.newBuilder().addAllImagePixels(pixelsList).setImageName(imageName != null ? imageName : VERSION_ID).setImageWidth(MIN_WIDTH).
                    setImageHeight(MIN_HEIGHT).setImageFormat(0).setImageExt("jpeg").build();
            // Build the Mddi stream image
            streamImage =
                    StreamImage.newBuilder().setImage(image).setTimestamp(Long.toString(System.currentTimeMillis())).
                            setCid(cid).setDebugFlag(false).build();

            return streamImage;
        }
        // Build the Mddi image using the image pixels from the Bitmap
        image = Image.newBuilder().addAllImagePixels(pixelsList).setImageName(imageName != null ?
                imageName : VERSION_ID).setImageWidth(MIN_WIDTH).
                setImageHeight(MIN_HEIGHT).setSno(sno).setImageFormat(0).setImageExt("jpeg").build();
        // Build the Mddi stream image
        streamImage =
                StreamImage.newBuilder().setImage(image).setTimestamp(Long.toString(System.currentTimeMillis())).
                        setCid(cid).setVersionId(VERSION_ID).setDebugFlag(false).setTenantId(tenantId).build();
        // Return the built stream image
        return streamImage;
    }

    /**
     * Calculate the time in Millis to HH:MM::SS format
     *
     * @param startTime is the star timestamp in milliseconds.
     * @param endTime   is the end timestamp in milliseconds.
     * @return time in 24 hour format.
     */
    public static String calculateTimeIn24HourFormat(long startTime, long endTime) {
        long elapsedTime = endTime - startTime;
        @SuppressLint("DefaultLocale") String periodHHMMSS = format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(elapsedTime),
                TimeUnit.MILLISECONDS.toMinutes(elapsedTime) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % TimeUnit.MINUTES.toSeconds(1));
        return periodHHMMSS;
    }

    /**
     * Append the messages to the buffer.
     *
     * @param stringBuffer is the buffer to collect the messages.
     * @param msg          is the actual message.
     * @param params       is the value of the object to be included in the message.
     */
    public static void appendMessages(StringBuffer stringBuffer, String msg, Object... params) {
        if (params.length > 0) {
            stringBuffer.append(MessageFormat.format(msg, params));
        } else {
            stringBuffer.append(msg);
        }
        stringBuffer.append(System.lineSeparator());
    }

    /**
     * Check whether the provided parameters are non null.
     *
     * @param params is the list of objects.(The objects can be of any type).
     */
    public static void requireNonNull(Object... params) {
        for (Object par : params) {
            Objects.requireNonNull(par);
        }
    }

    public static boolean channelClosedCheck(Throwable t) {
        return Objects.requireNonNull(t.getMessage()).endsWith("Channel shutdownNow " +
                "invoked") || (Objects.requireNonNull(t.getMessage()).endsWith(
                "Channel shutdown invoked"));
    }


    /**
     * Closing the gRPC channel right after the completion of the task.
     *
     * @param tag            is the tag to attach the log message for debugging.
     * @param managedChannel is the gRPC channel which is used for client server communication.
     */
    public static void closeGrpcChannel(String tag, ManagedChannel managedChannel) {
        if (managedChannel != null) {
            // Close the gRPC managed-channel if not shut down already.
            if (!managedChannel.isShutdown()) {
                Log.d(tag + "_CLOSE_GRPC_CHANNEL", "Shutting down the gRPC channel");
                managedChannel.shutdown();
                return;
            }
            // Forceful shut down if still not terminated.
            if (!managedChannel.isTerminated()) {
                Log.d(tag + "_FORCE_CLOSE_GRPC_CHANNEL", "Force shut down the gRPC channel");
                managedChannel.shutdownNow();
            }
        }
    }
}
