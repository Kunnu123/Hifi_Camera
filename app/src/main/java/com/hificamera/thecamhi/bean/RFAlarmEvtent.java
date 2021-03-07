package com.hificamera.thecamhi.bean;


public class RFAlarmEvtent {
    private int typeNum;
    private String timezone;
    private String code;
    public String type;
    private String name;
    private int isHaveRecord;


    public RFAlarmEvtent(int typeNum, String timezone, String code, String type, String name, int isHaveRecord) {
        super();
        this.typeNum = typeNum;
        this.timezone = timezone;
        this.code = code;
        this.type = type;
        this.name = name;
        this.isHaveRecord = isHaveRecord;
    }


    public int getTypeNum() {
        return typeNum;
    }


    public void setTypeNum(int typeNum) {
        this.typeNum = typeNum;
    }


    public String getTimezone() {
        return timezone;
    }


    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }


    public String getCode() {
        return code;
    }


    public void setCode(String code) {
        this.code = code;
    }


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public int getIsHaveRecord() {
        return isHaveRecord;
    }


    public void setIsHaveRecord(int isHaveRecord) {
        this.isHaveRecord = isHaveRecord;
    }


}



