package com.aver.superdirector.utility;

public class AVerCallLog {
    public static final int STAT_OUT = 1;
    public static final int STAT_IN = 2;
    public static final int STAT_MISS = 3;

    public long Id;
    public String StartDateTime;
    public String EndDateTime;

    /*
     * if DurationSecond is -1: Call Fail
     * if DurationSecond is 0: Call Cancelled
     * if DurationSecond is +N: Duration Seconds
     */
    public int DurationSecond;
    public String SipH323;
    public String RemoteUri;
    public int CallStat;
    public String ContactName;

}