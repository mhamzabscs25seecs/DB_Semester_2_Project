-- Note that in SQLITE there is no CREATE DATABASE Statement.
-- Moreover, in SQLITE (particularly in older versions of SQLITE) FKs are not enforced by default. So we have to do:
PRAGMA foreign_keys = ON;

CREATE TABLE Users (
	/* This User table will contain the core account data. Social media apps like ours absolutely depend on at least one 
	 core thing and that is to allow the user to login. So, we make this table. */
 /* Every user has a username, an id (for the system management), email which is a must to even view content,
  a password (which can be NOT UNIQUE) */
 
user_id INTEGER PRIMARY KEY AUTOINCREMENT,
username TEXT NOT NULL UNIQUE CHECK(LENGTH(username) <= 50),    
email TEXT NOT NULL UNIQUE CHECK(LENGTH(email) <=50),
password_hash TEXT NOT NULL,
role TEXT NOT NULL DEFAULT 'user' CHECK(role IN ('user', 'admin')),
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

-- Clixky is a platform for 'GROUP' discussions around topics. A group having a commonly discussed topic is called a Community
CREATE TABLE Communities (
	/* Although there is no harm in communities having same name, i think its better if we keep them unique so that if a user wants to join a 
		specific community he might have trouble navigating */
community_id INTEGER PRIMARY KEY AUTOINCREMENT,
community_name TEXT NOT NULL COLLATE NOCASE UNIQUE CHECK(LENGTH(TRIM(community_name)) > 0 AND LENGTH(community_name) <= 50),
description TEXT CHECK(LENGTH(description) <= 300),

-- Each community is created by one user at the time of creation.
-- If that user is deleted later, created_by becomes null.
-- But one User can create multiple communities
created_by INTEGER,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

FOREIGN KEY (created_by) REFERENCES Users(user_id) ON DELETE SET NULL
);

-- One user can join many communities and one community can have many User. So it is a many-to-many relationship
-- So we will model it with a Junction Table called Community_Membership

CREATE TABLE Community_Membership (
user_id INTEGER NOT NULL,
community_id INTEGER NOT NULL,
joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

PRIMARY KEY (user_id, community_id),

FOREIGN KEY (user_id) REFERENCES Users (user_id) ON DELETE CASCADE,
FOREIGN KEY (community_id) REFERENCES Communities (community_id) ON DELETE CASCADE
);

-- Now, our database will also have Posts table which keeps a track of who, where and when it was posted. Also its title and description
/*  Moreover, suppose if the admin of the Clixky app decide to delete a User and his account maybe because of a post that 
	is not according to the app's guidelines, they do not want to go into details and search for all the posts of that user.
	Meaning that they want to delete the posts of that user too. */
CREATE TABLE Posts (
post_id INTEGER PRIMARY KEY AUTOINCREMENT,
posted_by INTEGER NOT NULL,
community_id INTEGER NOT NULL,
title TEXT NOT NULL CHECK(LENGTH(title) <= 100),
body TEXT NOT NULL CHECK(LENGTH(TRIM(body)) > 0 AND LENGTH(body) <= 5000),
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME,

FOREIGN KEY (posted_by) REFERENCES Users(user_id) ON DELETE CASCADE,
FOREIGN KEY (community_id) REFERENCES Communities(community_id) ON DELETE CASCADE
);




CREATE TABLE Comments (
comment_id INTEGER PRIMARY KEY AUTOINCREMENT,
post_id INTEGER NOT NULL,
commenter_id INTEGER NOT NULL,
comment_body TEXT NOT NULL CHECK(LENGTH(comment_body) <= 2000),

commented_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME,
parent_comment_id INTEGER,


FOREIGN KEY (commenter_id) REFERENCES Users (user_id) ON DELETE CASCADE,
FOREIGN KEY (post_id) REFERENCES Posts (post_id) ON DELETE CASCADE,
-- If a comment is deleted, all its replies should also be deleted.
FOREIGN KEY (parent_comment_id) REFERENCES Comments(comment_id) ON DELETE CASCADE  

);

/* In our app, below every post there is an option for voting.
	This vote can be an upvote or a downvote */
-- We keep the (user_id, post_id) combination unique as we want one user to have 
-- only one vote (either upvote or downvote and certainly not both) per post.
CREATE TABLE Post_Votes (
vote_id INTEGER PRIMARY KEY AUTOINCREMENT,
post_id INTEGER NOT NULL,
user_id INTEGER NOT NULL,

vote_type INTEGER NOT NULL CHECK(vote_type IN(-1, 1)),

FOREIGN KEY (post_id) REFERENCES Posts (post_id) 
								ON DELETE CASCADE,		 
FOREIGN KEY (user_id) REFERENCES Users (user_id) 
								ON DELETE CASCADE,						
					
UNIQUE(user_id, post_id)	
);


-- Every post has comments which not only have their child comments, but also the comments have their own votes.

CREATE TABLE Comment_Votes (
comment_vote_id INTEGER PRIMARY KEY AUTOINCREMENT,
comment_id INTEGER NOT NULL,
comment_voter_id INTEGER NOT NULL,

comment_vote_type INTEGER NOT NULL CHECK (comment_vote_type IN(-1, 1)),

FOREIGN KEY (comment_id) REFERENCES Comments (comment_id) 
											ON DELETE CASCADE,
											
FOREIGN KEY (comment_voter_id) REFERENCES Users (user_id) 
											ON DELETE CASCADE,
											
UNIQUE (comment_voter_id, comment_id)
);

CREATE TABLE Saved_Posts (
user_id INTEGER NOT NULL,
post_id INTEGER NOT NULL,
saved_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

PRIMARY KEY (user_id, post_id),

FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
FOREIGN KEY (post_id) REFERENCES Posts(post_id) ON DELETE CASCADE
);

CREATE TABLE Reports (
report_id INTEGER PRIMARY KEY AUTOINCREMENT,
reported_by INTEGER NOT NULL,
target_type TEXT NOT NULL CHECK(target_type IN ('post', 'comment', 'community')),
target_id INTEGER NOT NULL,
reason TEXT NOT NULL CHECK(LENGTH(TRIM(reason)) > 0 AND LENGTH(reason) <= 80),
details TEXT CHECK(LENGTH(details) <= 500),
status TEXT NOT NULL DEFAULT 'open' CHECK(status IN ('open', 'reviewed', 'dismissed')),
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

FOREIGN KEY (reported_by) REFERENCES Users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_reports_target ON Reports(target_type, target_id);
CREATE INDEX idx_reports_status ON Reports(status);

CREATE TABLE Messages (
message_id INTEGER PRIMARY KEY AUTOINCREMENT,
sender_id INTEGER NOT NULL,
recipient_id INTEGER NOT NULL,
message_body TEXT NOT NULL CHECK(LENGTH(TRIM(message_body)) > 0 AND LENGTH(message_body) <= 1000),
sent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
read_at DATETIME,

FOREIGN KEY (sender_id) REFERENCES Users(user_id) ON DELETE CASCADE,
FOREIGN KEY (recipient_id) REFERENCES Users(user_id) ON DELETE CASCADE,
CHECK(sender_id <> recipient_id)
);

CREATE INDEX idx_messages_conversation ON Messages(sender_id, recipient_id, sent_at);
CREATE INDEX idx_messages_recipient_read ON Messages(recipient_id, read_at);

CREATE TABLE User_Follows (
follower_id INTEGER NOT NULL,
followed_id INTEGER NOT NULL,
followed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

PRIMARY KEY (follower_id, followed_id),

FOREIGN KEY (follower_id) REFERENCES Users(user_id) ON DELETE CASCADE,
FOREIGN KEY (followed_id) REFERENCES Users(user_id) ON DELETE CASCADE,
CHECK(follower_id <> followed_id)
);

CREATE INDEX idx_user_follows_followed ON User_Follows(followed_id, followed_at);
