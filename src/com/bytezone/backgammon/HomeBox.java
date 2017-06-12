package com.bytezone.backgammon;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class HomeBox extends AbstractPoint
{
  public HomeBox (Color color, StackDirection direction)
  {
    super (color, direction);
  }

  @Override
  public void draw (GraphicsContext gc)
  {
    gc.setFill (color);
    gc.fillRect (coordinates.x, coordinates.y, coordinates.width, coordinates.height);

    if (checkers.size () > 0)
    {
      double x = coordinates.x + coordinates.width / 2;     // mid line
      double y =
          coordinates.y + (direction == StackDirection.DOWN ? 10 : coordinates.height - 11);
      double offset = direction == StackDirection.DOWN ? 15 : -15;
      for (Checker checker : checkers)
      {
        checker.drawEdge (gc, x, y);
        y += offset;
      }
    }
  }

  @Override
  public String toString ()
  {
    return "home";
  }
}