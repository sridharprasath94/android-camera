package com.dynamicelement.sdk.android.mddiclient;

public class MddiVariables {
    /**
     * Minimum width of the MDDI image
     */
    protected static int MIN_WIDTH = 480;
    /**
     * Minimum height of the MDDI image
     */
    protected static int MIN_HEIGHT = 640;

    public enum MddiImageSize {
        FORMAT_480X640(480, 640),
        FORMAT_512X512(512, 512);

        private final int width;
        private final int height;

        MddiImageSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }
    }
}


