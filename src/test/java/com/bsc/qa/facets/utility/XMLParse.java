package com.bsc.qa.facets.utility;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * To parse the XML file
 *
 */
public class XMLParse {

	private Document document = null;
//	private String strUniqueData = null;
	
	public Map<String, String> getXmlDataMap(String tag, String claimID) {
		 Map<String, String> xmlMap = new HashMap<String, String>();
		 
		// Validating tags to get the claim information details
		if ("THG835X2_2100".equalsIgnoreCase(tag)) {
			
			xmlMap.putAll(getElementDataByClaimId(tag, claimID));
		} 
		else {
			xmlMap.putAll(getElementData(tag));
		}	
		return xmlMap;
	}

	/**
	 * To create instance of document builder for the XML
	 * 
	 * @param xmlPath : XML file complete path
	 */
	public XMLParse(String xmlPath) {
		DocumentBuilder documentBuilder;
		File fXmlFile = new File(xmlPath);// Creating file for the given path
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

		try {
			// creating instance for the DocumentBuilder
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			document = documentBuilder.parse(fXmlFile);
		}
		catch (Exception e) {
			System.out.println("Failed due to Exception on " + fXmlFile);
			e.printStackTrace();
		}
	}

	/**
	 * To extract Unique section from XML 
	 * 
	 * @param tagName          : Parent tag name
	 * @param cliamID     : Unique ID to fetch specific section
	 */
	public Map<String, String> getElementDataByClaimId(String tagName, String claimID) {
		Map<String, String> elementClaimIDDataMap= new HashMap<String, String>();

		NodeList pagesList = document.getElementsByTagName(tagName);
		// Checking each parent tag sections in the XML using for loop
		for (int nodeNo = 0; nodeNo < pagesList.getLength(); nodeNo++) {
			Node node = (Node) pagesList.item(nodeNo);
			if (hasChildElements((Element) node)) {
				try {
					elementClaimIDDataMap.putAll(parseChildElements((Element) node, claimID, tagName, (Element) node));
				} catch (Exception e) {
					System.out.println("Failed due to Exception : " + e);
					e.printStackTrace();
				}

			}
		}
		return elementClaimIDDataMap;
	}

	public Map<String, String> getElementData(String tagName) {
		Map<String, String> elementDataMap =new HashMap<String, String>();
		NodeList pagesList = document.getElementsByTagName(tagName);
		// Checking each parent tag sections in the XML using for loop
		for (int nodeNo = 0; nodeNo < pagesList.getLength(); nodeNo++) {
			Node node = (Node) pagesList.item(nodeNo);
			// Calling recursive function to fetch and compare tag values with database values
			if (hasChildElements((Element) node)) {
				try {
					elementDataMap.putAll(parseChildElements((Element) node, tagName, (Element) node));
				} catch (Exception e) {
					System.out.println("Failed due to Exception : " + e);
					e.printStackTrace();
				}
			}
		}
		return elementDataMap;
	}

	/**
	 * To extract Unique data from XML
	 * 
	 * @param tagName          : Primary parent tag name
	 * @param strUniqueTagName : Unique tag name
	 * @return : To return all unique values with "|" this delimiter
	 */

	public String uniqueDetailsExtraction(String tagName, String strUniqueTagName) {

		String strUniqueData = null;
		// To get the elements using tagName
		NodeList pagesList = document.getElementsByTagName(tagName);
		// Checking each parent tag sections in the XML using for loop
		for (int nodeNo = 0; nodeNo < pagesList.getLength(); nodeNo++) {
			Node node = (Node) pagesList.item(nodeNo);
			// To check the node when it is having child elements
			if (hasChildElements((Element) node)) {
				// To extract Unique tag data from XML
				parseChildElementsForUniqueIDs((Element) node, strUniqueTagName);
			}
		}
		// Returning unique data with pipe (|) delimiter
		return strUniqueData;
	}

	/**
	 * To extract Unique data from XML
	 * 
	 * @param nNode            : Parent node to retrieve child nodes along with tag
	 *                         values
	 * @param strUniqueTagName : Unique tag name to retrieve corresponding value
	 */
	public String parseChildElementsForUniqueIDs(Element nNode, String strUniqueTagName) {
		String elementName = "";
		String elementvalue = "";
		String strUniqueData = "";
		NodeList children = nNode.getChildNodes();// get child nodes of node
		for (int i = 0; i < children.getLength(); i++) { // loop through all child nodes
			Node node = (Node) children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				elementName = node.getNodeName().trim().toString();
				elementvalue = node.getTextContent().trim().toString();
				// Checking unique element name in the XML
				if (elementName.equalsIgnoreCase(strUniqueTagName)) {
					// Storing Unique data into strUniqueData variable with pipe (|) delimiter
					strUniqueData = strUniqueData + "|" + elementvalue;


				} else if (hasChildElements((Element) node)) {// checking for the child elements
					// calling recursive function to fetch data from child
					parseChildElementsForUniqueIDs((Element) node, strUniqueTagName);
				}
			}
		}
		return strUniqueData;
	}

	/**
	 * Recursive method to parse all the elements under the node *
	 * 
	 * @param nNode            : Node name for extraction
	 * @param claimId          : Unique id to retrieve matching section from XML
	 * @param tagname          : Tag Name
	 * @param pNode            : Parent node name
	 */
	public Map<String, String> parseChildElements(Element nNode, String claimId, String tagname,
			Element pNode) {
		 Map<String, String> parseChildElements= new HashMap<String, String>();
		NodeList children = nNode.getChildNodes();// get child nodes of node
		for (int i = 0; i < children.getLength(); i++) { // loop through child nodes

			Node node = (Node) children.item(i);
			// Checking for the element node
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String elementName = node.getNodeName().trim().toString();
				String elementvalue = node.getTextContent().trim().toString();

				if ("THG835X2_2100_CLP07__PayerClaimControlNumber".equalsIgnoreCase(elementName)
						&& elementvalue.equalsIgnoreCase(claimId)) {
					parseChildElements.putAll(fetchElementAtttributes((Element) pNode));
					parseChildElements.putAll(fetchChildElements((Element) pNode, claimId, tagname));
				} else if (hasChildElements((Element) node)) {// checking for child nodes
					// Calling the recursive method inside the same method
					parseChildElements.putAll(parseChildElements((Element) node, claimId, tagname, pNode));
				}
			}
		}
		return parseChildElements;
	}

	public Map<String, String> parseChildElements(Element nNode, String tagname, Element pNode) {
		Map<String, String> parseChildElements= new HashMap<String, String>();
		NodeList children = nNode.getChildNodes();// get child nodes of node
		for (int i = 0; i < children.getLength(); i++) { // loop through child nodes

			Node node = (Node) children.item(i);
			// Checking for the element node
			if (node.getNodeType() == Node.ELEMENT_NODE) {

				parseChildElements.putAll(fetchElementAtttributes((Element) pNode));
				// getting attributes of elements
				parseChildElements.putAll(fetchChildElements((Element) pNode,tagname));
				if (hasChildElements((Element) node)) {// checking for child nodes
					// Calling the recursive method inside the same method
					parseChildElements.putAll(parseChildElements((Element) node, tagname, pNode));
				}
			}
		}
		return parseChildElements;
	}

	public Map<String, String> fetchElementAtttributes(Element node) {
		Map<String, String> fecthAttributes= new HashMap<String, String>();

		if (node.hasAttributes()) {// checking for attributes in node
			NamedNodeMap attributesList = node.getAttributes();// all attributes in node
			// Loop through all attribute lists
			for (int attributeNumber = 0; attributeNumber < attributesList.getLength(); attributeNumber++) {
				Attr attr = (Attr) attributesList.item(attributeNumber);
				String attributeName = attr.getNodeName();// Attribute name capture
				String attributeValue = attr.getNodeValue();// attribute value capture
				fecthAttributes.put(attributeName.trim().toString().toUpperCase(), attributeValue.replaceAll("\\s+", ""));
			}
		}
		return fecthAttributes;
	}

	/**
	 * 
	 * @param node         : Node name for comparison
	 * @param claimID : Unique id to replace in the query
	 */

	public Map<String, String> fetchValuesOfElement(Element node, String claimID) {
		
		Map<String, String> fecthValues= new HashMap<String, String>();

		fecthValues.put(node.getNodeName().trim().toString().toUpperCase(), node.getTextContent().replaceAll("\\s+", ""));
		return fecthValues;

	}

	public Map<String, String> fetchValuesOfElement(Element node) {
		Map<String, String> fetchValuesOfElement= new HashMap<String, String>();
		fetchValuesOfElement.put(node.getNodeName().trim().toString().toUpperCase(), node.getTextContent().replaceAll("\\s+", ""));
		return fetchValuesOfElement;
	}

	/**
	 * To retrieve child nodes of node
	 * 
	 * @param nNode        : Main node name
	 * @param claimID : To retrieve unique specific section
	 * @param tagname      : Tag Name
	 */
	public Map<String, String> fetchChildElements(Element nNode, String claimID, String tagname) {
		Map<String, String>fetchChildElements= new HashMap<String, String>();
		NodeList children = nNode.getChildNodes();// To get child nodes of node

		// loop through all child nodes
		for (int i = 0; i < children.getLength(); i++) {
			Node node = (Node) children.item(i);
			// Checking for the element node
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (hasChildElements((Element) node)) {// checking for child nodes
					fetchChildElements.putAll(fetchElementAtttributes((Element) node));
					fetchChildElements.putAll(fetchChildElements((Element) node, claimID, tagname));// getting child elements
					{
						if (node.hasAttributes()) // Checking for the node attributes

						{
							NamedNodeMap attributesList = node.getAttributes();
							for (int attributeNumber = 0; attributeNumber < attributesList
									.getLength(); attributeNumber++) {
							}
						}
					}
				}

				else {
					fetchChildElements.putAll(fetchValuesOfElement((Element) node,  claimID));
				}

			}

		}
		return fetchChildElements;
	}

	public Map<String, String> fetchChildElements(Element nNode, String tagname) {
		
		Map<String, String> fetchChildElements= new HashMap<String, String>();
		NodeList children = nNode.getChildNodes();// To get child nodes of node

		// loop through all child nodes
		for (int i = 0; i < children.getLength(); i++) {
			Node node = (Node) children.item(i);
			// Checking for the element node
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (hasChildElements((Element) node)) {// checking for child nodes
					fetchChildElements.putAll(fetchElementAtttributes((Element) node));
					fetchChildElements.putAll(fetchChildElements((Element) node, tagname));// getting child elements
					{
						if (node.hasAttributes()) // Checking for the node attributes

						{
							NamedNodeMap attributesList = node.getAttributes();
							for (int attributeNumber = 0; attributeNumber < attributesList
									.getLength(); attributeNumber++) {
							}
						}
					}
				}

				else {
					fetchChildElements.putAll(fetchValuesOfElement((Element) node));
				}

			}

		}
		return fetchChildElements;
	}

	/**
	 * To check the child nodes of the node
	 * 
	 * @param element : Node name
	 * @return: Boolean value True or False
	 */
	public boolean hasChildElements(Element element) {
		boolean flag = false;
		// To get child nodes for the given node
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			// looping through the nodes and checking for element node
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
				flag = true;
			}
		}
		return flag;
	}
}