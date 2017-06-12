package com.bytezone.backgammon;

public interface CheckerContainer
{
  public void push (Checker checker);

  public void swapCheckers (Point other);

  public Checker pop ();

  public Checker peek ();

  public void clear ();

  public int size ();
}