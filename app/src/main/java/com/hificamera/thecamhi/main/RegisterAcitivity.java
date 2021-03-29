package com.hificamera.thecamhi.main;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.hificamera.R;
import com.hificamera.thecamhi.utils.SharePreUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class RegisterAcitivity extends AppCompatActivity implements View.OnClickListener {

    AppCompatTextView txtLogin;
    Button btnRegister;

    ScrollView mainLayout;
    LinearLayout llDate;
    EditText edt_cust_id, edt_username, edt_email, edt_camera_id, edt_camera_dop, edt_phone_number, edt_address, edt_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regiter);

        initview();

    }

    private void initview() {
        btnRegister = (Button) findViewById(R.id.btnRegister);
        txtLogin = (AppCompatTextView) findViewById(R.id.txtLogin);
        mainLayout = (ScrollView) findViewById(R.id.mainLayout);
        llDate = (LinearLayout) findViewById(R.id.llDate);
        edt_cust_id = (EditText) findViewById(R.id.edt_cust_id);
        edt_username = (EditText) findViewById(R.id.edt_username);
        edt_email = (EditText) findViewById(R.id.edt_email);
        edt_camera_id = (EditText) findViewById(R.id.edt_camera_id);
        edt_camera_dop = (EditText) findViewById(R.id.edt_camera_dop);
        edt_phone_number = (EditText) findViewById(R.id.edt_phone_number);
        edt_address = (EditText) findViewById(R.id.edt_address);
        edt_password = (EditText) findViewById(R.id.edt_password);

        btnRegister.setOnClickListener(this);
        txtLogin.setOnClickListener(this);
        llDate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRegister:
                if (TextUtils.isEmpty(edt_cust_id.getText().toString().trim())) {
                    Snackbar.make(mainLayout, "Please enter customer Id", Snackbar.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(edt_username.getText().toString().trim())) {
                    Snackbar.make(mainLayout, "Please enter user name", Snackbar.LENGTH_LONG).show();
                } else if (!isValidEmail(edt_email.getText().toString().trim())) {
                    Snackbar.make(mainLayout, "Please enter valid email address", Snackbar.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(edt_camera_id.getText().toString().trim())) {
                    Snackbar.make(mainLayout, "Please enter camera Id", Snackbar.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(edt_camera_dop.getText().toString().trim())) {
                    Snackbar.make(mainLayout, "Please select camera DOP", Snackbar.LENGTH_LONG).show();
                }else if (TextUtils.isEmpty(edt_phone_number.getText().toString().trim())) {
                    Snackbar.make(mainLayout, "Please enter phone number", Snackbar.LENGTH_LONG).show();
                }else if (TextUtils.isEmpty(edt_address.getText().toString().trim())) {
                    Snackbar.make(mainLayout, "Please enter address", Snackbar.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(edt_password.getText().toString().trim())) {
                    Snackbar.make(mainLayout, "Please enter password", Snackbar.LENGTH_LONG).show();
                } else {
                    if (isNetworkConnected(RegisterAcitivity.this)) {
                        showProgressDialog();
                        doRegister();
                    } else {
                        showConnectionAlert(RegisterAcitivity.this);
                    }
                }
                break;
            case R.id.txtLogin:
                finish();
                break;
            case R.id.llDate:
                final Calendar newCalendar = Calendar.getInstance();
                final DatePickerDialog StartTime = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, monthOfYear, dayOfMonth);
                        SimpleDateFormat dateFormatter=new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                        edt_camera_dop.setText(dateFormatter.format(newDate.getTime()));
                    }
                }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
                StartTime.show();
                break;
        }
    }

    private void doRegister() {
        AndroidNetworking.post("http://oriontechnolabs.com/stock_guard/web_service/user_signup")
                .addBodyParameter("customer_id", edt_cust_id.getText().toString().trim())
                .addBodyParameter("user_name", edt_username.getText().toString().trim())
                .addBodyParameter("email", edt_email.getText().toString().trim())
                .addBodyParameter("camera_id", edt_camera_id.getText().toString().trim())
                .addBodyParameter("phone_no", edt_phone_number.getText().toString().trim())
                .addBodyParameter("address", edt_address.getText().toString().trim())
                .addBodyParameter("camera_dop", edt_camera_dop.getText().toString().trim())
                .addBodyParameter("password", edt_password.getText().toString().trim())
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        cancelProgressDialog();
                        try {
                            if (response.getString("status").equalsIgnoreCase("200")) {
                                JSONObject dataObject = response.getJSONObject("data");
                                SharePreUtils.putBoolean("isfshow", RegisterAcitivity.this, "isLogin", true);
                                SharePreUtils.putString("isfshow", RegisterAcitivity.this, "pre_userid", dataObject.getString("user_id"));
                                SharePreUtils.putString("isfshow", RegisterAcitivity.this, "pre_customer_id", dataObject.getString("customer_id"));
                                SharePreUtils.putString("isfshow", RegisterAcitivity.this, "pre_user_name", dataObject.getString("user_name"));
                                SharePreUtils.putString("isfshow", RegisterAcitivity.this, "pre_address", dataObject.getString("address"));
                                SharePreUtils.putString("isfshow", RegisterAcitivity.this, "pre_email", dataObject.getString("email"));
                                SharePreUtils.putString("isfshow", RegisterAcitivity.this, "pre_phone_no", dataObject.getString("phone_no"));
                                SharePreUtils.putString("isfshow", RegisterAcitivity.this, "pre_camera_id", dataObject.getString("camera_id"));
                                SharePreUtils.putString("isfshow", RegisterAcitivity.this, "pre_is_camera_live", dataObject.getString("is_camera_live"));
                                SharePreUtils.putString("isfshow", RegisterAcitivity.this, "pre_create_date", dataObject.getString("create_date"));

                                Intent intent = new Intent(RegisterAcitivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                finish();
                            } else {
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
                Objects.requireNonNull(context), R.style.CustomDialogTheme);
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

    public Dialog mDialog;

    public void cancelProgressDialog() {
        if (mDialog != null
                && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void showProgressDialog() {
        if (RegisterAcitivity.this != null
                && mDialog == null) {
            mDialog = new Dialog(RegisterAcitivity.this);
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setContentView(R.layout.dialog);

            mDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(RegisterAcitivity.this, android.R.color.transparent));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(mDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;

            mDialog.getWindow().setAttributes(lp);
            mDialog.show();
        }
    }
}