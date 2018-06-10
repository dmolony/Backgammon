package com.bytezone.backgammon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bytezone.backgammon.Point.StackDirection;

import javafx.beans.InvalidationListener;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class BackgammonBoard extends Canvas implements Board
{
  //  private static final FontLoader fontLoader =
  //      com.sun.javafx.tk.Toolkit.getToolkit ().getFontLoader ();
  public static final int HOME_VALUE = 0;
  public static final int BAR_VALUE = 25;

  private final List<Point> boardPoints = new ArrayList<> (24);
  private final List<Point> otherPoints = new ArrayList<> (4);

  private double border = 15;
  private double barWidth = 34;
  private final double homeBoxWidth = 50;
  private double pointWidth;
  private double pointHeight;

  private Player player, opponent;
  private final HomeBox bottomHome, topHome;
  private final Bar bottomBar, topBar;

  private final Font scoreFont;

  private Orientation orientation = Orientation.ANTICLOCKWISE;
  private Perspective perspective = Perspective.DESCENDING;

  public BackgammonBoard (Player player, Player opponent)
  {
    super (800, 600);

    this.player = player;
    this.opponent = opponent;

    scoreFont = Font.font (36);

    // create points in alternating colors
    Color[] boardColors = { Color.BROWN, Color.BLACK };
    for (int i = 0; i < 6; i++)
    {
      boardPoints.add (new BackgammonPoint (boardColors[0], StackDirection.UP));
      boardPoints.add (new BackgammonPoint (boardColors[1], StackDirection.UP));
    }

    for (int i = 0; i < 6; i++)
    {
      boardPoints.add (new BackgammonPoint (boardColors[0], StackDirection.DOWN));
      boardPoints.add (new BackgammonPoint (boardColors[1], StackDirection.DOWN));
    }

    bottomBar = new Bar (Color.GRAY, StackDirection.UP);
    topBar = new Bar (Color.GRAY, StackDirection.DOWN);
    bottomHome = new HomeBox (Color.LIGHTGRAY, StackDirection.UP);
    topHome = new HomeBox (Color.LIGHTGRAY, StackDirection.DOWN);

    otherPoints.add (bottomBar);
    otherPoints.add (topBar);
    otherPoints.add (bottomHome);
    otherPoints.add (topHome);

    InvalidationListener invalidationListener = evt ->
    {
      layoutBoard ();
      draw ();
    };

    widthProperty ().addListener (invalidationListener);
    heightProperty ().addListener (invalidationListener);

    layoutBoard ();
  }

  @Override
  public void placeCheckers (Player player, Player opponent)
  {
    this.player = player;
    this.opponent = opponent;

    // empty all the board containers
    for (Point point : boardPoints)
      point.clear ();
    for (Point point : otherPoints)
      point.clear ();

    // add the player's home, bar and points
    for (Checker checker : player)
    {
      int value = checker.getValue ();
      if (value == HOME_VALUE)
        getHome (player).push (checker);
      else if (value == BAR_VALUE)
        getBar (player).push (checker);
      else
        getPoint (value).push (checker);
    }

    // add the opponent's home, bar and points
    for (Checker checker : opponent)
    {
      int value = checker.getValue ();
      if (value == HOME_VALUE)
        getHome (opponent).push (checker);
      else if (value == BAR_VALUE)
        getBar (opponent).push (checker);
      else
        getPoint (25 - value).push (checker);       // use the opposite point system
    }
  }

  @Override
  public Bar getBar (Player whichPlayer)
  {
    if (perspective == Perspective.DESCENDING)
      return whichPlayer == player ? bottomBar : topBar;
    else
      return whichPlayer == player ? topBar : bottomBar;
  }

  @Override
  public HomeBox getHome (Player whichPlayer)
  {
    if (perspective == Perspective.DESCENDING)
      return whichPlayer == player ? bottomHome : topHome;
    else
      return whichPlayer == player ? topHome : bottomHome;
  }

  @Override
  public Point getPoint (int value)
  {
    if (value <= HOME_VALUE)
      return getHome (player);

    if (value == BAR_VALUE)
      return getBar (player);

    if (orientation == Orientation.ANTICLOCKWISE)
      if (value <= 12)
        value = 13 - value;
      else
        value = 37 - value;

    if (perspective == Perspective.DESCENDING)
      return boardPoints.get (value - 1);
    else
      return boardPoints.get (24 - value);
  }

  @Override
  public Optional<Point> getPoint (double x, double y)
  {
    for (Point point : boardPoints)
      if (point.contains (x, y))
        return Optional.of (point);

    return Optional.empty ();
  }

  @Override
  public int getPointValue (Point point)
  {
    int index = boardPoints.indexOf (point);
    if (index < 0)
      return -1;

    int value = perspective == Perspective.DESCENDING ? index + 1 : 24 - index;

    if (orientation == Orientation.ANTICLOCKWISE)
      if (value <= 12)
        value = 13 - value;
      else
        value = 37 - value;

    return value;
  }

  @Override
  public void reverseOrientation ()
  {
    if (orientation == Orientation.CLOCKWISE)
      orientation = Orientation.ANTICLOCKWISE;
    else
      orientation = Orientation.CLOCKWISE;

    for (int i = 1; i <= 6; i++)
      getPoint (i).swapCheckers (getPoint (13 - i));
    for (int i = 13; i <= 18; i++)
      getPoint (i).swapCheckers (getPoint (37 - i));
  }

  @Override
  public void reversePerspective ()
  {
    if (perspective == Perspective.ASCENDING)
      perspective = Perspective.DESCENDING;
    else
      perspective = Perspective.ASCENDING;

    for (int i = 1; i <= 12; i++)
      getPoint (i).swapCheckers (getPoint (25 - i));

    topBar.swapCheckers (bottomBar);
    topHome.swapCheckers (bottomHome);
  }

  private void layoutBoard ()
  {
    if (player == null)         // not ready yet
      return;

    double width = getWidth ();
    double height = getHeight ();
    double boardWidth = width - homeBoxWidth - border;

    pointWidth = (boardWidth - barWidth - 2 * border) / 12;
    pointHeight = (height - 2 * border) / 2;

    double radius1 = pointHeight / 14;
    double radius2 = pointWidth * .30;
    double r3 = (radius1 + radius2) / 2;

    BackgammonChecker.setRadius (r3);

    border = (boardWidth - 12 * pointWidth - barWidth) / 2;
    barWidth = boardWidth - 12 * pointWidth - 2 * border;

    // UP points, left-to-right
    double x = border;
    double y = height - border - pointHeight;
    for (int i = 0; i < 12; i++)
    {
      boardPoints.get (i)
          .setCoordinates (new Coordinates (x, y, pointWidth, pointHeight));
      x += pointWidth;
      if (i == 5)
        x += barWidth;
    }

    // DOWN points, left-to-right
    x = border;
    y = border;
    for (int i = 23; i >= 12; i--)
    {
      boardPoints.get (i)
          .setCoordinates (new Coordinates (x, y, pointWidth, pointHeight));
      x += pointWidth;
      if (i == 18)
        x += barWidth;
    }

    layoutBar (topBar, height);
    layoutBar (bottomBar, height);
    layoutHomeBox (topHome, width, height);
    layoutHomeBox (bottomHome, width, height);

    // Dice
    BackgammonDie.setSize (pointWidth * .75);
    double offset = BackgammonDie.getSize () * .75;
    double quadrantMidPoint = 3 * pointWidth + border;
    layoutDice (player.getDice (), quadrantMidPoint, offset, height / 2);
    layoutDice (opponent.getDice (), quadrantMidPoint, offset, height / 2);
  }

  private void layoutBar (Bar bar, double height)
  {
    double halfHeight = (height - 2 * border) / 2;
    double x = border + 6 * pointWidth;
    double y = border;
    if (bar.getDirection () == StackDirection.DOWN)
      y += halfHeight;

    bar.setCoordinates (new Coordinates (x, y, barWidth, halfHeight));
  }

  private void layoutHomeBox (HomeBox homeBox, double width, double height)
  {
    double halfHeight = (height - 2 * border) / 2;
    double x = width - border - homeBoxWidth;
    double y = border;
    if (homeBox.getDirection () == StackDirection.UP)
      y += halfHeight;

    homeBox.setCoordinates (new Coordinates (x, y, homeBoxWidth, halfHeight));
  }

  private void layoutDice (List<Die> dice, double x, double offset, double y)
  {
    dice.get (0).setCoordinates (x - offset, y);                  // single
    dice.get (0).setAlternateCoordinates (x - offset * 3, y);     // double

    dice.get (1).setCoordinates (x + offset, y);                  // single
    dice.get (1).setAlternateCoordinates (x - offset, y);         // double

    dice.get (2).setCoordinates (x + offset, y);
    dice.get (3).setCoordinates (x + offset * 3, y);
  }

  @Override
  public void draw (Move move)
  {
    //    System.out.println (move);
    //    Point pointFrom = getPoint (move.getValueFrom ());
    //    Point pointTo = getPoint (move.getValueTo ());
    //
    //    System.out.println ("from");
    //    System.out.println (pointFrom);
    //    System.out.println ("to");
    //    System.out.println (pointTo);
  }

  @Override
  public void draw ()
  {
    GraphicsContext gc = getGraphicsContext2D ();

    double width = getWidth ();
    double height = getHeight ();

    gc.setFill (Color.GREEN);
    gc.fillRect (0, 0, width, height);

    gc.setFill (Color.GRAY);
    gc.fillRect (0, 0, width, border);                      // top border
    gc.fillRect (0, height - border, width, border);        // bottom border
    gc.fillRect (0, 0, border, height);                     // left border
    gc.fillRect (width - border, 0, border, height);        // right border
    gc.fillRect (width - homeBoxWidth - 2 * border, 0, border, height);

    topHome.draw (gc);
    bottomHome.draw (gc);

    topBar.draw (gc);
    bottomBar.draw (gc);

    // draw points, checkers and point values
    for (int i = 1; i <= 24; i++)
      ((BackgammonPoint) getPoint (i)).draw (gc, i);

    // draw dice
    player.drawDice (gc);

    // draw scores
    String score = player.getScore () + " : " + opponent.getScore ();
    gc.setFill (Color.WHITE);
    gc.setFont (scoreFont);

    double quadrantMidPoint = getWidth () - (3 * pointWidth + border * 2 + homeBoxWidth);

    //    float textWidth = fontLoader.computeStringWidth (score, scoreFont);
    final Text text = new Text (score);
    text.setFont (scoreFont);
    text.applyCss ();
    float textWidth = (float) text.getLayoutBounds ().getWidth ();

    gc.fillText (score, quadrantMidPoint - textWidth / 2, height / 2 + 10);
  }

  boolean keyHandled (KeyEvent e)
  {
    switch (e.getCharacter ())
    {
      case "b":
        reverseOrientation ();
        draw ();
        return true;

      case "p":
        reversePerspective ();
        draw ();
        return true;
    }
    return false;
  }

  @Override
  public boolean isResizable ()
  {
    return true;
  }
}