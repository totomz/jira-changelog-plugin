package it.myideas.mvn.pomutils;

import java.lang.reflect.Field;
import java.util.List;

import com.atlassian.jira.rest.client.domain.Issue;

/**
 * A maven <dependency> with the additiona configuration settings used by this plugin
 * @author Tommaso Doninelli
 *
 */
public class AnnotatedDependency {
	
    private String artifactId;
    private String version;
//	public String jiraUser;
//	public String jiraPasswd;
//	public String jiraUrl;
	private String projectKey;
	private String jql;	
	private List<Issue> issues;
    private String originalVersion;
	
	public AnnotatedDependency(){}
	
	public String getArtifactId() {
		return artifactId;
	}
	
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getJiraVersion(){
	    return this.originalVersion;
	}
	
	public void setVersion(String version) {
	    this.originalVersion = version;
		version = version.replace("-SNAPSHOT", "");	// strip -SNAPSHOT from the version
		this.version = version;
	}
	
	
    public List<Issue> getIssues() {
        return issues;
    }
	
//	public String getJiraUser() {
//		return jiraUser;
//	}
//
//	public void setJiraUser(String jiraUser) {
//		this.jiraUser = jiraUser;
//	}
//
//	public String getJiraPasswd() {
//		return jiraPasswd;
//	}
//
//	public void setJiraPasswd(String jiraPasswd) {
//		this.jiraPasswd = jiraPasswd;
//	}
//
//	public String getJiraUrl() {
//		return jiraUrl;
//	}
//
//	public void setJiraUrl(String jiraUrl) {
//		this.jiraUrl = jiraUrl;
//	}

	public String getJql() {
		return jql;
	}

	public void setJql(String jql) {
		this.jql = jql;
	}

	@Override
	public String toString() {
		
		return "artifactId:" + this.artifactId + ", pomVersion:" + this.version;
	}

	public String projectKey() {
		return (this.projectKey != null)?this.projectKey:this.artifactId;
	}

	public void setIssues(List<Issue> issues) {
		this.issues = issues;
	}
	
	
	
	
}
