package ru.phsystems.irisx.utils.betaface;

import org.jdom2.*;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
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

public class TestBetaFace {

    private static Logger log = Logger.getLogger(TestBetaFace.class.getName());

    public static void main(String[] args) throws IOException, JDOMException, InterruptedException {

        log.info("Start BetaFace API test!");

        // Upload images of a couple different people.
        Image img1 = new Image("1.jpg");
        Image img2 = new Image("2.jpg");
        Image img3 = new Image("lance1.jpg");
        Image img4 = new Image("lance2.jpg");
        Image img5 = new Image("lance3.jpg");

        // Get detected face for each.
        // Currently only support one face per picture.
        ArrayList<Face> faces1 = img1.getFaces();
        ArrayList<Face> faces2 = img2.getFaces();
        ArrayList<Face> faces3 = img3.getFaces();
        ArrayList<Face> faces4 = img4.getFaces();
        ArrayList<Face> faces5 = img5.getFaces();

        log.info("UID1: "+faces1.get(0).getUID()+
        		"\nUID2: "+faces2.get(0).getUID()+
        		"\nUID3: "+faces3.get(0).getUID()+
        		"\nUID4: "+faces4.get(0).getUID()+
        		"\nUID5: "+faces5.get(0).getUID()
        		);

        Person nik = new Person();
        nik.setName("NikolayViguro");
        nik.addUID(faces1.get(0).getUID());
        //nik.addUID(faces2.get(0).getUID());
        //nik.rememberPerson();

        Person lance = new Person();
        lance.setName("Lance");
        lance.addUID(faces3.get(0).getUID());
        //lance.addUID(faces4.get(0).getUID());
        //lance.rememberPerson();

        ArrayList<String> nikFaces = new ArrayList<String>();
        nikFaces.add(faces1.get(0).getUID());
        nikFaces.add(faces2.get(0).getUID());

        ArrayList<String> lanceFaces = new ArrayList<String>();
        //lanceFaces.add(faces3.get(0).getUID());
        lanceFaces.add(faces4.get(0).getUID());
        lanceFaces.add(faces5.get(0).getUID());
        lanceFaces.add(faces1.get(0).getUID());
        lanceFaces.add(faces2.get(0).getUID());
        
        // compare
        
        // Does return true.
        log.info("Is Nikolay Viguro present on nik images? " + nik.compareWithUIDsForConfidence(nikFaces));
        
        // Does return false.
        log.info("Is Nikolay Viguro present on lance images? " + nik.compareWithUIDsForConfidence(lanceFaces));
        
        // ~6x% confidence for correct first two, ~3x% for incorrect second two
        log.info("Is Lance present in lance and niko images? " + lance.compareWithUIDsForConfidence(lanceFaces));
        
        log.info("Is Lance present in niko images? " + lance.compareWithUIDsForConfidence(nikFaces));
    }

}

