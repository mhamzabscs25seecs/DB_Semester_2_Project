#!/usr/bin/env python3
import sqlite3
import os

db_path = "/home/muhammad-hamza/Desktop/DB_Semester_2_Project/SQL files/Clixky.db"
dml_path = "/home/muhammad-hamza/Desktop/DB_Semester_2_Project/SQL files/DML.sql"

try:
    conn = sqlite3.connect(db_path)
    with open(dml_path, 'r') as f:
        sql_script = f.read()
    conn.executescript(sql_script)
    conn.commit()
    print("✅ Database populated successfully!")

    # Verify with counts
    cursor = conn.cursor()
    cursor.execute("SELECT COUNT(*) FROM Users")
    users = cursor.fetchone()[0]
    cursor.execute("SELECT COUNT(*) FROM Posts")
    posts = cursor.fetchone()[0]
    cursor.execute("SELECT COUNT(*) FROM Comments")
    comments = cursor.fetchone()[0]
    cursor.execute("SELECT COUNT(*) FROM Messages")
    messages = cursor.fetchone()[0]
    cursor.execute("SELECT COUNT(*) FROM Saved_Posts")
    saved = cursor.fetchone()[0]
    cursor.execute("SELECT COUNT(*) FROM User_Follows")
    follows = cursor.fetchone()[0]
    cursor.execute("SELECT COUNT(*) FROM Reports")
    reports = cursor.fetchone()[0]
    cursor.execute("SELECT COUNT(*) FROM Communities")
    communities = cursor.fetchone()[0]

    print("\n📊 Database Summary:")
    print(f"   🧑 Users: {users}")
    print(f"   📝 Posts: {posts}")
    print(f"   💬 Comments: {comments}")
    print(f"   💌 Messages: {messages}")
    print(f"   🔖 Saved Posts: {saved}")
    print(f"   👥 Follows: {follows}")
    print(f"   🚩 Reports: {reports}")
    print(f"   🏘️  Communities: {communities}")

    # Show the 4 main users
    print("\n👨‍💼 Main Users:")
    cursor.execute("SELECT user_id, username, email, role FROM Users ORDER BY user_id LIMIT 4")
    for row in cursor.fetchall():
        user_id, username, email, role = row
        print(f"   {user_id}. {username} ({role}) - {email}")

    conn.close()
except Exception as e:
    print(f"❌ Error: {e}")
    import traceback
    traceback.print_exc()

