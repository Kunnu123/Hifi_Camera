package com.hificamera.thecamhi.main;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hificamera.R;
import com.hichip.sdk.HiChipSDK;
import com.hificamera.thecamhi.base.TitleView;
import com.hificamera.thecamhi.bean.HiDataValue;

public class AboutFragment extends HiFragment {
    private View view;
    private int mClickNum;
    private TextView tvUserAgreement, tvPrivacyAgreement;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_about, null);
        initView();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mClickNum = 0;
    }

    private void initView() {
        TitleView title = (TitleView) view.findViewById(R.id.title_top);
        title.setTitle(getResources().getString(R.string.title_about_fragment));

        PackageManager manager = getActivity().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getActivity().getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = " ";
        if (info != null) {
            version = info.versionName;
        }

        TextView app_version_tv = (TextView) view.findViewById(R.id.app_version_tv);
        app_version_tv.setText(version);
        app_version_tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickNum++;
                if (mClickNum >= 10 && mClickNum <= 15) {
                    HiDataValue.shareIsOpen = true;
                }
            }
        });

        TextView txt_SDK_version = (TextView) view.findViewById(R.id.txt_SDK_version);
        txt_SDK_version.setText(HiChipSDK.getSDKVersion());

        tvPrivacyAgreement = view.findViewById(R.id.tv_privacy_agreement);
        tvUserAgreement = view.findViewById(R.id.tv_user_agreement);
        tvUserAgreement.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               jumpToAgreement();
            }
        });
        tvPrivacyAgreement.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              jumpToPrivacy();
            }
        });

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
          mClickNum=0;
        }
    }
}
