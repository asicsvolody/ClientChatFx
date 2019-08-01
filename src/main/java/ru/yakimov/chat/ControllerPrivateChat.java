package ru.yakimov.chat;

import ru.yakimov.ChatMain;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ControllerPrivateChat {

    @FXML
    private Label nickNamePrivate;

    @FXML
    private TextField messageTextField;

    @FXML
    private VBox vBoxMessage;

    @FXML
    ScrollPane scrollPaneMsg;





    void setLabelNickTo(String nick){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                nickNamePrivate.setText(nick);

            }
        });
    }

    void addToVBoxMessage(HBox messageHBox){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vBoxMessage.getChildren().add(messageHBox);
                scrollPaneMsg.vvalueProperty().bind(vBoxMessage.heightProperty());

            }
        });
    }



    public void sendMsg(){
        String nickTo = ((PrivateStage)vBoxMessage.getScene().getWindow()).privateNickTo;
        ChatMain.controllerChat.sendMsgFromString("/w "+nickTo+" "+messageTextField.getText());
        messageTextField.clear();
        messageTextField.requestFocus();
    }

}
