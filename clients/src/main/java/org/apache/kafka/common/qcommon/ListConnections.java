package org.apache.kafka.common.qcommon;

import java.util.List;

public class ListConnections {

    private String connectionType;

    private List<String> brokerList;


    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public List<String> getBrokerList() {
        return brokerList;
    }

    public void setBrokerList(List<String> brokerList) {
        this.brokerList = brokerList;
    }
}
