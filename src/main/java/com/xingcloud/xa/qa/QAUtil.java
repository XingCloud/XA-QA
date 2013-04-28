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
  
  public List<Map<String, String>> getIndexs() throws Exception {
    List<Dom> domList = ConfigReader.getDomList("mi.xml", "monitor", "index");
    List<Map<String, String>> indexs = new ArrayList<Map<String, String>>();
    for(Dom dom:domList){
      Map<String, String> index = new HashMap<String, String>();
      index.put("project", dom.elementText("project"));
      index.put("type", dom.elementText("type"));
      index.put("event", dom.elementText("event"));
      index.put("segmentJson","TOTAL_USER");
      index.put("attr",dom.elementText("property"));
      indexs.add(index);          
    }

    return indexs;
  }
}
