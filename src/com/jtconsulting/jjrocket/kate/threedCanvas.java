package com.jtconsulting.jjrocket.kate;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.geometry.Cylinder;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.PointLight;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.apache.commons.math3.linear.RealMatrix;

import java.awt.GraphicsConfiguration;
import java.awt.BorderLayout;
import java.awt.Label;
import java.applet.Applet;
import com.sun.j3d.utils.applet.MainFrame;

public class threedCanvas extends Applet {

	
	
    /**
     * 
     */
    public threedCanvas(TransformGroup tg_parent, TransformGroup viewTransformGroup, Rocket r,
    		int v1, int v2, int v3, int v4, int v5, int v6, int v7, int v8, int v9)  { 
        
        setLayout(new BorderLayout());
        GraphicsConfiguration config = 
            SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas = new Canvas3D(config);  
        add("North",new Label("This is the top"));
        add("Center", canvas);
        add("South",new Label("This is the bottom"));
        
        
        
  	   //ViewingPlatform viewPlatform = universe.getViewingPlatform();
       ViewingPlatform viewingPlatform = new ViewingPlatform();
       viewingPlatform.getViewPlatform().setActivationRadius(300f);        
       TransformGroup viewTransform = viewingPlatform.getViewPlatformTransform();
  	   Transform3D view3d = new Transform3D();
  	   viewTransform.getTransform(view3d);
  	   // view3d.lookAt(new Point3d(52,16,52),  new Point3d(0,20,0),  new Vector3d(0,1,0));
  	   view3d.lookAt(new Point3d(v1,v2,v3),  new Point3d(v4,v5,v6),  new Vector3d(v7,v8,v9));
  	   view3d.invert();
  	   viewTransform.setTransform(view3d);
  	   Viewer viewer = new Viewer(canvas);
  	   View view = viewer.getView();
  	   view.setBackClipDistance(300);
  	   
        
       
       
       
 	   // INIT
  	   SimpleUniverse universe = new SimpleUniverse(viewingPlatform, viewer);
 	   BranchGroup group = new BranchGroup();
 	   TransformGroup axis = new TransformGroup();
 	   TransformGroup tg_top = new TransformGroup();
 	   
 	   
 	   // LIGHT
       //DirectionalLight light = new DirectionalLight( true,
       //        new Color3f(1.0f, 1.0f, 1.0f),
       //        new Vector3f(4.0f, -3.0f, 3.0f));
       // new Vector3f(-2.0f, -2.0f, -2.0f));
 	   // AmbientLight light = new AmbientLight(new Color3f(1.0f, 1.0f, 1.0f));
 	   PointLight light = new PointLight();
 	   light.setPosition(new Point3f(20.0f, 6.0f, 20.0f));
 	   light.setColor(new Color3f(1.0f, 1.0f, 1.0f));
 	   light.setAttenuation(0.0f, 0.0f, 0.0f);

       light.setInfluencingBounds(new BoundingSphere(new Point3d(), 2100.0));	          
       group.addChild(light);
        

 	   // MATERIAL - Marking
       Appearance red_ap = new Appearance();
       Material red_ma = new Material();
       red_ma.setDiffuseColor(1.0f, 0.0f, 0.0f);
       red_ap.setMaterial(red_ma);
       LineAttributes myLA = new LineAttributes( );
       myLA.setLineWidth( 3.0f );
       red_ap.setLineAttributes( myLA );
       
       

 	   // MATERIAL - Rocket
       Appearance ap = new Appearance();
       Material ma = new Material();
       ma.setDiffuseColor(0.0f, 0.0f, 1.0f);
       ap.setMaterial(ma);
       
        
 	   // MATERIAL - Spinner rockets
       //Appearance ap_sp1 = new Appearance();
       //Material ma_sp1 = new Material();
       //ma_sp1.setDiffuseColor(1.0f, 0.0f, 0.0f);
       //ap_sp1.setMaterial(ma_sp1);     
       
       
 	   // MATERIAL - AXIS
       Appearance ap_axis = new Appearance();
       Material ma_axis = new Material();
       ma_axis.setDiffuseColor(0.0f, 1.0f, 0.0f);
       ap_axis.setMaterial(ma_axis);   
       
        
       // All rockets
 	   Cylinder rocket =  new Cylinder((float) r.getRadius_external() * 6, (float) r.getLength(), ap);

 	   
 	   // Line 
 	   LineArray line = new LineArray(2, LineArray.COORDINATES);
 	   line.setCoordinate(0, new Point3d(r.getRadius_external() * 6,  -r.getLength()/2, 0d));
 	   line.setCoordinate(1, new Point3d(r.getRadius_external() * 6,  r.getLength()/2, 0d ));
 	   Shape3D line_shape = new Shape3D(line, red_ap);
 	   
 	   
 	   // ENTIRE TRANSFORM GROUP
 	   //TransformGroup tg_parent = new TransformGroup();
 	   tg_parent.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 	   viewTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 	   
 	   
 	   // Move the tg_parent, dependng upon where the rocket is (translation)
 	   Transform3D translation = new Transform3D();
 	   Vector3f translation_vector = new Vector3f((float) r.getX(), (float) r.getY(), (float) r.getZ());
 	   translation.setTranslation(translation_vector);
 	   tg_top.setTransform(translation);
 	    	   
 	   
 
       // Y-Axis
 	   Cylinder yaxis =  new Cylinder((float) 0.08, (float) 80, ap_axis);
 	   axis.addChild(yaxis);
 	   
 	   // Y-Axis
 	   Cylinder xaxis =  new Cylinder((float) 0.08, (float) 60, ap_axis);
 	   TransformGroup xaxis_tg = new TransformGroup();
 	   Transform3D xaxis_rotation = new Transform3D();
 	   xaxis_rotation.rotZ(Math.PI/2);
 	   xaxis_tg.setTransform(xaxis_rotation);
 	   xaxis_tg.addChild(xaxis);
 	   axis.addChild(xaxis_tg); 	   

 	   // Z-Axis
 	   Cylinder zaxis =  new Cylinder((float) 0.02, (float) 60, ap_axis);
 	   TransformGroup zaxis_tg = new TransformGroup();
 	   Transform3D zaxis_rotation = new Transform3D();
 	   zaxis_rotation.rotX(Math.PI/2);
 	   zaxis_tg.setTransform(zaxis_rotation);
 	   zaxis_tg.addChild(zaxis);
 	   axis.addChild(zaxis_tg); 	
 	   
 	   
 	   
 	   // Combining all Transform Groups
 	   tg_parent.addChild(tg_top);
 	   tg_top.addChild(rocket);
 	   tg_top.addChild(line_shape);
 	   group.addChild(tg_parent);
 	   group.addChild(axis);
 	   group.compile();
 	   
 	   

 	   universe.addBranchGraph(group);
 	   
    }
}
