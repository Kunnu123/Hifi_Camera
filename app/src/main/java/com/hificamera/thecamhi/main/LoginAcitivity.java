package com.hificamera.thecamhi.main;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.hificamera.R;
import com.hificamera.thecamhi.utils.SharePreUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class LoginAcitivity extends AppCompatActivity implements View.OnClickListener {

    EditText edt_email, edt_password;
    AppCompatTextView txtRegister;
    Button btnLogin;
    ScrollView mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_acitivity);

        initview();

    }

    private void initview() {
        edt_email = (EditText) findViewById(R.id.edt_email);
        edt_password = (EditText) findViewById(R.id.edt_password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        txtRegister = (AppCompatTextView) findViewById(R.id.txtRegister);
        mainLayout = (ScrollView) findViewById(R.id.mainLayout);

        btnLogin.setOnClickListener(this);
        txtRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLogin:
                if (!isValidEmail(edt_email.getText().toString().trim())){
                    Snackbar.make(mainLayout, "Please enter valid email address", Snackbar.LENGTH_LONG).show();
                }else if(TextUtils.isEmpty(edt_password.getText().toString().trim())){
                    Snackbar.make(mainLayout, "Please enter password", Snackbar.LENGTH_LONG).show();
                }else {
                    if (isNetworkConnected(LoginAcitivity.this)){
                        showProgressDialog();
                        doLogin();
                    }else {
                        showConnectionAlert(LoginAcitivity.this);
                    }
                }
                break;
            case R.id.txtRegister:
                Intent intent = new Intent(LoginAcitivity.this, RegisterAcitivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
        }
    }

    private void doLogin() {
        AndroidNetworking.post("http://oriontechnolabs.com/stock_guard/web_service/login")
                .addBodyParameter("email", edt_email.getText().toString().trim())
                .addBodyParameter("password", edt_password.getText().toString().trim())
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                         // do anything with response
                        cancelProgressDialog();
                        try {
                            if (response.getString("status").equalsIgnoreCase("200")){
                                JSONObject dataObject = response.getJSONObject("data");
                                SharePreUtils.putBoolean("isfshow", LoginAcitivity.this, "isLogin", true);
                                SharePreUtils.putString("isfshow", LoginAcitivity.this, "pre_userid", dataObject.getString("user_id"));
                                SharePreUtils.putString("isfshow", LoginAcitivity.this, "pre_customer_id", dataObject.getString("customer_id"));
                                SharePreUtils.putString("isfshow", LoginAcitivity.this, "pre_user_name", dataObject.getString("user_name"));
                                SharePreUtils.putString("isfshow", LoginAcitivity.this, "pre_address", dataObject.getString("address"));
                                SharePreUtils.putString("isfshow", LoginAcitivity.this, "pre_email", dataObject.getString("email"));
                                SharePreUtils.putString("isfshow", LoginAcitivity.this, "pre_phone_no", dataObject.getString("phone_no"));
                                SharePreUtils.putString("isfshow", LoginAcitivity.this, "pre_camera_id", dataObject.getString("camera_id"));
                                SharePreUtils.putString("isfshow", LoginAcitivity.this, "pre_is_camera_live", dataObject.getString("is_camera_live"));
                                SharePreUtils.putString("isfshow", LoginAcitivity.this, "pre_create_date", dataObject.getString("create_date"));

                                Intent intent = new Intent(LoginAcitivity.this, MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                finish();
                            }else {
                                Snackbar.make(mainLayout, response.getString("message"), Snackbar.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        cancelProgressDialog();
                        Snackbar.make(mainLayout, "Something went wrong. Please try again later...", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    public boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public boolean isNetworkConnected(Context activtiy) {
        ConnectivityManager cmss = (ConnectivityManager) activtiy
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo niss = cmss.getActiveNetworkInfo();
        return niss != null;
    }

    public void showConnectionAlert(final Context context) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                Objects.requireNonNull(context),R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle(context.getString(R.string.alert_title_connectionError));
        alertDialogBuilder.setMessage(context.getString(R.string.alert_msg_connectionError))
                .setPositiveButton(context.getString(R.string.action_ok), null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public  Dialog mDialog;
    public  void cancelProgressDialog() {
        if (mDialog != null
                && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void showProgressDialog() {
        if (LoginAcitivity.this != null
                && mDialog == null) {
            mDialog = new Dialog(LoginAcitivity.this);
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setContentView(R.layout.dialog);

            mDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(LoginAcitivity.this, android.R.color.transparent));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(mDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;

            mDialog.getWindow().setAttributes(lp);
            mDialog.show();
        }
    }
}