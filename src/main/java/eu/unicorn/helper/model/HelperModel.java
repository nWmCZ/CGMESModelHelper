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

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static eu.unicorn.helper.utils.FileUtils.saveXML;
import static eu.unicorn.helper.utils.FileUtils.saveZip;
import static eu.unicorn.helper.utils.NameUtils.*;
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
            profiles.put(profileType, saveXML(profileType, path.get(ConfigurationSaveAs.XML), getFilenameXML(getCgmesProfileDesc(profileType).get()) + "." + ConfigurationSaveAs.XML.toString().toLowerCase(), this.model));
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
            saveZip(path.get(ConfigurationSaveAs.ZIP), getFilenameZIPforBD(getCgmesProfileDesc(CgmesProfileType.TOPOLOGY_BOUNDARY).get()), profiles.get(CgmesProfileType.TOPOLOGY_BOUNDARY), profiles.get(CgmesProfileType.EQUIPMENT_BOUNDARY));
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

    public Optional<CgmesProfileDesc> getCgmesProfileDesc(CgmesProfileType cgmesProfileType) {
        return this.model.getCgmesProfileDescs().values().stream().filter(cgmesProfileDesc -> cgmesProfileDesc.getCgmesProfileType().equals(cgmesProfileType)).findAny();
    }
}
