/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kafka.coordinator.group

import java.util

import kafka.utils.nonthreadsafe

// 组成员概要数据，提取了最核心的元数据信息。
case class MemberSummary(memberId: String,                 // 成员ID，由Kafka自动生成
                        // 消费者组静态成员的ID。静态成员机制的引入能够规避不必要的消费者组Rebalance操作。
                         // 它是非常新且高阶的功能，这里你只要稍微知道它的含义就可以了。
                         // 如果你对此感兴趣，建议你去官网看看group.instance.id参数的说明。
                         groupInstanceId: Option[String],  // Consumer端参数group.instance.id值
                         clientId: String,                 // client.id参数值
                         clientHost: String,               // Consumer端程序主机名
                         metadata: Array[Byte],            // 消费者组成员使用的分配策略
                         assignment: Array[Byte])          // 成员订阅分区

// 仅仅定义了一个工具方法，供上层组件调用。
private object MemberMetadata {
  // 提取分区分配策略的集合
  // 用来统计一个消费组下的成员到底配置了多少种分区分配策略
  def plainProtocolSet(supportedProtocols: List[(String, Array[Byte])]) = supportedProtocols.map(_._1).toSet
}

/**
 * Member metadata contains the following metadata:
 *
 * Heartbeat metadata:
 * 1. negotiated heartbeat session timeout
 * 2. timestamp of the latest heartbeat
 *
 * Protocol metadata:
 * 1. the list of supported protocols (ordered by preference)
 * 2. the metadata associated with each protocol
 *
 * In addition, it also contains the following state information:
 *
 * 1. Awaiting rebalance callback: when the group is in the prepare-rebalance state,
 *                                 its rebalance callback will be kept in the metadata if the
 *                                 member has sent the join group request
 * 2. Awaiting sync callback: when the group is in the awaiting-sync state, its sync callback
 *                            is kept in metadata until the leader provides the group assignment
 *                            and the group transitions to stable
 */
@nonthreadsafe
// 消费者组成员的元数据，Kafka为消费者组成员定义了很多数据
private[group] class MemberMetadata(var memberId: String,
                                    val groupInstanceId: Option[String],
                                    val clientId: String,
                                    val clientHost: String,
                                   // Rebalance操作的超时时间，即一次Rebalance操作必须在这个时间内完成，否则被视为超时。
                                    // 这个字段的值是Consumer端参数max.poll.interval.ms的值。
                                    val rebalanceTimeoutMs: Int, // Rebalane操作超时时间
                                   // 当前消费者组成员依靠心跳机制“保活”。如果在会话超时时间之内未能成功发送心跳，组成员就被判定成“下线”，
                                    // 从而触发新一轮的Rebalance。这个字段的值是Consumer端参数session.timeout.ms的值。
                                    val sessionTimeoutMs: Int, // 会话超时时间
                                    val protocolType: String, // 对消费者组而言，是"consumer"，其他比如 connect
                                    // 成员配置的多套分区分配策略
                                    var supportedProtocols: List[(String, Array[Byte])],
                                    // 分区分配方案
                                    var assignment: Array[Byte] = Array.empty[Byte]) {

  // 表示组成员是否正在等待加入组
  var awaitingJoinCallback: JoinGroupResult => Unit = null
  // 表示组成员是否正在等待GroupCoordinator发送分配方案
  var awaitingSyncCallback: SyncGroupResult => Unit = null
  // 表示组成员是否发起“退出组”的操作
  var isLeaving: Boolean = false
  // 表示是否是消费者组下的新成员
  var isNew: Boolean = false

  def isStaticMember: Boolean = groupInstanceId.isDefined

  // 用来追踪延迟的心跳请求。如果接收到了一次心跳请求的回复，这个值会被置为 true。
  // This variable is used to track heartbeat completion through the delayed
  // heartbeat purgatory. When scheduling a new heartbeat expiration, we set
  // this value to `false`. Upon receiving the heartbeat (or any other event
  // indicating the liveness of the client), we set it to `true` so that the
  // delayed heartbeat can be completed.
  var heartbeatSatisfied: Boolean = false

  def isAwaitingJoin = awaitingJoinCallback != null
  def isAwaitingSync = awaitingSyncCallback != null

  /**
   * Get metadata corresponding to the provided protocol.
   */
    // 从该成员配置的分区分配方案列表中寻找给定策略的详情
  def metadata(protocol: String): Array[Byte] = {
    // 从配置的分区分配策略中寻找给定策略
    supportedProtocols.find(_._1 == protocol) match {
      case Some((_, metadata)) => metadata
      case None =>
        throw new IllegalArgumentException("Member does not support protocol")
    }
  }

  def hasSatisfiedHeartbeat: Boolean = {
    if (isNew) {
      // 注意，新成员在等待加入组的过程中，其心跳是被忽略的，所以这里直接返回true
      // New members can be expired while awaiting join, so we have to check this first
      heartbeatSatisfied
    } else if (isAwaitingJoin || isAwaitingSync) {
      // Members that are awaiting a rebalance automatically satisfy expected heartbeats
      true
    } else {
      // Otherwise we require the next heartbeat
      heartbeatSatisfied
    }
  }

  /**
   * Check if the provided protocol metadata matches the currently stored metadata.
   */
  def matches(protocols: List[(String, Array[Byte])]): Boolean = {
    if (protocols.size != this.supportedProtocols.size)
      return false

    for (i <- protocols.indices) {
      val p1 = protocols(i)
      val p2 = supportedProtocols(i)
      if (p1._1 != p2._1 || !util.Arrays.equals(p1._2, p2._2))
        return false
    }
    true
  }

  // 组成员概要数据，提取了最核心的元数据信息
  def summary(protocol: String): MemberSummary = {
    MemberSummary(memberId, groupInstanceId, clientId, clientHost, metadata(protocol), assignment)
  }

  def summaryNoMetadata(): MemberSummary = {
    MemberSummary(memberId, groupInstanceId, clientId, clientHost, Array.empty[Byte], Array.empty[Byte])
  }

  /**
   * Vote for one of the potential group protocols. This takes into account the protocol preference as
   * indicated by the order of supported protocols and returns the first one also contained in the set
   */
  def vote(candidates: Set[String]): String = {
    supportedProtocols.find({ case (protocol, _) => candidates.contains(protocol)}) match {
      case Some((protocol, _)) => protocol
      case None =>
        throw new IllegalArgumentException("Member does not support any of the candidate protocols")
    }
  }

  override def toString: String = {
    "MemberMetadata(" +
      s"memberId=$memberId, " +
      s"groupInstanceId=$groupInstanceId, " +
      s"clientId=$clientId, " +
      s"clientHost=$clientHost, " +
      s"sessionTimeoutMs=$sessionTimeoutMs, " +
      s"rebalanceTimeoutMs=$rebalanceTimeoutMs, " +
      s"supportedProtocols=${supportedProtocols.map(_._1)}, " +
      ")"
  }
}
