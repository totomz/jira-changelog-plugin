package it.myideas.mvn.pomutils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class PomReader<T> {

	private String TAG_NAME = "@JIRAChangeLog";	// I will look for this in the comments
	private String PARENT_NODE = "dependency";		// I will check comments of this node only
	private Class<T> CLAZZ_TO_ENRICH;	// I will unmarshall this class
	
	private Document pomDoc;
	private XPath xpath;
	
	private ArrayList<T> elements;
	
	public PomReader(File file, Class clazz){
		if(file == null || !file.exists()){
			throw new RuntimeException("File not found");
		}
		
		this.CLAZZ_TO_ENRICH = clazz;
		
		XPathExpression listDependency = null;
		XPathExpression getComment = null;
		
        try {
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            pomDoc = builder.parse(file);
            
            XPathFactory xPathfactory = XPathFactory.newInstance();
            xpath = xPathfactory.newXPath();
    
            listDependency = xpath.compile("//" + PARENT_NODE);
            getComment = xpath.compile("comment()[contains(., '"+TAG_NAME+"')][1]");
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        
        // For each <dependency>
        try {        	
			NodeList dependencies = (NodeList)listDependency.evaluate(pomDoc, XPathConstants.NODESET);
			elements = new ArrayList<T>();
			
			for(int n=0;n<dependencies.getLength(); n++) {
				
				Node node = dependencies.item(n);
				
				// Check if node is annotated
				Node comment = (Node)getComment.evaluate(node, XPathConstants.NODE);
				if(comment == null){
					continue;
				}
				
				// Create a new annotated element
				// I will use reflection for custom attributes.
				String commentToParse = comment.getTextContent();
				
				// Instantiate the container for the fields
				T obj;
				try {
					obj = CLAZZ_TO_ENRICH.newInstance();

					// Set the xml attributes
					NodeList cc = node.getChildNodes();
					for(int i=0; i<cc.getLength();i++){
						Node child = cc.item(i);
						String nodeName = child.getNodeName();
						if(nodeName.equalsIgnoreCase("comment" ) || nodeName.equalsIgnoreCase("text")){
							continue;
						}
						setFieldValue(obj, nodeName, child.getTextContent());
					}
					
					// Now set the custom attibutes
					String[] lines = commentToParse.split("\n");
					for(String l : lines) {
						l = l.trim();
						if(l.length() == 0){continue;}
						String[] attrs = getFieldAndValue(l);						
						setFieldValue(obj, attrs[0], attrs[1]);
					}
				
					elements.add(obj);
					
				} catch (InstantiationException | IllegalAccessException e) {				
					e.printStackTrace();
				} 
			}						
		} 
        catch (XPathExpressionException e) {
			e.printStackTrace();
		}       	
	}
	
	public List<T> getElements(){
		return elements;
	}
	
	/**
	 * 
	 * @return [0] fieldName, [1] fieldValue
	 */
	private String[] getFieldAndValue(String string) {		
		String[] parts =  string.split("=");
		parts[0] = parts[0].replaceAll(" ", "").replaceAll("@", "");
		if(parts.length == 1){
			return new String[]{parts[0], null};
		}
		else {
			parts[1] = parts[1].trim();
		}
		return parts;
	}
	
	private void setFieldValue(Object obj, String propertyName, Object value)  {
		
		try {
			
			Method[] setters = obj.getClass().getDeclaredMethods();
			boolean set = false;
			for(Method setter : setters) {
				
				if(set){
					break;
				}
				
				if(setter.getName().equalsIgnoreCase("set"+propertyName)){
					
					try {
						setter.invoke(obj, value);
						set = true;
					}
					catch(Exception e){}					
				}				
			}
			
		}
		catch(Exception e){
			//e.printStackTrace();
		}
	}
	
	public Model getModel(String fileName) {
		DefaultModelBuildingRequest req = new DefaultModelBuildingRequest();
        req.setProcessPlugins(false);
        req.setPomFile(new File(fileName));
        req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        
        Model pom = null;
        
        try {
            pom = new DefaultModelBuilderFactory().newInstance().build(req).getEffectiveModel();
        }
        catch (ModelBuildingException e1) {
            e1.printStackTrace();
        }
        
        return pom;
	}
	
}
