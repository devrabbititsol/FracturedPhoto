package com.logictreeit.android.fracturedphoto.helpers;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.KeyEvent;

public class CustomAlertDialog extends AlertDialog.Builder implements OnKeyListener{

	public static class CustomAlertDialogDetails {
		public Drawable dialogIcon;
		public String dialogTitle;
		public String dialogMessage;
		public ArrayList<CustomButton> dialogButton;
		
	}

	public static class CustomButton {
		public static final int LEFT = 1;
		public static final int RIGHT = 2;
		public String mBtnName;
		public int mBtnPosition;
		public DialogInterface.OnClickListener onClickListener;
		public CustomButton(String btnName , int btnPositon) {
		    mBtnName = btnName;
		    mBtnPosition = btnPositon;
        }
		public CustomButton(String btnName) {
            mBtnName = btnName;
            mBtnPosition = LEFT;
        }
	}
	
	public CustomAlertDialog(Context context, CustomAlertDialogDetails customAlertDialogDetails) {
		super(context);
		buildJumptuitDialog(customAlertDialogDetails);
		setOnKeyListener(this);
	}

	private void buildJumptuitDialog(CustomAlertDialogDetails customAlertDialogDetails) {

		if (customAlertDialogDetails == null) {
			return;
		}

		if (customAlertDialogDetails.dialogIcon != null) {
			setIcon(customAlertDialogDetails.dialogIcon);
		}

		if (!TextUtils.isEmpty(customAlertDialogDetails.dialogTitle)) {
			setTitle(customAlertDialogDetails.dialogTitle);
		}
		
		if (!TextUtils.isEmpty(customAlertDialogDetails.dialogMessage)) {
			setMessage(customAlertDialogDetails.dialogMessage);
		}
		
		if (customAlertDialogDetails.dialogButton != null) {
			for (int i = 0; i < customAlertDialogDetails.dialogButton.size(); i++) {
				
				switch (customAlertDialogDetails.dialogButton.get(i).mBtnPosition) {
				
				case CustomButton.RIGHT:
					
						setNegativeButton(customAlertDialogDetails.dialogButton.get(i).mBtnName,
								customAlertDialogDetails.dialogButton.get(i).onClickListener);
					
					
					break;
				case CustomButton.LEFT:
					
						setPositiveButton(customAlertDialogDetails.dialogButton.get(i).mBtnName,
								customAlertDialogDetails.dialogButton.get(i).onClickListener);
					
					
					break;
				default:
					break;
				}
			}
		}
	}

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
       
        if(keyCode == KeyEvent.KEYCODE_SEARCH)
        {
            return true;
        }
        return false;
    }
}
