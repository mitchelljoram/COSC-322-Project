
package ubc.cosc322;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sfs2x.client.entities.Room;
import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;
import ygraph.ai.smartfox.games.amazons.HumanPlayer;

/**
 * An example illustrating how to implement a GamePlayer
 * @author Yong Gao (yong.gao@ubc.ca)
 * Jan 5, 2021
 *
 */
public class COSC322Bot extends GamePlayer{

    private GameClient gameClient = null; 
    private BaseGameGUI gamegui = null;
	
    private String userName = null;
    private String passwd = null;
    private Board board;
	
    /**
     * The main method
     * @param args for name and passwd (current, any string would work)
     */
    public static void main(String[] args) {				 
    	COSC322Bot player = new COSC322Bot(args[0], args[1]);
    	//HumanPlayer player = new HumanPlayer();
    	
    	if(player.getGameGUI() == null) {
    		player.Go();
    	}
    	else {
    		BaseGameGUI.sys_setup();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                	player.Go();
                }
            });
    	}
    }
	
    /**
     * Any name and passwd 
     * @param userName
     * @param passwd
     */
    public COSC322Bot(String userName, String passwd) {
    	this.userName = userName;
    	this.passwd = passwd;
    	
    	//To make a GUI-based player, create an instance of BaseGameGUI
    	//and implement the method getGameGUI() accordingly
    	this.gamegui = new BaseGameGUI(this);
    }
 


    @Override
    public void onLogin() {
    	System.out.println("Congratualations!!! "
    			+ "I am called because the server indicated that the login is successfully");
    	System.out.println("The next step is to find a room and join it: "
    			+ "the gameClient instance created in my constructor knows how!");
    	
    	/*
    	List<Room> rooms = gameClient.getRoomList();
    	for(Room room: rooms) {System.out.print(room.getName() + " ");}
    	System.out.println();
    	int roomIndex = (int) Math.random(rooms.size());
    	gameClient.joinRoom(rooms.get(roomIndex).getName());
    	System.out.print("You have successfully joined room " + roomIndex + ".");
    	*/
    	
    	userName = gameClient.getUserName();
    	if(gamegui != null) {
    		gamegui.setRoomInformation(gameClient.getRoomList());
    	}
    }

    @Override
    public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
    	//This method will be called by the GameClient when it receives a game-related message
    	//from the server.
    	//For a detailed description of the message types and format, 
    	//see the method GamePlayer.handleGameMessage() in the game-client-api document.
    	
    	System.out.println(msgDetails.get(messageType));
    	switch(messageType) {
    	case GameMessage.GAME_STATE_BOARD:
    		gamegui.setGameState((ArrayList<Integer>) msgDetails.get("game-state"));
    		break;
    	case GameMessage.GAME_ACTION_START:
    		if(msgDetails.get(AmazonsGameMessage.PLAYER_BLACK).equals(this.userName)) {
    			System.out.println("White: " + msgDetails.get(AmazonsGameMessage.PLAYER_WHITE));
    			System.out.println("Black: " + this.userName);
    			board = new Board(true);
    			handleGameActionStart();
    		}
    		else {
    			System.out.println("White: " + this.userName);
    			System.out.println("Black: " + msgDetails.get(AmazonsGameMessage.PLAYER_BLACK));
    			board = new Board(false);
    		}
    		break;
    	case GameMessage.GAME_ACTION_MOVE:
    		try {
    			gamegui.updateGameState(msgDetails);
				handleGameActionMove(msgDetails);
			} catch (Exception e) {
				e.printStackTrace();
			}
    		break;
    	}
    	
    	return true;
    }
    
    public void handleGameActionStart() {
    	for(Queen queen: board.allies) {
        	queen.actions.getActions(board,queen);
        }
        Queen chosen = board.allies[(int) (Math.random()*4)];
        ArrayList<Integer> action = chosen.actions.actions.get((int) (Math.random()*chosen.actions.actions.size()));
        ArrayList<Integer> queenPrevPos = new ArrayList<Integer>();
        queenPrevPos.add(chosen.getRow()); queenPrevPos.add(chosen.getCol());
        ArrayList<Integer> queenNewPos = new ArrayList<Integer>();
        queenNewPos.add(chosen.getRow()+action.get(0)); queenNewPos.add(chosen.getCol()+action.get(1));
        board.updateBoard(queenPrevPos, queenNewPos);
        chosen.actions.getArrowThrows(board, chosen);
        ArrayList<Integer> arrowThrow = chosen.actions.arrowThrows.get((int) (Math.random()*chosen.actions.arrowThrows.size()));
        ArrayList<Integer> arrPos = new ArrayList<Integer>();
        arrPos.add(arrowThrow.get(0)); arrPos.add(arrowThrow.get(1));
        board.updateBoard(arrPos);
        gameClient.sendMoveMessage(queenPrevPos,queenNewPos,arrPos);
        gamegui.updateGameState(queenPrevPos, queenNewPos, arrPos);
        //board.printBoard();
    }
    
    public void handleGameActionMove(Map<String, Object> msgDetails) {
    	// opponent's move
    	ArrayList<Integer> queenPrevPos = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
        ArrayList<Integer> queenNewPos = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.Queen_POS_NEXT);
        ArrayList<Integer> arrPos = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);
        board.updateBoard(queenPrevPos, queenNewPos, arrPos);
        //board.printBoard();
        // game over check
        int gameOver = board.gameOverCheck(false);
        if(gameOver == 0) return;
        // our move
        Queen chosen = board.allies[(int) (Math.random()*4)];
        ArrayList<Integer> action = chosen.actions.actions.get((int) (Math.random()*chosen.actions.actions.size()));
        queenPrevPos = new ArrayList<Integer>();
        queenPrevPos.add(chosen.getRow()+1); queenPrevPos.add(chosen.getCol()+1);
        queenNewPos = new ArrayList<Integer>();
        queenNewPos.add(chosen.getRow()+action.get(0)+1); queenNewPos.add(chosen.getCol()+action.get(1)+1);
        board.updateBoard(queenPrevPos, queenNewPos);
        chosen.actions.getArrowThrows(board, chosen);
        ArrayList<Integer> arrowThrow = chosen.actions.arrowThrows.get((int) (Math.random()*chosen.actions.arrowThrows.size()));
        arrPos = new ArrayList<Integer>();
        arrPos.add(chosen.getRow()+arrowThrow.get(0)+1); arrPos.add(chosen.getCol()+arrowThrow.get(1)+1);
        board.updateBoard(arrPos);
        gameClient.sendMoveMessage(queenPrevPos,queenNewPos,arrPos);
        gamegui.updateGameState(queenPrevPos, queenNewPos, arrPos);
        //board.printBoard();
        // game over check
        gameOver = board.gameOverCheck(true);
        if(gameOver == 1) return;
    }
    
    
    @Override
    public String userName() {
    	return userName;
    }

	@Override
	public GameClient getGameClient() {
		// TODO Auto-generated method stub
		return this.gameClient;
	}

	@Override
	public BaseGameGUI getGameGUI() {
		// TODO Auto-generated method stub
		return  this.gamegui;
	}

	@Override
	public void connect() {
		// TODO Auto-generated method stub
    	gameClient = new GameClient(userName, passwd, this);			
	}

 
}//end of class