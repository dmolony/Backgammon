package com.bytezone.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import com.bytezone.backgammon.GameRequest;
import com.bytezone.backgammon.Move;
import com.bytezone.backgammon.NetworkGame;

import javafx.application.Platform;

public class GameClient implements Runnable
{
  public static final byte INITIAL_ROLL = 1;
  public static final byte SEND_DICE = 2;
  public static final byte SEND_MOVE = 3;
  public static final byte SWAP_DICE = 4;
  public static final byte UNDO_MOVE = 5;
  public static final byte FINISH_TURN = 6;
  public static final byte QUIT = 7;
  public static final byte SEND_NAME = 8;
  public static final byte SEND_MESSAGE = 9;

  private final NetworkGame game;
  private final Socket socket;
  private final InputStream in;
  private final OutputStream out;

  //  private final String name;
  private final boolean debug = true;
  private final GameRequest gameRequest;

  public GameClient (NetworkGame game, Socket socket, GameRequest gameRequest)
      throws IOException
  {
    this.game = game;
    this.socket = socket;
    this.gameRequest = gameRequest;

    in = socket.getInputStream ();
    out = socket.getOutputStream ();
  }

  @Override
  public void run ()
  {
    int bytesRead;
    byte[] buffer = new byte[256];

    try
    {
      while ((bytesRead = in.read (buffer)) != -1)
      {
        if (debug)
          dump ("Recd", bytesRead, buffer);

        int ptr = 0;
        boolean error = false;
        while (ptr < bytesRead && !error)
        {
          switch (buffer[ptr])
          {
            case INITIAL_ROLL:
            case SEND_DICE:
              ptr += receiveDice (buffer, ptr);
              break;

            case SEND_MOVE:
              ptr += receiveMove (buffer, ptr);
              break;

            case FINISH_TURN:
            case SWAP_DICE:
            case UNDO_MOVE:
            case QUIT:
              ptr += receiveCommand (buffer, ptr);
              break;

            case SEND_MESSAGE:
            case SEND_NAME:
              ptr += receiveText (buffer, ptr);
              break;

            default:
              System.out.printf ("Unknown command: %02X%n", buffer[ptr]);
              error = true;
          }
        }
      }
    }
    catch (IOException e)
    {
      if (e instanceof SocketException)
        System.out.println ("Client closing");
      else
        e.printStackTrace ();
    }
  }

  public String getName ()
  {
    return gameRequest.playerName;
  }

  public GameRequest getGameRequest ()
  {
    return gameRequest;
  }

  private int receiveCommand (byte[] buffer, int ptr)
  {
    byte command = buffer[ptr];       // take a copy before ptr is incremented
    Platform.runLater ( () -> game.receiveCommand (command));
    return 1;
  }

  private int receiveMove (byte[] buffer, int ptr)
  {
    byte[] bufferCopy = new byte[4];
    System.arraycopy (buffer, ptr, bufferCopy, 0, bufferCopy.length);
    Platform.runLater ( () -> game.receiveMove (bufferCopy));
    return bufferCopy.length;
  }

  private int receiveDice (byte[] buffer, int ptr)
  {
    byte[] bufferCopy = new byte[3];
    System.arraycopy (buffer, ptr, bufferCopy, 0, bufferCopy.length);
    Platform.runLater ( () -> game.receiveDice (bufferCopy));
    return bufferCopy.length;
  }

  private int receiveText (byte[] buffer, int ptr)
  {
    int length = (buffer[ptr + 1] & 0xFF) + 2;    // message length + length byte + msgid
    byte[] bufferCopy = new byte[length];
    System.arraycopy (buffer, ptr, bufferCopy, 0, bufferCopy.length);
    Platform.runLater ( () -> game.receiveText (bufferCopy));
    return bufferCopy.length;
  }

  public void sendMove (Move move)
  {
    byte[] buffer = new byte[4];

    buffer[0] = SEND_MOVE;
    buffer[1] = (byte) move.getDieValue ();
    buffer[2] = (byte) move.getValueFrom ();
    buffer[3] = (byte) move.getValueTo ();

    write (buffer);
  }

  public void sendCommand (byte command)
  {
    byte[] buffer = new byte[1];
    buffer[0] = command;
    write (buffer);
  }

  public void sendName (String name)
  {
    assert name.equals (gameRequest.playerName);
    byte[] buffer = new byte[name.length () + 2];
    buffer[0] = SEND_NAME;
    buffer[1] = (byte) name.length ();
    System.arraycopy (name.getBytes (), 0, buffer, 2, name.length ());
    write (buffer);
  }

  public void sendMessage (String message)
  {
    byte[] buffer = new byte[message.length () + 2];
    buffer[0] = SEND_MESSAGE;
    buffer[1] = (byte) message.length ();
    System.arraycopy (message.getBytes (), 0, buffer, 2, message.length ());
    write (buffer);
  }

  public void sendDice (int msgNo, byte[] dice)
  {
    assert msgNo == SEND_DICE || msgNo == INITIAL_ROLL;
    assert dice.length == 2;
    write (msgNo, dice);
  }

  private void dump (String direction, int length, byte[] buffer)
  {
    System.out.printf ("%s: ", direction);
    for (int i = 0; i < length; i++)
    {
      if (i > 0 && i % 16 == 0)
        System.out.printf ("%n      ");
      System.out.printf ("%02X ", buffer[i]);
    }
    System.out.println ();
  }

  public void write (byte[] buffer)
  {
    write (0, buffer);
  }

  public void write (int msgNo, byte[] msg)
  {
    try
    {
      if (msgNo > 0)
      {
        byte[] buffer = new byte[msg.length + 1];
        buffer[0] = (byte) msgNo;
        System.arraycopy (msg, 0, buffer, 1, buffer.length - 1);
        out.write (buffer);
        if (debug)
          dump ("Sent", buffer.length, buffer);
      }
      else
      {
        out.write (msg);            // msg already contains the msgNo
        if (debug)
          dump ("Sent", msg.length, msg);
      }
      out.flush ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }

  public void close ()
  {
    try
    {
      socket.close ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }
}