package com.bytezone.backgammon;

import java.util.List;
import java.util.Optional;

import com.bytezone.analyse.Play;
import com.bytezone.analyse.State;
import com.bytezone.backgammon.Game.CheckerType;

import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public interface Player extends Iterable<Checker>
{
  public void setName (String name);

  public String getName ();

  public Color getColor ();

  public void setCheckerType (CheckerType checkerType);

  public void rollDice ();

  public void setDice (int die1, int die2);

  public boolean isDouble ();

  public void swapDice ();

  public List<Die> getDice ();

  public byte[] getDiceValues ();

  public Die getCurrentDie ();

  public boolean moreMoves ();

  public void drawDice (GraphicsContext gc);

  public void add (Checker checker);

  public boolean canBearOff ();

  public void doMove ();

  public Optional<Move> undoMove ();

  public Optional<Move> redoMove ();

  public int getHighestPoint ();

  public int getScore ();

  public void clearCheckerDisplay ();

  public void clear ();

  public void analyse (Board board, State state);

  public ObservableList<Play> getPlays ();

  public boolean playFound (State state);
}