#!/bin/bash
# Database Population Script for Clixky
# This script populates the SQLite database with sample data

set -e  # Exit on error

PROJECT_DIR="/home/muhammad-hamza/Desktop/DB_Semester_2_Project"
DB_FILE="$PROJECT_DIR/SQL files/Clixky.db"
DML_FILE="$PROJECT_DIR/SQL files/DML.sql"

echo "========================================"
echo "🚀 Clixky Database Population Script"
echo "========================================"
echo ""

# Check if files exist
if [ ! -f "$DB_FILE" ]; then
    echo "❌ Database file not found: $DB_FILE"
    exit 1
fi

if [ ! -f "$DML_FILE" ]; then
    echo "❌ DML script not found: $DML_FILE"
    exit 1
fi

echo "📁 Database: $DB_FILE"
echo "📄 DML Script: $DML_FILE"
echo ""

# Execute the DML script
echo "⏳ Populating database..."
sqlite3 "$DB_FILE" < "$DML_FILE"

echo "✅ Database populated successfully!"
echo ""

# Verify the population
echo "📊 Database Contents:"
echo "  Users:"
sqlite3 "$DB_FILE" "SELECT COUNT(*) FROM Users;" | xargs echo "    Count:"

echo "  Posts:"
sqlite3 "$DB_FILE" "SELECT COUNT(*) FROM Posts;" | xargs echo "    Count:"

echo "  Comments:"
sqlite3 "$DB_FILE" "SELECT COUNT(*) FROM Comments;" | xargs echo "    Count:"

echo "  Communities:"
sqlite3 "$DB_FILE" "SELECT COUNT(*) FROM Communities;" | xargs echo "    Count:"

echo "  Messages:"
sqlite3 "$DB_FILE" "SELECT COUNT(*) FROM Messages;" | xargs echo "    Count:"

echo "  Saved Posts:"
sqlite3 "$DB_FILE" "SELECT COUNT(*) FROM Saved_Posts;" | xargs echo "    Count:"

echo "  User Follows:"
sqlite3 "$DB_FILE" "SELECT COUNT(*) FROM User_Follows;" | xargs echo "    Count:"

echo "  Reports:"
sqlite3 "$DB_FILE" "SELECT COUNT(*) FROM Reports;" | xargs echo "    Count:"

echo ""
echo "👥 Main Users:"
sqlite3 "$DB_FILE" ".mode column" ".headers on" "SELECT user_id, username, email, role FROM Users WHERE user_id <= 4;"

echo ""
echo "========================================"
echo "✨ All done! Database is ready to use."
echo "========================================"

