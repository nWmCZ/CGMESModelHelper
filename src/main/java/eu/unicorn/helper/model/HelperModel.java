package eu.unicorn.helper.model;

import eu.unicorn.cgmes.model.CgmesProfileDesc;
import eu.unicorn.cgmes.model.CgmesProfileType;
import eu.unicorn.cgmes.model.Model;
import eu.unicorn.cgmes.stream.*;
import eu.unicorn.helper.utils.ConfigurationSaveAs;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HelperModel {

    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private Model model;

    public Model parseInput(List<File> files) {
        final CgmesReaderContext ctx = CgmesReaderFactory.getContext();
        final CgmesReader reader = CgmesReaderFactory.getReader();
        long start, end;
        for (File file: files) {

            if (file != null) {
                InputStream is = null;
                start = System.currentTimeMillis();
                try {
                    is = new FileInputStream(file);
                    reader.read(is, ctx);
                } catch (Exception e) {
                    // OPTIONAL : FileInputStream throws FileNotFoundException, parser throws more
                    e.printStackTrace();
                }
                end = System.currentTimeMillis();
                long parsingTime = end - start;
            }
        }
        this.model = ctx.getModel();
        return ctx.getModel();
    }

    public void saveToDestination(ConfigurationSaveAs configurationSaveAs, Map<ConfigurationSaveAs, String> path, CgmesProfileType... profileTypes) {


        for (CgmesProfileType profileType: profileTypes) {
            switch (configurationSaveAs) {
                case XML:
                    saveXML(profileType, path.get(ConfigurationSaveAs.XML), getFilenameXML(profileType) + "." + ConfigurationSaveAs.XML.toString().toLowerCase());
                    break;
                case ZIP:
                    saveZip(profileType, path.get(ConfigurationSaveAs.ZIP), getFilenameZIP(profileType) + "." +  ConfigurationSaveAs.ZIP.toString().toLowerCase());
                    break;
                case BOTH:
                    saveXML(profileType, path.get(ConfigurationSaveAs.XML), getFilenameXML(profileType) + "." +  ConfigurationSaveAs.XML.toString().toLowerCase());
                    saveZip(profileType, path.get(ConfigurationSaveAs.ZIP), getFilenameZIP(profileType) + "." +  ConfigurationSaveAs.ZIP.toString().toLowerCase());
            }
        }
    }

    private String getFilenameZIP(CgmesProfileType profileType) {
        return "undefined";
    }

    // 20140601T0000Z_ENTSO-E_EQ_BD_001.xml and 20140601T0000Z_ENTSO-E_TP_BD_001.xml
    // 20140601T1030Z_01_ELIA_SSH_001.xml TP SV
    // 20140601T1030Z_ELIA_EQ_001.xml
    private String getFilenameXML(CgmesProfileType cgmesProfileType) {
        Optional<CgmesProfileDesc> profileDesc = getCgmesProfileDesc(cgmesProfileType);
        if (profileDesc.isPresent()) {
            Date date = profileDesc.get().getScenarioTime();
            LocalDateTime localDatetime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            int year   = localDatetime.getYear();
            int month  = localDatetime.getMonthValue();
            int day    = localDatetime.getDayOfMonth();
            int hour   = localDatetime.getHour();
            int minute = localDatetime.getMinute();

            String version = profileDesc.get().getVersion();

            // TODO resolve tso name
            String tso = "ELIA";
            // TODO resolve resolution
            String resolution = "01";
            switch (cgmesProfileType) {
                case EQUIPMENT:
                    return String.valueOf(year) + fillToTwo(String.valueOf(month)) + fillToTwo(String.valueOf(day)) + "T" + String.valueOf(hour) + String.valueOf(minute) + "Z_" + tso + "_EQ_" + fillToThree(version);
                case TOPOLOGY:
                    return String.valueOf(year) + fillToTwo(String.valueOf(month)) + fillToTwo(String.valueOf(day)) + "T" + String.valueOf(hour) + String.valueOf(minute) + "Z_" + resolution + "_"+ tso + "_TP_" + fillToThree(version);
                case STATE_VARIABLES:
                    return String.valueOf(year) + fillToTwo(String.valueOf(month)) + fillToTwo(String.valueOf(day)) + "T" + String.valueOf(hour) + String.valueOf(minute) + "Z_" + resolution + "_"+ tso + "_SV_" + fillToThree(version);
                case STEADY_STATE_HYPOTHESIS:
                    return String.valueOf(year) + fillToTwo(String.valueOf(month)) + fillToTwo(String.valueOf(day)) + "T" + String.valueOf(hour) + String.valueOf(minute) + "Z_" + resolution + "_"+ tso + "_EQ_" + fillToThree(version);
                case EQUIPMENT_BOUNDARY:
                    return String.valueOf(year) + fillToTwo(String.valueOf(month)) + fillToTwo(String.valueOf(day)) + "T" + String.valueOf(hour) + String.valueOf(minute) + "Z_ENTSOE-E_EQ_BD_" + fillToThree(version);
                case TOPOLOGY_BOUNDARY:
                    return String.valueOf(year) + fillToTwo(String.valueOf(month)) + fillToTwo(String.valueOf(day)) + "T" + String.valueOf(hour) + String.valueOf(minute) + "Z_ENTSOE-E_TP_BD_" + fillToThree(version);
                    default: return "undefined";
            }
        } else {
            return "undefined";
        }
    }

    private String fillToThree(String string) {
        return ("000" + string).substring(string.length());
    }

    private String fillToTwo(String string) {
        return ("00" + string).substring(string.length());
    }

    public void updateProfile(CgmesProfileType profileType, Map<ProfileField, String> changedValues) {

        Optional<CgmesProfileDesc> cgmesProfileType = this.model.getCgmesProfileDescs().values().stream().filter(findProfileType -> findProfileType.getCgmesProfileType().equals(profileType)).findAny();

        if (cgmesProfileType.isPresent()) {
            cgmesProfileType.get().setRdfId(changedValues.get(ProfileField.RDF_ABOUT));
            DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH);
            try {
                cgmesProfileType.get().setCreated(dateFormat.parse(changedValues.get(ProfileField.CREATED)));
                cgmesProfileType.get().setScenarioTime(dateFormat.parse(changedValues.get(ProfileField.SCENARIO_TIME)));
            } catch (ParseException e) {
                // TODO error handling
                e.printStackTrace();
            }
            String version = changedValues.get(ProfileField.VERSION);
            // TODO fix to 3 positions
            cgmesProfileType.get().setVersion(("000" + version).substring(version.length()));
            cgmesProfileType.get().setModelingAuthoritySet(changedValues.get(ProfileField.AUTORITY_SET));
        }
    }

    private void saveXML(CgmesProfileType profileType, String path, String filename) {

            final CgmesWriterContext writerCtx = CgmesWriterFactory.getContext(this.model);

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
    }

    private void saveZip(CgmesProfileType profileType, String path, String filename) {
        //TODO save zip
    }

    public Optional<CgmesProfileDesc> getCgmesProfileDesc(CgmesProfileType cgmesProfileType) {
        return this.model.getCgmesProfileDescs().values().stream().filter(cgmesProfileDesc -> cgmesProfileDesc.getCgmesProfileType().equals(cgmesProfileType)).findAny();
    }
}
