package eu.unicorn.helper.utils;


import eu.unicorn.cgmes.model.CgmesProfileType;
import eu.unicorn.cgmes.model.Model;
import eu.unicorn.cgmes.stream.CgmesWriter;
import eu.unicorn.cgmes.stream.CgmesWriterContext;
import eu.unicorn.cgmes.stream.CgmesWriterFactory;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static eu.unicorn.helper.utils.NameUtils.getNameWithoutSuffix;
import static eu.unicorn.helper.utils.NameUtils.getSuffixFromName;
import static java.time.LocalTime.now;

public class FileUtils {

    public static ParsingResult getXmlFilesFromInpput(List<File> files) {
        ParsingResult parsingResult = new ParsingResult();

        // First parse input
        parseInput(parsingResult, files);

        // Then parse all other zip files
        while (parsingResult.getZipFiles().size() > 0) {
            List<File> zipFiles = new ArrayList<>();
            zipFiles.addAll(parsingResult.getZipFiles());
            System.out.println(now() + " ParsingResult.getZipFiles().size() " + parsingResult.getZipFiles().size());
            parsingResult.getZipFiles().clear();
            parseInput(parsingResult, zipFiles);
        }

        return parsingResult;
    }

    private static void parseInput(ParsingResult ParsingResult, List<File> files) {
        for (File file : files) {
            if (getSuffixFromName(file.getName()).equalsIgnoreCase(ConfigurationSaveAs.XML.toString())) {
                ParsingResult.getXmlFiles().add(file);
            } else if (getSuffixFromName(file.getName()).equalsIgnoreCase(ConfigurationSaveAs.ZIP.toString())) {
                ParsingResult.getZipFiles().add(file);
            } else {
                ParsingResult.getUnrecognizedFiles().add(file);
            }
        }
    }

    /**
     * For saving zip files, which takes filename from xml file for zipping
     * @param path
     * @param file
     * @throws IOException
     */

    public static void saveZip(String path, File file) throws IOException {

            String name = getNameWithoutSuffix(file.getName());

            File zipFile = new File(path, name + "." + ConfigurationSaveAs.ZIP.toString().toLowerCase());
            FileOutputStream fos = null;

            FileInputStream fis = new FileInputStream(file);

            fos = new FileOutputStream(zipFile);
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipOut.putNextEntry(zipEntry);

            final byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            zipOut.close();
            fis.close();
            fos.close();
    }

    /**
     * For saving BDS, which doesn't take name from xml file
     * @param path
     * @param name
     * @param files
     * @throws IOException
     */

    public static void saveZip(String path, String name, File... files) throws IOException {
        File zipFile = new File(path, name + "." + ConfigurationSaveAs.ZIP.toString().toLowerCase());

        FileOutputStream fos = null;

        fos = new FileOutputStream(zipFile);
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        for (File file : files) {

            FileInputStream fis = new FileInputStream(file);

            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipOut.putNextEntry(zipEntry);

            final byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        fos.close();
    }

    public static File saveXML(CgmesProfileType profileType, String path, String filename, Model model) {

        final CgmesWriterContext writerCtx = CgmesWriterFactory.getContext(model);

        final CgmesWriter writer = CgmesWriterFactory.getWriter();

        OutputStream outputStream;
        File file;

        file = new File(path, filename);
        try {
            outputStream = new FileOutputStream(file);
            writer.write(outputStream, profileType, writerCtx);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
