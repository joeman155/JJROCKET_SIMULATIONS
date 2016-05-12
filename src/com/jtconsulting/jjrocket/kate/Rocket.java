package com.jtconsulting.jjrocket.kate;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;


public class Rocket {
	
	private double length;
	private double mass;
	private RealMatrix momentOfInertia;
	private double radius_internal;
	private double radius_external;
	private double x;
	private double y;
	private double z;
	private double vx;
	private double vy;
	private double vz;
	private double ax;
	private double ay;
	private double az;
	
	private double roll;
	private double pitch;
	private double yaw;
	
	private double ang_x, ang_y, ang_z;
	private double ang_vx, ang_vy, ang_vz;
	private double ang_ax, ang_ay, ang_az;
	
	private double roll_vel;
	private double pitch_vel;
	private double yaw_vel;
	
	private double cgx, cgy, cgz;
	
	private Motor motor;
	private Smoother smoother1;
	private Smoother smoother2;
	
	
	
	
	
	
	
	

	public Smoother getSmoother1() {
		return smoother1;
	}
	public void setSmoother1(Smoother smoother1) {
		this.smoother1 = smoother1;
	}
	public Smoother getSmoother2() {
		return smoother2;
	}
	public void setSmoother2(Smoother smoother2) {
		this.smoother2 = smoother2;
	}
	public final RealMatrix getMomentOfInertia() {
		return momentOfInertia;
	}
	public final void setMomentOfInertia(RealMatrix momentOfInertia) {
		this.momentOfInertia = momentOfInertia;
	}
	public final double getRadius_internal() {
		return radius_internal;
	}
	public final void setRadius_internal(double radius_internal) {
		this.radius_internal = radius_internal;
	}
	public final double getRadius_external() {
		return radius_external;
	}
	public final void setRadius_external(double radius_external) {
		this.radius_external = radius_external;
	}
	public final double getAng_x() {
		return ang_x;
	}
	public final void setAng_x(double ang_x) {
		this.ang_x = ang_x;
	}
	public final double getAng_y() {
		return ang_y;
	}
	public final void setAng_y(double ang_y) {
		this.ang_y = ang_y;
	}

	
	
	public final double getAng_z() {
		return ang_z;
	}
	public final void setAng_z(double ang_z) {
		this.ang_z = ang_z;
	}
	public final double getAng_vx() {
		return ang_vx;
	}
	public final void setAng_vx(double ang_vx) {
		this.ang_vx = ang_vx;
	}
	public final double getAng_vy() {
		return ang_vy;
	}
	public final void setAng_vy(double ang_vy) {
		this.ang_vy = ang_vy;
	}
	public final double getAng_vz() {
		return ang_vz;
	}
	public final void setAng_vz(double ang_vz) {
		this.ang_vz = ang_vz;
	}
	public final double getAng_ax() {
		return ang_ax;
	}
	public final void setAng_ax(double ang_ax) {
		this.ang_ax = ang_ax;
	}
	public final double getAng_ay() {
		return ang_ay;
	}
	public final void setAng_ay(double ang_ay) {
		this.ang_ay = ang_ay;
	}
	public final double getAng_az() {
		return ang_az;
	}
	public final void setAng_az(double ang_az) {
		this.ang_az = ang_az;
	}
	public final Motor getMotor() {
		return motor;
	}
	public final void setMotor(Motor motor) {
		this.motor = motor;
	}
	public double getLength() {
		return length;
	}
	public void setLength(double length) {
		this.length = length;
	}
	public double getMass() {
		return mass;
	}
	public void setMass(double mass) {
		this.mass = mass;
	}
	public final double getX() {
		return x;
	}
	public final void setX(double x) {
		this.x = x;
	}
	public final double getY() {
		return y;
	}
	public final void setY(double y) {
		this.y = y;
	}
	public final double getZ() {
		return z;
	}
	public final void setZ(double z) {
		this.z = z;
	}
	public final double getVx() {
		return vx;
	}
	public final void setVx(double vx) {
		this.vx = vx;
	}
	public final double getVy() {
		return vy;
	}
	public final void setVy(double vy) {
		this.vy = vy;
	}
	public final double getVz() {
		return vz;
	}
	public final void setVz(double vz) {
		this.vz = vz;
	}
	public final double getAx() {
		return ax;
	}
	public final void setAx(double ax) {
		this.ax = ax;
	}
	public final double getAy() {
		return ay;
	}
	public final void setAy(double ay) {
		this.ay = ay;
	}
	public final double getAz() {
		return az;
	}
	public final void setAz(double az) {
		this.az = az;
	}
	public final double getRoll() {
		return roll;
	}
	public final void setRoll(double roll) {
		this.roll = roll;
	}
	public final double getPitch() {
		return pitch;
	}
	public final void setPitch(double pitch) {
		this.pitch = pitch;
	}
	public final double getYaw() {
		return yaw;
	}
	public final void setYaw(double yaw) {
		this.yaw = yaw;
	}
	public final double getRoll_vel() {
		return roll_vel;
	}
	public final void setRoll_vel(double roll_vel) {
		this.roll_vel = roll_vel;
	}
	public final double getPitch_vel() {
		return pitch_vel;
	}
	public final void setPitch_vel(double pitch_vel) {
		this.pitch_vel = pitch_vel;
	}
	public final double getYaw_vel() {
		return yaw_vel;
	}
	public final void setYaw_vel(double yaw_vel) {
		this.yaw_vel = yaw_vel;
	}

	
	
	
	
	public final double getCgx() {
		return cgx;
	}
	public final void setCgx(double cgx) {
		this.cgx = cgx;
	}
	public final double getCgy() {
		return cgy;
	}
	public final void setCgy(double cgy) {
		this.cgy = cgy;
	}
	public final double getCgz() {
		return cgz;
	}
	public final void setCgz(double cgz) {
		this.cgz = cgz;
	}
	public final void updateRotationMotionState(double new_rotx_vel, double new_roty_vel, double new_rotz_vel,
			double time_slice) {
		// ANGULAR MOTION
		double old_ang_vx = this.getAng_vx();
		double old_ang_vy = this.getAng_vy();
		double old_ang_vz = this.getAng_vz();	
		
		// Set new Velocities
		this.setAng_vx(new_rotx_vel);
		this.setAng_vy(new_roty_vel);
		this.setAng_vz(new_rotz_vel);
		
		// Calculate average velocity over this time slice.
		double ang_vx_avg = (old_ang_vx + this.getAng_vx())/2;
		double ang_vy_avg = (old_ang_vy + this.getAng_vy())/2;
		double ang_vz_avg = (old_ang_vz + this.getAng_vz())/2;
		
		
		// Calculate the change in orientation
		this.setAng_x(this.getAng_x()   + time_slice * ang_vx_avg);
		this.setAng_y(this.getAng_y()   + time_slice * ang_vy_avg);
		this.setAng_z(this.getAng_z()   + time_slice * ang_vz_avg);
		
		
		
	}
	public final void updateState(RealVector torque_vector, RealVector thrust_force_vector, 
									double mass, double time_slice) {
			
		// Re-compute where Centre of Gravity is
		computeCg();		
		
		// Deduce current moment of inertia
		computeMomentOfInertia();


		
		
		
		// TRANSLATIONAL MOTION
		double old_vx = this.getVx();
		double old_vy = this.getVy();
		double old_vz = this.getVz();
		
		
		// Calculate Acceleration
		this.setAx(thrust_force_vector.getEntry(0)/mass);
		this.setAy(thrust_force_vector.getEntry(1)/mass);
		this.setAz(thrust_force_vector.getEntry(2)/mass);
		
		
		// Calculate the change in velocity
		this.setVx(this.getVx() + time_slice * this.getAx());
		this.setVy(this.getVy() + time_slice * this.getAy());
		this.setVz(this.getVz() + time_slice * this.getAz());
		
		
		// Calculate average velocity over this time frame, so we can then calcalte new position 
		double vx_avg = (old_vx + this.getVx())/2;
		double vy_avg = (old_vy + this.getVy())/2;
		double vz_avg = (old_vz + this.getVz())/2;
		
		// Calculate new position
		this.setX(this.getX()  + time_slice * vx_avg);
		this.setY(this.getY()  + time_slice * vy_avg);
		this.setZ(this.getZ()  + time_slice * vz_avg);
		
		
		
		
		// ANGULAR MOTION
		double old_ang_vx = this.getAng_vx();
		double old_ang_vy = this.getAng_vy();
		double old_ang_vz = this.getAng_vz();
		
		
		// Get the rotation matrix
		RealMatrix rotation_matrix = this.getRotationMatrix();
		// System.out.println("ROTATION X:  "  + rotation_matrix.getEntry(0, 0) + ", " + rotation_matrix.getEntry(0,1) + ", " + rotation_matrix.getEntry(0,2));
		// System.out.println("ROTATION Y:  "  + rotation_matrix.getEntry(1, 0) + ", " + rotation_matrix.getEntry(1,1) + ", " + rotation_matrix.getEntry(1,2));			
		// System.out.println("ROTATION Z:  "  + rotation_matrix.getEntry(2, 0) + ", " + rotation_matrix.getEntry(2,1) + ", " + rotation_matrix.getEntry(2,2));		
		
					
		// Get various matrices, that are required later on
		RealMatrix rotation_matrix_inverse = new LUDecomposition(rotation_matrix).getSolver().getInverse();
		RealMatrix rotation_matrix_inverse_transpose = rotation_matrix_inverse.transpose();
		
		// Compute new Inertia Matrix (in Global Reference System)
		RealMatrix inertia_global = rotation_matrix_inverse_transpose.multiply(this.getMomentOfInertia()).multiply(rotation_matrix_inverse);
		RealMatrix inertia_global_inverse = new LUDecomposition(inertia_global).getSolver().getInverse();		
		

		// Calculate the Angular Acceleration Vector
		RealVector rot_accel =  utils.matrixVectorMultiply(inertia_global_inverse, torque_vector);		
		
		
		// Calculate the change in Angular velocities
		this.setAng_vx(this.getAng_vx() + time_slice * rot_accel.getEntry(0));
		this.setAng_vy(this.getAng_vy() + time_slice * rot_accel.getEntry(1));
		this.setAng_vz(this.getAng_vz() + time_slice * rot_accel.getEntry(2));
		
		
		double ang_vx_avg = (old_ang_vx + this.getAng_vx())/2;
		double ang_vy_avg = (old_ang_vy + this.getAng_vy())/2;
		double ang_vz_avg = (old_ang_vz + this.getAng_vz())/2;
		

		
		// Calculate the change in orientation
		this.setAng_x(this.getAng_x()   + time_slice * ang_vx_avg);
		this.setAng_y(this.getAng_y()   + time_slice * ang_vy_avg);
		this.setAng_z(this.getAng_z()   + time_slice * ang_vz_avg);
		
		
		// Update Angular Acceleration
		this.setAng_ax(rot_accel.getEntry(0));
		this.setAng_ay(rot_accel.getEntry(1));
		this.setAng_az(rot_accel.getEntry(2));
		
		
		

		
	}
	
	
	
	
	public RealVector getDirection() {
		RealVector direction;
		double vector_mag = Math.pow(Math.cos(this.getPitch()) * Math.pow(Math.cos(this.getYaw()), 2) +  
				                     Math.pow(Math.sin(this.getPitch()), 2) +
				                     Math.cos(this.getPitch()) * Math.pow(Math.sin(this.getYaw()), 2), 
				                     0.5);
		double vector_tmp[] = {Math.cos(this.getPitch()) * Math.cos(this.getYaw())/vector_mag, Math.sin(this.getPitch())/vector_mag, Math.cos(this.getPitch()) * Math.sin(this.getYaw())/vector_mag};
		
		direction = MatrixUtils.createRealVector(vector_tmp);
		
		return direction;
	}
	
	public void setPosition(double i, double j, double k) {
		this.setX(i);
		this.setY(j);
		this.setZ(k);
		
	}

	
	public void computeCg() {
		
		double cy = (this.getMotor().getMass() * this.getMotor().getLen()/2 + this.getLength() * this.getMass()/2 + this.smoother1.getMass() * this.smoother1.getY() + this.smoother2.getMass() * this.smoother2.getY());
		cy = cy/(this.getMotor().getMass() + this.getMass() + this.smoother1.getMass() + this.smoother2.getMass());
		
		
		double c1, c2, s1, s2;
		c1 = Math.cos(this.smoother1.getAng_y());
		c1 = (double) Math.round(c1 * 1000d) / 1000d;
		c2 = Math.cos(this.smoother2.getAng_y());
		c2 = (double) Math.round(c2 * 1000d) / 1000d;
		s1 = Math.sin(this.smoother1.getAng_y());
		s1 = -(double) Math.round(s1 * 1000d) / 1000d;  // Make negative to correct direction
		s2 = Math.sin(this.smoother2.getAng_y());
		s2 = -(double) Math.round(s2 * 1000d) / 1000d;  // Make negative to correct direction
		
		double cx = (this.smoother1.getMass() * this.smoother1.getR() * c1 + this.smoother2.getMass() * this.smoother2.getR() * c2);
		cx = cx/(this.smoother1.getMass() + this.smoother2.getMass() + this.getMass() + this.motor.getMass());
		
		double cz = (this.smoother1.getMass() * this.smoother1.getR() * s1 + this.smoother2.getMass() * this.smoother2.getR() *  s2);
		cz = cz/(this.smoother1.getMass() + this.smoother2.getMass() + this.getMass() + this.motor.getMass());		
		
		// System.out.println(Math.sin(this.smoother1.getAng_y()) + "    ---  " + Math.sin(this.smoother2.getAng_y()));

		
		cx = (double) Math.round(cx * 100000d) / 100000d;
		cy = (double) Math.round(cy * 100000d) / 100000d;
		cz = (double) Math.round(cz * 100000d) / 100000d;
		
		this.setCgx(cx);
		this.setCgy(cy);
		this.setCgz(cz);				
	}
	
	
	public final void computeMomentOfInertia() {
		
		// Rocket Tube
		double ixx =       this.getMass() * (3 * (Math.pow(this.getRadius_external(),2) + Math.pow(this.getRadius_internal(),2)) + Math.pow(this.getLength(),2))/12;
		double iyy = 0.5 * this.getMass() * (Math.pow(this.getRadius_external(),2) + Math.pow(this.getRadius_internal(),2));
		double izz =       this.getMass() * (3 * (Math.pow(this.getRadius_external(),2) + Math.pow(this.getRadius_internal(),2)) + Math.pow(this.getLength(),2))/12;
		double ixy =  0.000025;
		double ixz =  0.00004;
		double iyx =  0.00003;
		double iyz = -0.000012;
		double izx =  0.00002;
		double izy = -0.000010;
		
		ixy = ixz = iyz = iyx = izx = izy = 0;
		
		double rocket_distance_from_cg = this.getLength()/2 - this.getCgy();
		ixx = ixx + this.getMass() * Math.pow(rocket_distance_from_cg,  2);
		izz = izz + this.getMass() * Math.pow(rocket_distance_from_cg,  2);
		
		
		// Engine (e = Engine)
		double ixxe = this.getMotor().getMass() * (3 * Math.pow(this.getMotor().getRadius(),  2) + Math.pow(this.getMotor().getLen(),  2))/12;
		double iyye = 0.5 * this.getMotor().getMass() * this.getMotor().getRadius() * this.getMotor().getRadius();
		double izze = ixxe; 

		double engine_distance_from_cg = this.getCgy() - this.getMotor().getLen()/2;
		ixxe = ixxe + this.getMotor().getMass() * Math.pow((engine_distance_from_cg),2);
		izze = izze + this.getMotor().getMass() * Math.pow((engine_distance_from_cg),2);
		
		// Add Engine Moment of Inertias to the Engine moment of Inertias
		ixx = ixx + ixxe;
		iyy = iyy + iyye;
		izz = izz + izze;
		
		
		
		// the GRAND Inertia matrix.
		double inertia_data[][] = { {ixx, ixy, ixz},
									{iyx, iyy, iyz},
									{izx, izy, izz}
								  };
				
		RealMatrix inertia = MatrixUtils.createRealMatrix(inertia_data);
		this.setMomentOfInertia(inertia);
				
	}
	
	
	public RealMatrix getRotationMatrix() {
		RealMatrix rotation_matrix;
		double c1, c2, c3;  // Cosine of roll (1), pitch (2), yaw (3)
		double s1, s2, s3;  // Sine of roll (1), pitch (2), yaw (3)
		
		c1 = Math.cos(this.getAng_x());
		c2 = Math.cos(this.getAng_y());
		c3 = Math.cos(this.getAng_z());
		s1 = Math.sin(this.getAng_x());
		s2 = Math.sin(this.getAng_y());
		s3 = Math.sin(this.getAng_z());
		
		double rotation_matrix_data[][] = { 
				{(c3 * c2), (s1 * c3 * s2 - c1 * s3), (s1 * s3 + c1 * c3 * s2)   },
				{(s3 * c2), (c3 * c1 + s3 * s2 * s1), (s3 * s2 * c1 - c3 * s1)   },
				{(-s2)    , (s1 * c2),                (c2 * c1)}
		};
		
		rotation_matrix = MatrixUtils.createRealMatrix(rotation_matrix_data);
		
		return rotation_matrix;
	}	
}
