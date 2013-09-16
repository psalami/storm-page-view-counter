# Storm Page View Counter
This project contains a Storm topology and Bolts that will count page views of a web application via Kafka.

## Prerequisites
* You should have a Kafka cluster to which your app servers send their logs. 
* You should also set up a Storm cluster, but out of the box, the code will run in local mode, so no Storm cluster is needed. 
* There is also a test spout that will emit sample URLs so the code can be tested without the need to set up a Kafka cluster.
* You should have maven and JDK 7+ installed for building this project.

## Installation and Running

After cloning the repo, do the following:
```
 mvn package
 java -cp target/storm-page-view-counter-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
   com.patricksalami.storm.topologies.PageCountTopology
```

Out of the box, this should run the topology using an in-process local Storm server and a test spout that will emit test URLs in place of the Kafka spout, so you don't have to have Kafka or Storm running separately to test the code. To switch to non-local mode, comment out
````
builder.setSpout("spout", new TestUrlSpout(), 1);
````
and un-comment
````
builder.setSpout("spout", kafkaSpout, 1);
````
in PageCountTopology.topology(). 

## Configuration
* Your topology will automatically end after a pre-defined number of seconds. This is useful during testing. This value can be adjusted by updating the runtimeInSeconds value in PageCountTopology.

* By default, the Bolt will convert all URLs to pretty page names using a local regex mapping file. The location of this file can be configured in PrettyPageNameBolt by changing the values of MAPPING_FILE_DIR and MAPPING_FILE_NAME.
* By default, the Bolt will count the number of page views and emit the total count once per hour and then reset the counter. This interval can be configured in PageViewAggregatorBolt by changing the DEFAULT_EMIT_FREQUENCY_IN_SECONDS parameter.
* You will want to review and possibly edit the additional settings defined in the PageCountTopology constructor:
    * kafkaZk* - update these parameters based on you Kafka ZooKeeper config. The defaults assume that Kafka ZK is running locally, which is useful for testing. 
    * You can also adjust the ZK path where Kafka stores its data (kafkaZkPath), as well as the ZK path where Storm can store offset metadata (kafkaZkStormPath). The default values will probably work here.
    * kafkaTopic - the name of the Kafka topic that Storm should subscribe to. This is the topic to which your appservers will write the log messages.


The output is emitted to the console (along with various debugging statements). The topology can be modified to send the output to a more meaningful location.