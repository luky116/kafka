package org.apache.kafka.qcommon;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.common.requests.RequestContext;
import org.apache.kafka.qcommon.monitor.ConnectionsMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QCommonManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(QCommonManager.class);

    private static final  QCommonManager INSTANCE = new QCommonManager();

    public static QCommonManager getInstance(){
        return INSTANCE;
    }

    public static String getLocalAddress() {
        try {
            // Traversal Network interface to get the first non-loopback and non-private address
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            List<String> ipv4Result = new ArrayList<String>();
            List<String> ipv6Result = new ArrayList<String>();
            while (enumeration.hasMoreElements()) {
                final NetworkInterface networkInterface = enumeration.nextElement();
                final Enumeration<InetAddress> en = networkInterface.getInetAddresses();
                while (en.hasMoreElements()) {
                    final InetAddress address = en.nextElement();
                    if (!address.isLoopbackAddress()) {
                        if (address instanceof Inet6Address) {
                            ipv6Result.add(normalizeHostAddress(address));
                        } else {
                            ipv4Result.add(normalizeHostAddress(address));
                        }
                    }
                }
            }

            // prefer ipv4
            if (!ipv4Result.isEmpty()) {
                for (String ip : ipv4Result) {
                    if (ip.startsWith("127.0") || ip.startsWith("192.168")) {
                        continue;
                    }

                    return ip;
                }

                return ipv4Result.get(ipv4Result.size() - 1);
            } else if (!ipv6Result.isEmpty()) {
                return ipv6Result.get(0);
            }
            //If failed to find,fall back to localhost
            final InetAddress localHost = InetAddress.getLocalHost();
            return normalizeHostAddress(localHost);
        } catch (Exception e) {
            LOGGER.error("Failed to obtain local address", e);
        }

        return null;
    }
    public static String normalizeHostAddress(final InetAddress localHost) {
        if (localHost instanceof Inet6Address) {
            return "[" + localHost.getHostAddress() + "]";
        } else {
            return localHost.getHostAddress();
        }
    }

    private final BrokerMetaData brokerMetaData = new BrokerMetaData();

    private final ConnectionsMonitor connectionsMonitor = new ConnectionsMonitor();


    private QCommonManager(){}

    public void init(Properties properties){
        Object start = properties.get("qcommon.start");
        brokerMetaData.setStart(!Objects.isNull(start) && Boolean.parseBoolean(start.toString()));

        if(Objects.nonNull(properties.get("super.users"))) { // 开启 ACL 功能
            brokerMetaData.setAcl(true);
        }

        brokerMetaData.setClusterName(properties.get("cluster.name").toString());
        brokerMetaData.setBrokerName(properties.get("broker.id").toString());
        brokerMetaData.setBrokerPort(Objects.nonNull(properties.get("port"))?Integer.parseInt(properties.get("port").toString()):9092);
        brokerMetaData.setBrokerAddress(getLocalAddress()); // 获取本机的 IP 地址，比如：10.106.106.189

        this.connectionsMonitor.setBrokerMetaData(this.brokerMetaData);
        LOGGER.info("QCommonManager init success , broker meta data is {}" , brokerMetaData);
    }

    public ConnectionsMonitor getConnectionsMonitor(){
        return connectionsMonitor;
    }


    public void checkRequest(RequestContext requestContext){
        if(!brokerMetaData.isStart()){
            return;
        }
        this.connectionsMonitor.checkRequestContext(requestContext);
    }

}
