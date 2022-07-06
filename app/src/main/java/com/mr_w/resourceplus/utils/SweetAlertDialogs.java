package com.mr_w.resourceplus.utils;

import android.app.Activity;
import android.content.Context;
import android.view.WindowManager;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SweetAlertDialogs {

    private static SweetAlertDialogs instance = null;

    private SweetAlertDialogs() {
    }

    public static SweetAlertDialogs getInstance() {
        if (instance == null) {
            instance = new SweetAlertDialogs();
        }
        return instance;
    }

    public void showDialogYesNo(Context context, String title, String message, OnDialogClickListener onClickPositiveButton, OnDialogClickListener onClickNegativeButton, boolean bIsCancelable) {
        try {
            SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
            if (title != null) {
                dialog.setTitle(title);
            }
            dialog.setContentText(message);
            dialog.setConfirmText("Yes");
            dialog.setConfirmClickListener(onClickPositiveButton);
            dialog.setCancelText("No");
            dialog.setCancelClickListener(onClickNegativeButton);
            dialog.setCancelable(bIsCancelable);
            dialog.show();
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        }
    }

    public void showDialogOK(Context context, String title, String message, OnDialogClickListener onClickPositiveButton, boolean bIsCancelable) {
        try {
            SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE);

            if (title != null) {
                dialog.setTitleText(title);
            }
            if (message != null) {
                dialog.setContentText(message);
            }
            dialog.setConfirmClickListener(onClickPositiveButton);
            dialog.setCancelable(bIsCancelable);
            dialog.show();
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        }
    }

    public void showDialogPermissionDenied(Context context, OnDialogClickListener onClickPositiveButton) {
        try {
            SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE);

            dialog.setTitleText("Permission Denied");

            dialog.setContentText("Allow all permissions to proceed");

            dialog.setConfirmClickListener(onClickPositiveButton);
            dialog.show();
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        }
    }

    public void showDialogOKCancel(Context context, String title, String message, OnDialogClickListener onClickPositiveButton, OnDialogClickListener onClickNegativeButton, boolean bIsCancelable) {
        try {
            SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE);
            if (title != null) {
                dialog.setTitle(title);
            }
            dialog.setContentText(message);
            dialog.setCancelText("Cancel - منسوخ");
            dialog.setCancelClickListener(onClickNegativeButton);
            dialog.setConfirmText("OK - ٹھیک");
            dialog.setConfirmClickListener(onClickPositiveButton);
            dialog.setCancelable(bIsCancelable);
            dialog.show();
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        }
    }

    public void showDialogOnBackPressed(Context context, Activity activity) {
        SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
        dialog.setTitle("All Data will be Lost - درج ڈیٹا کھو جائے گا");
        dialog.setContentText("Are You Sure You Want to Exit?\nکیا آپ واقعی بند کرنا چاھتے ہیں؟");
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                activity.finish();
            }
        });
        dialog.setConfirmText("Yes - جی ہاں");
        dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();

            }
        });
        dialog.setCancelText("No - نہیں");
        dialog.show();
    }

    public void showDialogOnExitApplication(Context context, Activity activity) {
        SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
        dialog.setTitle("Exit?");
        dialog.setContentText("Are You Sure You Want to Exit Application?\nکیا آپ واقعی ایپ بند کرنا چاھتے ہیں؟");
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
                activity.finish();
            }
        });
        dialog.setConfirmText("Yes - جی ہاں");
        dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();

            }
        });
        dialog.setCancelText("No - نہیں");
        dialog.show();
    }

    public interface OnDialogClickListener extends SweetAlertDialog.OnSweetClickListener {
    }
}