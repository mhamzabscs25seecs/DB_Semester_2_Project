#!/bin/bash
cd "/home/muhammad-hamza/Desktop/DB_Semester_2_Project"
sqlite3 "SQL files/Clixky.db" < "SQL files/DML.sql"
echo "Database population script executed"
sqlite3 "SQL files/Clixky.db" "SELECT COUNT(*) as Users FROM Users; SELECT COUNT(*) as Posts FROM Posts; SELECT COUNT(*) as Comments FROM Comments;"

