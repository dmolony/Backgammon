package com.bytezone.backgammon;

public class Move
{
  private final int valueFrom;
  private final int valueTo;
  private final Die die;

  Checker capturedChecker;

  public Move (Die die, int valueFrom, int valueTo)
  {
    this.die = die;
    this.valueFrom = valueFrom;
    this.valueTo = valueTo;
    die.setMove (this);
  }

  public Move (Die die, int valueFrom, int valueTo, Checker capturedChecker)
  {
    this (die, valueFrom, valueTo);
    this.capturedChecker = capturedChecker;
  }

  public int getValueFrom ()
  {
    return valueFrom;
  }

  public int getValueTo ()
  {
    return valueTo;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("From: %02d, To: %02d, Die: %d, Captured: %s", valueFrom,
        valueTo, die.getValue (), capturedChecker));

    return text.toString ();
  }

  public int getDieValue ()
  {
    return die.getValue ();
  }
}