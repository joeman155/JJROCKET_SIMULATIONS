package com.jtconsulting.jjrocket.kate;

import java.math.BigDecimal;

public class Motor {

	
	private double mass_nonfuel;
	private double mass_fuel;
	private double len;
	private double radius;
	private double peak_thrust;
	private double peek_thrust_start_time;
	private double peek_thrust_end_time;
	private double norm_thrust;
	private double norm_thrust_start_time;
	private double norm_thrust_end_time;
	private double ignition_delay;
	
	private double mass;
	
	
	


	public final double getRadius() {
		return radius;
	}
	public final void setRadius(double radius) {
		this.radius = radius;
	}
	public final double getMass_nonfuel() {
		return mass_nonfuel;
	}
	public final void setMass_nonfuel(double mass_nonfuel) {
		this.mass_nonfuel = mass_nonfuel;
	}
	public double getMass_fuel() {
		return mass_fuel;
	}
	public void setMass_fuel(double mass_fuel) {
		this.mass_fuel = mass_fuel;
	}
	/**
	 * @return the len
	 */
	public final double getLen() {
		return len;
	}
	/**
	 * @param len the len to set
	 */
	public final void setLen(double len) {
		this.len = len;
	}
	/**
	 * @return the peak_thrust
	 */
	public final double getPeak_thrust() {
		return peak_thrust;
	}
	/**
	 * @param peak_thrust the peak_thrust to set
	 */
	public final void setPeak_thrust(double peak_thrust) {
		this.peak_thrust = peak_thrust;
	}
	/**
	 * @return the peek_thrust_start_time
	 */
	public final double getPeek_thrust_start_time() {
		return peek_thrust_start_time;
	}
	/**
	 * @param peek_thrust_start_time the peek_thrust_start_time to set
	 */
	public final void setPeek_thrust_start_time(double peek_thrust_start_time) {
		this.peek_thrust_start_time = peek_thrust_start_time;
	}
	/**
	 * @return the peek_thrust_end_time
	 */
	public final double getPeek_thrust_end_time() {
		return peek_thrust_end_time;
	}
	/**
	 * @param peek_thrust_end_time the peek_thrust_end_time to set
	 */
	public final void setPeek_thrust_end_time(double peek_thrust_end_time) {
		this.peek_thrust_end_time = peek_thrust_end_time;
	}
	/**
	 * @return the norm_thrust
	 */
	public final double getNorm_thrust() {
		return norm_thrust;
	}
	/**
	 * @param norm_thrust the norm_thrust to set
	 */
	public final void setNorm_thrust(double norm_thrust) {
		this.norm_thrust = norm_thrust;
	}
	/**
	 * @return the norm_thrust_start_time
	 */
	public final double getNorm_thrust_start_time() {
		return norm_thrust_start_time;
	}
	/**
	 * @param norm_thrust_start_time the norm_thrust_start_time to set
	 */
	public final void setNorm_thrust_start_time(double norm_thrust_start_time) {
		this.norm_thrust_start_time = norm_thrust_start_time;
	}
	/**
	 * @return the norm_thrust_end_time
	 */
	public final double getNorm_thrust_end_time() {
		return norm_thrust_end_time;
	}
	/**
	 * @param norm_thrust_end_time the norm_thrust_end_time to set
	 */
	public final void setNorm_thrust_end_time(double norm_thrust_end_time) {
		this.norm_thrust_end_time = norm_thrust_end_time;
	}
	
	
	
	public final double getThrust(double time) {
		double thrust;
		
		
		
		time = time - this.getIgnition_delay();
		thrust = 0;
	
		if (time < 0) {
			thrust = 0;
		} else if (time < peek_thrust_start_time) {
			thrust = this.getPeak_thrust() * time / this.getPeek_thrust_start_time();
		} else if (time >= this.getPeek_thrust_start_time() && time <= this.getPeek_thrust_end_time()) {
			thrust = this.getPeak_thrust();
		} else if (time > this.getPeek_thrust_end_time() && time < this.getNorm_thrust_start_time()) {
			thrust = this.getPeak_thrust() + (this.getNorm_thrust() - this.peak_thrust) * (time - this.getPeek_thrust_end_time())/(this.getNorm_thrust_start_time() - this.getPeek_thrust_end_time());
		} else if (time >= this.getNorm_thrust_start_time() && time <= this.getNorm_thrust_end_time()) {
			thrust = this.getNorm_thrust();
		} 
		
		return thrust;
	}
	/**
	 * @return the ignition_delay
	 */
	public final double getIgnition_delay() {
		return ignition_delay;
	}
	/**
	 * @param ignition_delay the ignition_delay to set
	 */
	public final void setIgnition_delay(double ignition_delay) {
		this.ignition_delay = ignition_delay;
	}
	
	
	
	public final void updateState(BigDecimal time) {
		double mf_rate = this.getMass_fuel()/this.norm_thrust_end_time;
		double mf = this.getMass_fuel() * (1 - time.doubleValue() * mf_rate/this.norm_thrust_end_time);
		
		this.mass = this.getMass_nonfuel() + mf;
	}
	
	public final double getMass() {
		return mass;
	}
	
	
	
	
}
