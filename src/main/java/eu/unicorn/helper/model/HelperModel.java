package eu.unicorn.helper.model;

import eu.unicorn.cgmes.model.CgmesProfileType;
import eu.unicorn.cgmes.model.Model;
import eu.unicorn.cgmes.stream.*;
import eu.unicorn.helper.utils.ConfigurationSaveAs;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class HelperModel {

    private Map<CgmesProfileType, Optional<Model>> profiles = new HashMap<>();

    public ParsingResult parseInput(File file) {
        final CgmesReaderContext ctx = CgmesReaderFactory.getContext();
        final CgmesReader reader = CgmesReaderFactory.getReader();
        long start, end;
        ParsingResult parsingResult = new ParsingResult();
        if (file != null) {
            InputStream is = null;
            start = System.currentTimeMillis();
            try {
                is = new FileInputStream(file);
                reader.read(is, ctx);
            } catch (Exception e) {
                // OPTIONAL : FileInputStream throws FileNotFoundException, parser throws more
                parsingResult.setException(e);
            }
            end = System.currentTimeMillis();
            long parsingTime = end - start;
            Model m = ctx.getModel();
            CgmesProfileType profileType = getCgmesProfileTypeFromModel(m);

            parsingResult.setModel(m);
            parsingResult.setParsingTime(parsingTime);
            parsingResult.setProfileType(profileType);

            addProfile(profileType, m);
        }
        return parsingResult;
    }

    public void addProfile(CgmesProfileType profileType, Model model) {
        // We assume only one profile, in multiple profiles, Set implementation must change
        this.profiles.put(profileType, Optional.of(model));
    }

    public void saveToDestination(ConfigurationSaveAs configurationSaveAs, String path, CgmesProfileType... profileTypes) {


        for (CgmesProfileType profileType: profileTypes) {

            // TODO proper file name
            String filename = profileType.name() + "_" + UUID.randomUUID().toString() + "." + configurationSaveAs.toString().toLowerCase();

            switch (configurationSaveAs) {
                case XML:
                    saveXML(profileType, path, filename);
                    break;
                case ZIP:
                    saveZip(profileType, path, filename);
                    break;
                case BOTH:
                    saveXML(profileType, path, filename);
                    saveZip(profileType, path, filename);
            }
        }
    }

    private void saveXML(CgmesProfileType profileType, String path, String filename) {

        Optional<Model> model = getModelByType(profileType);

        if (model.isPresent()) {
            final CgmesWriterContext writerCtx = CgmesWriterFactory.getContext(model.get());

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
        } else {
            System.out.println("Model not present, no write out");
        }
    }

    private void saveZip(CgmesProfileType profileType, String path, String filename) {
        //TODO save zip
    }

    public Map<CgmesProfileType, Optional<Model>> getProfiles() {
        return profiles;
    }

    public void setProfiles(Map<CgmesProfileType, Optional<Model>> profiles) {
        this.profiles = profiles;
    }

    public Optional<Model> getModelByType(CgmesProfileType profileType) {
        return this.profiles.get(profileType);
    }

    public static CgmesProfileType getCgmesProfileTypeFromModel(Model model) {
        return model.getCgmesProfileDescs().values().iterator().next().getCgmesProfileType();
    }
}
