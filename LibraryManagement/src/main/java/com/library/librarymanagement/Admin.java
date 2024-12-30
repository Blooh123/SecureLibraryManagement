package com.library.librarymanagement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Admin {
    @FXML
    private BorderPane borderPane;
    private Map<String, Pane> loadedScenes = new HashMap<>();

    @FXML
    private void userManagementAction(ActionEvent event) throws IOException {
        setCenteredPane("AdminUserManagement.fxml");
    }
    @FXML
    private void bookInventoryAction(ActionEvent event) throws  IOException{
        setCenteredPane("AdminBookInventory.fxml");
    }
    @FXML
    private void reportsAction(ActionEvent event) throws IOException {
        setCenteredPane("AdminReports.fxml");
    }

    private void setCenteredPane(String fxml) throws IOException {
        Pane pane = loadedScenes.get(fxml);
        if (pane == null) {  // Load and cache if not already loaded
            pane = FXMLLoader.load(getClass().getResource(fxml));
            loadedScenes.put(fxml, pane);
        }
        borderPane.setCenter(pane);
    }
}
