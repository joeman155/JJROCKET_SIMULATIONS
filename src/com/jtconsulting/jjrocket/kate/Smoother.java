package com.jtconsulting.jjrocket.kate;

public class Smoother {
	private double mass;  // Mass of smoother. 
	private double r;     // X Location of smoother away from centre line
	private double y;     // Y Location from bottom of the rocket
	private double ang_y; // Angle about Y axis (starting from X axis)
	private double max_angular_speed; // Radians/second
	
		

	public double getMax_angular_speed() {
		return max_angular_speed;
	}
	public void setMax_angular_speed(double max_angular_speed) {
		this.max_angular_speed = max_angular_speed;
	}
	public double getMass() {
		return mass;
	}
	public void setMass(double mass) {
		this.mass = mass;
	}
	public double getR() {
		return r;
	}
	public void setR(double r) {
		this.r = r;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getAng_y() {
		return ang_y;
	}
	public void setAng_y(double ang_y) {
		this.ang_y = ang_y;
	}
	
	
	
	
	
	

}
