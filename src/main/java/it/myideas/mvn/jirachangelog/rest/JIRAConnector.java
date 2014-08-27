package it.myideas.mvn.jirachangelog.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;

public class JIRAConnector {
	
	private String uri;
	private String username;
	private String password;
	private JiraRestClient jira;
	
	public JIRAConnector(String uri, String username, String password) {
		super();
		this.uri = uri;
		this.username = username;
		this.password = password;
		jira = JIRAConnector.getClient(uri, username, password);
	}

	public static JiraRestClient getClient(String uri, String username, String password) {
		
		JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
		JiraRestClient jira = null;
		try {
			jira = factory.createWithBasicHttpAuthentication(new URI(uri), username, password);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}		
		return jira;
	}
	
	/**
	 * Execute a JQL search and returns a list of {@link Issue} with all their fields.
	 * @param jql
	 * @return
	 */
	public List<Issue> executeFullSearch(String jql) {
	
//		System.out.println("Start jql search");
//		long start = System.currentTimeMillis();
//		SearchResult search = jira.getSearchClient().searchJql("project = \"TORINO\" and status not in (Resolved, Closed)", null);
//		ArrayList<Issue> results = new ArrayList<Issue>(search.getTotal());
//		long elapsed = System.currentTimeMillis() - start;
//		System.out.println("Found " + search.getTotal() + " elements in " + elapsed + "ms. Looking for issue properties");
		
		// The first search returns a lis of issue ID. Then, with a threadpool,
		// get all the features attributes
		ExecutorService executor = Executors.newFixedThreadPool(10);
		List<Future<Issue>> futureIssues = new ArrayList<Future<Issue>>();
		
		long start = System.currentTimeMillis();
		
		System.out.println("Performing JQL search");
		SearchResult search = jira.getSearchClient().searchJql(jql , JIRARequestProgress.EMPTY_PROGRESS);
		
		// The results are paged (fuck!)
		int featurePerPage = search.getMaxResults();
		int featureToGet = search.getTotal();
		int featureCurrentCursor = 0;
		
		System.out.println("Found " + featureToGet + " issues. Start getting attributes");
		ArrayList<Issue> results = new ArrayList<Issue>(featureToGet);
		
		boolean doNewRequest = false;		
		
		do {
			
			Iterator<BasicIssue> basicIssues = search.getIssues().iterator();
			while(basicIssues.hasNext()) {
				Future<Issue> issue = executor.submit(new QueryIssue(basicIssues.next().getKey()));
				futureIssues.add(issue);
				featureCurrentCursor++;
			}
			
			doNewRequest = (featureToGet > featureCurrentCursor);
			
			if(doNewRequest) {
				search = jira.getSearchClient().searchJql(jql, featurePerPage, featureCurrentCursor, JIRARequestProgress.EMPTY_PROGRESS);
			}				
		}while(doNewRequest);
		
		System.out.println("Waiting for requests to end");
		executor.shutdown();
		
		try {
			executor.awaitTermination(20, TimeUnit.MINUTES);			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for(Future<Issue> fut : futureIssues){
			try {
				results.add(fut.get());
			} catch (InterruptedException | ExecutionException e) {
				System.out.println("Error getting Issue attributes");
				e.printStackTrace();
			}
		}
		
		System.out.println("Done getting issues in " + (System.currentTimeMillis() - start) + "ms");
		
		return results;
	}
	
	class QueryIssue implements Callable<Issue> {

		private String issueKey;
		
		public QueryIssue(String issueKey) {
			this.issueKey = issueKey;
		}
		
		@Override
		public Issue call() throws Exception {
			JiraRestClient _jira = JIRAConnector.getClient(uri, username, password);
//			System.out.println("    - " + issueKey + " STARTED");
			Issue issue = _jira.getIssueClient().getIssue(issueKey, null);
//			System.out.println("    - " + issueKey + " END");
			return issue;
		}
		
	}
}
