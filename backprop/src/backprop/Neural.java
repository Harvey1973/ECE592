package backprop;
import java.util.Arrays;


public class Neural {

	public static double[][] Matrix_trans(double [][] m){
		int row_num = m.length;
		int col_num = m[0].length;
		double[][] temp = new double[col_num][row_num];
	    for (int i = 0; i < m.length; i++)
	         for (int j = 0; j < m[0].length; j++)
	              temp[j][i] = m[i][j];
		return temp;
	}
	public static double[][] Matrix_sub(double [][]m,double [][]n){
		int row_num = m.length;
		int col_num = m[0].length;
		double [][]sub = new double[row_num][col_num];
		for(int i=0; i<row_num; i++) {
			
			for(int j=0; j<col_num; j++) {
				
				sub[i][j] = m[i][j] - n[i][j];
			}
		}
		return sub;
	}
	public static double [][] Matrix_dot(double [][]m, double [][]n){
		double [][] result = new double [m.length][n[0].length];
		for (int i = 0; i < m.length; i++) { 
		    for (int j = 0; j < n[0].length; j++) { 
		        for (int k = 0; k < m[0].length; k++) { 
		            result[i][j] += m[i][k] * n[k][j];
		        }
		    }
		}
		return result;
	}
	public static double [][] Matrix_mul(double [][] m, double [][] n){
		int row_num = m.length;
		int col_num = m[0].length;
		double [][]result = new double [row_num][col_num];
		for(int i=0; i<row_num; i++) {
			
			for(int j=0; j<col_num; j++) {
				
				result[i][j] = m[i][j] * n[i][j];
			}
		}
		return result;
	}
	public static double [][] initialize_weights(int row,int col){
		double [][]weights = new double[row][col];
		double min = -0.5;
		double max = 0.5;
		
		for(int i=0; i<row; i++) {
			
			for(int j=0; j<col; j++) {
				/*change this to random initialization*/
				weights[i][j] = (double)(Math.random() * (max - min) + min);
			}
		}
		return weights;
	}
	public static double [][] initialize_bias(int row,int col){
		double [][]weights = new double[row][col];
		
		for(int i=0; i<row; i++) {
			
			for(int j=0; j<col; j++) {
				/*change this to random initialization*/
				weights[i][j] = 0.0;
			}
		}
		return weights;
	}
	
	public static double[][] sigmoid(double [][]m){
		int row_num = m.length;
		int col_num = m[0].length;
		double [][] results = new double[row_num][col_num];
		for(int i=0; i<row_num; i++) {
			
			for(int j=0; j<col_num; j++) {
				/*perform sigmoid activation for every element in the matrix  1/(1+e^(-x))*/
				results[i][j] = 1/(1+Math.exp(-1*m[i][j]));
			}
		}
		return results;
	}
	public static double [][] Matrix_add(double [][]m,double [][] n){
		int row_num = m.length;
		int col_num = m[0].length;
		double [][] sum = new double[row_num][col_num];
		for(int i=0; i<row_num; i++) {
			
			for(int j=0; j<col_num; j++) {
				
				sum[i][j] = m[i][j] + n[i][j];
			}
		}
		return sum;
	}
	public static double [][] initialize_ones(double [][]m){
		int row_num = m.length;
		int col_num = m[0].length;
		double [][] ones = new double[row_num][col_num];
		for(int i=0; i<row_num; i++) {
			
			for(int j=0; j<col_num; j++) {
				
				ones[i][j] = 1.0;
			}
		}
		return ones;
		
	}
	public static double [][] Sigmoid_deriv(double [][]m){
		int row_num = m.length;
		int col_num = m[0].length;
		double [][] results = new double[row_num][col_num];
		double [][] ones = new double [row_num][col_num];
		ones = initialize_ones(m);
		results = Matrix_mul(m,Matrix_sub(ones,m)); 
		return results;
		
	}

	public static double [][] update(double lr, double[][]weights,double[][]d_weights){
		/* return the updated weights matrix*/
		int row_num = weights.length;
		int col_num = weights[0].length;
		double [][] results = new double[row_num][col_num];
		for(int i=0; i<row_num; i++) {
			
			for(int j=0; j<col_num; j++) {
				
				results[i][j] = weights[i][j]-lr*d_weights[i][j];

			}
		}
		return results;
	}
	public static double [][] lr_product(double lr,double[][]d_weights){
		/* return the updated weights matrix*/
		int row_num = d_weights.length;
		int col_num = d_weights[0].length;
		double [][] results = new double[row_num][col_num];
		for(int i=0; i<row_num; i++) {
			
			for(int j=0; j<col_num; j++) {
				
				results[i][j] =-lr*d_weights[i][j];

			}
		}
		return results;
	}
 
	public static double[][] create_v(double[][]m){
		int row_num = m.length;
		int col_num = m[0].length;
		double [][] results = new double[row_num][col_num];
		for(int i=0; i<row_num; i++) {
			
			for(int j=0; j<col_num; j++) {
				
				results[i][j] =0.0;

			}
		}
		return results;
	}
	public static double[][] create_mu(double[][]m,double mu){
		int row_num = m.length;
		int col_num = m[0].length;
		double [][] results = new double[row_num][col_num];
		for(int i=0; i<row_num; i++) {
			
			for(int j=0; j<col_num; j++) {
				
				results[i][j] = mu;

			}
		}
		return results;
	}
	
	public static void main(String[] args) {
		
		
		double [][][] X = {{{0},{0}},{{0},{1}},{{1},{0}},{{1},{1}}};
		double [][][] Y = {{{0}},{{1}},{{1}},{{0}}};
		double [][] weights_1 = initialize_weights(4,2);
		double [][] bias_1 = initialize_bias(4,1);
		double [][] weights_2 = initialize_weights(1,4);
		double [][] bias_2 = initialize_bias(1,1);
		/* momentum matrix*/
		double [][] v1 = create_v(weights_1);
		double [][] v2 = create_v(weights_2);
		double [][] v3 = create_v(bias_1);
		double [][] v4 = create_v(bias_2);
		double [][] mu1 = create_mu(weights_1,0.99);
		double [][] mu2 = create_mu(weights_2,0.99);
		double [][] mu3 = create_mu(bias_1,0.99);
		double [][] mu4 = create_mu(bias_2,0.99);
		/*----------------------------*/
		int count = 0;
		double cost = 0.0;
		
		/* ourter loop for ietrations */
		for (int j =0; j<7000;j++) {
			cost = 0.0;
			/* iterating through examples*/
			for (int i = 0; i <4;i++) {		
				/* Z1 = W1*X[0]+bias_1*/
				double [][] Z1 = Matrix_add(Matrix_dot(weights_1,X[i]),bias_1);
				double [][] A1 = sigmoid(Z1);		
				/* Z2 = W2*A1 + bias_2*/
				double [][] Z2 = Matrix_add(Matrix_dot(weights_2,A1),bias_2);
				double [][] A2 = sigmoid(Z2);
				cost += (Y[i][0][0]-A2[0][0])*(Y[i][0][0]-A2[0][0]);
				/*back prop*/
				/* dZ2 = (A2-Y[0])*(A2)*(1-A2)*/
				double [][]sigmoid_deriv_1 = Sigmoid_deriv(A2);
				double [][]dZ2 = Matrix_mul(Matrix_sub(A2,Y[i]),sigmoid_deriv_1);
				/*dW2 = np.dot(dZ2,A1.T)*/
				double [][]dW2 = Matrix_dot(dZ2,Matrix_trans(A1));
				weights_2 = update(0.2,weights_2,dW2);
				/* db2 = dZ2*/
				double [][]db2 = dZ2 ;
				bias_2 = update(0.2,bias_2,db2);
				/*dZ1 = np.dot(W2.T,dZ2)*(A1)*(1-A1)*/
				double [][]dZ1 = Matrix_mul(Matrix_dot(Matrix_trans(weights_2),dZ2),Sigmoid_deriv(A1));
				/*dW1 = np.dot(dZ1,X.T)*/
				double [][]dW1 = Matrix_dot(dZ1,Matrix_trans(X[i]));
				weights_1 = update(0.2,weights_1,dW1); 
				/*db1 = dZ1*/
				double [][]db1 = dZ1 ;
				bias_1 = update(0.2,bias_1,db1);
				/* momentum update*/
				/* v = mu*v - learning_rate*dW
				v1 = Matrix_add(Matrix_mul(mu1,v1),lr_product(0.05,dW1));
				v2 = Matrix_add(Matrix_mul(mu2,v2),lr_product(0.05,dW2));
				v3 = Matrix_add(Matrix_mul(mu3,v3),lr_product(0.05,db1));
				v4 = Matrix_add(Matrix_mul(mu4,v4),lr_product(0.05,db2));
				weights_1 = Matrix_add(weights_1,v1);
				weights_2 = Matrix_add(weights_2,v2);
				bias_1 = Matrix_add(bias_1,v3);
				bias_2 = Matrix_add(bias_2,v4);
				*/
				
				
				/*---------------------------*/
				
				
				/* update parameters*/
				
				
				
				
		}
		cost = cost*0.5;

		//if (j%1000 == 0) {
		count+=1;
		//System.out.println("cost 1000 iteration");
		System.out.println(count + " "+cost);
		//}
		if (cost <= 0.05) {
			System.out.println("Number of iterations needed to reach 0.05 error is" + count);
			break;
		}

		} /* this bracket closes the outer most loop*/
		/* predict */
		for (int i = 0; i <4;i++) {
			double [][] Z1 = Matrix_add(Matrix_dot(weights_1,X[i]),bias_1);
			
			double [][] A1 = sigmoid(Z1);
	
	
			/* Z2 = W2*A1 + bias_2*/
			double [][] Z2 = Matrix_add(Matrix_dot(weights_2,A1),bias_2);
			double [][] A2 = sigmoid(Z2);
			System.out.println(Arrays.deepToString(A2));
		}
		
	}
		

}


