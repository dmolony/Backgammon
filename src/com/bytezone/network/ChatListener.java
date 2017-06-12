package com.bytezone.network;

import com.bytezone.backgammon.Player;

public interface ChatListener
{
  public void displayMessage (Player player, String message);
}