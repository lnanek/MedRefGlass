package ru.phsystems.irisx.utils.betaface;

/**
 * IRIS-X Project
 * Author: Nikolay A. Viguro
 * WWW: smart.ph-systems.ru
 * E-Mail: nv@ph-systems.ru
 * Date: 09.10.12
 * Time: 12:24
 */

public class Face extends BetaFace {

    private String faceUID;
    private String gender = "none";

    public Face ()
    {

    }

    public void setUID (String uid)
    {
        faceUID = uid;
    }

    public String getUID ()
    {
        return faceUID;
    }

    public void setGender (String gender)
    {
        this.gender = gender;
    }

    public String getGender ()
    {
        return gender;
    }
}
