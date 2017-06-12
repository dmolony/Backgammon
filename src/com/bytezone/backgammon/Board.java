package com.bytezone.backgammon;

import java.util.Optional;

public interface Board
{
  enum Orientation
  {
    CLOCKWISE, ANTICLOCKWISE
  }

  enum Perspective
  {
    ASCENDING, DESCENDING
  }

  void placeCheckers (Player player, Player opponent);

  Bar getBar (Player whichPlayer);

  HomeBox getHome (Player whichPlayer);

  Point getPoint (int value);

  int getPointValue (Point point);

  Optional<Point> getPoint (double x, double y);

  void draw ();

  void draw (Move move);

  void reverseOrientation ();

  void reversePerspective ();
}