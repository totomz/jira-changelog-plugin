package it.myideas.mvn.jirachangelog.rest;

import com.atlassian.jira.rest.client.ProgressMonitor;

/**
 * No-op class for further implementaiton 
 * see {@link ProgressMonitor}
 * @author Tommaso Doninelli
 *
 */
public class JIRARequestProgress implements ProgressMonitor {

	/**
	 * Returns an empty instance of this this class
	 */
	public static final JIRARequestProgress EMPTY_PROGRESS = new JIRARequestProgress();
	
}
