package ru.phsystems.irisx.utils.betaface;

import org.apache.commons.codec.binary.Base64;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
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

public class Image extends BetaFace{

    //private BufferedImage imageBuffered;
    private byte[] imageBytes;
    private static Logger log = Logger.getLogger(Image.class.getName());

    
    public Image(String file) throws IOException {

        if(debug)
            log.info("Build image from file");

        /*
        byte[] temp = readFile(file);
        imageBuffered = checkSize(temp);
        this.imageBytes = convertBuffImageToByteArray(imageBuffered);
        */
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(new FileInputStream(file), baos, 1024);
        this.imageBytes = baos.toByteArray();
    }    
    
    public Image(InputStream is) throws IOException {

        if(debug)
            log.info("Build image from InputStream");

        /*
        byte[] temp = readFile(file);
        imageBuffered = checkSize(temp);
        this.imageBytes = convertBuffImageToByteArray(imageBuffered);
        */
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(is, baos, 1024);
        this.imageBytes = baos.toByteArray();
    }


    /*
    public Image(byte[] file) throws IOException {

        if(debug)
            log.info("Build image from byte array");

         byte[] temp = file;
         imageBuffered = checkSize(temp);
         this.imageBytes = convertBuffImageToByteArray(imageBuffered);
    }
    */
    
    public static void copy(InputStream input,
    	      OutputStream output,
    	      int bufferSize)
    	      throws IOException {
    	    byte[] buf = new byte[bufferSize];
    	    int bytesRead = input.read(buf);
    	    while (bytesRead != -1) {
    	      output.write(buf, 0, bytesRead);
    	      bytesRead = input.read(buf);
    	    }
    	    output.flush();
    	  }

    /*
    public Image(URL url) throws IOException {

        if(debug)
            log.info("Build image from HTTP url");

        BufferedImage img = ImageIO.read(url);
        byte[] temp = convertBuffImageToByteArray(img);

        imageBuffered = checkSize(temp);
        this.imageBytes = convertBuffImageToByteArray(imageBuffered);
    }
    */

    // Resize image
    /////////////////////////////////////////////////
    /*
    public BufferedImage resizeImage(BufferedImage buffImage, int height, int width)
    {
        if(debug)
        log.info("Resizing image to "+height+"x"+width);

        int type = buffImage.getType() == 0? buffImage.TYPE_INT_ARGB : buffImage.getType();

        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(buffImage, 0, 0, height, width, null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        return resizedImage;
    }
*/
    
    // Read image from file
    /////////////////////////////////////////////////

    private byte[] readFile(String filename) throws IOException {

        if(debug)
        log.info("Read file "+filename);

        File file = new File(filename);
        InputStream is = new FileInputStream(file);

        long length = file.length();
        if (length > Integer.MAX_VALUE) {
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();

        return bytes;
    }
/*
    private byte[] convertBuffImageToByteArray(BufferedImage img) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        return baos.toByteArray();
    }
*/
    //  Check image size
    /////////////////////////////////////////////////
/*
    private BufferedImage checkSize(byte[] image) throws IOException
    {
        InputStream in = new ByteArrayInputStream( image );
        BufferedImage buffImage = ImageIO.read(in);

        int origWidth = buffImage.getWidth();
        int origHeight = buffImage.getHeight();

        if(origHeight > MAXHEIGHT || origWidth > MAXWIDTH)
        {
            buffImage = resizeImage(buffImage, MAXHEIGHT, MAXWIDTH);
        }

        return buffImage;
    }
*/
    // Send image and get faces UID
    /////////////////////////////////////////////////

    public ArrayList<Face> getFaces () throws IOException, JDOMException, InterruptedException {

        if(debug)
        log.info("Retriving faces from image");

        Element rootElement = new Element("ImageRequestBinary");

        //Namespace ns1 = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
        //Namespace ns2 = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        //rootElement.addNamespaceDeclaration(ns1);
        //rootElement.addNamespaceDeclaration(ns2);

        rootElement.addContent(new Element("api_key").addContent(apiKey));
        rootElement.addContent(new Element("api_secret").addContent(apiSecret));
        rootElement.addContent(new Element("detection_flags").addContent("0"));
        rootElement.addContent(new Element("imagefile_data").addContent(encode64(imageBytes)));
        rootElement.addContent(new Element("original_filename").addContent("temp.jpg"));

        String content = process("/UploadNewImage_File", new Document(rootElement));

        SAXBuilder builder = new SAXBuilder();
        Reader in = new StringReader(content);

        Document resp = builder.build(in);
        Element root = resp.getRootElement();
        String uid = root.getChild("img_uid").getText();

        if(debug)
        log.info("Image UID = "+uid);

        ///////////////////////////////////////////

        ArrayList<Face> faceList = new ArrayList<Face>();

        while(true)
        {
            faceList = getUid(uid);
            if(faceList.size() != 0)
            {
                return faceList;
            }
            else
            {
                if(debug)
                log.info("Waiting for response");
                Thread.sleep(1000);
            }

        }
    }

    private ArrayList<Face> getUid(String uid) throws IOException, JDOMException {

            ArrayList<Face> faces = new ArrayList<Face>();

            Element rootElement = new Element("ImageInfoRequestUid");

            //Namespace ns1 = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
            //Namespace ns2 = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

            //rootElement.addNamespaceDeclaration(ns1);
            //rootElement.addNamespaceDeclaration(ns2);

            rootElement.addContent(new Element("api_key").addContent(apiKey));
            rootElement.addContent(new Element("api_secret").addContent(apiSecret));
            rootElement.addContent(new Element("img_uid").addContent(uid));

            String contents = process("/GetImageInfo", new Document(rootElement), false);
            
            log.info("Contents: " + contents);
            
            contents = contents.trim().replaceFirst("^([\\W]+)<","<");
            SAXBuilder builder = new SAXBuilder();
            Reader in = new StringReader(contents);

            Document resp = builder.build(in);
            Element root = resp.getRootElement();
            String uid2 = root.getChild("int_response").getText();

            if(Integer.valueOf(uid2) == 1)
            {
                return faces;
            }

        List<Element> facesRoot = root.getChild("faces").getChildren("FaceInfo");

        for(int i = 0; i < facesRoot.size(); i++)
        {
            Element faceInfo = facesRoot.get(i);
            Face face = new Face();

            face.setUID(faceInfo.getChild("uid").getText());
            face.setGender(faceInfo.getChild("gender").getText());
            //TODO add more face params from xml

            faces.add(face);
            if(debug)
            log.info("Process face "+i+", UID = "+faceInfo.getChild("uid").getText());
        }

        return faces;
    }

    //  Encode byte array to Base64
    /////////////////////////////////////////////////

    public String encode64(byte[] image)
    {
        byte[] encoded = Base64.encodeBase64(image);
        return new String(encoded);
    }

    //  Decode Base64 String to byte array
    /////////////////////////////////////////////////

    public byte[] decode64(String image)
    {
        return new Base64().decode(image);
    }

	public byte[] getImageBytes() {
		return imageBytes;
	}
    
    


}
