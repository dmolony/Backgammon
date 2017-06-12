package com.bytezone.analyse;

import com.bytezone.backgammon.BackgammonBoard;
import com.bytezone.backgammon.Checker;
import com.bytezone.backgammon.Player;

public class State
{
  int point;
  int[] board;
  int score;            // could use this to determine minimum legal moves

  // create an empty board
  public State ()
  {
    board = new int[26];                // 0=home, 1:24=points, 25=bar
  }

  // create a new state from the current board layout
  public State (BackgammonBoard board, Player player, Player opponent)
  {
    this ();

    // set the bar and home values
    this.board[0] = board.getHome (player).size ();
    this.board[25] = board.getBar (player).size ();

    // set positive values for our checkers
    for (Checker checker : player)
    {
      int value = checker.getValue ();
      if (value >= 1 && value <= 24)
        this.board[value]++;
    }

    // set negative values for opponent's checkers
    for (Checker checker : opponent)
    {
      int value = checker.getValue ();
      if (value >= 1 && value <= 24)
        this.board[25 - value]--;
    }

    calculateScore ();
  }

  // create a new state given a previous state and a valid move
  State (int[] board, int pointFrom, int dieValue)
  {
    this ();
    this.point = pointFrom;

    // copy previous state
    for (int i = 0; i < board.length; i++)
      this.board[i] = board[i];

    // calculate the target point (0=bar)
    int target = pointFrom - dieValue;
    if (target < 0)
      target = 0;

    this.board[pointFrom]--;            // remove our checker
    assert this.board[target] >= -1;    // legal move
    if (this.board[target] == -1)       // capturing?
      this.board[target] = 1;           // replace with our checker
    else
      this.board[target]++;             // add our checker

    calculateScore ();
  }

  private void calculateScore ()
  {
    score = 0;
    for (int point = 1; point <= 25; point++)
      if (board[point] > 0)
        score += board[point] * point;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    for (int i = 13; i <= 24; i++)
      System.out.printf ("%2d ", board[i]);
    System.out.println ();
    for (int i = 12; i >= 1; i--)
      System.out.printf ("%2d ", board[i]);
    System.out.printf ("Score: %d%n%n", score);

    return text.toString ();
  }
}