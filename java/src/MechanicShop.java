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
					case 8: ListCarsBefore1995With50000Miles(esql); break;
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
	
	/* ADD CUSTOMER FUNCTION DESCRIPTION
	 *	Function: 	AddCustomer
	 *	Author: 	Dominic Renales
	 *	Input: 		MechanicShop esql
	 *	Output: 	void
	 *	Summary: 	Function that executes the pSQL query for adding an item to the Customer table.
	 *	Code Flow:
	 *			Line 1) Function is called and tries to perform execute function passing the string "INSERT INTO 
	 *			the Customer table with values (id, first name, last name, phone #, and address)" plus 
	 *			the input data obtained from the user from the GetCustomerInfo function.
	 *			Line 2) If the pSQL query fails, the user will be prompted with "Invalid input:" plus the pSQL
	 *			error message. Otherwise, the function skips this line signaling successful addition of new item
	 * 			into the Customer table
	*/
	public static void AddCustomer(MechanicShop esql) throws SQLException {//1
		/* PSQL CUSTOMER TABLE DATA INSERTION */
		try	{ esql.executeUpdate("INSERT INTO Customer (id, fname, lname, phone, address) VALUES (" + GetCustomerInfo(esql) + ");"); }
		catch (SQLException e) { System.out.println("Invalid Input: " + e.toString()); }
	}
	
	/* ADDMECHANIC FUNCTION DESCRIPTION
	 *	Function:	AddMechanic
	 *	Author:		Dominic Renales
	 *	Input: 		MechanicShop esql
	 * 	Output: 	void
	 *  	Summary:	Function that executes the pSQL query for adding an item to the Customer table.
	 *  	Code Flow: 	
	 * 			Line 1) Function is called and tries to perform execute function passing the string "INSERT INTO
	 * 			the Mechanic table with values (id, first name, last name, years of experience" plus the input
	 * 			data obtained from the user from the GetMechanicInfo function.
	 * 			Line 2) If the pSQL query fails the user will be prompted with "Invalid input:" plus the pSQL
	 * 			error message. Otherwise, the function skips this line signaling successful addition of new item
	 * 			into the Mechanic table.
	*/
	public static void AddMechanic(MechanicShop esql) throws SQLException {//2
		/* PSQL MECHANIC DATA INSERTION */
		try { esql.executeUpdate("INSERT INTO Mechanic (id, fname, lname, experience) VALUES (" + GetMechanicInfo(esql) + ");"); }
		catch(SQLException e) { System.out.println("Invalid Input: " + e.toString()); }
	}
	
	/* ADDCAR FUNCTION DESCRIPTION
	 *	Function: 	AddCar
	 *	Author: 	Dominic Renales
	 *	Input:		MechanicShop esql
	 * 	Output:		void
	 *  	Summary:	Function that executes the pSQL query for adding an item to the Car table.
	 *  	Code Flow: 	
	 * 			Line 1-7) Instantiation and declaration of necessary variables
	 * 			Line 8) Prompts user for owner's first name and assigns that to variable fname
	 * 			Line 9) Prompts user for owner's last name and assigns that to variable lname
	 * 			Line 10) Executes pSQL query to retireve a list of customers ID, first name, last name, and phone where the first and last name are equivalent to fname, lname respectively
	 * 			Line 11) If no customer exists, the user will be prompted to add the customer to the database before trying again
	 * 			Line 13) Else if there is only one customer, the car information will be put into the database
	 * 			Line 14) And then the "Owns" table will be updated to match the car with its respective user.
	 * 			Line 15-22) Else the user will be prompted to select which customer they were actually trying to select  
	 * 			Line 23) The appropriate customer id will be assigned to c_id
	 * 			Line 24) The car information will be put into the database
	 * 			Line 25) The "Owns" table will be updated to match the car with its respective user
	*/
	public static void AddCar(MechanicShop esql) throws SQLException {//3
		/* VARIABLES USED: SEARCH CUSTOMER */
		Scanner input = new Scanner(System.in);
		String fname = "",
				lname = "",
				 carInfo = GetCarInfo(),
				  vin = carInfo.substring(0,18);
		int choiceInput = 0, c_id = 0;
		List<List<String>> customerList = null;

		/* PSQL CAR DATA INSERTION */
		//try { esql.executeUpdate("INSERT INTO Car (vin, make, model, year) VALUES (" + GetCarInfo(esql) + ");"); }
		//catch(SQLException e) { System.out.println("Invalid Input: " + e.toString()); }

		System.out.print("Enter the owner's first name: "); fname = input.nextLine();
		System.out.print("Enter the owner's last name: ");  lname = input.nextLine();

		try{
			customerList = esql.executeQueryAndReturnResult("SELECT id, fname, lname, phone FROM Customer WHERE fname = \'" + fname + "\' AND lname = \'" + lname + "\';");
			if(customerList.size() == 0) { System.out.println("Customer does not exist. Add customer to the database before trying again."); return; }
			else if(customerList.size() == 1) {
				esql.executeUpdate("INSERT INTO Car (vin, make, model, year) VALUES (" + carInfo + ");");
				esql.executeUpdate("INSERT INTO Owns (ownership_id, customer_id, car_vin) VALUES (" + (GetHighestID(esql, "Owns", "ownership_id") + 1) + "," + customerList.get(0).get(0) + "," + vin + ");");
			}
			else {
				System.out.println("Select a customer:");
				for (int pos = 0; pos < customerList.size(); pos++){ 
					String phone = customerList.get(pos).get(3).replaceAll("\\s",""); //
					String ln = customerList.get(pos).get(2).replaceAll("\\s",""); //remove whitespace with regex
					String fn = customerList.get(pos).get(1).replaceAll("\\s",""); //remove whitespace with regex
					System.out.println(Integer.toString(pos) + ". " + ln + ", " + fn + ", " + phone);
				}
				//Wait for userinput to choose a customer
				do {
					choiceInput = readChoice();
					if ( choiceInput < customerList.size() & ( choiceInput > -1) ) break;
				} while (true);

				c_id = Integer.parseInt(customerList.get(choiceInput).get(0));

				esql.executeUpdate("INSERT INTO Car (vin, make, model, year) VALUES (" + GetCarInfo() + ");");
				esql.executeUpdate("INSERT INTO Owns (ownership_id, customer_id, car_vin) VALUES " + (GetHighestID(esql, "Owns", "ownership_id") + 1) + "," + Integer.toString(c_id) + "," + vin + ");");
			}
		}
		catch(SQLException e) { System.out.println("Error Processing: " + e.toString()); }
	}
	
	/* InsertServiceRequest FUNCTION DESCRIPTION
	 *	Function: 	InsertServiceRequest
	 *	Author: 	Krischin Layon
	 *	Input:		MechanicShop esql
	 * 	Output:		void
	 * 		Summary: Starts a service request 
	 * 		Code Flow:
	 * 			Phase 1) We collect the necessary information needed to initiate the service request.
	 * 					 This starts by determining the customer. If they are not in the database, the user is prompted to add them. The respective query wil then be ran.
	 * 					 We then determine the customer's car. If it is not in the database, the user is prompted to add it. The respective query will then be ran.
	 * 			Phase 2) We collect the information for the service request we would like to insert.
	 * 			Phase 3) Using the Customer's ID, the Car's VIN, and the Service Request information. Run the insertion query.
	 * 					 We include error handling for if the query fails. 
	*/
	public static void InsertServiceRequest(MechanicShop esql) throws SQLException{//4
		//---------Phase 1: Collect Variables---------

		//VARIABLES: Customer
		String lname = "";
		String catchTest;
		int ownership_id = -1;

		//VARIABLES: UI;
		Scanner input = new Scanner (System.in);
		int choiceInput; //used in number-choices;
		List<List<String>> customerList = null;
		List<List<String>> carList = null;
		int c_id = -1; String vin = null;

		//VARIABLES: Service Request
		int rid = -1;
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
					} while (c_id == -1);

					break;
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
									vin = esql.AddAndReturnCar(esql).get(0);
									//create a unique ownership id
									try{ownership_id = GetHighestID(esql,"Owns", "ownership_id") + 1;}
									catch(SQLException e){ System.out.println("Invalid Input: " + e.toString()); }
									System.out.println("New Ownership ID:" + Integer.toString(ownership_id));

									String q = Integer.toString(ownership_id) + ", " + Integer.toString(c_id) + ",\'" + vin +"\'";

									//run a new query adding the C_ID and VIN to the Owns table
									try { esql.executeUpdate("INSERT INTO Owns (ownership_id, customer_id, car_vin) VALUES (" + q + ");"); }
									catch(SQLException e) { System.out.println("Invalid Input: " + e.toString()); }
									
									break;
								case 2: return; //just exit
								default: continue;
							}
							
						} while (vin == null );
						break;
					case 1: //only one car showed up
						vin = carList.get(0).get(2);
						break;
					default: //multiple cars showed up

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

			//---------Phase 2: Collect Service Request Details---------

			//Prompt for Query Information
			try{rid = GetHighestID(esql,"Service_Request", "rid") + 1;}
			catch(SQLException e){ System.out.println("Invalid Input: " + e.toString()); }
			System.out.println("New Request ID:" + Integer.toString(rid));
			System.out.print("Enter Date (MM/DD/YYYY): ");  date = input.nextLine();
			System.out.print("Enter Odometer Reading: "); odometer = input.nextInt(); catchTest = input.nextLine();
			System.out.print("Enter Complaint: ");  complain = input.nextLine();
			
			
			//---------Phase 3: Run Service Request Query---------
			//Run Insertion Query for Service Request
			String q = "\'" + rid + "\',\'" + c_id + "\',\'"  + vin + "\',\'"+ date + "\',\'" + odometer + "\',\'" + complain + "\'"; 
			/* PSQL CAR DATA INSERTION */
			try { esql.executeUpdate("INSERT INTO Service_Request (rid, customer_id, car_vin, date, odometer, complain) VALUES (" + q + ");"); }
			catch(SQLException e) { System.out.println("Invalid Input: " + e.toString()); }
		}
		
	}
	/* CloseServiceRequest FUNCTION DESCRIPTION
	 *	Function: 	CloseServiceRequest
	 *	Author: 	Krischin Layon
	 *	Input:		MechanicShop esql
	 * 	Output:		void
	 * 		Summary: Collects and validates information about closing the service Request. Then adds it to the Closed_Request table
	 * 		Code Flow:
	 * 			Phase 1) Collects the RID & Mechanic ID. Runs a query to validate that they are real.
	 * 					 If the query only returns one record, then they are validated.
	 * 			Phase 2) Collect the closure date. Validates it by making sure it is further than the opening date.
	 * 					 Colelcts additional information about the service request. Makes sure that the bill is a positive integer.
	 * 			Phase 3) Given the RID, MID, date, and additional information, runs the insertion query.
	 * 					 We've included error handling if the query fails.
	*/
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
	//---------Phase 1: Collect and Validate RID & Mechanic ID---------
		
		//VARIABLES: existing request
		int rid; int mid; List<List<String>> record = null;

		//VARIABLES: closing request
		String date; int wid = -1;
		String comment; int bill;

		String catchTest;
		Scanner input = new Scanner(System.in);
		
		//Given an RID
		System.out.print("Enter Request ID #: ");  rid = input.nextInt(); catchTest = input.nextLine();
		//And a mechanic ID
		System.out.print("Enter Mechanic ID #: "); mid = input.nextInt(); catchTest = input.nextLine();
		
		//(check if both IDs are valid)
		/* 
		SELECT
			Service_Request.rid,
			Service_Request.date,
			Mechanic.id
		FROM
			Service_Request,
			Mechanic
		WHERE
			Service_Request.rid = " + Integer.toString(rid) + "
			AND
			Mechanic.id = " + Integer.toString(mid) + "
			AND
			Service_Request.rid NOT IN (
				SELECT
				closed_request.rid
				FROM
				Closed_Request
			);
		*/
		try {record = esql.executeQueryAndReturnResult("SELECT Service_Request.rid, Service_Request.date, Mechanic.id FROM Service_Request, Mechanic WHERE Service_Request.rid = " + Integer.toString(rid) + " AND Mechanic.id = " + Integer.toString(mid) + " AND Service_Request.rid NOT IN (SELECT closed_request.rid FROM Closed_Request);"); }
		catch(SQLException e){ System.out.println("Invalid Input: " + e.toString()); }
		System.out.println(record);
		
	//---------Phase 2: Validate Date and collect Closure Information---------
		
	if (record.size() == 1){ //only 1 record should show up, which contains a real rid and its date, additionally, a real mechanic id
			//create a closing record

			//prompt for date
			System.out.print("Enter Request Closing Date (YYYY-MM-DD): "); date = input.nextLine();
			//check if inputed date is after the request's opening date
			if ( Integer.parseInt(date.replaceAll("\\-","")) > Integer.parseInt(record.get(0).get(1).replaceAll("\\-","")) ){
				//get unique WID
				try{wid = GetHighestID(esql,"Closed_Request", "wid") + 1;}
				catch(SQLException e){ System.out.println("Invalid Input: " + e.toString()); }
				System.out.println("New Closed_Request ID:" + Integer.toString(wid));

				//prompt for comment
				System.out.print("Enter comment: "); comment = input.nextLine();
				//prompt for bill
				System.out.print("Enter bill: "); bill = input.nextInt(); catchTest = input.nextLine();
		

	//---------Phase 3: Run Query---------
				if (bill > 0){
					//run query
					String q = wid + "," + rid + ", " + mid + ", \'" + date + "\', \'" + comment + "\'," + bill;
					
					try{esql.executeUpdate("INSERT INTO Closed_Request (wid, rid, mid, date, comment, bill) VALUES (" + q + ");");}
					catch(SQLException e) {System.out.println("Invalid Input: " + e.toString()); }
				
				}
			}  else {
				System.out.println("This is an invalid date.");
			}

	} else {
		System.out.println("Invalid inputs. Either the Request ID or Mechanic ID are non-existant, or your Request ID has already been closed.");
	}

	}
	/* ListCustomersWithBillLessThan100 FUNCTION DESCRIPTION
	 *	Function: 	ListCustomersWithBillLessThan100
	 *	Author: 	Krischin Layon
	 *	Input:		MechanicShop esql
	 * 	Output:		void
	 * 		Summary: 
	 * 		Code Flow:
	 * 			-
	*/
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		try {
			//the prompt asks for both "date, comment & bill" as well as "customers". so i will be doing both
			//we need to remove the whitespace in the names when using this function
			//SQL:
			/*SELECT
				Closed_Request.date,
				Closed_Request.bill, 
				Closed_Request.comment, 
				Customer.fname, 
				Customer.lname 
			FROM Customer 
				FULL JOIN Service_Request ON Customer.id = Service_Request.customer_id
				FULL JOIN Closed_Request ON Closed_Request.rid = Service_Request.rid
			WHERE  Closed_Request.bill < 100;
			*/
			esql.executeQueryAndPrintResult("SELECT Closed_Request.date, Closed_Request.bill, Closed_Request.comment, Customer.fname, Customer.lname FROM Customer FULL JOIN Service_Request ON Customer.id = Service_Request.customer_id FULL JOIN Closed_Request ON Closed_Request.rid = Service_Request.rid WHERE  Closed_Request.bill < 100;");
		} catch (SQLException e){
			System.out.println("Error with Request: " + e.toString());
		}
		
	}
	
	/* LISTCUSTOMERSWITHMORETHAN20CARS FUNCTION DESCRIPTION
	 *	Function: 	ListCustomersWithMoreThan20Cars
	 *	Author: 	Dominic Renales
	 *	Input:		MechanicShop esql
	 * 	Output:		void
	 *  	Summary:	Function that executes the pSQL query to print all customers in the table with more than 20 cars.
	 *  	Code Flow: 	 
	 * 			Line 1) Function tries to execute and print the results of the following pSQL query:
	 * 			SELECT fname, lname
	 * 			FROM Customer C
	 * 			WHERE 20 < (SELECT COUNT(O.customer_id)
	 * 				    FROM OWNS O
	 * 				    WHERE C.id = O.customer_id).
	 * 			Line 2) If the pSQL query fails the user will be prompted with "Error with request" plus the pSQL
	 * 			error message. Otherwise, the function displays all customers that fit the relational query criteria.
	*/
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql)  throws SQLException {//7
		try { esql.executeQueryAndPrintResult("SELECT fname, lname FROM Customer C WHERE 20 < (SELECT COUNT(O.customer_id) FROM OWNS O WHERE C.id = O.customer_id);"); }
		catch(SQLException e) { System.out.println("Error With Request: " + e.toString()); }
	}
	
	/* LISTCARSBEFORE1995WITH50000Miles FUNCTION DEFINITION
	 * 	Function:	ListCarsBefore1995With50000Miles
	 * 	Author: 	Dominic Renales
	 * 	Input: 		MechanicShop esql
	 * 	Output: 	void
	 * 	Summary: 	Function that executes the pSQL query to print the make, model, and year (before 1995) of all cars with less than 50000 miles and 
	 * 	Code Flow:
	 * 			Line 1) Function tries to execute and print the results of the following pSQL query:
	 * 				SELECT C.make, C.model, C.year
	 * 				FROM Car C, Service_Request S
	 * 				WHERE C.vin = S.car_vin
	 * 				AND C.year < 1995
	 * 				AND S.odometer < 50000
	 * 			Line 2) If the pSQL query fails the user will be prompted with " Error with request" plus the pSQL 
	 * 			error message. Otherwise, the function displays make, model, year of all cars before 1995 with less than
	 * 			50000 miles on the odometer.
	 */
	public static void ListCarsBefore1995With50000Miles(MechanicShop esql) throws SQLException {//8
		try { esql.executeQueryAndPrintResult("SELECT C.make, C.model, C.year FROM Car C, Service_Request S WHERE C.vin = S.car_vin AND C.year < 1995 AND S.odometer < 50000;"); }
		catch(SQLException e) { System.out.println("Error With Request: " + e.toString()); }
	}
	/* ListKCarsWithTheMostServices FUNCTION DESCRIPTION
	 *	Function: 	ListKCarsWithTheMostServices
	 *	Author: 	Krischin Layon
	 *	Input:		MechanicShop esql
	 * 	Output:		void
	 * 		Summary: Prompts the User for the number of cars they want to print, then runs the respective query to print the cars with the most services
	 * 		Code Flow: 
	 * 			-Commented below is the SQL query ran.
	 * 			-Prompts the User for the number of cars.
	 * 			-Executes the SQL query
	 * 			-Includes error handling
	*/
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		
		/*
		SELECT
			Car.make,
			Car.model,
			Car.year,
			Service_Request.car_vin,
			COUNT(Service_Request.car_vin) as requests
		FROM Car, Service_Request 
		WHERE Car.vin = Service_Request.car_vin
		GROUP BY
			car.make,
			car.model,
			car.year,
			Service_Request.car_vin
		ORDER BY requests DESC
		LIMIT " + Integer.toString(k) + ";"
		*/
		
		int k; String catchTest;
		Scanner input = new Scanner (System.in);

		System.out.println("How many cars do you want to find?: "); k = input.nextInt();  catchTest = input.nextLine();
		if (k > 0){
			try {
				esql.executeQueryAndPrintResult("SELECT Car.make, Car.model, Car.year, Service_Request.car_vin, COUNT(Service_Request.car_vin) as requests FROM Car, Service_Request WHERE Car.vin = Service_Request.car_vin GROUP BY car.make, car.model, car.year, Service_Request.car_vin ORDER BY requests DESC LIMIT " + Integer.toString(k) + ";");
			} catch (SQLException e){
				System.out.println("Error with Request: " + e.toString());
			}
		}
		
	}

	/* ListCustomersInDescendingOrderOfTheirTotalBill FUNCTION DESCRIPTION
	 *	Function: 	ListCustomersInDescendingOrderOfTheirTotalBill
	 *	Author: 	Krischin Layon
	 *	Input:		MechanicShop esql
	 * 	Output:		void
	 * 		Summary: Runs the SQL query to display the customers in descending order of their total bill
	 * 		Code Flow:
	 * 			-Commented below is the SQL query that is to be run
	 * 			-We limit this list to 10 to prevent query spam.
	 * 			-Runs query
	 * 			-Includes error handling
	*/
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//10
		/*
			SELECT
				Customer.fname,
				Customer.lname,
				Customer.id as c_id,
				SUM(Closed_Request_bill) as total_bill
			FROM Customer,Service_Request, Closed_Request
			WHERE
				Customer.id = Service_Request.customer_id
				AND
				Closed_Request.rid = Service_Request.rid
			GROUP BY
				Customer.fname,
				Customer.lname,
				Customer.id
			ORDER BY total_bill DESC
			LIMIT 10;
		*/
		try {
			esql.executeQueryAndPrintResult("SELECT Customer.fname, Customer.lname, Customer.id as c_id, SUM(Closed_Request.bill) as total_bill FROM Customer,Service_Request, Closed_Request WHERE Customer.id = Service_Request.customer_id AND Closed_Request.rid = Service_Request.rid GROUP BY Customer.fname, Customer.lname, Customer.id ORDER BY total_bill DESC LIMIT 10;");
		} catch (SQLException e){
			System.out.println("Error with Request: " + e.toString());
		}
		
	}
	
	/*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
	/* Helper Functions Written by: Dominic Renales                                                                  */
	/*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
	
	/* GETCUSTOMERINFO FUNCTION DESCRIPTION
	 *	Function: 	GetCustomerInfo
	 *	Author: 	Dominic Renales, Krischin Layon
	 *	Input:		void
	 * 	Output:		String
	 *  	Summary:	Function that returns a string from the user input for Customer table
	 *  	Code Flow: 	
	 * 			Line 1-7) Necessary variables for the customer table are declared and initialized. 
	 * 			Line 8-10) Collects the highest used ID in the Customer table and assigns the "id" variable to 1 above it for a unique ID. 
	 * 			Line 11) Prompts user to input the customer's first name and assigns that input to the "fname" variable.
	 * 			Line 12) Prompts user to input the customer's last name and assigns that input to the "lname" variable.
	 * 			Line 13) Prompts the user to input the customer's phone number and assigns that input to the "phone" variable.
	 * 			Line 14) Prompts the user to input the customer's address and assigns that input to the "address" variable.
	 * 			Line 15) Returns the string in the following format: "(id),'(fname)','(lname)','(phone)','(address)'".
	*/
	public static String GetCustomerInfo(MechanicShop esql) {
		/* VARIABLES USED: CUSTOMER */
		int id = 0;
		String fname = "",
		        lname = "",
		         phone = "",
				  address = "",
				   catchTest = "";
		Scanner input = new Scanner(System.in);
		
		/* VARIABLE INITIALIZATION <:NOTES:> *catchTest variable used after nextInt to catch '\n' (hooray for brute force)* */
		try{id = GetHighestID(esql,"Customer", "id") + 1;}
		catch(SQLException e){ System.out.println("Invalid Input: " + e.toString()); }
		System.out.println("New Customer ID:" + Integer.toString(id));
		System.out.print("Enter Customer First Name: "); fname = input.nextLine();
		System.out.print("Enter Customer Last Name: ");	 lname = input.nextLine();
		System.out.print("Enter Customer Phone #: ");    phone = input.nextLine();
		System.out.print("Enter Customer Address: ");    address = input.nextLine();

		return Integer.toString(id) + ',' + '\'' + fname + '\'' + ',' + '\'' + lname + '\'' + ',' + '\'' + phone + '\'' + ',' + '\'' + address + "\'";
	}

	/* GETMECHANICINFO FUNCTION DESCRIPTION
	 *	Function: 	GetMechanicInfo
	 *	Author: 	Dominic Renales, Krischin Layon
	 *	Input:		MechanicShop esql
	 * 	Output:		void
	 *  	Summary:	Function that returns a string from the user input for Mechanic table
	 *  	Code Flow: 	 
	 * 			Line 1-6) Necessary info for the Mechanic table are declared and initialized.
	 * 			Line 7-9) Gets the highest used ID in the Mechanic table and assigns the "id" to 1+ that value for a unique value.
	 * 			Line 10) Prompts the user to input the Mechanic's first name and assigns that to the "fname" variable.
	 * 			Line 11) Prompts the user to input the Mechanic's last name and assigns that to the "lname" variable.
	 * 			Line 12) Prompts the user to input the Mechanic's years of experience and assigns that to the "years" variable.
	 * 			Line 13) Returns the string in the following format: "(id),'(fname)','(lname)',years".
	*/
	public static String GetMechanicInfo(MechanicShop esql) {
		/* VARIABLES USED: MECHANIC */
		int id = 0,
			 years = 0;
		String fname = "",
				lname = "",
				 catchTest = "";
		Scanner input = new Scanner(System.in);
		
		/* VARIABLE INITIALIZATION <:NOTES:> refer to catchTest notes in Customer comment */
		//System.out.print("Enter Mechanic ID: ");                  id = input.nextInt(); catchTest = input.nextLine();
		try{id = GetHighestID(esql,"Mechanic", "id") + 1;}
		catch(SQLException e){ System.out.println("Invalid Input: " + e.toString()); }
		System.out.println("New Mechanic ID:" + Integer.toString(id));

		System.out.print("Enter Mechanic First Name: ");          fname = input.nextLine();
		System.out.print("Enter Mechanic Last Name: ");           lname = input.nextLine();
		System.out.print("Enter Mechanic Years of Experience: "); years = input.nextInt();

		return Integer.toString(id) + ",\'" + fname + "\',\'" + lname + "\'," + years;
	}

	/* GETCARINFO FUNCTION DESCRIPTION
	 *	Function: 	GetCarInfo
	 *	Author: 	Dominic Renales, Krischin Layon
	 *	Input:		void
	 * 	Output:		String
	 *  	Summary:	Function that returns a string from the user input for Car table 
	 *  	Code Flow: 	 
	 * 			Line 1-6) Necessary info for Car table are declared and initialized.
	 * 			Line 7-11) Prompts the user to input a unique VIN for the car. If it is unique, Assigns that to the "vin" variable. 
	 * 			Line 12) Prompts the user to input the Car's make and assigns that to the "make" variable.
	 * 			Line 13) Prompts the user to input the Car's model and assigns that to the "model" variable.
	 * 			Line 14) Prompts the user to input the Car's year and assigns that to the "year" variable.
	 * 			Line 15) Returns a string in the following format: "'(vin)','(make)','(model)',(year)". 
	*/
	public static String GetCarInfo(MechanicShop esql) {
		/* VARIABLES USED: CAR */
		String vin = "",
				make = "",
				 model = "";
		int year = 0;
		Scanner input = new Scanner(System.in);
		boolean IsUniqueVIN = false;

		/* VARIABLE INITIALIZATION <:NOTES:> */
		do{System.out.print("Enter Car VIN#: ");  
		   vin = input.nextLine();
		   try{IsUniqueVIN = IsUniqueVIN(esql,vin);}
		   catch(SQLException e){System.out.println("Invalid Input: " + e.toString()); }
		} while(IsUniqueVIN == false);
		
		System.out.print("Enter Car Make: ");  make = input.nextLine();
		System.out.print("Enter Car Model: "); model = input.nextLine();
		System.out.print("Enter Car Year: ");  year = input.nextInt();

		return "\'" + vin + "\',\'" + make + "\',\'" + model + "\'," + Integer.toString(year);
	}
	
	/*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
	/* Helper Functions Written By: Krischin Layon                                                                   */
	/*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
	
	/* AddAndReturnCar FUNCTION DESCRIPTION
	 *	Function: 	AddAndReturnCar
	 *	Author: 	Krischin Layon
	 *	Input:		void
	 * 	Output:		List<String>
	 * 		Summary: Modification of AddCar() to return a List<String> of inputted information.
	 * 				 This is needed for when a Car is not in the database and we need to access the new entry's VIN
	 * 		Code Flow:
	 * 				A combination of AddCar() and GetCarInfo().
	 * 				There is a new List<String> called record which is appeneded to for every user input.
	 * 				This record is returned. Note that it is in the same format as the records returned when Selection Queries are ran.
	*/
	public static List<String> AddAndReturnCar(MechanicShop esql) throws SQLException {
		/* VARIABLES USED: CAR */
		String vin = "",
				make = "",
				 model = "";
		int year = 0;
		Scanner input = new Scanner(System.in);
		List<String> record = new ArrayList<String>();
		boolean IsUniqueVIN = false;

		/* VARIABLE INITIALIZATION <:NOTES:> */
		do{System.out.print("Enter Car VIN#: ");  
		   vin = input.nextLine();
		   try{IsUniqueVIN = IsUniqueVIN(esql,vin);}
		   catch(SQLException e){System.out.println("Invalid Input: " + e.toString()); }
		} while(IsUniqueVIN == false);
		record.add(vin);

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

	/* AddAndReturnCustomer FUNCTION DESCRIPTION
	 *	Function: 	AddAndReturnCustomer
	 *	Author: 	Krischin Layon
	 *	Input:		void
	 * 	Output:		List<String>
	 * 		Summary: Modification of AddCustomer() to return a List<String> of inputted information.
	 * 				 This is needed for when a Customer is not in the database and we need to access the new entry's C_ID
	 * 		Code Flow:
	 * 				A combination of AddCustomer() and GetCustomerInfo().
	 * 				There is a new List<String> called record which is appeneded to for every user input.
	 * 				This record is returned. Note that it is in the same format as the records returned when Selection Queries are ran.
	*/
	public static List<String> AddAndReturnCustomer(MechanicShop esql) throws SQLException {//1
		/* VARIABLES USED: CUSTOMER */
		int id = 0;
		String fname = "",
		        lname = "",
		         phone = "",
				  address = "",
				   catchTest = "";
		Scanner input = new Scanner(System.in);
		List<String> record = new ArrayList<String>();
		
		/* VARIABLE INITIALIZATION <:NOTES:> *catchTest variable used after nextInt to catch '\n' (hooray for brute force)* */
		try{id = GetHighestID(esql,"Customer", "id") + 1;}
		catch(SQLException e){ System.out.println("Invalid Input: " + e.toString()); }
		System.out.println("New Customer ID:" + Integer.toString(id));
		record.add(Integer.toString(id));
		System.out.print("Enter Customer First Name: "); fname = input.nextLine(); record.add(fname);
		System.out.print("Enter Customer Last Name: ");	 lname = input.nextLine(); record.add(lname);
		System.out.print("Enter Customer Phone #: ");    phone = input.nextLine(); record.add(phone);
		System.out.print("Enter Customer Address: ");    address = input.nextLine(); record.add(address);

		String q = Integer.toString(id) + ',' + '\'' + fname + '\'' + ',' + '\'' + lname + '\'' + ',' + '\'' + phone + '\'' + ',' + '\'' + address + "\'";
		
		/* PSQL CUSTOMER TABLE DATA INSERTION */
		try	{ esql.executeUpdate("INSERT INTO Customer (id, fname, lname, phone, address) VALUES (" + q + ");"); }
		catch (SQLException e) { System.out.println("Invalid Input: " + e.toString()); }

		return record;
	}

	/* GetHighestID FUNCTION DESCRIPTION
	 *	Function:   GetHighestID
	 *	Author: 	Krischin Layon
	 *	Input:		String tableName - the name of the table that you would like to colelct from
	 				String columnName - the name of the ID that you want to collect from
	 * 	Output:		int
	 * 		Summary: Runs a query on the specified table and column to collect the highest ID
	 * 				 This is useful for finding new unique values for C_ID, ownership_id, RID, MID, and WID, which are all incremental.
	*/
	public static int GetHighestID(MechanicShop esql, String tableName, String columnName) throws SQLException {
		int id = -1;
		/*
			SELECT MAX(" + columnName + ")
			FROM " + tableName + ";

		*/
		try{id = Integer.parseInt( esql.executeQueryAndReturnResult("SELECT MAX(" + columnName + ") FROM " + tableName + ";").get(0).get(0)) ; }
		catch(SQLException e){ System.out.println("Invalid Input: " + e.toString()); }

		if (id == -1){
			System.out.println("Query didn't return an integer.");
		}

		return id;
	}

	/* IsUniqueVIN FUNCTION DESCRIPTION
	 *	Function:   GetHighestID
	 *	Author: 	Krischin Layon
	 *	Input:		String VIN - VIN to test.
	 * 	Output:		bool
	 * 		Summary: Since VIN is a 16-char string and VINs are not incremental, we have to test to see if the VIN is in the database
	 * 				 We do this by running a selection query with the provided VIN 
	 * 				 Returns False if a record is returned.
	 * 				 Returns True if a record isn't returned.
	*/
	public static boolean IsUniqueVIN(MechanicShop esql, String VIN) throws SQLException {
		/*
			SELECT * FROM Car WHERE vin = \'" + VIN + "\';
		*/

		List<List<String>> record = null;
		try{record = esql.executeQueryAndReturnResult("SELECT * FROM Car WHERE vin=\'" + VIN + "\';"); }
		catch(SQLException e){ System.out.println("Invalid Input: " + e.toString()); }

		if (record.size() > 0){
			System.out.println("\'" + VIN + "\' is not a unique VIN");
			return false;
		}
		else {return true;}
	}
}
