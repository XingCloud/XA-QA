package com.xingcloud.xa.qa;

import com.xingcloud.adhocprocessorV2.hbase.model.CopResultV2;
import com.xingcloud.adhocprocessorV2.query.XAQueryDEUCoprocessor;
import com.xingcloud.adhocprocessorV2.query.model.Filter;
import com.xingcloud.adhocprocessorV2.query.model.FormulaQueryDescriptor;
import com.xingcloud.basic.conf.Dom;
import com.xingcloud.basic.utils.DateUtils;
import org.junit.Test;
import com.xingcloud.basic.conf.ConfigReader;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: wangchangli
 * Date: 4/28/13
 * Time: 6:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class QATest {
  
  private Map<String,Number[]> getCacheKeyValue(FormulaQueryDescriptor formulaQueryDescriptor, Map<Object, CopResultV2> result){
    
    //todo: wzj
    return null;
  } 
  
  @Test
  public void testQA() throws Exception {
    List<Dom> domList = ConfigReader.getDomList("mi.xml", "monitor", "index");  
    for(Dom dom:domList){
      String projectId = dom.elementText("project");
      String type = dom.elementText("type");
      String eventFilter = dom.elementText("event");
      String segmentJson = "TOTAL_USER";
      SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
      String startDate = df.format(DateUtils.today());
      String endDate = df.format(DateUtils.today());
      String yesterday = df.format(DateUtils.yesterday());
      double percent = 1;
      Filter filter = Filter.ALL;
      
      
      if(type.equals("common")){
        FormulaQueryDescriptor desc = new FormulaQueryDescriptor(projectId, startDate, 
          endDate, eventFilter, 
          segmentJson, Filter.ALL, 0, 0, true, true, true, FormulaQueryDescriptor.CommonQueryType.NORMAL);
        Set<FormulaQueryDescriptor> descs = new HashSet<FormulaQueryDescriptor>();
        descs.add(desc);
        Map<FormulaQueryDescriptor, Map<Object, CopResultV2>> resultMap = XAQueryDEUCoprocessor.getInstance().queryBatchCommon(projectId, startDate, 
          endDate, eventFilter, 
          descs, percent);
        desc.getCacheKey();
        
      }else if(type.equals("group_by_user_property")){
        String attr = dom.elementText("property");
        FormulaQueryDescriptor desc = new FormulaQueryDescriptor(projectId, startDate, 
          endDate, eventFilter, 
          segmentJson, Filter.ALL, 0, 0, true, true, true, attr, FormulaQueryDescriptor.Interval.DAY);
        
        Map<Object, CopResultV2> result = XAQueryDEUCoprocessor.getInstance().queryAllGroupByAttr(projectId, segmentJson,
          eventFilter, filter,  
          startDate, endDate, 
          attr, percent);
        
        Map<String, Number[]> cacheKeyValue = getCacheKeyValue(desc, result);
        for(Map.Entry<Object, CopResultV2> entry: result.entrySet()){
          long userNum = entry.getValue().getUserNum();
          long eventNum = entry.getValue().getEventNum();
          long eventAmount = entry.getValue().getEventAmount();
          
          //todo:wzj get the day/yesterday corresponding result from redis
          
          
        }
          
        
        for (Map.Entry<Object, CopResultV2> entry : result.entrySet()) {
          CopResultV2 resultTuple = entry.getValue();
          System.out.println(entry.getKey() + " " + resultTuple.toString());
        }

      }else if(type.equals("group_by_min5")){
        
      }else if(type.equals("group_by_hour")){
      
      }
      
    }
  } 
  
}
