/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.common.requests;

import org.apache.kafka.common.errors.InvalidRequestException;
import org.apache.kafka.common.message.ApiVersionsRequestData;
import org.apache.kafka.common.network.ClientInformation;
import org.apache.kafka.common.network.ListenerName;
import org.apache.kafka.common.network.Send;
import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.common.security.auth.KafkaPrincipalSerde;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Optional;

import static org.apache.kafka.common.protocol.ApiKeys.API_VERSIONS;

public class RequestContext implements AuthorizableRequestContext {
    public final RequestHeader header; // Request头部数据，主要是一些对用户不可见的元数据信息，如Request类型、Request API版本、clientId等
    public final String connectionId; // Request发送方的TCP连接串标识，由Kafka根据一定规则定义，主要用于表示TCP连接
    public final InetAddress clientAddress; // Request发送方IP地址
    public final KafkaPrincipal principal;  // Kafka用户认证类，用于认证授权
    public final ListenerName listenerName; // 监听器名称，可以是预定义的监听器（如PLAINTEXT），也可自行定义
    public final SecurityProtocol securityProtocol; // 安全协议类型，目前支持4种：PLAINTEXT、SSL、SASL_PLAINTEXT、SASL_SSL
    public final ClientInformation clientInformation; // 用户自定义的一些连接方信息
    // 从给定的ByteBuffer中提取出Request和对应的Size值
    public final boolean fromPrivilegedListener;
    public final Optional<KafkaPrincipalSerde> principalSerde;

    public RequestContext(RequestHeader header,
                          String connectionId,
                          InetAddress clientAddress,
                          KafkaPrincipal principal,
                          ListenerName listenerName,
                          SecurityProtocol securityProtocol,
                          ClientInformation clientInformation,
                          boolean fromPrivilegedListener) {
        this(header,
            connectionId,
            clientAddress,
            principal,
            listenerName,
            securityProtocol,
            clientInformation,
            fromPrivilegedListener,
            Optional.empty());
    }

    public RequestContext(RequestHeader header,
                          String connectionId,
                          InetAddress clientAddress,
                          KafkaPrincipal principal,
                          ListenerName listenerName,
                          SecurityProtocol securityProtocol,
                          ClientInformation clientInformation,
                          boolean fromPrivilegedListener,
                          Optional<KafkaPrincipalSerde> principalSerde) {
        this.header = header;
        this.connectionId = connectionId;
        this.clientAddress = clientAddress;
        this.principal = principal;
        this.listenerName = listenerName;
        this.securityProtocol = securityProtocol;
        this.clientInformation = clientInformation;
        this.fromPrivilegedListener = fromPrivilegedListener;
        this.principalSerde = principalSerde;
    }

    public RequestAndSize parseRequest(ByteBuffer buffer) {
        if (isUnsupportedApiVersionsRequest()) {
            // Unsupported ApiVersion requests are treated as v0 requests and are not parsed
            ApiVersionsRequest apiVersionsRequest = new ApiVersionsRequest(new ApiVersionsRequestData(), (short) 0, header.apiVersion());
            return new RequestAndSize(apiVersionsRequest, 0);
        } else {
            ApiKeys apiKey = header.apiKey();
            try {
                short apiVersion = header.apiVersion();
                return AbstractRequest.parseRequest(apiKey, apiVersion, buffer);
            } catch (Throwable ex) {
                throw new InvalidRequestException("Error getting request for apiKey: " + apiKey +
                        ", apiVersion: " + header.apiVersion() +
                        ", connectionId: " + connectionId +
                        ", listenerName: " + listenerName +
                        ", principal: " + principal, ex);
            }
        }
    }

    /**
     * Build a {@link Send} for direct transmission of the provided response
     * over the network.
     */
    public Send buildResponseSend(AbstractResponse body) {
        return body.toSend(header.toResponseHeader(), apiVersion());
    }

    /**
     * Serialize a response into a {@link ByteBuffer}. This is used when the response
     * will be encapsulated in an {@link EnvelopeResponse}. The buffer will contain
     * both the serialized {@link ResponseHeader} as well as the bytes from the response.
     * There is no `size` prefix unlike the output from {@link #buildResponseSend(AbstractResponse)}.
     *
     * Note that envelope requests are reserved only for APIs which have set the
     * {@link ApiKeys#forwardable} flag. Notably the `Fetch` API cannot be forwarded,
     * so we do not lose the benefit of "zero copy" transfers from disk.
     */
    public ByteBuffer buildResponseEnvelopePayload(AbstractResponse body) {
        return body.serializeWithHeader(header.toResponseHeader(), apiVersion());
    }

    private boolean isUnsupportedApiVersionsRequest() {
        return header.apiKey() == API_VERSIONS && !API_VERSIONS.isVersionSupported(header.apiVersion());
    }

    public short apiVersion() {
        // Use v0 when serializing an unhandled ApiVersion response
        if (isUnsupportedApiVersionsRequest())
            return 0;
        return header.apiVersion();
    }

    @Override
    public String listenerName() {
        return listenerName.value();
    }

    @Override
    public SecurityProtocol securityProtocol() {
        return securityProtocol;
    }

    @Override
    public KafkaPrincipal principal() {
        return principal;
    }

    @Override
    public InetAddress clientAddress() {
        return clientAddress;
    }

    @Override
    public int requestType() {
        return header.apiKey().id;
    }

    @Override
    public int requestVersion() {
        return header.apiVersion();
    }

    @Override
    public String clientId() {
        return header.clientId();
    }

    @Override
    public int correlationId() {
        return header.correlationId();
    }
}
