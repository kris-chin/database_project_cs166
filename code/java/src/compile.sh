#! /bin/bash
export CLASSPATH=$CLASSPATH:PWD/pg73jdbc3.jar

javac -Xlint:deprecation MechanicShop.java
java MechanicShop $USER"_DB" $PGPORT $USER

#ay lmao

#rm -rf bin/*.class
#javac -cp ".;lib/postgresql-42.1.4.jar;" src/MechanicShop.java -d bin/
