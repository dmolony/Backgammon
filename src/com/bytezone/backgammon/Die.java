package com.bytezone.backgammon;

import javafx.scene.canvas.GraphicsContext;

public interface Die
{
  public void draw (GraphicsContext gc);

  public void drawAlternate (GraphicsContext gc);

  public int roll ();

  public void setCoordinates (double x, double y);

  public void setAlternateCoordinates (double x, double y);

  public void setHighlighted (boolean highlighted);

  public void setUnusable (boolean unusable);

  public boolean getUnusable ();

  public boolean getHighlighted ();

  public void setMove (Move move);

  public Move getMove ();

  public void setValue (int value);

  public int getValue ();
}