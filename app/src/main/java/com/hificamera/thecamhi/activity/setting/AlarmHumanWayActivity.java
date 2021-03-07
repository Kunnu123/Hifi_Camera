package com.hificamera.thecamhi.activity.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hificamera.R;
import com.hificamera.thecamhi.base.HiToast;
import com.hificamera.thecamhi.base.TitleView;
import com.hificamera.thecamhi.main.HiActivity;

public class AlarmHumanWayActivity extends HiActivity implements View.OnClickListener {
    private RelativeLayout rlAlone, rlJoint;
    private ImageView ivAlone, ivJoint;
    private int index = 0;
    private boolean isOpenMotion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_human_way);
        initView();
    }

    private void initView() {
        index = getIntent().getIntExtra("index", 0);
        isOpenMotion = getIntent().getBooleanExtra("isOpenMotion", false);
        TitleView title = (TitleView) findViewById(R.id.title_top);
        title.setTitle(getResources().getString(R.string.human_trigger_mode));
        title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        title.setNavigationBarButtonListener(new TitleView.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        AlarmHumanWayActivity.this.finish();
                        break;
                }

            }
        });
        rlAlone = findViewById(R.id.rl_alone);
        rlJoint = findViewById(R.id.rl_joint);
        ivAlone = findViewById(R.id.iv_alone);
        ivJoint = findViewById(R.id.iv_joint);
        if (index == 0) {
            ivAlone.setVisibility(View.VISIBLE);
            ivJoint.setVisibility(View.GONE);
        } else {
            ivAlone.setVisibility(View.GONE);
            ivJoint.setVisibility(View.VISIBLE);
        }
        rlJoint.setOnClickListener(this);
        rlAlone.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_alone:
                ivAlone.setVisibility(View.VISIBLE);
                ivJoint.setVisibility(View.GONE);
                setBundleData(0);
                break;
            case R.id.rl_joint:
                if (!isOpenMotion) {
                    HiToast.showToast(this, getResources().getString(R.string.open_the_motion_detection));
                    return;
                }
                ivAlone.setVisibility(View.GONE);
                ivJoint.setVisibility(View.VISIBLE);
                setBundleData(1);
                break;

        }
    }

    private void setBundleData(int index) {
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }
}
