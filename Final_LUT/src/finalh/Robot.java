package finalh;




import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.RobocodeFileOutputStream;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;

public class Robot extends AdvancedRobot {
	// state quantizations 
	public static int X_coor = 8;
	public static int Y_coor = 6;
	public static int Distance = 10;
	public static int Bearing = 4;
	public static int State_num = 8*6*4*10;
	public static int Action_num = 5;
	// reward definition 
	private double reward;
	private static final double win = +5;
	private static final double death = -5;
	private static final double hitEnemy= 3;
	private static final double bulletMissed = -1.5;
	private static final double hitByBullet = -3;
	private static final double hitWall = -2;
	public  boolean is_terminal = false ; 
	// LUT constructor 
	public static LUT lookuptable = new LUT();
	// enemy class 
	public static Enemy enemy ;
	// offpolicy flag 
	public static final int offpolicy=1;
	// win rate 
	public static double [] win_rate = new double [500];
	public static int win_count = 0;
	public static int index_win = 0;
	
	public class Enemy{
		public double y;
		public double x;
		public double distance;
		public double bearing;
	}
	public void run() {
		enemy = new Enemy();
		loadData();
		//setAdjustGunForRobotTurn(true);
		//setAdjustRadarForRobotTurn(true);
		turnGunRight(360);
		//turnRadarRight(360);
		while (true) {
			
			
			// choose action based on state
			//System.out.println("the current state is" + getState());
			int action = lookuptable.selectAction(getState());
			//System.out.println("the current action is " + action);
			 //perform update 
			System.out.println("the current reward is" + reward);
			System.out.println("is terminal    " + is_terminal);
			lookuptable.Q_learning(getState(), action, reward,is_terminal);
			
			// reset reward 
			reward = 0.0;
			switch (action){
			case 0:
				ahead(100);
				//setAhead(100);
				break;
			case 1:
				back(100);
				//setBack(100);
				break;
			case 2:
				//turnGunRight(360);
				//setTurnLeft(90);
				//setAhead(100);
				turnLeft(90);
				ahead(100);
			case 3:
				//turnGunRight(360);
				//setTurnRight(90);
				//setAhead(100);
				turnRight(90);
				ahead(100);
				break;
			case 4:
				//setTurnRight(180);
				//setAhead(100);
				turnRight(180);
				ahead(100);
				break;
			}
			//turnRadarRight(360); // scan after taking actions
			turnGunRight(360);
			execute();

			
		}

		
	}
	
	
	public void onScannedRobot(ScannedRobotEvent e){
		enemy.distance = e.getDistance();
		enemy.bearing = e.getBearing();
		fire(3);
	}
	
	// on round end save win rate 
	public void onRoundEnded(RoundEndedEvent e_1) {	
		//saveData();
		System.out.println("win rate is  !!!" + win_count/((double)getRoundNum()+1.0));
		if(getRoundNum() % 10 ==0) {
			win_rate[index_win] = win_count/((double)getRoundNum()+1);
			System.out.println("win rate array is" + Arrays.toString(win_rate));	
			index_win +=1;
			savewins(getDataFile("win_rate.txt"));
		}


		   }
	// reward events 
	/*
	public void onBulletHit(BulletHitEvent event){
		this.reward += hitEnemy;
	}
	public void onBulletMissed(BulletMissedEvent event){
		this.reward += bulletMissed;
	}
	public void onHitByBullet(HitByBulletEvent event){
		this.reward += hitByBullet;
	}
	public void onHitWall(HitWallEvent event){
		this.reward += hitWall;

	}
	*/
	public void onWin(WinEvent event){
		
		is_terminal = true;
		win_count += 1;
		this.reward += win;
		int action = lookuptable.selectAction(getState());
		 //perform update 
		System.out.println("is terminal    " + is_terminal);
		lookuptable.Q_learning(getState(), action, reward,is_terminal);
		this.reward = 0;
		saveData();
	}
	public void onDeath(DeathEvent event){
		is_terminal = true;
		this.reward += death;
		int action = lookuptable.selectAction(getState());
		 //perform update 
		System.out.println("is terminal    " + is_terminal);
		lookuptable.Q_learning(getState(), action, reward,is_terminal);
		this.reward = 0;
		saveData();
	}
	
	// get the current state
	public int getState(){
		int x = 0;
		int y = 0;
		int distance = 0;
		int bearing = 0;
		x = quantize_position(this.getX());
		y = quantize_position(this.getY());
		distance = quantize_distance(enemy.distance);
		bearing = quantize_bearing(enemy.bearing);
		return index(x,y,distance,bearing);
	}
	
	// get the index of state 
	static public int index( int x, int y, int distance , int bearing ){
		int index = 0;
		index = x*Bearing*Distance*Y_coor+
		y*Distance*Bearing+distance*Bearing+bearing;
		return index;
	} // working 
	// absolute bearing 
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
	// quantize state functions  
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
		int bearing = 0 ;
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
	public int quantize_absbearing(double bearing_angle) {
		int bearing = 0 ;
		if ((bearing_angle>=0)&&(bearing_angle<90)){
			bearing= 0;
			
		}
		else if ((bearing_angle>=90)&&(bearing_angle<=180)){
			bearing= 1;
			
		}
		else if ((bearing_angle>180)&&(bearing_angle<=270)){
			bearing= 2;
			
		}
		else if ((bearing_angle>270)&&(bearing_angle<=360)){
			bearing= 3;
			
		}
		return bearing;
		
	}// quantized bearing works
	public int quantize_distance(double distance) {
		int dist = 0;
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
	
	// call from LUT class
	public void saveData(){
	try {
		lookuptable.saveData(getDataFile("FinalLUT.txt"));
		}
	catch (Exception e){
		System.out.println("cannot save data for Agent ooo");
		}
	}
	
	public void loadData(){
	try{
		lookuptable.loadData(getDataFile("FinalLUT.txt"));
		}
	catch (Exception e){
		System.out.println("cannot load data for Agent ooo");
		}
	}
	
public void savewins(File file){
	PrintStream w = null;
	try {
		w = new PrintStream(new RobocodeFileOutputStream(file));
		for (int i = 0; i < win_rate.length; i++)
			w.println(new Double(win_rate[i]));
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
	}
}
