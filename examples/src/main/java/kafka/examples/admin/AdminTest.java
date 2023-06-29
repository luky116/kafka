package kafka.examples.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListConnectionsResult;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.qcommon.ListConnections;
import org.apache.kafka.qcommon.monitor.Connections;

public class AdminTest {

    private AdminClient adminClient;

    public AdminTest(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "DemoProducer");



        props.put(SaslConfigs.SASL_MECHANISM , "SCRAM-SHA-256");
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,"SASL_PLAINTEXT");
        props.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"admin\" password=\"admin\";");
        adminClient = AdminClient.create(props);
    }

    public void listConnections() throws ExecutionException, InterruptedException {
        ListConnections listConnections = new ListConnections();
        listConnections.setConnectionType("current");
        List<String> list = new ArrayList<>();
        list.add("127.0.0.1:9092");
        listConnections.setBrokerList(list);
        ListConnectionsResult result = adminClient.listConnections(listConnections);
        List<Connections> connectionsList = result.values().get();
        connectionsList.size();
    }


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        AdminTest admin = new AdminTest();
        admin.listConnections();


    }
}
