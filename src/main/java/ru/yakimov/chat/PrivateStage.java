package ru.yakimov.chat;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.yakimov.ChatMain;

import java.io.IOException;

public class PrivateStage extends Stage {
    ControllerPrivateChat controllerPrivateChat;
    String privateNickTo;


    public PrivateStage(String privateNickTo) {
        this.privateNickTo = privateNickTo;
        Parent root = null;
        try {
            FXMLLoader loaderPrivateChat = new FXMLLoader(getClass().getResource("../../../../resources/fxml/privateChat.fxml"));
            root = loaderPrivateChat.load();
            setTitle("PrivateChat");
            controllerPrivateChat= loaderPrivateChat.getController();
            Scene scene = new Scene(root,400,575);
            controllerPrivateChat= loaderPrivateChat.getController();

            setScene(scene);
            setResizable(false);
            setOnCloseRequest(windowEvent->{
                ChatMain.controllerChat.deleteFromPrivateStageArrayList(PrivateStage.this);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
