
public class WumpusModel {
	// Stay - North - South - East - West
	double[] pMoves;
	int step;
	int lastX = 0;
	int lastY = 0;
	WumpusModel(double stay, double north, double south, double east, double west, int step){
		pMoves = new double[5];
		pMoves[0] = stay;
		pMoves[1] = north;
		pMoves[2] = south;
		pMoves[3] = east;
		pMoves[4] = west;
		this.step = step;
	}
	
	public void printProbs() {
		if(AgentFunction.debugMode) {
			System.out.print("->Wumpus probs: ");
			for(int i = 0; i < this.pMoves.length; i++) {
				System.out.print(this.pMoves[i]);
				System.out.print(", ");
			}
			System.out.println("");
		}
	}
	
}
