package eu.unicorn.helper.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParsingResult {
    List<File> xmlFiles = new ArrayList<>();
    List<File> zipFiles = new ArrayList<>();
    List<File> unrecognizedFiles = new ArrayList<>();

    public List<File> getXmlFiles() {
        return xmlFiles;
    }

    public List<File> getZipFiles() {
        return zipFiles;
    }

    public List<File> getUnrecognizedFiles() {
        return unrecognizedFiles;
    }
}
