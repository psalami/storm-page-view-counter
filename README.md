# Storm Page View Counter
This project contains a Storm topology and Bolts that will count page views of a web application via Kafka.

## Prerequisites
* You should have a Kafka cluster to which your app servers send their logs. 
* You should also set up a Storm cluster, but out of the box, the code will run in local mode, so no Storm cluster is needed. 
* There is also a test spout that will emit sample URLs so the code can be tested without the need to set up a Kafka cluster.

## Installation and Running

After cloning the repo, do the following:

`mvn package`
`java -cp target/storm-page-view-counter-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.patricksalami.storm.topologies.PageCountTopology`

## Configuration
* By default, the Bolt will convert all URLs to pretty page names using a local regex mapping file. The location of this file can be configured in PrettyPageNameBolt by changing the values of MAPPING_FILE_DIR and MAPPING_FILE_NAME.
* By default, the Bolt will count the number of page views and emit the total count once per hour and then reset the counter. This interval can be configured in PageViewAggregatorBolt by changing the DEFAULT_EMIT_FREQUENCY_IN_SECONDS parameter.

The output is emitted to the console (along with various debugging statements). The topology can be modified to send the output to a more meaningful location.