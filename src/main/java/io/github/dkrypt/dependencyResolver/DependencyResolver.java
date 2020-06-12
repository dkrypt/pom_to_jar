package io.github.dkrypt.dependencyResolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Hello world!
 *
 */
public class DependencyResolver
{
	private static void logger(String level, String message) {
		System.out.println(new Date() +" "+level.toUpperCase()+ " PomParser - "+message);
		
	};
	
	private static String getContextPath(String gid, String aid, String v)
	{
		return gid.replaceAll("\\.", "/")
				.concat("/")
				.concat(aid)
				.concat("/")
				.concat(v)
				.concat("/")
				.concat(aid)
				.concat("-")
				.concat(v)
				.concat(".jar");
	}
	public static void main(String[] args) {
		if(args.length != 2) {
			logger("info","Incorrect Usage.");
			logger("info","Correct Usage: pomParser <input-file> <output-directory> ");
			System.exit(0);
		}
		
		String input = args[0]; // Read first arg, filename
		String output = args[1]; // Read second arg, output directory name
		
		Path inputFilePath = Paths.get(args[0]).toAbsolutePath().normalize();
		Path outputFolderPath = Paths.get(args[1]).toAbsolutePath().normalize();
		
		final Path Base_Dir = Paths.get(".");
		
		// Read File
		File file = new File(inputFilePath.toString());
		// Create OutputDir
		File outputFolder = new File(outputFolderPath.toString());
		outputFolder.mkdirs();
		
		logger("info","Processing :\nFile = "+file.getName()+"\nOutput Dir = "+outputFolder);
		
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			NodeList deps = doc.getDocumentElement().getChildNodes();
			
			for (int itr = 0; itr < deps.getLength(); itr++) {
				Node dep = deps.item(itr);
				if (dep.getNodeType() == Node.ELEMENT_NODE)  {
					Element eElement = (Element) dep;  
					String groupId = eElement.getElementsByTagName("groupId").item(0).getTextContent();
					String artifactId = eElement.getElementsByTagName("artifactId").item(0).getTextContent();
					String version = eElement.getElementsByTagName("version").item(0).getTextContent();
					logger("info","Handling Dependency : " + groupId+":"+artifactId+":"+version);
					
					String jarName = artifactId.concat("-").concat(version).concat(".jar");
					String jarNameFull = outputFolderPath + "\\" + jarName;
					
					if(!new File(jarNameFull).exists()) {
						logger("info","Dependency : " + groupId+":"+artifactId+":"+version+" - not Found. Attempting to Download...");
						String contextPath = getContextPath(groupId, artifactId, version);
						URL mavenRepo = new URL("https://repo1.maven.org/maven2/"+contextPath);
						ReadableByteChannel rbc = Channels.newChannel(mavenRepo.openStream());
						FileOutputStream fos = new FileOutputStream(Base_Dir+"\\"+output+"\\"+jarName);
						fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					}else {
						logger("info","Dependency Exists. Skip Download : " + groupId+":"+artifactId+":"+version);
					}
				}
			}
		} catch (SAXException e) {
			logger("error", "SAXException occurred "+ e.getStackTrace());
		} catch (IOException e) {
			logger("error","IOException occurred "+ e.getStackTrace());
		} catch (ParserConfigurationException e) {
			logger("error","ParserConfigurationException occurred "+ e.getStackTrace());
		}
	}
}
