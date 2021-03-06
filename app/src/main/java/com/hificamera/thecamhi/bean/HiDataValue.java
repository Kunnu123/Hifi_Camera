package com.hificamera.thecamhi.bean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;
public class HiDataValue {
	public static boolean shareIsOpen=false;
	public final static boolean isDebug=false;
	public final static int DEFAULT_VIDEO_QUALITY = 1;
	public final static int DEFAULT_ALARM_STATE = 0;
	public final static int DEFAULT_PUSH_STATE = 0;

	public final static int NOTICE_RUNNING_ID = 20001;
	public final static int NOTICE_ALARM_ID = 20002;
	public final static int REQUEST_CODE_ASK_PERMISSON=20003;
	
	public static int ANDROID_VERSION=0;


	public final static String EXTRAS_KEY_UID = "uid";
	public final static String EXTRAS_KEY_DATA = "data";
	public final static String EXTRAS_KEY_DATA_OLD = "data_old";
	public final static String EXTRAS_RF_TYPE = "rfType";



	public final static String ACTION_CAMERA_INIT_END= "camera_init_end";

	public final static String CAMERA_OLD_ALARM_ADDRESS= "49.213.12.136";
	public final static String CAMERA_ALARM_ADDRESS_233= "47.91.149.233";
	public final static String CAMERA_ALARM_ADDRESS_WTU_122= "47.75.171.122";
	public static final String CAMERA_ALARM_ADDRESS_XYZ_173 = "47.90.64.173";
	public static final String CAMERA_ALARM_ADDRESS_AACC_148 = "47.56.201.148";
	public static final String CAMERA_ALARM_ADDRESS_SSAA_161 = "47.52.128.161";

	public final static String CAMERA_ALARM_ADDRESS_DERICAM_148    = "52.52.228.148";
    public final static String CAMERA_ALARM_ADDRESS_FDT_221="52.8.148.221";
//    public final static String NEW_ALARM_ADDRESS="47.75.91.181";

	public final static int HANDLE_MESSAGE_SESSION_STATE = 0x90000001;
	public final static int HANDLE_MESSAGE_RECEIVE_IOCTRL = 0x90000003;
	public final static int HANDLE_MESSAGE_SCAN_RESULT = 0x90000005;
	public final static int HANDLE_MESSAGE_SCAN_CHECK = 0x90000006;
	public final static int HANDLE_MESSAGE_DOWNLOAD_STATE=0x90000007;
	public final static int HANDLE_MESSAGE_DELETE_FILE=0X10001;
	
	public final static int HANDLE_MESSAGE_PLAY_STATE = 0x80000001;
	public final static int HANDLE_MESSAGE_PROGRESSBAR_RUN = 0x80000002;

	public static List<MyCamera> CameraList = new ArrayList<MyCamera>();
	public static String[] zifu={"&","'","~","*","(",")","/","\"","%","!",":",";",".","<",">",",","'"};

	public static boolean isOnLiveView = false;
	public static final String STYLE="style";

	public static String XGToken;
	public static String NewPushToken;
	public static  String FcmToken ;
	public static final String limit[] = {"FDTAA","DEAA","AAES"};
	
	public static final String[] SUBUID={"XXXX","YYYY","ZZZZ","SSSS","MMMM"};
	public static final String[] SUBUID_WTU={"WWWW","TTTT","UUUU"};
	public static final String[] SUBUID_P={"PPPP"};
	public static final String[] SUBUID_AACC={"AACC"};
	public static final String[] SUBUID_SSAA={"SSAA"};
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	//The location where the remote video is saved
	public static final String ONLINE_VIDEO_PATH=Environment.getExternalStorageDirectory().getAbsolutePath()+"/download/";
	//Local video saved location
	public static final String LOCAL_VIDEO_PATH=Environment.getExternalStorageDirectory().getAbsolutePath() +"/VideoRecoding/";
	public static final String LOCAL_CONVERT_PATH=Environment.getExternalStorageDirectory().getAbsolutePath() +"/Convert/";
	
	public static final String FILEPROVIDER="com.hichip.fileprovider"; 
	
	public static final String DB_NAME="HiChipCamera.db";
	public static final String company = "hichip";
	public static  String HuaWeiToken = "";
	public static  String VivoToken = "";
	public static  String OppoToken = "";
	public static  String XiaoMiToke = "";
	public static  String MeiZuToke = "";
	public static  String JpushToke = "";

	public static  String huaweiAppid = "100303331";

	
}
