/*
 * Class that defines the agent function.
 * 
 * Written by James P. Biagioni (jbiagi1@uic.edu)
 * for CS511 Artificial Intelligence II
 * at The University of Illinois at Chicago
 * 
 * Last modified 2/19/07 
 * 
 * DISCLAIMER:
 * Elements of this application were borrowed from
 * the client-server implementation of the Wumpus
 * World Simulator written by Kruti Mehta at
 * The University of Texas at Arlington.
 * 
 */
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class AgentFunction {
	
	// string to store the agent's name
	// do not remove this variable
	private String agentName = "green-frog";
	
	// all of these variables are created and used
	// for illustration purposes; you may delete them
	// when implementing your own intelligent agent
	private int[] actionTable;
	private boolean bump;
	private boolean glitter;
	private boolean breeze;
	private boolean stench;
	private boolean scream;
	private Random rand;
	private WorldState state;
	// 
	private int lastAction = -1;
	
	//*********************************
	// Use these 2 variables to set the number of runs
	// and whether the output of each run is printed out
	static boolean debugMode = false;
	static int trial = 10000;
	//*********************************
	
	public AgentFunction()
	{
		// for illustration purposes; you may delete all code
		// inside this constructor when implementing your 
		// own intelligent agent

		// this integer array will store the agent actions
//		actionTable = new int[6];
//				  
//		actionTable[0] = Action.NO_OP;
//		actionTable[1] = Action.GO_FORWARD;
//		actionTable[2] = Action.TURN_RIGHT;
//		actionTable[3] = Action.TURN_LEFT;
//		actionTable[4] = Action.GRAB;
//		actionTable[5] = Action.SHOOT;
//		
		// new random number generator, for
		// randomly picking actions to execute
		rand = new Random();
		
		state = new WorldState();
		if(debugMode == true)
			state.printState();
	}
	
	public int process(TransferPercept tp)
	{
		// To build your own intelligent agent, replace
		// all code below this comment block. You have
		// access to all percepts through the object
		// 'tp' as illustrated here:
		
		// read in the current percepts
		bump = tp.getBump();
		glitter = tp.getGlitter();
		breeze = tp.getBreeze();
		stench = tp.getStench();
		scream = tp.getScream();
		
		if(glitter == true) {
			return Action.GRAB;
		}
		// update the state based on current percept
		// and the most recent action
		boolean[] percepts = {bump, glitter, breeze, stench, scream};
		this.state.updateState(lastAction, percepts);
		if(debugMode == true)
			this.state.printState();
		// return action to be performed
		MCTS mcts = new MCTS(this.state, 10);// world state, number of iterations
		mcts.buildSearchTree();
		//lastAction = this.getAction();
		//if(lastAction < 0)
		lastAction = mcts.getBestAction();
		//lastAction = this.actionRules();
		return lastAction;
	}
	
	private int getAction() {
		// get the squares around the agent
		Square agentLoc = this.state.getAgentLocation();
		HashMap<String, Square> squares = this.state.getAroundSquares(agentLoc);
		Square frontSquare = squares.get("front");
		Square leftSquare = squares.get("left");
		Square rightSquare = squares.get("right");
		Square backSquare = squares.get("back");
		boolean takeRisk = false;
		if(frontSquare.isPit > 0) {
			// if the agent is stuck(either walls, pits or wumpus surround), do nothing
			if((leftSquare.isPit > 0 || leftSquare.isWall)
					&& (rightSquare.isPit > 0 || rightSquare.isWall)
					&& (backSquare.isPit > 0 || backSquare.isWall)) {
				if(agentLoc.hasStench == true) {
					return rand.nextBoolean() ? Action.GO_FORWARD : Action.NO_OP;
				}
			}
			else {
				// count the number of pits around the agent
				// if the number is greater than 1, and the agent
				// has been in the current square for more than 
				// 8 time steps the agent will start to take
				// the risk 
				int count = 0;
				if(leftSquare.isPit > 0) {
					count++;
				}
				if(rightSquare.isPit > 0) {
					count++;
				}
				if(backSquare.isPit > 0) {
					count++;
				}
				if(frontSquare.isPit > 0) {
					count++;
				}
				if(count > 2
					&& agentLoc.numVisit > 8) {
					takeRisk = true;
				}
			}
			
			// The agent choose the square that has lower
			// chance of having pit to go forward
			if(frontSquare.isPit < 0.26
				&& (leftSquare.isPit > 0.4 || rightSquare.isPit > 0.4) 
				&& takeRisk == true) {
				return Action.GO_FORWARD;
			}
		}
		
		return -1;
	}
	
	private int actionRules() {
		// get the squares around the agent
		Square agentLoc = this.state.getAgentLocation();
		HashMap<String, Square> squares = this.state.getAroundSquares(agentLoc);
		Square frontSquare = squares.get("front");
		Square leftSquare = squares.get("left");
		Square rightSquare = squares.get("right");
		Square backSquare = squares.get("back");
		
		
		// if there's no gold, stop exploring the environment
		if(this.state.checkGold() == false) {
			return Action.NO_OP;
		}
		
		// if there is a wall in front, either turn left or right
		if(frontSquare.isWall) {
			return rand.nextBoolean() ? Action.TURN_LEFT : Action.TURN_RIGHT;
		}
		
		// if there is a chance of having wumpus in front
		// and the agent still have arrow, it will shoot
		if(frontSquare.isWumpus > 0 && !this.state.checkArrow()) {
			return Action.SHOOT;
		}
		
		// if there is either a pit or wumpus in front
		if(frontSquare.isWumpus > 0 || frontSquare.isPit > 0) {
			boolean takeRisk = false;
			
			// if the agent is stuck(either walls, pits or wumpus surround), do nothing
			if((leftSquare.isPit > 0 || leftSquare.isWumpus > 0 || leftSquare.isWall)
					&& (rightSquare.isPit > 0 || rightSquare.isWumpus > 0 || rightSquare.isWall)
					&& (backSquare.isPit > 0 || backSquare.isWumpus > 0 || backSquare.isWall)) {
				return Action.NO_OP;
			}
			else {
				// count the number of pits around the agent
				// if the number is greater than 1, and the agent
				// has been in the current square for more than 
				// 8 time steps the agent will start to take
				// the risk 
				int count = 0;
				if(leftSquare.isPit > 0) {
					count++;
				}
				if(rightSquare.isPit > 0) {
					count++;
				}
				if(backSquare.isPit > 0) {
					count++;
				}
				if(frontSquare.isPit > 0) {
					count++;
				}
				if(count > 2
					&& agentLoc.numVisit > 8) {
					takeRisk = true;
				}
			}
			
			// The agent choose the square that has lower
			// chance of having pit to go forward
			if(frontSquare.isPit < 0.26
				&& (leftSquare.isPit > 0.25 || rightSquare.isPit > 0.25) 
				&& takeRisk == true) {
				return Action.GO_FORWARD;
			}
			else {
				return rand.nextBoolean() ? Action.TURN_LEFT : Action.TURN_RIGHT;
			}
		}
		else {
			// if the left, right and back of the agent have obstacles, 
			// the agent will go forward
			if((leftSquare.isPit > 0 || leftSquare.isWumpus > 0 || leftSquare.isWall)
					&& (rightSquare.isPit > 0 || rightSquare.isWumpus > 0 || rightSquare.isWall)
					&& (backSquare.isPit > 0 || backSquare.isWumpus > 0 || backSquare.isWall)) {
				return Action.GO_FORWARD;
			}
			
			// The agent will compare the number of visits it has
			// made to the squares in front, left and right, it
			// will turn or go forward to the square with the
			// least number of visits
			if(frontSquare.numVisit <= rightSquare.numVisit
				&& frontSquare.numVisit <= leftSquare.numVisit) {
				return Action.GO_FORWARD;
			}
			if(leftSquare.numVisit < frontSquare.numVisit 
				&& leftSquare.numVisit < rightSquare.numVisit
				&& leftSquare.numVisit < frontSquare.numVisit) {
				return Action.TURN_LEFT;
			}
			if(rightSquare.numVisit < leftSquare.numVisit 
				&& rightSquare.numVisit < frontSquare.numVisit) {
				return Action.TURN_RIGHT;
			}
			else {
				// otherwise, the agent will proceed based on the number of visits
				// For example, if the number of visits of the square in front
				// of the agent is 8, there is 20% chance that the agent will 
				// go forward to this square, and it will turn left or right 80% time
				if(rand.nextInt(10) < frontSquare.numVisit)
					return rand.nextBoolean() ? Action.TURN_LEFT : Action.TURN_RIGHT;
				else
					return Action.GO_FORWARD;
			}
		}
	}
	
	// public method to return the agent's name
	// do not remove this method
	public String getAgentName() {
		return agentName;
	}
	
	
}