package com.bytezone.backgammon;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

interface Point extends CheckerContainer, Iterable<Checker>
{
  enum StackDirection
  {
    UP, DOWN
  }

  public void draw (GraphicsContext gc);

  public void setColor (Color color);

  public Color getColor ();

  public void setCoordinates (Coordinates coordinates);

  public Coordinates getCoordinates ();

  public boolean contains (double x, double y);

  public StackDirection getDirection ();
}