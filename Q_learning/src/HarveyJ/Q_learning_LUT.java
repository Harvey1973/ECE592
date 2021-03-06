package HarveyJ;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;
import robocode.*;
import java.awt.geom.Point2D;
import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobocodeFileOutputStream;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;

public class Q_learning_LUT extends AdvancedRobot {
	public double PI = Math.PI;
	double y_test;
	double x_test;
	double dist_test;
	double bearing_test;
	private byte scanDirection = 1;
	private AdvancedEnemyBot enemy = new AdvancedEnemyBot();

	// state representations will be a 4 tuple{x_a,y_a,distance_to_enemy,Bearing}
	// below is the quantized states
	int x = 0;// x _coordinates are quantized in to integers in range [0,8] representing 0-800 pixels
	int y = 0;
	int dist = 0;// distance is quantized in 10 levels 
	int bearing = 0;
	double getBearing ;
	double Velocity ;
	double turnGunAmt;
	String current_state = null;
	int current_state_index = 0;
	String next_state = null;
	int next_state_index = 0;
	int max_q_action;
	double current_q = 0.0;
	double next_q = 0.0;
	double reward = 0.0;
	double total_reward_per_action = 0.0;
	double cum_reward;
	double [] reward_array = new double [3000];   // record rewards for multiple battles
	// hyper paramaters 
	double alpha = 0.05;  // learning rate
	double gamma = 0.99;  // discount factor
	double epsilon = 0.1;
	double [] current_state_action = new double[5];    //available actions for one particular state 
	static int row_num = 8*6*10*4;
	static int col_num = 6;
	boolean initialize = false;
	boolean explore_mode = true;
	boolean greedy_mode = false;
	String [][] Q_table = new String [row_num][col_num];  // This Q_table is a String matrix, use this to save Q_table on disk 
	double [][] Q_table_double = new double[row_num][col_num]; // This Q_table is a double matrix , use this to perform numeric operations 
	int index1 = 0;
	public void initialize_Q_table(String[][]Q_table) {
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
				}// Initializing function works
	

	public void save_table() {

		PrintStream table = null;
		try {
			table = new PrintStream(new RobocodeFileOutputStream(getDataFile("Q_table_eplison0.1_3000_run.txt")));
			for (int i=0;i<Q_table.length;i++) {
				table.println(Q_table[i][0]+"    "+Q_table[i][1]+"    "+Q_table[i][2]+"    "+Q_table[i][3]+"    "+Q_table[i][4]+"    "+Q_table[i][5]);
			}
		} catch (IOException e) {
			
		}
		table.flush();
		table.close();
		

	} // save function works
	
	public void load1() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(getDataFile("reward_3000.txt")));
		String line ;
		//System.out.println("the length of reward array is" + reward_array.length);
		try {
		    int count_2=0;
		    while ((line= br.readLine()) != null) {
		    	String splitLine[] = line.split("    ");

				//System.out.println("reward_array[m] is " + reward_array[count_2]);
				//System.out.println("splitline[m] is " + splitLine[0]);
				reward_array[count_2]=Double.parseDouble(splitLine[0]); 
				count_2+=1;
				
				//System.out.println("reward_array[m] has PRINTED OUT  " + count_2 + "	times" );
		    	
		    }
		    //System.out.println("the last line loaded Q table entry is 	 "+ Arrays.toString(Q_table[count_2-1]));
		    
		} catch (IOException e) {
			
		}
			br.close();

		}// load function works
		public void save1() {

			PrintStream w = null;
			try {
				w = new PrintStream(new RobocodeFileOutputStream(getDataFile("reward_3000.txt")));
				for (int i=0;i<reward_array.length;i++) {
					w.println(reward_array[i]);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				w.flush();
				w.close();
			}

		}//save	
public void onRoundEnded(RoundEndedEvent e_1) {
	System.out.println("cumulative reward of one full battle is ");	
	System.out.println(cum_reward);
	System.out.println("index number ");	
	System.out.println(getRoundNum());
	try{
		load1();
	}	catch (IOException e) {
		
	}
	/*
	System.out.println("The previous reward array is  ");
	for(int i=0;i<reward_array.length;i++){
		System.out.println(reward_array[i]);
		System.out.println();
	}
	*/
	reward_array[getRoundNum()]=cum_reward;
	save1();
	try{
		load1();
	}
	catch (IOException e) {
		
	}
	/*
	System.out.println("The current reward array is");
	for(int i=0;i<reward_array.length;i++){
		System.out.println(reward_array[i]);
		System.out.println();
	}
	
	*/
	index1=index1+1;
	
	   }
	
	public void load() throws IOException {
	BufferedReader br = new BufferedReader(new FileReader(getDataFile("Q_table_eplison0.1_3000_run.txt")));
	String line ;
	try {
        int count_2=0;
        while ((line= br.readLine()) != null) {
        	String splitLine[] = line.split("    ");
			for (int m =0 ; m<Q_table[0].length;m++){
				Q_table[count_2][m]=splitLine[m]; 
				
			}
        	count_2+=+1;
        	
        }
        //System.out.println("the last line loaded Q table entry is 	 "+ Arrays.toString(Q_table[count_2-1]));
        
	} catch (IOException e) {
		
	}
		br.close();
	
}// load function works
	
	public void run() {
		if(initialize) {
		initialize_Q_table(Q_table);
		save_table();
		}
		initialize = false;
		try {
		load();

		} catch(IOException e) {
			
		}
		//setAdjustRadarForRobotTurn(true);   /// radar set up 
		//setAdjustGunForRobotTurn(true);     // gun set up
		//setTurnRadarRight(360);  // this allows the robot to perform actual scanning 
		turnGunRight(360);
		while(true) {
			save_table(); // make sure to save table every time we update the Q_values
			System.out.println("initialization flag is		 "+ initialize);
			//System.out.println("loaded Q table entry is 	 "+ Q_table[0][1]);
			

		 
			try {
				load();		// load the Q_table from disk   --- necessary ?

				} catch(IOException e) {
					
				}
			

			//setTurnRadarRight(360);
			for (int i=0;i<Q_table.length;i++) {
				for (int j=0; j<Q_table[0].length;j++) {
					
					Q_table_double[i][j] = Double.parseDouble(Q_table[i][j]);
				}
			}
			//Q_learning starts
			
			//step 1 get initial state ----works
			
			current_state = x+""+y+""+dist+""+bearing;
			//current_state= "0000";
			System.out.println("current_state is		 "+ current_state);
			// step 2 , based on current state , get the Q_value for  available actions ------works
			current_state_action = choose_action(current_state,Q_table);
			
			System.out.println("current_state_actions is		 "+ Arrays.toString(current_state_action));
			//step 3  find the action that would result in maximum Q_value    ---- works
			if(Math.random()>epsilon) {
			max_q_action = argmax(current_state_action);
			System.out.println("I am taking greedy action");
			}
			else  {
			System.out.println("I am taking random action");
			max_q_action = randInt(1,5);
			}
			System.out.println("max action index is		 "+ max_q_action);
			
			// find current state index and current q value  --- works
			current_state_index =state_index(current_state,Q_table);
			current_q = Double.parseDouble(Q_table[current_state_index][max_q_action]);
			System.out.println("current_state index 	is		 "+ current_state_index);
			System.out.println("current_q value is 		 "+ current_q);
			total_reward_per_action = 0.0;
			
			// step 4 take action and observe reward  ---- works
			take_action(max_q_action);

			// step 5 , after taking action , register the new state ---- works
			turnGunRight(360);
			//setTurnRadarRight(360); 
			//doGun();
			//execute();     // carry out all queued actions
			//setTurnRadarRight(360);
			next_state = x+""+y+""+dist+""+bearing;
			System.out.println("next_state is		 "+ next_state);
			//step 6 perform update Q(s,a) = Q(s,a) + alpha*(reward+gamma*Q(s',a')-Q(s,a))

			// find next state index ---works
			next_state_index = state_index(next_state,Q_table);
			System.out.println("next_state index 	is		 "+ next_state_index);
			next_q = max((Q_table_double[next_state_index]));
			System.out.println("next_q 	is		 "+ next_q);
			//perform the update
			System.out.println("The observed reward is 		 "+ total_reward_per_action);
			Q_table_double[current_state_index][max_q_action] += alpha*(total_reward_per_action+gamma*next_q-current_q);
			System.out.println("updated Q value 	is		 "+ Q_table_double[current_state_index][max_q_action]);
			System.out.println("The difference of old Q value and updated Q value is		 "+(Q_table_double[current_state_index][max_q_action]-current_q));
			
			Q_table[current_state_index][max_q_action] = Double.toString(Q_table_double[current_state_index][max_q_action]); // -- works 
			cum_reward += total_reward_per_action;	
			
			
			
			
		}

		
	}
	//robotStatus.getX();
	public void onScannedRobot(ScannedRobotEvent e) {

		x_test = getX();
		y_test = getY();
		dist_test = e.getDistance();
		bearing_test = e.getBearing();
		Velocity =e.getVelocity();

		x = quantize_position(getX());
		y = quantize_position(getY());
		dist = quantize_distance(e.getDistance());
		bearing = quantize_bearing(e.getBearing());

		if ( enemy.none() || e.getDistance() < enemy.getDistance() - 70 ||
				e.getName().equals(enemy.getName())) {

			// track him using the NEW update method
			enemy.update(e, this);
		}
		

		if (dist==1) {
			fire(3);
		}
		//else if(dist ==2) {
		//	fire(1);
		//}
	//	else if(dist ==3) {
	//		fire(1);
	//	}
		
		


		
		
	}
	public void onRobotDeath(RobotDeathEvent e) {
		// see if the robot we were tracking died
		if (e.getName().equals(enemy.getName())) {
			enemy.reset();
		}
	}
	void doGun() {

		// don't shoot if I've got no enemy
		if (enemy.none())
			return;

		// calculate firepower based on distance
		double firePower = Math.min(500 / enemy.getDistance(), 3);
		// calculate speed of bullet
		double bulletSpeed = 20 - firePower * 3;
		// distance = rate * time, solved for time
		long time = (long)(enemy.getDistance() / bulletSpeed);

		// calculate gun turn to predicted x,y location
		double futureX = enemy.getFutureX(time);
		double futureY = enemy.getFutureY(time);
		double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);
		// non-predictive firing can be done like this:
		//double absDeg = absoluteBearing(getX(), getY(), enemy.getX(), enemy.getY());

		// turn the gun to the predicted x,y location
		setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));

		// if the gun is cool and we're pointed in the right direction, shoot!
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
			setFire(firePower);
		}
	}
	// computes the absolute bearing between two points
	double absoluteBearing(double x1, double y1, double x2, double y2) {
		double xo = x2-x1;
		double yo = y2-y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
		double arcSin = Math.toDegrees(Math.asin(xo / hyp));
		double bearing = 0;

		if (xo > 0 && yo > 0) { // both pos: lower-Left
			bearing = arcSin;
		} else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
			bearing = 360 + arcSin; // arcsin is negative here, actually 360 - ang
		} else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
			bearing = 180 - arcSin;
		} else if (xo < 0 && yo < 0) { // both neg: upper-right
			bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
		}

		return bearing;
	}

	// normalizes a bearing to between +180 and -180
	double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}
	
	/* used in exlpore mode to randomly pick  actions  */
	public int randInt(int min, int max) {


	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
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
	public  double[] choose_action(String state,String[][] Q_table){
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
	public  double max(double[] array) {
		double largest = -99999999999.0;
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
	public  int argmax(double[] array) {
		int index = 1;   // if the row is all zeros then just take the first action 
		double largest = -99999999;
		// starts with index 1 because the first element is the state number ,but we are only 
		// interested in the actions 
		if (all_zero(array)){
			index = randInt(1,5);
		}
		else {
		for ( int i = 1; i < array.length; i++ )
		{
		    if ( array[i] > largest )
		    {
		        largest = array[i];
		        index = i;
		    }
		}
		
		}
		return index ;
		
	}
	public boolean all_zero(double array[]) {
		boolean all0 = true;
		for (int i = 1 ; i< array.length;i++) {
			if(array[i]!=0) {
				all0 = false;
			}
		}
		return all0;
	}
	public void take_action(int action_index) {
		// circle our enemy
		int moveDirection=+1; 
		int moveDirection_1=-1;
		if(action_index ==1) {
			ahead(100);
			/*
			if (Velocity == 0)
				moveDirection *= 1;
			setTurnRight(getBearing + 90);
			setAhead(150 * moveDirection);
			*/
		}
		else if (action_index==2) {
			back(100);
			/*
		    moveDirection_1=-1;  
			if (Velocity == 0)
				moveDirection_1 *= 1;
			setTurnRight(getBearing + 90);
			setAhead(150 * moveDirection_1);
			*/
			
		}
		else if(action_index==3) {
			/*
			turnGunRight(turnGunAmt); // Try changing these to setTurnGunRight,
			turnRight(bearing_test-25); // and see how much Tracker improves...
			// (you'll have to make Tracker an AdvancedRobot)
			 */ 
			turnLeft(90); 
			ahead(100);
			
			
		}
		else if(action_index==4) {
			/*
			turnGunRight(turnGunAmt); // Try changing these to setTurnGunRight,
			turnRight(bearing_test-25); // and see how much Tracker improves...
			// (you'll have to make Tracker an AdvancedRobot)
			 */
			turnRight(90);
			ahead(100);
			
		}
		else if(action_index == 5) {
			turnLeft(180);
			ahead(100);
		}
	} // take action works
	public int quantize_position(double x_coor) {
		int quantized = 0;
		//System.out.println("The argument I receive is "+ x_coor);
		if((x_coor>=0)&&(x_coor<100)) {
			quantized = 0;
		}
		else if((x_coor>=100)&&(x_coor<200)) {
			quantized = 1 ;
		}
		else if((x_coor>=200)&&(x_coor<300)) {
			quantized = 2 ;
		}
		else if((x_coor>=300)&&(x_coor<400)) {
			quantized = 3 ;
		}
		else if((x_coor>=400)&&(x_coor<500)) {
			quantized = 4 ;
		}
		else if((x_coor>=500)&&(x_coor<600)) {
			quantized = 5 ;
		}
		else if((x_coor>=600)&&(x_coor<700)) {
			quantized = 6 ;
		}
		else if((x_coor>=700)&&(x_coor<800)) {
			quantized = 7 ;
		}

		return quantized;
	} // quantized coordinates works
	
	public int quantize_bearing(double bearing_angle) {
		if ((bearing_angle>=0)&&(bearing_angle<90)){
			bearing= 0;
			
		}
		else if ((bearing_angle>=90)&&(bearing_angle<=180)){
			bearing= 1;
			
		}
		else if ((bearing_angle<0)&&(bearing_angle>=-90)){
			bearing= 2;
			
		}
		else if ((bearing_angle<-90)&&(bearing_angle>=-180)){
			bearing= 3;
			
		}
		return bearing;
		
	}// quantized bearing works
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
	// reward functions 
	public void onHitRobot(HitRobotEvent event){
		double reward_1 =-2;
		total_reward_per_action += reward_1;
		System.out.println("HITT robot");
		} 
	public void onBulletHit(BulletHitEvent event){
		double reward_2=3;
		total_reward_per_action += reward_2;
		System.out.println("HITTING BULLET");
		} 
	public void onHitByBullet(HitByBulletEvent event){
		double reward_3=-3;
		total_reward_per_action += reward_3;
		System.out.println(" GETTING HIT BY BULLET ");
		//turnLeft(90);
		//ahead(100);
		}
	//wall smoothing (To make sure RL robot does not get stuck in the wall)
	public void onHitWall(HitWallEvent e){
		double reward_4=-2.0;//earlier it was -2
		total_reward_per_action += reward_4;
		System.out.println(" HITTING A WALL ");
		double xPos=this.getX();
		double yPos=this.getY();
		double width=this.getBattleFieldWidth();
		double height=this.getBattleFieldHeight();
		if(yPos<80)//too close to the bottom
		{
			
			turnLeft(getHeading() % 90);
			//System.out.println("Get heading");
			//System.out.println(getHeading());
			if(getHeading()==0){turnLeft(0);}
			if(getHeading()==90){turnLeft(90);}
			if(getHeading()==180){turnLeft(180);}
			if(getHeading()==270){turnRight(90);}
			ahead(150);
			System.out.println("Too close to the bottom!!!!!!!");
			if ((this.getHeading()<180)&&(this.getHeading()>90))
			{
				this.setTurnLeft(90);
			}
			else if((this.getHeading()<270)&&(this.getHeading()>180))
			{
				this.setTurnRight(90);
			}
			
			
		}
		else if(yPos>height-80){ //to close to the top
			System.out.println("Too close to the Top!!!!!!!");
			if((this.getHeading()<90)&&(this.getHeading()>0)){this.setTurnRight(90);}
			else if((this.getHeading()<360)&&(this.getHeading()>270)){this.setTurnLeft(90);}
			turnLeft(getHeading() % 90);
			//System.out.println("Get heading");
			//System.out.println(getHeading());
			if(getHeading()==0){turnRight(180);}
			if(getHeading()==90){turnRight(90);}
			if(getHeading()==180){turnLeft(0);}
			if(getHeading()==270){turnLeft(90);}
			ahead(150);
			
		}
		else if(xPos<80){
			turnLeft(getHeading() % 90);
			//System.out.println("Get heading");
			//System.out.println(getHeading());
			if(getHeading()==0){turnRight(90);}
			if(getHeading()==90){turnLeft(0);}
			if(getHeading()==180){turnLeft(90);}
			if(getHeading()==270){turnRight(180);}
			ahead(150);
		}
		else if(xPos>width-80){
			turnLeft(getHeading() % 90);
			//System.out.println("Get heading");
			//System.out.println(getHeading());
			if(getHeading()==0){turnLeft(90);}
			if(getHeading()==90){turnLeft(180);}
			if(getHeading()==180){turnRight(90);}
			if(getHeading()==270){turnRight(0);}
			ahead(150);
		}
		
	}
}
