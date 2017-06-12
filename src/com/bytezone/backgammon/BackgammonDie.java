package com.bytezone.backgammon;

import java.util.Random;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class BackgammonDie implements Die
{
  private static final Random rand = new Random ();
  private static double size = 50;
  private static double dotRadius = size / 12;

  private Color color;
  private double x, y;
  private double altX, altY;

  private int value;
  private boolean highlighted;
  private boolean unusable;
  private Move move;

  public BackgammonDie (BackgammonPlayer player)
  {
    this.color = player.getColor ();
  }

  public void setColor (Color color)
  {
    this.color = color;
  }

  @Override
  public int roll ()
  {
    value = 1 + rand.nextInt (6);
    unusable = false;
    return value;
  }

  public static void setSize (double size)
  {
    BackgammonDie.size = size;
    BackgammonDie.dotRadius = size / 12;
  }

  public static double getSize ()
  {
    return size;
  }

  @Override
  public void setValue (int value)
  {
    this.value = value;
    unusable = false;
  }

  @Override
  public int getValue ()
  {
    return value;
  }

  @Override
  public void setHighlighted (boolean highlighted)
  {
    this.highlighted = highlighted;
  }

  @Override
  public boolean getHighlighted ()
  {
    return highlighted;
  }

  @Override
  public void setMove (Move move)
  {
    this.move = move;
  }

  @Override
  public Move getMove ()
  {
    return move;
  }

  @Override
  public void setUnusable (boolean unusable)
  {
    this.unusable = unusable;
  }

  @Override
  public boolean getUnusable ()
  {
    return unusable;
  }

  @Override
  public void setCoordinates (double x, double y)
  {
    this.x = x;
    this.y = y;
  }

  @Override
  public void setAlternateCoordinates (double x, double y)
  {
    altX = x;
    altY = y;
  }

  @Override
  public void draw (GraphicsContext gc)
  {
    draw (gc, x, y);
  }

  @Override
  public void drawAlternate (GraphicsContext gc)
  {
    draw (gc, altX, altY);
  }

  private void draw (GraphicsContext gc, double x, double y)
  {
    gc.setFill (color);
    gc.fillRoundRect (x - size / 2, y - size / 2, size, size, 20, 20);

    if (unusable)
    {
      gc.setStroke (Color.RED);
      gc.setLineWidth (6);
      gc.setLineCap (StrokeLineCap.ROUND);
      gc.strokeLine (x - size / 2, y - size / 2, x + size / 2, y + size / 2);
      gc.strokeLine (x + size / 2, y - size / 2, x - size / 2, y + size / 2);
    }

    gc.setFill (Color.BLACK);
    if (value == 1)
      draw1 (gc, x, y);
    else if (value == 2)
      draw2 (gc, x, y);
    else if (value == 3)
      draw3 (gc, x, y);
    else if (value == 4)
      draw4 (gc, x, y);
    else if (value == 5)
      draw5 (gc, x, y);
    else if (value == 6)
      draw6 (gc, x, y);

    if (highlighted && !unusable)
    {
      gc.setStroke (color);
      gc.setLineWidth (5);
      gc.setLineCap (StrokeLineCap.ROUND);
      double radius = size / 2;
      double offset = size / 1.3;
      gc.strokeLine (x - radius, y + offset, x + radius, y + offset);
    }
  }

  private void draw1 (GraphicsContext gc, double x, double y)
  {
    drawDot (gc, x, y);
  }

  private void draw2 (GraphicsContext gc, double x, double y)
  {
    drawDot (gc, x - size / 4, y - size / 4);
    drawDot (gc, x + size / 4, y + size / 4);
  }

  private void draw3 (GraphicsContext gc, double x, double y)
  {
    draw1 (gc, x, y);
    draw2 (gc, x, y);
  }

  private void draw4 (GraphicsContext gc, double x, double y)
  {
    drawDot (gc, x - size / 4, y - size / 4);
    drawDot (gc, x + size / 4, y - size / 4);
    drawDot (gc, x - size / 4, y + size / 4);
    drawDot (gc, x + size / 4, y + size / 4);
  }

  private void draw5 (GraphicsContext gc, double x, double y)
  {
    draw1 (gc, x, y);
    draw4 (gc, x, y);
  }

  private void draw6 (GraphicsContext gc, double x, double y)
  {
    draw4 (gc, x, y);
    drawDot (gc, x, y - size / 4);
    drawDot (gc, x, y + size / 4);
  }

  private void drawDot (GraphicsContext gc, double x, double y)
  {
    gc.fillOval (x - dotRadius, y - dotRadius, dotRadius * 2, dotRadius * 2);
  }
}