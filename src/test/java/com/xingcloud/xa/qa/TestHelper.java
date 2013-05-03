package com.xingcloud.xa.qa;

import com.xingcloud.adhocprocessorV2.exception.AdhocQueryException;
import com.xingcloud.adhocprocessorV2.hbase.model.CopResultV2;
import com.xingcloud.adhocprocessorV2.query.XAQueryDEUCoprocessor;
import com.xingcloud.adhocprocessorV2.query.model.Filter;
import com.xingcloud.adhocprocessorV2.query.model.FormulaQueryDescriptor;
import com.xingcloud.basic.conf.Dom;
import com.xingcloud.basic.utils.DateUtils;
import com.xingcloud.cache.XCache;
import com.xingcloud.cache.redis.NoSelectRedisXCacheOperator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Test;
import com.xingcloud.basic.conf.ConfigReader;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wangchangli
 * Date: 4/28/13
 * Time: 6:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestHelper {
  
  private static String today;
  private static String yesterday;
  static {
    DOMConfigurator.configure("src/test/resources/log4j.xml");
  }
  
  private static Log LOG = LogFactory.getLog(TestHelper.class);
  
  private static Map<Object, CopResultV2> getCommonResultFromHBase(String project, String event, String segmentJson, double  percent, String date) throws AdhocQueryException {
    FormulaQueryDescriptor desc = new FormulaQueryDescriptor(project, date,
      date, event,
      segmentJson, Filter.ALL, 0, 0, true, true, true,
      FormulaQueryDescriptor.CommonQueryType.NORMAL, FormulaQueryDescriptor.Interval.PERIOD);

    Set<FormulaQueryDescriptor> descs = new HashSet<FormulaQueryDescriptor>();
    descs.add(desc);
    Map<FormulaQueryDescriptor, Map<Object, CopResultV2>> resultMap = XAQueryDEUCoprocessor.getInstance().queryBatchCommon(project, date,
      date, event,
      descs, percent);
    
    return resultMap.get(desc);
  } 

  private static Map<Object, CopResultV2> getGroupbyAttrResult(String project, String event, String segmentJson, double  percent, String date, String attr) throws AdhocQueryException {
    Map<Object, CopResultV2> result = XAQueryDEUCoprocessor.getInstance().queryAllGroupByAttr(project, segmentJson,
      event, Filter.ALL,
      date, date,
      attr, percent);
    return result;
  }
  
  private static Map<Object, CopResultV2> getGroupbyPeriod(String project, String event, String segmentJson, double percent, String date, int period ) throws AdhocQueryException {
    Map<Object, CopResultV2> result = XAQueryDEUCoprocessor.getInstance().queryAllByPeriodOfMins(project, segmentJson,
        event, Filter.ALL, 
      date, date, 
      period, percent);
    
    return result;
  }
  
  private static boolean deviationCheck(FormulaQueryDescriptor todayDesc, FormulaQueryDescriptor yesterdayDesc,double O2ODeviation, double T2YDeviation, Map<String, Number[]> todayRedis, Map<String, Number[]> yesterdayRedis, Map<Object, CopResultV2> todayHBase){
    LOG.info("QA result for " + todayDesc.getCacheKey() +":");
    boolean mismatching = false;
    String indent = ">>>>";
    for(Map.Entry<Object, CopResultV2> entry: todayHBase.entrySet()){
      String todayKey = (String)entry.getKey();
      String yesterdayKey = todayKey.replace(today, yesterday);
      Number[] hBaseValue = new Number[]{entry.getValue().getEventNum(), entry.getValue().getEventAmount(),entry.getValue().getUserNum()};
     
      StringBuilder deviationReport = new StringBuilder();
      boolean existDeviation=false;
      //redis and hbase contrast
      if (contrast(hBaseValue, todayRedis.get(todayKey), O2ODeviation)){
        existDeviation = true;
        deviationReport.append("H2R outside allowable deviation on ");
        deviationReport.append("'"+todayKey+"':");
        deviationReport.append(numberArrayToString(hBaseValue));
        deviationReport.append(" vs ");
        deviationReport.append(numberArrayToString(todayRedis.get(todayKey)));
        deviationReport.append("\n");
      }
      
      //today and yesterday contrast
      deviationReport = new StringBuilder();
      if(yesterdayRedis != null){
        if (! yesterdayRedis.containsKey(yesterdayKey)){
          deviationReport.append("yesterday redis doesn't contain key "+yesterdayKey);
        }else{
          if (contrast(todayRedis.get(todayKey), yesterdayRedis.get(yesterdayKey), T2YDeviation)){
            existDeviation = true;
            deviationReport.append("T2Y outside allowable deviation on ");
            deviationReport.append("'"+todayKey+"':");
            deviationReport.append(numberArrayToString(todayRedis.get(todayKey)));
            deviationReport.append(" vs ");
            deviationReport.append(numberArrayToString(yesterdayRedis.get(yesterdayKey)));
          }
        }
      }
      
      if(existDeviation){
        mismatching = true;
        LOG.error(indent+"index:"+todayDesc.getCacheKey());
        LOG.error(indent+deviationReport.toString());
      }
      
    }  
    if(!mismatching)  LOG.info(indent+"well matched");
    return mismatching;
  }
  private static String numberArrayToString(Number[] a){
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for(int i=0; i<a.length-1; i++){
      sb.append(a[i]);
      sb.append(",");
    }  
    sb.append(a[a.length-1]);
    sb.append("]");
    
    return sb.toString();
  }
  
  private static boolean contrast(Number[] a, Number[] b, double allowableDeviation){
    for(int i=0; i<3; i++){
      if(Math.abs(1-a[i].doubleValue()/b[i].doubleValue())>allowableDeviation){
        return true;
      }
    }
    return false;
  }
  public static boolean run(String project,String type,String event, String segmentJson, String attr, double O2ODeviation, double T2YDeviation) throws Exception{
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    today = df.format(DateUtils.previousDay(DateUtils.today()));
    yesterday = df.format(DateUtils.previousDay(DateUtils.yesterday()));
    
    String todaySegment = segmentJson.replace("date", today);
    String yesterdaySegment = segmentJson.replace("date", yesterday);
    
    double percent = 1;

    Map<Object, CopResultV2> todayHBase = null;
    FormulaQueryDescriptor todayDesc = null;
    FormulaQueryDescriptor yesterdayDesc = null;
    
    if(type.equals("common")){
      todayDesc = new FormulaQueryDescriptor(project, today,
        today, event,
        todaySegment, Filter.ALL, 0, 0, true, true, true,
        FormulaQueryDescriptor.CommonQueryType.NORMAL, FormulaQueryDescriptor.Interval.PERIOD);

      yesterdayDesc = new FormulaQueryDescriptor(project, yesterday,
        yesterday, event,
        yesterdaySegment, Filter.ALL, 0, 0, true, true, true,
        FormulaQueryDescriptor.CommonQueryType.NORMAL, FormulaQueryDescriptor.Interval.PERIOD);

       todayHBase = getCommonResultFromHBase(project,event,todaySegment,percent,today);
      
    }else if(type.equals("group_by_user_property")){

      todayDesc = new FormulaQueryDescriptor(project, today,
        today, event,
        todaySegment, Filter.ALL, 0, 0, true, true, true,
        attr, FormulaQueryDescriptor.Interval.PERIOD);

      yesterdayDesc = new FormulaQueryDescriptor(project, yesterday,
        yesterday, event,
        yesterdaySegment, Filter.ALL, 0, 0, true, true, true, 
        attr, FormulaQueryDescriptor.Interval.PERIOD);

      todayHBase = getGroupbyAttrResult(project, event, todaySegment, percent, today, attr);  

    }else if(type.equals("group_by_min5")){
      todayDesc = new FormulaQueryDescriptor(project,today,
        today,event,
        todaySegment, Filter.ALL,0 ,0, true, true, true,
        FormulaQueryDescriptor.GroupByQueryType.PERIOD, 5, FormulaQueryDescriptor.Interval.MIN5);

      yesterdayDesc = new FormulaQueryDescriptor(project,yesterday,
        yesterday,event,
        yesterdaySegment, Filter.ALL,0 ,0, true, true, true,
        FormulaQueryDescriptor.GroupByQueryType.PERIOD, 5, FormulaQueryDescriptor.Interval.MIN5);
      
      todayHBase = getGroupbyPeriod(project,event,todaySegment,percent,today,5);
    }else if(type.equals("group_by_hour")){
      todayDesc = new FormulaQueryDescriptor(project,today,
        today,event,
        todaySegment, Filter.ALL,0 ,0, true, true, true,
        FormulaQueryDescriptor.GroupByQueryType.PERIOD, 60, FormulaQueryDescriptor.Interval.HOUR);

      yesterdayDesc = new FormulaQueryDescriptor(project,yesterday,
        yesterday,event,
        yesterdaySegment, Filter.ALL,0 ,0, true, true, true,
        FormulaQueryDescriptor.GroupByQueryType.PERIOD, 60, FormulaQueryDescriptor.Interval.HOUR);

      todayHBase = getGroupbyPeriod(project,event,todaySegment,percent,today,60);
    }
    if (todayDesc != null && yesterdayDesc != null && todayHBase !=null){
      //get the day/yesterday corresponding result from redis
      LOG.debug(todayDesc.getCacheKey());
      XCache xCacheToday = NoSelectRedisXCacheOperator.getInstance().getCache(todayDesc.getCacheKey(),0);
      if(xCacheToday == null){
        LOG.error("redis doesn't contain the key:"+todayDesc.getCacheKey());
        return true; //give up contrast, cause H2R and T2Y both need today's data from redis 
      }
      Map<String, Number[]> todayRedis = xCacheToday.getValue();
      
      LOG.debug(yesterdayDesc.getCacheKey());
      XCache xCacheYesterday = NoSelectRedisXCacheOperator.getInstance().getCache(yesterdayDesc.getCacheKey(),0);
      Map<String, Number[]> yesterdayRedis = null;
      if(xCacheYesterday == null){
        LOG.error("redis doesn't contain the key:"+yesterdayDesc.getCacheKey()); // do H2R contrast only 
      }else{
        yesterdayRedis = xCacheYesterday.getValue();
      }
      
      if (deviationCheck(todayDesc, yesterdayDesc, O2ODeviation, T2YDeviation, todayRedis, yesterdayRedis, todayHBase)){
        return false;
      }
    }
    return true;

  }
  
}
