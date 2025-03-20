package com.flashandroid.sdk.ui;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CameraSelect {
    private static final String[] POSSIBLE_CAMERA_IDS = new String[10];

    /**
     * Select the camera ID.The aim is to select the macro camera (if available).
     * For some of the tested android devices,the camera ID of the macro camera is known.So,they are hardcoded.
     * For other devices, the provided method will try to detect the macro camera with default camera properties.
     *
     * @param cameraManager is the camera manager from the system camera service.
     * @return The ID of the current selected camera.
     */
    protected static String selectCamera(CameraManager cameraManager) throws CameraAccessException {
        // Get the required camera ID from the camera manager.
        return switch (Build.MODEL) {
            case "VOG-L29" ->
                // Get the required camera ID from the camera manager.
                    "3";
            case "moto g(100)", "SM-G998B" -> "2";
            case "motorola one fusion+", "V2056A" -> "4";
            case "HD1903", "M2103K19G", "SM-G980F", "IN2023", "CPH2025" -> "0";
            default -> detectMacroCamera(cameraManager);
        };
    }

    /**
     * Tries to detect the best available camera for macro captures.
     *
     * @param cameraManager is the camera manager from the system camera service.
     * @returnhe ID of the current macro camera.
     */
    protected static String detectMacroCamera(CameraManager cameraManager) throws CameraAccessException {
        Map<String, Float> focalLengths = chooseCameraByFocalDistance(cameraManager);

        // Order cameras by their minimum focus distance.
        Map<String, Float> orderedFocusDistances = sortByValue(focalLengths);
        List<Map.Entry<String, Float>> orderedEntryList =
                new ArrayList<>(orderedFocusDistances.entrySet());

        if (!orderedEntryList.isEmpty()) {
            List<String> smallestFocalLengths = new ArrayList<>();
            // Set a high value here to avoid missing focal lengths.
            Float minFocalLength = 100000f;
            for (Map.Entry<String, Float> entry : orderedEntryList) {

                if (entry.getValue() <= minFocalLength) {
                    smallestFocalLengths.add(entry.getKey());
                    minFocalLength = entry.getValue();
                    continue;
                }
                // TODO @Sridhar why is this commented out?
//                Float minFocusDistance = cameraManager.getCameraCharacteristics(entry.getKey())
//                        .get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
//                if (minFocusDistance == null || minFocusDistance == 0 || minFocusDistance > 10.0f) {
//                    continue;
//                }
                break;
            }
            Log.d("CAMERA_LIST", String.valueOf(smallestFocalLengths));

            if (smallestFocalLengths.size() == 1) {
                // Get camera with nearest focus.
                return orderedEntryList.get(0).getKey();
            }
            // Check the minimum focus distance.
            Map<String, Float> minFocusDistances = new HashMap<>();
            for (String cameraId : smallestFocalLengths) {

                Float minFocusDistance = cameraManager.getCameraCharacteristics(cameraId)
                        .get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
                if (minFocusDistance != null) {
                    minFocusDistances.put(cameraId, minFocusDistance);
                }
            }
            Map<String, Float> orderedMinFocusDistances = sortByValue(minFocusDistances);
            List<Map.Entry<String, Float>> orderedMinFocusDistancesList =
                    new ArrayList<>(orderedMinFocusDistances.entrySet());
            return orderedMinFocusDistancesList.get(0).getKey();
        }
        return "0";
    }

    /**
     * Sort the hash map by value in ascending order and returns the sorted hash map.
     *
     * @param map is the map containing key and value.
     * @param <K> is the key type of the map.
     * @param <V> is the value type of the map.
     * @return Value sorted(ascending order) hash map.
     */
    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * @param cameraManager is the camera manager from the system camera service.
     * @return Hash map with mapped camera IDs and focal lengths.
     */
    private static Map<String, Float> chooseCameraByFocalDistance(CameraManager cameraManager) throws CameraAccessException {
        Map<String, Float> focalLengthMap = new HashMap<>();

        for (int i = 0; i < POSSIBLE_CAMERA_IDS.length; i++) {
            POSSIBLE_CAMERA_IDS[i] = String.valueOf(i);
        }
        //Get the camera with the minimum focal distance
        for (String cameraId : cameraManager.getCameraIdList()) {
            addCameraFocalLengthToMap(cameraManager, cameraId, focalLengthMap);
        }
        // Check all other possible camera IDs.
        for (String cameraId : POSSIBLE_CAMERA_IDS) {
            if (focalLengthMap.containsKey(cameraId)) {
                //Will not proceed for the already available IDs
                continue;
            }
            // Skip not available cameras.
            try {
                cameraManager.getCameraCharacteristics(cameraId);
            } catch (Throwable t) {
                //For unknown camera IDs, it will not move to next step.
                continue;
            }
            addCameraFocalLengthToMap(cameraManager, cameraId, focalLengthMap);
        }
        Log.d("CAMERA_LIST", String.valueOf(focalLengthMap));
        return focalLengthMap;
    }

    /**
     * @param cameraManager is the camera manager from the system camera service.
     * @param cameraId      is the ID of the currently selected camera.
     * @param focalLengths  is the hash map to map the current camera ID and its focal length.
     */
    private static void addCameraFocalLengthToMap(CameraManager cameraManager,
                                                  String cameraId,
                                                  Map<String, Float> focalLengths) throws CameraAccessException {
        Integer lensFacingKey = cameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.LENS_FACING);
        if (lensFacingKey == null || lensFacingKey != CameraCharacteristics.LENS_FACING_FRONT) {
            float[] focusDistancesCamera = cameraManager.getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
            if (focusDistancesCamera != null && focusDistancesCamera.length > 0) {
                Float minFocusDistance = cameraManager.getCameraCharacteristics(cameraId)
                        .get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
                Integer focusCalibration = cameraManager.getCameraCharacteristics(cameraId).
                        get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION);
                Log.d("CAMERA_FOCUS_DISTANCE", "Camera ID" + ":" + cameraId
                        + "/" + "Focus Distance" + ":" + minFocusDistance
                        + "/" + "Focus Calibration" + ":" + focusCalibration);
                // Map the focal distance for the current camera Id
                focalLengths.put(cameraId, focusDistancesCamera[0]);
            }
        }
    }

    /**
     * Get the list of all the available back cameras.
     *
     * @param cameraManager is the camera manager from the system camera service.
     * @return List of all the available back camera IDs.
     */
    protected static ArrayList<String> getCameraList(CameraManager cameraManager) throws CameraAccessException {
        ArrayList<String> cameraList = new ArrayList<>();
        Map<String, Float> focalLengths = chooseCameraByFocalDistance(cameraManager);

        for (Map.Entry<String, Float> entry : focalLengths.entrySet()) {
            cameraList.add(entry.getKey());
        }
        return cameraList;
    }

    protected static String detectMainBackLens(CameraManager cameraManager) throws CameraAccessException {
        String defaultBackCameraId = "0";
        List<String> availableCameras = getCameraList(cameraManager);
        // First check default back camera ID.
        if (availableCameras.contains(defaultBackCameraId)) {
            Integer lensFacingKey = cameraManager.getCameraCharacteristics(defaultBackCameraId)
                    .get(CameraCharacteristics.LENS_FACING);
            if (lensFacingKey == null || lensFacingKey != CameraCharacteristics.LENS_FACING_FRONT) {
                return defaultBackCameraId;
            }
        }
        // Otherwise check all other available cameras.
        for (String camera : availableCameras) {
            Integer lensFacingKey = cameraManager.getCameraCharacteristics(camera)
                    .get(CameraCharacteristics.LENS_FACING);
            if (lensFacingKey == null || lensFacingKey != CameraCharacteristics.LENS_FACING_FRONT) {
                return camera;
            }
        }
        return defaultBackCameraId;
    }
}
