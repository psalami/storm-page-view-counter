package com.patricksalami.storm.bolts;


import backtype.storm.Config;
import backtype.storm.topology.OutputFieldsDeclarer;
import java.util.Map;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import java.util.HashMap;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestUrlSpout extends BaseRichSpout {
    public static Logger LOG = LoggerFactory.getLogger(TestUrlSpout.class);
    boolean _isDistributed;
    SpoutOutputCollector _collector;

    public TestUrlSpout() {
        this(true);
    }

    public TestUrlSpout(boolean isDistributed) {
        _isDistributed = isDistributed;
    }
        
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        _collector = collector;
    }
    
    public void close() {
        
    }
        
    public void nextTuple() {
        Utils.sleep(1000);
        final String[] words = new String[] {"/my/page/user1", "/my/page/user2", "/other/page/p/s", "/other/page/n/g", "somethingelse"};
        final Random rand = new Random();
        final String word = words[rand.nextInt(words.length)];
        _collector.emit(new Values(word));
    }
    
    public void ack(Object msgId) {

    }

    public void fail(Object msgId) {
        
    }
    
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        if(!_isDistributed) {
            Map<String, Object> ret = new HashMap<String, Object>();
            ret.put(Config.TOPOLOGY_MAX_TASK_PARALLELISM, 1);
            return ret;
        } else {
            return null;
        }
    }    
}