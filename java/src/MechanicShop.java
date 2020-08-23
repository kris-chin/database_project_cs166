/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	public static void AddCustomer(MechanicShop esql) throws SQLException {//1
		/* VARIABLES USED: CUSTOMER */
		int id = 0;
		String fname = "",
		        lname = "",
		         phone = "",
				  address = "",
				   catchTest = "";
		Scanner input = new Scanner(System.in);
		
		/* VARIABLE INITIALIZATION <:NOTES:> *catchTest variable used after nextInt to catch '\n' (hooray for brute force)* */
		System.out.print("Enter Customer ID: ");         id = input.nextInt(); catchTest = input.nextLine();
		System.out.print("Enter Customer First Name: "); fname = input.nextLine();
		System.out.print("Enter Customer Last Name: ");	 lname = input.nextLine();
		System.out.print("Enter Customer Phone #: ");    phone = input.nextLine();
		System.out.print("Enter Customer Address: ");    address = input.nextLine();
		
		/* PSQL QUERY STRING */
		String q = Integer.toString(id) + ',' + '\'' + fname + '\'' + ',' + '\'' + lname + '\'' + ',' + '\'' + phone + '\'' + ',' + '\'' + address + '\'';
		
		/* PSQL CUSTOMER TABLE DATA INSERTION */
		try	{ esql.executeUpdate("INSERT INTO Customer (id, fname, lname, phone, address) VALUES (" + q + ");"); }
		catch (SQLException e) { System.out.println("Invalid Input: " + e.toString()); }
	}
	
	/* ADD CUSTOMER FUNCTION DESCRIPTION
	 *	Function: 	AddCustomer
	 *	Author: 	Dominic Renales
	 *	Input: 		MechanicShop esql
	 *	Output: 	void
	 *	Summary: 	Function that executes the pSQL query for adding an item to the Customer table.
	 *	Code Flow:
	 *		Line 1) Function is called and tries to perform execute function passing the string "INSERT INTO 
	 *		the Customer table with values (id, first name, last name, phone #, and address)" plus 
	 *		the input data obtained from the user from the GetCustomerInfo function.
	 *		Line 2) If the pSQL query fails, the user will be prompted with "Invalid input:" plus the pSQL
	 *		error message. Otherwise, the function skips this line signaling successful addition of new item
	 * 		into the Customer table
	*/
	public static void AddCustomer(MechanicShop esql) throws SQLException {//1
		/* PSQL CUSTOMER TABLE DATA INSERTION */
		try	{ esql.executeUpdate("INSERT INTO Customer (id, fname, lname, phone, address) VALUES (" + GetCustomerInfo() + ");"); }
		catch (SQLException e) { System.out.println("Invalid Input: " + e.toString()); }
	}
	
	/* ADDMECHANIC FUNCTION DESCRIPTION
	 *	Function:	AddMechanic
	 *	Author:		Dominic Renales
	 *	Input: 		MechanicShop esql
	 * 	Output: 	void
	 *  Summary:	Function that executes the pSQL query for adding an item to the Customer table.
	 *  Code Flow: 	
	 * 		Line 1) Function is called and tries to perform execute function passing the string "INSERT INTO
	 * 		the Mechanic table with values (id, first name, last name, years of experience" plus the input
	 * 		data obtained from the user from the GetMechanicInfo function.
	 * 		Line 2) If the pSQL query fails the user will be prompted with "Invalid input:" plus the pSQL
	 * 		error message. Otherwise, the function skips this line signaling successful addition of new item
	 * 		into the Mechanic table.
	*/
	public static void AddMechanic(MechanicShop esql) throws SQLException {//2
		/* PSQL MECHANIC DATA INSERTION */
		try { esql.executeUpdate("INSERT INTO Mechanic (id, fname, lname, experience) VALUES (" + GetMechanicInfo() + ");"); }
		catch(SQLException e) { System.out.println("Invalid Input: " + e.toString()); }
	}
	
	/* ADDCAR FUNCTION DESCRIPTION
	 *	Function: 	AddCar
	 *	Author: 	Dominic Renales
	 *	Input:		MechanicShop esql
	 * 	Output:		void
	 *  Summary:	Function that executes the pSQL query for adding an item to the Car table.
	 *  Code Flow: 	
	 * 		Line 1) Function is called and tries to perform execute function passing the string "INSERT INTO
	 * 		the Car table with values (VIN #, make, model, and year)" plus the input data obtained from the
	 * 		user from the GetCarInfo function. 
	 * 		Line 2) If the pSQL query fails the user will be prompted with "Invalid input:" plus the pSQL
	 * 		error message. Otherwise, the function skips this line signaling successful addition of the new 
	 * 		item into the Car table.
	*/
	public static void AddCar(MechanicShop esql) throws SQLException {//3
		/* PSQL CAR DATA INSERTION */
		try { esql.executeUpdate("INSERT INTO Car (vin, make, model, year) VALUES (" + GetCarInfo() + ");"); }
		catch(SQLException e) { System.out.println("Invalid Input: " + e.toString()); }
	}

	public static List<String> AddAndReturnCar(MechanicShop esql) throws SQLException {
		/* VARIABLES USED: CAR */
		String vin = "",
				make = "",
				 model = "";
		int year = 0;
		Scanner input = new Scanner(System.in);
		List<String> record = new ArrayList<String>();

		/* VARIABLE INITIALIZATION <:NOTES:> */
		System.out.print("Enter Car VIN#: ");  vin = input.nextLine(); record.add(vin);
		System.out.print("Enter Car Make: ");  make = input.nextLine(); record.add(make);
		System.out.print("Enter Car Model: "); model = input.nextLine(); record.add(model);
		System.out.print("Enter Car Year: ");  year = input.nextInt(); record.add(Integer.toString(year));

		/* PSQL QUERY STRING */
		String q = "\'" + vin + "\',\'" + make + "\',\'" + model + "\'," + Integer.toString(year); 

		/* PSQL CAR DATA INSERTION */
		try { esql.executeUpdate("INSERT INTO Car (vin, make, model, year) VALUES (" + q + ");"); }
		catch(SQLException e) { System.out.println("Invalid Input: " + e.toString()); }
		
		return record;
	}
	
	public static void InsertServiceRequest(MechanicShop esql) throws SQLException{//4
		//VARIABLES: Customer
		String lname = "";
		String catchTest;

		//VARIABLES: UI;
		Scanner input = new Scanner (System.in);
		int choiceInput; //used in number-choices;
		List<List<String>> customerList = null;
		List<List<String>> carList = null;
		int c_id; String vin = null;

		//VARIABLES: Service Request
		int rid;
		String date = null;
		int odometer; //must be positive
		String complain = null;
 
		//Initialize Variables
		System.out.print("Enter Customer Last Name: "); lname = input.nextLine();

		//Run Customer Query
		try { customerList = esql.executeQueryAndReturnResult("SELECT * FROM Customer WHERE lname = \'" + lname + "\';"); }
		catch(SQLException e) { System.out.println("Invalid Input: " + e.toString()); }

		//Acquire Customer Data
		if (customerList != null){ //if the query returned a List<List<String>>
			switch ( customerList.size() ){

				case 0: //no customer shows up. proceed to add a customer
					System.out.print("There is no customer with the last name of \'" + lname + "\'. \nWould you like to initiate Add Customer procedure? <1 - Yes/ 2 - No>\n");
					
					do {
						choiceInput = readChoice();
						switch (choiceInput){
							case 1:
								c_id = Integer.parseInt(esql.AddAndReturnCustomer(esql).get(0));
								break;
							case 2: return; //just exit
							default: continue;
						}
					} while (true);
					
				case 1: //only one customer shows up. 
					c_id = Integer.parseInt(customerList.get(0).get(0));
					break;
				default: //more than one customer shows up. prompt for which customer;
					System.out.println("Select a customer:");
					for (int i = 0; i < customerList.size(); i ++){ //print customers
						String ln = customerList.get(i).get(2).replaceAll("\\s",""); //remove whitespace with regex
						String fn = customerList.get(i).get(1).replaceAll("\\s",""); //remove whitespace with regex
						System.out.println(Integer.toString(i) + ". " + ln + ", " + fn);
					}
					//Wait for userinput to choose a customer
					do {choiceInput = readChoice();
						if ( choiceInput < customerList.size() & ( choiceInput > -1) ){break;}
					} while (true);
					c_id = Integer.parseInt(customerList.get(choiceInput).get(0));
					
					break;
			}

			//Run Car Query.
			try { carList = esql.executeQueryAndReturnResult("SELECT * FROM Owns WHERE customer_id = \'" + c_id + "\';"); }
			catch(SQLException e) { System.out.println("Invalid Input: " + e.toString()); }

			//Acquire Car Data (NOTE: this is very similar to the procedure for getting customer data)
			if (carList != null){ //if the query returned a List<List<String>>
				switch ( carList.size() ) {
					case 0: //no cars showed up
						System.out.print("There is no cars associated with this customer.\n Would you like to initiate Add Car procedure? <1 - Yes/ 2 - No>\n");
						do {
							choiceInput = readChoice();
							switch (choiceInput){
								case 1:
									vin = esql.AddAndReturnCar(esql).get(2);
									break;
								case 2: return; //just exit
								default: continue;
							}
						} while (true);
					case 1: //only one car showed up
						vin = carList.get(0).get(2);
						break;
					default: //multiple cars showed up

						//we might need to add a display for the make, model, and year of each car

						System.out.println("Select a car VIN:");
						for (int i = 0; i < carList.size(); i ++){ //print cars
							String car_vin = carList.get(i).get(2).replaceAll("\\s",""); //remove whitespace with regex
							
							System.out.println(Integer.toString(i) + ". VIN: " + car_vin);
						}
						//Wait for userinput to choose a car
						do {choiceInput = readChoice();
							if ( choiceInput < carList.size() & ( choiceInput > -1) ){break;}
						} while (true);
						vin = carList.get(choiceInput).get(2);
					
						break;
				}
			}

			//Prompt for Query Information
			System.out.print("Enter Request ID #: ");  rid = input.nextInt(); catchTest = input.nextLine();
			System.out.print("Enter Date (MM/DD/YYYY): ");  date = input.nextLine();
			System.out.print("Enter Odometer Reading: "); odometer = input.nextInt(); catchTest = input.nextLine();
			System.out.print("Enter Complaint: ");  complain = input.nextLine();
			//Run Insertion Query for Service Request
			String q = "\'" + rid + "\',\'" + c_id + "\',\'"  + vin + "\',\'"+ date + "\',\'" + odometer + "\',\'" + complain + "\'"; 
			/* PSQL CAR DATA INSERTION */
			try { esql.executeUpdate("INSERT INTO Service_Request (rid, customer_id, car_vin, date, odometer, complain) VALUES (" + q + ");"); }
			catch(SQLException e) { System.out.println("Invalid Input: " + e.toString()); }
		}
		
	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		
	}
	
	/* LISTCUSTOMERSWITHMORETHAN20CARS FUNCTION DESCRIPTION
	 *	Function: 	ListCustomersWithMoreThan20Cars
	 *	Author: 	Dominic Renales
	 *	Input:		MechanicShop esql
	 * 	Output:		void
	 *  Summary:	Function that executes the pSQL query to print all customers in the table with more than 20 cars.
	 *  Code Flow: 	 
	 * 		Line 1) Function tries to execute and print the results of the following pSQL query:
	 * 			SELECT fname, lname
	 * 			FROM Customer C
	 * 			WHERE 20 < (SELECT COUNT(O.customer_id)
	 * 						FROM OWNS O
	 * 						WHERE C.id = O.customer_id).
	 * 		Line 2) If the pSQL query fails the user will be prompted with "Error with request" plus the pSQL
	 * 		error message. Otherwise, the function displays all customers that fit the relational query criteria.
	*/
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql)  throws SQLException {//7
		try { esql.executeQueryAndPrintResult("SELECT fname, lname FROM Customer C WHERE 20 < (SELECT COUNT(O.customer_id) FROM OWNS O WHERE C.id = O.customer_id);"); }
		catch(SQLException e) { System.out.println("Error With Request: " + e.toString()); }
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		//
		
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//9
		//
		
	}
	
	/* AUXILLARY FUNCTIONS FOR I/O */

	/* GETCUSTOMERINFO FUNCTION DESCRIPTION
	 *	Function: 	GetCustomerInfo
	 *	Author: 	Dominic Renales
	 *	Input:		void
	 * 	Output:		String
	 *  Summary:	Function that returns a string from the user input for Customer table
	 *  Code Flow: 	
	 * 		Line 1-7) Necessary variables for the customer table are declared and initialized. 
	 * 		Line 8) Prompts user to input the customer's ID and assigns that input to the "id" variable. 
	 * 		Line 9) Prompts user to input the customer's first name and assigns that input to the "fname" variable.
	 * 		Line 10) Prompts user to input the customer's last name and assigns that input to the "lname" variable.
	 * 		Line 11) Prompts the user to input the customer's phone number and assigns that input to the "phone" variable.
	 * 		Line 12) Prompts the user to input the customer's address and assigns that input to the "address" variable.
	 * 		Line 13) Returns the string in the following format: "(id),'(fname)','(lname)','(phone)','(address)'".
	*/
	public static String GetCustomerInfo() {
		/* VARIABLES USED: CUSTOMER */
		int id = 0;
		String fname = "",
		        lname = "",
		         phone = "",
				  address = "",
				   catchTest = "";
		Scanner input = new Scanner(System.in);
		
		/* VARIABLE INITIALIZATION <:NOTES:> *catchTest variable used after nextInt to catch '\n' (hooray for brute force)* */
		System.out.print("Enter Customer ID: ");         id = input.nextInt(); catchTest = input.nextLine();
		System.out.print("Enter Customer First Name: "); fname = input.nextLine();
		System.out.print("Enter Customer Last Name: ");	 lname = input.nextLine();
		System.out.print("Enter Customer Phone #: ");    phone = input.nextLine();
		System.out.print("Enter Customer Address: ");    address = input.nextLine();

		return Integer.toString(id) + ',' + '\'' + fname + '\'' + ',' + '\'' + lname + '\'' + ',' + '\'' + phone + '\'' + ',' + '\'' + address + "\'";
	}

	/* GETMECHANICINFO FUNCTION DESCRIPTION
	 *	Function: 	GetMechanicInfo
	 *	Author: 	Dominic Renales
	 *	Input:		MechanicShop esql
	 * 	Output:		void
	 *  Summary:	Function that returns a string from the user input for Mechanic table
	 *  Code Flow: 	 
	 * 		Line 1-6) Necessary info for the Mechanic table are declared and initialized.
	 * 		Line 7) Prompts the user to input the Mechanic's ID and assigns that to the "id" variable.
	 * 		Line 8) Prompts the user to input the Mechanic's first name and assigns that to the "fname" variable.
	 * 		Line 9) Prompts the user to input the Mechanic's last name and assigns that to the "lname" variable.
	 * 		Line 10) Prompts the user to input the Mechanic's years of experience and assigns that to the "years" variable.
	 * 		Line 11) Returns the string in the following format: "(id),'(fname)','(lname)',years".
	*/
	public static String GetMechanicInfo() {
		/* VARIABLES USED: MECHANIC */
		int id = 0,
			 years = 0;
		String fname = "",
				lname = "",
				 catchTest = "";
		Scanner input = new Scanner(System.in);
		
		/* VARIABLE INITIALIZATION <:NOTES:> refer to catchTest notes in Customer comment */
		System.out.print("Enter Mechanic ID: ");                  id = input.nextInt(); catchTest = input.nextLine();
		System.out.print("Enter Mechanic First Name: ");          fname = input.nextLine();
		System.out.print("Enter Mechanic Last Name: ");           lname = input.nextLine();
		System.out.print("Enter Mechanic Years of Experience: "); years = input.nextInt();

		return Integer.toString(id) + ",\'" + fname + "\',\'" + lname + "\'," + years;
	}

	/* GETCARINFO FUNCTION DESCRIPTION
	 *	Function: 	GetCarInfo
	 *	Author: 	Dominic Renales
	 *	Input:		void
	 * 	Output:		String
	 *  Summary:	Function that returns a string from the user input for Car table 
	 *  Code Flow: 	 
	 * 		Line 1-5) Necessary info for Car table are declared and initialized.
	 * 		Line 6) Prompts user to input the Car's VIN number and assigns that to the "vin" variable.
	 * 		Line 7) Prompts the user to input the Car's make and assigns that to the "make" variable.
	 * 		Line 8) Prompts the user to input the Car's model and assigns that to the "model" variable.
	 * 		Line 9) Prompts the user to input the Car's year and assigns that to the "year" variable.
	 * 		Line 10) Returns a string in the following format: "'(vin)','(make)','(model)',(year)". 
	*/
	public static String GetCarInfo() {
		/* VARIABLES USED: CAR */
		String vin = "",
				make = "",
				 model = "";
		int year = 0;
		Scanner input = new Scanner(System.in);

		/* VARIABLE INITIALIZATION <:NOTES:> */
		System.out.print("Enter Car VIN#: ");  vin = input.nextLine();
		System.out.print("Enter Car Make: ");  make = input.nextLine();
		System.out.print("Enter Car Model: "); model = input.nextLine();
		System.out.print("Enter Car Year: ");  year = input.nextInt();

		return "\'" + vin + "\',\'" + make + "\',\'" + model + "\'," + Integer.toString(year);
	}
}
