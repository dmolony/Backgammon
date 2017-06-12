package com.bytezone.backgammon;

import javafx.scene.canvas.GraphicsContext;

public interface Checker extends Comparable<Checker>
{
  public void drawEdge (GraphicsContext gc, double x, double y);

  public void drawFlat (GraphicsContext gc, double x, double y);

  public Player getOwner ();

  public int getValue ();

  public void setValue (int value);

  public void pushMove (Move move);

  public Move popMove ();

  public void clearMoves ();
}