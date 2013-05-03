package com.xingcloud.xa.qa;

import com.xingcloud.basic.conf.ConfigReader;
import com.xingcloud.basic.conf.Dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: wangchangli
 * Date: 4/28/13
 * Time: 10:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class QAUtil {
  private static QAUtil instance = new QAUtil();
  public static QAUtil getInstance(){
    return instance;
  }
  
  public Map<String, List<Map<String, Object>>> getIndexs() throws Exception {
    List<Dom> domList = ConfigReader.getDomList("mi-age.xml", "monitor", "index");

    Map<String, List<Map<String, Object>>> indexs = new HashMap<String, List<Map<String, Object>>>();
    for(Dom dom:domList){
      
      String project =  dom.elementText("project");
      if(! indexs.containsKey(project)){
        indexs.put(project, new ArrayList<Map<String, Object>>());
      }
      Map<String, Object> index = new HashMap<String, Object>();
      index.put("type", dom.elementText("type"));
      index.put("event", dom.elementText("event"));
      if(dom.existElement("segment")){
        index.put("segmentJson", dom.elementText("segment"));
      }else{
        index.put("segmentJson","TOTAL_USER");
      }
      String[] deviation = dom.elementText("deviation").split("#");
      double O2ODeviation = Double.valueOf(deviation[0]);
      double T2YDeviation = Double.valueOf(deviation[1]);
      index.put("O2ODeviation", O2ODeviation);
      index.put("T2YDeviation", T2YDeviation);
      if (dom.elementText("type").equals("group_by_user_property")){
        index.put("attr",dom.elementText("property"));
      }else{
        index.put("attr","");
      }
      
      index.put("identifier", (index.get("type")+"_"+((String)index.get("event")).replace(".","dot").replace("*","star")));
      indexs.get(project).add(index);         
    }

    return indexs;
  }
}
