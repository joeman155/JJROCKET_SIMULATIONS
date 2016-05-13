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
		long flight_duration = 8000;
		double time_slice;   		  // How long each time slice is. 
		double new_rotx_vel = 0;
		double new_roty_vel = 0;
		double new_rotz_vel = 0;
		
		
		// CSV FILE - LOAD DATA IN
		Map<Long, rotationData> rotMap = new HashMap<Long, rotationData>();
		String dataFile = "/tmp/data.csv";
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
				
				
				double d1, d2, d3;
				d1 = Double.parseDouble(lineParts[1]);
				d2 = Double.parseDouble(lineParts[2]);
				d3 = Double.parseDouble(lineParts[3]);
				long l0;
				l0 = Long.parseLong(lineParts[0]);
				
				rotationData d = new rotationData();
				d.setRotation(d1, d2, d3);
				rotMap.put(l0, d);
				
					
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
		r.setAng_vx(0.1);
		r.setAng_vy(0);
		r.setAng_vz(0);		
		
		
		// Initialise 3D Canvas
		threedCanvas canvas = new threedCanvas(rocket_system, viewTransformGroup, r, -7, 3, -3,0,0,0,0,1,0);
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
				// System.out.println("Comparing " + c1 + " with " + c2);
				if (c2 > milliseconds_since_flight * 1000) {
					
					// Get rotational velocities				
					long k = Long.parseLong(time.toString());
					rotationData data = rotMap.get(k);
					new_rotx_vel = data.getRot_vx();
					new_roty_vel = data.getRot_vy();
					new_rotz_vel = data.getRot_vz();
					
					// System.out.print("Key: " + time.toString());
					// System.out.println("                      ------- " + rotMap.get(k).getRot_vx());
					
					break;
				}
			}
		
			
			
			// Update state of whole rocket
			r.updateRotationMotionState(new_rotx_vel, new_roty_vel, new_rotz_vel, time_slice);			
			
			
			
			
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
