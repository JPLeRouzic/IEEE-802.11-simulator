/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.conf_screen;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Jean-Pierre Le Rouzic
 */
public class ParseFor {

    public static boolean parseForResume(Document aDocument) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "//JE802Control/@resume";
        try {
            Node resumeNode = (Node) xpath.evaluate(expression, aDocument, XPathConstants.NODE);
            if (resumeNode != null) 
                {
                return Boolean.valueOf(resumeNode.getNodeValue());
                }
            return false;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static long parseForRandomSeed(Document configuration) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "//JE802StatEval/@seed";
        try {
            Node seedNode = (Node) xpath.evaluate(expression, configuration, XPathConstants.NODE);
            return new Long(seedNode.getNodeValue());
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double parseForDuration(Document aDocument) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "//JE802Control/@EmulationDuration_ms";
        try {
            Node emulationDurationNode = (Node) xpath.evaluate(expression, aDocument, XPathConstants.NODE);
            return new Double(emulationDurationNode.getNodeValue());
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static String parseForHibernationFile(Document aDocument) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "//JE802Control/@resumeFile";
        try {
            Node fileNameNode = (Node) xpath.evaluate(expression, aDocument, XPathConstants.NODE);
            if (fileNameNode != null) {
                return fileNameNode.getNodeValue();
            }
            return null;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean parseForShowGui(Document configuration) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "//JE802Control/@showGui";
        try {
            Node guiNode = (Node) xpath.evaluate(expression, configuration, XPathConstants.NODE);
            return Boolean.valueOf(guiNode.getNodeValue());
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Document parseDocument(String aScenarioFilename) {
        Document anXMLdoc = null;
        File theScenarioFile = new File(aScenarioFilename);
        if (!theScenarioFile.exists()) {
            System.err.println("This is Jemula802. Error: could not open the XML scenario description file " + theScenarioFile);
            System.exit(0);
        } else {
            System.out.println("This is Jemula802. XML scenario file: " + theScenarioFile.getName());
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser;
            try {
                parser = factory.newDocumentBuilder();
                anXMLdoc = parser.parse(theScenarioFile);
            } catch (ParserConfigurationException e1) {
                e1.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return anXMLdoc;
    }
    
}
