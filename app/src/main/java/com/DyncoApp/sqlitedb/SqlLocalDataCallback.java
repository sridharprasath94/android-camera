package com.DyncoApp.sqlitedb;

import java.util.List;

public interface SqlLocalDataCallback {
    void onInstanceAdded(List<String> sqlList, CredentialsData result);

    void onDeletingInstance(List<String> List);

    void onDefaultsRestored(String message);

    void onGettingDefaultData(List<String> List);

    void onGettingInputData(CredentialsData result);

    void onError(String error);
}
