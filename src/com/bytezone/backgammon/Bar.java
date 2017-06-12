package com.bytezone.backgammon;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Bar extends AbstractPoint
{
  public Bar (Color color, StackDirection direction)
  {
    super (color, direction);
  }

  @Override
  public void draw (GraphicsContext gc)
  {
    gc.setFill (color);
    gc.fillRect (coordinates.x, coordinates.y, coordinates.width, coordinates.height);
    double radius = BackgammonChecker.getRadius ();

    if (checkers.size () > 0)
    {
      double x = coordinates.x + coordinates.width / 2;     // mid line
      double y = coordinates.y + (direction == StackDirection.DOWN ? radius * 2
          : coordinates.height - radius * 2);

      double offset = direction == StackDirection.DOWN ? radius * 2 : -2 * radius;
      for (Checker checker : checkers)
      {
        checker.drawFlat (gc, x, y);
        y += offset;
      }
    }
  }

  @Override
  public String toString ()
  {
    return "bar";
  }
}