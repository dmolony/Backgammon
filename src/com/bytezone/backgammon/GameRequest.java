package com.bytezone.backgammon;

import com.bytezone.backgammon.Game.CheckerType;
import com.bytezone.backgammon.NetworkGame.GameMode;

public class GameRequest
{
  public final GameMode gameMode;
  public final String playerName;
  public final String opponentName;
  public final String ipAddress;
  public final CheckerType playerCheckerType;
  public final CheckerType opponentCheckerType;

  public GameRequest (GameMode gameMode, String playerName, String text,
      CheckerType checkerType)
  {
    this.gameMode = gameMode;
    this.playerName = playerName;
    playerCheckerType = checkerType;
    opponentCheckerType = checkerType == Game.CheckerType.LIGHT ? Game.CheckerType.DARK
        : Game.CheckerType.LIGHT;

    if (gameMode == GameMode.SELF)
    {
      this.opponentName = text;
      this.ipAddress = "";
    }
    else if (gameMode == GameMode.CLIENT)
    {
      this.opponentName = "Opponent";
      this.ipAddress = text;
    }
    else
    {
      this.opponentName = "Opponent";
      this.ipAddress = "";
    }
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Game mode ...... %s%n", gameMode));
    text.append (String.format ("User name 1 .... %s%n", playerName));
    text.append (String.format ("User name 2 .... %s%n", opponentName));
    text.append (String.format ("IP address ..... %s%n", ipAddress));
    text.append (String.format ("Checker type ... %s", playerCheckerType));

    return text.toString ();
  }
}