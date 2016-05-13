package com.jtconsulting.jjrocket.kate;

public class rotationData {
	
	double rot_vx; 
	double rot_vy;
	double rot_vz;
	public double getRot_vx() {
		return rot_vx;
	}
	public void setRot_vx(double rot_vx) {
		this.rot_vx = rot_vx;
	}
	public double getRot_vy() {
		return rot_vy;
	}
	public void setRot_vy(double rot_vy) {
		this.rot_vy = rot_vy;
	}
	public double getRot_vz() {
		return rot_vz;
	}
	public void setRot_vz(double rot_vz) {
		this.rot_vz = rot_vz;
	}
	
	
	public void setRotation(double x, double y, double z) {
		this.setRot_vx(x);
		this.setRot_vy(y);
		this.setRot_vz(z);
	}

}
