package com.bytezone.analyse;

import java.util.ArrayList;
import java.util.List;

public class Evaluation
{
  private List<Play> plays;
  private final boolean debug = false;
  private int minDice;
  private int maxDice;

  public List<Play> getPlays (byte[] actualDice, State initialState)
  {
    assert actualDice.length == 2;

    plays = new ArrayList<> ();
    maxDice = 0;
    minDice = 99;

    byte[] dice = getDice (actualDice);
    boolean doubles = actualDice[0] == actualDice[1];

    int[] points = new int[dice.length];
    calculatePlays (dice, points, 0, initialState);

    if (!doubles)                     // swap dice and recalculate
    {
      dice[0] = actualDice[1];
      dice[1] = actualDice[0];
      calculatePlays (dice, points, 0, initialState);
    }

    if (debug)
      System.out.printf ("Total plays: %d%n%n", plays.size ());

    if (plays.size () > 0 && minDice < maxDice)
      plays.removeIf (play -> play.size () < maxDice);

    // if only one move can be made - must use the larger die if both dice are possible
    if (maxDice == 1 && !doubles)
    {
      System.out.println ("Checking for larger die moves");
      for (Play play : plays)
        System.out.println (play);

      int min = Math.min (dice[0], dice[1]);
      int max = Math.max (dice[0], dice[1]);

      boolean containsMax = false;
      for (Play play : plays)
        if (play.firstDieMatches (max))
        {
          containsMax = true;
          break;
        }

      if (containsMax)
      {
        System.out.println ("removing smaller die from possible plays");
        plays.removeIf (play -> play.firstDieMatches (min));

        System.out.println ("remaining plays");
        for (Play play : plays)
          System.out.println (play);
      }
    }

    return plays;
  }

  public int getMaxDice ()
  {
    return maxDice;
  }

  private void calculatePlays (byte[] dice, int[] points, int currentDieNo,
      State initialState)
  {
    // get a list of all possible states from the current state and a single die value
    List<State> legalStates = getLegalStates (initialState, dice[currentDieNo]);

    if (legalStates.size () == 0)
      addPlay (dice, points, currentDieNo, initialState);
    else
      for (State possibleState : legalStates)
      {
        points[currentDieNo] = possibleState.point;
        if (dice.length == (currentDieNo + 1))          // no more dice to check
          addPlay (dice, points, currentDieNo + 1, possibleState);
        else
          calculatePlays (dice, points, currentDieNo + 1, possibleState);
      }
  }

  private void addPlay (byte[] dice, int[] points, int diceUsed, State state)
  {
    if (diceUsed == 0 || playExists (state.board))
      return;

    Play play = new Play (dice, points, diceUsed, state);
    plays.add (play);

    maxDice = Math.max (maxDice, play.size ());
    minDice = Math.min (minDice, play.size ());
  }

  private boolean playExists (int[] state)
  {
    for (Play play : plays)
      if (play.matches (state))
        return true;

    return false;
  }

  private List<State> getLegalStates (State state, int dieValue)
  {
    if (debug)
    {
      System.out.printf ("%d%n", dieValue);
      System.out.println (state);
    }
    boolean allHome = allHome (state.board);
    List<State> legalStates = new ArrayList<> ();

    // check every point from the bar down for a possible move
    for (int point = 25; point > 0; point--)
    {
      if (point < 25 && state.board[25] > 0)        // must empty the bar first
        break;

      if (state.board[point] <= 0)                  // no checkers on that point
        continue;

      int target = point - dieValue;
      if (target <= 0)                              // bearing off
      {
        if (!allHome)
          continue;                                 // cannot bear off

        if (point != dieValue && biggerHomePoint (point, state.board))
          continue;                                 // cannot bear off

        target = 0;
      }
      else if (state.board[target] < -1)            // blocked by opponent
        continue;

      // create new state from the move
      legalStates.add (new State (state.board, point, dieValue));
    }

    if (debug)
    {
      System.out.printf ("%d legal states found%n", legalStates.size ());
      for (State thisState : legalStates)
        System.out.println (thisState);
    }

    return legalStates;
  }

  private byte[] getDice (byte[] actualDice)
  {
    if (actualDice[0] == actualDice[1])
    {
      byte[] dice = new byte[4];
      dice[0] = dice[1] = dice[2] = dice[3] = actualDice[0];
      return dice;
    }

    byte[] dice = new byte[2];
    dice[0] = actualDice[0];
    dice[1] = actualDice[1];
    return dice;
  }

  private boolean biggerHomePoint (int point, int[] state)
  {
    for (int i = 6; i > point; i--)
      if (state[i] > 0)
        return true;
    return false;
  }

  private boolean allHome (int[] state)
  {
    int checkers = 0;
    for (int point = 0; point <= 6; point++)
      if (state[point] > 0)
        checkers += state[point];
    return checkers == 15;
  }

  public static void main (String[] args)
  {
    Evaluation evaluation = new Evaluation ();
    State state = new State ();
    byte[] dice = null;

    int test = 2;
    switch (test)
    {
      case 0:                  // initial board setting
        state.board[24] = 2;
        state.board[13] = 5;
        state.board[8] = 3;
        state.board[6] = 5;

        state.board[1] = -2;
        state.board[12] = -5;
        state.board[17] = -3;
        state.board[19] = -5;

        dice = new byte[] { 6, 5 };
        break;

      case 1:
        state.board[24] = 4;
        state.board[6] = 1;
        state.board[5] = 2;
        state.board[4] = 3;
        state.board[3] = 3;
        state.board[2] = 2;

        state.board[23] = -2;
        state.board[22] = -6;
        state.board[21] = -2;
        state.board[20] = -2;
        state.board[19] = -2;

        dice = new byte[] { 3, 5 };
        break;

      case 2:
        state.board[24] = 1;
        state.board[6] = 1;
        //        state.board[5] = 1;

        state.board[21] = -2;
        state.board[19] = -2;
        //        state.board[1] = -2;

        dice = new byte[] { 3, 5 };
        break;

      default:
        break;
    }

    List<Play> plays = evaluation.getPlays (dice, state);

    if (plays.size () == 0)
      System.out.println ("Blocked");

    System.out.println ("\nResult:");
    for (Play play : plays)
      System.out.println (play);
  }
}