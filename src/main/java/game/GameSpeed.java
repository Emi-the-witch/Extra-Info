package game;

import java.io.IOException;

import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import view.keyboard.KEYS;

public final class GameSpeed {
	/////#!# actualSpeed is made public so that IManager can get the speed value.
	private boolean tmpPaused;
	private double speed;
	private double prevSpeed;
	public static double actualSpeed;
	private double actualSpeedI;
	private boolean updateOnce;
	
	public final int speed0 = 0;
	public final double speed05 = 0.25;
	public final int speed1 = 1;
	public final int speed2 = 5;
	public final int speed3 = 25;
	public final int speed4 = 500;
	
	GameSpeed(){
		
	}
	
	double update(double slowTheFuckDown) {
		if (tmpPaused)
			return clearAndReturn(0);
		
		if (updateOnce)
			return clearAndReturn(1);
		
		double s = speed;
		if (SETT.ARMIES().enemy().men() > 0)
			s = CLAMP.d(s, 0, 25);
		
		if (actualSpeed < s && slowTheFuckDown < 1) {
			actualSpeed++;
		}else if (slowTheFuckDown >= 1){
			actualSpeed /= 1.0 + (slowTheFuckDown-1.0)*0.5;
		}
		
		actualSpeed = CLAMP.d(actualSpeed, 1, s);
		if (actualSpeed > s)
			actualSpeed = s;
		if (actualSpeed < 1 && s >= 1)
			actualSpeed = 1;
		
		actualSpeedI = 1.0/CLAMP.d(actualSpeed, 1, 1000);
		
		return clearAndReturn(actualSpeed);
		
	}
	
	private double clearAndReturn(double i) {
		tmpPaused = false;
		updateOnce = false;
		return i;
	}
	
	public boolean isPaused() {
		return speed == 0 || tmpPaused;
	}
	
	public double speedTarget() {
		return tmpPaused ? 0 : speed;
	}
	
	public void updateOnce(){
		updateOnce = true;
	}
	
	public void tmpPause() {
		tmpPaused = true;
	}
	
	public void togglePause() {
		if (speed == 0) {
			speed = prevSpeed;
			
		}
			
		else{
			prevSpeed = speed;
			speed = 0;
		}
		actualSpeed = speed;
		actualSpeedI = 1.0/CLAMP.d(actualSpeed, 1, 1000);
	}
	
	public double speed() {
		return actualSpeed;
	}
	
	public double speedI() {
		return actualSpeedI;
	}
	
	public void speedSet(double speed) {
		this.prevSpeed = this.speed;
		this.speed = speed;
		this.actualSpeed = speed;
		actualSpeedI = 1.0/CLAMP.d(actualSpeed, 1, 1000);
	}

	void save(FilePutter file) {
		file.d(speed);
	}

	void load(FileGetter file) throws IOException {
		prevSpeed = file.d();
		clear();
		
	}
	
	void clear() {
		speed = 0;
		actualSpeed = 0;
		tmpPaused = false;
		updateOnce = false;
	}
	
	public void poll() {
		if (KEYS.MAIN().SPEED0.consumeClick()) {
			speedSet(speed0);
		}
		if(KEYS.MAIN().SPEED1.consumeClick()) {
			if (speed == speed1)
				speedSet(speed05);
			else
				speedSet(speed1);
		}
		if(KEYS.MAIN().SPEED2.consumeClick()) {
			speedSet(speed2);
		}
		if(KEYS.MAIN().SPEED3.consumeClick()) {
			if (speed == 25)
				speedSet(speed4);
			else
				speedSet(speed3);
		}
		if (KEYS.MAIN().PAUSE.consumeClick()){
			togglePause();
		}
	}
	
}
