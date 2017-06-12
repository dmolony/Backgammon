package com.bytezone.network;

import java.awt.Toolkit;
import java.io.IOException;
import java.net.*;
import java.util.Optional;

import com.bytezone.backgammon.GameRequest;
import com.bytezone.backgammon.NetworkGame;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;

public class GameServer implements Runnable
{
  private static GameServer gameServer;
  private static Alert serverAlert;
  private static Alert joinerAlert;

  private static boolean isConnected = false;
  private static volatile boolean running;

  private final int serverPort;
  private final NetworkGame networkGame;
  private ServerSocket serverSocket;
  private static GameRequest serverGameRequest;

  public GameServer (NetworkGame game, int serverPort)
  {
    this.serverPort = serverPort;
    this.networkGame = game;

    serverAlert = new Alert (Alert.AlertType.INFORMATION);
    serverAlert.setTitle ("Start Network Game");
    serverAlert.setHeaderText (null);
    serverAlert.setContentText ("Waiting for opponent to join");
    ButtonType cancel = new ButtonType ("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    serverAlert.getButtonTypes ().setAll (cancel);
  }

  public static boolean startServer (NetworkGame game, int port, GameRequest gameRequest)
  {
    serverGameRequest = gameRequest;
    gameServer = new GameServer (game, port);
    Thread serverThread = new Thread (gameServer);
    serverThread.start ();

    serverAlert.showAndWait ();
    return isConnected;
  }

  @Override
  public void run ()
  {
    try
    {
      // wait for a player to join us
      serverSocket = new ServerSocket (serverPort);
      Socket clientSocket = serverSocket.accept ();         // blocks
      isConnected = true;

      GameClient gameClient =
          new GameClient (networkGame, clientSocket, serverGameRequest);
      Thread clientThread = new Thread (gameClient);
      clientThread.start ();

      close ();         // only wait for one client

      Platform.runLater ( () -> serverAlert.close ());
      Platform.runLater ( () -> networkGame.startNetworkGame (gameClient));
    }
    catch (IOException e)
    {
      if (e instanceof SocketException)
        System.out.println ("Server closing");
      else
        e.printStackTrace ();
    }
  }

  public static void closeServer ()
  {
    //    System.out.println ("quitting 1");
    if (gameServer != null)
      gameServer.close ();

    if (running)
    {
      running = false;
      if (joinerAlert != null)
        Platform.runLater ( () -> joinerAlert.close ());
    }
  }

  private void close ()
  {
    //    System.out.println ("quitting 2");

    if (serverSocket != null)
      try
      {
        serverSocket.close ();        // causes a SocketException
        serverSocket = null;
      }
      catch (IOException e)
      {
        e.printStackTrace ();
      }
  }

  public static boolean joinServer (NetworkGame game, int port, GameRequest gameRequest)
  {
    running = true;
    while (running)
    {
      try
      {
        // external servers fail immediately, local servers wait for 1 minute
        SocketAddress sockaddr = new InetSocketAddress (gameRequest.ipAddress, port);
        Socket socket = new Socket ();
        socket.connect (sockaddr, 250);            // quarter second timeout

        GameClient gameClient = new GameClient (game, socket, gameRequest);
        Thread clientThread = new Thread (gameClient);
        clientThread.start ();

        Platform.runLater ( () -> game.startNetworkGame (gameClient));
        running = false;

        return true;
      }
      catch (ConnectException | SocketTimeoutException e)
      {
        Toolkit.getDefaultToolkit ().beep ();

        if (joinerAlert == null)
          joinerAlert = getJoinAlert (gameRequest);

        Optional<ButtonType> result = joinerAlert.showAndWait ();
        if (!result.isPresent ()
            || result.get ().getButtonData () == ButtonData.CANCEL_CLOSE)
        {
          joinerAlert = null;
          return false;
        }
      }
      catch (SocketException e)
      {
        System.out.println ("Did somebody close me?");
        return false;
      }
      catch (IOException e)
      {
        e.printStackTrace ();
        return false;
      }
    }
    return false;
  }

  private static Alert getJoinAlert (GameRequest gameRequest)
  {
    Alert joinAlert = new Alert (Alert.AlertType.WARNING);

    joinAlert.setTitle ("Join Network Game");
    joinAlert.setHeaderText (null);
    joinAlert.setContentText ("No response from " + gameRequest.ipAddress);

    ButtonType retry = new ButtonType ("Retry", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancel = new ButtonType ("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    joinAlert.getButtonTypes ().setAll (retry, cancel);

    return joinAlert;
  }
}