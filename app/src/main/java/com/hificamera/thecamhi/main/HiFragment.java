package com.hificamera.thecamhi.main;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.hificamera.R;
import com.hificamera.hichip.progressload.DialogUtils;
import com.hificamera.thecamhi.utils.SystemUtils;

public class HiFragment extends Fragment{
	protected ProgressDialog progressDialog;
	HiActivity.MyDismiss myDismiss;
	public Dialog mJhLoading;
	
	public void showLoadingProgress(){
		progressDialog=new ProgressDialog(getActivity());
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(true);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setIcon(R.drawable.ic_launcher);
		progressDialog.setMessage(getText(R.string.tips_loading));
		
		progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {  
			  
            @Override  
            public void onDismiss(DialogInterface dialog) {  
              if(myDismiss!=null){
            	  myDismiss.OnDismiss();
              }
  
            }  
        }); 
		
		
		progressDialog.show();

	}
	
	public void dismissLoadingProgress() {
		if(progressDialog!=null) {
			progressDialog.cancel();
		}
	}
	
	public void dismissjuHuaDialog() {
		if (mJhLoading != null) {
			mJhLoading.dismiss();
		}
	}

	public void showjuHuaDialog() {
		if(getContext()==null)return;
		if (mJhLoading == null) {
			mJhLoading = DialogUtils.createLoadingDialog(getContext(), true,true);
		}
		mJhLoading.show();
	}
	
	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);		
		getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	public void  jumpToAgreement(){
		Intent intent = new Intent(getActivity(), WebViewActivity.class);
		intent.putExtra("title", getResources().getString(R.string.about_user_agreement));
		if (SystemUtils.isZh(getActivity())) {
			intent.putExtra("webUrl", "http://www.hichip.org/service_ch.html");
		} else {
			intent.putExtra("webUrl", "http://www.hichip.org/service_en.html");
		}

		startActivity(intent);

	}
	public void  jumpToPrivacy(){
		Intent intent = new Intent(getActivity(), WebViewActivity.class);
		intent.putExtra("title", getResources().getString(R.string.about_user_privacy));
		if (SystemUtils.isZh(getActivity())) {
			intent.putExtra("webUrl", "http://www.hichip.org/privacy_ch.html");
		} else {
			intent.putExtra("webUrl", "http://www.hichip.org/privacy_en.html");
		}
		startActivity(intent);
	}
	
	
}




