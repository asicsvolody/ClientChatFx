package ru.yakimov.chat;

import javafx.application.Platform;
import ru.yakimov.ChatMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

class PrivateStage extends Stage {
    ControllerPrivateChat controllerPrivateChat;
    String privateNickTo;
    private Parent root = null;



    PrivateStage(String privateNickTo) {
        this.privateNickTo = privateNickTo;
        try {
            FXMLLoader loaderPrivateChat = new FXMLLoader(getClass().getResource("/resourcesChat/privateChat.fxml"));
            root = loaderPrivateChat.load();
            setTitle("PrivateChat");
            controllerPrivateChat= loaderPrivateChat.getController();
            root.getStylesheets().addAll(ChatMain.sceneHashMap.get("sceneChat").getRoot().getStylesheets());
            Scene scene = new Scene(root,400,575);
            controllerPrivateChat= loaderPrivateChat.getController();
            setScene(scene);
            setResizable(false);
//            setOnCloseRequest(windowEvent-> ChatMain.controllerChat.deleteFromPrivateStageHashMap(privateNickTo));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void changeCss(String cssUrl){
        root.getStylesheets().clear();
        root.getStylesheets().add(cssUrl);
    }

    void putTop(){
        Platform.runLater(()-> {
            PrivateStage.this.show();
            PrivateStage.this.setAlwaysOnTop(true);
            PrivateStage.this.setAlwaysOnTop(false);
            });
    }



}
