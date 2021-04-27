package com.logictreeit.android.fracturedphoto.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;

public class OkDialog extends BaseDefaultContentDialog {

    private static final String MESSAGE = "message";

    public static OkDialog newInstance(String message) {

        Bundle arguments = new Bundle();
        arguments.putString(MESSAGE, message);

        OkDialog okDialog = new OkDialog();
        okDialog.setArguments(arguments);

        return okDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        String message = arguments.getString(MESSAGE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (!TextUtils.isEmpty(message)) {
            builder.setMessage(message);
        }

        return builder.setCancelable(false)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }
}