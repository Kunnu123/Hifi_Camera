package com.hificamera.thecamhi.activity.setting;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.control.HiCamera;
import com.hificamera.R;
import com.hificamera.thecamhi.base.HiToast;
import com.hificamera.thecamhi.base.HiTools;
import com.hificamera.thecamhi.base.TitleView;
import com.hificamera.thecamhi.bean.CamHiDefines;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.bean.MyCamera;
import com.hificamera.thecamhi.main.HiActivity;
import com.hificamera.thecamhi.utils.SystemUtils;
import com.hichip.tools.Packet;

import java.util.ArrayList;
import java.util.List;


public class FourGInfoActivity extends HiActivity implements ICameraIOSessionCallback {
    private MyCamera mCamera;
    private TextView tv_g4_ver, tv_g4_quality, tv_g4_status, tv_all_info;
    private EditText et_g4_sUser, et_g4_sPwd, et_g4_apn;
    private Spinner spAuthType, spRunMode;
    private Button bu_set_apn;
    private TextView tv_g4_Sname;
    private CamHiDefines.HI_P2P_GET_4GPARAM hi_p2P_get_4GPARAM;
    private CamHiDefines.HI_P2P_GET_4GPARAM_EXT hi_p2P_get_4GPARAM_ext;
    private LinearLayout llG4, llOperator;
    private ImageView ivShow;
    int i = 0;
    boolean isSupportG4_EXT, isSupportG5;
    private LinearLayout llImei, llIcCid, llRunMode, llRunModeExt;
    private ImageView ivImei, ivIcCid;
    private TextView tvImei, tvIcCid, tvRunMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fg_info);
        String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
        for (MyCamera camera : HiDataValue.CameraList) {
            if (uid.equals(camera.getUid())) {
                mCamera = camera;
                isSupportG5 = mCamera.commandFunction.getAppCmdFunction(CamHiDefines.HI_P2P_SUPPORT_5G);
                isSupportG4_EXT = mCamera.commandFunction.getAppCmdFunction(CamHiDefines.HI_P2P_GET_4GPARAM_EXT);
                if (isSupportG4_EXT || isSupportG5) {
                    mCamera.sendIOCtrl(CamHiDefines.HI_P2P_GET_4GPARAM_EXT, new byte[0]);
                } else {
                    mCamera.sendIOCtrl(CamHiDefines.HI_P2P_GET_4GPARAM, new byte[0]);
                }
                break;
            }
        }
        showjuHuaDialog();
        initView();
    }

    private void initView() {

        TitleView title = findViewById(R.id.title_top);
        String stitle = getString(R.string.g4_set);
        if (isSupportG5&&stitle.contains("4G")) {
            stitle=stitle.replace("4G","5G");
        }
        title.setTitle(stitle);
        title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        title.setNavigationBarButtonListener(which -> {
            if (which == TitleView.NAVIGATION_BUTTON_LEFT) {
                FourGInfoActivity.this.finish();
            }
        });

        tv_g4_ver = findViewById(R.id.tv_g4_ver);
        tv_g4_quality = findViewById(R.id.tv_g4_quality);
        et_g4_apn = findViewById(R.id.et_g4_apn);
        spRunMode = findViewById(R.id.sp_g4_runmode);
        tv_g4_status = findViewById(R.id.tv_g4_status);
        bu_set_apn = findViewById(R.id.tv_apn_set);
        tv_all_info = findViewById(R.id.tv_all_info);
        bu_set_apn.setOnClickListener(v -> {
            setAPN();
        });

        et_g4_sPwd = findViewById(R.id.et_g4_pwd);
        et_g4_sUser = findViewById(R.id.et_g4_username);
        llG4 = findViewById(R.id.ll_manual_model);
        llOperator = findViewById(R.id.ll_operator);
        tv_g4_Sname = findViewById(R.id.tv_g4_sname);
        spAuthType = findViewById(R.id.sp_g4_authtype);
        spAuthType.setDropDownVerticalOffset(HiTools.dip2px(this, 40));
        spRunMode.setDropDownVerticalOffset(HiTools.dip2px(this, 30));
        ivShow = findViewById(R.id.iv_show);

        llIcCid = findViewById(R.id.ll_iccid);
        llImei = findViewById(R.id.ll_imei);
        ivIcCid = findViewById(R.id.iv_iccid);
        ivImei = findViewById(R.id.iv_imei);
        tvIcCid = findViewById(R.id.tv_g4_iccid);
        tvImei = findViewById(R.id.tv_g4_imei);
        llRunMode = findViewById(R.id.ll_runmode);
        llRunModeExt = findViewById(R.id.ll_runmode_ext);
        tvRunMode = findViewById(R.id.tv_g4_runmode);
        if (isSupportG4_EXT||isSupportG5) {
            llIcCid.setVisibility(View.VISIBLE);
            llImei.setVisibility(View.VISIBLE);
            ivIcCid.setVisibility(View.VISIBLE);
            ivImei.setVisibility(View.VISIBLE);
            llRunModeExt.setVisibility(View.VISIBLE);
            llRunMode.setVisibility(View.GONE);
        } else {
            llRunModeExt.setVisibility(View.GONE);
            llRunMode.setVisibility(View.VISIBLE);
        }
        List<String> datas = new ArrayList<>();
        datas.add(getResources().getString(R.string.g4_apn_model_auto));
        datas.add(getResources().getString(R.string.g4_apn_model_manual));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, datas);
        spRunMode.setAdapter(adapter);
        spRunMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    llG4.setVisibility(View.VISIBLE);
                    ivShow.setVisibility(View.VISIBLE);
                    bu_set_apn.setVisibility(View.VISIBLE);
                } else {
                    llG4.setVisibility(View.GONE);
                    ivShow.setVisibility(View.GONE);
                    //                    bu_set_apn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void setAPN() {
        String anp = et_g4_apn.getText().toString().trim();
        String sUser = et_g4_sUser.getText().toString().trim();
        String sPwd = et_g4_sPwd.getText().toString().trim();
        if (anp.getBytes().length > 31) {
            HiToast.showToast(this, getString(R.string.g4_apn_name));
            return;
        }
        if (sUser.getBytes().length > 63) {
            HiToast.showToast(this, getString(R.string.tips_username_tolong));
            return;
        }
        if (sPwd.getBytes().length > 63) {
            HiToast.showToast(this, getString(R.string.tips_password_tolong));

            return;
        }
        Log.e("4g", "==" + spAuthType.getSelectedItemPosition());
        if (mCamera != null) {
            showjuHuaDialog();
            if (isSupportG4_EXT||isSupportG5) {
                CamHiDefines.HI_P2P_SET_4GAPN_EXT hi_p2P_set_4GAPN_ext = new CamHiDefines.HI_P2P_SET_4GAPN_EXT();
                mCamera.sendIOCtrl(CamHiDefines.HI_P2P_SET_4GAPN_EXT, hi_p2P_set_4GAPN_ext.parseContent(anp, sUser, sPwd, spAuthType.getSelectedItemPosition(), spRunMode.getSelectedItemPosition()));
            } else {
                CamHiDefines.HI_P2P_SET_4GAPN hi_p2P_set_4GAPN = new CamHiDefines.HI_P2P_SET_4GAPN();
                mCamera.sendIOCtrl(CamHiDefines.HI_P2P_SET_4GAPN, hi_p2P_set_4GAPN.parseContent(anp, sUser, sPwd, spAuthType.getSelectedItemPosition()));
            }
        }
    }


    @Override
    public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {
        if (arg0 != mCamera)
            return;

        Bundle bundle = new Bundle();
        bundle.putByteArray(HiDataValue.EXTRAS_KEY_DATA, arg2);
        Message msg = handler.obtainMessage();
        msg.what = HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL;
        msg.obj = arg0;
        msg.arg1 = arg1;
        msg.arg2 = arg3;
        msg.setData(bundle);
        handler.sendMessage(msg);

    }

    @SuppressLint("HandlerLeak") private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
                    if (msg.arg2 == 0) {

                        Bundle bundle = msg.getData();
                        byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);
                        switch (msg.arg1) {
                            case CamHiDefines.HI_P2P_GET_4GPARAM:
                                if (data == null)
                                    return;
                                hi_p2P_get_4GPARAM = new CamHiDefines.HI_P2P_GET_4GPARAM(data);
                                Log.e("4g", hi_p2P_get_4GPARAM.toString());
                                String ver = Packet.getString(hi_p2P_get_4GPARAM.s4GVersion);
                                String apn = Packet.getString(hi_p2P_get_4GPARAM.s4G_APN);
                                tv_g4_ver.setText(ver);
                                et_g4_apn.setText(apn);
                                String s = Packet.getString(hi_p2P_get_4GPARAM.sName);
                                String sName = TextUtils.isEmpty(s) ? getString(R.string.none) : s;
                                if (SystemUtils.isZh(FourGInfoActivity.this)) {
                                    if (sName.contains("CHINA MOBILE")) {
                                        sName = getString(R.string.cmcc);
                                    } else if (sName.contains("CHN-UNICOM")) {
                                        sName = getString(R.string.cucc);
                                    } else if (sName.contains("CHN-CT")) {
                                        sName = getString(R.string.ctcc);
                                    } else {
                                        sName = getString(R.string.none);
                                    }
                                }
                                tv_g4_Sname.setText(sName);
                                tv_g4_quality.setText(hi_p2P_get_4GPARAM.s324GSignal + "");
                                tv_g4_status.setText(hi_p2P_get_4GPARAM.s324GStatus + "");
                                if (hi_p2P_get_4GPARAM.s324GRunMode == 1) {
                                    bu_set_apn.setVisibility(View.VISIBLE);
                                    ivShow.setVisibility(View.VISIBLE);
                                    tvRunMode.setText(R.string.g4_apn_model_manual);
                                    llG4.setVisibility(View.VISIBLE);
                                    et_g4_sUser.setText(Packet.getString(hi_p2P_get_4GPARAM.sUser));
                                    et_g4_sPwd.setText(Packet.getString(hi_p2P_get_4GPARAM.sPwd));
                                    int index = hi_p2P_get_4GPARAM.s32AuthType;
                                    if (index > 3 || index < 0)
                                        index = 3;
                                    if (spAuthType != null) {
                                        spAuthType.setSelection(index);
                                    }
                                } else {
                                    tvRunMode.setText(R.string.g4_apn_model_auto);
                                }
                                dismissjuHuaDialog();
                                break;
                            case CamHiDefines.HI_P2P_GET_4GPARAM_EXT:
                                if (data == null)
                                    return;
                                bu_set_apn.setVisibility(View.VISIBLE);
                                hi_p2P_get_4GPARAM_ext = new CamHiDefines.HI_P2P_GET_4GPARAM_EXT(data);
                                Log.e("4g", "EXT:" + hi_p2P_get_4GPARAM_ext.toString());
                                String verExt = Packet.getString(hi_p2P_get_4GPARAM_ext.s4GVersion);
                                String apnEXT = Packet.getString(hi_p2P_get_4GPARAM_ext.s4G_APN);
                                String ccidEXT = Packet.getString(hi_p2P_get_4GPARAM_ext.sSIM_ICCID);
                                String iemeEXT = Packet.getString(hi_p2P_get_4GPARAM_ext.sSIM_IMEI);

                                tv_g4_ver.setText(verExt);
                                et_g4_apn.setText(apnEXT);
                                tvIcCid.setText(ccidEXT);
                                tvImei.setText(iemeEXT);
                                String sExt = Packet.getString(hi_p2P_get_4GPARAM_ext.sName);
                                String sNameExt = TextUtils.isEmpty(sExt) ? getString(R.string.none) : sExt;
                                if (SystemUtils.isZh(FourGInfoActivity.this)) {
                                    if (sNameExt.contains("CHINA MOBILE")) {
                                        sNameExt = getString(R.string.cmcc);
                                    } else if (sNameExt.contains("CHN-UNICOM")) {
                                        sNameExt = getString(R.string.cucc);
                                    } else if (sNameExt.contains("CHN-CT")) {
                                        sNameExt = getString(R.string.ctcc);
                                    } else {
                                        sNameExt = getString(R.string.none);
                                    }
                                }
                                tv_g4_Sname.setText(sNameExt);

                                tv_g4_quality.setText(hi_p2P_get_4GPARAM_ext.s324GSignal + "");
                                tv_g4_status.setText(hi_p2P_get_4GPARAM_ext.s324GStatus + "");
                                et_g4_sUser.setText(Packet.getString(hi_p2P_get_4GPARAM_ext.sUser));
                                et_g4_sPwd.setText(Packet.getString(hi_p2P_get_4GPARAM_ext.sPwd));
                                int index = hi_p2P_get_4GPARAM_ext.s32AuthType;
                                if (index > 3||index<0)
                                    index = 3;
                                if (spAuthType != null) {
                                    spAuthType.setSelection(index);
                                }
                                if (hi_p2P_get_4GPARAM_ext.s324GRunMode == 1) {
                                    bu_set_apn.setVisibility(View.VISIBLE);
                                    ivShow.setVisibility(View.VISIBLE);
                                    spRunMode.setSelection(1);
                                    llG4.setVisibility(View.VISIBLE);
                                } else {
                                    spRunMode.setSelection(0);
                                }
                                dismissjuHuaDialog();
                                break;
                            case CamHiDefines.HI_P2P_SET_4GAPN:
                            case CamHiDefines.HI_P2P_SET_4GAPN_EXT:
                                dismissjuHuaDialog();
                                HiToast.showToast(FourGInfoActivity.this, getString(R.string.application_success));
                                break;

                        }
                    } else {
                        switch (msg.arg1) {
                            case CamHiDefines.HI_P2P_SET_4GAPN:
                            case CamHiDefines.HI_P2P_SET_4GAPN_EXT:
                                dismissjuHuaDialog();
                                HiToast.showToast(FourGInfoActivity.this, getString(R.string.application_fail));
                                break;
                            case CamHiDefines.HI_P2P_GET_4GPARAM:
                            case CamHiDefines.HI_P2P_GET_4GPARAM_EXT:
                                Log.e("4g", "get 4g param error");
                                dismissjuHuaDialog();
                                break;
                        }
                    }
                    break;

            }
        }
    };

    @Override
    public void receiveSessionState(HiCamera arg0, int arg1) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera != null) {
            mCamera.registerIOSessionListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.unregisterIOSessionListener(this);
        }
    }

}
