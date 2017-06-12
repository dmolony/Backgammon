package com.bytezone.backgammon;

import java.util.Optional;
import java.util.prefs.Preferences;

import com.bytezone.analyse.Play;
import com.bytezone.backgammon.NetworkGame.GameMode;
import com.bytezone.network.ChatPane;
import com.bytezone.network.GameServer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Backgammon extends Application
{
  private static final int PORT = 27984;
  private static final int LIST_WIDTH = 225;
  private static final int CHAT_WIDTH = 225;
  private static final int BOARD_WIDTH = 950;
  private static final int BOARD_HEIGHT = 800;
  private static final String MOVE_PREF = "showmoves";
  private static final String CHAT_PREF = "showchat";

  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private final MenuBar menuBar = new MenuBar ();

  private final Menu fileMenu = new Menu ("File");
  private final Menu optionMenu = new Menu ("Options");

  private final MenuItem newLocalGame = new MenuItem ("New game");
  private final MenuItem toggleMoves = new MenuItem ("Show moves");
  private final MenuItem toggleChat = new MenuItem ("Show chat");

  private final int maxTestItems = 4;

  private Game game;
  private boolean movesShowing, chatShowing;
  private final BorderPane borderPane = new BorderPane ();
  private Stage primaryStage;
  private ChatPane chatPane;

  @Override
  public void start (Stage primaryStage) throws Exception
  {
    Launcher launcher = new Launcher (prefs);

    launcher.show ();
    Optional<GameRequest> opt = launcher.getResult ();
    if (!opt.isPresent ())
    {
      exit ();
      return;
    }

    GameRequest gameRequest = opt.get ();
    if (gameRequest.gameMode == GameMode.SELF)
      game = new Game (primaryStage, gameRequest);
    else
      game = new NetworkGame (primaryStage, gameRequest);

    this.primaryStage = primaryStage;

    Pane wrapperPane = new Pane ();
    borderPane.setCenter (wrapperPane);

    BackgammonBoard board = game.getBoard ();
    wrapperPane.getChildren ().add (board);

    Scene scene = new Scene (borderPane);
    primaryStage.setScene (scene);

    final String version = System.getProperty ("java.version");
    primaryStage.setTitle ("Backgammon - Java " + version);

    final String os = System.getProperty ("os.name");
    if (os != null && os.startsWith ("Mac"))
      menuBar.setUseSystemMenuBar (true);

    // menus
    borderPane.setTop (menuBar);
    menuBar.getMenus ().addAll (fileMenu, optionMenu);
    fileMenu.getItems ().addAll (newLocalGame);
    optionMenu.getItems ().addAll (toggleMoves, toggleChat, new SeparatorMenuItem ());

    if (gameRequest.gameMode == GameMode.SELF)
      for (int i = 1; i <= maxTestItems; i++)
      {
        MenuItem item = new MenuItem ("Test " + i);
        optionMenu.getItems ().add (item);
        item.setAccelerator (KeyCombination.keyCombination ("SHORTCUT+" + i));
        final int x = i;
        item.setOnAction (e -> game.setState (x));
      }

    // accelerators
    newLocalGame.setAccelerator (KeyCombination.keyCombination ("SHORTCUT+N"));
    toggleMoves.setAccelerator (KeyCombination.keyCombination ("SHORTCUT+M"));
    toggleChat.setAccelerator (KeyCombination.keyCombination ("SHORTCUT+T"));

    // actions
    //    newLocalGame.setOnAction (e -> game.startLocalGame ());
    newLocalGame.setDisable (true);
    toggleMoves.setOnAction (e -> toggleMoves ());
    toggleChat.setOnAction (e -> toggleChat ());

    // exit action
    primaryStage.setOnCloseRequest (e -> exit ());

    // bind keys and mouse
    scene.setOnKeyTyped (e -> game.keyTyped (e));
    board.setOnMouseClicked (e -> game.mousePressed (e));

    // moves pane
    ListView<Play> listView = game.getMovesList ();
    listView.setPrefWidth (LIST_WIDTH);
    listView.setStyle ("-fx-font-family: monospaced;-fx-font-size: 9pt;");

    if (false)
      listView.focusedProperty ().addListener ( (arg, oldVal, newVal) -> System.out
          .printf ("ListView %s focus%n", (newVal ? "in" : "out of")));

    // chat pane

    if (gameRequest.gameMode != GameMode.SELF)
    {
      chatPane = new ChatPane ((NetworkGame) game);
      chatPane.setPrefWidth (CHAT_WIDTH);
      ((NetworkGame) game).addChatListener (chatPane);

      if (prefs.getBoolean (CHAT_PREF, false))
      {
        toggleChat ();
        borderPane.setRight (chatPane);
      }
    }

    if (prefs.getBoolean (MOVE_PREF, false))
    {
      toggleMoves ();
      borderPane.setLeft (listView);
    }

    // status bar
    borderPane.setBottom (game.getStatusBar ());

    // size screen
    double screenHeight = Screen.getPrimary ().getVisualBounds ().getHeight ();
    double screenWidth = Screen.getPrimary ().getVisualBounds ().getWidth ();

    double width = BOARD_WIDTH + (movesShowing ? LIST_WIDTH : 0);
    primaryStage.setWidth (screenWidth < width ? screenWidth - 50 : width - 50);
    primaryStage
        .setHeight (screenHeight < BOARD_HEIGHT ? screenHeight - 50 : BOARD_HEIGHT - 50);
    primaryStage.centerOnScreen ();

    // ensure board redraws when resized
    board.widthProperty ().bind (wrapperPane.widthProperty ());
    board.heightProperty ().bind (wrapperPane.heightProperty ());

    board.requestFocus ();
    primaryStage.show ();

    if (gameRequest.gameMode == GameMode.SELF)
      game.startGame ();
    else
    {
      boolean result = false;
      if (gameRequest.gameMode == GameMode.SERVER)
        result = GameServer.startServer ((NetworkGame) game, PORT, gameRequest);
      else
        result = GameServer.joinServer ((NetworkGame) game, PORT, gameRequest);
      if (!result)
        exit ();
    }
  }

  private void toggleMoves ()
  {
    double windowWidth = primaryStage.getWidth ();
    double windowX = primaryStage.getX ();

    if (movesShowing)
    {
      borderPane.setLeft (null);                          // hide pane
      primaryStage.setWidth (windowWidth - LIST_WIDTH);
      primaryStage.setX (windowX + LIST_WIDTH);
      toggleMoves.setText ("Show moves");
    }
    else
    {
      borderPane.setLeft (game.getMovesList ());          // show pane
      primaryStage.setWidth (windowWidth + LIST_WIDTH);
      primaryStage.setX (windowX - LIST_WIDTH);
      toggleMoves.setText ("Hide moves");
    }

    movesShowing = !movesShowing;
  }

  private void toggleChat ()
  {
    double windowWidth = primaryStage.getWidth ();
    //    double windowX = primaryStage.getX ();

    if (chatShowing)
    {
      borderPane.setRight (null);                       // hide pane
      primaryStage.setWidth (windowWidth - CHAT_WIDTH);
      toggleChat.setText ("Show chat");
    }
    else
    {
      borderPane.setRight (chatPane);                   // show pane
      primaryStage.setWidth (windowWidth + CHAT_WIDTH);
      toggleChat.setText ("Hide chat");
    }

    chatShowing = !chatShowing;
  }

  private void exit ()
  {
    prefs.putBoolean (MOVE_PREF, movesShowing);
    prefs.putBoolean (CHAT_PREF, chatShowing);

    if (game != null && game instanceof NetworkGame)
      ((NetworkGame) game).close ();

    Platform.exit ();
  }

  public static void main (String[] args)
  {
    Application.launch (args);
  }
}