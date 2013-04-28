/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.xingcloud.xa.qa;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class CaseGenerator {
  public static void main(String[] args) throws IOException {
    doGenerateCases();
  }

  private static void doGenerateCases() throws IOException {
    System.out.println("TODO GENERATING...");
    
    Template template = getTemplate("src/main/resources/TestTemplate.java");
    generateTestFor(template, "Age", "Pay", "Visit");
  }

  private static void generateTestFor(Template template, String project, String... events) throws IOException {
    Context context = new VelocityContext();
    context.put("project", project);
    context.put("events", events);
    StringWriter writer = new StringWriter();
    template.merge(context, writer);
    System.out.println("template = " + template);
    saveFile(writer.toString(), "src/test/java/com/xingcloud/xa/qa/cases/"+project+"Test.java");
  }

  private static void saveFile(String src, String path) throws IOException {
    FileWriter writer = new FileWriter(path);
    writer.append(src);
    writer.flush();
    writer.close();
  }
  private static Template getTemplate(String path) throws IOException {
     VelocityEngine engine = new VelocityEngine();
     engine.init();
     return engine.getTemplate(path);
   }
  
}
