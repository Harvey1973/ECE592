package finalh;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

import finalh.Robot;

import robocode.RobocodeFileOutputStream;


public class LUT {
	public static double[][] Q_table;
	// hyper parameters
	public static final double alpha = 0.15;
	public static final double gamma = 0.9;
	public static double epsilon = 0.1;
	
	// state place holder 
	private int previousState;
	private int previousAction;
	public LUT (){
		Q_table = new double[Robot.State_num][Robot.Action_num];
		initialize_Q_table();
	}
	
	public void initialize_Q_table(){
		for (int i=0; i<Robot.State_num; i++)
		for (int j=0; j<Robot.Action_num; j++)
		Q_table[i][j]=0.0;
	}
	public void Q_learning(int currentState, int currentAction, double reward, boolean is_terminal){
		
		double previousQ = Q_table[previousState][previousAction];
		if (is_terminal == false) {
			//System.out.println("previous Q" + Double.toString(previousQ));
			if(Robot.offpolicy==1){
				double currentQ= previousQ+alpha*(reward + gamma*maxQ(currentState)-
						previousQ );
				Q_table[previousState][previousAction]=currentQ;
				//System.out.println("current Q" + Double.toString(currentQ));
				//System.out.println("The difference between previous Q and current Q is" + Double.toString(currentQ-previousQ));
			}
			else{
				double currentQ = previousQ+alpha*(reward + gamma *
						Q_table[currentState][currentAction]-previousQ);
				Q_table[previousState][previousAction]=currentQ;
			}
		}else {
			double currentQ = (1-alpha)*previousQ + alpha* reward;
			Q_table[previousState][previousAction] = currentQ;
		}
		
		previousState=currentState;
		previousAction=currentAction;
	}
	

	public int selectAction(int state) {
		
		if (Math.random()<epsilon){
			System.out.println("I am taking random actions");
			return randInt(0,4); //exploration: random
		}
		else{
			System.out.println("I am taking greedy actions");
			return argmax(state); //exploration: greedy
		}
	
	

	} // works
	
	public int argmax(int state) {
		// maybe add random permutation later
		double maxinum = Double.NEGATIVE_INFINITY;
		int bestAction = 0;
		for (int i = 0; i < Q_table[state].length; i++) {
			double qValue = Q_table[state][i];
			if (qValue > maxinum) {
				maxinum = qValue;
				bestAction = i;
			}
		}
		return bestAction;
	}// works 
	
	public double maxQ(int state){
		double Qmax = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < Q_table[state].length; i++) {
			if (Q_table[state][i] > Qmax)
				Qmax = Q_table[state][i];
		}
		return Qmax;
	}// works
	/* used in exlpore mode to randomly pick  actions  */
	public int randInt(int min, int max) {


	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}

	public void saveData(File file){
		PrintStream w = null;
		try {
			w = new PrintStream(new RobocodeFileOutputStream(file));
			for (int i = 0; i < Robot.State_num; i++)
				for (int j = 0; j < Robot.Action_num; j++)
					w.println(new Double(Q_table[i][j]));
			if (w.checkError())
				System.out.println("Could not save the data!");
			w.close();
		}
		catch (IOException e) {
			System.out.println("IOException trying to write: " + e);
		}
		finally {
			try {
				if (w != null)
					w.close();
				}
			catch (Exception e) {
				System.out.println("Exception trying to close witer: " + e);
			}
		}
	}// working 
	
	public void loadData(File file){
		BufferedReader r = null;
		try{
			r = new BufferedReader(new FileReader(file));
			for (int i = 0; i < Robot.State_num; i++)
				for (int j = 0; j < Robot.Action_num; j++)
					Q_table[i][j] = Double.parseDouble(r.readLine());
		}
		catch (IOException e) {
			System.out.println("IOException trying to open reader: " + e);
			initialize_Q_table();
		}
			catch (NumberFormatException e) {
				initialize_Q_table();
		}
		finally {
			try {
				if (r != null)
					r.close();
			}
			catch (IOException e) {
				System.out.println("IOException trying to close reader: " + e);
			}
		}
	} // working 

	
	
	
	
}
