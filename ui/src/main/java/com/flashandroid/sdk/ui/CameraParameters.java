package com.flashandroid.sdk.ui;

public class CameraParameters {
    protected boolean defaultLayout;
    protected CameraConstants.CameraMode cameraMode;
    protected boolean primaryCamera;
    protected CameraRatioMode cameraRatioMode;
    protected Integer captureDelay;

    /**
     * @param cameraParametersBuilder Build the camera parameters with camera parameters builder.
     */
    public CameraParameters(Builder cameraParametersBuilder) {
        this.defaultLayout = cameraParametersBuilder.defaultLayout;
        this.cameraMode = cameraParametersBuilder.cameraMode;
        this.primaryCamera = cameraParametersBuilder.primaryCamera;
        this.cameraRatioMode = cameraParametersBuilder.cameraRatioMode;
        this.captureDelay = cameraParametersBuilder.captureDelay;
    }

    public static class Builder {
        private boolean defaultLayout = false;
        private CameraConstants.CameraMode cameraMode = CameraConstants.CameraMode.CAMERA_PREVIEW;
        private boolean primaryCamera = false;
        private CameraRatioMode cameraRatioMode = CameraRatioMode.RATIO_3X4;
        private Integer captureDelay = 1000;

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
        public Builder updateCameraMode(CameraConstants.CameraMode cameraMode) {
            this.cameraMode = cameraMode;
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



    public void setPrimaryCamera(boolean primaryCamera) {
        this.primaryCamera = primaryCamera;
    }

    public void setCameraRatioMode(CameraRatioMode cameraRatioMode) {
        this.cameraRatioMode = cameraRatioMode;
    }

    public void setCaptureDelay(Integer captureDelay) {
        this.captureDelay = captureDelay;
    }
}
