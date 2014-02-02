package assignment_robots;

import java.util.Arrays;

// this class declares the configuration of a car robot;
// standard set and get function;

public class CarState {
	protected double[] s;

	public CarState () {
		s = new double[3];
		s[0] = 0;
		s[1] = 0;
		s[2] = 0;
	}

	public CarState (double x, double y, double theta) {
		s = new double[3];
		s[0] = x;
		s[1] = y;
		s[2] = theta;
	}

	public CarState (CarState cs) {
		s = new double[3];
		System.arraycopy(cs.s, 0, s, 0, 3);
	}

	public void set(double x, double y, double theta) {
		s[0] = x;
		s[1] = y;
		s[2] = theta;		
	}

	public void set(CarState cs) {
		System.arraycopy(cs.s, 0, s, 0, 3);	
	}

	@Override
	public boolean equals(Object other) {
		return Arrays.equals(s, ((CarState) other).s);
	}

	public double getX() {
		return s[0];
	}

	public double getY() {
		return s[1];
	}

	public double getTheta() {
		return s[2];
	}

	public double[] get() {
		return s;
	}
}
