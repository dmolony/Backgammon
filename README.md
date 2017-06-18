# Backgammon
This game can be used by one or two players on a single computer, or by two players over a network. The network can be a local network, or the internet (if port forwarding on 27984 is set up for the server).

### Installation
* Install the **latest** version of the [Java Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (JRE or JDK).
* Download Backgammon.jar from the [releases](https://github.com/dmolony/Backgammon/releases) page.
* Double-click the jar file, or enter 'java -jar Backgammon.jar' in the terminal.
### Example Screens
#### Start Game
For networked games the first player must start the server and when it is ready the second player can join.
![Select game type](resources/start.png?raw=true "Select game type")
#### Board
Click on the piece you wish to move. It will move using the currently selected die. To use both dice on the same piece, use shift-click. Undo a move with U, redo a move with R. Swap the order of the dice with X.
![Initial board](resources/board1.png?raw=true "Initial board")
#### Possible Moves
Command-M displays all of the possible moves. Click on a move to use it.
![Board with moves](resources/board2.png?raw=true "Board with moves")
#### Commands
|Key             |Action                                 |
|----------------|---------------------------------------|
|X               | Swap dice                             |
|Space           | Finish move                           |
|U               | Undo move                             |
|R               | Redo move                             |
|P               | Swap player perspective               |
|B               | Swap board perspective                |
|cmd-M           | Show/hide possible moves              |
|cmd-T           | Show/hide chat window                 |
