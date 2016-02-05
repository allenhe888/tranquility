/*
 * Licensed to Metamarkets Group Inc. (Metamarkets) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Metamarkets licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.metamx.tranquility.config

import com.metamx.common.scala.net.curator.DiscoAnnounceConfig
import com.metamx.common.scala.net.curator.DiscoConfig
import com.metamx.common.scala.untyped.Dict
import com.metamx.tranquility.beam.ClusteredBeamTuning
import com.metamx.tranquility.druid.DruidBeamConfig
import com.metamx.tranquility.tranquilizer.Tranquilizer
import java.util.Properties
import org.joda.time.Period
import org.skife.config.Config
import org.skife.config.ConfigurationObjectFactory

abstract class PropertiesBasedConfig(
  private[config] val globalPropertyNames: Set[String]
) extends DiscoConfig
{
  private var props: Properties = null

  def this() {
    this(Set[String]())
  }

  def properties: Properties = props

  @Config(Array("druid.selectors.indexing.serviceName"))
  def druidIndexingServiceName: String = "druid/overlord"

  @Config(Array("task.partitions"))
  def taskPartitions: Int = ClusteredBeamTuning().partitions

  @Config(Array("task.replicants"))
  def taskReplicants: Int = ClusteredBeamTuning().replicants

  @Config(Array("task.warmingPeriod"))
  def taskWarmingPeriod: Period = ClusteredBeamTuning().warmingPeriod

  @Config(Array("zookeeper.connect"))
  def zookeeperConnect: String

  @Config(Array("zookeeper.timeout"))
  def zookeeperTimeout: Period = new Period("PT20S")

  @Config(Array("tranquility.maxBatchSize"))
  def tranquilityMaxBatchSize: Int = Tranquilizer.DefaultMaxBatchSize

  @Config(Array("tranquility.maxPendingBatches"))
  def tranquilityMaxPendingBatches: Int = Tranquilizer.DefaultMaxPendingBatches

  @Config(Array("tranquility.lingerMillis"))
  def tranquilityLingerMillis: Int = Tranquilizer.DefaultLingerMillis

  @Config(Array("druid.discovery.curator.path"))
  def discoPath: String = "/druid/discovery"

  @Config(Array("druidBeam.firehoseGracePeriod"))
  def firehoseGracePeriod: Period = DruidBeamConfig().firehoseGracePeriod

  @Config(Array("druidBeam.firehoseQuietPeriod"))
  def firehoseQuietPeriod: Period = DruidBeamConfig().firehoseQuietPeriod

  @Config(Array("druidBeam.firehoseRetryPeriod"))
  def firehoseRetryPeriod: Period = DruidBeamConfig().firehoseRetryPeriod

  @Config(Array("druidBeam.firehoseChunkSize"))
  def firehoseChunkSize: Int = DruidBeamConfig().firehoseChunkSize

  @Config(Array("druidBeam.randomizeTaskId"))
  def randomizeTaskId: Boolean = DruidBeamConfig().randomizeTaskId

  @Config(Array("druidBeam.indexRetryPeriod"))
  def indexRetryPeriod: Period = DruidBeamConfig().indexRetryPeriod

  @Config(Array("druidBeam.firehoseBufferSize"))
  def firehoseBufferSize: Int = DruidBeamConfig().firehoseBufferSize

  def druidBeamConfig: DruidBeamConfig = DruidBeamConfig.builder()
    .firehoseGracePeriod(firehoseGracePeriod)
    .firehoseQuietPeriod(firehoseQuietPeriod)
    .firehoseRetryPeriod(firehoseRetryPeriod)
    .firehoseChunkSize(firehoseChunkSize)
    .randomizeTaskId(randomizeTaskId)
    .indexRetryPeriod(indexRetryPeriod)
    .firehoseBufferSize(firehoseBufferSize)
    .build()

  override def discoAnnounce: Option[DiscoAnnounceConfig] = None
}

object PropertiesBasedConfig
{
  def fromDict[ConfigType <: PropertiesBasedConfig](
    d: Dict,
    clazz: Class[ConfigType]
  ): ConfigType =
  {
    val properties = new Properties
    for ((k, v) <- d if v != null) {
      properties.setProperty(k, String.valueOf(v))
    }
    val configFactory = new ConfigurationObjectFactory(properties)
    val config = configFactory.build(clazz)
    config.props = properties
    config
  }
}