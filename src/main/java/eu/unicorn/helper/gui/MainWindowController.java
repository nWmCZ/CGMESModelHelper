package eu.unicorn.helper.gui;

import eu.unicorn.cgmes.model.CgmesProfileDesc;
import eu.unicorn.cgmes.model.CgmesProfileType;
import eu.unicorn.cgmes.model.Model;
import eu.unicorn.helper.model.HelperModel;
import eu.unicorn.helper.model.ProfileField;
import eu.unicorn.helper.utils.ConfigurationParameter;
import eu.unicorn.helper.utils.ConfigurationSaveAs;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

import static eu.unicorn.cgmes.model.CgmesProfileType.*;
import static eu.unicorn.helper.model.HelperModel.DATE_PATTERN;

public class MainWindowController {

    private HelperModel helperModel;

    Logger logger = LoggerFactory.getLogger(MainWindowController.class);

    @FXML
    public void initialize() {
        helperModel = new HelperModel();
        choiseBoxSaveAs.setItems(FXCollections.observableArrayList(ConfigurationSaveAs.values()));
        choiseBoxSaveAs.setValue(ConfigurationSaveAs.XML);

        loadUserConfiguration();
    }

    private void loadUserConfiguration() {
        // TODO make it as application.properties
        String customPrefix = "PREF_";
        String customPostfix = "_POST";
        String defaultSavePathXML = "/Work/qas2_work_folder/profiles/Test_HappyDay/output/";
        String defaultSavePathZIP = "/Work/qas2_work_folder/profiles/Test_HappyDay/output/";

        if (customPrefix != null && customPrefix.length() > 0) {
            textFieldCustomPrefix.setText(customPrefix);
        }

        if (customPostfix != null && customPostfix.length() > 0) {
            textFieldCustomPostfix.setText(customPostfix);
        }

        if (defaultSavePathXML != null && defaultSavePathXML.length() > 0) {
            textFieldSaveDestinationXML.setText(defaultSavePathXML);
        }

        if (defaultSavePathZIP != null && defaultSavePathZIP.length() > 0) {
            textFieldSaveDestinationZIP.setText(defaultSavePathZIP);
        }

        choiseBoxSaveAs.setValue(ConfigurationSaveAs.XML);
    }

    @FXML
    void customPostfixOnAction(ActionEvent event) {
        if (checkBoxCustomPostfix.isSelected()) {
            textFieldCustomPostfix.setDisable(false);
        } else {
            textFieldCustomPostfix.setDisable(true);
        }
    }

    @FXML
    void customPrefixOnAction(ActionEvent event) {
        if (checkBoxCustomPrefix.isSelected()) {
            textFieldCustomPrefix.setDisable(false);
        } else {
            textFieldCustomPrefix.setDisable(true);
        }
    }

    @FXML
    void checkBoxAllOnAction(ActionEvent event) {
        if (checkBoxAll.isSelected()) {
            checkBoxSV.setSelected(true);
            checkBoxSSH.setSelected(true);
            checkBoxEQ.setSelected(true);
            checkBoxTP.setSelected(true);
            checkBoxTPBD.setSelected(true);
            checkBoxEQBD.setSelected(true);
            checkedCheckBoxCount = 6;
        } else {
            checkBoxSV.setSelected(false);
            checkBoxSSH.setSelected(false);
            checkBoxEQ.setSelected(false);
            checkBoxTP.setSelected(false);
            checkBoxTPBD.setSelected(false);
            checkBoxEQBD.setSelected(false);
            checkedCheckBoxCount = 0;
        }
        updateCheckboxes(checkedCheckBoxCount);
    }

    private int checkedCheckBoxCount;

    private void updateCheckboxes(int size) {
        if (checkedCheckBoxCount > 0) {
            buttonSaveToDestination.setDisable(false);
        } else {
            buttonSaveToDestination.setDisable(true);
            checkBoxAll.setSelected(false);
        }
    }

    @FXML
    void checkBoxSingleOnAction(ActionEvent event) {
        CheckBox checked = (CheckBox) event.getSource();
        if (checked.isSelected()) {
            checkedCheckBoxCount++;
            System.out.println(checkedCheckBoxCount);
        } else {
            checkedCheckBoxCount--;
            System.out.println(checkedCheckBoxCount);
        }
        updateCheckboxes(checkedCheckBoxCount);
    }

    private Map<ProfileField, String> changedValues = new HashMap<>();
    @FXML
    void textBoxProfileValuesChanged(Event event) {
        // TODO not working event
        logger.info(event.toString());
        buttonUpdateModel.setDisable(false);
        TextField sourceTextField = (TextField) event.getSource();
        CgmesProfileType profileType = CgmesProfileType.valueOf(textFieldProfileType.getText());
//        if (changedValues.containsKey(profileType)) {
//            changedValues.replace(ProfileField.VERSION, sourceTextField.getText());
//        }
    }

    private ProfileField getProfileField(TextField textField) {
        // TODO
        return ProfileField.VERSION;
    }

    @FXML
    void buttonUpdateModelOnAction(ActionEvent event) {
        Map<ProfileField, String> configuration = new HashMap<>();

        configuration.put(ProfileField.RDF_ABOUT, textFieldProfileRdfAbout.getText());
        configuration.put(ProfileField.CREATED, textFieldCreated.getText());
        configuration.put(ProfileField.SCENARIO_TIME, textFieldScenarioTime.getText());
        configuration.put(ProfileField.VERSION, textFieldProfileVersion.getText());
        configuration.put(ProfileField.AUTORITY_SET, textFieldProfileAutoritySet.getText());

        helperModel.updateProfile(CgmesProfileType.valueOf(textFieldProfileType.getText()), configuration);
    }

    @FXML
    void buttonModelOnAction(ActionEvent event) {
        Button button = (Button) event.getSource();
        Optional<CgmesProfileDesc> model;
        if (button.getId().equals(buttonSV.getId())) {
            model = helperModel.getCgmesProfileDesc(STATE_VARIABLES);
        } else if (button.getId().equals(buttonSSH.getId())) {
            model = helperModel.getCgmesProfileDesc(STEADY_STATE_HYPOTHESIS);
        } else if (button.getId().equals(buttonEQ.getId())) {
            model = helperModel.getCgmesProfileDesc(EQUIPMENT);
        } else if (button.getId().equals(buttonTP.getId())) {
            model = helperModel.getCgmesProfileDesc(TOPOLOGY);
        } else if (button.getId().equals(buttonEQBD.getId())) {
            model = helperModel.getCgmesProfileDesc(EQUIPMENT_BOUNDARY);
        } else {
            model = helperModel.getCgmesProfileDesc(TOPOLOGY_BOUNDARY);
        }
        populateFields(model.get());
    }

    @FXML
    void buttonSaveToDestinationOnAction(ActionEvent event) {
        List<CgmesProfileType> profilesToSend = new ArrayList<>();

        if (checkBoxSV.isSelected()) profilesToSend.add(STATE_VARIABLES);
        if (checkBoxEQ.isSelected()) profilesToSend.add(CgmesProfileType.EQUIPMENT);
        if (checkBoxTP.isSelected()) profilesToSend.add(CgmesProfileType.TOPOLOGY);
        if (checkBoxSSH.isSelected()) profilesToSend.add(CgmesProfileType.STEADY_STATE_HYPOTHESIS);
        if (checkBoxTPBD.isSelected()) profilesToSend.add(CgmesProfileType.TOPOLOGY_BOUNDARY);
        if (checkBoxEQBD.isSelected()) profilesToSend.add(CgmesProfileType.EQUIPMENT_BOUNDARY);

        Map<ConfigurationParameter, Object> configurationParameters = new HashMap<>();

        if (checkBoxGenerateAllNewIDs.isSelected()) {
            configurationParameters.put(ConfigurationParameter.GENERATE_NEW_IDS, true);
        }

        if (checkBoxIncreaseVersion.isSelected()) {
            configurationParameters.put(ConfigurationParameter.INCREASE_VERSIONS, true);
        }

        if (checkBoxCustomPrefix.isSelected()) {
            configurationParameters.put(ConfigurationParameter.PREFIX, textFieldCustomPrefix.getText());
        }

        if (checkBoxCustomPostfix.isSelected()) {
            configurationParameters.put(ConfigurationParameter.POSTFIX, textFieldCustomPostfix.getText());
        }

        Map<ConfigurationSaveAs, String> saveAsWithPath = new HashMap();
        if (choiseBoxSaveAs.getValue().equals(ConfigurationSaveAs.XML)) {
            saveAsWithPath.put(ConfigurationSaveAs.XML, textFieldSaveDestinationXML.getText());
            helperModel.saveToDestination(configurationParameters, saveAsWithPath , profilesToSend.toArray(new CgmesProfileType[profilesToSend.size()]));
        } else if (choiseBoxSaveAs.getValue().equals(ConfigurationSaveAs.ZIP)) {
            saveAsWithPath.put(ConfigurationSaveAs.ZIP, textFieldSaveDestinationZIP.getText());
            helperModel.saveToDestination(configurationParameters, saveAsWithPath, profilesToSend.toArray(new CgmesProfileType[profilesToSend.size()]));
        } else {
            // TODO create for both
            logger.error("Not implemented yet");
        }
    }

    @FXML
    void buttonSendToOpdeClientOnAction(ActionEvent event) {

    }

    @FXML
    void buttonChooseSavePathOnAction(ActionEvent event) {

        Button button = (Button )event.getSource();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Path");
        File f = directoryChooser.showDialog(mainVBox.getScene().getWindow());
        if (f != null) {
            switch (button.getId()) {
                case "buttonChooseSavePathXML":
                    textFieldSaveDestinationXML.setText(f.getAbsolutePath());
                    buttonSaveToDestination.setDisable(false);
                case "buttonChooseSavePathZIP":
            }
            // TODO when sending to OPDE is implemented
            // buttonSendToOpdeClient.setDisable(false);
        }
    }



    @FXML
    void buttonUploadOnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource Files");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP", "*.zip"));
        List<File> files = fileChooser.showOpenMultipleDialog(mainVBox.getScene().getWindow());

        Model model = helperModel.parseInput(files);
        if (model.getCgmesProfileDescs().size() == 6) {
            checkBoxAll.setDisable(false);
        }
        processParsedModel(model);
    }

    private void processParsedModel(Model model) {

        for (CgmesProfileDesc profileDescription: model.getCgmesProfileDescs().values()) {
            switch (profileDescription.getCgmesProfileType()) {
                case STATE_VARIABLES:
                    buttonSV.setDisable(false);
                    checkBoxSV.setDisable(false);
                    break;
                case TOPOLOGY:
                    buttonTP.setDisable(false);
                    checkBoxTP.setDisable(false);
                    break;
                case STEADY_STATE_HYPOTHESIS:
                    buttonSSH.setDisable(false);
                    checkBoxSSH.setDisable(false);
                    break;
                case EQUIPMENT:
                    buttonEQ.setDisable(false);
                    checkBoxEQ.setDisable(false);
                    break;
                case TOPOLOGY_BOUNDARY:
                    buttonTPBD.setDisable(false);
                    checkBoxTPBD.setDisable(false);
                    break;
                case EQUIPMENT_BOUNDARY:
                    buttonEQBD.setDisable(false);
                    checkBoxEQBD.setDisable(false);
                    break;
            }
        }
    }

    private void populateFields(CgmesProfileDesc profileDesc) {
        textFieldProfileType.setText(profileDesc.getCgmesProfileType().toString());
        textFieldProfileRdfAbout.setText(profileDesc.getRdfId());
        Format formatter = new SimpleDateFormat(DATE_PATTERN);

        textFieldCreated.setText(formatter.format(profileDesc.getCreated()));
        textFieldScenarioTime.setText(formatter.format(profileDesc.getScenarioTime()));
        textFieldProfileVersion.setText(profileDesc.getVersion());
        textFieldProfileAutoritySet.setText(profileDesc.getModelingAuthoritySet());

        // TODO dependentOn is empty
        listViewProfileDependentOn.getItems().clear();
        listViewProfileDependentOn.getItems().addAll(FXCollections.observableArrayList(profileDesc.getDependentOn()));
    }

    @FXML
    private TextField textFieldProfileType;

    @FXML
    private TextField textFieldProfileAutoritySet;

    @FXML
    private Line lineSVtoSSH;

    @FXML
    private Button buttonSSH;

    @FXML
    private Line lineSVtoTPBD;

    @FXML
    private CheckBox checkBoxCustomPostfix;

    @FXML
    private TextField textFieldCustomPrefix;

    @FXML
    private Line lineEQtoEQBD;

    @FXML
    private Line lineTPtoEQ;

    @FXML
    private AnchorPane anchorPaneMain;

    @FXML
    private ChoiceBox<ConfigurationSaveAs> choiseBoxSaveAs;

    @FXML
    private Label labelParsingTime;

    @FXML
    private Line lineSSHtoEQ;

    @FXML
    private TextField textFieldCreated;

    @FXML
    private TextField textFieldProfileVersion;

    @FXML
    private Button buttonUpload;

    @FXML
    private TextField textFieldProfileRdfAbout;

    @FXML
    private Button buttonTP;

    @FXML
    private Button buttonSV;

    @FXML
    private Line lineTPBDtoEQBD;

    @FXML
    private TextField textFieldScenarioTime;

    @FXML
    private ListView<CgmesProfileDesc> listViewProfileDependentOn;

    @FXML
    private Button buttonEQ;

    @FXML
    private Button buttonTPBD;

    @FXML
    private VBox mainVBox;

    @FXML
    private TextField textFieldCustomPostfix;

    @FXML
    private CheckBox checkBoxCustomPrefix;

    @FXML
    private TextField textFieldSaveDestinationXML;

    @FXML
    private TextField textFieldSaveDestinationZIP;

    @FXML
    private Line lineSVtoTP;

    @FXML
    private Button buttonEQBD;

    @FXML
    private Button buttonSaveToDestination;

    @FXML
    private Button buttonChooseSavePathXML;

    @FXML
    private Button buttonChooseSavePathZIP;

    @FXML
    private Button buttonSendToOpdeClient;

    @FXML
    private Button buttonUpdateModel;

    @FXML
    private CheckBox checkBoxAll;

    @FXML
    private CheckBox checkBoxSV;

    @FXML
    private CheckBox checkBoxSSH;

    @FXML
    private CheckBox checkBoxEQ;

    @FXML
    private CheckBox checkBoxTP;

    @FXML
    private CheckBox checkBoxEQBD;

    @FXML
    private CheckBox checkBoxTPBD;

    @FXML
    private CheckBox checkBoxIncreaseVersion;

    @FXML
    private CheckBox checkBoxGenerateAllNewIDs;
}
