package it.myideas.mvn.jirachangelog;

import static org.junit.Assert.*;
import it.myideas.mvn.pomutils.AnnotatedDependency;
import it.myideas.mvn.pomutils.PomReader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.junit.Test;

/**
 * Read a sample pom.xml and verify that the {@link AnnotatedDependency} are parsed correctly
 * @author Tommaso Doninelli
 *
 */
public class PomParser {
	
	/**
	 * Test the parsing of the pom.xml
	 */
	@Test
	public void ParsePome(){
		File pomFile = new File("src/test/resources/sample-project/pom.xml");
		assertTrue("Missing pom.xml!", pomFile.exists());
	
		PomReader<AnnotatedDependency> reader = new PomReader<AnnotatedDependency>(pomFile, AnnotatedDependency.class);
		
		List<AnnotatedDependency> depList = reader.getElements();
		assertTrue("No annotated dependecy found in the pom!", depList.size() > 0);
		
		HashMap<String, AnnotatedDependency> depMap = new HashMap<String, AnnotatedDependency>(depList.size());
		for(AnnotatedDependency dep : depList){
			depMap.put(dep.getArtifactId(), dep);
		}
		
		assertNotNull("Dependency commons-io not found, or was not annotated", depMap.get("commons-io"));
		assertEquals("-SNAPSHOT has not been stripped from the version of commons.io", depMap.get("commons-io").getVersion(), "2.4-tde-2.2");
		
	}
	
	
}
