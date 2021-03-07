package com.hificamera.thecamhi.bean;

import android.util.Log;

import com.hichip.tools.Packet;

import java.util.Arrays;

public class CamHiDefines {


    public static final int HI_P2P_ALARM_TOKEN_REGIST = 0x00004132;
    public static final int HI_P2P_ALARM_TOKEN_UNREGIST = 0x00004133;
    public static final int HI_P2P_GET_OSD_PARAM = 0x00003109;
    public static final int HI_P2P_SET_OSD_PARAM = 0x00003110;

    public static final int HI_P2P_ALARM_ADDRESS_GET = 0x0000415e;
    public static final int HI_P2P_ALARM_ADDRESS_SET = 0x0000415f;


    public static final int HI_P2P_CUSTOM_ALARM = 0x0000418c;
    public static final int HI_P2P_GET_SMART_HSR_PARAM = 0x0000419b;
    public static final int HI_P2P_SET_SMART_HSR_PARAM = 0x0000419c;
    public static final int HI_P2P_ALARM_HSR = 0x0000f006;
    public static final int HI_P2P_SUPPORT_WIFICHECK = 0x000041a0;
    public static final int HI_P2P_SUPPORT_FTPUPLOAD = 0x000041a1;
    public static final int HI_P2P_SDK_TOKEN_LEN = 68;

    public static final int HI_P2P_SUPPORT_4G = 0x000041a2;
    public static final int HI_P2P_GET_4GPARAM = 0x000041a3;
    public static final int HI_P2P_SET_4GAPN = 0x000041a4;
    public static final int HI_P2P_GET_SIGNAL_OPERATOR = 0x000041a5;
    public static final int HI_P2P_GET_SIGNAL_OPERATOR_EXT = 0x000041a6;
    public static final int HI_P2P_GET_4GPARAM_EXT = 0x000041aa;
    public static final int HI_P2P_SET_4GAPN_EXT = 0x000041ab;
    public static final int HI_P2P_SUPPORT_5G =  0x000041ae;

    /******HI_P2P_ALARM_TOKEN_REGIST******/
    public static class HI_P2P_ALARM_TOKEN_INFO {
        int u32Chn;
        byte[] szTokenId = new byte[HI_P2P_SDK_TOKEN_LEN];
        int u32UtcTime;
        byte s8Enable;
        public byte sReserved[] = new byte[3];


        public static byte[] parseContent(int u32Chn, int szTokenId, int u32UtcTime, int enable) {
            byte[] info = new byte[77];

            byte[] bChannel = Packet.intToByteArray_Little(u32Chn);
            byte[] bToken = Packet.intToByteArray_Little(szTokenId);
            byte[] bUtcTime = Packet.intToByteArray_Little(u32UtcTime);
            byte[] bEnable = Packet.intToByteArray_Little(enable);


            System.arraycopy(bChannel, 0, info, 0, 4);
            System.arraycopy(bToken, 0, info, 4, 4);
            System.arraycopy(bUtcTime, 0, info, 72, 4);
            System.arraycopy(bEnable, 0, info, 76, 1);


            return info;
        }

        public HI_P2P_ALARM_TOKEN_INFO(byte[] byt) {
            u32Chn = Packet.byteArrayToInt_Little(byt, 0);
            System.arraycopy(byt, 0, szTokenId, 4, HI_P2P_SDK_TOKEN_LEN);
            u32UtcTime = Packet.byteArrayToInt_Little(byt, 72);
            s8Enable = byt[76];

        }

    }
    public static class HI_P2P_S_OSD {
        int u32Chn;
        int u32EnTime;
        int u32EnName;
        int u32PlaceTime;
        int u32PlaceName;
        public byte[] strName = new byte[64];

        public static byte[] parseContent(int u32Chn, int u32EnTime, int u32EnName, int u32PlaceTime, int u32PlaceName, String name) {
            byte[] osd = new byte[84];

            byte[] bChn = Packet.intToByteArray_Little(u32Chn);
            byte[] bEnTime = Packet.intToByteArray_Little(u32EnTime);
            byte[] bEnName = Packet.intToByteArray_Little(u32EnName);
            byte[] bPlaceTime = Packet.intToByteArray_Little(u32PlaceTime);
            byte[] bPlaceName = Packet.intToByteArray_Little(u32PlaceName);
            byte[] bName = name.getBytes();

            System.arraycopy(bChn, 0, osd, 0, 4);
            System.arraycopy(bEnTime, 0, osd, 4, 4);
            System.arraycopy(bEnName, 0, osd, 8, 4);
            System.arraycopy(bPlaceTime, 0, osd, 12, 4);
            System.arraycopy(bPlaceName, 0, osd, 16, 4);
            System.arraycopy(bName, 0, osd, 20, bName.length > 64 ? 64 : bName.length);
            return osd;
        }


        public HI_P2P_S_OSD(byte[] byt) {
            u32Chn = Packet.byteArrayToInt_Little(byt, 0);
            u32EnTime = Packet.byteArrayToInt_Little(byt, 4);
            u32EnName = Packet.byteArrayToInt_Little(byt, 8);
            u32PlaceTime = Packet.byteArrayToInt_Little(byt, 12);
            u32PlaceName = Packet.byteArrayToInt_Little(byt, 16);

            System.arraycopy(byt, 20, strName, 0, strName.length > 64 ? 64 : strName.length);


        }


    }


    public static class HI_P2P_ALARM_ADDRESS {
        public byte[] szAlarmAddr = new byte[32];
        byte[] sReserved = new byte[4];

        public static byte[] parseContent(String szAlarmAddr) {
            byte[] addr = new byte[32];

            byte[] bAlarmAddr = szAlarmAddr.getBytes();


            System.arraycopy(bAlarmAddr, 0, addr, 0, bAlarmAddr.length > 32 ? 32 : bAlarmAddr.length);

            return addr;
        }

        public HI_P2P_ALARM_ADDRESS(byte[] data) {
            System.arraycopy(data, 0, szAlarmAddr, 0, data.length > 32 ? 32 : data.length);
        }

    }


    //
    public static class HI_P2P_VIDEO_IMAGE_PARAM_SCOPE {
        int profile;
        public byte[] resolution = new byte[32];
        byte[] brightness = new byte[16];
        byte[] saturation = new byte[16];
        byte[] contrast = new byte[16];
        byte[] hue = new byte[16];
        byte[] u8Reserve = new byte[4];

        public HI_P2P_VIDEO_IMAGE_PARAM_SCOPE(byte[] byt) {
            if (byt.length >= 36) {
                System.arraycopy(byt, 4, resolution, 0, resolution.length > 32 ? 32 : resolution.length);
            }
        }
    }

    public static class HI_P2P_GET_SMART_HSR_PARAM {
        public int u32HSRenable;
        public int u32DrawRect;
        public int u32Link;

        public byte[] parseContent() {
            byte[] result = new byte[16];
            byte[] u32HSRenable = Packet.intToByteArray_Little(this.u32HSRenable);
            byte[] u32DrawRect = Packet.intToByteArray_Little(this.u32DrawRect);
            byte[] u32Link = Packet.intToByteArray_Little(this.u32Link);
            System.arraycopy(u32HSRenable, 0, result, 0, 4);
            System.arraycopy(u32DrawRect, 0, result, 4, 4);
            System.arraycopy(u32Link, 0, result, 8, 4);
            return result;
        }

        public HI_P2P_GET_SMART_HSR_PARAM(byte[] byt) {
            if (byt.length >= 16) {
                this.u32HSRenable = Packet.byteArrayToInt_Little(byt, 0);
                this.u32DrawRect = Packet.byteArrayToInt_Little(byt, 4);
                this.u32Link = Packet.byteArrayToInt_Little(byt, 8);
            }

        }
    }
    //    typedef struct
    //    {
    //        HI_U32 u32Type;
    //        HI_U32 u32Num;
    //        HI_P2P_ALARM_MD hsr[0];
    //    }HI_P2P_SMART_HSR_AREA;

    public static class HI_P2P_SMART_HSR_AREA {
        public int u32Type;
        public int u32Num;

        public HI_P2P_SMART_HSR_AREA(byte[] byt) {
            if (byt.length >= 8) {
                this.u32Type = Packet.byteArrayToInt_Little(byt, 0);
                this.u32Num = Packet.byteArrayToInt_Little(byt, 4);
            }
        }
    }


    //    typedef struct
    //    {
    //        HI_U32 u32Area;
    //        HI_U32 u32X;
    //        HI_U32 u32Y;
    //        HI_U32 u32Width;
    //        HI_U32 u32Height;
    //    } HI_P2P_ALARM_MD;
    public static class HI_P2P_ALARM_MD {
        public int u32Area;
        public int u32X;
        public int u32Y;
        public int u32Width;
        public int u32Height;

        public HI_P2P_ALARM_MD(byte[] byt) {
            if (byt.length >= 20) {
                this.u32Area = Packet.byteArrayToInt_Little(byt, 0);
                this.u32X = Packet.byteArrayToInt_Little(byt, 4);
                this.u32Y = Packet.byteArrayToInt_Little(byt, 8);
                this.u32Width = Packet.byteArrayToInt_Little(byt, 12);
                this.u32Height = Packet.byteArrayToInt_Little(byt, 16);
            }
        }

        @Override
        public String toString() {
            return "HI_P2P_ALARM_MD{" + ", u32Area=" + u32Area + ", u32X=" + u32X + ", u32Y=" + u32Y + ", u32Width=" + u32Width + ", u32Height=" + u32Height + '}';
        }
    }

    public static class HI_P2P_GET_4GPARAM {
        public int s324GRunMode;
        public int s324GHadSimCard;
        public int s324GStatus;
        public int s324GSignal;
        public byte[] s4GVersion = new byte[32];
        public byte[] s4G_MCC = new byte[16];
        public byte[] s4G_MNC = new byte[16];
        public byte[] s4G_APN = new byte[32];
        public byte[] sName = new byte[64];
        public byte[] sUser = new byte[64];
        public byte[] sPwd = new byte[64];
        public int s32AuthType;

        public HI_P2P_GET_4GPARAM(byte[] byt) {
            if (byt.length >= 308) {
                int pos = 0;
                this.s324GRunMode = Packet.byteArrayToInt_Little(byt, pos);
                pos = pos + 4;
                this.s324GHadSimCard = Packet.byteArrayToInt_Little(byt, pos);
                pos = pos + 4;
                this.s324GStatus = Packet.byteArrayToInt_Little(byt, pos);
                pos = pos + 4;
                this.s324GSignal = Packet.byteArrayToInt_Little(byt, pos);
                pos = pos + 4;
                System.arraycopy(byt, pos, this.s4GVersion, 0, 32);
                pos += 32;
                System.arraycopy(byt, pos, this.s4G_MCC, 0, 16);
                pos += 16;
                System.arraycopy(byt, pos, this.s4G_MNC, 0, 16);
                pos += 16;
                System.arraycopy(byt, pos, this.s4G_APN, 0, 32);
                pos += 32;
                System.arraycopy(byt, pos, this.sName, 0, 64);
                pos += 64;
                System.arraycopy(byt, pos, this.sUser, 0, 64);
                pos += 64;
                System.arraycopy(byt, pos, this.sPwd, 0, 64);
                pos += 64;
                this.s32AuthType = Packet.byteArrayToInt_Little(byt, pos);
                //System.arraycopy(byt, pos, Packet.intToByteArray_Little(this.s32AuthType), 0, Packet.intToByteArray_Little(this.s32AuthType).length);
                //pos += 4;

            }
        }

        @Override
        public String toString() {
            return "HI_P2P_GET_4GPARAM{" + "s324GRunMode=" + s324GRunMode + "\n" + ", s324GHadSimCard=" + s324GHadSimCard + "\n" + ", s324GStatus=" + s324GStatus + "\n" + ", s324GSignal=" + s324GSignal + "\n" + ", s4GVersion=" + Packet.getString(s4GVersion) + "\n" + ", s4G_MCC=" + Packet.getString(s4G_MCC) + "\n" + ", s4G_MNC=" + Packet.getString(s4G_MNC) + "\n" + ", s4G_APN=" + Packet.getString(s4G_APN) + "\n" + ", sName=" + Packet.getString(sName) + "\n" + ", sUser=" + Packet.getString(sUser) + "\n" + ", sPwd=" + Packet.getString(sPwd) + "\n" + ", s32AuthType=" + s32AuthType + '}';
        }
    }

    public static class HI_P2P_SET_4GAPN {
        byte[] s4G_APN = new byte[32];
        byte[] sUser = new byte[64];
        byte[] sPwd = new byte[64];
        int s32AuthType;

        public HI_P2P_SET_4GAPN() {
        }

        public byte[] parseContent(String apn, String sUser, String sPwd, int authType) {
            byte[] g4 = new byte[164];
            byte[] b_s4G_APN = apn.getBytes();
            byte[] b_sUser = sUser.getBytes();
            byte[] b_sPwd = sPwd.getBytes();
            Log.e("4g", "len===" + b_s4G_APN.length);
            byte[] b_s32AuthType = Packet.intToByteArray_Little(authType);
            System.arraycopy(b_s4G_APN, 0, g4, 0, Math.min(b_s4G_APN.length, 32));
            System.arraycopy(b_sUser, 0, g4, 32, Math.min(b_sUser.length, 64));
            System.arraycopy(b_sPwd, 0, g4, 96, Math.min(b_sPwd.length, 64));
            System.arraycopy(b_s32AuthType, 0, g4, 160, b_s32AuthType.length);

            return g4;
        }
    }

    public static class HI_P2P_SIGNAL_OPERATOR {
        public int s32ShowSignal;
        public int s32ShowName;
        public int s32SignalQuality;
        public int s32OperatorName;
        public byte[] sReserved = new byte[8];

        public HI_P2P_SIGNAL_OPERATOR(byte[] byt) {
            if (byt.length >= 24) {
                this.s32ShowSignal = Packet.byteArrayToInt_Little(byt, 0);
                this.s32ShowName = Packet.byteArrayToInt_Little(byt, 4);
                this.s32SignalQuality = Packet.byteArrayToInt_Little(byt, 8);
                this.s32OperatorName = Packet.byteArrayToInt_Little(byt, 12);
                //System.arraycopy(byt,16,this.sReserved,0,8);
            }
        }

        @Override
        public String toString() {
            return "HI_P2P_SIGNAL_OPERATOR{" + "s32ShowSignal=" + s32ShowSignal + ", s32ShowName=" + s32ShowName + ", s32SignalQuality=" + s32SignalQuality + ", s32OperatorName=" + s32OperatorName + ", sReserved=" + Packet.getString(sReserved) + '}';
        }
    }

    public static class HI_P2P_GET_SIGNAL_OPERATOR_EXT {
        public int s32ShowSignal;
        public int s32ShowName;
        public byte s32OperatorName[] = new byte[64];
        public int s32SignalQuality;
        public byte sReserved[] = new byte[12];

        public HI_P2P_GET_SIGNAL_OPERATOR_EXT(byte[] byt) {
            if (byt.length >= 88) {
                int pos = 0;
                this.s32ShowSignal = Packet.byteArrayToInt_Little(byt, pos);
                pos += 4;
                this.s32ShowName = Packet.byteArrayToInt_Little(byt, pos);
                pos += 4;
                System.arraycopy(byt, pos, this.s32OperatorName, 0, 64);
                pos += 64;
                this.s32SignalQuality = Packet.byteArrayToInt_Little(byt, pos);
                pos += 4;
                // System.arraycopy(byt, pos, this.sReserved, 0, 12);

            }
        }

        @Override
        public String toString() {
            return "HI_P2P_GET_SIGNAL_OPERATOR_EXT{" + "s32ShowSignal=" + s32ShowSignal + ", s32ShowName=" + s32ShowName + ", s32OperatorName=" + Packet.getString(s32OperatorName) + ", s32SignalQuality=" + s32SignalQuality + ", sReserved=" + Packet.getString(sReserved) + '}';
        }
    }

    public static class HI_P2P_GET_4GPARAM_EXT {
        public int s324GRunMode;
        public int s324GHadSimCard;
        public int s324GStatus;
        public int s324GSignal;
        public byte[] s4GVersion = new byte[32];
        public byte[] s4G_MCC = new byte[16];
        public byte[] s4G_MNC = new byte[16];
        public byte[] s4G_APN = new byte[32];
        public byte[] sName = new byte[64];
        public byte[] sUser = new byte[64];
        public byte[] sPwd = new byte[64];
        public int s32AuthType;
        public byte[] sSIM_IMEI = new byte[32];
        public byte[] sSIM_ICCID = new byte[32];

        public HI_P2P_GET_4GPARAM_EXT(byte[] byt) {
            if (byt.length >= 372) {
                int pos = 0;
                this.s324GRunMode = Packet.byteArrayToInt_Little(byt, pos);
                pos = pos + 4;
                this.s324GHadSimCard = Packet.byteArrayToInt_Little(byt, pos);
                pos = pos + 4;
                this.s324GStatus = Packet.byteArrayToInt_Little(byt, pos);
                pos = pos + 4;
                this.s324GSignal = Packet.byteArrayToInt_Little(byt, pos);
                pos = pos + 4;
                System.arraycopy(byt, pos, this.s4GVersion, 0, 32);
                pos += 32;
                System.arraycopy(byt, pos, this.s4G_MCC, 0, 16);
                pos += 16;
                System.arraycopy(byt, pos, this.s4G_MNC, 0, 16);
                pos += 16;
                System.arraycopy(byt, pos, this.s4G_APN, 0, 32);
                pos += 32;
                System.arraycopy(byt, pos, this.sName, 0, 64);
                pos += 64;
                System.arraycopy(byt, pos, this.sUser, 0, 64);
                pos += 64;
                System.arraycopy(byt, pos, this.sPwd, 0, 64);
                pos += 64;
                this.s32AuthType = Packet.byteArrayToInt_Little(byt, pos);
                pos += 4;
                System.arraycopy(byt, pos, this.sSIM_IMEI, 0, 32);
                pos+=32;
                System.arraycopy(byt, pos, this.sSIM_ICCID, 0, 32);
            }
        }

        @Override
        public String toString() {
            return "HI_P2P_GET_4GPARAM_EXT{" + "s324GRunMode=" + s324GRunMode
                    + ", s324GHadSimCard=" + s324GHadSimCard
                    + ", s324GStatus=" + s324GStatus
                    + ", s324GSignal=" + s324GSignal
                    + ", s4GVersion=" + Packet.getString(s4GVersion)
                    + ", s4G_MCC=" +Packet.getString(s4G_MCC)
                    + ", s4G_MNC=" + Packet.getString(s4G_MNC)
                    + ", s4G_APN=" + Packet.getString(s4G_APN)
                    + ", sName=" + Packet.getString(sName)
                    + ", sUser=" +Packet.getString(sUser)
                    + ", sPwd=" + Packet.getString(sPwd)
                    + ", s32AuthType=" + s32AuthType
                    + ", sSIM_IMEI=" + Packet.getString(sSIM_IMEI)
                    + ", sSIM_ICCID=" + Packet.getString(sSIM_ICCID) + '}';
        }
    }

    public static class  HI_P2P_SET_4GAPN_EXT {
        byte[] s4G_APN = new byte[32];
        byte[] sUser = new byte[64];
        byte[] sPwd = new byte[64];
        int s32AuthType;
        int s32RunMode;
        public  HI_P2P_SET_4GAPN_EXT() {
        }

        public byte[] parseContent(String apn, String sUser, String sPwd, int authType,int s32RunMode) {
            byte[] g4 = new byte[168];
            byte[] b_s4G_APN = apn.getBytes();
            byte[] b_sUser = sUser.getBytes();
            byte[] b_sPwd = sPwd.getBytes();
            Log.e("4g", "len===" + b_s4G_APN.length);
            byte[] b_s32AuthType = Packet.intToByteArray_Little(authType);
            byte[] b_s32RunMode = Packet.intToByteArray_Little(s32RunMode);
            System.arraycopy(b_s4G_APN, 0, g4, 0, Math.min(b_s4G_APN.length, 32));
            System.arraycopy(b_sUser, 0, g4, 32, Math.min(b_sUser.length, 64));
            System.arraycopy(b_sPwd, 0, g4, 96, Math.min(b_sPwd.length, 64));
            System.arraycopy(b_s32AuthType, 0, g4, 160, b_s32AuthType.length);
            System.arraycopy(b_s32RunMode, 0, g4, 164, b_s32RunMode.length);

            return g4;
        }
    }
}
