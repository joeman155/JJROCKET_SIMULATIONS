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
		int data_ticks = 100;   // i.e. for every tick (a tick being time_slice long, we get a measurement!)
								// We do this to try and simulate a sensor that only returns information at a certain
								// frequency. e.g. with time_slize = 0.0001, data_ticks = 100 ---> measurement every 0.01 seconds. 
								// i.e 100Hz.
		
		// Properties used in calculation of Drag and Lift
		double Cd = 0.75;
		
		// Density at sea level
		// double density = 1.225;
		
		// Density at approx 30km up
		double density = 0.01841;
				
		// Density at approx 40km up
		// double density = 0.03996;
				
		// Density at approx 60km up
		// double density = 0.003097;
		
		// Density at approx 80km up
		// double density = 0.0001846;
		
		
		
		int y_intercept = 54;
		int multiplier = 12;	// How far away we are from rocket
		boolean launch_detect;
		boolean servo_move;
		boolean track_mode;
		double reference_angle; // The angle we want to maintain in regards to the direction of the masses
		launch_detect = false;  // Set true at 0.1 seconds...bit of a hack...we expect launch to be detected no more than 0.1 seconds after launch
		servo_move = false;     // Set true after we move the servo!
		track_mode = false;     // Set true when we are trying to counteract the motion of the rocket around the y-Axis.
		reference_angle = 0;
        

		// Java 3-D
		TransformGroup rocket_system = new TransformGroup();
		Transform3D trans = new Transform3D();
		TransformGroup viewTransformGroup = new TransformGroup();
		Transform3D viewtranslation = new Transform3D();
		
		
		// TIMING
		BigDecimal interval = new BigDecimal(String.valueOf(time_slice));
		BigDecimal time = new BigDecimal("0.0");
		
		
		System.out.println("Starting simulation");
		
		
		/*
		// D12 ENGINE - MAIN MOTOR
		double motor_burn_duration = 1.65;
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
		m1.setNorm_thrust_end_time(motor_burn_duration);
		m1.setIgnition_delay(0.0);
		*/
		
		
/*		
		// 125G131-14A (SS)
		double motor_burn_duration = 0.9;
		Motor m1 = new Motor();
		m1.setLen(0.187);
		m1.setRadius(0.015);
		m1.setMass_nonfuel(0.106);
		m1.setMass_fuel(0.094);
		m1.setPeak_thrust(150);
		m1.setPeek_thrust_start_time(0.05);
		m1.setPeek_thrust_end_time(0.875);
		m1.setNorm_thrust(0);
		m1.setNorm_thrust_start_time(0.88);
		m1.setNorm_thrust_end_time(motor_burn_duration);
		m1.setIgnition_delay(0.0);
*/		
		
		// G67G-6
		double motor_burn_duration = 1.725;
		Motor m1 = new Motor();
		m1.setLen(0.187);
		m1.setRadius(0.015);
		m1.setMass_nonfuel(0.087);
		m1.setMass_fuel(0.06);
		m1.setPeak_thrust(150);
		m1.setPeek_thrust_start_time(0.05);
		m1.setPeek_thrust_end_time(0.07);
		m1.setNorm_thrust(0);
		m1.setNorm_thrust_start_time(1.720);
		m1.setNorm_thrust_end_time(motor_burn_duration);
		m1.setIgnition_delay(0.0);
		
		
		// We use the dimensions of rocket to calculate quantities
		//
		// Moment of Inertia is a VERY basic estimate
		//
		Rocket r = new Rocket();
		r.setLength(1.46);
		r.setMass(1.113);
		r.setRadius_external(0.028);
		r.setRadius_internal(0.025);
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
		
		
		
		double smoother_speed = (Math.PI/3)/0.0095;
		Smoother s1 = new Smoother();
		s1.setMass(0.045);
		s1.setR(0.02);
		s1.setY(1.06);
		s1.setMax_angular_speed(smoother_speed);
		s1.setAng_y(0);
		
		Smoother s2 = new Smoother();
		s2.setMass(0.045);
		s2.setR(0.02);
		s2.setY(1.06);
		s2.setMax_angular_speed(smoother_speed);
		s2.setAng_y(Math.PI);
		
		
		r.setSmoother1(s1);
		r.setSmoother2(s2);
		
		
		// Objects we repeatedly use
		RealMatrix rotationMatrix;
		

		// Smoother adjustment settings
		double upper_velocity_threshold = 5;
		double lower_velocity_threshold = 2;
		double upper_acceleration_threshold = 0;   // Normally 25...trying to force code to activate stabilisation JOE
		double lower_acceleration_threshold = 5;
		
		double set_course = 0;		
		double corrective_angle = 0;
		double move_to_neutral_distance = 0;
		double intermediate_move = 0;
		double final_angle_move = 0;
		double resting_angle_move = 0;
		double s1_direction = 0, s2_direction = 0;
		double s1_diff = 0, s2_diff = 0;
		double offset = 0.000;  // 1/2 angle between final resting place of smoothers
		double ease_back_timer = 0;  // How long to wait before easing back...trying to correct somewhat for position
		
		// We can't make much correction when we are getting close to the end of the rocket burn...so we don't bother trying to correct
		double time_no_more_adjustments = motor_burn_duration - Math.PI / smoother_speed;  
		

		
		// Initialise 3D Canvas
		// view3d.lookAt(new Point3d(52,16,52),  new Point3d(0,20,0),  new Vector3d(0,1,0));
		// threedCanvas canvas = new threedCanvas(rocket_system, viewTransformGroup, r, 52, 16,52,0,20,0,0,1,0);
		threedCanvas canvas = new threedCanvas(rocket_system, viewTransformGroup, r, 15 * multiplier, 1 * multiplier,15 * multiplier, 0,y_intercept,0, 0,1,0);
		new MainFrame(canvas, 1280, 1024);
		
		System.out.println("time_no_more_adjustments: " + time_no_more_adjustments);		
		System.out.println("Starting Iterations...");
		for (int n = 0; n < num_intervals; n++) {
			
			time = time.add(interval);
			utils.debug(time, "Interval: " + n + ", Time = "+ time.toString());
			
			Double thrust = m1.getThrust(time.doubleValue());
			thrust = thrust * 1;
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
			double angle_deviation1 =   0.0;
			double angle_deviation2 =   0.0;
			double angle_deviation3 =   0.00;	
			double[] thrust_force_angles = {Math.PI * angle_deviation1/180, Math.PI * angle_deviation2/180, Math.PI * angle_deviation3/180};

			
			// DRAG
			double drag_force_amount = 0.5 * density * r.getArea() * r.getVy() * r.getVy() * Cd;
			utils.debug(time, "Drag is:   "  + drag_force_amount);
			
			
			// MOTOR THRUST minus Drag
			double[] perfect_thrust = {0, thrust - drag_force_amount, 0};
			RealVector perfect_thrust_vector = MatrixUtils.createRealVector(perfect_thrust);
			RealMatrix rotation_matrix = utils.createRotationMatrix(thrust_force_angles[0], thrust_force_angles[1], thrust_force_angles[2]);
			RealVector thrust_force_vector = utils.matrixVectorMultiply(rotation_matrix,  perfect_thrust_vector);
			utils.debug(time, "Local  Force Vector is:   "  + thrust_force_vector.getEntry(0) + ", " + thrust_force_vector.getEntry(1) + ", " + thrust_force_vector.getEntry(2));

			// Deduce the Thrust force in global co-ordinate system
			RealVector thrust_force_vector_global = utils.matrixVectorMultiply(r.getRotationMatrix(), thrust_force_vector);
			utils.debug(time, "Global Force Vector is:   "  + thrust_force_vector_global.getEntry(0) + ", " + thrust_force_vector_global.getEntry(1) + ", " + thrust_force_vector_global.getEntry(2));

			
			
			// Get Angle of attack - THIS IS NOT GENERIC CODE - IT ASSUMES ROCKET only rotates about the X-axis as
			// it takes off
			//
			//
			// Incorrect code...kept for inspiration
			// double angle_of_attack = utils.DotProductAngle(Math.cos(r.getAng_x()), r.getAng_y(),r.getAng_z(),
			// 												Math.cos(r.getAng_vx()), r.getAng_vy(), r.getAng_vz());
			
			// utils.debug(time,  "vector1 = " + r.getAng_x() + "," + r.getAng_y() + "," + r.getAng_z());
			// utils.debug(time,  "vector2 = " + r.getAng_vx() + "," + r.getAng_vy() + "," + r.getAng_vz());
			
			// Ignoring any wind...compare direction of orientation of rocket with its motion...get difference in angle
			double angle_of_attack;
			if (r.getVy() != 0) {
			    angle_of_attack = r.getAng_x() - (Math.atan(r.getVz()/r.getVy()));
			} else {
				angle_of_attack = 0;
			}
			utils.debug(time, "angle_of_attack is:   " + angle_of_attack * 180/Math.PI);
			
			
			// LIFT CALCS - (on the fins) - VERY basic....just using something cobbled together from FoilSim
			double gammaval = 2*1* Math.sin(angle_of_attack);
			double clift = gammaval * 4 * 3.1415 /0.328;   
			double q0 = 0.5 * density * 0.0193* r.getVy() * r.getVy() * 3.28 * 3.28;
			// 0.5 = constant
			// density = density in kg/m^3
			// 0.193 = convert kg/M^3 to slugs/ft^3
			// r.getVy = approximate stream velocity
			// 3.28 used to convert stream velocity to ft/second
			
			double lift = -2 *  4.44 * (clift * q0* 0.107584 * 0.5);
			
			
			// - = corrects direction
			// 2 = for two fins
			// 4.44 converts Pounds Force to Newtons
			// 25.49 = q0 = ....
			// 0.107584 = area of a fin (approx 10cm x 10cm) 0.328 * 0.328 = 0.1075ft^2  (if square)
			// 0.5 ... because fins are triangular.....so half a square
			
			utils.debug(time, "Fin Lift:   " + lift + " Newtons");
			
			// Torque is in the X direction, no other component (assuming rocket is not rotating)
			double distance_to_cg = 0.5;   // Taken from OpenRocket of Callisto
			double fin_lift_torque = lift * distance_to_cg;
			utils.debug(time, "Fin Lift Torque:   " + fin_lift_torque + " Newtons.metres");
			
			
			
					
			/*
			// DRAG
			double drag_force_amount = 0.5 * r.getArea() * r.getVy() * r.getVy() * Cd;
			double[] drag_force = {0, -drag_force_amount, 0};
			utils.debug(time, "Drag is:   "  + drag_force_amount);
			RealVector drag_vector = MatrixUtils.createRealVector(drag_force);
			RealVector drag_force_vector = utils.matrixVectorMultiply(rotation_matrix,  drag_vector);
					
			// Deduce the DRAG force in global co-ordinate system
			RealVector drag_force_vector_global = utils.matrixVectorMultiply(r.getRotationMatrix(), drag_force_vector);
			utils.debug(time, "Drag Force Vector is:   "  + drag_force_vector_global.getEntry(0) + ", " + drag_force_vector_global.getEntry(1) + ", " + drag_force_vector_global.getEntry(2));
			*/
			
			
			
			// Compute CG - and get a vector for it - taking into account rotation of the rocket
			r.computeCg();
			double cg_vector_tmp[] = {r.getCgx(), r.getCgy(), r.getCgz()};
			RealVector cg_vector = MatrixUtils.createRealVector(cg_vector_tmp);
			RealVector cg_vector_global = utils.matrixVectorMultiply(r.getRotationMatrix(), cg_vector);
			// utils.debug(time, "CG          - cx  = " + cg_vector.getEntry(0) + ", cy   = " + cg_vector.getEntry(1) + ", cz  = " + cg_vector.getEntry(2));
			utils.debug(time, "CG  GLOB    - cx  = " + cg_vector_global.getEntry(0) + ", cy   = " + cg_vector_global.getEntry(1) + ", cz  = " + cg_vector_global.getEntry(2));
			
			// Compute moment generated by the thrust. Cross product
			RealVector torque_vector = utils.crossProduct(thrust_force_vector_global, cg_vector_global);
			
			//
			// Add Torque from fins to the torque caused by the Masses....we should see that the fins torque 
			// Stabilises the motion of the rocket...and it does seems to quite well.
			// The rocket goes 'crazy' at end of cals, but suspect this is due to some modelling issue.
			//
			double x_torque = torque_vector.getEntry(0);
			utils.debug(time, "OLD X toruqe = " + x_torque); 
			x_torque = x_torque + fin_lift_torque;
			utils.debug(time, "NEW X toruqe = " + x_torque); 
			torque_vector.setEntry(0, x_torque);
			
			
			utils.debug(time, "MOMENT      - mx  = " + torque_vector.getEntry(0) + ", my   = " + torque_vector.getEntry(1) + ", mz  = " + torque_vector.getEntry(2) + " Nm");

			
			
			// Update state of whole rocket
			r.updateState(torque_vector, thrust_force_vector_global, mass_total, time_slice);
			
			
			// Deduce rotation velocity in the LOCAL co-ordinate system
			rotationMatrix = utils.createRotationMatrix(r.getAng_x(), r.getAng_y(), r.getAng_z());
			RealMatrix rotationMatrix_transpose = rotationMatrix.transpose();
			double[] rotation_velocity_tmp = {r.getAng_vx(), r.getAng_vy(), r.getAng_vz()};
			RealVector rotation_velocity_vector = MatrixUtils.createRealVector(rotation_velocity_tmp);
			RealVector rotation_velocity_local = utils.matrixVectorMultiply(rotationMatrix_transpose, rotation_velocity_vector);
			utils.debug(time, "LOCAL ANGULAR VEL - VX  = " + 180 * rotation_velocity_local.getEntry(0)/Math.PI +    ", VY  = " + 180 * rotation_velocity_local.getEntry(1)/Math.PI +  ", VZ  = " + 180 * rotation_velocity_local.getEntry(2)/Math.PI + " degrees/sec");
			
			
			// Deduce rotation acceleration in the LOCAL co-ordinate system			
			double[] rotation_acceleration_tmp = {r.getAng_ax(), r.getAng_ay(), r.getAng_az()};
			RealVector rotation_acceleration_vector = MatrixUtils.createRealVector(rotation_acceleration_tmp);
			RealVector rotation_acceleration_local = utils.matrixVectorMultiply(rotationMatrix_transpose, rotation_acceleration_vector);			
			utils.debug(time, "LOCAL ANGULAR ACC - AX  = " + 180 * rotation_acceleration_local.getEntry(0)/Math.PI +    ", AY  = " + 180 * rotation_acceleration_local.getEntry(1)/Math.PI +  ", AZ  = " + 180 * rotation_acceleration_local.getEntry(2)/Math.PI + "degrees/sec/sec");
	
			
			
// CODE BELOW TO "THISISTHEEND" is to simulate the KATE system
			
// THIS ROUTINE IS GARBAGE. the s1, s2 Ang are in RESPECT to fixed point on bottom of rocket
// i.e. local Axis system.			
// Servos hold their position, so they rotate around with the rocket			
//			s1.setAng_y(s1.getAng_y() + time_slice * r.getAng_vy());
//			s2.setAng_y(s2.getAng_y() + time_slice * r.getAng_vy());			

			if (time.doubleValue() > 0.1 && ! launch_detect) {
				launch_detect = true;
				System.out.println("");
				System.out.println("");
				System.out.println("");
				System.out.println("");
				System.out.println("");
				System.out.println("Launch Detected!");
				System.out.println("");
				System.out.println("");
				System.out.println("");
				System.out.println("");
				System.out.println("");	
			}
			
			
			// When a launch is detected, move the Servo
			if (launch_detect && ! servo_move) {
				servo_move = true;
				track_mode = true;
				System.out.println("");
				System.out.println("");
				System.out.println("");
				System.out.println("");
				System.out.println("");
				System.out.println("Moving the Servos!!");
				System.out.println("");
				System.out.println("");
				System.out.println("");
				System.out.println("");
				System.out.println("");			
				
				
				// We subtract r.getAng_y() to get the masses pointing in -Z exactly - global Axis system
				s1.setAng_y(Math.PI/2 - r.getAng_y());
				s2.setAng_y(Math.PI/2 - r.getAng_y());
				
				
				reference_angle = 180 * r.getAng_y() / Math.PI;
				
			}
			
			
			// Track rotational motion of the rocket
			if (track_mode) {
				
				double angley = 180 * r.getAng_y() / Math.PI;
				double angley_diff = angley - reference_angle;
				
				// Only proceed if angle differences are significant
				if (Math.abs(angley_diff) > 1 ) {
					reference_angle = angley;
					s1.setAng_y(s1.getAng_y() - Math.PI * angley_diff/180);
					s2.setAng_y(s2.getAng_y() - Math.PI * angley_diff/180);
				}
				
			}
			
// "THISISTHEEND"
			
			
			// SMOOTHER CONTROL
			if (time.doubleValue() < time_no_more_adjustments && (n % data_ticks) == 0 && set_course == 0 && 
					(
							(	Math.abs(180 * rotation_velocity_local.getEntry(0)/Math.PI) > upper_velocity_threshold
									&&
								Math.abs(180 * rotation_acceleration_local.getEntry(0)/Math.PI) > upper_acceleration_threshold	
							)
							|| 
							(	Math.abs(180 * rotation_velocity_local.getEntry(2)/Math.PI) > upper_velocity_threshold)
									&&
								Math.abs(180 * rotation_acceleration_local.getEntry(2)/Math.PI) > upper_acceleration_threshold
							)
					) {

				
				double skip_control = 1;    // 1 = DISABLE Stabilisation,   0 = ENABLE Stabilisation
				// We check to see how the rotation acceleration is compared to velocity. If the rotation velocity is slowing down, then
				// we exit here... because we are headed in the right direction
				if (
						(Math.signum(rotation_acceleration_local.getEntry(0)) * Math.signum(rotation_velocity_local.getEntry(0)) == -1 || Math.abs(180 * rotation_velocity_local.getEntry(0)/Math.PI) <  upper_velocity_threshold)
						&&
						(Math.signum(rotation_acceleration_local.getEntry(2)) * Math.signum(rotation_velocity_local.getEntry(2)) == -1 || Math.abs(180 * rotation_velocity_local.getEntry(2)/Math.PI) < upper_velocity_threshold) 
						) {
					skip_control = 1;
					set_course = 0;
					System.out.println("Returning to equilibrium, so not proceeding to make any adjustments to Smoothers");
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
					
					// Figure out if    0 < angle 180  OR   180 < angle < 360
					double zcross = x_vector.getEntry(0) * corrective_cg_vector.getEntry(2) - corrective_cg_vector.getEntry(0) * x_vector.getEntry(2);					
					
					
					// The smoothers are put 180 degrees out from the direction the CG vectors point in.
					// corrective_angle = corrective_angle + Math.PI;
					if (zcross > 0) {
						corrective_angle = 2 * Math.PI - corrective_angle;
					}

					// Smoothers 180 degrees out of phase from direction of 'corrective CG vector'
					corrective_angle = corrective_angle + Math.PI;
					
					// Make sure angle is between 0 and 6.28
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
				
				
				// We know that mid_point_angle MUST be less then 180 degrees BECAUSE this angle is got from dot-product				
				if (mid_point_angle < Math.PI) {
					move_to_neutral_distance = (Math.PI - mid_point_angle)/2;
				} else {
					move_to_neutral_distance = 0;
				}
				

				
				set_course = 2;
				
				if (utils.angle_reorg(s2.getAng_y()) >= utils.angle_reorg(s1.getAng_y())) {
					s1_direction = 1; // CW
					s2_direction = 2; // CCW
				} else if (utils.angle_reorg(s2.getAng_y()) < utils.angle_reorg(s1.getAng_y())) {
					s1_direction = 2; // CW
					s2_direction = 1; // CCW
				}				
				
				System.out.println("NEUTRALMIDPOINT: " + mid_point_angle);
				System.out.println("NEUTRAL: " + move_to_neutral_distance);
				System.out.println("s1_direction: " + s1_direction);
				
				s1.setAng_y(utils.angle_reorg(s1.getAng_y()));
				s2.setAng_y(utils.angle_reorg(s2.getAng_y()));
			}
			
			
			
			// Based on what was determined in set_course == 1 step above, we move the smoothers
			if (set_course == 2) {
				
				if (move_to_neutral_distance <= 0) {
					set_course = 3;
				} else {
					move_to_neutral_distance = move_to_neutral_distance - interval.doubleValue() * s2.getMax_angular_speed();
					
					
					/*
					double mid_point_angle = Math.acos(Math.cos(s1.getAng_y()) * Math.cos(s2.getAng_y()) + Math.sin(s1.getAng_y()) * Math.sin(s2.getAng_y()));
					double zcross = Math.cos(s1.getAng_y()) * Math.sin(s2.getAng_y()) - Math.cos(s2.getAng_y()) * Math.sin(s1.getAng_y());
					
					// The smoothers are put 180 degrees out from the direction the CG vectors point in.
					// corrective_angle = corrective_angle + Math.PI;
					if (zcross > 0) {
						mid_point_angle = (Math.PI * 2 - mid_point_angle); //  + Math.PI;
					}
					
					
					if (mid_point_angle > Math.PI) {
						s1.setAng_y(s1.getAng_y() + interval.doubleValue() * s1.getMax_angular_speed());
						s2.setAng_y(s2.getAng_y() - interval.doubleValue() * s2.getMax_angular_speed());
					} else if (mid_point_angle < Math.PI) {
						s1.setAng_y(s1.getAng_y() - interval.doubleValue() * s1.getMax_angular_speed());
						s2.setAng_y(s2.getAng_y() + interval.doubleValue() * s2.getMax_angular_speed());
					} else {
						set_course = 3;
					}
					*/
					if (s1_direction == 1) {
						s1.setAng_y(s1.getAng_y() - interval.doubleValue() * s1.getMax_angular_speed());
						s2.setAng_y(s2.getAng_y() + interval.doubleValue() * s2.getMax_angular_speed());
					} else if (s1_direction == 2) {
						s1.setAng_y(s1.getAng_y() + interval.doubleValue() * s1.getMax_angular_speed());
						s2.setAng_y(s2.getAng_y() - interval.doubleValue() * s2.getMax_angular_speed());
					} else {
						System.out.println("Unusual state. Not able to move back to resting state");
					} 
					
					
				}
				
				
				// IT IS POSSIBLE THAT WE FIND THAT THE RETURN TO NEUTRAL POSITION WORKS WELL... AND SO WE WANT 
				// TO STOP ANY MORE ADJUSTMENTS
				//
				// If velocity < lower_velocity_threshold, then start to reduce acceleration
				if ( Math.abs(180 * rotation_velocity_local.getEntry(0)/Math.PI) <  lower_velocity_threshold &&
						Math.abs(180 * rotation_velocity_local.getEntry(2)/Math.PI) <  lower_velocity_threshold) {
					System.out.println("WAS MOVING TO NEUTRAL, BUT SUCCESSFULLY REDUCING VELOCITY! WILL STOP");
				
					set_course = 0;
					
					/*
					double angle1 = Math.acos(Math.cos(s1.getAng_y()) * Math.cos(s2.getAng_y()) + Math.sin(s1.getAng_y()) * Math.sin(s2.getAng_y()));
					resting_angle_move = (Math.PI - angle1)/2;
					System.out.println("RESTING ANGLE: " + resting_angle_move);
					ease_back_timer = 0.0;  // Potentially delay the easing
					
					
					s1.setAng_y(utils.angle_reorg(s1.getAng_y()));
					s2.setAng_y(utils.angle_reorg(s2.getAng_y()));
					
					if (s2.getAng_y() >= s1.getAng_y()) {
						s1_direction = 1; // CW
						s2_direction = 2; // CCW
					} else if (s2.getAng_y() < s1.getAng_y()) {
						s1_direction = 2; // CW
						s2_direction = 1; // CCW
					}
					*/
				}
				
				// System.out.println("BACK TO NEUTRAL S1 ANGLE: " + s1.getAng_y());
				// System.out.println("BACK TO NEUTRAL S2 ANGLE: " + s2.getAng_y());
			}			
			
			
			// If the course needs setting, we find out what it should be....
			// but FIRST we move the middle position of the two masses to the direction
			// This helps to reduce instabilities introduced
			if (set_course == 3) {
				
				/// Find angle between the two smoothers...then halve...this is the mid-point
				double mid_point_angle = Math.acos(Math.cos(s1.getAng_y()) * Math.cos(s2.getAng_y()) + Math.sin(s1.getAng_y()) * Math.sin(s2.getAng_y()));  
				double mid_point_direction = mid_point_angle /2 + s1.getAng_y();
				
					
				
				// Deduce the distance we need 
				intermediate_move = Math.abs(corrective_angle - mid_point_direction);
				
				// If Greater than Pi, then we are being in-efficient
				if (intermediate_move >= Math.PI) {
					intermediate_move = intermediate_move - Math.PI;
				} 
				
				
				// s1_direction = 1;  // 0 - No movement, 1 = CCW, 2 = CW
				// s2_direction = 1;  // 0 - No movement, 1 = CCW, 2 = CW
				
				if (intermediate_move <= Math.PI/2 && corrective_angle <= mid_point_direction) {
					s1_direction = 2;
					s2_direction = 2;
				} else if (intermediate_move > Math.PI/2 && corrective_angle <= mid_point_direction) {
					s1_direction = 1;
					s2_direction = 1;
					intermediate_move = Math.PI - intermediate_move;
				} else if (intermediate_move <= Math.PI/2 && corrective_angle > mid_point_direction) {
					s1_direction = 1;
					s2_direction = 1;
				} else if (intermediate_move > Math.PI/2 && corrective_angle > mid_point_direction) {
					s1_direction = 2;
					s2_direction = 2;
					intermediate_move = Math.PI - intermediate_move;
				}
				
				
				
				System.out.println("Midpoint Angle: " + mid_point_angle);
				System.out.println("Intermediate move (distance): " + intermediate_move);
				System.out.println("S1 Direction: " + s1_direction);
				System.out.println("S2 Direction: " + s2_direction);
				
				// Signal to code to go on to 'Intermediate' move
				set_course = 4;

			}
			
			

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
					
					// System.out.println("New S1 Diff: " + s1_diff);
					// System.out.println("New S2 Diff: " + s2_diff);
					// System.out.println("final_angle_move = " + final_angle_move);
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
					
				// System.out.println("S1 ANGLE: " + s1.getAng_y());
				// System.out.println("S2 ANGLE: " + s2.getAng_y());
				
			}
			
			// Course is known...now we need to 'move' there
			// No smarts in how to get there....Just get us there.
			if (set_course == 5) {

				
				// If corrective Angle > 0
				final_angle_move = final_angle_move - interval.doubleValue() * s1.getMax_angular_speed();
					
				if (final_angle_move < 0) {
					set_course = 6; 
					
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
					
				// System.out.println("FINAL S1 ANGLE: " + s1.getAng_y());
				// System.out.println("FINAL S2 ANGLE: " + s2.getAng_y());
				

				
				// IT IS POSSIBLE THAT WE FIND THAT THE ADJUSTMENT IS WORKING WELL... AND SO WE WANT 
				// TO EASE BACK BEFORE GETTING ALL THE WAY OUT
				//
				// If velocity < lower_velocity_threshold, then start to reduce acceleration
				if (Math.abs(180 * rotation_velocity_local.getEntry(0)/Math.PI) <  lower_velocity_threshold &&
						Math.abs(180 * rotation_velocity_local.getEntry(2)/Math.PI) <  lower_velocity_threshold) {
					System.out.println("STILL MOVING OUT, BUT SUCCESSFULLY REDUCING VELOCITY! Need to ease back back");
				
					set_course = 8;
					double angle1 = Math.acos(Math.cos(s1.getAng_y()) * Math.cos(s2.getAng_y()) + Math.sin(s1.getAng_y()) * Math.sin(s2.getAng_y()));
					resting_angle_move = (Math.PI - angle1)/2;
					System.out.println("RESTING ANGLE: " + resting_angle_move);
					ease_back_timer = 0.0;  // Potentially delay the easing
					
					
					s1.setAng_y(utils.angle_reorg(s1.getAng_y()));
					s2.setAng_y(utils.angle_reorg(s2.getAng_y()));
					
					if (s2.getAng_y() >= s1.getAng_y()) {
						s1_direction = 1; // CW
						s2_direction = 2; // CCW
					} else if (s2.getAng_y() < s1.getAng_y()) {
						s1_direction = 2; // CW
						s2_direction = 1; // CCW
					}
				}
				
			}			
			
			
			
			
			// We try to see how the acceleration is going....do we need to back off a little
			// i.e. we want to 'moderate' the deacceleration
			if ((n % data_ticks) == 0 && set_course == 6) {
				// First make sure that acceleration is in opposite direction of velocity (i.e. it is slowing down)
				if ((Math.signum(rotation_acceleration_local.getEntry(0)) * Math.signum(rotation_velocity_local.getEntry(0)) != -1 && Math.abs(180 * rotation_velocity_local.getEntry(0)/Math.PI) > upper_velocity_threshold) 
						|| 
				    (Math.signum(rotation_acceleration_local.getEntry(2)) * Math.signum(rotation_velocity_local.getEntry(2)) != -1 && Math.abs(180 * rotation_velocity_local.getEntry(2)/Math.PI) > upper_velocity_threshold)) {
				
					

					System.out.println("NOT REDUCING VELOCITY. EITHER malfunction in code, or change in forces or we are now over correcting!");
					
					// So. let's assume we are over-correcting
					set_course = 9;
					resting_angle_move = Math.PI;
					
					s1.setAng_y(utils.angle_reorg(s1.getAng_y()));
					s2.setAng_y(utils.angle_reorg(s2.getAng_y()));
					
					if (s2.getAng_y() >= s1.getAng_y()) {
						s1_direction = 1; // CW
						s2_direction = 2; // CCW
					} else if (s2.getAng_y() < s1.getAng_y()) {
						s1_direction = 2; // CW
						s2_direction = 1; // CCW
					}					
					
				}
				
				

				
				// If velocity < lower_velocity_threshold, then start to reduce acceleration
				if ( Math.abs(180 * rotation_velocity_local.getEntry(0)/Math.PI) <  lower_velocity_threshold &&
						Math.abs(180 * rotation_velocity_local.getEntry(2)/Math.PI) <  lower_velocity_threshold) {
					System.out.println("SUCCESSFULLY REDUCING VELOCITY! Need to ease back back");
				
					set_course = 8;
					resting_angle_move = Math.PI/2;
					ease_back_timer = 0.0;  // Potentially delay the easing back
					
					
					s1.setAng_y(utils.angle_reorg(s1.getAng_y()));
					s2.setAng_y(utils.angle_reorg(s2.getAng_y()));
					
					if (s2.getAng_y() >= s1.getAng_y()) {
						s1_direction = 1; // CW
						s2_direction = 2; // CCW
					} else if (s2.getAng_y() < s1.getAng_y()) {
						s1_direction = 2; // CW
						s2_direction = 1; // CCW
					}
				}				
				
				
				
			}
			
			
			
			// We want to 'ease' off, but we want to wait a fraction of a second to allow for correction of angular position
			if (set_course == 8) {
				ease_back_timer = ease_back_timer - interval.doubleValue();
				if (ease_back_timer <= 0) {
					set_course = 9;
					System.out.println("EASING BACK NOW");
				}				
				
			}
			
			
			// From results in (set_course == 6), we now need to make adjustments
			if (set_course == 9) {
				resting_angle_move = resting_angle_move - interval.doubleValue() * s1.getMax_angular_speed();
				
				System.out.println("S1:" + utils.angle_reorg(s1.getAng_y()));
				System.out.println("S2:" + utils.angle_reorg(s2.getAng_y()));
				if (s1_direction == 1) {
					s1.setAng_y(s1.getAng_y() - interval.doubleValue() * s1.getMax_angular_speed());
					s2.setAng_y(s2.getAng_y() + interval.doubleValue() * s2.getMax_angular_speed());
				} else if (s1_direction == 2) {
					s1.setAng_y(s1.getAng_y() + interval.doubleValue() * s1.getMax_angular_speed());
					s2.setAng_y(s2.getAng_y() - interval.doubleValue() * s2.getMax_angular_speed());
				} else {
					System.out.println("Unusual state. Not able to move back to resting state");
				}
				
				// System.out.println("RESTING S1 ANGLE: " + s1.getAng_y());
				// System.out.println("RESTING S2 ANGLE: " + s2.getAng_y());
				
				if (resting_angle_move <= 0 || (Math.abs(180 * rotation_acceleration_local.getEntry(0)/Math.PI) < 5 && Math.abs(180 * rotation_acceleration_local.getEntry(2)/Math.PI) < 5)) {
					set_course = 0;
					System.out.println("Back to a semi-stable state, " + resting_angle_move + ", " + Math.abs(rotation_acceleration_local.getEntry(0)) + ", " + Math.abs(rotation_acceleration_local.getEntry(2)));
				}
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
			utils.debug(time, "S1:" + utils.angle_reorg(s1.getAng_y()));
			utils.debug(time, "S2:" + utils.angle_reorg(s2.getAng_y()));
			utils.debug(time,  "Course Step: " + set_course);
			
			
			// Blank line(s) between intervals
			utils.debug(time, "");
			utils.debug(time, "");
			
			
			// The rocket starts off at the origin with the bottom of the rocket at 0,0,0
			// The rocket is :-
			//   -- First Rotated ABOUT the origin 0,0,0
			//   -- Second, it is translated 
			//
			// We need to remember that the rotation is ABOUT the point 0,0,0 and the rocket sits VERY close
			// to 0,0,0. This means that order of the transforms is important.
			// 
			// First
			//   - The rotation MUST happen first
			//   - The rocket MUST start at 0,0     
			//
			// In Summary, do NOT rotate when the rocket is not at 0,0,0 !!!!
			//
			
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
