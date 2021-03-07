package com.hificamera.thecamhi.push;

import android.content.Context;

import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.utils.SharePreUtils;
import com.meizu.cloud.pushsdk.MzPushMessageReceiver;
import com.meizu.cloud.pushsdk.handler.MzPushMessage;
import com.meizu.cloud.pushsdk.platform.message.PushSwitchStatus;
import com.meizu.cloud.pushsdk.platform.message.RegisterStatus;
import com.meizu.cloud.pushsdk.platform.message.SubAliasStatus;
import com.meizu.cloud.pushsdk.platform.message.SubTagsStatus;
import com.meizu.cloud.pushsdk.platform.message.UnRegisterStatus;

public class MeiZuPushMsgReceiver extends MzPushMessageReceiver {
    @Override
    public void onRegisterStatus(Context context, RegisterStatus registerStatus) {
        HiDataValue.MeiZuToke = registerStatus.getPushId();
        SharePreUtils.putString("NewPushToken", context, "pushtoken", registerStatus.getPushId());

    }
    @Override
    public void onUnRegisterStatus(Context context, UnRegisterStatus unRegisterStatus) {

    }

    @Override
    public void onPushStatus(Context context, PushSwitchStatus pushSwitchStatus) {


    }

    @Override
    public void onSubTagsStatus(Context context, SubTagsStatus subTagsStatus) {

    }

    @Override
    public void onSubAliasStatus(Context context, SubAliasStatus subAliasStatus) {

    }

    @Override
    public void onNotificationClicked(Context context, MzPushMessage mzPushMessage) {
    }

    @Override
    public void onNotificationArrived(Context context, MzPushMessage mzPushMessage) {

    }

}
