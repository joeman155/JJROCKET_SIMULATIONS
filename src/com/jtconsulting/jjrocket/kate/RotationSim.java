package com.jtconsulting.jjrocket.kate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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



public class RotationSim {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		boolean END_OF_FLIGHT = false;
		long flight_duration = 8000;
		double time_slice = 0.0001;   // How long each time slice is. 
		double total_time = 5;        // How long we do the simulation for
		int    num_intervals = (int) (total_time/time_slice);
		
		
		// CSV FILE - LOAD DATA IN
		String dataFile = "/tmp/data.csv";
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ",";
		
		try {
			br = new BufferedReader (new FileReader(dataFile));
			while ((line = br.readLine()) != null) {
				System.out.print(line);
					
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		// TIMING
		BigDecimal interval = new BigDecimal(String.valueOf(time_slice));
		BigDecimal time = new BigDecimal("0.0");		
		
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
		r.setPosition(0.3,0.5,0.3);
//		r.setMotor(m1);
		r.setAng_x(0);
		r.setAng_y(0);
		r.setAng_z(0);
		r.setAng_vx(0.1);
		r.setAng_vy(0);
		r.setAng_vz(0);		
		
		
		// Initialise 3D Canvas
		threedCanvas canvas = new threedCanvas(rocket_system, viewTransformGroup, r, 5, 5,5,0,0,0,0,1,0);
		new MainFrame(canvas, 1280, 1024);
		

		System.out.println("Starting Iterations...");
		long current_time;
		long milliseconds_since_flight;
		long start_time = System.currentTimeMillis();
		// for (int n = 0; n < num_intervals; n++) {
		while (! END_OF_FLIGHT) {
			current_time = System.currentTimeMillis();
			milliseconds_since_flight = current_time - start_time;
			
			if (milliseconds_since_flight > flight_duration) {
		    	END_OF_FLIGHT = true;
		    }

		    
		    
		    // Determine where we are up in the flight (referring to CSV data).
		
		
			
			
			double new_rotx_vel = 0.1;
			double new_roty_vel = 0;
			double new_rotz_vel = 0;
			
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
			
			
		}
		
		
		System.out.println("Finished simulation");
		
		
	}
	
}
