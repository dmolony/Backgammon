package com.bytezone.backgammon;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class BackgammonPoint extends AbstractPoint
{
  private static Font font = Font.font (12);
  //  private static FontLoader fontLoader =
  //      com.sun.javafx.tk.Toolkit.getToolkit ().getFontLoader ();

  protected double[] shapeX = new double[4];
  protected double[] shapeY = new double[4];

  public BackgammonPoint (Color color, StackDirection direction)
  {
    super (color, direction);
  }

  @Override
  public void setCoordinates (Coordinates coordinates)
  {
    super.setCoordinates (coordinates);

    if (direction == StackDirection.DOWN)
    {
      this.shapeX[0] = coordinates.x;
      this.shapeY[0] = coordinates.y;

      this.shapeX[1] = coordinates.x + coordinates.width;
      this.shapeY[1] = coordinates.y;

      this.shapeX[2] = coordinates.x + coordinates.width / 2;
      this.shapeY[2] = coordinates.y + coordinates.height * .85;

      this.shapeX[3] = coordinates.x;
      this.shapeY[3] = coordinates.y;
    }
    else
    {
      this.shapeX[0] = coordinates.x;
      this.shapeY[0] = coordinates.y + coordinates.height;

      this.shapeX[1] = coordinates.x + coordinates.width;
      this.shapeY[1] = coordinates.y + coordinates.height;

      this.shapeX[2] = coordinates.x + coordinates.width / 2;
      this.shapeY[2] = coordinates.y + coordinates.height * .15;

      this.shapeX[3] = coordinates.x;
      this.shapeY[3] = coordinates.y + coordinates.height;
    }
  }

  void draw (GraphicsContext gc, int value)
  {
    draw (gc);

    gc.setStroke (Color.BLACK);
    gc.setLineWidth (1);
    gc.setFont (font);

    // draw point value in the border
    String text = value + "";

    //    float width = fontLoader.computeStringWidth (text, font) / 2;
    final Text ftext = new Text (text);
    ftext.setFont (font);
    ftext.applyCss ();
    float width = (float) ftext.getLayoutBounds ().getWidth () / 2;

    if (direction == StackDirection.DOWN)
      gc.strokeText (text, shapeX[2] - width, shapeY[0] - 2);
    else
      gc.strokeText (text, shapeX[2] - width, shapeY[0] + 11);
  }

  @Override
  public void draw (GraphicsContext gc)
  {
    gc.setFill (color);
    gc.fillPolygon (shapeX, shapeY, shapeX.length);
    double radius = BackgammonChecker.getRadius ();

    if (checkers.size () > 0)
    {
      double x = coordinates.x + coordinates.width / 2;     // mid line
      double y;
      double offset;

      if (direction == StackDirection.DOWN)
      {
        y = coordinates.y + radius + 1;
        offset = radius * 2;
      }
      else
      {
        y = coordinates.y + coordinates.height - radius - 1;
        offset = radius * 2 * -1;
      }

      for (Checker checker : checkers)
      {
        checker.drawFlat (gc, x, y);
        y += offset;
      }
    }
  }
}