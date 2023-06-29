package org.apache.kafka.qcommon.monitor;


import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.requests.RequestContext;
import org.apache.kafka.common.requests.RequestHeader;
import org.apache.kafka.qcommon.BrokerMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionsMonitor {

    private static final Logger log = LoggerFactory.getLogger(ConnectionsMonitor.class);
    private BrokerMetaData brokerMetaData;

    private Map<String, Connections>  connectMap = new ConcurrentHashMap<>();

    private List<Connections>  closeConnect = new ArrayList<>();

    public void setBrokerMetaData(BrokerMetaData brokerMetaData) {
        this.brokerMetaData = brokerMetaData;
    }

    public void connectEstablish(String id, SocketChannel channel){
        if(Objects.isNull(brokerMetaData)){
            return;
        }
        if(!this.brokerMetaData.isStart()){
            return;
        }
        Connections connect = this.createConnect(id,channel);
        connectMap.put(id , connect);
    }

    private Connections createConnect(String id, SocketChannel channel)  {
        Connections connect = new Connections();
        connect.setChannelId(id);
        connect.setClusterName(brokerMetaData.getClusterName());
        connect.setBrokerName(brokerMetaData.getBrokerName());
        connect.setBrokerAddress(brokerMetaData.getBrokerAddress());
        connect.setBrokerPort(brokerMetaData.getBrokerPort());
        try {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) channel.getRemoteAddress();
            connect.setClientAddress(inetSocketAddress.getAddress().getHostAddress());
            connect.setClientPort(Integer.valueOf(inetSocketAddress.getPort()).toString());
        }catch (Exception e){
            log.error(e.getMessage() ,e);
        }
        connect.setEstablishTime(System.currentTimeMillis());
        return connect;
    }

    public void connectClose(String id, Channel channel){
        if(Objects.isNull(brokerMetaData)){
            return;
        }
        if(!this.brokerMetaData.isStart()){
            return;
        }
        Connections connect = connectMap.remove(id);
        if(Objects.isNull(connect)){
            return;
        }
        connect.setCloseTime(System.currentTimeMillis());
        synchronized (this){
            // 如果没有admin client 进行采集，会出现内存溢出。
            // 防止内存溢出，超过十万就清空
            if(closeConnect.size() > 100000){
                closeConnect = new ArrayList<>();
            }
            this.closeConnect.add(connect);
        }
    }

    public void checkRequestContext(RequestContext requestContext){
        Connections connections = connectMap.get(requestContext.connectionId);
        if(brokerMetaData.isAcl()){
            connections.setUserName(requestContext.principal.getName());
        }else{
            connections.setUserName(requestContext.header.clientId());
        }
        if(Objects.equals(ApiKeys.PRODUCE, requestContext.header.apiKey()) ||Objects.equals(ApiKeys.FETCH, requestContext.header.apiKey()) ){
            connections.setGroupType(requestContext.header.apiKey().name);
        }


    }

    public List<Connections>  getCloseConnections(){
        if(!this.brokerMetaData.isStart()){
            return Collections.emptyList();
        }
        List<Connections> connectList;
        synchronized (this) {
            connectList = this.closeConnect;
            this.closeConnect = new ArrayList<>();
        }
        return connectList;
    }

    public List<Connections>  getCurrentConnections(){
        if(!this.brokerMetaData.isStart()){
            return Collections.emptyList();
        }
        List<Connections> connectList = new ArrayList<>(this.connectMap.values().size());
        connectList.addAll(this.connectMap.values());
        return connectList;
    }
}