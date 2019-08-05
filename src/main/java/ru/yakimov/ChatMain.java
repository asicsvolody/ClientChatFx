package ru.yakimov;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.yakimov.chat.ControllerChat;
import ru.yakimov.registration.RegController;

import java.io.IOException;
import java.util.HashMap;

public class ChatMain  extends Application {

    public static HashMap<String,Scene> sceneHashMap;
    public static HashMap<String, FXMLLoader> loaderHashMap;

    private final String[] urlFxmlArr = new String[]{
            "/resourcesLogin/loginPanel.fxml",
            "/resourcesReg/registration.fxml",
            "/resourcesChat/chatPanel.fxml",
            "/resourcesReg/passRecovery.fxml"
    };


    private final int WINDOW_WIDTH = 600;
    private final int WINDOW_HEIGHT = 575;

    public static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        ChatMain.primaryStage = primaryStage;
        sceneInitialization();
        primaryStage.setTitle("Yakimovs Chat");
        primaryStage.setScene(sceneHashMap.get("loginPanelScene"));
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(windowEVENT -> closeAllAndExit());

    }

    private void sceneInitialization() throws IOException {
        sceneHashMap = new HashMap<>();
        loaderHashMap = new HashMap<>();

        for (String urlFxml : urlFxmlArr) {
            String loaderName = getNameFromUrl(urlFxml)+"Loader";
            String sceneName = getNameFromUrl(urlFxml)+"Scene";
            loaderHashMap.put(loaderName,new FXMLLoader());
            Parent rootRecovery = loaderHashMap.get(loaderName).load(getClass().getResourceAsStream(urlFxml));
            sceneHashMap.put(sceneName, new Scene(rootRecovery,WINDOW_WIDTH,WINDOW_HEIGHT));
        }
    }

    private String getNameFromUrl(String url){
        char[] urlCharArr = url.toCharArray();
        StringBuilder sb = new StringBuilder() ;
        for (char c: urlCharArr ) {
            if(c == '.'){
                break;
            }
            sb.append(c);
            if(c == '/'){
                sb = new StringBuilder();
            }

        }return sb.toString();
    }

    private void closeAllAndExit(){
        ControllerChat controllerChat = loaderHashMap.get("chatPanelLoader").getController();
        RegController regController = loaderHashMap.get("registrationLoader").getController();
        controllerChat.dispose();
        regController.dispose();
        Platform.exit();
        System.exit(0);
    }
}

