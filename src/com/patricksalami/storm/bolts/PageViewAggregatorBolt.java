package com.patricksalami.storm.bolts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.Config;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class PageViewAggregatorBolt extends BaseRichBolt {

	private static final int DEFAULT_EMIT_FREQUENCY_IN_SECONDS = 3600;
	private final HashMap<String, Integer> counts = new HashMap<String, Integer>();
	private OutputCollector collector;


	/**
	 * schedules a thread to run once an hour to emit the aggregate pageview
	 * count and reset the counter
	 */
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
	}

	/**
	 * This method will receive tuples that contain pretty page names as they
	 * are being viewed. It will count the occurrence of each unique page name.
	 */
	public void execute(Tuple input) {
		if(input.getSourceStreamId().equals("__tick")){
			//we expect to receive a "tick tuple" at every interval at which
			//counts should be emitted and the counters should be reset (i.e. once per hour)
			emitAndResetCounter();
		}else{
			countAndAck(input);
		}
	}
	
	private void countAndAck(Tuple input){
		String pageName = input.getStringByField("pageName");
		
		if (counts.containsKey(pageName)) {
			Integer currentCount = counts.get(pageName);
			counts.put(pageName, currentCount + 1);
		} else {
			counts.put(pageName, 1);
		}

		collector.ack(input);
	}
	
	private void emitAndResetCounter(){
		Calendar cal = new GregorianCalendar();
		DateFormat dateDf = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat hourDf = new SimpleDateFormat("HH:mm");
		
		String date = dateDf.format(cal.getTime());
		String hour = hourDf.format(cal.getTime());
		
		//emit the page count for the last hour for each page in the map
		for(String pageName : counts.keySet()){
			collector.emit(new Values(pageName, date, hour, counts.get(pageName)));
		}
		
		//flush all the counts in the map since we are starting a new hour
		counts.clear();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("pageName", "date", "hour", "pageViews"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		Map<String, Object> conf = new HashMap<String, Object>();
		conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, DEFAULT_EMIT_FREQUENCY_IN_SECONDS);
		return conf;
	}
	

}
