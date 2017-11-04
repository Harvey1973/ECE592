import java.io.IOException;
import java.io.PrintStream;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.RobocodeFileOutputStream;
import robocode.ScannedRobotEvent;
import robocode.control.events.RoundEndedEvent;

public class RL_LUT extends AdvancedRobot {
	// define state components 
	// state representations will be a 4 tuple{x_a,y_a,distance_to_enemy,Bearing}
	// x _coordinates are quantized in to integers in range [0,8] representing 0-800 pixels
	// below is the quantized states
	int x = 0;
	int y = 0;
	// distance is quantized in 10 levels 
	int dist = 0;
	int bearing = 0;
	double gamma = 0.99;
	double learning_rate = 0.1;
	//declaring states
	int[] x_a = new int[8];
	int[] y_a = new int[6];
	int[] distance = new int[10];
	int[] x_energy = new int[4];
	int[] y_energy = new int[4];
	int[] bearing_angle = new int [4];
	// declaring action space   right now : assume 5 available actions
	int[] actions = new int[5];
	double [] current_state_action = new double[5];
	int max_action_index = 0;
	double reward = 0;
	double current_q = 0.0;   // Q(s,a) where a is the action that would result in max Q value
	double next_q = 0.0;  	// Q(s',a')  is the maximum q_value given the new state
	int state_index_1 = 0;
	int state_index_2 = 0;
	int total_reward = 0;
	// Q-table initialization   2 dimension ---- rows represent state  columns represent corresponding q value for available actions
	static int row_num = 8*6*10*4;
	static int col_num = 6;
	static int count = 0;    // flag for initialization
	String [][] Q_table = new String [row_num][col_num]; 
	static double [][] Q_table_double = new double[row_num][col_num];
	String current_state = "0000";
	String next_state = "0000";
	
	public static void initialize_Q_table(String[][]Q_table) {
		System.out.println("Initializing Q Table");
		// An example entry of Q_table will be like 
		//x    y   dist  Bearing    action_1 	action_2	action_3	action_4	action_5  
		//0    0   0       0  			0.0				0.0			0.0			0.0			0.0
		int index_count = 0;
		for (int x_1=0;x_1<8;x_1++) {
			for (int y_1 =0 ; y_1<6;y_1++) {
				for (int d=0 ; d<10;d++) {
					for (int B =0 ; B<4;B++) {
							Q_table[index_count][0] = x_1 + ""+y_1 + ""+ d +""+B;		
								for (int a = 1; a<6; a++) {		
										Q_table[index_count][a] = "0";
										
									}
									index_count +=1;
								}
							}
						}
					}
				}
	public void save_table() {

		PrintStream table = null;
		try {
			table = new PrintStream(new RobocodeFileOutputStream(getDataFile("Q_table.txt")));
			for (int i=0;i<Q_table.length;i++) {
				table.println(Q_table[i][0]+"    "+Q_table[i][1]+"    "+Q_table[i][2]+"    "+Q_table[i][3]+"    "+Q_table[i][4]+"    "+Q_table[i][5]);
			}
		} catch (IOException e) {
			
		}
		table.flush();
		table.close();
		

	}
	
	public int quantize_position(double coordinates) {
		if(coordinates<=100) {
			x = 0;
			}
		else if((coordinates>100)&&(coordinates<=200)){
			x = 1;
		}
		else if((coordinates>200)&&(coordinates<=300)) {
			x = 2 ;
		}
		else if((coordinates>300)&&(coordinates<=400)) {
			x = 3 ;
		}
		else if((coordinates>400)&&(coordinates<=500)) {
			x = 4 ;
		}
		else if((coordinates>500)&&(coordinates<=600)) {
			x = 5 ;
		}
		else if((coordinates>600)&&(coordinates<=700)) {
			x = 6 ;
		}
		else if((coordinates>700)&&(coordinates<=800)) {
			x = 7 ;
		}
		return x ;
	}
	
	public int quantize_bearing(double bearing_angle) {
		if ((bearing_angle>=0)&&(bearing_angle<90)){
			bearing= 1;
			
		}
		else if ((bearing_angle>=90)&&(bearing_angle<=180)){
			bearing= 2;
			
		}
		else if ((bearing_angle<0)&&(bearing_angle>=-90)){
			bearing= 3;
			
		}
		else if ((bearing_angle<-90)&&(bearing_angle>=-180)){
			bearing= 4;
			
		}
		return bearing;
		
	}
	public int quantize_distance(double distance) {
		if((distance>=0)&&(distance<100)) {
			dist = 0;
		}
		else if((distance>=100)&&(distance<200)) {
			dist = 1 ;
		}
		else if((distance>=200)&&(distance<300)) {
			dist = 2 ;
		}
		else if((distance>=300)&&(distance<400)) {
			dist = 3 ;
		}
		else if((distance>=400)&&(distance<500)) {
			dist = 4 ;
		}
		else if((distance>=500)&&(distance<600)) {
			dist = 5 ;
		}
		else if((distance>=600)&&(distance<700)) {
			dist = 6 ;
		}
		else if((distance>=700)&&(distance<800)) {
			dist = 7 ;
		}
		else if((distance>=800)&&(distance<900)) {
			dist = 8 ;
		}
		else if((distance>=900)&&(distance<1000)) {
			dist = 9 ;
		}
		return dist;
	}
	
	public static double[] choose_action(String state,String[][] Q_table){
		int index = 0;
		for (int i=0;i<Q_table.length;i++) {

			if(Q_table[i][0].equals(state)) {
				index = i;
				System.out.println("index is"+index);
				break;
			}
		}
		return Q_table_double[index];
	}
	public static int state_index(String state,String[][] Q_table){
		int index = 0;
		for (int i=0;i<Q_table.length;i++) {

			if(Q_table[i][0].equals(state)) {
				index = i;
				
			}
		}
		return index;
	}
	
	public static int argmax(double[] array) {
		int index = 0;
		double largest = Double.MIN_VALUE;
		// starts with index 1 because the first element is the state number 
		for ( int i = 1; i < array.length; i++ )
		{
		    if ( array[i] > largest )
		    {
		        largest = array[i];
		        index = i;
		    }
		}
		return index ;
		
	}
	public static double max(double[] array) {
		double largest = Double.MIN_VALUE;
		// starts with index 1 because the first element is the state number 
		for ( int i = 1; i < array.length; i++ )
		{
		    if ( array[i] > largest )
		    {
		        largest = array[i];
		        
		    }
		}
		return largest;
	}
	//public void OnRoundEnded(RoundEndedEvent event)
	//{
	//	System.out.println("The round has ended");
	//}
	public void run() {
		if(count == 0) {
			initialize_Q_table(Q_table);
			save_table();
		}
		count = count +1;
		while(true) {
		setAhead(100);
		// convert the Q table from 2D string to 2D double in order to pick action that results in max Q value
		for (int i=0;i<Q_table.length;i++) {
			for (int j=0; j<Q_table[0].length;j++) {
				
				Q_table_double[i][j] = Double.parseDouble(Q_table[i][j]);
			}
		}
		current_state = x+""+y+""+dist+""+bearing;
		
		//	find the row corresponding to that state and retrieve the whole row 
		// e.g [1111.0, 0.0, 0.0, 0.0, 0.0, 0.0]
		current_state_action = choose_action(current_state,Q_table);
		// find the action(index) that results in maximum Q value 
		max_action_index = argmax(current_state_action);
		//take action 
		take_action(max_action_index);
		// assign new states , get reward, determine if its terminal state
		next_state = 	x+""+y+""+dist+""+bearing;	
		//perform update
		state_index_1 = state_index(current_state,Q_table);
		current_q = Q_table_double[state_index_1][max_action_index];  //Q(s,a)
		state_index_2 = state_index(next_state,Q_table);
		next_q = max(Q_table_double[state_index_2])*gamma+reward;   //Q(s',a')*gamma+reward
		Q_table_double[state_index_1][max_action_index] += learning_rate*(next_q-current_q); 
		Q_table[state_index_1][max_action_index] = Double.toString(Q_table_double[state_index_1][max_action_index]);
		total_reward += reward;	
		
	} // this closes the while loop
	}  //
	// onScannedRobot will regularly give information about the battle including the (x,y) positions,bearing of robots to determine states 
	public void onScannedRobot(ScannedRobotEvent e) {
		x = quantize_position(getX());
		y = quantize_position(getY());
		dist = quantize_distance(e.getDistance());
		bearing = quantize_bearing(e.getBearing());
	
		
	}
	public void onHitRobot(HitRobotEvent event){
		reward-=2;
		} 
	public void onBulletHit(BulletHitEvent event){
		reward+=3;
		} 
	public void onHitByBullet(HitByBulletEvent event){
		reward-=2;
		} 
	public void take_action(int action_num) {
		// preliminary actions , need to change it later 
		if (action_num==1) {
			ahead(50);
		}
		else if(action_num ==2) {
			setBack(50);
		}
		else if(action_num ==3) {
			setTurnLeft(90);
			setAhead(50);
		}
		else if(action_num==4) {
			setTurnGunRight(50);
			setAhead(50);
		}
		else if(action_num ==5) {
			fire(1);
		}
	}
	
	
}

