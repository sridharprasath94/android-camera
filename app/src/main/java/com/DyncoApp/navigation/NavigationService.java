package com.DyncoApp.navigation;

import android.view.View;

import com.DyncoApp.R;
import com.DyncoApp.ui.cameraScan.CameraScanScreenDirections;
import com.DyncoApp.ui.common.SummaryViewArguments;
import com.DyncoApp.ui.common.MddiMode;
import com.DyncoApp.ui.home.HomeScreenDirections;
import com.DyncoApp.ui.modeSelect.ModeSelectScreenDirections;
import com.DyncoApp.ui.selectCollection.SelectCollectionScreenDirections;
import com.DyncoApp.ui.summary.SummaryScreenDirections;
import com.DyncoApp.ui.verificationFailure.VerificationFailureScreenDirections;

public class NavigationService {

    public static class HomeNav {
        public static void moveToSecretView(View currentView) {
            androidx.navigation.Navigation.findNavController(currentView).navigate(R.id.action_homeScreen_to_secretScreen);
        }

        public static void moveToSelectCollectionView(View currentView, boolean userMode, boolean createCollection) {
            HomeScreenDirections.ActionHomeScreenToSelectCollectionScreen action =
                    HomeScreenDirections.actionHomeScreenToSelectCollectionScreen(userMode, createCollection);
            androidx.navigation.Navigation.findNavController(currentView).navigate(action);
        }
    }

    public static class SelectCollectionNav {
        public static void moveToModeSelectView(View currentView, boolean userMode, boolean createCollection, String mddiCid) {
            SelectCollectionScreenDirections.ActionSelectCollectionScreenToModeSelectScreen action =
                    SelectCollectionScreenDirections.actionSelectCollectionScreenToModeSelectScreen(userMode, createCollection, mddiCid);
            androidx.navigation.Navigation.findNavController(currentView).navigate(action);
        }

        public static void moveToHomeView(View currentView) {
            androidx.navigation.Navigation.findNavController(currentView).navigate(R.id.action_selectCollectionScreen_to_homeScreen);
        }
    }

    public static class ModeSelectNav {
        public static void moveToCameraView(View currentView, boolean userMode, boolean createCollection, String mddiCid, MddiMode mddiMode) {
            ModeSelectScreenDirections.ActionModeSelectScreenToCameraScanScreen action = ModeSelectScreenDirections
                    .actionModeSelectScreenToCameraScanScreen(userMode, createCollection, mddiCid, mddiMode);
            androidx.navigation.Navigation.findNavController(currentView).navigate(action);
        }

        public static void moveToSelectCollectionView(View currentView, boolean userMode, boolean createCollection) {
            ModeSelectScreenDirections.ActionModeSelectScreenToSelectCollectionScreen action =
                    ModeSelectScreenDirections.actionModeSelectScreenToSelectCollectionScreen(userMode, createCollection);
            androidx.navigation.Navigation.findNavController(currentView).navigate(action);
        }
    }

    public static class CameraNav {
        public static void moveToSummaryView(View currentView, SummaryViewArguments args) {
            CameraScanScreenDirections.ActionCameraScanScreenToSummaryScreen action =
                    CameraScanScreenDirections.actionCameraScanScreenToSummaryScreen(args);
            androidx.navigation.Navigation.findNavController(currentView).navigate(action);
        }

        public static void moveToVerificationFailureView(View currentView, boolean userMode, boolean createCollection, String mddiCid, MddiMode mddiMode) {
            CameraScanScreenDirections.ActionCameraScanScreenToVerificationFailureScreen action =
                    CameraScanScreenDirections.actionCameraScanScreenToVerificationFailureScreen(userMode, createCollection, mddiCid, mddiMode);
            androidx.navigation.Navigation.findNavController(currentView).navigate(action);
        }

        public static void moveToModeSelectView(View currentView, boolean userMode, boolean createCollection, String mddiCid) {
            CameraScanScreenDirections.ActionCameraScanScreenToModeSelectScreen action =
                    CameraScanScreenDirections.actionCameraScanScreenToModeSelectScreen(userMode, createCollection, mddiCid);
            androidx.navigation.Navigation.findNavController(currentView).navigate(action);
        }
    }

    public static class SummaryNav {
        public static void moveToHomeView(View currentView) {
            androidx.navigation.Navigation.findNavController(currentView).navigate(R.id.action_summaryScreen_to_homeScreen);
        }

        public static void moveToCameraView(View currentView, boolean userMode, boolean createCollection, String mddiCid, MddiMode mddiMode) {
            SummaryScreenDirections.ActionSummaryScreenToCameraScanScreen action = SummaryScreenDirections
                    .actionSummaryScreenToCameraScanScreen(userMode, createCollection, mddiCid, mddiMode);
            androidx.navigation.Navigation.findNavController(currentView).navigate(action);
        }
    }

    public static class VerificationFailureNav {
        public static void moveToHomeView(View currentView) {
            androidx.navigation.Navigation.findNavController(currentView).navigate(R.id.action_verificationFailureScreen_to_homeScreen);
        }

        public static void moveToCameraView(View currentView, boolean userMode, boolean createCollection, String mddiCid, MddiMode mddiMode) {
            VerificationFailureScreenDirections.ActionVerificationFailureScreenToCameraScanScreen action =
                    VerificationFailureScreenDirections.actionVerificationFailureScreenToCameraScanScreen(userMode, createCollection, mddiCid, mddiMode);
            androidx.navigation.Navigation.findNavController(currentView).navigate(action);
        }

        public static void moveToModeSelectView(View currentView, boolean userMode, boolean createCollection, String mddiCid) {
            VerificationFailureScreenDirections.ActionVerificationFailureScreenToModeSelectScreen action =
                    VerificationFailureScreenDirections.actionVerificationFailureScreenToModeSelectScreen(userMode, createCollection, mddiCid);
            androidx.navigation.Navigation.findNavController(currentView).navigate(action);
        }
    }


}
