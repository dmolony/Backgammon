package com.bytezone.network;

import com.bytezone.backgammon.NetworkGame;
import com.bytezone.backgammon.Player;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class ChatPane extends BorderPane implements ChatListener
{
  private final TextArea displayField = new TextArea ();
  private final TextField inputField = new TextField ();
  private final NetworkGame game;

  public ChatPane (NetworkGame game)
  {
    this.game = game;
    displayField.setEditable (false);
    setCenter (displayField);
    setBottom (inputField);

    inputField.setOnAction (e ->
    {
      game.sendMessage (inputField.getText ());
      inputField.setText ("");
    });

    if (false)
      inputField.focusedProperty ().addListener ( (arg, oldVal, newVal) -> System.out
          .printf ("Input field %s focus%n", (newVal ? "in" : "out of")));
  }

  @Override
  public void displayMessage (Player player, String message)
  {
    displayField.appendText (player.getName ());
    displayField.appendText (" says:\n");
    displayField.appendText (message);
    displayField.appendText ("\n\n");
  }
}