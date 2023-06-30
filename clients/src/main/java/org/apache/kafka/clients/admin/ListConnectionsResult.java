package org.apache.kafka.clients.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.internals.KafkaFutureImpl;
import org.apache.kafka.qcommon.monitor.Connections;

public class ListConnectionsResult {

    private List<Connections> connectionsList = new ArrayList<>();

    private KafkaFutureImpl<List<Connections>> future;
    private Integer index;

    public ListConnectionsResult(KafkaFutureImpl<List<Connections>> future, Integer index) {
        this.future = future;
        this.index = index;
    }

    public synchronized void setConnectionsList(List<Connections> connectionsList){

        this.connectionsList.addAll(connectionsList);
        index--;
        if(index == 0){
            future.complete(this.connectionsList);
        }
    }

    public KafkaFuture<List<Connections>> values() {
        return future;
    }
}
