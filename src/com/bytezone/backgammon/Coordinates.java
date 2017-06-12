package com.bytezone.backgammon;

public class Coordinates
{
  double x, y;
  double width, height;

  public Coordinates (double x, double y, double width, double height)
  {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  @Override
  public String toString ()
  {
    return String.format ("[x=%f, y=%f, w=%f, h=%f]", x, y, width, height);
  }
}