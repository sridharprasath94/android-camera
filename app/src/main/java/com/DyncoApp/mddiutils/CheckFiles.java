package com.DyncoApp.mddiutils;

import com.mddi.exceptions.ClientException;
import com.mddi.exceptions.ExceptionType;
import com.mddi.misc.InstanceType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import lombok.Builder;
import lombok.NonNull;

/**
 * Task to check the file format.
 */
public class CheckFiles {
    protected ArrayList<String> cidList = new ArrayList<>();
    protected ArrayList<String> snoList = new ArrayList<>();

    @Builder
    public CheckFiles(@NonNull InstanceType instanceType,
                      @NonNull File currentFolder,
                      @NonNull CheckFilesCallback checkFilesCallback) throws ClientException {

        if (!currentFolder.exists()) {
            checkFilesCallback.onError(ExceptionType.CLIENT_EXCEPTION,
                    new ClientException("File Format Error : There is no such directory."));
            return;
        }
        //Get the list of '.jpg' files from the provided directory
        File[] jpgFiles = currentFolder.listFiles(file -> (file.getPath().endsWith(".jpg")));
        //Sort the files by name
        sortFiles(Objects.requireNonNull(jpgFiles));
        //Get the list of '.txt' files from the provided directory
        File[] txtFiles = currentFolder.listFiles(file -> (file.getPath().endsWith(".txt")));
        //Sort the files by name
        sortFiles(Objects.requireNonNull(txtFiles));

        if (jpgFiles.length == 0 && txtFiles.length == 0) {
            //Returns the error message if there are no '.jpg' images or '.txt' files
            checkFilesCallback.onError(ExceptionType.CLIENT_EXCEPTION,
                    new ClientException("File Format Error : The directory is empty."));
            return;
        }

        if (jpgFiles.length != txtFiles.length) {
            //Execute this block if there are no equal '.jpg' and '.txt' files
            checkFilesCallback.onError(ExceptionType.CLIENT_EXCEPTION,
                    new ClientException("File Format Error : There are no equal jpg and text files."));
            return;
        }
        //Initialize the boolean array for checking the text file name
        Boolean[] txtFileNameArray = new Boolean[jpgFiles.length];
        Arrays.fill(txtFileNameArray, true);
        //Initialize the boolean array for checking the text file content
        Boolean[] txtFileContentArray = new Boolean[jpgFiles.length];
        Arrays.fill(txtFileContentArray, true);

        for (int count = 0; count < jpgFiles.length; ++count) {
            File file = jpgFiles[count];
            //If the names are same, make the element of the boolean array as true
            txtFileNameArray[count] = compareName(file, txtFiles);
            //If the contents are in the correct format, make the element of the boolean array as true
            txtFileContentArray[count] = checkContent(file, txtFiles, instanceType);
        }

        //If the names of '.txt' and '.jpg' files are not same, return from here
        if (areAllTrue(txtFileNameArray)) {
            checkFilesCallback.onError(ExceptionType.CLIENT_EXCEPTION,
                    new ClientException("File Format Error : There are no matching '.txt' files for the '.jpg' files in this directory."));
            return;
        }
        //If the contents of '.txt' are not in correct format, return from here
        if (areAllTrue(txtFileContentArray)) {
            checkFilesCallback.onError(ExceptionType.CLIENT_EXCEPTION,
                    new ClientException("File Format Error : Text files are not in the corresponding format. Please check the files."));
            return;
        }
        //If there is no unique cid
        if (!sameCidForAllFiles(cidList)) {
            checkFilesCallback.onError(ExceptionType.CLIENT_EXCEPTION,
                    new ClientException("File Format Error : The files contains multiple CID's."));
            return;
        }
        //If there is valid sno
        if (!validSnoForAllFiles(snoList)) {
            checkFilesCallback.onError(ExceptionType.CLIENT_EXCEPTION,
                    new ClientException("File Format Error : Some of the files contain empty SNO."));
            return;
        }
        checkFilesCallback.onCorrectFormat(jpgFiles, txtFiles);
    }

    /**
     * To find whether the format of the text file is correct or not
     */
    private boolean checkContent(@NonNull File file,
                                 @NonNull File[] txtFiles,
                                 @NonNull InstanceType instanceType) throws ClientException {
        boolean result = false;
        int count = 0;
        int i = 0;
        ////////////////////////// Assigning CID and SNO to the '.jpg' image //////////////////////////
        //Check if there any '.txt' file in the respective folder
        if (txtFiles.length == 0) {
            return false;
        }
        //Loop through all the ".txt" files to see if there is a match in the file name of the current '.jpg' file
        for (File txt : txtFiles) {
            //Check whether the name of the '.jpg' and '.txt' files are similar(excluding the last 4 characters)
            if (file.getName().substring(0, file.getName().length() - 4).equals
                    (txt.getName().substring(0, txt.getName().length() - 4))) {
                //Read out that particular '.txt' file
                String fileTxt = readFromFile(txt.getPath()).trim();
                if (instanceType == InstanceType.DB_SNO) {
                    if (fileTxt.length() != 27) {
                        return false;
                    }

                    if (!fileTxt.substring(0, 13).matches("http://d2.vc/")) {
                        return false;
                    }
                    cidList.add(fileTxt.substring(13, 16));
                    snoList.add(fileTxt.substring(17, 27));
                } else {
                    while ((i = fileTxt.indexOf(",", i)) != -1) {
                        i += ",".length();
                        count++;
                    }
                    if (count != 1) {
                        return false;
                    }
                    cidList.add(fileTxt.substring(0, fileTxt.indexOf(",")));
                    snoList.add(fileTxt.substring(fileTxt.indexOf(",") + 1));
                }
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Check if CID occurs multiple times
     */
    private static boolean sameCidForAllFiles(@NonNull ArrayList<String> list) {
        return list.isEmpty() || Collections.frequency(list, list.get(0)) == list.size();
    }

    /**
     * Check if any SNO is empty
     */
    private static boolean validSnoForAllFiles(@NonNull ArrayList<String> list) {
        return !list.contains("");
    }

    /**
     * TO read string from text file
     */
    private static String readFromFile(@NonNull String filename) throws ClientException {
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
            throw new ClientException(e.getMessage());
        }

        return text.toString();
    }

    /**
     * Check if all the boolean values of the array are true or false
     */
    private static boolean areAllTrue(@NonNull Boolean[] array) {
        for (boolean b : array) if (!b) return true;
        return false;
    }

    /**
     * Function for sorting the files in a folder
     */
    private static void sortFiles(@NonNull File[] files) {
        assert files != null;
        if (files.length > 1) {

            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    int n1 = extractNumber(o1.getName());
                    int n2 = extractNumber(o2.getName());
                    return n1 - n2;
                }

                private int extractNumber(String name) {
                    int i;
                    try {
                        int s = name.indexOf('_') + 1;
                        int e = name.lastIndexOf('.');
                        String number = name.substring(s, e);
                        i = Integer.parseInt(number);
                    } catch (Exception e) {
                        i = 0; // if filename does not match the format
                        // then default to 0
                    }
                    return i;
                }
            });
        }
    }

    /**
     * Function - Compare JPG file and text file names
     */
    private static boolean compareName(@NonNull File file, @NonNull File[] txtFiles) {
        boolean result = false;

        //Check if there any '.txt' file in the respective folder
        if (txtFiles.length == 0) {
            return false;
        }
        //Loop through all the ".txt" files to see if there is a match in the file name of the current '.jpg' file
        for (File txt : txtFiles) {

            //Cut out the last 4 characters of the file name(Cut out '.jpg')
            String jpgSubString = file.getName().substring(0, file.getName().length() - 4);
            //Cut out the last 4 characters of the file name(Cut out '.txt')
            String txtSubString = txt.getName().substring(0, txt.getName().length() - 4);

            //Check if both file names are equal
            if (jpgSubString.equals(txtSubString)) {
                result = true;
                break;
            }
        }

        return result;
    }
}
