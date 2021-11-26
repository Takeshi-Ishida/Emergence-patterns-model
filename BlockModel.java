// Source code of the following paper.
// 
// Takeshi Ishida,
// Emergence of Turing Patterns in a Simple Cellular Automata-Like Model via Exchange of Integer Values between Adjacent Cells,
// Discrete Dynamics in Nature and Society, Volume 2020, Article ID 2308074, 12 pages (2020.1)
// https://doi.org/10.1155/2020/2308074
// Copy right : Takeshi Ishida, 2018

package BlockModelTuring;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class BlockModel extends JFrame{

    private static final long serialVersionUID=1L;

    // ******* Setting the constant values ****************
    final static int x0 = 20;                     // Origin of the graphic coordinates
    final static int y0 = 40;
    final static int pich = 8 ;                   // Interval of cell (dot)
    final static int meshNumMax = 100 ;           // Vertical and horizontal max cell number

    final static int meshNum=100 ;                // Vertical and horizontal cell number
    final static int state =12;                   // Number of states (12*2=24)
    final static int layer =2;                    // Number of token types (1 type in this paper) 

    /************** constructor *****************/
	public  BlockModel() {
        // Window size and captions 
		setSize(1200,900) ;
		setTitle("Turing pattern Model");

		// Stop when the "x" button is pressed
		addWindowListener(new WindowAdapter() {
			public void windowsClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		// Get the container and paste the panel 
		MyJPanel myJPanel = new MyJPanel();
		Container c = getContentPane();     //  
		c.add(myJPanel);                    //  paste the panel
	}
	
	/******************* main ******************/
	public static void main(String[] args) {
		// Object class generation 
		JFrame w = new  BlockModel();
        w.show();
	}

	/****************** MyJPanel ****************/
	public class MyJPanel extends JPanel implements ActionListener {
		
		//  Class variables 
 		JButton calcButton ;    //  Generate calculation button 
 		JButton stopButton ;    //  Generate stop button
 	    Timer pTimer ;
 		
        //  Variables used in the model
 		public int ni=0 ;
 		public int nj=0 ;
		public int num_particle1 ;   //  Number of tokens
        
		public int iterate=0 ;       //  Iterate number
        public float range1,range2 ;   // 
        public double w ;  // Morphogenesis parameter  
        public double x ;  // Residual rate of token 
        public double y ;  // Removal rate of token
        public int z ;     // Generation number of token

		//  Declaration of array variables 
		/*******************************************************/
 		public int   mesh_n[][];   // Cell number 
	    public float mesh_x[][];   // X coordinate of cell
	    public float mesh_y[][];   // Y coordinate of cell 
	    public int   mesh_adjacent[][][][] ;   // Adjacent cell number [row][Column][direction][coordinate] 
                                               // direction ; 1 upper left, 2 upper right, 3 right, 4 lower right, 5 lower left, 6 left
		                                       // coordinate ; ni direction,  1 nj direction

	    public int part[][][][];        // Cell state 
	    public int part_next[][][][];   // Cell state (Next step)
		/*******************************************************/

		/**********  Constructor of MyJPanelclass ****************/
		public MyJPanel(){

			// Generation of array variables 
		    mesh_n = new int[meshNumMax+1][meshNumMax+1] ;     // Cell number 	
		    mesh_x = new float[meshNumMax+1][meshNumMax+1] ;   // X coordinate of cell
		    mesh_y = new float[meshNumMax+1][meshNumMax+1] ;   // Y coordinate of cell
		    mesh_adjacent = new int[meshNumMax+1][meshNumMax+1][7][4] ;  // Adjacent cell number [row][Column][direction][coordinate] 
                                                                         // direction ; 1 upper left, 2 upper right, 3 right, 4 lower right, 5 lower left, 6 left
                                                                         // coordinate ; ni direction,  1 nj direction
		    
		 	part = new int[layer+1][meshNumMax+1][meshNumMax+1][state*2+1]  ;   // Cell state 
		 	part_next = new int[layer+1][meshNumMax+1][meshNumMax+1][state*2+1]  ;   // Cell state (Next step)
			
		 	// Setting cell number and cell coordinates 
		 	for (int i = 1; i <= meshNum; i++) {
		 	for (int j = 1; j <= meshNum; j++) {
		 		 mesh_n[i][j]=i + (j-1)*meshNum;
		 		 mesh_x[i][j]=(float) (x0+((j-1)%2)*pich/2+(i-1)*pich);
		 		 mesh_y[i][j]=(float) (y0+ (j-1)*pich*Math.sqrt(3)/2);
		 	}
		 	}

		 	 // Adjacent cell number setting 
		 	  for (int i = 1; i <= meshNum; i++) {
		 	  for (int j = 1; j <= meshNum; j++) {

		 		if ((j-1)%2==1) {
		 		  // 1 : center
		 		  ni= i;
		 		  nj= j;
		 		  mesh_adjacent[i][j][0][0]=ni ;
		 		  mesh_adjacent[i][j][0][1]=nj ;

		 		  // 2 :upper
		 		  ni= i;
		 		  nj= j+(meshNum-1)-((int)((j-1+meshNum-1)/meshNum))*meshNum;
		 		  mesh_adjacent[i][j][1][0]=ni ;
		 		  mesh_adjacent[i][j][1][1]=nj ;

		 		  // 3 :righat
		 		  ni= (i+1)-((int)(i/meshNum))*meshNum;
		 		  nj= j;
		 		  mesh_adjacent[i][j][3][0]=ni ;
		 		  mesh_adjacent[i][j][3][1]=nj ;

		 		  // 4 :lower
		 		  ni= i;
		 		  nj= (j+1)-((int)(j/meshNum))*meshNum;
		 		  mesh_adjacent[i][j][5][0]=ni ;
		 		  mesh_adjacent[i][j][5][1]=nj ;

		 		  // 5 :left
		 		  ni= i+(meshNum-1)-((int)((i-1+meshNum-1)/meshNum))*meshNum;
		 		  nj= j;
		 		  mesh_adjacent[i][j][6][0]=ni ;
		 		  mesh_adjacent[i][j][6][1]=nj ;

		 		  // 6 :upper right
		 		  ni= (i+1)-((int)(i/meshNum))*meshNum;
		 		  nj= j+(meshNum-1)-((int)((j-1+meshNum-1)/meshNum))*meshNum;
		 		  mesh_adjacent[i][j][2][0]=ni ;
		 		  mesh_adjacent[i][j][2][1]=nj ;

		 		  // 7 :lower right
		 		  ni= (i+1)-((int)(i/meshNum))*meshNum;
		 		  nj= (j+1)-((int)(j/meshNum))*meshNum;
		 		  mesh_adjacent[i][j][4][0]=ni ;
		 		  mesh_adjacent[i][j][4][1]=nj ;

		 		  // 8 :lower left
		 		  ni= i+(meshNum-1)-((int)((i-1+meshNum-1)/meshNum))*meshNum;
		 		  nj= (j+1)-((int)(j/meshNum))*meshNum;

		 		  // 9 :upper left
		 		  ni= i+(meshNum-1)-((int)((i-1+meshNum-1)/meshNum))*meshNum;
		 		  nj= j+(meshNum-1)-((int)((j-1+meshNum-1)/meshNum))*meshNum;
		 		}

		 		if ((j-1)%2==0) {
		 		  // 1 :center
		 		  ni= i;
		 		  nj= j;
		 		  mesh_adjacent[i][j][0][0]=ni ;
		 		  mesh_adjacent[i][j][0][1]=nj ;

		 		  // 2 :upper
		 		  ni= i;
		 		  nj= j+(meshNum-1)-((int)((j-1+meshNum-1)/meshNum))*meshNum;
		 		  mesh_adjacent[i][j][2][0]=ni ;
		 		  mesh_adjacent[i][j][2][1]=nj ;

		 		  // 3 :right
		 		  ni= (i+1)-((int)(i/meshNum))*meshNum;
		 		  nj= j;
		 		  mesh_adjacent[i][j][3][0]=ni ;
		 		  mesh_adjacent[i][j][3][1]=nj ;

		 		  // 4 :lower
		 		  ni= i;
		 		  nj= (j+1)-((int)(j/meshNum))*meshNum;
		 		  mesh_adjacent[i][j][4][0]=ni ;
		 		  mesh_adjacent[i][j][4][1]=nj ;

		 		  // 5 :left
		 		  ni= i+(meshNum-1)-((int)((i-1+meshNum-1)/meshNum))*meshNum;
		 		  nj= j;
		 		  mesh_adjacent[i][j][6][0]=ni ;
		 		  mesh_adjacent[i][j][6][1]=nj ;

		 		  // 6 :upper right
		 		  ni= (i+1)-((int)(i/meshNum))*meshNum;
		 		  nj= j+(meshNum-1)-((int)((j-1+meshNum-1)/meshNum))*meshNum;

		 		  // 7 :lower right
		 		  ni= (i+1)-((int)(i/meshNum))*meshNum;
		 		  nj= (j+1)-((int)(j/meshNum))*meshNum;

		 		  // 8 :lower left
		 		  ni= i+(meshNum-1)-((int)((i-1+meshNum-1)/meshNum))*meshNum;
		 		  nj= (j+1)-((int)(j/meshNum))*meshNum;
		 		  mesh_adjacent[i][j][5][0]=ni ;
		 		  mesh_adjacent[i][j][5][1]=nj ;

		 		  // 9 :upper left
		 		  ni= i+(meshNum-1)-((int)((i-1+meshNum-1)/meshNum))*meshNum;
		 		  nj= j+(meshNum-1)-((int)((j-1+meshNum-1)/meshNum))*meshNum;
		 		  mesh_adjacent[i][j][1][0]=ni ;
		 		  mesh_adjacent[i][j][1][1]=nj ;
		 		}

		 	  }
		 	  }
		 	
		 	
		 	// Initialization of cell state 
		 	for (int i = 0; i <= meshNum; i++) {
		 	for (int j = 0; j <= meshNum; j++) {
			for (int k = 0; k <= state*2; k++) {
			for (int l = 0; l <= layer; l++) {

                part[l][i][j][k]     =0 ;
		 		part_next[l][i][k][0]=0 ;
			}
			}
			}
		 	}


			// ******************************************
            // Initial value setting (direct specification)  	  
			// ******************************************

 			    part[1][50][49][0]=1 ;  part[1][50][49][1]=1000 ;
 			 //   part[1][51][49][0]=1 ;  part[1][51][49][1]=1000 ;
				part[1][50][50][0]=1 ;  part[1][50][50][1]=1000 ;
				part[1][51][49][0]=1 ;  part[1][51][49][1]=1000 ;
				part[1][51][50][0]=1 ;  part[1][51][50][1]=1000 ;

/*				part[1][52][49][0]=1 ;  part[1][52][49][1]=1000 ;
				part[1][52][50][0]=1 ;  part[1][52][50][1]=1000 ;
				part[1][53][49][0]=1 ;  part[1][53][49][1]=1000 ;
				part[1][53][50][0]=1 ;  part[1][53][50][1]=1000 ;
*/

				
			// ******************************************
			//  Initial value setting (specified by random numbers)   
			// ******************************************	
/*	 		int l=0 ; int i=0; int j=0; int k=0; 
			for (l = 0; l <1000; l++) {
				java.util.Random r0 = new java.util.Random() ;
		        i = r0.nextInt(meshNum-1);
				j = r0.nextInt(meshNum-1);
				//k = r0.nextInt(6)+1;
				
				part[1][i][j][0]=1 ;
				part[1][i][j][1]=1000 ;
			}
*/
			// -------------------------------------------------------

			//  -------------------------------------------------------------------
			//  Set panel 
			setBackground(Color.white);     // Set background color 
		
		    //  Creating control buttons 
			setLayout(new FlowLayout(FlowLayout.LEFT));   //  Buttons are arranged sequentially from the left 
			calcButton = new JButton("Start");            //  Creating a button 
        	add(calcButton);                    	      //  Put a button on the panel 
			calcButton.addActionListener(this);           //  Button function enabled 
			calcButton.setActionCommand("caluculation");  //  Give the button a function name 

			stopButton = new JButton("Stop");             //  Creating a button
        	add(stopButton);                    	      //  Put a button on the panel 
			stopButton.addActionListener(this);           //  Button function enabled
			stopButton.setActionCommand("waitCaluculation");  //  Give the button a function name

			//@Set Timer
			pTimer =new Timer(100,this);
			pTimer.setActionCommand("calStart");  
	        //pTimer.start();

		}
		
	/****************** paintComponent ***********/
	public void paintComponent (Graphics g) {
		super.paintComponent(g); 	    

	    //	g.setColor(new Color(0,50,255));   // Red 0-255, green 0-255, blue 0-255 
		
		// Draw legend 
		g.setColor(Color.black);
		g.drawString("Number of State 1:@"+num_particle1, 900, 100);    // Number of token
		g.drawString("Iteration@ : "+(iterate), 900, 120);              // Iterate number
		
		g.drawString("Transition parameter w : "+(w), 900, 160);          // Morphogenesis parameter 
		g.drawString("Residual ratioe x      : "+(x), 900, 180);          // Residual rate of token 
		g.drawString("Elimination ratio  y   : "+(y), 900, 200);          // Removal rate of token
		g.drawString("Block amount z         : "+(z), 900, 220);          // Generation number of token

		// State 1
    	g.setColor(Color.black);
	    g.fillOval(900,260,10,10);
		g.setColor(Color.black);
		g.drawString("State 1 ", 920, 270);
		
		// State 2
    	g.setColor(Color.gray);
	    g.drawOval(900,280,10,10);
		g.setColor(Color.black);
		g.drawString("State 0 ", 920, 290);
		
		
		//  Draw cells 
		for (int i = 1; i <= meshNum; i++) {
		for (int j = 1; j <= meshNum; j++) {
//			g.setColor(Color.blue);
			g.setColor(Color.gray);
			g.drawOval((int)(mesh_x[i][j]-4),(int)(mesh_y[i][j]-4),8,8);
		}
		}

		// Draw tokens
		for (int l = 1; l <= layer; l++) {
		for (int i = 1; i <= meshNum; i++) {
		for (int j = 1; j <= meshNum; j++) {
		  if (part[l][i][j][0]==1) {
		  	 g.setColor(Color.black);
		     g.fillOval((int)(mesh_x[i][j]-5),(int)(mesh_y[i][j]-5),10,10);
    		 }

		}
		}
		}
	}
	
	/*************   Procedure when the button is pressed *******************/
	public void actionPerformed(ActionEvent e){
		
		//  Procedure when the start button is pressed 
		if(e.getActionCommand().equals("caluculation")){
		    pTimer.start();
	    }
		
		/***************************************************/
		//  Procedure when the stop button is pressed 
		if(e.getActionCommand().equals("waitCaluculation")){
		    pTimer.stop();
	    }
		
		/***************************************************/
		//  Action processing from timer 
		if(e.getActionCommand().equals("calStart")){
		
		int i,j ;	
        int p,s ;
        int t ;
        
        // Set of parameters
        x=0.05000;        //  Residual rate
        y=0.0080000;      //  Removal rate
        z=10000;          //  Generarion number of tokens
        w = 0.600 ;       //  Morphogenesis parameter

        // Count of iteration number
	    iterate = iterate +1 ;
	    
        // ***********************************
     	// Token distribution, transition 
     	// ************************************
        //  Setting token production 
	    for (i = 1; i <= meshNum; i++) {
		for (j = 1; j <= meshNum; j++) {

			if (part[1][i][j][0]==1) part[1][i][j][1]= z;    // If cell state = 1 then set of token number 
		}
	    }

	    // The process of moving tokens to an adjacent cell 
        for (s=1 ; s<=state*2-1; s++) {			
		for (i = 1; i <= meshNum; i++) {
		for (j = 1; j <= meshNum; j++) {

                   t= part[1][i][j][s] ;  
                   //  Distribution of tokens to adjacent cells 
                   for (p=0 ; p<=6 ; p++) { 
		    	     part[1][i][j][s] =	part[1][i][j][s] - (int) (t*(1.0-x)/7);
 				     part_next[1][mesh_adjacent[i][j][p][0]][mesh_adjacent[i][j][p][1]][s+1] =	part_next[1][mesh_adjacent[i][j][p][0]][mesh_adjacent[i][j][p][1]][s+1] + (int)(t*(1.0-x)/7);
	  		    	 } 
	  		         //  Tokens remaining in your cell 
    		    	 part[1][i][j][s] =	part[1][i][j][s] - (int)(t*x);
    		   	     part_next[1][i][j][s] =	part_next[1][i][j][s]+ (int)(t*x);
   	             
	               // If there is a remainder, the direction will be assigned by a random number. 
     	  	       t= part[1][i][j][s] ;  

    		   	   while (t>0) {  
  	  			   java.util.Random r0 = new java.util.Random() ;
  	  		       p = r0.nextInt(9-1) ;
  	  		       if (p<=6) { 
  		    	     part[1][i][j][s] =	part[1][i][j][s] - 1;
   				     part_next[1][mesh_adjacent[i][j][p][0]][mesh_adjacent[i][j][p][1]][s+1] =	part_next[1][mesh_adjacent[i][j][p][0]][mesh_adjacent[i][j][p][1]][s+1] + 1;
      	             } else {
      		    	 part[1][i][j][s] =	part[1][i][j][s] - 1;
      		   	     part_next[1][i][j][s] =	part_next[1][i][j][s]+ 1;
      	             }
  	  		       t=t-1 ;
              	  }

     	 }   //  j_loop
		 }   //  i_loop
    
		for (i = 1; i <= meshNum; i++) {
		for (j = 1; j <= meshNum; j++) {

                part[1][i][j][s]   =	part_next[1][i][j][s] ;
				part[1][i][j][s+1] =	part_next[1][i][j][s+1] ;

				part_next[1][i][j][s]   =	0 ;
				part_next[1][i][j][s+1] =	0 ;

				// Reduce the number of tokens to 0 by the removal rate 
				if (part[1][i][j][s]  <(z*y)) part[1][i][j][s]  =0;
	            if (part[1][i][j][s+1]<(z*y)) part[1][i][j][s+1]=0;

		}   //  j_loop
        }   //  i_loop
        }	//  s_loop
  
        // ************************************
		//  Applying state transition rules 
        // ************************************
		for (i = 1; i <= meshNum; i++) {
		for (j = 1; j <= meshNum; j++) {

			// Counting the number of tokens
		    range1=0 ; range2=0;
        
            for (s = 1; s <= state-1; s++) {
			   range1 = range1+ part[1][i][j][s];
		    }
            for (s = state; s <= state*2; s++) {
			   range2 = range2+ part[1][i][j][s];
		    }
               range2= range1 + range2 ;

               // Judgment of the next cell state 
               if(range1 > ((range2)*w) ) {part_next[1][i][j][0]=1 ;} ;
               if(range1 < ((range2)*w) ) {part_next[1][i][j][0]=0 ;} ;
               if(range1 == ((range2)*w) ) {
            	   if (part[1][i][j][0] == 0)  part_next[1][i][j][0]=0 ;
            	   if (part[1][i][j][0] == 1)  part_next[1][i][j][0]=1 ;
                   } ;
        	   
		 }   //  j_loop
		 }   //  i_loop


        // ************************************
     	// Replacing the following state values 
		// ************************************
		for (i = 1; i <= meshNum; i++) {
		for (j = 1; j <= meshNum; j++) {
			part[1][i][j][0] =part_next[1][i][j][0];
			part_next[1][i][j][0] =0 ;
			
		 }   //  j_loop
		 }   //  i_loop

		for (i = 1; i <= meshNum; i++) {
		for (j = 1; j <= meshNum; j++) {
			for (s = 1; s <= state*2; s++) {
				part[1][i][j][s] =0;
				part_next[1][i][j][s] =	0;
			 }
         }   //  j_loop
		 }   //  i_loop

        // ************************************
     	// Count of tokens
		// ************************************
		num_particle1 =0;
		for (i = 1; i <= meshNum; i++) {
		for (j = 1; j <= meshNum; j++) {
			if (part[1][i][j][0]==1)  num_particle1 = num_particle1 +1 ;
         }   //  j_loop
		 }   //  i_loop
		
		//  Panel redraw 
		repaint() ;

		}
	}
	

	}
}
