package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.network.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

public class MultiplayerLobbyScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(MultiplayerLobbyScene.class);

    private Communicator communicator;
    private Timer timer;
    private TimerTask channelRefresh;
    private StringProperty channel = new SimpleStringProperty("");
    private StringProperty playerName = new SimpleStringProperty();
    private BooleanProperty amItheHost = new SimpleBooleanProperty(false);
    private Calendar cal = Calendar.getInstance();
    private SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
    private ScrollPane scroll;

    private ChannelBox channelBox;
    private UsersList usersList;
    private VBox messageBox = null;

    //Multiplayer lobby that uses the communicator
    public MultiplayerLobbyScene(GameWindow gameWindow){
        super(gameWindow);
        communicator = gameWindow.getCommunicator();
        logger.info("Building Multiplayer Lobby Scene");
    }

    //Initialising the multiplayer lobby, handling the commands recieved by the communicator and refreshing the channel list
    @Override
    public void initialise() {
        logger.info("Initialising the Multiplayer Lobby");
        communicator.send("LIST");
        channelRefresh = new TimerTask() {
            @Override
            public void run() {
                communicator.send("LIST");
                logger.info("Channels refreshed");
            }
        };
        timer = new Timer();
        timer.schedule(channelRefresh, 0, 3000);
        scene.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.ESCAPE){
                if (this.timer != null) {
                    this.timer.purge();
                    this.timer.cancel();
                    this.timer = null;
                }
                communicator.send("PART");
                if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                    Multimedia.playAudio("rotate.wav");
                } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                    Multimedia.playAudio("purvi1.mp3");
                }
                gameWindow.startMenu();
            }
        });
        communicator.addListener(message -> Platform.runLater(() -> {
            handleIncomingMessages(message);
        }));
    }

    /*Method that tells the programme what to do with the incoming commands
    *(1) If the command is CHANNELS the programme updates the channel list
    *(2) If the command is JOIN the programme joins a channel
    *(3) If the command is HOST the programme sets the user as a host of the channel
    *(4) If the command is ERROR the programme send an error notification
    *(5) If the command is NICK the programme changes the nick name of the user
    *(6) If the command is START the programme starts the multiplayer game
    *(7) If the command is PARTED the programme leaves the current channel
    *(8) If the command is USERS the programme gets a list of the users currently in the channel/game
    *(9) If the command is MSG the programme send a message with a timestamp
    * */
    public void handleIncomingMessages(String message){

        String[] parts = message.split(" ", 2);
        String command = parts[0];

        //(1)
        if(command.equals("CHANNELS")){
            logger.info("Received channel list");
            if(parts.length <= 1){
                channelBox.changeChannels(new ArrayList<>());
            } else {
                String additions = parts[1];
                String[] additionsList = additions.split("\\R");
                List<String> list = Arrays.asList(additionsList);
                channelBox.changeChannels(list);
            }
        //(2)
        } else if(command.equals("JOIN")){
            String join = parts[1];
            amItheHost.set(false);
            channelBox.addChannel(join);
            channel.set(join);
            messageBox.getChildren().clear();
            var joinMessage = new Text("Welcome to the lobby");
            messageBox.getChildren().add(joinMessage);
            logger.info("Channel joined");
        //(3)
        }  else if(command.equals("HOST")){
            amItheHost.set(true);
            logger.info("You are the host");
        //(4)
        } else if(command.equals("ERROR")){
            String error = parts[1];
            Alert alert = new Alert(Alert.AlertType.ERROR, error, new javafx.scene.control.ButtonType[0]);
            alert.showAndWait();
            logger.error(error);
        //(5)
        } else if(command.equals("NICK") && parts.length > 1){
            String nickname = parts[1];
            if(!nickname.contains(":")){
                playerName.set(nickname);
            }
        //(6)
        } else if(command.equals("START")){
            startGame();
        //(7)
        } else if(command.equals("PARTED")){
            channel.set("");
            logger.info("Channel left");
        //(8)
        } else if(command.equals("USERS") && parts.length > 1){
            String user = parts[1];
            String[] users = user.split("\\R");
            List<String> usersList = Arrays.asList(users);
            this.usersList.setUsers(usersList);
            if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                Multimedia.playAudio("message.wav");
            } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                Multimedia.playAudio("vahaha.mp3");
            }
        //(9)
        } else if(command.equals("MSG")){
            String messageAddition = parts[1];
            String[] messages = messageAddition.split(":", 2);
            var messageText = new TextFlow();
            messageText.getStyleClass().add("message");
            var time = new Text("[" + this.time.format(cal.getTime()) + "] ");
            var username = new Text("<" + messages[0] + "> ");
            var text = new Text(messages[1]);
            messageText.getChildren().addAll(time, username, text);
            messageBox.getChildren().add(messageText);
            scroll.getParent().layout();
            scroll.layout();
            scroll.setVvalue(1);
            if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                Multimedia.playAudio("message.wav");
            } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                Multimedia.playAudio("vahaha.mp3");
            }
        }
    }

    //Method to join a game
    public void joinRequest(String name){
        if(channel.get().equals(name))return;
        amItheHost.set(false);
        communicator.send("JOIN " + name);
    }

    //Method that starts the game and cancels the timer refreshing the server list
    public void startGame(){
        logger.info("Starting multiplayer game");
        if(timer != null){
            timer.purge();
            timer.cancel();
            timer = null;
        }
        gameWindow.startMultiplayerGame();
    }

    //Method to handle the chat messages such as /start, /part and /nick
    public void messageVariants(String message){
        if(message.startsWith("/")){
            String[] messageParts = message.split(" ", 2);
            String command = messageParts[0].toLowerCase();
            if(command.equals("/start") && amItheHost.get()){
                communicator.send("START");
            } else if(command.equals("/part")){
                communicator.send("PART");
            } else if(command.equals("/nick") && messageParts.length > 1){
                String nick = messageParts[1];
                communicator.send("NICK " + nick);
            }
        } else {
            communicator.send("MSG " + message);
        }
    }

    //Method to build the lobby scene
    @Override
    public void build() {
        logger.info("Building the Multiplayer Lobby");

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var mlPane = new StackPane();
        mlPane.setMaxWidth(gameWindow.getWidth());
        mlPane.setMaxHeight(gameWindow.getHeight());
        mlPane.getStyleClass().add("menu-background");
        root.getChildren().add(mlPane);

        var mainPane = new BorderPane();
        mlPane.getChildren().add(mainPane);

        var title = new Text("Multiplayer");
        title.getStyleClass().add("title");
        title.setTextAlignment(TextAlignment.CENTER);
        mainPane.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        var layout = new GridPane();
        layout.setPadding(new Insets(5, 5, 5, 5));
        layout.setVgap(10);
        layout.setHgap(10);
        mainPane.setCenter(layout);

        var heading1 = new Text("Current Games");
        heading1.getStyleClass().add("heading");
        heading1.setTextAlignment(TextAlignment.CENTER);
        layout.add(heading1, 0, 0);

        channelBox = new ChannelBox();
        channelBox.getChannel().bind(channel);
        layout.add(channelBox, 0, 1);

        var roomName = new Text();
        roomName.getStyleClass().add("heading");
        roomName.setTextAlignment(TextAlignment.CENTER);
        roomName.textProperty().bind(channel);
        layout.add(roomName, 1, 0);

        var gameBox = new VBox();
        gameBox.getStyleClass().add("gameBox");
        gameBox.setPadding(new Insets(5, 5, 5, 5));
        gameBox.setSpacing(10);
        layout.add(gameBox, 1, 1);
        gameBox.visibleProperty().bind(channel.isNotEmpty());
        GridPane.setHgrow(gameBox, Priority.ALWAYS);

        usersList = new UsersList();
        gameBox.getChildren().add(usersList);

        messageBox = new VBox();
        messageBox.getStyleClass().add("messages");

        var messageField = new TextField();
        messageField.getStyleClass().add("messageBox");
        messageField.setPromptText("Write a message here");
        messageField.setOnKeyPressed(e -> {
            if(e.getCode().equals(KeyCode.ENTER)){
                messageVariants(messageField.getText());
                messageField.clear();
            }
        });
        gameBox.getChildren().add(messageField);

        scroll = new ScrollPane();
        scroll.getStyleClass().add("scroller");
        scroll.setPrefHeight(gameWindow.getHeight()/2);
        scroll.setFitToWidth(true);
        gameBox.getChildren().add(scroll);
        scroll.setContent(messageBox);

        var buttonPane = new AnchorPane();
        gameBox.getChildren().add(buttonPane);

        var startButton = new Button("Start Game");
        startButton.visibleProperty().bind(amItheHost);
        startButton.setOnMouseClicked(e -> communicator.send("START"));
        buttonPane.getChildren().add(startButton);

        var leaveButton = new Button("Leave Game");
        leaveButton.setOnMouseClicked(e -> {
            communicator.send("PART");
            communicator.send("LIST");
        });
        buttonPane.getChildren().add(leaveButton);

        AnchorPane.setLeftAnchor(startButton, Double.valueOf(0));
        AnchorPane.setRightAnchor(leaveButton, Double.valueOf(0));

    }

    //Class that creates the channel list box
    public class ChannelBox extends VBox {

        private StringProperty channel = new SimpleStringProperty();
        private HashMap<String,Text> channelsList = new HashMap<>();

        public ChannelBox(){

            setPrefWidth(150);
            setSpacing(8);
            setPadding(new Insets(8, 8, 8, 8));
            getStyleClass().add("channelList");

            var hostGame = new Text("Host New Game");
            hostGame.getStyleClass().add("channelItem");
            this.getChildren().add(hostGame);

            var newChannel = new TextField();
            newChannel.setVisible(false);
            this.getChildren().add(newChannel);

            hostGame.setOnMouseClicked(e -> {
                newChannel.setVisible(true);
                newChannel.setPromptText("Enter channel's name here");
                newChannel.requestFocus();
                if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                    Multimedia.playAudio("rotate.wav");
                } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                    Multimedia.playAudio("purvi1.mp3");
                }
            });
            newChannel.setOnKeyPressed(e -> {
                if(e.getCode().equals(KeyCode.ENTER)){
                    MultiplayerLobbyScene.this.communicator.send("CREATE " + newChannel.getText().trim());
                    MultiplayerLobbyScene.this.communicator.send("LIST");
                    newChannel.setVisible(false);
                    newChannel.clear();
                    if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                        Multimedia.playAudio("rotate.wav");
                    } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                        Multimedia.playAudio("purvi1.mp3");
                    }
                }
            });
        }

        //Method to handle the changing of the channels
        public void changeChannels(List<String> channels){
            Set<String> currentChannels = channelsList.keySet();
            if(currentChannels.size() == channels.size() && currentChannels.containsAll(channels)){
                return;
            }
            Set<String> removedChannel = new HashSet<>();
            for(String channel : currentChannels){
                if(channels.contains(channel)) continue;
                removedChannel.add(channel);
            }
            for(String removed : removedChannel){
                this.getChildren().remove(channelsList.get(removed));
            }
            channelsList.keySet().removeAll(removedChannel);
            for(String channel : channels){
                addChannel(channel);
            }
        }

        //Method to handle the joining of a channel
        public void addChannel(String name){
            if(channelsList.containsKey(name)) return;
            var newChannel = new Text(name);
            newChannel.getStyleClass().add("channelItem");
            this.getChildren().add(newChannel);
            channelsList.put(name, newChannel);
            newChannel.setOnMouseClicked(e -> {
                MultiplayerLobbyScene.this.joinRequest(name);
                logger.info("Channel joined");
            });
        }

        public StringProperty getChannel(){
            return channel;
        }
    }

    //Class that creates a list with users names
    public class UsersList extends TextFlow {

        private ArrayList<String> usersList = new ArrayList<>();

        public UsersList(){
        }

        //Method that adds users to the list
        public void setUsers(List<String> users){
            usersList.clear();
            usersList.addAll(users);
            this.getChildren().clear();
            for(String user : usersList){
                var name = new Text(user + " ");
                if(user.equals(MultiplayerLobbyScene.this.playerName.get())){
                    name.getStyleClass().add("myname");
                } else {
                    name.getStyleClass().add("playerName");
                }
                this.getChildren().add(name);
            }
        }
    }
}
