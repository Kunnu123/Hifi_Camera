package com.hificamera.thecamhi.utils;


import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;

import com.hificamera.R;

public final class PactDialogUtils {

    private Context context;
    private int themeResId;
    private View layout;
    private boolean cancelable = true;
    private CharSequence title, message, cancelText, sureText;//
    private View.OnClickListener sureClickListener, cancelClickListener,agreementClickListener,privacyClickListener;

    public PactDialogUtils(Context context) {
        this(context, R.style.CustomDialog);
    }

    private PactDialogUtils(Context context, int themeResId) {
        this(context, themeResId, ((LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.my_dialog_pact_layout, null));
    }

    private PactDialogUtils(Context context, int themeResId, View layout) {
        this.context = context;
        this.themeResId = themeResId;
        this.layout = layout;
    }


    public PactDialogUtils setCancelable(Boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public PactDialogUtils title(CharSequence title) {
        this.title = title;
        return this;
    }

    public PactDialogUtils message(CharSequence message) {
        this.message = message;
        return this;
    }

    public PactDialogUtils cancelText(CharSequence str) {
        this.cancelText = str;
        return this;
    }


    public PactDialogUtils sureText(CharSequence str) {
        this.sureText = str;
        return this;
    }

    public PactDialogUtils setSureOnClickListener(View.OnClickListener listener) {
        this.sureClickListener = listener;
        return this;
    }

    public PactDialogUtils setCancelOnClickListener(View.OnClickListener listener) {
        this.cancelClickListener = listener;
        return this;
    }
    public PactDialogUtils setAgreementClickListener(View.OnClickListener listener) {
        this.agreementClickListener = listener;
        return this;
    }
    public PactDialogUtils setPrivacyClickListener(View.OnClickListener listener) {
        this.privacyClickListener = listener;
        return this;
    }
    public Dialog build() {
        final Dialog dialog = new Dialog(context, themeResId);
        dialog.setCancelable(cancelable);
        dialog.addContentView(layout, new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));

        setText(title, R.id.title);
        setText(message, R.id.message);
        setText(cancelText, R.id.cancel);
        setText(sureText, R.id.sure);
        if (isValid(cancelText) || isValid(sureText)) {
            layout.findViewById(R.id.line2).setVisibility(View.VISIBLE);
        }
        if (isValid(cancelText) && isValid(sureText)) {
            layout.findViewById(R.id.line).setVisibility(View.VISIBLE);
        }

        final TextView textView = (TextView)layout.findViewById(R.id.message);
        textView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if(textView.getLineCount() ==1){
                    textView.setGravity(Gravity.CENTER);
                }
                return true;
            }
        });

        if (sureClickListener != null) {
            layout.findViewById(R.id.sure).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sureClickListener.onClick(view);
                    dialog.dismiss();
                }
            });
        }
        if (cancelClickListener != null) {
            layout.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelClickListener.onClick(view);
                    dialog.dismiss();
                }
            });
        }

        if (privacyClickListener != null) {
            layout.findViewById(R.id.tv_privacy_agreement).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    privacyClickListener.onClick(view);
                   // dialog.dismiss();
                }
            });
        }
        if (agreementClickListener != null) {
            layout.findViewById(R.id.tv_user_agreement).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    agreementClickListener.onClick(view);
                    //dialog.dismiss();
                }
            });
        }
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.80);
        dialog.getWindow().setAttributes(params);
        return dialog;
    }

    private void setText(CharSequence text, int id) {
        if (isValid(text)) {
            TextView textView = (TextView) layout.findViewById(id);
            textView.setText(text);
            textView.setVisibility(View.VISIBLE);
        }
    }

    private boolean isValid(CharSequence text) {
        return text != null && !"".equals(text.toString().trim());
    }
}

