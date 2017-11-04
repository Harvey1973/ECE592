import robocode.Robot;
import robocode.ScannedRobotEvent;




public class test_robot extends Robot {
	public void run() {
		turnLeft(getHeading()%90);
		turnGunRight(90);
		while (true) {
			ahead(1000);
			turnRight(90);
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		fire(1);
	}
}

