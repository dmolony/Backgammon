package com.bytezone.backgammon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;

public abstract class AbstractPoint implements Point
{
  protected Coordinates coordinates;

  protected List<Checker> checkers = new ArrayList<> ();
  protected StackDirection direction;
  protected Color color;

  public AbstractPoint (Color color, StackDirection direction)
  {
    this.color = color;
    this.direction = direction;
  }

  @Override
  public StackDirection getDirection ()
  {
    return direction;
  }

  @Override
  public void swapCheckers (Point other)
  {
    List<Checker> temp = this.checkers;
    this.checkers = ((AbstractPoint) other).checkers;
    ((AbstractPoint) other).checkers = temp;
  }

  @Override
  public void push (Checker checker)
  {
    if (checkers.contains (checker))
      throw new IllegalArgumentException ("Checker is already on this point");

    checkers.add (checker);
  }

  @Override
  public Checker pop ()
  {
    if (checkers.size () == 0)
    {
      Alert alert = new Alert (AlertType.INFORMATION, "No checkers are on this point",
          ButtonType.OK);
      alert.setHeaderText (null);             // remove stupid text
      alert.showAndWait ();
    }
    if (checkers.size () == 0)
      throw new IllegalArgumentException ("No checkers are on this point");

    Checker checker = checkers.get (checkers.size () - 1);
    checkers.remove (checker);

    return checker;
  }

  @Override
  public Checker peek ()
  {
    if (checkers.size () == 0)
      throw new IllegalArgumentException ("No checkers are on this point");

    return checkers.get (checkers.size () - 1);
  }

  @Override
  public void clear ()
  {
    checkers.clear ();
  }

  @Override
  public void setCoordinates (Coordinates coordinates)
  {
    this.coordinates = coordinates;
  }

  @Override
  public Coordinates getCoordinates ()
  {
    return coordinates;
  }

  @Override
  public boolean contains (double x, double y)
  {
    return (x >= coordinates.x && y >= coordinates.y
        && x < coordinates.x + coordinates.width
        && y < coordinates.y + coordinates.height);
  }

  @Override
  public int size ()
  {
    return checkers.size ();
  }

  @Override
  public void setColor (Color color)
  {
    this.color = color;
  }

  @Override
  public Color getColor ()
  {
    return color;
  }

  @Override
  public Iterator<Checker> iterator ()
  {
    return checkers.iterator ();
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    for (Checker checker : checkers)
      text.append (String.format ("%s%n", checker));

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}