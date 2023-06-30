package org.apache.kafka.common.qcommon;

import java.util.List;

public class ListConnections {

    public static final String CONNECTION_CLOSE_TYPE = "colse";

    public static final String CONNECTION_CURRENT_TYPE = "current";

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
