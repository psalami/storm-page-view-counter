package com.patricksalami.storm.bolts;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class PrettyPageNameBolt extends BaseRichBolt{
	
	public static final String MAPPING_FILE_DIR = ".";
	public static final String MAPPING_FILE_NAME = "pretty-page-names.txt";
	private OutputCollector collector;
	private HashMap<String,String> pageNames = new HashMap<String,String>();
	
	public static Logger LOG = LoggerFactory.getLogger(PrettyPageNameBolt.class);
	
	

	/**
	 * reads the regex mapping to pretty page names from a local file into memory
	 */
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		Path path = FileSystems.getDefault().getPath(MAPPING_FILE_DIR, MAPPING_FILE_NAME);
		List<String> lines = null;
		try{
			lines = Files.readAllLines(path, Charset.defaultCharset() );
		}catch(IOException e){
			//handle exception
		}
		
		if(lines != null){
			for(String line : lines){
				String[] lineComponents = line.split("=>");
				if(lineComponents.length == 2){
					pageNames.put(lineComponents[0].trim(), lineComponents[1].trim());
				}else{
					//skip invalid line and emit warning
				}
			}
		}
		
		this.collector = collector;
	}

	/**
	 * translates the incoming original URL to a pretty page name
	 */
	public void execute(Tuple input) {
		String pageName = input.getStringByField("word");
		collector.ack(input);
		boolean matched = false;
		for(String regex : pageNames.keySet()){
			if(pageName.matches(regex)){
				matched = true;
				//if we can find a match for a pretty page name, emit the pretty name...
				collector.emit(new Values(pageNames.get(regex)));
			}
		}
		
		//...otherwise, emit the original URL
		if(!matched){
			collector.emit(new Values(pageName));
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("pageName"));
	}

}
