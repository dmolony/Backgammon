package com.bytezone.analyse;

public class Move
{
  public final int dieValue;
  public final int pointFrom;

  public Move (int dieValue, int from)
  {
    this.dieValue = dieValue;
    this.pointFrom = from;
  }
}