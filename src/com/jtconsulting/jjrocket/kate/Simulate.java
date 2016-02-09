package com.jtconsulting.jjrocket.kate;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3f;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import com.jtconsulting.jjrocket.kate.utils;
import com.jtconsulting.jjrocket.kate.threedCanvas;
import com.sun.j3d.utils.applet.MainFrame;



public class Simulate {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Constants
		double time_slice = 0.0001;   // How long each time slice is. 
		double total_time = 5;        // How long we do the simulation for
		int    num_intervals = (int) (total_time/time_slice);
		double g = 9.81;

		// Java 3-D
		TransformGroup rocket_system = new TransformGroup();
		Transform3D trans = new Transform3D();
		TransformGroup viewTransformGroup = new TransformGroup();
		Transform3D viewtranslation = new Transform3D();
		
		
		// TIMING
		BigDecimal interval = new BigDecimal(String.valueOf(time_slice));
		BigDecimal time = new BigDecimal("0.0");
		
		
		System.out.println("Starting simulation");
		
		// D12 ENGINE - MAIN MOTOR
		Motor m1 = new Motor();
		m1.setLen(0.07);
		m1.setRadius(0.012);
		m1.setMass_nonfuel(0.018);
		m1.setMass_fuel(0.025);
		m1.setPeak_thrust(30);
		m1.setPeek_thrust_start_time(0.3);
		m1.setPeek_thrust_end_time(0.35);
		m1.setNorm_thrust(10);
		m1.setNorm_thrust_start_time(0.45);
		m1.setNorm_thrust_end_time(2.65);
		m1.setIgnition_delay(0.0);
		
		
		Rocket r = new Rocket();
		r.setLength(0.584);
		r.setMass(.221);
		r.setRadius_external(0.0206);
		r.setRadius_internal(0.019);
		r.setPitch(Math.PI/2);
		r.setYaw(0);
		r.setRoll(0);
		r.setPosition(-0.2,0,0);
		r.setMotor(m1);
		r.setAng_x(0);
		r.setAng_y(0);
		r.setAng_z(0);
		r.setAng_vx(0);
		r.setAng_vy(0);
		r.setAng_vz(0);
		
		
		Smoother s1 = new Smoother();
		s1.setMass(0.035);
		s1.setR(0.019);
		s1.setY(0.250);
		s1.setMax_angular_speed((Math.PI/3)/0.08);
		s1.setAng_y(0);
		
		Smoother s2 = new Smoother();
		s2.setMass(0.035);
		s2.setR(0.019);
		s2.setY(0.250);
		s2.setMax_angular_speed((Math.PI/3)/0.08);
		s2.setAng_y(Math.PI);
		
		
		r.setSmoother1(s1);
		r.setSmoother2(s2);
		
		
		// Objects we repeatedly use
		RealMatrix rotationMatrix;
		

		// Smoother adjustment settings
		double velocity_threshold = 5;
		double set_course = 0;		
		double corrective_angle = 0;
		double move_to_neutral_distance = 0;
		double intermediate_move = 0;
		double final_angle_move = 0;
		double s1_direction = 0, s2_direction = 0;
		double theta;
		double s1_diff = 0, s2_diff = 0;
		double offset = 0.9;  // 1/2 angle between final resting place of smoothers
		
		
		// View Transform
		double viewtransform_y = 15;
		
		// Initialise 3D Canvas
		threedCanvas canvas = new threedCanvas(rocket_system, viewTransformGroup, r);
		new MainFrame(canvas, 1280, 1024);
		
		
		System.out.println("Starting Iterations...");
		for (int n = 0; n < num_intervals; n++) {
			
			time = time.add(interval);
			utils.debug(time, "Interval: " + n + ", Time = "+ time.toString());
			
			Double thrust = m1.getThrust(time.doubleValue());
			thrust = thrust * 0.5;
			utils.debug(time,  "Thrust: " + thrust);
			
			// End of simulation when no more thrust
			if (time.doubleValue() > 0 && thrust == 0) {
				System.out.println("NO MORE THRUST. EXITING");
				break;  // EXIT
			}
			
			// Update state of rocket motor (mass remaining)
			m1.updateState(time);		
			
			
			// Get total Mass of whole rocket system.
			double mass_total = r.getMass() + m1.getMass(); 		
			utils.debug(time, "Total Mass: " + mass_total);
			
			
			// Deduce resultant force vector on rocket
			double rocket_wt = g * mass_total;
			
			// Inaccuracies in Thrust (angles)
			double angle_deviation1 =   0.5;
			double angle_deviation2 =   0.0;
			double angle_deviation3 =   0.0;			
			double[] thrust_force_angles = {Math.PI * angle_deviation1/180, Math.PI * angle_deviation2/180, Math.PI * angle_deviation3/180};

			double[] perfect_thrust = {0, thrust, 0};
			RealVector perfect_thrust_vector = MatrixUtils.createRealVector(perfect_thrust);
			RealMatrix rotation_matrix = utils.createRotationMatrix(thrust_force_angles[0], thrust_force_angles[1], thrust_force_angles[2]);
			RealVector thrust_force_vector = utils.matrixVectorMultiply(rotation_matrix,  perfect_thrust_vector);
		

			// Deduce the Thrust force in global co-ordinate system
			RealVector thrust_force_vector_global = utils.matrixVectorMultiply(r.getRotationMatrix(), thrust_force_vector);
			utils.debug(time, "Global Force Vector is:   "  + thrust_force_vector_global.getEntry(0) + ", " + thrust_force_vector_global.getEntry(1) + ", " + thrust_force_vector_global.getEntry(2));

			
			// Compute CG - and get a vector for it - taking into account rotation of the rocket
			r.computeCg();
			double cg_vector_tmp[] = {r.getCgx(), r.getCgy(), r.getCgz()};
			RealVector cg_vector = MatrixUtils.createRealVector(cg_vector_tmp);
			RealVector cg_vector_global = utils.matrixVectorMultiply(r.getRotationMatrix(), cg_vector);
			// utils.debug(time, "CG          - cx  = " + cg_vector.getEntry(0) + ", cy   = " + cg_vector.getEntry(1) + ", cz  = " + cg_vector.getEntry(2));
			// utils.debug(time, "CG  GLOB    - cx  = " + cg_vector_global.getEntry(0) + ", cy   = " + cg_vector_global.getEntry(1) + ", cz  = " + cg_vector_global.getEntry(2));
			
			// Compute moment generated by the thrust. Cross product
			RealVector torque_vector = utils.crossProduct(thrust_force_vector_global, cg_vector_global);
			utils.debug(time, "MOMENT      - mx  = " + torque_vector.getEntry(0) + ", my   = " + torque_vector.getEntry(1) + ", mz  = " + torque_vector.getEntry(2));

			
			
			// Update state of whole rocket
			r.updateState(torque_vector, thrust_force_vector_global, mass_total, time_slice);
			
			
			// Deduce rotation velocity in the LOCAL co-ordinate system
			rotationMatrix = utils.createRotationMatrix(r.getAng_x(), r.getAng_y(), r.getAng_z());
			RealMatrix rotationMatrix_transpose = rotationMatrix.transpose();
			double[] rotation_velocity_tmp = {r.getAng_vx(), r.getAng_vy(), r.getAng_vz()};
			RealVector rotation_velocity_vector = MatrixUtils.createRealVector(rotation_velocity_tmp);
			RealVector rotation_velocity_local = utils.matrixVectorMultiply(rotationMatrix_transpose, rotation_velocity_vector);
			utils.debug(time, "LOCAL ANGULAR VEL - VX  = " + 180 * rotation_velocity_local.getEntry(0)/Math.PI +    ", VY  = " + 180 * rotation_velocity_local.getEntry(1)/Math.PI +  ", VZ  = " + 180 * rotation_velocity_local.getEntry(2)/Math.PI);
			// utils.debug(time, "rotationMatrix_transpose ROW 1:  "  + rotationMatrix_transpose.getEntry(0, 0) + ", " + rotationMatrix_transpose.getEntry(0,1) + ", " + rotationMatrix_transpose.getEntry(0,2));
			// utils.debug(time, "rotationMatrix_transpose ROW 2:  "  + rotationMatrix_transpose.getEntry(1, 0) + ", " + rotationMatrix_transpose.getEntry(1,1) + ", " + rotationMatrix_transpose.getEntry(1,2));			
			// utils.debug(time, "rotationMatrix_transpose ROW 3:  "  + rotationMatrix_transpose.getEntry(2, 0) + ", " + rotationMatrix_transpose.getEntry(2,1) + ", " + rotationMatrix_transpose.getEntry(2,2));
			
			
			// Deduce rotation acceleration in the LOCAL co-ordinate system			
			double[] rotation_acceleration_tmp = {r.getAng_ax(), r.getAng_ay(), r.getAng_az()};
			RealVector rotation_acceleration_vector = MatrixUtils.createRealVector(rotation_acceleration_tmp);
			RealVector rotation_acceleration_local = utils.matrixVectorMultiply(rotationMatrix_transpose, rotation_acceleration_vector);			
			utils.debug(time, "LOCAL ANGULAR ACC - AX  = " + 180 * rotation_acceleration_local.getEntry(0)/Math.PI +    ", AY  = " + 180 * rotation_acceleration_local.getEntry(1)/Math.PI +  ", AZ  = " + 180 * rotation_acceleration_local.getEntry(2)/Math.PI);
			

			// SMOOTHER CONTROL
			if (set_course == 0 && 
					(Math.abs(180 * rotation_velocity_local.getEntry(0)/Math.PI) > velocity_threshold || Math.abs(180 * rotation_velocity_local.getEntry(2)/Math.PI) > velocity_threshold)
					) {

				
				double skip_control = 0;
				// We check to see how the rotation acceleration is compared to velocity. If the rotation velocity is slowing down, then
				// we exit here... because we are headed in the right direction
				if (Math.signum(rotation_acceleration_local.getEntry(0)) * Math.signum(rotation_velocity_local.getEntry(0)) == -1 
						&&
					Math.signum(rotation_acceleration_local.getEntry(0)) * Math.signum(rotation_velocity_local.getEntry(0)) == -1) {
					skip_control = 1;
					set_course = 0;
				}
				
				
				
				if (skip_control == 0) {
					// Deduce where the smoother should be
					// theta = Math.atan(r.getAng_vz()/r.getAng_vx());
					// We Already have the Rotational velocity co-ordinate in the local system
					// RealVector corrective_torque_direction = utils.revolveVector(0, Math.PI,  0, rotation_velocity_local);
					double[] corrective_torque_direction_tmp = {-1 * rotation_velocity_local.getEntry(0), -1 * rotation_velocity_local.getEntry(1), -1 * rotation_velocity_local.getEntry(2)};
					RealVector corrective_torque_direction = MatrixUtils.createRealVector(corrective_torque_direction_tmp);
					double dist = Math.pow(Math.pow(corrective_torque_direction.getEntry(0), 2) + Math.pow(corrective_torque_direction.getEntry(1), 2) + Math.pow(corrective_torque_direction.getEntry(2), 2), 0.5);
					// theta = Math.atan(corrective_torque_direction.getEntry(2)/corrective_torque_direction.getEntry(0));
				
					// System.out.println("corrective_torque_direction - X  = " + 180 * corrective_torque_direction.getEntry(0)/Math.PI +    ", Y  = " + 180 * corrective_torque_direction.getEntry(1)/Math.PI +  ", Z  = " + 180 * corrective_torque_direction.getEntry(2)/Math.PI);
					System.out.println("!!!!GETTING EXCESSIVE ROTATION!!!!"); //  - " + theta + "   " + corrective_torque_direction.getEntry(2) + ", " + corrective_torque_direction.getEntry(0));
				
				
					// Create Unit Correction'Vector'
					double[] corrective_rotation_tmp = {corrective_torque_direction.getEntry(0)/dist, corrective_torque_direction.getEntry(1)/dist, corrective_torque_direction.getEntry(2)/dist};
					RealVector corrective_rotation = MatrixUtils.createRealVector(corrective_rotation_tmp);
				
					// Generate the thrust vector ... not caring about magnitude...only direction...in local coordinate system
					double[] thrust_vector_tmp = {0, 1, 0};
					RealVector thrust_vector = MatrixUtils.createRealVector(thrust_vector_tmp);
				
					// Determine the direction of the CG vector...needed to produce torque to oppose the current motion
					RealVector corrective_cg_vector = utils.crossProduct(thrust_vector, corrective_rotation);
				
				
					// Determine angle vector in X-direction in local reference frame... Use this later to find angle the CG vector makes with X-axis
					double[] x_vector_tmp = {1, 0, 0};
					RealVector x_vector = MatrixUtils.createRealVector(x_vector_tmp);				
				
				
					// Determine the angle this CG makes 
					double corrective_cg_vector_size = Math.pow(Math.pow(corrective_cg_vector.getEntry(0), 2) + Math.pow(corrective_cg_vector.getEntry(1), 2) + Math.pow(corrective_cg_vector.getEntry(2), 2), 0.5 );
					corrective_angle = Math.acos((x_vector.getEntry(0) * corrective_cg_vector.getEntry(0) + x_vector.getEntry(1) * corrective_cg_vector.getEntry(1) + x_vector.getEntry(2) * corrective_cg_vector.getEntry(2))/corrective_cg_vector_size);
					System.out.println("RAW Corrective angle       = " + corrective_angle);
				
					
					// Figure out if    0 < angle 180  OR   180 < angle < 360
					double zcross = x_vector.getEntry(0) * corrective_cg_vector.getEntry(2) - corrective_cg_vector.getEntry(0) * x_vector.getEntry(2);
					System.out.println("zcross: " + zcross);
					
					// The smoothers are put 180 degrees out from the direction the CG vectors point in.
					// corrective_angle = corrective_angle + Math.PI;
					if (zcross < 0) {
						System.out.println("WHATEVER");
						corrective_angle = corrective_angle + Math.PI;
					}
					
					
					corrective_angle = utils.angle_reorg(corrective_angle);
				
				
				
					System.out.println("Corrective_Rotation    = " + corrective_rotation.getEntry(0)   + ", " + corrective_rotation.getEntry(1)   + ", " + corrective_rotation.getEntry(2));
					System.out.println("Rotated_thrust_vector  = " + thrust_vector.getEntry(0)         + ", " + thrust_vector.getEntry(1)         + ", " + thrust_vector.getEntry(2));
					System.out.println("Corrective_cg_vector   = " + corrective_cg_vector.getEntry(0)  + ", " + corrective_cg_vector.getEntry(1)  + ", " + corrective_cg_vector.getEntry(2));
					System.out.println("Corrective angle       = " + corrective_angle);
				
				
				

				
					// Next step...find out the course to set....
                	set_course = 1;			
				}
			}
			
			
			// We want to get ourselves back to the neutral position...where the CG of the two smoothers is at (0, cy, 0)
			if (set_course == 1) {
				
				double mid_point_angle = Math.acos(Math.cos(s1.getAng_y()) * Math.cos(s2.getAng_y()) + Math.sin(s1.getAng_y()) * Math.sin(s2.getAng_y()));
				mid_point_angle = utils.angle_reorg(mid_point_angle);
				if (mid_point_angle > Math.PI) {
					mid_point_angle = mid_point_angle - Math.PI;
				}
				
				if (mid_point_angle < Math.PI) {
					move_to_neutral_distance = mid_point_angle/2;
				} else {
					move_to_neutral_distance = 0;
				}
				
				
				
				set_course = 2;
			}
			
			
			
			// Based on what was determined in set_course == 1 step above, we move the smoothers
			if (set_course == 2) {
				
				if (move_to_neutral_distance <= 0) {
					set_course = 3;
					System.out.println("CAPTAIN");
				} else {
					move_to_neutral_distance = move_to_neutral_distance - interval.doubleValue() * s2.getMax_angular_speed();
					
					double mid_point_angle = Math.acos(Math.cos(s1.getAng_y()) * Math.cos(s2.getAng_y()) + Math.sin(s1.getAng_y()) * Math.sin(s2.getAng_y()));
					double zcross = Math.cos(s1.getAng_y()) * Math.sin(s2.getAng_y()) - Math.cos(s2.getAng_y()) * Math.sin(s1.getAng_y());
					
					// The smoothers are put 180 degrees out from the direction the CG vectors point in.
					// corrective_angle = corrective_angle + Math.PI;
					if (zcross < 0) {
						mid_point_angle = mid_point_angle + Math.PI;
					}
					
					
					if (mid_point_angle > Math.PI) {
						s1.setAng_y(s1.getAng_y() + interval.doubleValue() * s1.getMax_angular_speed());
						s2.setAng_y(s2.getAng_y() - interval.doubleValue() * s2.getMax_angular_speed());
					} else if (mid_point_angle < Math.PI) {
						s1.setAng_y(s1.getAng_y() - interval.doubleValue() * s1.getMax_angular_speed());
						s2.setAng_y(s2.getAng_y() + interval.doubleValue() * s2.getMax_angular_speed());
					} else {
						set_course = 3;
						System.out.println("UNDERPANTS");
					}
					
					
				}
				System.out.println("BACK TO NEUTRAL S1 ANGLE: " + s1.getAng_y());
				System.out.println("BACK TO NEUTRAL S2 ANGLE: " + s2.getAng_y());
			}			
			
			
			// If the course needs setting, we find out what it should be....
			// but FIRST we move the middle position of the two masses to the direction
			// This helps to reduce instabilities introduced
			if (set_course == 3) {
				// Get current angles in 0...2Pi range (no negative, nothing > 2Pi
				//s1.setAng_y(s1.getAng_y());
				//s2.setAng_y(s2.getAng_y());
				
				/// Find angle between the two smoothers...then halve...this is the mid-point
				double mid_point_angle = Math.acos(Math.cos(s1.getAng_y()) * Math.cos(s2.getAng_y()) + Math.sin(s1.getAng_y()) * Math.sin(s2.getAng_y()));  
				mid_point_angle = mid_point_angle / 2;
				
				

				
				// HACK
				// corrective_angle = Math.PI;
				
				
				// Deduce the distance we need 
				intermediate_move = Math.abs(corrective_angle - mid_point_angle);
				
				// If Greater than Pi, then we are being in-efficient
				if (intermediate_move >= Math.PI) {
					intermediate_move = intermediate_move - Math.PI;
				} 
				
				// Figure out which direction to move in...to take up least amount of time
				if (intermediate_move < Math.PI && intermediate_move > Math.PI/2) {
					intermediate_move = Math.PI - intermediate_move;
					s1_direction = 2;
					s2_direction = 2;
				} else if (intermediate_move < Math.PI/2 && intermediate_move > 0) {
					s1_direction = 1;
					s2_direction = 1;
				} else {
					s1_direction = 0;
					s2_direction = 0;
					intermediate_move = 0;
				}
				
				System.out.println("Midpoint Angle: " + mid_point_angle);
				System.out.println("Intermediate Angle: " + intermediate_move);
				System.out.println("S1 Direction: " + s1_direction);
				System.out.println("S2 Direction: " + s2_direction);
				
				// Signal to code to go on to 'Intermediate' move
				set_course = 4;
				
				// Intermediate move...before motion to get into final position
				// intermediate_move = Math.PI/2;  // How far we move BOTH
				// s1_direction = 1;  // 0 - No movement, 1 = CCW, 2 = CW
				// s2_direction = 1;  // 0 - No movement, 1 = CCW, 2 = CW
			}
			
			
			// Course is known...now we need to 'move' there
			// No smarts in how to get there....Just get us there.
			/*
			 * 			if (set_course == 2) {

				// If corrective Angle > 0
				corrective_angle = corrective_angle - interval.doubleValue() * s1.getMax_angular_speed();
					
				if (corrective_angle < 0) {
					set_course = 3;
				}
					
				// Move S1 around
				if (s1_diff < 0) {
					s1.setAng_y(s1.getAng_y() + interval.doubleValue() * s1.getMax_angular_speed());
				} else if (s1_diff > 0) {
					s1.setAng_y(s1.getAng_y() - interval.doubleValue() * s1.getMax_angular_speed());
				}
					
				
				// Move S2 around
				if (s2_diff < 0) {
					s2.setAng_y(s2.getAng_y() + interval.doubleValue() * s2.getMax_angular_speed());
				} else if (s1_diff > 0) {
					s2.setAng_y(s2.getAng_y() - interval.doubleValue() * s2.getMax_angular_speed());
				}
					
				//s1.setAng_y(3.14);
				//s2.setAng_y(3.14);
				//set_course = 3;
				System.out.println("S1 ANGLE: " + s1.getAng_y());
				System.out.println("S2 ANGLE: " + s2.getAng_y());
				
			}
			 */
			if (set_course == 4) {			
				
				// Intermediate move - moving both weights together
				intermediate_move = intermediate_move - interval.doubleValue() * s1.getMax_angular_speed();
					
				if (intermediate_move < 0) {
					
					s1_diff = s1.getAng_y() - corrective_angle;
					s2_diff = s2.getAng_y() - corrective_angle;
					
					final_angle_move = Math.acos(Math.cos(s1.getAng_y()) * Math.cos(s2.getAng_y()) + Math.sin(s1.getAng_y()) * Math.sin(s2.getAng_y()));  
					final_angle_move = final_angle_move / 2 - offset;
					
					// final_angle_move = Math.abs(s1_diff);
					set_course = 5;
					
					System.out.println("New S1 Diff: " + s1_diff);
					System.out.println("New S2 Diff: " + s2_diff);
					System.out.println("final_angle_move = " + final_angle_move);
				}
					
				// Move S1 around
				if (s1_direction == 1) {
					s1.setAng_y(s1.getAng_y() + interval.doubleValue() * s1.getMax_angular_speed());
				} else if (s1_direction == 2) {
					s1.setAng_y(s1.getAng_y() - interval.doubleValue() * s1.getMax_angular_speed());
				}
					
				
				// Move S2 around
				if (s2_direction == 1) {
					s2.setAng_y(s2.getAng_y() + interval.doubleValue() * s2.getMax_angular_speed());
				} else if (s2_direction == 2) {
					s2.setAng_y(s2.getAng_y() - interval.doubleValue() * s2.getMax_angular_speed());
				}
					
				System.out.println("S1 ANGLE: " + s1.getAng_y());
				System.out.println("S2 ANGLE: " + s2.getAng_y());
				
			}
			
			// Course is known...now we need to 'move' there
			// No smarts in how to get there....Just get us there.
			if (set_course == 5) {

				
				// If corrective Angle > 0
				final_angle_move = final_angle_move - interval.doubleValue() * s1.getMax_angular_speed();
					
				if (final_angle_move < 0) {
					set_course = 0;
					
				}
					

				// Move S1 around
				if (s1_diff > Math.PI) {
					s1.setAng_y(s1.getAng_y() + interval.doubleValue() * s1.getMax_angular_speed());
				} else if (s1_diff <= Math.PI && s1_diff > 0) {
					s1.setAng_y(s1.getAng_y() - interval.doubleValue() * s1.getMax_angular_speed());
				} else if (s1_diff < 0 && s1_diff > -Math.PI){
					s1.setAng_y(s1.getAng_y() + interval.doubleValue() * s1.getMax_angular_speed());
				} else if (s1_diff <= -Math.PI) {
					s1.setAng_y(s1.getAng_y() - interval.doubleValue() * s1.getMax_angular_speed());
				} else { 
					System.out.println("No Movement required - s1");
				}
					
				
				// Move S2 around
				if (s2_diff > Math.PI) {
					s2.setAng_y(s2.getAng_y() + interval.doubleValue() * s2.getMax_angular_speed());
				} else if (s2_diff > 0 && s2_diff <= Math.PI) {
					s2.setAng_y(s2.getAng_y() - interval.doubleValue() * s2.getMax_angular_speed());
				} else if (s2_diff < 0 && s2_diff > -Math.PI){
					s2.setAng_y(s2.getAng_y() + interval.doubleValue() * s2.getMax_angular_speed());
				} else if (s2_diff <= -Math.PI) {
					s2.setAng_y(s2.getAng_y() - interval.doubleValue() * s2.getMax_angular_speed());
				} else { 
					System.out.println("No Movement required - s2");
				}
					
				System.out.println("FINAL S1 ANGLE: " + s1.getAng_y());
				System.out.println("FINAL S2 ANGLE: " + s2.getAng_y());
				
			}			
			
			
			
			// Print out new state
			utils.debug(time, "POSITION    - X   = " + r.getX() +                       ", Y   = " + r.getY() +   ", Z   = " + r.getZ());
			utils.debug(time, "VELOCITY    - VX  = " + r.getVx() +                      ", VY  = " + r.getVy() +  ", VZ  = " + r.getVz());
			utils.debug(time, "ACCELERATE  - AX  = " + r.getAx() +                      ", AY  = " + r.getAy() +  ", AZ  = " + r.getAz());
			utils.debug(time, "Centre Grav - CGX = " + r.getCgx() +                     ", CGY = " + r.getCgy() + ", CGZ = " + r.getCgz());
			utils.debug(time, "ANGULAR POS - X   = " + 180 * r.getAng_x()/Math.PI	+   ", Y   = " + 180 * r.getAng_y()/Math.PI +   ", Z   = " + 180 * r.getAng_z()/Math.PI);
			utils.debug(time, "ANGULAR VEL - VX  = " + 180 * r.getAng_vx()/Math.PI +    ", VY  = " + 180 * r.getAng_vy()/Math.PI +  ", VZ  = " + 180 * r.getAng_vz()/Math.PI);
			utils.debug(time, "ANGULAR ACC - AX  = " + 180 * r.getAng_ax()/Math.PI +    ", AY  = " + 180 * r.getAng_ay()/Math.PI +  ", AZ  = " + 180 * r.getAng_az()/Math.PI);
			utils.debug(time, "Inertia ROW 1:  "  + r.getMomentOfInertia().getEntry(0, 0) + ", " + r.getMomentOfInertia().getEntry(0,1) + ", " + r.getMomentOfInertia().getEntry(0,2));
			utils.debug(time, "Inertia ROW 2:  "  + r.getMomentOfInertia().getEntry(1, 0) + ", " + r.getMomentOfInertia().getEntry(1,1) + ", " + r.getMomentOfInertia().getEntry(1,2));			
			utils.debug(time, "Inertia ROW 3:  "  + r.getMomentOfInertia().getEntry(2, 0) + ", " + r.getMomentOfInertia().getEntry(2,1) + ", " + r.getMomentOfInertia().getEntry(2,2));
			
			
			
			// Blank line(s) between intervals
			utils.debug(time, "");
			utils.debug(time, "");
			
			
			
			// Update the Java 3-D calculations
			rotationMatrix = utils.createRotationMatrix(r.getAng_x(), r.getAng_y(), r.getAng_z());
			Matrix3d matrix3d = new Matrix3d();
			matrix3d.setColumn(0,  rotationMatrix.getColumn(0));
			matrix3d.setColumn(1,  rotationMatrix.getColumn(1));
			matrix3d.setColumn(2,  rotationMatrix.getColumn(2));
			trans.setRotation(matrix3d);
				
			// Translation of rocket (up)
		 	Transform3D translation = new Transform3D();
		 	Vector3f translation_vector = new Vector3f((float) r.getX(), (float) r.getY(), (float) r.getZ());
		 	translation.setTranslation(translation_vector);
		 	
		 	
		 	// Combining all moves
		 	translation.mul(trans);
		 	viewTransformGroup.setTransform(viewtranslation);
			rocket_system.setTransform(translation);
			
			

			
			
			try {
				TimeUnit.MILLISECONDS.sleep(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
		
		
		System.out.println("Finished simulation");
	}

}
