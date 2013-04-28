package com.xingcloud.xa.qa.cases;

import org.junit.Test;

public class ${project}Test{
  #foreach ( $event in $events )
  @Test
  public void test${event}() throws Exception{
    System.out.println("testing$event!");
  }
  #end
}