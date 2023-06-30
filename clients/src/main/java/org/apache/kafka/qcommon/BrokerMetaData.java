package org.apache.kafka.qcommon;

public class BrokerMetaData {

    private boolean isStart = false;

    private boolean isAcl = false;

    private String clusterName;

    private String brokerName;

    private String brokerAddress;

    private Integer brokerPort;

    public boolean isStart() {
        return isStart;
    }

    public boolean isAcl() {
        return isAcl;
    }

    public void setStart(boolean start) {
        isStart = start;
    }

    public void setAcl(boolean acl) {
        isAcl = acl;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getBrokerAddress() {
        return brokerAddress;
    }

    public void setBrokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
    }

    public Integer getBrokerPort() {
        return brokerPort;
    }

    public void setBrokerPort(Integer brokerPort) {
        this.brokerPort = brokerPort;
    }


    @Override
    public String toString() {
        return "BrokerMetaData{" +
                "isStart=" + isStart +
                ", isAcl=" + isAcl +
                ", clusterName='" + clusterName + '\'' +
                ", brokerName='" + brokerName + '\'' +
                ", brokerAddress='" + brokerAddress + '\'' +
                ", brokerPort=" + brokerPort +
                '}';
    }
}
