package ru.yakimov.registration;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ru.yakimov.ChatMain;


public class RecoveryController {

    private  RegController regController;

    @FXML
    private Label recoveryLbl;

    @FXML
    private TextField loginFieldRecovery;

    @FXML
    private TextField controlWordRecovery;

    public void toRecovery(){
        regController = ChatMain.loaderHashMap.get("registrationLoader").getController();
        if(regController.socket == null || regController.socket.isClosed()){
            regController.connectReg();
        }
        if(controlWordRecovery.getText().length()==0){
            recoveryLbl.setText("Вы не ввели контрольное слово!");
        }else if(loginFieldRecovery.getText().length()==0){
            recoveryLbl.setText("Вы не ввели логин");
        }else {
            regController.sendToServer("/recovery "+loginFieldRecovery.getText()+" "+controlWordRecovery.getText());
        }
    }

    void readResult(String serverMsg){
        String[] magArr = serverMsg.split(" ",2);
        Platform.runLater(()-> recoveryLbl.setText(magArr[1]));
    }

    public void backToLoginScene(){
        regController.backToLoginScene();
    }
}
