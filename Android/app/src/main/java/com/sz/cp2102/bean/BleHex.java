package com.sz.cp2102.bean;

public class BleHex {
    //Start
    public static final String START = "5A8101000000000000000000000000";
    //Stop
    public static final String STOP =  "5A8100000000000000000000000000";
    //Auto-start voltage
    public static final String setV = "5A8501";
    //Output voltage
    public static final String setVMAX = "5A8201";
    //Auto-shutdown current
    public static final String setI = "5A8601";
    //Maximum current limit
    public static final String setIMAX = "5A8301";
    //end
    public static final String setEnd = "00000000000000000000";
}
