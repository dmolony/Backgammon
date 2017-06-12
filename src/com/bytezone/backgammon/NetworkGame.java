package com.bytezone.backgammon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bytezone.analyse.Play;
import com.bytezone.network.ChatListener;
import com.bytezone.network.GameClient;
import com.bytezone.network.GameServer;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class NetworkGame extends Game
{
  private static final int SERVER = 0;
  private static final int CLIENT = 1;

  protected GameMode gameMode;
  protected GameClient gameClient;
  private final List<ChatListener> listeners = new ArrayList<> ();

  public enum GameMode
  {
    SELF, SERVER, CLIENT
  }

  public NetworkGame (Stage stage, GameRequest gameRequest)
  {
    super (stage, gameRequest);
    gameMode = gameRequest.gameMode;
  }

  // called from GameServer.run() when a SERVER is joined by a CLIENT
  // called from GameServer.joinServer() when a CLIENT joins a SERVER

  public void startNetworkGame (GameClient gameClient)
  {
    this.gameClient = gameClient;

    initialiseBoard ();
    gameClient.sendName (gameClient.getName ());          // both players exchange names

    if (gameMode == GameMode.SERVER)
      players[SERVER].setName (gameClient.getName ());    // set own name
    else
    {
      players[CLIENT].setName (gameClient.getName ());    // set own name

      // set checker colour
      players[CLIENT].setCheckerType (gameRequest.playerCheckerType);
      players[SERVER].setCheckerType (gameRequest.opponentCheckerType);
    }
  }

  // --------------------------------------------------------------------------------- //
  // Commands received from network opponent
  // --------------------------------------------------------------------------------- //

  public void receiveText (byte[] buffer)
  {
    if (buffer[0] == GameClient.SEND_MESSAGE)
      receiveMessage (buffer);
    else if (buffer[0] == GameClient.SEND_NAME)
      receiveName (buffer);
    else
      System.out.println ("bollocks");
  }

  private void receiveName (byte[] buffer)
  {
    assert gameMode != GameMode.SELF;
    assert buffer[0] == GameClient.SEND_NAME;

    int length = buffer[1] & 0xFF;
    String name = new String (buffer, 2, length);

    if (gameMode == GameMode.SERVER)
    {
      players[CLIENT].setName (name);
      stage.setTitle (players[SERVER].getName () + " v " + players[CLIENT].getName ());
    }
    else
    {
      players[SERVER].setName (name);
      stage.setTitle (players[CLIENT].getName () + " v " + players[SERVER].getName ());
    }

    if (gameMode == GameMode.SERVER)
    {
      //      byte[] dice = rollStartDice ();
      rollStartDice ();
      byte[] dice = player.getDiceValues ();
      if (dice[0] < dice[1])
        board.reversePerspective ();
      gameClient.sendDice (GameClient.INITIAL_ROLL, dice); // calls client's receiveDice()
    }
  }

  private void receiveMessage (byte[] buffer)
  {
    assert gameMode != GameMode.SELF;
    assert buffer[0] == GameClient.SEND_MESSAGE;

    int length = buffer[1] & 0xFF;
    String message = new String (buffer, 2, length);

    for (ChatListener chatListener : listeners)
      chatListener.displayMessage (
          gameMode == GameMode.CLIENT ? players[SERVER] : players[CLIENT], message);
  }

  public void receiveDice (byte[] buffer)
  {
    assert gameMode != GameMode.SELF;
    assert (buffer[0] == GameClient.INITIAL_ROLL && gameMode == GameMode.CLIENT)
        || (buffer[0] == GameClient.SEND_DICE && gameMode != GameMode.SELF);

    int die1 = buffer[1];
    int die2 = buffer[2];

    if (buffer[0] == GameClient.INITIAL_ROLL)               // Server -> Client
    {
      currentPlayer = (die1 > die2) ? SERVER : CLIENT;      // winner of the die roll
      assignPlayerAndOpponent ();
      status.setText (player.getName () + " wins the dice roll");

      if (die1 > die2)                      // current player is SERVER, we are CLIENT
        board.reversePerspective ();
    }

    player.setDice (die1, die2);
    board.placeCheckers (player, opponent);
    refreshDisplay ();
  }

  public void receiveMove (byte[] buffer)
  {
    assert gameMode != GameMode.SELF;
    assert buffer[0] == GameClient.SEND_MOVE;

    // process opponent's move
    int dieValue = buffer[1];
    int fromValue = buffer[2];
    int toValue = buffer[3];

    Die die = player.getCurrentDie ();
    assert dieValue == die.getValue ();

    // calculate points from/to
    Point fromPoint = board.getPoint (fromValue);
    Point toPoint = board.getPoint (toValue);
    Checker capturedChecker = null;

    // check for captured checker
    if (toPoint.size () > 0)
    {
      Checker checker = toPoint.peek ();
      if (checker.getOwner () == opponent)
      {
        capturedChecker = toPoint.pop ();           // remove it from the point
        assert toPoint.size () == 0;                // point is now empty
      }
    }

    // process move and draw resulting position
    Move move = new Move (die, fromValue, toValue, capturedChecker);
    doMove (move, fromPoint, toPoint);
  }

  public void receiveCommand (byte command)
  {
    assert gameMode != GameMode.SELF;

    switch (command)
    {
      case GameClient.FINISH_TURN:
        swapPlayers ();
        board.reversePerspective ();
        rollDiceAndDraw ();                                 // my turn now - roll dice
        gameClient.sendDice (GameClient.SEND_DICE, player.getDiceValues ());
        break;

      case GameClient.SWAP_DICE:
        undoAllMoves ();
        player.swapDice ();

        refreshDisplay ();
        break;

      case GameClient.UNDO_MOVE:
        undoMove ();                      // don't notify opponent, he just notified us
        break;

      case GameClient.QUIT:
        int opponent = gameMode == GameMode.CLIENT ? SERVER : CLIENT;
        showMessage (AlertType.INFORMATION, players[opponent].getName () + " has quit");
        break;

      default:
        System.out.println ("Unknown command: " + command);
    }
  }

  @Override
  protected boolean keystrokeUndo ()
  {
    assert gameMode != GameMode.SELF;
    boolean result = super.keystrokeUndo ();
    if (result)// && gameMode != GameMode.SELF)
      gameClient.sendCommand (GameClient.UNDO_MOVE);

    return result;
  }

  @Override
  protected void handleKey (KeyEvent e)
  {
    //    assert gameMode != GameMode.SELF;
    if (!waitingForOpponent ())
      super.handleKey (e);
  }

  @Override
  protected void finishTurn ()
  {
    //    assert gameMode != GameMode.SELF;
    //    if (gameMode == GameMode.SELF)
    //      super.finishTurn ();
    //    else
    //    {
    board.reversePerspective ();
    gameClient.sendCommand (GameClient.FINISH_TURN);    // opponent will roll dice
    //    }
  }

  public void sendMessage (String message)
  {
    //    assert gameMode != GameMode.SELF;
    //    if (gameMode != GameMode.SELF)
    gameClient.sendMessage (message);

    for (ChatListener chatListener : listeners)
      chatListener.displayMessage (
          gameMode == GameMode.CLIENT ? players[CLIENT] : players[SERVER], message);
  }

  @Override
  protected void mousePressed (MouseEvent e)
  {
    assert gameMode != GameMode.SELF;
    if (!waitingForOpponent ())
      super.mousePressed (e);
  }

  @Override
  protected void doSelection (Play play)
  {
    assert gameMode != GameMode.SELF;
    if (play != null && !waitingForOpponent ())
      super.doSelection (play);
  }

  @Override
  protected void swapDice ()
  {
    super.swapDice ();

    //    assert gameMode != GameMode.SELF;
    //    if (gameMode != GameMode.SELF)
    gameClient.sendCommand (GameClient.SWAP_DICE);
  }

  @Override
  protected Optional<Move> doPointClick (Point sourcePoint)
  {
    assert gameMode != GameMode.SELF;
    Optional<Move> opt = super.doPointClick (sourcePoint);

    if (opt.isPresent ())// && gameMode != GameMode.SELF)
      gameClient.sendMove (opt.get ());

    return opt;
  }

  @Override
  protected void undoAllMoves ()
  {
    assert gameMode != GameMode.SELF;
    while (undoMove ())
      if (!waitingForOpponent ())// && gameMode != GameMode.SELF)
        gameClient.sendCommand (GameClient.UNDO_MOVE);
  }

  protected boolean waitingForOpponent ()
  {
    assert gameMode != GameMode.SELF;
    if ((gameMode == GameMode.SERVER && currentPlayer != SERVER)
        || (gameMode == GameMode.CLIENT && currentPlayer != CLIENT))
    {
      beep (String.format ("Waiting for %s to finish", player.getName ()));
      return true;
    }

    status.setText ("");
    return false;
  }

  public void addChatListener (ChatListener listener)
  {
    assert gameMode != GameMode.SELF;
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  void close ()
  {
    assert gameMode != GameMode.SELF;
    GameServer.closeServer ();

    if (gameClient != null)// && gameMode != GameMode.SELF)
    {
      gameClient.sendCommand (GameClient.QUIT);
      gameClient.close ();
    }
  }
}