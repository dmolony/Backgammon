package com.bytezone.backgammon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.bytezone.analyse.Evaluation;
import com.bytezone.analyse.Play;
import com.bytezone.analyse.State;
import com.bytezone.backgammon.Game.CheckerType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class BackgammonPlayer implements Player
{
  private static final Color[] colors = { Color.ANTIQUEWHITE, Color.BURLYWOOD };

  private String name;
  private Color color;
  private CheckerType checkerType;
  private final List<Checker> checkers = new ArrayList<> ();
  private final List<Die> dice = new ArrayList<> ();
  private int currentDie;

  private final Evaluation evaluation = new Evaluation ();
  private final ObservableList<Play> plays = FXCollections.observableArrayList ();
  private int maxDice;

  public BackgammonPlayer (String name, CheckerType checkerType)
  {
    this.name = name;
    this.checkerType = checkerType;

    // create four dice, we only use two except when doubles are rolled
    for (int i = 0; i < 4; i++)
      dice.add (new BackgammonDie (this));

    setCheckerType (checkerType);
  }

  @Override
  public void setCheckerType (CheckerType checkerType)
  {
    this.checkerType = checkerType;
    color = colors[checkerType == CheckerType.DARK ? 1 : 0];

    for (Die die : dice)
      ((BackgammonDie) die).setColor (color);
  }

  @Override
  public Color getColor ()
  {
    return color;
  }

  @Override
  public List<Die> getDice ()
  {
    return dice;
  }

  @Override
  public byte[] getDiceValues ()
  {
    byte[] diceValues = new byte[2];

    diceValues[0] = (byte) dice.get (0).getValue ();
    diceValues[1] = (byte) dice.get (1).getValue ();

    return diceValues;
  }

  @Override
  public Die getCurrentDie ()
  {
    return dice.get (currentDie);
  }

  @Override
  public void clear ()
  {
    checkers.clear ();
  }

  @Override
  public void clearCheckerDisplay ()
  {
    for (Checker checker : checkers)
      checker.clearMoves ();
  }

  @Override
  public void rollDice ()
  {
    int d1 = dice.get (0).roll ();
    int d2 = dice.get (1).roll ();

    if (d1 == d2)         // doubles
    {
      dice.get (2).setValue (d1);
      dice.get (3).setValue (d1);
    }

    currentDie = 0;
    highlightCurrentDie ();
  }

  @Override
  public void setDice (int value1, int value2)
  {
    dice.get (0).setValue (value1);
    dice.get (1).setValue (value2);

    if (value1 == value2)       // doubles
    {
      dice.get (2).setValue (value1);
      dice.get (3).setValue (value1);
    }

    currentDie = 0;
    highlightCurrentDie ();
  }

  @Override
  public void analyse (Board board, State state)
  {
    optimiseDice (board);
    plays.setAll (evaluation.getPlays (getDiceValues (), state));

    maxDice = evaluation.getMaxDice ();

    for (int i = 3; i >= maxDice; i--)
      dice.get (i).setUnusable (true);
  }

  @Override
  public ObservableList<Play> getPlays ()
  {
    return plays;
  }

  @Override
  public boolean playFound (State state)
  {
    //    State state = new State (board, player, opponent);
    if (plays.size () == 0)
      return true;

    for (Play play : plays)
      if (play.matches (state))
        return true;

    return false;
  }

  private boolean optimiseDice (Board board)
  {
    if (isDouble ())                                  // can't optimise doubles
      return false;

    byte[] dice = getDiceValues ();

    if (board.getBar (this).size () > 0)              // something on the bar
    {
      if (canMove (board, 25 - dice[0]))              // first die can be played
        return false;

      if (canMove (board, 25 - dice[1]))              // second die can be played
      {
        swapDice ();
        return true;
      }
      return false;
    }

    //    System.out.printf ("Can use %d: %s%n", dice[0], canUseDie (board, dice[0]));
    //    System.out.printf ("Can use %d: %s%n", dice[1], canUseDie (board, dice[1]));
    //    System.out.println ();

    // nothing on the bar
    if (!canUseDie (board, dice[0]) && canUseDie (board, dice[1]))
    {
      swapDice ();
      return true;
    }

    return false;
  }

  private boolean canUseDie (Board board, int dieValue)
  {
    for (Checker checker : checkers)
    {
      int checkerValue = checker.getValue ();
      int target = checkerValue - dieValue;
      if (checkerValue <= 0)                    // checker is already off
        continue;
      if (target <= 0 && !canBearOff ())        // cannot bear off
        continue;
      if (canMove (board, target))
        return true;
    }
    return false;
  }

  private boolean canMove (Board board, int pointPosition)
  {
    Point target = board.getPoint (pointPosition);
    if (target.size () <= 1)                    // can move to empty point or single
      return true;
    Player owner = target.peek ().getOwner ();
    if (owner == this)                          // can move to our own point
      return true;
    return false;
  }

  @Override
  public void swapDice ()
  {
    // can only swap if dice are different and we haven't made a move yet
    if (isDouble () || currentDie > 0)
      return;

    Die die0 = dice.get (0);
    Die die1 = dice.get (1);

    int temp1 = die0.getValue ();
    boolean temp2 = die0.getUnusable ();
    Move temp3 = die0.getMove ();

    die0.setValue (die1.getValue ());
    die0.setUnusable (die1.getUnusable ());
    die0.setMove (die1.getMove ());

    die1.setValue (temp1);
    die1.setUnusable (temp2);
    die1.setMove (temp3);

    // not sure why this doesn't redraw the new dice
    //    Die tempDie = dice.get (0);
    //    dice.set (0, dice.get (1));
    //    dice.set (1, tempDie);
  }

  @Override
  public void drawDice (GraphicsContext gc)
  {
    if (isDouble ())
    {
      dice.get (0).drawAlternate (gc);
      dice.get (1).drawAlternate (gc);
      dice.get (2).draw (gc);
      dice.get (3).draw (gc);
    }
    else
    {
      dice.get (0).draw (gc);
      dice.get (1).draw (gc);
    }
  }

  @Override
  public void add (Checker checker)
  {
    checkers.add (checker);
  }

  @Override
  public boolean canBearOff ()
  {
    for (Checker checker : checkers)
      if (checker.getValue () > 6)
        return false;

    return true;
  }

  @Override
  public boolean isDouble ()
  {
    return dice.get (0).getValue () == dice.get (1).getValue ();
  }

  @Override
  public void doMove ()
  {
    ++currentDie;
    highlightCurrentDie ();
  }

  @Override
  public Optional<Move> undoMove ()
  {
    if (currentDie == 0)                  // no moves to undo
      return Optional.empty ();

    Move undoMove = dice.get (--currentDie).getMove ();
    highlightCurrentDie ();

    return Optional.of (undoMove);
  }

  @Override
  public Optional<Move> redoMove ()
  {
    if (currentDie == 2 && !isDouble ())
      return Optional.empty ();
    if (currentDie == 4 && isDouble ())
      return Optional.empty ();
    Move redoMove = dice.get (currentDie).getMove ();
    if (redoMove == null)
      return Optional.empty ();
    return Optional.of (redoMove);
  }

  @Override
  public boolean moreMoves ()
  {
    return currentDie < maxDice;
  }

  private void highlightCurrentDie ()
  {
    int count = 0;
    for (Die die : dice)
      die.setHighlighted (count++ == currentDie);
  }

  @Override
  public int getHighestPoint ()
  {
    Collections.sort (checkers);
    return checkers.get (checkers.size () - 1).getValue ();
  }

  @Override
  public int getScore ()
  {
    int score = 0;

    for (Checker checker : checkers)
      score += checker.getValue ();

    return score;
  }

  @Override
  public Iterator<Checker> iterator ()
  {
    return checkers.iterator ();
  }

  @Override
  public void setName (String name)
  {
    this.name = name;
  }

  @Override
  public String getName ()
  {
    return name;
  }

  @Override
  public String toString ()
  {
    return String.format ("Player name=%s, Color=%s", name, checkerType);
  }
}