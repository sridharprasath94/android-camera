package com.dynamicelement.sdk.android.ui;

public class CameraParameters {
    protected boolean defaultLayout;
    protected boolean enableBarcodeScan;
    protected boolean primaryCamera;
    protected CameraRatioMode cameraRatioMode;
    protected Integer captureDelay;
    protected boolean blurBeforeBarcode;
    protected boolean checkBarcodeFormat;

    /**
     * @param cameraParametersBuilder Build the camera parameters with camera parameters builder.
     */
    public CameraParameters(Builder cameraParametersBuilder) {
        this.defaultLayout = cameraParametersBuilder.defaultLayout;
        this.enableBarcodeScan = cameraParametersBuilder.enableBarcodeScan;
        this.primaryCamera = cameraParametersBuilder.primaryCamera;
        this.cameraRatioMode = cameraParametersBuilder.cameraRatioMode;
        this.captureDelay = cameraParametersBuilder.captureDelay;
        this.blurBeforeBarcode = cameraParametersBuilder.blurBeforeBarcode;
        this.checkBarcodeFormat = cameraParametersBuilder.checkBarcodeFormat;
    }

    public static class Builder {
        private boolean defaultLayout = false;
        private boolean enableBarcodeScan = false;
        private boolean primaryCamera = false;
        private CameraRatioMode cameraRatioMode = CameraRatioMode.RATIO_3X4;
        private Integer captureDelay = 1000;
        private boolean blurBeforeBarcode = false;
        private boolean checkBarcodeFormat = true;

        /**
         * Enable the default layout.
         **/
        public Builder enableDefaultLayout(boolean defaultLayout) {
            this.defaultLayout = defaultLayout;
            return this;
        }

        /**
         * Enable the barcode scan.
         **/
        public Builder enableBarcodeScan(boolean enableBarcodeScan) {
            this.enableBarcodeScan = enableBarcodeScan;
            return this;
        }

        /**
         * Enable the primary camera - The camera id '0' will be selected.
         **/
        public Builder selectPrimaryCamera(boolean primaryCamera) {
            this.primaryCamera = primaryCamera;
            return this;
        }

        /**
         * Select the ratio 1:1 or 3:4
         **/
        public Builder selectRatio(CameraRatioMode cameraRatioMode) {
            this.cameraRatioMode = cameraRatioMode;
            return this;
        }

        /**
         * Initialise the capture delay value
         **/
        public Builder initialiseCaptureDelay(Integer captureDelay) {
            this.captureDelay = captureDelay;
            return this;
        }

        /**
         * Initialise the option blurBeforeBarcode(To check whether image needs to be blurred for efficient barcode detection)
         **/
        public Builder blurBeforeBarcode(boolean blurBeforeBarcode) {
            this.blurBeforeBarcode = blurBeforeBarcode;
            return this;
        }

        public Builder checkBarcodeFormat(boolean checkBarcodeFormat) {
            this.checkBarcodeFormat = checkBarcodeFormat;
            return this;
        }

        /**
         * Build the CameraParameters with this builder.
         **/
        public CameraParameters build() {
            return new CameraParameters(this);
        }
    }

    public enum CameraRatioMode {

        RATIO_1X1(1d),
        RATIO_3X4(3d / 4d);

        private final double numVal;

        CameraRatioMode(double numVal) {
            this.numVal = numVal;
        }

        public double getNumVal() {
            return this.numVal;
        }
    }

    public boolean isPrimaryCamera() {
        return this.primaryCamera;
    }

    public CameraRatioMode getCameraRatioMode() {
        return this.cameraRatioMode;
    }

    public Integer getCaptureDelay() {
        return this.captureDelay;
    }

    public boolean isBlurBeforeBarcode() {
        return this.blurBeforeBarcode;
    }

    public boolean isCheckBarcodeFormat() {
        return this.checkBarcodeFormat;
    }


    public void setPrimaryCamera(boolean primaryCamera) {
        this.primaryCamera = primaryCamera;
    }

    public void setCameraRatioMode(CameraRatioMode cameraRatioMode) {
        this.cameraRatioMode = cameraRatioMode;
    }

    public void setCaptureDelay(Integer captureDelay) {
        this.captureDelay = captureDelay;
    }

    public void setBlurBeforeBarcode(boolean blurBeforeBarcode) {
        this.blurBeforeBarcode = blurBeforeBarcode;
    }

    public void setCheckBarcodeFormat(boolean checkBarcodeFormat) {
        this.checkBarcodeFormat = checkBarcodeFormat;
    }
}
