package com.tbocek.tools.combatmap;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.tbocek.android.combatmap.R;

/**
 * Optimizes art credits file by replacing resource names with resource IDs.
 * @author Tim
 *
 */
public class OptimizeArtCredits {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			File inputXmlFile = new File(args[1]);
			File outputXmlFile = new File(args[2]);
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			
			Document doc = dBuilder.parse(inputXmlFile);
			
			walkDom(doc.getDocumentElement());
			
			 // Use a Transformer for output
			 TransformerFactory tFactory =
			    TransformerFactory.newInstance();
			 Transformer transformer = tFactory.newTransformer();

			 DOMSource source = new DOMSource(doc);
			 FileOutputStream out = new FileOutputStream(outputXmlFile);
			 StreamResult result = new StreamResult(out);
			 transformer.transform(source, result); 
			 out.close();
			

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Walks the DOM, replacing node elements with their
	 * @param element
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */
	private static void walkDom(Element node) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
		String res = node.getAttribute("res");
		if (res != null) {
			int resourceId = R.drawable.class.getDeclaredField(res).getInt(null);
			node.setAttribute("res", Integer.toString(resourceId));
		}
		
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			if (children.item(i) instanceof Element) {
				walkDom((Element) children.item(i));
			}
		}
	}
}
