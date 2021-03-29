package com.hificamera.thecamhi.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hichip.base.HiLog;
import com.hichip.sdk.HiChipSDK;
import com.hificamera.R;
import com.hificamera.thecamhi.base.TitleView;
import com.hificamera.thecamhi.bean.BlackUid;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.utils.PactDialogUtils;
import com.hificamera.thecamhi.utils.SharePreUtils;
import com.hificamera.thecamhi.utils.SystemUtils;

import com.huawei.hms.aaid.HmsInstanceId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static com.hificamera.thecamhi.utils.UidConfigUtil.blackUidMap;

public class SplashActivity extends HiActivity {
    private TextView tvUserAgreement, tvPrivacyAgreement;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        setContentView(R.layout.activity_splash);
        initView();
        isShowAgreement();
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startMainActivity();
//            }
//        }, 2000);
    }
    @SuppressLint("StringFormatInvalid")
    private void isShowAgreement() {
        boolean isShow = SharePreUtils.getBoolean("isfshow", this, "isfshow");
        if (!isShow) {
            String content = getString(R.string.sj_content);
            String msg = String.format(content, getString(R.string.app_name)); //+ getString(R.string.about_user_agreement) + getString(R.string.about_and) + getString(R.string.about_user_privacy);
            new PactDialogUtils(this).title(getString(R.string.tip_hint)).message(msg).cancelText(getString(R.string.sj_disagree)).sureText(getString(R.string.sj_agree)).setCancelable(false).setCancelOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SplashActivity.this.finish();
//                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }).setSureOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startMainActivity();
                        }
                    }, 2000);
                    SharePreUtils.putBoolean("isfshow", SplashActivity.this, "isfshow", true);
                }
            }).setAgreementClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    jumpToAgreement();
                }
            }).setPrivacyClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    jumpToPrivacy();
                }
            }).build().show();
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startMainActivity();
                }
            }, 2000);
        }
    }
    private void initView() {

        PackageManager manager = getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = " ";
        if (info != null) {
            version = info.versionName;
        }

        TextView app_version_tv = (TextView) findViewById(R.id.app_version_tv);
        app_version_tv.setText(version);

        TextView txt_SDK_version = (TextView) findViewById(R.id.txt_SDK_version);
        txt_SDK_version.setText(HiChipSDK.getSDKVersion());

        tvPrivacyAgreement = (TextView)findViewById(R.id.tv_privacy_agreement);
        tvUserAgreement = (TextView) findViewById(R.id.tv_user_agreement);
        tvUserAgreement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToAgreement();
            }
        });
        tvPrivacyAgreement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToPrivacy();
            }
        });


        if (SystemUtils.isHuaweiMoblie(this)) {
            HmsInstanceId inst = HmsInstanceId.getInstance(this);
            getToken();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                initBlackUidMap();
            }
        }).start();
    }

    public void initBlackUidMap() {


        StringBuilder builder = new StringBuilder();
        InputStream inputStream = null;
        try {
            inputStream = getResources().getAssets().open("blackUid.json");
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(isr);
            String jsonLine;
            while ((jsonLine = reader.readLine()) != null) {
                builder.append(jsonLine);
            }
            reader.close();
            isr.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String result = builder.toString();
        //Log.d("==json", result);
        Gson gson = new Gson();
        BlackUid blackUid = gson.fromJson(result, BlackUid.class);

       HiLog.e("==map"+ "Start: Initialize the map" + blackUid.getUid().size());

        for (List<String> strings : blackUid.getUid()) {
            blackUidMap.put(strings.get(0), new String[]{strings.get(1), strings.get(2)});
        }

        for (String key : blackUidMap.keySet()) {
            String[] a = blackUidMap.get(key);
           //HiLog.e("==map", key + "--" + Arrays.toString(a));
        }
    }

    @SuppressLint("HandlerLeak") private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        }
    };

    private void getToken() {
        new Thread() {
            @Override
            public void run() {
                try {
                    String getToken = HmsInstanceId.getInstance(SplashActivity.this).getToken(HiDataValue.huaweiAppid, "HCM");
                    if (!TextUtils.isEmpty(getToken)) {
                       HiLog.e("==push"+"Huawei getToken su::" + getToken);
                        HiDataValue.HuaWeiToken = getToken;
                        SharePreUtils.putString("NewPushToken", getApplicationContext(), "pushtoken", getToken);
                    }
                } catch (Exception e) {
                   HiLog.e("==push"+ "Huawei getToken failed."+ e);
                }
            }
        }.start();
    }


    private void startMainActivity() {
        boolean isLogin = SharePreUtils.getBoolean("isfshow", this, "isLogin");
        if (isLogin){
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }else {
            Intent intent = new Intent(SplashActivity.this, LoginAcitivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }


}
