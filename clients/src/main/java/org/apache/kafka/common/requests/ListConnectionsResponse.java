package org.apache.kafka.common.requests;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.message.ListConnectionsResponseData;
import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.protocol.ApiMessage;
import org.apache.kafka.common.protocol.ByteBufferAccessor;
import org.apache.kafka.common.protocol.Errors;

public class ListConnectionsResponse extends AbstractResponse {

    public static final ListConnectionsResponse parse(ByteBuffer buffer, short version){
        return new ListConnectionsResponse(new ListConnectionsResponseData(new ByteBufferAccessor(buffer) , version));
    }

    private ListConnectionsResponseData data;

    public ListConnectionsResponse(ListConnectionsResponseData data) {
        super(ApiKeys.LIST_CONNECTIONS);
        this.data = data;
    }

    @Override
    public ListConnectionsResponseData data() {
        return data;
    }

    @Override
    public Map<Errors, Integer> errorCounts() {
        return new HashMap<>();
    }

    @Override
    public int throttleTimeMs() {
        return 0;
    }
}
