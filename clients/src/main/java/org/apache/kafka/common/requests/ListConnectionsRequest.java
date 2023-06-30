package org.apache.kafka.common.requests;


import java.nio.ByteBuffer;
import org.apache.kafka.common.message.ListConnectionsRequestData;
import org.apache.kafka.common.message.ListConnectionsResponseData;
import org.apache.kafka.common.message.UnregisterBrokerRequestData;
import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.protocol.ApiMessage;
import org.apache.kafka.common.protocol.ByteBufferAccessor;

public class ListConnectionsRequest extends AbstractRequest{

    public static ListConnectionsRequest parse(ByteBuffer buffer, short version) {
        return new ListConnectionsRequest(new ListConnectionsRequestData(new ByteBufferAccessor(buffer), version),
                version);
    }

    public static class Builder extends AbstractRequest.Builder<ListConnectionsRequest> {
        private final ListConnectionsRequestData data;

        public Builder(ListConnectionsRequestData data) {
            super(ApiKeys.LIST_CONNECTIONS);
            this.data = data;
        }

        @Override
        public ListConnectionsRequest build(short version) {
            return new ListConnectionsRequest(data, version);
        }
    }

    private ListConnectionsRequestData data;

    public ListConnectionsRequest(ListConnectionsRequestData data, short version) {
        super(ApiKeys.LIST_CONNECTIONS, version);
        this.data = data;
    }

    @Override
    public AbstractResponse getErrorResponse(int throttleTimeMs, Throwable e) {
        ListConnectionsResponseData data = new ListConnectionsResponseData();
        data.setThrottleTimeMs(throttleTimeMs);
        data.setErrorMessage(e.getMessage());
        data.setErrorCode(ApiError.fromThrowable(e).error().code());
        return new ListConnectionsResponse(data);
    }

    @Override
    public ListConnectionsRequestData data() {
        return this.data;
    }

}
