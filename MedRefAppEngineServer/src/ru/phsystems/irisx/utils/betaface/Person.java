package ru.phsystems.irisx.utils.betaface;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * IRIS-X Project
 * Author: Nikolay A. Viguro
 * WWW: smart.ph-systems.ru
 * E-Mail: nv@ph-systems.ru
 * Date: 09.10.12
 * Time: 12:24
 */

public class Person extends BetaFace {

    private String personName;
    private ArrayList<String> uidList = new ArrayList<String>();
    private static Logger log = Logger.getLogger(Person.class.getName());

    public Person ()
    {

    }

    public void setName (String name)
    {
        this.personName = name;
    }

    public String getName ()
    {
        return personName;
    }

    public void addUID (String uid)
    {
        uidList.add(uid);
    }

    public boolean deleteUID (String uid)
    {
        int index = uidList.indexOf(uid);

        try
        {
            uidList.remove(index);
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    public ArrayList<String> getUIDs ()
    {
        return uidList;
    }

    // Send person to BetaFace
    /////////////////////////////////////////////////

    public boolean rememberPerson () throws IOException, JDOMException {

        Element rootElement = new Element("FacesSetPersonRequest");
        //rootElement.setAttribute("namespace_id", "1");

        //Namespace ns1 = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
        //Namespace ns2 = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        //rootElement.addNamespaceDeclaration(ns1);
        //rootElement.addNamespaceDeclaration(ns2);

        Element faceUids = new Element("face_uids");

        for(int i=0; i < uidList.size(); i++) {
            String uid = uidList.get(i);

            Namespace ns = Namespace.getNamespace("http://schemas.microsoft.com/2003/10/Serialization/Arrays");
            Element guid = new Element("guid", ns).addContent(uid);
            faceUids.addContent(guid);
        }

        rootElement.addContent(new Element("api_key").addContent(apiKey));
        rootElement.addContent(new Element("api_secret").addContent(apiSecret));
        rootElement.addContent(faceUids);
        rootElement.addContent(new Element("person_id").addContent(personName));
        //rootElement.addContent(new Element("namespace_id").addContent("name.nanek.medref"));

        String content = process("/Faces_SetPerson", new Document(rootElement));
        if ( null == content ) {
        	return false;
        }
        
        SAXBuilder builder = new SAXBuilder();
        Reader in = new StringReader(content);

        Document resp = builder.build(in);
        Element root = resp.getRootElement();
        String status = root.getChild("int_response").getText();

        if(Integer.valueOf(status) != 1)
        {
            return true;
        }
        return false;
    }

    // Recognize person
    /////////////////////////////////////////////////

    private String getRecognizeUid (ArrayList<String> recognizeUIDs) throws IOException, JDOMException {

        Element rootElement = new Element("FacesRecognizeRequest");
        Element faceUids = new Element("faces_uids");

        for(int i=0; i < uidList.size(); i++) {
            String uid = uidList.get(i);

            Namespace ns = Namespace.getNamespace("http://schemas.microsoft.com/2003/10/Serialization/Arrays");
            Element guid = new Element("guid", ns).addContent(uid);
            faceUids.addContent(guid);
        }

        rootElement.addContent(new Element("api_key").addContent(apiKey));
        rootElement.addContent(new Element("api_secret").addContent(apiSecret));
        rootElement.addContent(faceUids);
        rootElement.addContent(new Element("group_results").addContent("false"));

        Element targets = new Element("targets");
        
        for(int i=0; i < recognizeUIDs.size(); i++) {
            String uid = recognizeUIDs.get(i);

            Namespace nsTargets = Namespace.getNamespace("http://schemas.microsoft.com/2003/10/Serialization/Arrays");
            Element string = new Element("string", nsTargets).addContent(uid);
            targets.addContent(string);

        }
      
        rootElement.addContent(targets);

        String content = process("/Faces_Recognize", new Document(rootElement));

        SAXBuilder builder = new SAXBuilder();
        Reader in = new StringReader(content);

        Document resp = builder.build(in);
        Element root = resp.getRootElement();
        String uidRecog = root.getChild("recognize_uid").getText();

        return uidRecog;
    }

    // Wait for recognize
    /////////////////////////////////////////////////

    public float compareWithUIDsForConfidence(ArrayList<String> compareUIDs) throws IOException, JDOMException, InterruptedException {

    	if ( null == compareUIDs || compareUIDs.isEmpty() ) {
    		return 0;
    	}
    	
        String recUID = getRecognizeUid(compareUIDs);

        while(true)
        {
            Element rootElement = new Element("RecognizeResultRequest");

            rootElement.addContent(new Element("api_key").addContent(apiKey));
            rootElement.addContent(new Element("api_secret").addContent(apiSecret));
            rootElement.addContent(new Element("recognize_uid").addContent(recUID));

            String content = process("/GetRecognizeResult", new Document(rootElement));

            SAXBuilder builder = new SAXBuilder();
            Reader in = new StringReader(content);

            Document resp = builder.build(in);
            Element root = resp.getRootElement();
            String uids = root.getChild("int_response").getText();

            if(Integer.valueOf(uids) != 1)
            {
                List<Element> facesRoot = root.getChild("faces_matches").getChildren("FaceRecognizeInfo");

                float highestConfidence = 0;
                for(int i = 0; i < facesRoot.size(); i++)
                {
                    Element faceInfo = facesRoot.get(i).getChild("matches").getChild("PersonMatchInfo");

                    if(debug) log.info("Process face " + i);
                    
                    String confidenceText = faceInfo.getChild("confidence").getText();
                    log.info("confidence: " + confidenceText);
                    float confidenceValue = Float.parseFloat(confidenceText);
                    if ( confidenceValue > highestConfidence) {
                    	highestConfidence = confidenceValue;
                    }

                    if(Boolean.valueOf(faceInfo.getChild("is_match").getText()) == true)
                    {
                        return 1f;
                    }
                }

                return highestConfidence;
            }
            else
            {
                if(debug)
                log.info("Waiting for response...");
                Thread.sleep(1000);
            }
        }
    }
    
    public boolean compareWithUIDs (ArrayList<String> compareUIDs) throws IOException, JDOMException, InterruptedException {

        String recUID = getRecognizeUid(compareUIDs);

        while(true)
        {
            Element rootElement = new Element("RecognizeResultRequest");

            rootElement.addContent(new Element("api_key").addContent(apiKey));
            rootElement.addContent(new Element("api_secret").addContent(apiSecret));
            rootElement.addContent(new Element("recognize_uid").addContent(recUID));

            String content = process("/GetRecognizeResult", new Document(rootElement));

            SAXBuilder builder = new SAXBuilder();
            Reader in = new StringReader(content);

            Document resp = builder.build(in);
            Element root = resp.getRootElement();
            String uids = root.getChild("int_response").getText();

            if(Integer.valueOf(uids) != 1)
            {
                List<Element> facesRoot = root.getChild("faces_matches").getChildren("FaceRecognizeInfo");

                for(int i = 0; i < facesRoot.size(); i++)
                {
                    Element faceInfo = facesRoot.get(i).getChild("matches").getChild("PersonMatchInfo");

                    if(debug)
                        log.info("Process face " + i);
                    
                    log.info("confidence: " + faceInfo.getChild("is_match").getText());

                    if(Boolean.valueOf(faceInfo.getChild("is_match").getText()) == true)
                    {
                        return true;
                    }
                }

                return false;
            }
            else
            {
                if(debug)
                log.info("Waiting for response...");
                Thread.sleep(1000);
            }
        }
    }


}
