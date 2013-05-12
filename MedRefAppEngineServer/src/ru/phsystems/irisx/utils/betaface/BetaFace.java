package ru.phsystems.irisx.utils.betaface;

import org.jdom2.*;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * IRIS-X Project
 * Author: Nikolay A. Viguro
 * WWW: smart.ph-systems.ru
 * E-Mail: nv@ph-systems.ru
 * Date: 28.09.12
 * Time: 14:03
 *
 * BetaFace.com API implements.
 */

public class BetaFace {

    public static final int MAXWIDTH = 480;
    public static final int MAXHEIGHT = 640;
    public static boolean debug = true;

    protected final String apiKey = "d45fd466-51e2-4701-8da8-04351c872236";
    protected final String apiSecret = "171e8465-f548-401d-b63b-caf0dc28df5f";
    protected final String serviceURL = "http://www.betafaceapi.com/service.svc";

    private static Logger log = Logger.getLogger(BetaFace.class.getName());

    // Constructor
    /////////////////////////////////////////////////

    public BetaFace() {}
    // Common method for send info
   public String process(String urlString, Document doc) throws IOException {
   return process(urlString, doc, false);
   }

     // Common method for send info
    public String process(String urlString, Document doc, boolean logRequest) throws IOException {

        HttpURLConnection connection = null;

        if(debug) log.info("Send request - "+urlString);

        URL url = new URL(serviceURL+urlString);
        URLConnection uc = url.openConnection();
        connection = (HttpURLConnection) uc;
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-type", "application/xml");
  
        
        if ( logRequest ) {
        	log.info("***Sending:");
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        log.info(outputter.outputString(doc));
        }
        
        PrintWriter out = new PrintWriter(connection.getOutputStream());
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        out.println(outputter.outputString(doc));
        out.flush();
        out.close();

        BufferedInputStream input = null;
        try {
        	log.info("response message: " + connection.getResponseMessage());
        	log.info("response code: " + connection.getResponseCode());
        	
        	if ( 200 != connection.getResponseCode() ) {

        		log.info("***failed:");
                return null;
        	}

            input = new BufferedInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            if(debug)
            	log.log(Level.SEVERE, "Error: " + connection.getResponseMessage(), e);
        }

        return convertStreamToString(input);

    }

    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        log.info("Result: " + result);
        return result;
    }

}

