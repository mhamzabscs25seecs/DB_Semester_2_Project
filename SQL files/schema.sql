CREATE TABLE Users (
	/* This User table will contain the core account data. Social media apps like ours absolutely depend on at least one 
	 core thing and that is to allow the user to login. So, we make this table. */
 /* Every user has a username, an id (for the system management), email which is a must to even view content,
  a password (which can be NOT UNIQUE) */
 
user_id INTEGER PRIMARY KEY AUTOINCREMENT,
username TEXT NOT NULL UNIQUE CHECK(LENGTH(username) <= 50),    
email TEXT NOT NULL UNIQUE CHECK(LENGTH(email) <=50),
password_hash TEXT NOT NULL,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP    
/*When the profile is created, we do not want to go to the hassle of 
	manually configuring the time of creation so we put DEFAULT value by CURRENT_TIMESTAMP */
								
);

CREATE TABLE User_Profiles (
	-- This table will focus on the personal info of the user for i.e Profile details
	/* A User has a User_Profile, it is a 1:1 relationship. So we use the PK of Users as the PK + FK Of User_Profiles Table.
		The reason is that Users is the core table . If a user wants to register on our application, first he enters valid email and username password
		etc and then he goes to the window where he gives the information for this User_Profiles Table */

user_id INTEGER PRIMARY KEY,
display_name TEXT CHECK(LENGTH(display_name) <= 50),	
bio_text TEXT CHECK(LENGTH(bio_text) <= 250),
country TEXT CHECK(LENGTH(country) <= 30), 
phone_no TEXT CHECK(LENGTH(phone_no) <= 20),
birth_year INTEGER NOT NULL CHECK (birth_year >= 1900),
is_private INTEGER NOT NULL DEFAULT 0 CHECK(is_private IN(0, 1)),

FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

CREATE 
