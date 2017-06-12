package com.bytezone.backgammon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Optional;
import java.util.prefs.Preferences;

import com.bytezone.backgammon.Game.CheckerType;
import com.bytezone.backgammon.NetworkGame.GameMode;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class Launcher
{
  private final BorderPane outerPane = new BorderPane ();
  private Optional<GameRequest> optRequest;
  private final Preferences prefs;

  TextField playerName = new TextField ();            // single-player player 1
  TextField opponentName = new TextField ();          // single-player player 2

  TextField serverName = new TextField ();            // start server player
  TextField ipAddressInternal = new TextField ();     // local network address
  TextField ipAddressExternal = new TextField ();     // internet address

  TextField joinerName = new TextField ();            // join server player
  TextField serverAddress = new TextField ();         // ip address to join

  Accordion accordion = new Accordion ();
  TitledPane titledPane1;
  TitledPane titledPane2;
  TitledPane titledPane3;

  ToggleGroup group1 = new ToggleGroup ();
  ToggleButton tb1g1 = new ToggleButton ("Light");
  ToggleButton tb2g1 = new ToggleButton ("Dark");

  ToggleGroup group2 = new ToggleGroup ();
  ToggleButton tb1g2 = new ToggleButton ("Light");
  ToggleButton tb2g2 = new ToggleButton ("Dark");

  ToggleGroup group3 = new ToggleGroup ();
  ToggleButton tb1g3 = new ToggleButton ("Light");
  ToggleButton tb2g3 = new ToggleButton ("Dark");

  public Launcher (Preferences prefs)
  {
    this.prefs = prefs;
    String name = prefs.get ("name", System.getProperty ("user.name"));
    serverAddress.setText (prefs.get ("ip", ""));

    // ------------- //
    // Single Player //
    // ------------- //

    GridPane grid1 = new GridPane ();
    grid1.setHgap (10);
    grid1.setVgap (10);
    grid1.setPadding (new Insets (10, 10, 10, 10));

    playerName.setText (prefs.get ("name1", "Laurel"));
    opponentName.setText (prefs.get ("name2", "Hardy"));

    tb1g1.setToggleGroup (group1);
    tb2g1.setToggleGroup (group1);

    group1.selectToggle (getToggleButton (group1, prefs.get ("chk1", "LIGHT")));

    HBox hBox1 = new HBox ();
    hBox1.getChildren ().addAll (tb1g1, tb2g1);

    grid1.add (new Label ("Player name"), 0, 0);
    grid1.add (playerName, 1, 0);
    grid1.add (hBox1, 2, 0);

    grid1.add (new Label ("Opponent name"), 0, 1);
    grid1.add (opponentName, 1, 1);

    // ------------ //
    // Start Server //
    // ------------ //

    GridPane grid2 = new GridPane ();
    grid2.setHgap (10);
    grid2.setVgap (10);
    grid2.setPadding (new Insets (10, 10, 10, 10));

    serverName.setText (prefs.get ("name3", name));

    tb1g2.setToggleGroup (group2);
    tb2g2.setToggleGroup (group2);

    group2.selectToggle (getToggleButton (group2, prefs.get ("chk3", "LIGHT")));

    HBox hBox2 = new HBox ();
    hBox2.getChildren ().addAll (tb1g2, tb2g2);
    Button getExternalAddressButton = new Button ("Check");
    getExternalAddressButton
        .setOnAction (e -> ipAddressExternal.setText (getExternalIPAddress ()));

    ipAddressInternal.setText (getInternalIPAddress ());
    ipAddressInternal.setEditable (false);
    ipAddressInternal.setFocusTraversable (false);

    ipAddressExternal.setText ("");
    ipAddressExternal.setEditable (false);
    ipAddressExternal.setFocusTraversable (false);

    grid2.add (new Label ("Player name"), 0, 0);
    grid2.add (serverName, 1, 0);
    grid2.add (hBox2, 2, 0);
    grid2.add (new Label ("Network address"), 0, 1);
    grid2.add (ipAddressInternal, 1, 1);
    grid2.add (new Label ("Internet address"), 0, 2);
    grid2.add (ipAddressExternal, 1, 2);
    grid2.add (getExternalAddressButton, 2, 2);

    // ----------- //
    // Join Server //
    // ----------- //

    GridPane grid3 = new GridPane ();
    grid3.setHgap (10);
    grid3.setVgap (10);
    grid3.setPadding (new Insets (10, 10, 10, 10));

    joinerName.setText (prefs.get ("name4", name));

    tb1g3.setToggleGroup (group3);
    tb2g3.setToggleGroup (group3);

    group3.selectToggle (getToggleButton (group3, prefs.get ("chk4", "LIGHT")));

    HBox hBox3 = new HBox ();
    hBox3.getChildren ().addAll (tb1g3, tb2g3);

    grid3.add (new Label ("Player name"), 0, 0);
    grid3.add (joinerName, 1, 0);
    grid3.add (hBox3, 2, 0);
    grid3.add (new Label ("Server address"), 0, 1);
    grid3.add (serverAddress, 1, 1);

    // --------------- //
    // Put it together //
    // --------------- //

    titledPane1 = new TitledPane ("Single player game", grid1);
    titledPane2 = new TitledPane ("Start network game", grid2);
    titledPane3 = new TitledPane ("Join network game", grid3);

    accordion.getPanes ().addAll (titledPane1, titledPane2, titledPane3);

    int openPane = prefs.getInt ("pane", 1);
    accordion.setExpandedPane (
        openPane == 1 ? titledPane1 : openPane == 2 ? titledPane2 : titledPane3);

    outerPane.setCenter (accordion);

    outerPane.setPrefWidth (450);
    outerPane.setPrefHeight (225);
  }

  public void show ()
  {
    Dialog<GameRequest> dialog = new Dialog<> ();
    dialog.setTitle ("Start Game");
    dialog.setHeaderText (null);
    dialog.getDialogPane ().getButtonTypes ().addAll (ButtonType.OK, ButtonType.CANCEL);
    dialog.getDialogPane ().setContent (outerPane);

    dialog.setResultConverter (dialogButton ->
    {
      if (dialogButton == ButtonType.OK)
        switch (accordion.getExpandedPane ().getText ())
        {
          case "Single player game":
            return new GameRequest (GameMode.SELF, playerName.getText (),
                opponentName.getText (), group1.getSelectedToggle () == tb1g1
                    ? CheckerType.LIGHT : CheckerType.DARK);
          case "Start network game":
            return new GameRequest (GameMode.SERVER, serverName.getText (), null,
                group2.getSelectedToggle () == tb1g2 ? CheckerType.LIGHT
                    : CheckerType.DARK);
          case "Join network game":
            return new GameRequest (GameMode.CLIENT, joinerName.getText (),
                serverAddress.getText (), group3.getSelectedToggle () == tb1g3
                    ? CheckerType.LIGHT : CheckerType.DARK);
        }

      return null;
    });

    optRequest = dialog.showAndWait ();
    if (optRequest.isPresent ())
    {
      prefs.put ("name1", playerName.getText ());
      prefs.put ("name2", opponentName.getText ());
      prefs.put ("name3", serverName.getText ());
      prefs.put ("name4", joinerName.getText ());
      prefs.put ("ip", serverAddress.getText ());
      prefs.put ("chk1", getToggle (group1, tb1g1));
      prefs.put ("chk3", getToggle (group2, tb1g2));
      prefs.put ("chk4", getToggle (group3, tb1g3));
      prefs.putInt ("pane", getOpenPane ());
    }
  }

  public Optional<GameRequest> getResult ()
  {
    return optRequest;
  }

  private int getOpenPane ()
  {
    TitledPane tp = accordion.getExpandedPane ();
    return tp == titledPane1 ? 1 : tp == titledPane2 ? 2 : 3;
  }

  private String getToggle (ToggleGroup tg, ToggleButton tb)
  {
    return tg.getSelectedToggle () == tb ? "LIGHT" : "DARK";
  }

  private ToggleButton getToggleButton (ToggleGroup tg, String value)
  {
    if (value.equals ("LIGHT"))
      return (ToggleButton) tg.getToggles ().get (0);

    return (ToggleButton) tg.getToggles ().get (1);
  }

  private String getInternalIPAddress ()
  {
    try
    {
      Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces ();
      while (e.hasMoreElements ())
      {
        NetworkInterface n = e.nextElement ();
        Enumeration<InetAddress> ee = n.getInetAddresses ();
        while (ee.hasMoreElements ())
        {
          InetAddress i = ee.nextElement ();
          String address = i.getHostAddress ();
          if (address.startsWith ("192"))
            return address;
        }
      }
    }
    catch (SocketException e1)
    {
      e1.printStackTrace ();
    }
    return "";
  }

  // this should run in a separate thread
  private String getExternalIPAddress ()
  {
    try
    {
      URL whatismyip = new URL ("http://checkip.amazonaws.com");
      BufferedReader in =
          new BufferedReader (new InputStreamReader (whatismyip.openStream ()));

      String externalIP = in.readLine ();
      in.close ();
      return externalIP;
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
    return "not found";
  }
}