package com.DyncoApp.ui.selectCollection;

import static android.content.Context.MODE_PRIVATE;
import static com.DyncoApp.ui.common.Constants.DEFAULT_CID;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import com.DyncoApp.R;
import com.DyncoApp.ui.common.CompletionCallback;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.delete.DeleteResult;
import com.dynamicelement.sdk.android.delete.DeleteStatus;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.getsample.GetSampleResult;
import com.dynamicelement.sdk.android.getsample.GetSampleStatus;
import com.dynamicelement.sdk.android.mddiclient.ClientService;

import java.util.Objects;


public class SelectCollectionModel {
    private final SharedPreferences sharedPreferences;
    private final String KEY_CID;
    private final SharedPreferences.Editor editor;
    private final Context context;
    private final ClientService clientService;

    public SelectCollectionModel(Context context, ClientService clientService) {
        this.context = context;
        this.clientService = clientService;
        KEY_CID = context.
                getString(R.string.select_collection_screen_cid);
        sharedPreferences = this.context.getSharedPreferences(context.
                getString(R.string.shared_preferences), MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    String getSavedCidText() {
        return sharedPreferences.getString(KEY_CID, DEFAULT_CID);
    }

    void saveCidOnFinishEditing(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                editor.putString(KEY_CID, editable.toString()).apply();
            }
        });
    }

    void checkExistingCid(String cid,
                          boolean createCollectionSelected,
                          CompletionCallback<GetSampleResult> completionCallback) {
        HandlerThread connectionHandlerThread = new HandlerThread("connection handler");
        connectionHandlerThread.start();
        Handler connectionHandler = new Handler(connectionHandlerThread.getLooper());
        connectionHandler.post(() -> this.clientService.getSample(cid, true, new Callback<GetSampleResult>() {
            @Override
            public void onResponse(GetSampleResult response) {
                handleResponse(response, createCollectionSelected, completionCallback);
            }

            @Override
            public void onError(ExceptionType exceptionType, Exception e) {
                handleException(e, createCollectionSelected, completionCallback);
            }
        }));
    }

    private void handleException(Exception e, boolean createCollectionSelected, CompletionCallback<GetSampleResult> completionCallback) {
        if (createCollectionSelected && Objects.requireNonNull(e.getMessage()).endsWith(this.context.getString(R.string.invalid_cid_error))) {
            completionCallback.showAlert(this.context.getString(R.string.create_cid_confirmation));
            return;
        } else if (!createCollectionSelected && Objects.requireNonNull(e.getMessage()).endsWith(this.context.getString(R.string.invalid_cid_error))) {
            Toast.makeText(context.getApplicationContext(), this.context.getString(R.string.cid_not_exist_error),
                    Toast.LENGTH_SHORT).show();
        }
        completionCallback.onError(e);
    }

    private void handleResponse(GetSampleResult response, boolean createCollectionSelected, CompletionCallback<GetSampleResult> completionCallback) {
        if (!createCollectionSelected) {
            if (response.getSampleImage() == null) {
                completionCallback.onError(new Exception(this.context.getString(R.string.wrong_size_format_error)));
                return;
            }
            completionCallback.onSuccess(response);
            return;
        }

        completionCallback.showAlert(response.getStatus() == GetSampleStatus.EXISTING_CID ?
                this.context.getString(R.string.cid_already_exists_warning) :
                this.context.getString(R.string.cid_not_exist_warning));
    }


    void deleteCollection(String cid, CompletionCallback<DeleteResult> completionCallback) {
        this.clientService.deleteCollection(cid, new Callback<DeleteResult>() {
            @Override
            public void onResponse(DeleteResult response) {
                if (response.getDeleteStatus() == DeleteStatus.DELETED ||
                        response.getDeleteStatus() == DeleteStatus.CID_NOT_EXISTS) {
                    completionCallback.onSuccess(response);
                }
            }

            @Override
            public void onError(ExceptionType exceptionType, Exception e) {
                completionCallback.onError(e);
            }
        });
    }

}
