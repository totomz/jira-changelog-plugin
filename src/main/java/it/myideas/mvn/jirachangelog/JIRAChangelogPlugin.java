package it.myideas.mvn.jirachangelog;

import it.myideas.mvn.jirachangelog.rest.JIRAConnector;
import it.myideas.mvn.pomutils.AnnotatedDependency;
import it.myideas.mvn.pomutils.PomReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.AbstractOwnableSynchronizer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.views.AbstractView;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.domain.Issue;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * This {@link Mojo} check the pom.xml for <dependency> annotated with @JIRAChangeLog 
 * @author Tommaso Doninelli
 *
 */
@Mojo(name = "get-changelog")
public class JIRAChangelogPlugin extends AbstractMojo {

	@Component private MavenProject pom;	
	@Parameter private String jiraurl;
	@Parameter private String jiraUser;
	@Parameter private String jiraPassword;
	@Parameter private String tplName;
	@Parameter private File outputFile;
	@Parameter private HashMap<String, Object> tplData;  // Stuff that is given to the template
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		/*
		 * 1) Parse the pom.xml, looking for annotated dependencies
		 * 2) For each dependency, download the issues from JIRA
		 * 3) Initialize a freemarker template. 
		 *    Once everything is setup, put the array pf dependency and issues in the template. 
		 * 4) Enjoy
		 */
		
		JIRAConnector jira = new JIRAConnector(jiraurl, jiraUser, jiraPassword);
		String jql;
		
		PomReader<AnnotatedDependency> reader = new PomReader<AnnotatedDependency>(pom.getFile(), AnnotatedDependency.class);
		List<AnnotatedDependency> dependencies = reader.getElements();

		for(AnnotatedDependency dep : dependencies) {
			getLog().info("Loading JIRA issues for " + dep.getArtifactId());
			
			// Build a valid JQL query
			if(dep.getJql() != null){
				jql = dep.getJql();
			}
			else {
				jql = String.format("status in (Resolved, Closed) and project = %s and fixVersion = %s", dep.projectKey(), dep.getVersion());
			}
			
			getLog().debug("JQL=" + jql);			
			dep.setIssues(jira.executeFullSearch(jql));
		}
		
		// Setup the template
		Configuration cfg = new Configuration();
		Template template = null;
		
		try {			
			if(tplName == null) {
				getLog().info("Loading default tepmlate");
				
				cfg.setTemplateLoader(new ClassTemplateLoader(getClass(), "/"));
				template = cfg.getTemplate("changelog.ftl");
			}
			else {
				getLog().error("Custom template not implemented yet; sorry");
				throw new MojoFailureException("Custom template are not supported yet - remove the config");
			}
		
			tplData.put("pom", pom);
			tplData.put("dependencies", dependencies);
			
			try(FileWriter out = new FileWriter(outputFile)){
				template.process(tplData, out);
		        out.flush();
			}
	        catch (Exception e) {
	        	throw new MojoFailureException("Error writing output", e);
			}
		}
		catch(Exception e){
			getLog().error(e.getMessage());
			throw new MojoFailureException("Impossible to show a FreemarkerTemplatedMessage :(", e);
		}
		
	}

}
