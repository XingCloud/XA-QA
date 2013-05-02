package com.xingcloud.xa.qa.cases;

import com.xingcloud.xa.qa.TestHelper;
import org.junit.Test;
import static org.junit.Assert.*;

public class ${project}Test{
  #foreach ( $index in $indexs )
  @Test
  public void test_${index.get("identifier")}() throws Exception{
    assertTrue(TestHelper.run("$project", "${index.get('type')}", "${index.get('event')}", "$index.get('segmentJson')", "$index.get('attr')", $index.get('O2ODeviation'), $index.get('T2YDeviation')));   
  }
  #end
}