package org.apache.kafka.common.requests;


import org.apache.kafka.common.message.ListConnectionsRequestData;
import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.protocol.ApiMessage;

public class ListConnectionsRequest extends AbstractRequest{


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
        super(ApiKeys.UNREGISTER_BROKER, version);
        this.data = data;
    }

    @Override
    public AbstractResponse getErrorResponse(int throttleTimeMs, Throwable e) {
        return null;
    }

    @Override
    public ListConnectionsRequestData data() {
        return this.data;
    }

}
