package eu.unicorn.helper.gui;

import eu.unicorn.cgmes.model.CgmesProfileDesc;
import eu.unicorn.cgmes.model.CgmesProfileType;
import eu.unicorn.cgmes.model.Model;
import eu.unicorn.helper.model.HelperModel;
import eu.unicorn.helper.model.ParsingResult;
import eu.unicorn.helper.utils.ConfigurationSaveAs;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;

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

import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

import static eu.unicorn.cgmes.model.CgmesProfileType.*;
import static eu.unicorn.helper.model.HelperModel.getCgmesProfileTypeFromModel;

public class MainWindowController {

    private HelperModel helperModel;

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
        String defaultSavePath = "/Work/qas2_work_folder/profiles/Test_HappyDay/output/";
        ConfigurationSaveAs saveAs = ConfigurationSaveAs.XML;

        if (customPrefix != null && customPrefix.length() > 0) {
            textFieldCustomPrefix.setText(customPrefix);
        }

        if (customPostfix != null && customPostfix.length() > 0) {
            textFieldCustomPostfix.setText(customPostfix);
        }

        if (defaultSavePath != null && defaultSavePath.length() > 0) {
            textFieldSaveDestination.setText(defaultSavePath);
            buttonSaveToDestination.setDisable(false);
        }

        choiseBoxSaveAs.setValue(saveAs);
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
        } else {
            checkBoxSV.setSelected(false);
            checkBoxSSH.setSelected(false);
            checkBoxEQ.setSelected(false);
            checkBoxTP.setSelected(false);
            checkBoxTPBD.setSelected(false);
            checkBoxEQBD.setSelected(false);
        }
    }

    @FXML
    void buttonSVOnAction(ActionEvent event) {
        Optional<Model> model = helperModel.getModelByType(STATE_VARIABLES);
        if (model.isPresent()) {
            populateFields(model.get());
        } else {
            System.out.println("Model not present, it cannot be populated to GUI");
        }
    }

    @FXML
    void buttonSSHOnAction(ActionEvent event) {
        // TODO refactor to one method
        Optional<Model> model = helperModel.getModelByType(STEADY_STATE_HYPOTHESIS);
        if (model.isPresent()) {
            populateFields(model.get());
        } else {
            System.out.println("Model not present, it cannot be populated to GUI");
        }
    }

    @FXML
    void buttonEQOnAction(ActionEvent event) {
        // TODO refactor to one method
        Optional<Model> model = helperModel.getModelByType(EQUIPMENT);
        if (model.isPresent()) {
            populateFields(model.get());
        } else {
            System.out.println("Model not present, it cannot be populated to GUI");
        }
    }

    @FXML
    void buttonTPOnAction(ActionEvent event) {
        // TODO refactor to one method
        Optional<Model> model = helperModel.getModelByType(TOPOLOGY);
        if (model.isPresent()) {
            populateFields(model.get());
        } else {
            System.out.println("Model not present, it cannot be populated to GUI");
        }
    }

    @FXML
    void buttonTPBDOnAction(ActionEvent event) {
        // TODO refactor to one method
        Optional<Model> model = helperModel.getModelByType(TOPOLOGY_BOUNDARY);
        if (model.isPresent()) {
            populateFields(model.get());
        } else {
            System.out.println("Model not present, it cannot be populated to GUI");
        }
    }

    @FXML
    void buttonEQBDOnAction(ActionEvent event) {
        // TODO refactor to one method
        Optional<Model> model = helperModel.getModelByType(EQUIPMENT_BOUNDARY);
        if (model.isPresent()) {
            populateFields(model.get());
        } else {
            System.out.println("Model not present, it cannot be populated to GUI");
        }
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

        helperModel.saveToDestination(choiseBoxSaveAs.getValue(), textFieldSaveDestination.getText(), profilesToSend.toArray(new CgmesProfileType[profilesToSend.size()]));
    }

    @FXML
    void buttonSendToOpdeClientOnAction(ActionEvent event) {

    }

    @FXML
    void buttonChooseSavePathOnAction(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Path");
        File f = directoryChooser.showDialog(mainVBox.getScene().getWindow());
        if (f != null) {
            textFieldSaveDestination.setText(f.getAbsolutePath());
            buttonSaveToDestination.setDisable(false);
            // TODO when sending to OPDE is implemented
            // buttonSendToOpdeClient.setDisable(false);
        }
    }



    @FXML
    void buttonUploadOnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(mainVBox.getScene().getWindow());

        ParsingResult parsingResult = helperModel.parseInput(file);
        labelParsingTime.setText("Parsing time: " + parsingResult.getParsingTime());

        if (parsingResult.getException() == null) {
            processParsingResult(parsingResult);
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setWidth(400);
            a.setHeight(400);
            Exception e = parsingResult.getException();
            a.setContentText(e.getMessage() + "\n" + e.getStackTrace().toString());
            a.show();
        }
    }

    private void processParsingResult(ParsingResult parsingResult) {

        CgmesProfileType profileType = parsingResult.getProfileType();
        switch (profileType) {
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
            default:
                System.out.println("No profile recognized: " + profileType);
        }

        populateFields(parsingResult.getModel());
    }

    private void populateFields(Model model) {
        textFieldProfileType.setText(getCgmesProfileTypeFromModel(model).toString());
        CgmesProfileDesc profileDesc = model.getCgmesProfileDescs().values().iterator().next();
        textFieldProfileRdfAbout.setText(profileDesc.getRdfId());
        Format formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

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
    private TextField textFieldSaveDestination;

    @FXML
    private Line lineSVtoTP;

    @FXML
    private Button buttonEQBD;

    @FXML
    private Button buttonSaveToDestination;

    @FXML
    private Button buttonSendToOpdeClient;

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
}
