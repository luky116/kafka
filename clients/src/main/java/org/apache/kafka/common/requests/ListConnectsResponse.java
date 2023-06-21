package org.apache.kafka.common.requests;

import java.util.Map;
import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.protocol.ApiMessage;
import org.apache.kafka.common.protocol.Errors;

public class ListConnectsResponse extends AbstractResponse {

    protected ListConnectsResponse(ApiKeys apiKey) {
        super(apiKey);
    }

    @Override
    public ApiMessage data() {
        return null;
    }

    @Override
    public Map<Errors, Integer> errorCounts() {
        return null;
    }

    @Override
    public int throttleTimeMs() {
        return 0;
    }
}
