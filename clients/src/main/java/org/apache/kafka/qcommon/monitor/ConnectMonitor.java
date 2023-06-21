package org.apache.kafka.qcommon.monitor;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.qcommon.BrokerMetaData;

public class ConnectMonitor {

    private BrokerMetaData brokerMetaData;

    private Map<String,Connect>  connectMap = new ConcurrentHashMap<>();

    private List<Connect>  closeConnect = new ArrayList<>();

    public void connectEstablish(String id, SocketChannel channel){
        Connect connect = this.createConnect(id,channel);
        connectMap.put(id , connect);

    }

    private Connect createConnect(String id, SocketChannel channel)  {
        Connect connect = new Connect();
        connect.setChannelId(id);
        connect.setClusterName(brokerMetaData.getClusterName());
        connect.setBrokerName(brokerMetaData.getBrokerName());
        connect.setBrokerAddress(brokerMetaData.getBrokerAddress());
        connect.setBrokerPort(brokerMetaData.getBrokerPort());
        try {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) channel.getRemoteAddress();
            connect.setClientAddress(inetSocketAddress.getAddress().getHostAddress());
            connect.setClientAddress(inetSocketAddress.getPort() + "");
        }catch (Exception e){

        }
        connect.setEstablishTime(System.currentTimeMillis());
        return connect;
    }

    public void connectClose(String id, Channel channel){
        Connect connect = connectMap.remove(id);
        connect.setCloseTime(System.currentTimeMillis());
        synchronized (this){
            this.closeConnect.add(connect);
        }
    }

    public List<Connect>  getCloseConnect(){
        synchronized (this) {
            List<Connect> connectList = this.closeConnect;
            this.closeConnect = new ArrayList<>();
            return connectList;
        }
    }

    public List<Connect>  getCurrentConnect(){
        List<Connect> connectList = new ArrayList<>(this.connectMap.values().size());
        connectList.addAll(this.connectMap.values());
        return connectList;
    }
}
