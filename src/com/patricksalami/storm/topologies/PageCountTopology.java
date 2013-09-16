package com.patricksalami.storm.topologies;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patricksalami.storm.bolts.PageViewAggregatorBolt;
import com.patricksalami.storm.bolts.PrettyPageNameBolt;
import com.patricksalami.storm.bolts.TestUrlSpout;

import storm.kafka.KafkaConfig;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.starter.bolt.PrinterBolt;
import storm.starter.util.StormRunner;
import backtype.storm.Config;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

public class PageCountTopology {

	private final TopologyBuilder builder;
	private final Config topologyConfig;
	private final String topologyName;
	private final String kafkaTopic; // topic to read from
	private final String kafkaZkHost;
	private final int kafkaZkPort;
	private final String kafkaZkPath; // the root path in Zookeeper for Kafka
	private final String kafkaZkStormPath; // the root path in Zookeeper for the spout to store the consumer offsets
	private final String kafkaZkConsumerId; // an id for this consumer for storing the consumer offsets in Zookeeper
	private final int runtimeInSeconds;
	
	
	public static Logger LOG = LoggerFactory.getLogger(PageCountTopology.class);

	public PageCountTopology() throws Exception {
		builder = topology();
		topologyConfig = config();
		topologyName = "PageViewCount";
		runtimeInSeconds = 1000;
		kafkaZkHost = "localhost";
		kafkaZkPort = 2181;
		kafkaZkPath = "/brokers";
		kafkaTopic = "pageviews"; 
		kafkaZkStormPath = "/kafkastorm";
		kafkaZkConsumerId = "aggregator";
	}

	private TopologyBuilder topology() throws Exception {

		TopologyBuilder builder = new TopologyBuilder();
		
		//set up Kafka Spout
		KafkaConfig.ZkHosts zkh = new KafkaConfig.ZkHosts(kafkaZkHost + ":" + kafkaZkPort, kafkaZkPath);
		SpoutConfig kafkaSpoutConfig = new SpoutConfig(
				zkh, kafkaTopic,
				kafkaZkStormPath, kafkaZkConsumerId);
		kafkaSpoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
		KafkaSpout kafkaSpout = new KafkaSpout(kafkaSpoutConfig);
		
		// == use local test spout for testing without Kafka ==
		//builder.setSpout("spout", kafkaSpout, 1);
		builder.setSpout("spout", new TestUrlSpout(), 1);
		
		builder.setBolt("makePretty", new PrettyPageNameBolt(), 1)
				.shuffleGrouping("spout");
		builder.setBolt("aggregate", new PageViewAggregatorBolt(), 1)
				.fieldsGrouping("makePretty", new Fields("pageName"));

		builder.setBolt("print", new PrinterBolt(), 1).shuffleGrouping(
				"aggregate");

		return builder;
	}

	private Config config() {
		Config topologyConfig = new Config();
		topologyConfig.setDebug(true);
		topologyConfig.setMaxTaskParallelism(3);
		return topologyConfig;
	}

	public void run() throws InterruptedException {
		StormRunner.runTopologyLocally(builder.createTopology(), topologyName,
				topologyConfig, runtimeInSeconds);
	}

	public static void main(String[] args) throws Exception {
		new PageCountTopology().run();
	}

}
