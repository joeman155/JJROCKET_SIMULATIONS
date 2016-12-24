package com.jtconsulting.jjrocket.kate;

import java.math.BigDecimal;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class utils {

	
	// This uses Tait-Bryan angles Z1X2Y3
	// 
	// See https://en.wikipedia.org/wiki/Euler_angles#Intrinsic_rotations
	//
	// Go to the "Rotation matrix" section and find it on the right.
	//
	public static RealMatrix createRotationMatrix(double xaxis, double yaxis, double zaxis) {
		// Note, roll is really rotation around X-Axis
		//       pitch is really rotation around Y-Axis
		//       yaw is really rotation around Z-Axis
		double c1, c2, c3;  // Cosine of roll (1), pitch (2), yaw (3)
		double s1, s2, s3;  // Sine of roll (1), pitch (2), yaw (3)
		
		/*
		c1 = Math.cos(roll);
		c2 = Math.cos(pitch);
		c3 = Math.cos(yaw);
		s1 = Math.sin(roll);
		s2 = Math.sin(pitch);
		s3 = Math.sin(yaw);
		

		double[][] matrix_data = {
				{c3 * c2,  (s1 * c3 * s2 - c1 * s3), (s1 * s3 + c1 * c3 * s2)},
		        {s3 * c2,  (c3 * c1 + s3 * s2 * s1), (s3 * s2 * c1 - c3 * s1)},
		        {(-s2)  ,  (s1 * c2),                (c2 * c1)}
		        };
		*/
		
		
		
		c1 = Math.cos(zaxis);
		c2 = Math.cos(xaxis);
		c3 = Math.cos(yaxis);
		s1 = Math.sin(zaxis);
		s2 = Math.sin(xaxis);
		s3 = Math.sin(yaxis);
		
		double rotation_matrix_data[][] = { 
				{(c1 * c3 - s1 *s2 * s3), (-c2 * s1), (c1 * s3 + c3 * s1 * s2)   },
				{(c3 * s1 + c1 *s2 * s3), (c1 * c2) , (s1 * s3 - c1 * c3 * s2)   },
				{(-c2 * s3)             , (s2)      , (c2 * c3)}
		};	
		
		RealMatrix matrix = MatrixUtils.createRealMatrix(rotation_matrix_data);
		
		return matrix;
	}
	
	public static double DotProductAngle(double x1, double y1, double z1,
			                      double x2, double y2, double z2) {
		
		double angle;
		double vector_1_mag = Math.pow(x1 * x1 + y1 * y1 + z1* z1,  0.5);
		double vector_2_mag = Math.pow(x2 * x2 + y2 * y2 + z2* z2,  0.5);
		
		angle = Math.acos((x1 * x2 + y1 * y2 + z1 * z2)/(vector_1_mag * vector_2_mag));
		
		return angle;
	}
	
	public static RealVector revolveVector(double roll, double pitch, double yaw, RealVector vec1) {
	
		double c1, c2, c3;  // Cosine of roll (1), pitch (2), yaw (3)
		double s1, s2, s3;  // Sine of roll (1), pitch (2), yaw (3)
		
		c1 = Math.cos(roll);
		c2 = Math.cos(pitch);
		c3 = Math.cos(yaw);
		s1 = Math.sin(roll);
		s2 = Math.sin(pitch);
		s3 = Math.sin(yaw);
		
		double vec1_x = vec1.getEntry(0);
		double vec1_y = vec1.getEntry(1);
		double vec1_z = vec1.getEntry(2);

		double[] vec2_data = {c3 * c2 * vec1_x + (s1 * c3 * s2 - c1 * s3) * vec1_y + (s1 * s3 + c1 * c3 * s2) * vec1_z,
		             s3 * c2 * vec1_x + (c3 * c1 + s3 * s2 * s1) * vec1_y + (s3 * s2 * c1 - c3 * s1) * vec1_z,
		             (-s2) * vec1_x + s1 * c2 * vec1_y + c2 * c1 * vec1_z};
		
		RealVector vec2 = MatrixUtils.createRealVector(vec2_data);
		
		return vec2;
	}
	
	public static RealVector crossProduct(RealVector vec1, RealVector vec2) {
		
        double[] result_vector_data = {vec1.getEntry(1) * vec2.getEntry(2) - vec1.getEntry(2) * vec2.getEntry(1),
        		vec1.getEntry(0) * vec2.getEntry(2) - vec1.getEntry(2) * vec2.getEntry(0),
        		vec1.getEntry(0) * vec2.getEntry(1) - vec1.getEntry(1) * vec2.getEntry(0)
        		};

        	// Moment due to Spin2
        	RealVector result_vector = MatrixUtils.createRealVector(result_vector_data);
        	
        	return result_vector;

	}

	public static RealVector matrixVectorMultiply(
			RealMatrix m, RealVector v) {
		double[][] md = {
				{m.getEntry(0, 0), m.getEntry(0, 1), m.getEntry(0, 2)  },
				{m.getEntry(1, 0), m.getEntry(1, 1), m.getEntry(1, 2)  },
				{m.getEntry(2, 0), m.getEntry(2, 1), m.getEntry(2, 2)  }
		};
		
		// System.out.println(m.getEntry(0, 0)+ ", " + m.getEntry(0, 1) + ", " + m.getEntry(0, 2));
		// System.out.println(m.getEntry(1, 0)+ ", " + m.getEntry(1, 1) + ", " + m.getEntry(1, 2));
		// System.out.println(m.getEntry(2, 0)+ ", " + m.getEntry(2, 1) + ", " + m.getEntry(2, 2));
		
		double[] vd = {v.getEntry(0), v.getEntry(1), v.getEntry(2)};
		
		
		double[] result_data = {
				md[0][0] * vd[0] + md[0][1] * vd[1] + md[0][2] * vd[2],
				md[1][0] * vd[0] + md[1][1] * vd[1] + md[1][2] * vd[2],
				md[2][0] * vd[0] + md[2][1] * vd[1] + md[2][2] * vd[2]};
		
		RealVector result = MatrixUtils.createRealVector(result_data);
		
		return result;
	}
	
	
	public static void debug(BigDecimal time, String str) {
		// Print out results every 0.01 seconds. Less debugging!
		
		BigDecimal time_split = new BigDecimal("0.001");
		BigDecimal result = time.divide(time_split);
		BigDecimal result_rounded = result.setScale(0, BigDecimal.ROUND_DOWN);
		BigDecimal mod = result.subtract(result_rounded).multiply(time_split);
		
		
		
		// System.out.println("Modulus " + mod);
		if (mod.doubleValue() == 0)  {
			System.out.println(str);
		}
	}
	
	public static double angle_reorg(double angle) {
		double new_angle;
		
		if (Math.abs(angle) >= 2 * Math.PI) {
			new_angle = 2 * Math.PI * ((angle/(2 * Math.PI)) - Math.round(angle/(2 * Math.PI)));
		} else {
			new_angle = angle;
		}
		
		if (new_angle < 0) {
			new_angle = new_angle + Math.PI * 2;
		}
		return new_angle;
		
	}
	
}
