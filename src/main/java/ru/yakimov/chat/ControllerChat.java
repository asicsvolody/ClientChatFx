package ru.yakimov.chat;

import ru.yakimov.ChatMain;
import ru.yakimov.login.ControllerLogin;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;

public class ControllerChat {

    public Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private HashMap<String, PrivateStage> privateStageHashMap = new HashMap<>();

    private ControllerLogin controllerLogin = ChatMain.controllerLogin;

    private boolean isLogin = false;
    private String login;
    private String password;
    private String nick;

    @FXML
    private TextField messageTextField;

    @FXML
    private Label nickName;

    @FXML
    private Circle circleIsInNet;

    @FXML
    ListView <String> clientList;

    @FXML
    private VBox vBoxMessage;

    @FXML
    private ScrollPane scrollPaneMsg;

    @FXML
    private Button btmSend;




    public void connect() {
        try {
            objectInitialization();

            componentsPreparation();

            reLoginAfterCrashServer();

            new Thread(()->{
                    try {
                        authorizationCycle();

                        mainWorkCycle();
                    }catch (EOFException e){
                        crashServerInstruction();
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void objectInitialization() throws IOException {

        socketWaitAndInitialisation();

        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    private void componentsPreparation(){
        ChatMain.controllerLogin.setConnect(true);
        clientList.setOnContextMenuRequested((event)-> {
                MyContextMenu myContextMenu = new MyContextMenu(clientList.getSelectionModel().getSelectedItem());
                myContextMenu.show(clientList,event.getScreenX(),event.getScreenY());
            });
        setDisableBtmAndField(false);
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

    private void authorizationCycle() throws IOException {
        while(true) {
            String str = in.readUTF();
            if(str.startsWith("/serverclosed")) break;
            if(str.startsWith("/authok")) {
                if(!isLogin) {
                    controllerLogin.setAuthorized(true);
                    String[] nickArr = str.split(" ");
                    nick = nickArr[1];
                    Platform.runLater(()->nickName.setText(nick));
                }
                break;
            }else{
                controllerLogin.writeToLabelNotIdentification(str);
            }
        }
    }

    private void mainWorkCycle() throws IOException {
        while(isLogin) {
            String str = in.readUTF();
            if (str.startsWith("/serverclosed")) break;
            if (str.startsWith("/clientlist")) {
                String[] tokens = str.split(" ");
                Platform.runLater(()-> {
                        clientList.getItems().clear();
                        for (int i = 1; i < tokens.length; i++) {
                            clientList.getItems().add(tokens[i]);
                        }
                    });
            } else if (str.startsWith("/w")) {
                getPrivateMessage(str);
            } else {
                inputToVBoxMessage(str );
            }
        }
    }

    private void crashServerInstruction(){
        setDisableBtmAndField(true);
        new Thread(ControllerChat.this::connect).start();
        inputToVBoxMessageSystem("Сервер упал. Ожидание подключения");
        System.out.println("Поиск сервера");
    }


    public void sendMsg(){
        try {
            out.writeUTF(messageTextField.getText());
            messageTextField.clear();
            messageTextField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void inputToVBoxMessage(String msg){
        String[] msgArr = msg.split(" ",2);
        if(msgArr[0].equals("/systemmsg")){
            inputToVBoxMessageSystem(msgArr[1]);
        } else if(msgArr[0].equals(nick)){
           inputToVBoxMessageMy(msgArr[1]);
        }else {
            inputToVBoxMessageOther(msgArr[0], msgArr[1]);
        }
        scrollPaneMsg.vvalueProperty().bind(vBoxMessage.heightProperty());

    }

    private void inputToVBoxMessageSystem(String msg){
        if(!msg.startsWith("null")) {
            Platform.runLater(()-> vBoxMessage.getChildren().add(new SystemMessageHBox(msg)));
        }
    }

    private void inputToVBoxMessageMy(String msg){
        Platform.runLater(()-> vBoxMessage.getChildren().add(new MyMessageHBox(makeMessageForLabel(msg))));
    }

    private void inputToVBoxMessageOther(String otherNick, String msg){
        Platform.runLater(()-> vBoxMessage.getChildren().add(new OtherMessageHBox(otherNick, makeMessageForLabel(msg))));
    }

    public void sendMsgFromString(String str){
        try {
            out.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void createPrivateChatFromClick (MouseEvent mouseEvent){
        if(mouseEvent.getClickCount() == 2){
            String nickTo = clientList.getSelectionModel().getSelectedItem();
            if(nickName.getText().equals(nickTo)){
                showWarningAlert("Вы не можете открыть приватный чат с самим собой!");
            }else{
                activationPrivateStage(nickTo);
            }
        }
    }

    private void showWarningAlert(String msg){
        Alert alert = new Alert(Alert.AlertType.WARNING,msg);
        alert.show();
    }


    private void getPrivateMessage(String str){

        String[] privateMsgArr = str.split(" ",4);

        activationPrivateStage(privateMsgArr[1]);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        writePrivateMsg(privateMsgArr[1],privateMsgArr[2],privateMsgArr[3],str.startsWith("/wsystemmsg"));


    }

    private void activationPrivateStage(String nickTo){
//        if(deletedPrivateStageHashMap.containsKey(nickTo)){
//            resurrectPrivateChat(nickTo);
//        }
        if(privateStageHashMap.containsKey(nickTo)){
            privateStageHashMap.get(nickTo).putTop();
        }else if(!nickTo.equals(nickName.getText())){
            createPrivateChat(nickTo);
        }
    }

    private void writePrivateMsg (String nickTo,String nickFrom, String msg, boolean isSystemMsg){
        if(isSystemMsg){
            privateStageHashMap.get(nickTo).controllerPrivateChat
                    .addToVBoxMessage(new SystemMessageHBox(nickFrom+" "+msg));
        }
        else if (nickFrom.equals(nickName.getText())) {
            privateStageHashMap.get(nickTo).controllerPrivateChat
                .addToVBoxMessage(new MyMessageHBox(makeMessageForLabel(msg)));
        }else{ privateStageHashMap.get(nickTo).controllerPrivateChat
                    .addToVBoxMessage(new OtherMessageHBox(nickFrom,makeMessageForLabel(msg)));
        }
    }


    private void createPrivateChat(String nickTo){
        Platform.runLater(()->{
            PrivateStage ps = new PrivateStage(nickTo);
            privateStageHashMap.put(nickTo, ps);
            ps.controllerPrivateChat.setLabelNickTo(nickTo);
            ps.show();
        });
    }

    private String makeMessageForLabel(String msg){
        final int NUMBER_IF_SINGS = 30;
        StringBuilder sb = new StringBuilder();
        char[] charMsgArr = msg.toCharArray();
        int charNumber = 0;
        for (char c: charMsgArr) {
            sb.append(c);
            if(charNumber++ >= NUMBER_IF_SINGS){
                sb.append("\n");
                charNumber = 0;
            }
        }
        return sb.toString();
    }


    private void setDisableBtmAndField(boolean isDisable){
        Platform.runLater(()->{
            if(isDisable){
                circleIsInNet.setStyle("-fx-fill: red");
            }else{
                circleIsInNet.setStyle("-fx-fill: green");
            }
            messageTextField.setDisable(isDisable);
            btmSend.setDisable(isDisable);
            clientList.setDisable(isDisable);
            privateStageHashMap.forEach((k,v)-> v.hide());
            });
    }


    private void reLoginAfterCrashServer(){
        if(isLogin){
            try {
                out.writeUTF("/auth "+login+" "+ password);
                inputToVBoxMessageSystem("Успешное подключение");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void writeLoginPassword(String login, String password){
        this.login = login;
        this.password = password;
    }


    public void makeGreenYellowTheme(){
       changeCssFromEverything("resourcesChat/css/StyleClassYellow.css");
    }

    public void makeBlueRedTheme(){
        changeCssFromEverything("resourcesChat/css/StyleClassBlue.css");
    }

    private void changeCssFromEverything(String cssUrl){
        ChatMain.sceneHashMap.get("sceneChat").getRoot().getStylesheets().clear();
        ChatMain.sceneHashMap.get("sceneChat").getRoot().getStylesheets().add(cssUrl);
        privateStageHashMap.forEach((k,v)->v.changeCss(cssUrl));

    }


    public void logout(){
        controllerLogin.setAuthorized(false);
        dispose();
    }

    public void clearBlacklist(){
        sendMsgFromString("/clearblacklist");
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    public void clearChat(){
        vBoxMessage.getChildren().clear();
    }



    public void dispose(){
        try {
            if( out != null && !socket.isClosed()) {
                System.out.println("Close");
                out.writeUTF("/end");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class MyContextMenu extends ContextMenu {

        private MyContextMenu(String nick) {
            MenuItem addBlackList = new MenuItem("add to BlackList");
            MenuItem removeFromBlackList = new MenuItem(" remove from BlackList");

            addBlackList.setOnAction((event)-> sendMsgFromString("/blacklist "+nick));
            removeFromBlackList.setOnAction((event)->sendMsgFromString("/delblacklist "+nick));

            this.getItems().addAll(addBlackList,removeFromBlackList);
        }
    }


    private class MyMessageHBox extends HBox {


        private MyMessageHBox(String message) {
            setMaxWidth(350);
            setAlignment(Pos.CENTER_LEFT);

            Pane pane = new Pane();
            pane.setPrefWidth(10);
            this.getChildren().add(pane);

            Label nickLabel = new Label("ME: ");
            nickLabel.setFont(Font.font("Arial", FontWeight.BOLD,16));
            nickLabel.setAlignment(Pos.CENTER);
            nickLabel.setTextFill(Color.RED);
            nickLabel.setMaxWidth(140);
            this.getChildren().add(nickLabel);


            this.getChildren().add(new PaneMessage(message, Color.YELLOW));

        }


    }

    private class PaneMessage extends Pane {
        private PaneMessage (String message, Color color){
            Text msg = new Text(message);
            msg.setFont(Font.font("Arial", FontPosture.ITALIC,14));
            Bounds messageBounds = msg.getBoundsInParent();

            double msgWight = messageBounds.getWidth();
            double msgHeight = messageBounds.getHeight();
            double paneWight = msgWight + 20;
            double paneHeight = msgHeight + 20;
            double msgX = (paneWight - msgWight)/2;
            double msgY = (paneHeight-msgHeight)/2;

            msg.relocate(msgX,msgY);

            Rectangle rectangle = new Rectangle(0 ,0,paneWight,paneHeight);
            rectangle.setArcWidth(20);
            rectangle.setArcHeight(20);
            rectangle.setFill(color);

           this.getChildren().addAll(rectangle, msg);
           setPrefSize(paneWight,paneHeight);

        }
    }





    private class SystemMessageHBox extends HBox {

        private SystemMessageHBox(String msg) {

            setPrefWidth(350);
            setAlignment(Pos.CENTER);

            Label messageLbl = new Label(msg);
            messageLbl.setFont(Font.font("Arial",12));
            messageLbl.setAlignment(Pos.CENTER);
            messageLbl.setTextFill(Color.GRAY);
            messageLbl.setMaxWidth(350);
            this.getChildren().add(messageLbl);
        }
    }



    private class OtherMessageHBox extends HBox {

        private OtherMessageHBox(String nickname, String message) {
            setMaxWidth(350);
            setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(new PaneMessage(message, Color.PINK));

            Label nickLabel = new Label(" :"+nickname);
            nickLabel.setFont(Font.font("Arial", FontWeight.BOLD,16));
            nickLabel.setAlignment(Pos.CENTER);
            nickLabel.setTextFill(Color.GREEN);
            nickLabel.setMaxWidth(140);
            this.getChildren().add(nickLabel);

            Pane pane = new Pane();
            pane.setPrefWidth(10);
            this.getChildren().add(pane);

        }
    }

}
