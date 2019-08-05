package ru.yakimov.login;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ru.yakimov.ChatMain;
import ru.yakimov.chat.ControllerChat;
import ru.yakimov.registration.RegController;

import java.net.Socket;

public class ControllerLogin {


    private ControllerChat controllerChat;

    @FXML
    private Label labelNotIdentification;

    @FXML
    private TextField loginField;

    @FXML
    private TextField passwordField;

    private volatile boolean isConnect = false;

    public void setAuthorized(boolean isAuthorized){
        controllerChat.setLogin(isAuthorized);
        if(isAuthorized){
            Platform.runLater(()-> ChatMain.primaryStage.setScene(ChatMain.sceneHashMap.get("sceneChat")));
        }else{
            Platform.runLater(()-> {
                    ChatMain.primaryStage.setScene(ChatMain.sceneHashMap.get("sceneLogin"));
                    ChatMain.controllerChat.clearChat();
                });
        }
    }

    public synchronized void tryToAuth() {
        controllerChat = ChatMain.controllerChat;
        Socket socket = controllerChat.socket;


        String login = loginField.getText();
        String password = passwordField.getText();

        if(isLoginPassValid(login, password)) {

            if(socket == null || socket.isClosed()) {
                controllerChat.connect();
            }
            waitSocket();
            controllerChat.writeLoginPassword(login, password);
            controllerChat.sendMsgFromString("/auth " + login + " " + password);
            loginField.clear();
            passwordField.clear();
            writeToLabelNotIdentification(" ");
        }else{
            writeToLabelNotIdentification("Введены неправильные значения");
        }
    }

    private void waitSocket(){
        while(!isConnect){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isLoginPassValid(String login, String password){
        return RegController.isTextFieldValid(login) && RegController.isTextFieldValid(password);
    }

    public void writeToLabelNotIdentification(String str){
        Platform.runLater(()-> labelNotIdentification.setText(str));
    }


    public void registrationScene(){
        ChatMain.primaryStage.setScene(ChatMain.sceneHashMap.get("sceneRegistration"));
    }

    public void recoveryPassScene(){
        ChatMain.primaryStage.setScene(ChatMain.sceneHashMap.get("sceneRecovery"));
    }

    public void setConnect(boolean connect) {
        isConnect = connect;
    }
}
