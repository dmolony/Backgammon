package com.bytezone.backgammon;

import java.awt.Toolkit;
import java.util.Optional;

import com.bytezone.analyse.Play;
import com.bytezone.analyse.State;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Game
{
  private static final boolean DO_BEEP = true;
  private static final boolean DONT_BEEP = false;

  protected final Stage stage;
  protected final BackgammonBoard board;
  private final HBox statusBar = new HBox (10);
  protected final Label status = new Label ();

  protected final Player[] players = new Player[2];
  protected int currentPlayer;        // 0 or 1
  protected Player player;
  protected Player opponent;
  protected final GameRequest gameRequest;

  private final ListView<Play> listView = new ListView<> ();

  enum CheckerType
  {
    LIGHT, DARK
  }

  // --------------------------------------------------------------------------------- //
  // Constructor
  // --------------------------------------------------------------------------------- //

  public Game (Stage stage, GameRequest gameRequest)
  {
    this.stage = stage;
    this.gameRequest = gameRequest;

    players[0] =
        new BackgammonPlayer (gameRequest.playerName, gameRequest.playerCheckerType);
    players[1] =
        new BackgammonPlayer (gameRequest.opponentName, gameRequest.opponentCheckerType);

    currentPlayer = 0;
    assignPlayerAndOpponent ();

    board = new BackgammonBoard (player, opponent);

    if (false)
      board.focusedProperty ().addListener ( (arg, oldVal, newVal) -> System.out
          .printf ("Board %s focus%n", (newVal ? "in" : "out of")));

    statusBar.getChildren ().add (status);
    statusBar.setPadding (new Insets (5));

    listView.getSelectionModel ().selectedItemProperty ()
        .addListener ( (observable, oldValue, newValue) -> doSelection (newValue));
  }

  // --------------------------------------------------------------------------------- //
  // Initialisation
  // --------------------------------------------------------------------------------- //

  void startGame ()
  {
    initialiseBoard ();
    rollStartDice ();
  }

  protected void initialiseBoard ()
  {
    int[] values = new int[] { 6, 8, 13, 24 };          // points with checkers
    int[] totals = new int[] { 5, 3, 5, 2 };            // how many checkers

    addCheckers (player, values, totals);
    addCheckers (opponent, values, totals);
  }

  private void addCheckers (Player player, int[] values, int[] totals)
  {
    player.clear ();

    // create checkers and assign them to the player
    for (int i = 0; i < values.length; i++)
      for (int j = 0; j < totals[i]; j++)
        player.add (new BackgammonChecker (player, values[i]));
  }

  protected void rollStartDice ()
  {
    do
      player.rollDice ();
    while (player.isDouble ());

    byte[] dice = player.getDiceValues ();

    currentPlayer = (dice[0] > dice[1]) ? 0 : 1;
    assignPlayerAndOpponent ();
    status.setText (player.getName () + " wins the dice roll");

    player.setDice (dice[0], dice[1]);
    board.placeCheckers (player, opponent);
    refreshDisplay ();
  }

  // --------------------------------------------------------------------------------- //
  // Keystrokes
  // --------------------------------------------------------------------------------- //

  void keyTyped (KeyEvent e)
  {
    status.setText ("");

    if (e.isMetaDown ())
      return;

    e.consume ();

    if (!board.keyHandled (e))
      handleKey (e);
  }

  protected void handleKey (KeyEvent e)
  {
    switch (e.getCharacter ())
    {
      case " ":
        keystrokeFinishTurn ();
        return;

      case "x":
        keystrokeSwapDice ();
        return;

      case "u":
        keystrokeUndo ();
        return;

      case "r":
        keystrokeRedo ();
        return;
    }
  }

  protected boolean keystrokeUndo ()
  {
    if (undoMove ())
      return true;

    beep ("Nothing to undo");
    return false;
  }

  protected boolean keystrokeRedo ()
  {
    if (redoMove ())
      return true;

    beep ("Nothing to redo");
    return false;
  }

  private void keystrokeFinishTurn ()
  {
    if (!canFinish ())
      return;

    swapPlayers ();         // clears the checker display
    finishTurn ();
  }

  protected void finishTurn ()
  {
    rollDiceAndDraw ();
  }

  private boolean canFinish ()
  {
    if (player.moreMoves ())
    {
      beep ("Unfinished move");
      return false;
    }

    if (!player.playFound (new State (board, player, opponent)))
    {
      beep ("Not a legal move");
      return false;
    }

    return true;
  }

  protected void keystrokeSwapDice ()
  {
    if (player.isDouble ())
    {
      beep ("Cannot swap doubles");
      return;
    }

    Bar bar = board.getBar (player);
    if (bar.size () > 0)
    {
      byte[] dice = player.getDiceValues ();
      if (!canMoveFrom (bar, dice[1]))
      {
        beep ("Second die cannot be played from the bar");
        return;
      }
    }

    if (player.getDice ().get (1).getUnusable ())
    {
      beep ("Second die cannot be used");
      return;
    }

    undoAllMoves ();
    swapDice ();

    refreshDisplay ();
  }

  protected void swapDice ()
  {
    player.swapDice ();
  }

  // --------------------------------------------------------------------------------- //
  // Mouse clicks
  // --------------------------------------------------------------------------------- //

  protected void mousePressed (MouseEvent e)
  {
    board.requestFocus ();

    if (!player.moreMoves ())
    {
      beep ("No moves left (press space to finish turn, or U to undo the last move)");
      return;
    }

    Bar bar1 = board.getBar (player);
    Bar bar2 = board.getBar (opponent);
    Optional<Move> optMove = null;

    if (bar1.size () > 0)
      if (barClicked (e, bar1, bar2))
        optMove = doPointClick (bar1);
      else
      {
        beep ("Must play from the bar");
        return;
      }
    else
    {
      Optional<Point> optionalPoint = board.getPoint (e.getX (), e.getY ());
      if (optionalPoint.isPresent ())
      {
        Point sourcePoint = optionalPoint.get ();

        if (e.isShiftDown () && !player.isDouble ())
        {
          // if move is blocked, check if reversing the dice will allow it
          Die die = player.getCurrentDie ();
          byte[] dice = player.getDiceValues ();
          if (die.getValue () == dice[0]                    // still on first move
              && !canMoveFrom (sourcePoint, dice[0])        // first die is blocked
              && canMoveFrom (sourcePoint, dice[1])         // second die is not blocked
              && canMoveFrom (sourcePoint, dice[0] + dice[1]))    // can use both dice
            swapDice ();
        }
        optMove = doPointClick (sourcePoint);
      }
      else
      {
        if (barClicked (e, bar1, bar2))
          beep ("Nothing on the bar");
        else
          beep ("No point clicked");
        return;
      }
    }

    // see whether the user wants to apply the second die
    if (optMove.isPresent () && e.isShiftDown () && player.moreMoves ())
      doPointClick (optMove.get ().getValueTo ());
  }

  private boolean barClicked (MouseEvent e, Bar... bars)
  {
    for (Bar bar : bars)
      if (bar.contains (e.getX (), e.getY ()))
        return true;
    return false;
  }

  private Optional<Move> doPointClick (int sourcePointValue)
  {
    return doPointClick (board.getPoint (sourcePointValue));
  }

  protected Optional<Move> doPointClick (Point sourcePoint)
  {
    Optional<Checker> optChecker = getChecker (sourcePoint, DO_BEEP);
    if (!optChecker.isPresent ())
      return Optional.empty ();

    Die die = player.getCurrentDie ();
    Point targetPoint = null;
    Checker capturedChecker = null;

    Checker checker = optChecker.get ();
    int sourceValue = checker.getValue ();
    int targetValue = sourceValue - die.getValue ();

    if (targetValue <= 0)                   // bearing off
    {
      if (!player.canBearOff ())
      {
        beep ("Cannot bear off");
        return Optional.empty ();
      }

      if (die.getValue () > sourceValue && player.getHighestPoint () > sourceValue)
      {
        beep ("Cannot bear off a non-matching point when a larger "
            + "point still has a checker");
        return Optional.empty ();
      }

      targetValue = 0;
      targetPoint = board.getHome (player);
    }
    else
    {
      targetPoint = board.getPoint (targetValue);

      if (targetPoint.size () > 0 && targetPoint.peek ().getOwner () != player)
      {
        if (targetPoint.size () > 1)
        {
          beep ("Blocked by opponent");
          return Optional.empty ();
        }

        status.setText ("Capture!");
        capturedChecker = targetPoint.pop ();
      }
    }

    Move move = new Move (die, checker.getValue (), targetValue, capturedChecker);
    doMove (move, sourcePoint, targetPoint);

    return Optional.of (move);
  }

  // --------------------------------------------------------------------------------- //
  // Moves
  // --------------------------------------------------------------------------------- //

  protected void doMove (Move move, Point sourcePoint, Point targetPoint)
  {
    board.draw (move);

    if (move.capturedChecker != null)
    {
      move.capturedChecker.setValue (BackgammonBoard.BAR_VALUE);
      board.getBar (opponent).push (move.capturedChecker);
    }

    player.doMove ();                             // increments player.currentDie

    Checker checker = sourcePoint.pop ();         // remove from point
    checker.pushMove (move);
    checker.setValue (move.getValueTo ());
    targetPoint.push (checker);

    board.draw ();

    if (player.getScore () == 0)
      showMessage (AlertType.INFORMATION, player.getName () + " wins!");
  }

  protected boolean redoMove ()
  {
    Optional<Move> optMove = player.redoMove ();    // gets it from the current die

    if (!optMove.isPresent ())                      // nothing to redo - ignore command
      return false;

    doPointClick (optMove.get ().getValueFrom ());

    return true;
  }

  protected boolean undoMove ()
  {
    Optional<Move> optMove = player.undoMove ();    // gets it from the current -1 die

    if (!optMove.isPresent ())                      // nothing to undo - ignore command
      return false;

    Move undoMove = optMove.get ();
    int valueTo = undoMove.getValueTo ();
    int valueFrom = undoMove.getValueFrom ();

    Point pointTo = board.getPoint (valueTo);
    Point pointFrom = board.getPoint (valueFrom);

    Checker checker = pointTo.pop ();

    Move move = checker.popMove ();                 // checker also has a copy 
    assert move == undoMove;                        // verify it's the same move

    checker.setValue (valueFrom);
    pointFrom.push (checker);

    if (undoMove.capturedChecker != null)
    {
      checker = board.getBar (opponent).pop ();     // remove it from the bar
      assert checker == undoMove.capturedChecker;   // verify it's the same checker

      checker.setValue (25 - valueTo);              // has to be in opponent's value
      pointTo.push (checker);
    }

    board.draw ();

    return true;
  }

  protected void undoAllMoves ()
  {
    while (undoMove ())
      ;
  }

  // --------------------------------------------------------------------------------- //
  // Utility routines
  // --------------------------------------------------------------------------------- //

  protected void rollDiceAndDraw ()
  {
    player.rollDice ();
    board.placeCheckers (player, opponent);
    refreshDisplay ();
  }

  protected void refreshDisplay ()
  {
    player.analyse (board, new State (board, player, opponent));
    listView.setItems (player.getPlays ());
    listView.getSelectionModel ().clearSelection ();
    board.draw ();
  }

  protected void swapPlayers ()
  {
    player.clearCheckerDisplay ();          // remove dice numbers
    currentPlayer = 1 - currentPlayer;
    assignPlayerAndOpponent ();
    status.setText (player.getName () + "'s turn");
  }

  protected void assignPlayerAndOpponent ()
  {
    player = players[currentPlayer];
    opponent = players[1 - currentPlayer];
  }

  protected void beep (String message)
  {
    status.setText (message);
    Toolkit.getDefaultToolkit ().beep ();
  }

  BackgammonBoard getBoard ()
  {
    return board;
  }

  ListView<Play> getMovesList ()
  {
    return listView;
  }

  HBox getStatusBar ()
  {
    return statusBar;
  }

  protected void showMessage (AlertType messageType, String message)
  {
    Alert alert = new Alert (messageType, message, ButtonType.OK);
    alert.setHeaderText (null);             // remove stupid text
    alert.showAndWait ();
  }

  // listener for the play list
  protected void doSelection (Play play)
  {
    if (play == null)
      return;

    undoAllMoves ();

    // make sure the dice are in the correct order
    int firstDieValue = player.getCurrentDie ().getValue ();
    if (!play.firstDieMatches (firstDieValue))
      swapDice ();

    // make each move
    for (com.bytezone.analyse.Move move : play)
      doPointClick (move.pointFrom);
  }

  private Optional<Checker> getChecker (Point sourcePoint, boolean beep)
  {
    if (sourcePoint.size () == 0)
    {
      if (beep)
        beep ("That point is empty");
      return Optional.empty ();
    }

    Checker checker = sourcePoint.peek ();

    if (checker.getOwner () != player)
    {
      if (beep)
        beep ("Cannot play opponent's pieces");
      return Optional.empty ();
    }

    return Optional.of (checker);
  }

  private boolean canMoveFrom (Point sourcePoint, int dieValue)
  {
    Optional<Checker> optChecker = getChecker (sourcePoint, DONT_BEEP);

    if (optChecker.isPresent ())
      return canMoveTo (optChecker.get ().getValue () - dieValue);

    return false;
  }

  private boolean canMoveTo (int pointPosition)
  {
    Point target = board.getPoint (pointPosition);
    if (target.size () <= 1)                    // can move to empty point or single
      return true;

    Player owner = target.peek ().getOwner ();  // 2 or more so check the owner
    if (owner == player)                        // can move to our own point
      return true;

    return false;
  }

  void setState (int id)
  {
    int[] values, totals;
    currentPlayer = 0;
    assignPlayerAndOpponent ();

    switch (id)
    {
      case 1:
        values = new int[] { 24, 6, 5 };
        totals = new int[] { 1, 1, 1 };
        addCheckers (player, values, totals);
        values = new int[] { 4, 6, };
        totals = new int[] { 2, 2, };
        addCheckers (opponent, values, totals);
        player.setDice (5, 3);
        break;

      case 2:
        values = new int[] { 24, 6, 5 };
        totals = new int[] { 1, 1, 1 };
        addCheckers (player, values, totals);
        values = new int[] { 4, 6, 24 };
        totals = new int[] { 2, 2, 2 };
        addCheckers (opponent, values, totals);
        player.setDice (5, 3);
        break;

      case 3:
        values = new int[] { 12, 6 };
        totals = new int[] { 1, 1 };
        addCheckers (player, values, totals);
        values = new int[] { 17 };
        totals = new int[] { 2 };
        addCheckers (opponent, values, totals);
        player.setDice (4, 3);
        break;

      case 4:
        values = new int[] { 6, 4, 3, 0 };
        totals = new int[] { 1, 1, 1, 12 };
        addCheckers (player, values, totals);
        values = new int[] { 17, 0 };
        totals = new int[] { 2, 13 };
        addCheckers (opponent, values, totals);
        player.setDice (5, 3);

      default:
        break;
    }

    board.placeCheckers (player, opponent);
    refreshDisplay ();
  }
}