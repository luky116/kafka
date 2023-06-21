package org.apache.kafka.qcommon;

import org.apache.kafka.qcommon.monitor.ConnectMonitor;

public class QCommonManager {

    private static final  QCommonManager INSTANCE = new QCommonManager();

    public static final QCommonManager getInstance(){
        return INSTANCE;
    }

    private QCommonManager(){}

    private boolean isStart = false;

    private boolean isAcl = false;


    public void init(){

    }

    public ConnectMonitor getConnectMonitor(){
        return null;
    }

}
