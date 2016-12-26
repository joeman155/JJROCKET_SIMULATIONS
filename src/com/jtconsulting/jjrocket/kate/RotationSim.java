package com.jtconsulting.jjrocket.kate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
import com.jtconsulting.jjrocket.kate.rotationData;
import com.sun.j3d.utils.applet.MainFrame;



public class RotationSim {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		boolean END_OF_FLIGHT = false;
		long flight_duration = 16000;
		double time_slice;   		  // How long each time slice is. 
		double new_rotx_vel = 0;
		double new_roty_vel = 0;
		double new_rotz_vel = 0;
		double new_ax = 0;
		double new_ay = 0;
		double new_az = 0;		
		
		int y_intercept = 0;   // Where on y-Axis it starts
		int multiplier = 4;	// How far away we are from rocket
		
		
		// CSV FILE - LOAD DATA IN
		Map<Long, rotationData> rotMap = new HashMap<Long, rotationData>();
		Map<Long, accelerationData> accMap = new HashMap<Long, accelerationData>();
		String dataFile = "c:\\tmp\\data.csv";
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ",";
		
		try {
			br = new BufferedReader (new FileReader(dataFile));
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split(csvSplitBy);
				
				/*
				System.out.print(lineParts[0]);
				System.out.print("   ");
				System.out.print(lineParts[1]);
				System.out.print("   ");
				System.out.print(lineParts[2]);
				System.out.print("   ");
				System.out.println(lineParts[3]);
				*/
				
				
				// d1, d2, d3 = Rotation rates (radians per second)
				// e1, e2, e3 = Acceleration rates ms-2
				double d1, d2, d3, e1, e2, e3;
				d1 = Double.parseDouble(lineParts[1]);
				d2 = Double.parseDouble(lineParts[2]);
				d3 = Double.parseDouble(lineParts[3]);
				
				
				// If there are 3 more numbers after the rotation rates, then these are treated as 
				// acceleration numbers....but only retrieve and process these if they are there.
				e1 = 0;
				e2 = 0;
				e3 = 0;
				try {
					e1 = Double.parseDouble(lineParts[4]);
					e2 = Double.parseDouble(lineParts[5]);
					e3 = Double.parseDouble(lineParts[6]);
				} catch(Exception e) {
				}
				
			
				long l0;
				l0 = Long.parseLong(lineParts[0]);
				
				
				rotationData     d = new rotationData();
				accelerationData e = new accelerationData();
				d.setRotation(d1, d2, d3);
				e.setAcceleration(e1, e2, e3);
				rotMap.put(l0, d);
				accMap.put(l0, e);
				
					
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Object[] times = rotMap.keySet().toArray();
		Arrays.sort(times);		
		
		
		/*
		for (Object time : times) {
			System.out.println("Key: " + time.toString());
		}
		*/		

		
		
		System.out.print("Rotation Simulation Starting...");
		
		// Objects we repeatedly use
		RealMatrix rotationMatrix;		
		
		// Java 3-D
		TransformGroup rocket_system = new TransformGroup();
		Transform3D trans = new Transform3D();
		TransformGroup viewTransformGroup = new TransformGroup();
		Transform3D viewtranslation = new Transform3D();
		
		
		Rocket r = new Rocket();
		r.setLength(1.46);
		r.setMass(1.113);
		r.setRadius_external(0.028);
		r.setRadius_internal(0.025);
		r.setPitch(Math.PI/2);
		r.setYaw(0);
		r.setRoll(0);
		r.setPosition(0.0,0.0,0.0);
//		r.setMotor(m1);
		r.setAng_x(0);
		r.setAng_y(0);
		r.setAng_z(0);
		r.setAng_vx(0);
		r.setAng_vy(0);
		r.setAng_vz(0);		
		
		new_ax = 0; new_ay = 10; new_az = 0;
		double[] local_accel1 = {new_ax, new_ay, new_az};
		System.out.println("Orig x, y, z = " + new_ax + "," + new_ay + ","  + new_az);
		RealVector local_accel_vector1 = MatrixUtils.createRealVector(local_accel1);
		RealVector global_accel_vector1 = utils.matrixVectorMultiply(r.getRotationMatrix(), local_accel_vector1);
		
		
		new_ax = global_accel_vector1.getEntry(0);
		new_ay = global_accel_vector1.getEntry(1);
		new_az = global_accel_vector1.getEntry(2);
		
		System.out.println("FINAL x, y, z = " + new_ax + "," + new_ay + ","  + new_az);
		
		// Initialise 3D Canvas
		// threedCanvas canvas = new threedCanvas(rocket_system, viewTransformGroup, r, -7, 3, -3,0,0,0,0,1,0);
		threedCanvas canvas = new threedCanvas(rocket_system, viewTransformGroup, r, 5 * multiplier, 1 * multiplier,5 * multiplier, 0,y_intercept,0, 0,1,0);
		new MainFrame(canvas, 1280, 1024);
		

		System.out.println("Starting Iterations...");
		long current_time;
		long milliseconds_since_flight = 0, milliseconds_since_flight_old = 0;
		long start_time = System.currentTimeMillis();
		long current_key = 0;
		// for (int n = 0; n < num_intervals; n++) {
		while (! END_OF_FLIGHT) {
			current_time = System.currentTimeMillis();
			
			milliseconds_since_flight = current_time - start_time;
			time_slice = (double) (milliseconds_since_flight - milliseconds_since_flight_old)/1000;
			System.out.println("Flight Time: " + milliseconds_since_flight);
			milliseconds_since_flight_old = milliseconds_since_flight;
			
			if (milliseconds_since_flight > flight_duration) {
		    	END_OF_FLIGHT = true;
		    }

		    
		    
		    // Determine where we are up in the flight (referring to CSV data).
			long c1 = milliseconds_since_flight * 1000;
			

			
			
			for (Object time : times) {
				long c2 = Long.parseLong(time.toString());
				
				// Reset values to zero...so it doesn't keep moving after data points
				new_rotx_vel = 0; new_roty_vel = 0; new_rotz_vel = 0;
				new_ax = new_ay = new_az = 0;
				
				// System.out.println("Comparing " + c1 + " with " + c2);
				if (c2 > milliseconds_since_flight * 1000) {
					
					// Get rotational velocities				
					long k = Long.parseLong(time.toString());
					rotationData rotdata = rotMap.get(k);
					new_rotx_vel = rotdata.getRot_vx();
					new_roty_vel = rotdata.getRot_vy();
					new_rotz_vel = rotdata.getRot_vz();
					
					accelerationData accdata = accMap.get(k);
					new_ax       = accdata.getax();
					new_ay       = accdata.getay();
					new_az       = accdata.getaz();
					System.out.println("Orig x, y, z = " + new_ax + "," + new_ay + ","  + new_az);
					
					double[] local_accel = {new_ax, new_ay, new_az};
					RealVector local_accel_vector = MatrixUtils.createRealVector(local_accel);
					RealVector global_accel_vector = utils.matrixVectorMultiply(r.getRotationMatrix(), local_accel_vector);
					
					
					new_ax = global_accel_vector.getEntry(0);
					new_ay = global_accel_vector.getEntry(1);
					new_az = global_accel_vector.getEntry(2);
					System.out.println("FINAL x, y, z = " + new_ax + "," + new_ay + ","  + new_az);
					
/*					
					if (Math.abs(new_ax) < 1 ) {
						new_ax = 0;
					}
					
					if (Math.abs(new_ay) < 1 ) {
						new_ay = 0;
					}
					
					if (Math.abs(new_az) < 1 ) {
						new_az = 0;
					}
*/
					

					
					
					
					
					// System.out.print("Key: " + time.toString());
					// System.out.println("                      ------- " + rotMap.get(k).getRot_vx());
					
					break;
				}
			}
		
			
			
			// Update state of whole rocket
			r.updateRotationMotionState(new_rotx_vel, new_roty_vel, new_rotz_vel, 
					                    new_ax,       new_ay,       new_az, 
					                    time_slice);			
			
			
			
			
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
				TimeUnit.MILLISECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				
		}
		
		
		System.out.println("Finished simulation");
		
		
	}
	
}
