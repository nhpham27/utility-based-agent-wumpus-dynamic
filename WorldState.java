import java.util.HashMap;

public class WorldState {
	private boolean alreadyShot;
	private int worldStateSize = 6;
	public Square[][] state;
	private WumpusModel wumpusModel;
	
	WorldState(){
		// initialize the state of the world
		state = new Square[this.worldStateSize][this.worldStateSize];
		this.wumpusModel = new WumpusModel(0.2,0.2,0.2,0.2,0.2);
		for(int i = 0;i < this.worldStateSize; i++) {
			for(int j = 0;j < this.worldStateSize; j++) {
				state[i][j] = new Square();
				state[i][j].x = i;
				state[i][j].y = j;
				if(i == 0 || i == 5 || j == 0 || j == 5) {
					state[i][j].isWall = true;
					//state[i][j].numVisit = 10;
				}
			}
		}
		
		// the agent is initially at 1,1 facing East
		this.state[1][1].agentDirection = '>';
		this.alreadyShot = false;
	}
	
	WorldState(Square[][] state, boolean alreadyShot){
		// initialize the state of the world
		this.state = state;
		
		this.alreadyShot = alreadyShot;
	}
	
	// print the state of the world
	public void printState() {
		for(int i = this.worldStateSize - 1;i >= 0; i--) {
			for(int j = 0;j < this.worldStateSize; j++) {
				Square temp = state[j][i];
				if(temp.isPit >= temp.isWumpus && temp.isPit > 0)
					System.out.print("[P]");
				else if(temp.isWall)
						System.out.print("[X]");
				else if(temp.isWumpus >= temp.isPit && temp.isWumpus > 0)
					System.out.print("[W]");
				else if(temp.agentDirection == ' ' && temp.numVisit > 0)
					System.out.print("[" + temp.numVisit + "]");
				else
					System.out.print("[" + temp.agentDirection + "]");
			}
			System.out.println("");
		}
		System.out.println("");
	}
	
	// get agent's location, aka, the square the agent is in
	public Square getAgentLocation() {
		Square agentLoc = null;
		for(int i = 0;i < this.worldStateSize; i++) {
			for(int j = 0;j < this.worldStateSize; j++) {
				if(state[i][j].agentDirection != ' ') {
					agentLoc = state[i][j];
				}
			}
		}
		return agentLoc;
	}
	
	// get the current orientation of the agent based on
	// previous orientation and action taken
	public char getDirection(char prevDirection, int action) {
		String orientations = "<A>v";
		// find the index of previous orientation in the list
		// of orientations
		int index = orientations.indexOf(prevDirection);
		int offset = 0;
		// get the offset to add to the index to get
		// new orientation of the agent
		if(action == Action.TURN_LEFT)
			offset = -1;
		else if(action == Action.TURN_RIGHT)
			offset = 1;
		index = index + offset;
		
		// if the index exceed the boundary,
		// set it to the value in the  other 
		// end of the list
		if(index < 0)
			index = orientations.length() - 1;
		else if(index > orientations.length() - 1)
			index = 0;
		
		return orientations.charAt(index);
	}
	
	public void updateAgentPosition(int action) {
		// get the square the agent is currently in
		Square agentLoc = this.getAgentLocation();
		if(action == Action.GO_FORWARD) {
			// if the agent goes forward, update the location
			
			if((agentLoc.agentDirection == '>' && agentLoc.x < 4)
				|| (agentLoc.agentDirection == 'v' && agentLoc.y > 1)
				|| (agentLoc.agentDirection == '<' && agentLoc.x > 1)
				|| (agentLoc.agentDirection == 'A' && agentLoc.y < 4)) {
				HashMap<String, Square> squares = this.getAroundSquares(this.getAgentLocation());
				Square frontSquare = squares.get("front");
				frontSquare.agentDirection = agentLoc.agentDirection;
				agentLoc.agentDirection = ' ';
			}
		}
		else {
			// if the agent turns, update the orientation
			agentLoc.agentDirection = this.getDirection(agentLoc.agentDirection, action);
		}
		
		// increase the number of visit in this current square
		agentLoc = this.getAgentLocation();
		agentLoc.numVisit += 1;
	}
	
	// update the world state based on the current percept and 
	// most recent action
	public void updateState(int action, boolean[] percepts) {
		// get the percept
		boolean bump = percepts[0];
		boolean glitter = percepts[1];
		boolean breeze = percepts[2];
		boolean stench = percepts[3];
		boolean scream = percepts[4];
		// get the square the agent is currently in
		Square agentLoc = this.getAgentLocation();
		// update the location and orientation of the agent
		// if the agent does not sense a bump
		HashMap<String, Square> squares = this.getAroundSquares(this.getAgentLocation());
		if(bump == false && action != -1) {
			if(action == Action.GO_FORWARD) {
				// if the agent goes forward, update the location
				squares.get("front").agentDirection = agentLoc.agentDirection;
				squares.get("front").isSafe = true;
				agentLoc.isSafe = true;
				agentLoc.agentDirection = ' ';
			}
			else {
				// if the agent turns, update the orientation
				agentLoc.agentDirection = this.getDirection(agentLoc.agentDirection, action);
			}
			
			// now, the location and orientation of the agent might be changed
			// so, get the new location and the squares around it
			agentLoc = this.getAgentLocation();
			squares = this.getAroundSquares(this.getAgentLocation());
		}
		
		int x = agentLoc.x;
		int y = agentLoc.y;
		// get the squares aroung the agent
		// top, left, bottom, right
		Square[] aroundSquares = {state[x][y+1], state[x-1][y], 
									state[x][y-1], state[x+1][y]};
		
		// increase the number of visit in this current square
		agentLoc.numVisit += 1;
		// update the percepts in current square
		if(stench == true) {
			// clear all squares that have Wumpus
			for(int i = 1;i < this.worldStateSize - 1; i++) {
				for(int j = 1;j < this.worldStateSize - 1; j++) {
					this.state[i][j].isWumpus = 0;
				}
			}
			agentLoc.hasStench = true;
			int numWall = 0;
			
			for(Square s : aroundSquares) {
				if(s.isWall || s.isSafe) {
					numWall++;
				}
			}
			for(Square s : aroundSquares) {
				if(!s.isWall && !s.isSafe) {
					s.isWumpus = 1.0/numWall;
				}
			}
		}
		else {
			agentLoc.hasStench = false;
			for(Square s : aroundSquares) {
				if(!s.isWall && !s.isSafe) {
					s.isWumpus = 0;
				}
			}
		}
		
		if(breeze == true) {
			agentLoc.hasBreeze = true;
			int numWall = 0;
			for(Square s : aroundSquares) {
				if(s.isWall || s.isSafe) {
					numWall++;
				}
			}
			for(Square s : aroundSquares) {
				if(!s.isWall && !s.isSafe) {
					s.isPit = 1.0/numWall;
				}
			}
		}
		else {
			for(Square s : aroundSquares) {
				if(!s.isWall && !s.isSafe) {
					s.isPit = 0;
				}
			}
		}
		
		if(glitter == true) {
			agentLoc.hasGlitter = true;
		}
		
		// there is no stench or breeze, mark
		// all around squares as safe
		if(stench == false && breeze == false) {
			squares.get("front").isSafe = true;
			squares.get("left").isSafe = true;
			squares.get("right").isSafe = true;
			squares.get("back").isSafe = true;
		}
		
		// if the agent senses bump, mark the square in front as Wall
		if(bump == true) {
			squares.get("front").isWall = true;
			squares.get("front").numVisit = 10;
		}
		
		// if there is a scream, mark all squares labeled as Wumpus as safe
		if(scream == true) {
			for(int i = this.worldStateSize - 1;i >= 0; i--) {
				for(int j = 0;j < this.worldStateSize; j++) {
					if(state[i][j].isWumpus > 0) {
						state[i][j].isWumpus = 0;
					}
					state[i][j].hasStench = false;
				}
			}
		}
		else {
			// there is no scream and the last action was SHOOT
			// there is no Wumpus infront of the agent, so clear
			// the square in front of the agent
			if(action == Action.SHOOT && !alreadyShot) {
				this.alreadyShot = true;
				squares.get("front").isWumpus = 0;
				if(squares.get("front").isPit == 0) {
					squares.get("front").isSafe = true;
				}
			}
		}
	}
	
	// get the squares around the agent
	public HashMap<String, Square> getAroundSquares(Square curLoc){
		HashMap<String, Square> squares = new HashMap<>();
		// get the current location
		
		// get the 4 squares around the agent
		// set their positions based on the orientation of the agent
		// Ex: if the agent is facing East, the square on the right is
		// in front, and the square on the left is in the back of the agent
		int x = curLoc.x;
		int y = curLoc.y;
		// get the squares around the agent
		Square topSquare = state[x][y+1];
		Square leftSquare = state[x-1][y];
		Square bottomSquare = state[x][y-1];
		Square rightSquare = state[x+1][y];
		// named all the squares based on the agent's orientation
		if(curLoc.agentDirection == 'A') {
			squares.put("front", topSquare);
			squares.put("left", leftSquare);
			squares.put("back", bottomSquare);
			squares.put("right", rightSquare);
		}
		else if(curLoc.agentDirection == '<') {
			squares.put("front", leftSquare);
			squares.put("left", bottomSquare);
			squares.put("back", rightSquare);
			squares.put("right", topSquare);
		}
		else if(curLoc.agentDirection == 'v') {
			squares.put("front", bottomSquare);
			squares.put("left", rightSquare);
			squares.put("back", topSquare);
			squares.put("right", leftSquare);
		}
		else if(curLoc.agentDirection == '>') {
			squares.put("front", rightSquare);
			squares.put("left", topSquare);
			squares.put("back", leftSquare);
			squares.put("right", bottomSquare);
		}
		
		return squares;
	}
	
	// get shot val
	public boolean checkArrow() {
		return this.alreadyShot;
	}
	
	// set arrow
	public void setArrow(boolean val) {
		this.alreadyShot = val;
	}
	
	// get world size
	public int getWorldSize() {
		return this.worldStateSize;
	}
	
	// check if there exists gold in the empty squares
	public boolean checkGold() {
		boolean hasGold = false, hasPath = true;
		for(int i = 1;i < this.worldStateSize - 1; i++) {
			for(int j = 1;j < this.worldStateSize - 1; j++) {
				if(state[i][j].numVisit == 0)
					hasGold = true;
			}
		}
		
		return (hasGold && hasPath);
	}
	
	// Evaluation function: evaluate the value of the current
	// state that the agent is currently in
	public double evaluationFunction(int lastAction) {
		// count number of visited squares
		double score = 1000;
		double count = 0;
		for(int i = 1;i < this.worldStateSize - 1; i++) {
			for(int j = 1;j < this.worldStateSize - 1; j++) {
				Square q = state[i][j];
				if(q.numVisit == 0 && q.isWumpus < 0.1 && q.isPit < 0.1 && !q.isWall) {
					count += q.numVisit;
				}
			}
		}
		
		Square agentLoc = this.getAgentLocation();
		HashMap<String, Square> squares = this.getAroundSquares(this.getAgentLocation());
		Square frontSquare = squares.get("front");
		Square leftSquare = squares.get("left");
		Square rightSquare = squares.get("right");
		Square backSquare = squares.get("back");
		
		// penalize the agent for entering square that may contain pit or wumpus
		if(agentLoc.isPit > 0 || agentLoc.isWumpus > 0)
			score += -(agentLoc.isPit*1000 + agentLoc.isWumpus*1000);
		
		// reward the agent for exploring unvisited square
		if(agentLoc.numVisit == 1)
			score += 1;
		
		// The agent is penalized for being in a square 
		// for multiple time steps
		score -= agentLoc.numVisit;
		
		// reward the agent for facing the squares that are not obstacles
		if(!frontSquare.isWall && frontSquare.isWumpus < 0.1 && frontSquare.isPit < 0.1)
			score += 1;
		
		// the agent is rewarded for doing nothing if it is surrounded by obstacles
		// and is penalized for not proceeding if there is a safe square ahead
		if(lastAction == Action.NO_OP) {
			if((frontSquare.isWall || frontSquare.isWumpus > 0 || frontSquare.isPit > 0)){
				score -= 1;
				if((leftSquare.isWall || leftSquare.isWumpus > 0 || leftSquare.isPit > 0)
					&& (rightSquare.isWall || rightSquare.isWumpus > 0 || rightSquare.isPit > 0)
					&& (backSquare.isWall || backSquare.isWumpus > 0 || backSquare.isPit > 0)) {
					score += 1;
				}
			}
			else if(frontSquare.isSafe)
				score -= 1;
		}
		
		// penalize the agent for going into the wall
		if(frontSquare.isWall && lastAction == Action.GO_FORWARD)
			score -= 1;

		// reward the agent if it grabs when senses glitter
		// penalize it otherwise
		if(agentLoc.hasGlitter) {
			if(lastAction == Action.GRAB) 
				score += 1000;
			else
				score -= 1000;
		}
		else {
			if(lastAction == Action.GRAB)
				score -= 1000;
		}

		// reward the agent for shooting if there is wumpus in front
		if(lastAction == Action.SHOOT) {
			if(this.alreadyShot == false && frontSquare.isWumpus > 0) {
				score += frontSquare.isWumpus*1000;
			}
			else if(this.alreadyShot == true) {
				score -= frontSquare.isWumpus*1000;
			}
			score -= 10;
		}
		
		// all actions cost 1 point except NO_OP
		if(lastAction != Action.NO_OP) {
			score -= 1;
		}
		return score;
	}
	
	// return a copy of the world state
	public WorldState copyWorldState() {
		Square[][] copyState = new Square[this.worldStateSize][this.worldStateSize];
		for(int i = 0;i < this.worldStateSize; i++) {
			for(int j = 0;j < this.worldStateSize; j++) {
				copyState[i][j] = this.state[i][j].copySquare();
			}
		}
		
		WorldState copyWorldState = new WorldState(copyState, this.alreadyShot);
		return copyWorldState;
	}
	
	private double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	}
}
