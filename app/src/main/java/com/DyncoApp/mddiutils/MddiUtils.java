package com.DyncoApp.mddiutils;


import com.dynamicelement.sdk.android.misc.InstanceType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MddiUtils {

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Function - Assign CID and SNO //////////////////////////
    public static CidSnoResult assignCidSno(File file, File[] txt_Files, InstanceType instanceType) {
        CidSnoResult cidSnoResult = new CidSnoResult();
        String cid_Image = "";
        String sno_Image = "";
        ////////////////////////// Assigning CID and SNO to the '.jpg' image //////////////////////////
        //Check if there any '.txt' file in the respective folder
        if (txt_Files.length != 0) {
            //Loop through all the ".txt" files to see if there is a match in the file name of the current '.jpg' file
            for (File txt : txt_Files) {
                //Get the file name of '.jpg' file
                String jpgPath = file.getName();
                //Get the file name of '.txt' file
                String txtPath = txt.getName();
                //Cut out the last 4 characters of the file name(Cut out '.jpg')
                String jpgSubString = jpgPath.substring(0, jpgPath.length() - 4);
                //Cut out the last 4 characters of the file name(Cut out '.txt')
                String txtSubString = txtPath.substring(0, txtPath.length() - 4);
                //Check if both file names are equal
                if (jpgSubString.equals(txtSubString)) {
                    //Read out that particular '.txt' file
                    String QR_str = readFromFile(txt.getPath()).trim();
                    switch (instanceType) {
                        case DB_SNO:
                            //break;
                            //We have to check the first 13 characters(as our codes are printed in this format)
                            if (QR_str.length() != 27) {
                                sno_Image = "";
                                cid_Image = "";
                                break;
                            }

                            if (!QR_str.startsWith("http://d2.vc/")) {
                                sno_Image = "";
                                cid_Image = "";
                                break;
                            }

                            //Get the SNO from the QR code
                            sno_Image = QR_str.substring(17, 27);
                            //Get the CID from the QR code
                            cid_Image = QR_str.substring(13, 16);

                            break;
                        case IVF:
                        case IVF_SNO:
                            cid_Image = QR_str.substring(0, QR_str.indexOf(","));
                            sno_Image = QR_str.substring(QR_str.indexOf(",") + 1);
                            break;
                    }
                }
            }
        }
        cidSnoResult.cid = cid_Image;
        cidSnoResult.sno = sno_Image;
        return cidSnoResult;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Function - Read string from text file //////////////////////////
    protected static String readFromFile(String filename) {
        //Read text from file
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            //We will need to add proper error handling here
        }
        return text.toString();
    }

    /**
     * CID and SNO Result
     */
    public static class CidSnoResult {

        public String cid;
        public String sno;
    }

}
