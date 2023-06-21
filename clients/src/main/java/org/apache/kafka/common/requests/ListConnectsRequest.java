package org.apache.kafka.common.requests;

import org.apache.kafka.common.message.ListConnectsRequestData;
import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.protocol.ApiMessage;

public class ListConnectsRequest extends AbstractRequest{

    public static class Builder extends AbstractRequest.Builder<ListConnectsRequest> {
        private final ListConnectsRequestData data;

        public Builder(ListConnectsRequestData data) {
            super(ApiKeys.LIST_CONNECT);
            this.data = data;
        }

        @Override
        public ListConnectsRequest build(short version) {
            return new ListConnectsRequest(data, version);
        }
    }

    private ListConnectsRequestData data;

    public ListConnectsRequest(ListConnectsRequestData data, short version) {
        super(ApiKeys.UNREGISTER_BROKER, version);
        this.data = data;
    }

    @Override
    public AbstractResponse getErrorResponse(int throttleTimeMs, Throwable e) {
        return null;
    }

    @Override
    public ApiMessage data() {
        return null;
    }
}
