package it.myideas.mvn.jirachangelog;

import it.myideas.mvn.jirachangelog.rest.JIRAConnector;
import it.myideas.mvn.jirachangelog.rest.JIRARequestProgress;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.domain.User;

/**
 * Connect to JIRA and perform simple tasks
 * (not really a unit test, it's more for testing the REST API)
 * @author Tommaso Doninelli
 *
 */
public class JIRAClientTest {

	private String url = "http://jira.sistemaits.com";
	private String username = "tommaso.doninelli";
	private String password = "doninelli";
	
//	private String jqlQuery = "project = \"TORINO\" and status not in (Resolved, Closed)";
	private String jqlQuery = "project = HARMONIZER AND fixVersion = whish-list";
	
	
	@Test
	public void testJiraConnection() throws URISyntaxException {
		
		JiraRestClient jira = JIRAConnector.getClient(url, username, password);	
		User me = jira.getUserClient().getUser("tommaso.doninelli", JIRARequestProgress.EMPTY_PROGRESS);
		
		assertNotNull("Can not connect to JIRA", me);		
		assertEquals("JIRA getUser() did not return my name :O", username, me.getName());				
	}
	
	@Test
	public void testJqlSearch() {
		JIRAConnector jira = new JIRAConnector(url, username, password);
		
		System.out.println("Executing a direct JQL search");
		JiraRestClient jiraDirect = JIRAConnector.getClient(url, username, password);
		SearchResult search = jiraDirect.getSearchClient().searchJql(jqlQuery, null);
		
		System.out.println("Expecting a resultset with " + search.getTotal());
		
		System.out.println("Starting JQL Search");		
		List<Issue> issues = jira.executeFullSearch(jqlQuery);		
		assertNotNull(issues);	
		 
		for(int i=0; i<2; i++){
		    System.out.println("-----------");
			Issue issue = issues.get(i);
			System.out.println(issue.getKey());
			System.out.println(issue.getIssueType().getName());
			System.out.println(issue.getSelf());
			System.out.println(issue.getStatus().getName());
			System.out.println(issue.getResolution());
			System.out.println(issue.getPriority().getName());
			System.out.println(issue.getDescription());
			System.out.println(issue.getSummary());		
		}
		
		assertEquals("Not all expected issues have been returned!", search.getTotal(), issues.size());
	}
	
}
