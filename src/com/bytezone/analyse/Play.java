package com.bytezone.analyse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Play implements Iterable<Move>
{
  private final List<Move> moves = new ArrayList<> ();
  private final State state;

  public Play (byte[] dice, int[] points, int depth, State state)
  {
    for (int i = 0; i < depth; i++)
      moves.add (new Move (dice[i], points[i]));

    this.state = state;
  }

  public int size ()
  {
    return moves.size ();
  }

  public int score ()
  {
    return state.score;
  }

  boolean matches (int[] state)
  {
    for (int i = 0; i < state.length; i++)
      if (state[i] != this.state.board[i])
        return false;
    return true;
  }

  public boolean matches (State state)
  {
    return matches (state.board);
  }

  public boolean firstDieMatches (int value)
  {
    return value == moves.get (0).dieValue;
  }

  @Override
  public String toString ()
  {
    if (moves.size () == 0)
      return "No moves";

    StringBuilder text = new StringBuilder ();

    for (Move move : moves)
    {
      String source = move.pointFrom == 25 ? "bar" : String.format ("%02d", move.pointFrom);
      int target = move.pointFrom - move.dieValue;
      String dest = target <= 0 ? "off" : String.format ("%02d", target);
      text.append (String.format ("%s->%s ", source, dest));
    }

    if (text.length () > 1)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  @Override
  public Iterator<Move> iterator ()
  {
    return moves.iterator ();
  }
}