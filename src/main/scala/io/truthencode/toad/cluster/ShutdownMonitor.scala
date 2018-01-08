package io.truthencode.toad.cluster

import com.hazelcast.core.{HazelcastInstance, HazelcastInstanceAware}

class ShutdownMonitor extends HazelcastInstanceAware with Runnable with Serializable {
  final var node: HazelcastInstance = _

  override def setHazelcastInstance(hazelcastInstance: HazelcastInstance): Unit = node = hazelcastInstance

  override def run(): Unit = node.getLifecycleService.shutdown()
}
