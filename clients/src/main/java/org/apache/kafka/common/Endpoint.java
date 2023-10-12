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
package org.apache.kafka.common;

import java.util.Objects;
import java.util.Optional;

import org.apache.kafka.common.annotation.InterfaceStability;
import org.apache.kafka.common.security.auth.SecurityProtocol;

/**
 * Represents a broker endpoint.
 */

@InterfaceStability.Evolving
public class Endpoint {

    /**
     * PLAINTEXT://kafka-host:9092
     *
     * - host：Broker主机名。
     * - port：Broker端口号。
     * - listenerName：监听器名字。目前预定义的名称包括PLAINTEXT、SSL、SASL\_PLAINTEXT和SASL\_SSL。Kafka允许你自定义其他监听器名称，比如CONTROLLER、INTERNAL等。
     * - securityProtocol：监听器使用的安全协议。Kafka支持4种安全协议，分别是 **PLAINTEXT**、 **SSL**、 **SASL\_PLAINTEXT** 和 **SASL\_SSL**。
     */

    private final String listenerName;
    private final SecurityProtocol securityProtocol;
    private final String host;
    private final int port;

    public Endpoint(String listenerName, SecurityProtocol securityProtocol, String host, int port) {
        this.listenerName = listenerName;
        this.securityProtocol = securityProtocol;
        this.host = host;
        this.port = port;
    }

    /**
     * Returns the listener name of this endpoint. This is non-empty for endpoints provided
     * to broker plugins, but may be empty when used in clients.
     */
    public Optional<String> listenerName() {
        return Optional.ofNullable(listenerName);
    }

    /**
     * Returns the security protocol of this endpoint.
     */
    public SecurityProtocol securityProtocol() {
        return securityProtocol;
    }

    /**
     * Returns advertised host name of this endpoint.
     */
    public String host() {
        return host;
    }

    /**
     * Returns the port to which the listener is bound.
     */
    public int port() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Endpoint)) {
            return false;
        }

        Endpoint that = (Endpoint) o;
        return Objects.equals(this.listenerName, that.listenerName) &&
            Objects.equals(this.securityProtocol, that.securityProtocol) &&
            Objects.equals(this.host, that.host) &&
            this.port == that.port;

    }

    @Override
    public int hashCode() {
        return Objects.hash(listenerName, securityProtocol, host, port);
    }

    @Override
    public String toString() {
        return "Endpoint(" +
            "listenerName='" + listenerName + '\'' +
            ", securityProtocol=" + securityProtocol +
            ", host='" + host + '\'' +
            ", port=" + port +
            ')';
    }
}
