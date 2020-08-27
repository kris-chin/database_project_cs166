# CS166 Project: Phase 3
Authors: Krischin Layon and Dominic Renales

Project Goal: To build a database for mechanics shop to track information regarding customers, mechanics, cars, car ownership, service requests, and billing information.

Available Functionality: 
1) Adding customers
2) Adding mechanics
3) Adding cars
4) Initiating service requests
5) Closing service requests
6) Finding data on customers with a bill less than $100
7) Finding data on customers who own more than 20 cars
8) Finding data on cars with a production year before 1995 and with less than 50000 miles
9) Finding data on 'k' number of cars with the highest number of service requests
10) Finding data on customers with the highest service bills in descending order 

For more details and information regarding the project refer to [this link](https://docs.google.com/document/d/1YKZq0HFVzzT7fzb6NdEFd0Ck7K0Z-C1VBL5tGzuRK0Y/edit?usp=sharing).

# Project Running Instructions
- Start Project:
1) Clone project file / repository
2) cd database_project_cs166/postgresql/
3) source startPostgreSQL.sh
4) source createPostgreDB.sh
5) cd ../java/
6) source compile.sh
7) source run.sh ${LOGNAME}_DB 9998 $LOGNAME 

- Project Use:
8) Follow prompts listed in application.

- Shut Down Project:
9) cd ../postgresql/
10) source stopPostgreDB.sh
11) exit
