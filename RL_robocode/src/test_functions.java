import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class test_functions {
	
	static int row_num = 8*6*10*4;
	static int col_num = 6;
	static int count = 0;    // flag for initialization
	static String state = null;
	static double [][] Q_table_double = new double[row_num][col_num];
	static String current_state = "1111";
	static double[] current_q = new double[6];
	
	public static void initialize_Q_table(String[][]Q_table) {
		System.out.println("Initializing Q Table");
		// An example entry of Q_table will be like 
		//x    y   dist  Bearing    action_1 	action_2	action_3	action_4	action_5  
		//0    0   0       0  			0.0				0.0			0.0			0.0			0.0
		int index_count = 0;
		for (int x_1=0;x_1<8;x_1++) {
			for (int y_1 =0 ; y_1<6;y_1++) {
				for (int d=0 ; d<10;d++) {
					for (int B =0 ; B<4;B++) {
							Q_table[index_count][0] = x_1 + ""+y_1 + ""+ d +""+B;		
								for (int a = 1; a<6; a++) {		
										Q_table[index_count][a] = "0";
										
									}
									index_count +=1;
								}
							}
						}
					}
				}
	public static void save_2 (String file, String[][] Q_table) throws IOException{
	    try {
		BufferedWriter outputWriter = null;
	    outputWriter = new BufferedWriter(new FileWriter(file));
	    for (int i=0;i<row_num;i++) {
	    	
		    outputWriter.write(String.valueOf(Q_table[i][0]+"    "+Q_table[i][1]+"    "+Q_table[i][2]+"    "+Q_table[i][3]+"    "+Q_table[i][4]+"    "+Q_table[i][5]));
		    outputWriter.newLine();
	    	
	    }

	    outputWriter.flush();  
	    outputWriter.close(); 
	}
	    catch(Exception e) {
	    	
	    }
}
	public static double[] choose_action(String state,String[][] Q_table){
		int index = 0;
		for (int i=0;i<Q_table.length;i++) {

			if(Q_table[i][0].equals(state)) {
				index = i;
				System.out.println("index is"+index);
			}
		}
		return Q_table_double[index];
	}
	public static int argmax(double[] array) {
		int index = 0;
		double largest = Double.MIN_VALUE;
		for ( int i = 0; i < array.length; i++ )
		{
		    if ( array[i] > largest )
		    {
		        largest = array[i];
		        index = i;
		    }
		}
		return index ;
		
	}
	public static double max(double[] array) {
		double largest = Double.MIN_VALUE;
		// starts with index 1 because the first element is the state number 
		for ( int i = 1; i < array.length; i++ )
		{
		    if ( array[i] > largest )
		    {
		        largest = array[i];
		        
		    }
		}
		return largest;
	}
	public static int state_index(String state,String[][] Q_table){
		int index = 0;
		for (int i=0;i<Q_table.length;i++) {

			if(Q_table[i][0].equals(state)) {
				index = i;
				
			}
		}
		return index;
	}
	public static void main(String[] args) {
		double test_array[] = {1.0,2.0,3.0};
		int max_index = 0;
		double max_value = 0;
		int state_index_1 = 0;
		String [][] Q_table = new String [row_num][col_num]; 
		initialize_Q_table(Q_table);
		System.out.println(Q_table[0][0]);
		for (int i=0;i<Q_table.length;i++) {
			for (int j=0; j<Q_table[0].length;j++) {
				
				Q_table_double[i][j] = Double.parseDouble(Q_table[i][j]);
			}
		}
		System.out.println(Arrays.toString(Q_table_double[100]));
		current_state = "1111";
		current_q = choose_action(current_state, Q_table);
		System.out.println(Arrays.toString(current_q));
		max_index = argmax(test_array);
		System.out.println(max_index);
		max_value = max(test_array);
		System.out.println(max_value);
		state_index_1 = state_index(current_state,Q_table); 
		System.out.println(state_index_1);
	}
	
}
