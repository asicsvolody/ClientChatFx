package ru.yakimov;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.yakimov.chat.ControllerChat;
import ru.yakimov.login.ControllerLogin;
import ru.yakimov.registration.RecoveryController;
import ru.yakimov.registration.RegController;

import java.io.IOException;
import java.util.HashMap;

public class ChatMain  extends Application {

    public static HashMap<String,Scene> sceneHashMap;
    public static ControllerChat controllerChat;
    public static ControllerLogin controllerLogin;
    public static RegController regController;
    public static RecoveryController recoveryController;


    private final int WINDOW_WIDTH = 600;
    private final int WINDOW_HEIGHT = 575;

    public static Stage primaryStage;
    @Override
    public void start(Stage primaryStage) throws Exception {
        ChatMain.primaryStage = primaryStage;
        sceneInitialization();
        primaryStage.setTitle("Yakimovs Chat");
        primaryStage.setScene(sceneHashMap.get("sceneLogin"));
        primaryStage.setResizable(false);
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(windowEVENT ->{
            controllerChat.dispose();
            regController.dispose();
            Platform.exit();
            System.exit(0);
        });

    }

    private void sceneInitialization() throws IOException{
        sceneHashMap = new HashMap<>();

        FXMLLoader loaderLogin = new FXMLLoader();
        Parent rootLogin = loaderLogin.load(getClass()
                .getResourceAsStream("/resourcesLogin/loginPanel.fxml"));
        sceneHashMap.put("sceneLogin", new Scene(rootLogin,WINDOW_WIDTH,WINDOW_HEIGHT));
        controllerLogin = loaderLogin.getController();

        FXMLLoader loaderRegistration = new FXMLLoader();
        Parent rootRegistration = loaderRegistration.load(getClass()
                .getResourceAsStream("/resourcesReg/registration.fxml"));
        sceneHashMap.put("sceneRegistration", new Scene(rootRegistration,WINDOW_WIDTH,WINDOW_HEIGHT));
        regController = loaderRegistration.getController();

        FXMLLoader loaderChat = new FXMLLoader();
        Parent rootChat = loaderChat.load(getClass()
                .getResourceAsStream("/resourcesChat/chatPanel.fxml"));
        sceneHashMap.put("sceneChat", new Scene(rootChat,WINDOW_WIDTH,WINDOW_HEIGHT));
        controllerChat = loaderChat.getController();

        FXMLLoader loaderRecovery = new FXMLLoader();
        Parent rootRecovery = loaderRecovery.load(getClass()
                .getResourceAsStream("/resourcesReg/passRecovery.fxml"));
        sceneHashMap.put("sceneRecovery", new Scene(rootRecovery,WINDOW_WIDTH,WINDOW_HEIGHT));
        recoveryController = loaderRecovery.getController();


    }


}

