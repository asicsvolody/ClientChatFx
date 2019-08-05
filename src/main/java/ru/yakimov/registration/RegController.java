package ru.yakimov.registration;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.yakimov.ChatMain;

public class RegController {

    private RecoveryController recoveryController = null ;

    @FXML
    Label registrationLbl;
    @FXML
    Label errMsg;

    @FXML
    TextField loginField;

    @FXML
    TextField passField;

    @FXML
    TextField rePassField;

    @FXML
    TextField nickField;

    @FXML
    TextField controlWord;

    @FXML
    Button regBtn;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;



    public void toRegistration(){
       if(isDataValid()) {
            if(socket == null || socket.isClosed()){
                connectReg();
            }
            sendToServer("/registration "+loginField.getText()+" "+passField.getText()+" "
                        +nickField.getText()+" "+controlWord.getText());
        }

    }

    private boolean isDataValid(){
        boolean res = true;
        if(!isTextFieldValid(loginField.getText()) || !isTextFieldValid(nickField.getText())
                || !isTextFieldValid(controlWord.getText())){
            errMsg.setText("Заполните все поля(4-20 символов)");
            res =false;
        } else if(!isPasswordValidSymbols(passField.getText())){
            errMsg.setText("Пароль неверного формата A-z,0-1,_,-");
            res = false;
        } else if(!isPasswordBigLetter(passField.getText())){
            errMsg.setText("В пароле нет заглавнх букв");
            res = false;
        } else if (!isPasswordNumbers(passField.getText())) {
            errMsg.setText("Должны быть цифры");
            res = false;
        } else if (!passField.getText().equals(rePassField.getText())) {
            errMsg.setText("Пароли не совпадают");
            res=false;
        }
        return res;
    }

    void sendToServer(String str){
        try {
            out.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void connectReg(){
        try {
            if(recoveryController == null) {
                recoveryController = ChatMain.loaderHashMap.get("passRecoveryLoader").getController();
            }

            socketWaitAndInitialisation();

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(()-> {
                    try {
                        registrationCycle();
                    }catch (IOException e){
                        e.printStackTrace();
                    }finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            ).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void socketWaitAndInitialisation() throws IOException {
        try {
            socket = new Socket(IP_ADDRESS, PORT);

        }catch(ConnectException e){
            System.out.println("Ожидание соединения");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            socketWaitAndInitialisation();
        }
    }

    private void registrationCycle() throws IOException {
        while (true) {
            String str = in.readUTF();
            if(str.startsWith("/serverclosed")) break;
            if(str.startsWith("/regok")){
                successRegAllClear();
                sendToServer("/end");
            }else if (str.startsWith("/recovery")){
                recoveryController.readResult(str);
                sendToServer("/end");
            }
            else{
                Platform.runLater(()-> errMsg.setText(str));
            }
        }
    }

    private void successRegAllClear(){
        Platform.runLater(()->{
            registrationLbl.setText(" Вы зарегистрированы");
            errMsg.setText("");
            loginField.clear();
            loginField.requestFocus();
            passField.clear();
            rePassField.clear();
            nickField.clear();
            controlWord.clear();
        });
    }

    public static boolean isTextFieldValid(String str){

            Pattern patternSymbols = Pattern.compile("^[A-z,0-9,_,\\-]{4,20}$");
            Matcher matcher = patternSymbols.matcher(str);
           return matcher.matches();
    }

    private static boolean isPasswordValidSymbols(String password) {
        Pattern patternSymbols = Pattern.compile(
                "^[A-z,0-9,_,\\-]{5,20}$");
        Matcher matcher = patternSymbols.matcher(password);
        return matcher.matches();
    }

    private static boolean isPasswordBigLetter(String password){
        Pattern patternSymbols = Pattern.compile(
                ".*[A-Z]+.*");
        Matcher matcher = patternSymbols.matcher(password);
        return matcher.matches();
    }

    private static boolean isPasswordNumbers(String password){
        Pattern patternSymbols = Pattern.compile(
                ".*\\d+.*");
        Matcher matcher = patternSymbols.matcher(password);
        return matcher.matches();
    }

    public void backToLoginScene(){
        ChatMain.primaryStage.setScene(ChatMain.sceneHashMap.get("loginPanelScene"));

    }

    public void dispose(){
        try {
            if(out != null && !socket.isClosed()) {
                System.out.println("Close");
                out.writeUTF("/end");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
