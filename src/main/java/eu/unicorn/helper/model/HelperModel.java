package eu.unicorn.helper.model;

import eu.unicorn.cgmes.model.CgmesProfileDesc;
import eu.unicorn.cgmes.model.CgmesProfileType;
import eu.unicorn.cgmes.model.Model;
import eu.unicorn.cgmes.stream.*;
import eu.unicorn.cgmes.stream.model.CgmesAssociationConnector;
import eu.unicorn.cgmes.stream.model.CgmesProfileDependencyConnector;
import eu.unicorn.helper.utils.ConfigurationParameter;
import eu.unicorn.helper.utils.ConfigurationSaveAs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.lang.String.format;

public class HelperModel {

    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private Model model;

    Logger logger = LoggerFactory.getLogger(HelperModel.class);

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

        logger.info("Resolving profile dependencies...");
        start = System.currentTimeMillis();
        final Set<CgmesProfileDependencyConnector> unresolvedDependencies = ctx.getProfileDependencyRegistry().resolveDependencies(ctx.getModel());
        end = System.currentTimeMillis();
        long dependencies = end - start;
        logger.info(format("Profile dependencies resolved in %s ms", dependencies));
        logger.info(format("Unresolved profile dependencies count: %s", unresolvedDependencies.size()));

        logger.trace("Connecting associated classes...");
        start = System.currentTimeMillis();
        final Set<CgmesAssociationConnector> unsatisfiedConnections = ctx.getAssociationRegistry().connectEntities(ctx.getModel());
        end = System.currentTimeMillis();
        long associations = end - start;
        logger.trace(format("Profile associations resolved in %s ms", associations));
        logger.info(format("Unsatisfied associations count: %s", unsatisfiedConnections.size()));

        this.model = ctx.getModel();
        return ctx.getModel();
    }

    public void saveToDestination(Map<ConfigurationParameter, Object> configurationParameter, Map<ConfigurationSaveAs, String> path, CgmesProfileType... profileTypes) throws IOException {

        processConfigurationParameters(configurationParameter);

        Map<CgmesProfileType, File> profiles = new HashMap<>();

        for (CgmesProfileType profileType: profileTypes) {
            profiles.put(profileType, saveXML(profileType, path.get(ConfigurationSaveAs.XML), getFilenameXML(profileType) + "." + ConfigurationSaveAs.XML.toString().toLowerCase()));
        }

        if (path.size() == 2) {
            // All non Boundary profiles
            for (Map.Entry entry: profiles.entrySet()) {
                CgmesProfileType cgmesProfileType = (CgmesProfileType) entry.getKey();
                if (!CgmesProfileType.EQUIPMENT_BOUNDARY.equals(cgmesProfileType) && !CgmesProfileType.TOPOLOGY_BOUNDARY.equals(cgmesProfileType)) {
                    saveZip(path.get(ConfigurationSaveAs.ZIP), (File) entry.getValue());
                }
            }
            // Boundary profiles in one zip
            saveZip(path.get(ConfigurationSaveAs.ZIP), profiles.get(CgmesProfileType.TOPOLOGY_BOUNDARY), profiles.get(CgmesProfileType.EQUIPMENT_BOUNDARY));
        }
    }

    private void processConfigurationParameters(Map<ConfigurationParameter, Object> configurationParameter) {
        if (configurationParameter.keySet().contains(ConfigurationParameter.INCREASE_VERSIONS)) {
            this.model.getCgmesProfileDescs().values().stream().forEach(cgmesProfileDesc -> {
                int version = Integer.parseInt(cgmesProfileDesc.getVersion());
                cgmesProfileDesc.setVersion(fillToThree(String.valueOf(++version)));
            });
        }

        if (configurationParameter.keySet().contains(ConfigurationParameter.GENERATE_NEW_IDS)) {

            Object prefixObject = configurationParameter.get(ConfigurationParameter.PREFIX);
            Object postfixObject = configurationParameter.get(ConfigurationParameter.POSTFIX);

            String prefix = (prefixObject != null) ? configurationParameter.get(ConfigurationParameter.PREFIX).toString() : "";
            String postfix = (postfixObject != null) ? configurationParameter.get(ConfigurationParameter.POSTFIX).toString() : "";

            this.model.getCgmesProfileDescs().values().stream().forEach(cgmesProfileDesc -> {

                String newId = prefix + UUID.randomUUID() + postfix;
                cgmesProfileDesc.setRdfId(newId);
            });
        }
    }

    // 20140601T0000Z_ENTSO-E_BD_001.zip
    private String getFilenameZIP(CgmesProfileType cgmesProfileType) {
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

            switch (cgmesProfileType) {
                case EQUIPMENT_BOUNDARY:
                    return String.valueOf(year) + fillToTwo(String.valueOf(month)) + fillToTwo(String.valueOf(day)) + "T" + String.valueOf(hour) + String.valueOf(minute) + "Z_ENTSOE-E_BD_" + fillToThree(version);
                case TOPOLOGY_BOUNDARY:
                    return String.valueOf(year) + fillToTwo(String.valueOf(month)) + fillToTwo(String.valueOf(day)) + "T" + String.valueOf(hour) + String.valueOf(minute) + "Z_ENTSOE-E_BD_" + fillToThree(version);
                default: return "undefined";
            }
        } else {
            return "undefined";
        }
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

    private File saveXML(CgmesProfileType profileType, String path, String filename) {

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
            return file;
    }

    private void saveZip(String path, File... files) throws IOException {

        if (files.length == 1) {
            String name = files[0].getName();
            String[] nameWithoutExtension = name.split("\\.");
            File zipFile = new File(path, nameWithoutExtension[0] + "." + ConfigurationSaveAs.ZIP.toString().toLowerCase());
            FileOutputStream fos = null;

            FileInputStream fis = new FileInputStream(files[0]);

            fos = new FileOutputStream(zipFile);
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            ZipEntry zipEntry = new ZipEntry(files[0].getName());
            zipOut.putNextEntry(zipEntry);

            final byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            zipOut.close();
            fis.close();
            fos.close();

        } else {

            String name = getFilenameZIP(CgmesProfileType.TOPOLOGY_BOUNDARY);
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
    }

    public Optional<CgmesProfileDesc> getCgmesProfileDesc(CgmesProfileType cgmesProfileType) {
        return this.model.getCgmesProfileDescs().values().stream().filter(cgmesProfileDesc -> cgmesProfileDesc.getCgmesProfileType().equals(cgmesProfileType)).findAny();
    }
}
