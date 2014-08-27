package it.myideas.mvn.jirachangelog.freemarker;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TestFreemarkerTemplate {

	@Test
	public void executeTemplate() throws IOException, TemplateException {
		Configuration cfg = new Configuration();
		
		File f = new File("");
		System.out.println(f.getAbsolutePath());
		
		Template template = cfg.getTemplate("src/main/resources/simple.ftl");
		Map<String, Object> data = new HashMap<String, Object>();
        data.put("user", "Hello World!");
        
        Writer out = new OutputStreamWriter(System.out);
        template.process(data, out);
        
        out.flush();
		
	}
	
	
}


