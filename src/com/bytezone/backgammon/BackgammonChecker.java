package com.bytezone.backgammon;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class BackgammonChecker implements Checker
{
  //  private static final FontLoader fontLoader =
  //      com.sun.javafx.tk.Toolkit.getToolkit ().getFontLoader ();
  private static double radius;

  private static Font font18 = Font.font (18);
  private static Font font14 = Font.font (14);
  private static Font font12 = Font.font (12);

  private final Player player;        // the owner of the checker
  //  private double radius;

  private final List<Move> moves = new ArrayList<> ();
  private int value;

  public BackgammonChecker (Player player, int value)
  {
    this.player = player;
    this.value = value;
    assert value >= 0 && value <= 25 : "value: " + value;
  }

  public static void setRadius (double radius)
  {
    BackgammonChecker.radius = radius;
  }

  public static double getRadius ()
  {
    return radius;
  }

  @Override
  public void setValue (int value)
  {
    //    if (value < 0 || value > 25)
    //      System.out.println ("Unexpected checker value: " + value);
    assert value >= 0 && value <= 25 : "value: " + value;
    this.value = value;
  }

  @Override
  public int getValue ()
  {
    assert value >= 0 && value <= 25 : "value: " + value;
    //    return value < 0 ? 0 : value;
    return value;
  }

  @Override
  public void drawFlat (GraphicsContext gc, double x, double y)
  {
    double w = radius * 2 - 2;
    double h = radius * 2 - 2;

    gc.setFill (player.getColor ());
    gc.fillOval (x - radius + 1, y - radius + 1, w, h);

    if (moves.size () > 0)
    {
      String text = getText ();
      int len = text.length ();
      gc.setFont (len <= 3 ? font18 : len <= 5 ? font14 : font12);
      drawText (gc, x, y, text);
    }
  }

  @Override
  public void drawEdge (GraphicsContext gc, double x, double y)
  {
    gc.setFill (player.getColor ());
    gc.fillRoundRect (x - radius, y - 7, radius * 2, 14, 8, 8);

    if (moves.size () > 0)
    {
      gc.setFont (font14);
      drawText (gc, x, y, getText ());
    }
  }

  private String getText ()
  {
    StringBuilder text = new StringBuilder ();
    for (Move move : moves)
    {
      text.append (move.getDieValue ());
      text.append (",");
    }
    text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  private void drawText (GraphicsContext gc, double x, double y, String text)
  {
    int len = text.length ();
    int offsetY = len <= 3 ? 6 : len <= 5 ? 5 : 4;

    gc.setFill (Color.BLACK);

    //    float width = fontLoader.computeStringWidth (text, gc.getFont ()) / 2;
    final Text ftext = new Text (text);
    ftext.setFont (gc.getFont ());
    ftext.applyCss ();
    float width = (float) ftext.getLayoutBounds ().getWidth () / 2;

    gc.fillText (text, x - width, y + offsetY);
  }

  @Override
  public void pushMove (Move move)
  {
    if (moves.contains (move))
      throw new IllegalArgumentException ("Move already exists");

    moves.add (move);
  }

  @Override
  public Move popMove ()
  {
    if (moves.size () == 0)
      throw new IllegalArgumentException ("No move to undo");

    Move move = moves.get (moves.size () - 1);
    moves.remove (moves.size () - 1);

    return move;
  }

  @Override
  public void clearMoves ()
  {
    moves.clear ();
  }

  @Override
  public Player getOwner ()
  {
    return player;
  }

  @Override
  public int compareTo (Checker o)
  {
    return this.value - o.getValue ();
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Owner.....%s%n", player));
    text.append (String.format ("Value.....%d%n", value));
    for (Move move : moves)
      text.append (String.format ("Move......%s%n", move));

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}