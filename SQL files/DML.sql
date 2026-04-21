PRAGMA foreign_keys = ON;

-- This DML script is doing a lot of things i.e deleting old rows, resetting the counters, inserting data (creating rows)
-- SO we write BEGIN TRANSACTION to tell the .sql script to run as a single unit i.e if either fails it rolls back to how 
-- the database was before the script had started.
BEGIN TRANSACTION;

-- Optional cleanup so this script can be re-run safely.
DELETE FROM Comment_Votes;
DELETE FROM Post_Votes;
DELETE FROM Comments;
DELETE FROM Posts;
DELETE FROM Community_Membership;
DELETE FROM Communities;
DELETE FROM User_Profiles;
DELETE FROM Users;

-- Reset AUTOINCREMENT counters for deterministic ids.
-- Actually SQLITE makes its own table if we use AUTOINCREMENT for a table.

DELETE FROM sqlite_sequence
WHERE name IN (
	'Users',
	'Communities',
	'Posts',
	'Comments',
	'Post_Votes',
	'Comment_Votes'
);

INSERT INTO Users (user_id, username, email, password_hash, created_at) VALUES
(1, 'ali_codes', 'ali@clixky.app', 'hash_ali_001', '2026-01-10 09:15:00'),
(2, 'sara_dev', 'sara@clixky.app', 'hash_sara_002', '2026-01-11 11:20:00'),
(3, 'hamza_db', 'hamza@clixky.app', 'hash_hamza_003', '2026-01-14 15:05:00'),
(4, 'noor_ui', 'noor@clixky.app', 'hash_noor_004', '2026-01-16 17:45:00'),
(5, 'zain_ml', 'zain@clixky.app', 'hash_zain_005', '2026-01-20 08:35:00'),
(6, 'maria_net', 'maria@clixky.app', 'hash_maria_006', '2026-01-21 21:10:00'),
(7, 'omar_linux', 'omar@clixky.app', 'hash_omar_007', '2026-01-23 10:00:00'),
(8, 'huda_stats', 'huda@clixky.app', 'hash_huda_008', '2026-01-25 13:30:00');

INSERT INTO User_Profiles (user_id, display_name, bio_text, country, phone_no, birth_year, is_private) VALUES
(1, 'Ali', 'Backend and database enthusiast.', 'Pakistan', '+92-300-1111111', 2002, 0),
(2, 'Sara', 'Java developer who loves clean architecture.', 'Pakistan', '+92-300-2222222', 2001, 0),
(3, 'Hamza', 'Learning relational modeling and optimization.', 'Pakistan', '+92-300-3333333', 2003, 0),
(4, 'Noor', 'UI designer and accessibility advocate.', 'Pakistan', '+92-300-4444444', 2002, 1),
(5, 'Zain', 'Interested in ML systems and data pipelines.', 'Pakistan', '+92-300-5555555', 2000, 0),
(6, 'Maria', 'Networking, cloud, and distributed systems.', 'Pakistan', '+92-300-6666666', 1999, 0),
(7, 'Omar', 'Linux power user and automation geek.', 'Pakistan', '+92-300-7777777', 2001, 1),
(8, 'Huda', 'Statistics student and SQL learner.', 'Pakistan', '+92-300-8888888', 2004, 0);

INSERT INTO Communities (community_id, community_name, description, created_by, created_at) VALUES
(1, 'JavaProgramming', 'Discussion around Java language and ecosystem.', 2, '2026-01-12 10:00:00'),
(2, 'DatabaseDesign', 'Schema design, SQL, and normalization tips.', 3, '2026-01-12 11:00:00'),
(3, 'LinuxLab', 'Linux workflows, tools, and shell scripting.', 7, '2026-01-13 09:30:00'),
(4, 'AIandData', 'Machine learning, AI tools, and data engineering.', 5, '2026-01-15 14:20:00');

INSERT INTO Community_Membership (user_id, community_id, joined_at) VALUES
(1, 1, '2026-01-13 08:00:00'),
(1, 2, '2026-01-13 08:10:00'),
(2, 1, '2026-01-12 10:05:00'),
(2, 2, '2026-01-14 12:30:00'),
(3, 2, '2026-01-12 11:05:00'),
(3, 4, '2026-01-16 09:00:00'),
(4, 1, '2026-01-17 13:10:00'),
(5, 4, '2026-01-15 14:30:00'),
(6, 3, '2026-01-16 18:20:00'),
(6, 4, '2026-01-16 18:30:00'),
(7, 3, '2026-01-13 09:35:00'),
(8, 2, '2026-01-18 10:45:00'),
(8, 4, '2026-01-18 10:50:00');

INSERT INTO Posts (post_id, posted_by, community_id, title, body, created_at, updated_at) VALUES
(1, 2, 1, 'Best practices for JDBC connection pooling?', 'I am building a desktop app and want efficient DB access. What patterns should I follow?', '2026-02-01 09:00:00', NULL),
(2, 3, 2, 'SQLite vs PostgreSQL for learning DB concepts', 'I am focusing on learning core SQL first. Should I start with SQLite then move to Postgres?', '2026-02-01 10:20:00', NULL),
(3, 7, 3, 'Useful Linux commands for beginners', 'Share commands that helped you automate daily tasks.', '2026-02-02 07:40:00', NULL),
(4, 5, 4, 'How to evaluate a simple ML model', 'What metrics should I track first for binary classification?', '2026-02-02 12:15:00', NULL),
(5, 1, 2, 'Normal forms explained with examples', 'Can someone explain 1NF, 2NF, and 3NF with practical examples?', '2026-02-03 08:25:00', NULL),
(6, 6, 4, 'Data pipeline scheduling options', 'Cron is easy, but what should I use when workflows get complex?', '2026-02-03 15:10:00', NULL),
(7, 4, 1, 'Java Swing layout tips', 'How do you keep Swing screens maintainable as features grow?', '2026-02-04 11:50:00', NULL),
(8, 8, 2, 'Indexing strategy for read-heavy tables', 'I have many reads and fewer writes. Which columns should I index first?', '2026-02-05 16:35:00', NULL);

INSERT INTO Comments (comment_id, post_id, commenter_id, comment_body, commented_at, updated_at, parent_comment_id) VALUES
(1, 1, 1, 'HikariCP is a solid option in Java projects.', '2026-02-01 09:10:00', NULL, NULL),
(2, 1, 3, 'Also keep transaction scope small for better throughput.', '2026-02-01 09:22:00', NULL, NULL),
(3, 1, 2, 'Thanks, I will benchmark both suggestions.', '2026-02-01 09:45:00', NULL, 1),
(4, 2, 8, 'SQLite is great to learn schema design quickly.', '2026-02-01 11:00:00', NULL, NULL),
(5, 2, 6, 'Move to PostgreSQL when you need advanced concurrency.', '2026-02-01 11:30:00', NULL, NULL),
(6, 5, 3, 'Start by identifying repeating groups for 1NF.', '2026-02-03 09:00:00', NULL, NULL),
(7, 5, 1, 'Then remove partial dependencies for 2NF.', '2026-02-03 09:20:00', NULL, 6),
(8, 8, 5, 'Index columns used in WHERE and JOIN clauses first.', '2026-02-05 17:05:00', NULL, NULL),
(9, 8, 2, 'Measure query plans before and after adding indexes.', '2026-02-05 17:20:00', NULL, 8),
(10, 4, 8, 'Accuracy alone can mislead on imbalanced datasets.', '2026-02-02 13:00:00', NULL, NULL);

INSERT INTO Post_Votes (vote_id, post_id, user_id, vote_type) VALUES
(1, 1, 1, 1),
(2, 1, 3, 1),
(3, 1, 4, 1),
(4, 1, 5, -1),
(5, 2, 1, 1),
(6, 2, 2, 1),
(7, 2, 6, 1),
(8, 3, 1, 1),
(9, 3, 6, 1),
(10, 4, 3, 1),
(11, 4, 8, 1),
(12, 5, 2, 1),
(13, 5, 8, 1),
(14, 6, 5, 1),
(15, 7, 2, 1),
(16, 8, 1, 1),
(17, 8, 3, 1),
(18, 8, 7, -1);

INSERT INTO Comment_Votes (comment_vote_id, comment_id, comment_voter_id, comment_vote_type) VALUES
(1, 1, 2, 1),
(2, 1, 3, 1),
(3, 2, 1, 1),
(4, 2, 5, 1),
(5, 3, 1, 1),
(6, 4, 3, 1),
(7, 5, 2, 1),
(8, 6, 8, 1),
(9, 7, 3, 1),
(10, 8, 2, 1),
(11, 9, 5, 1),
(12, 10, 6, 1);

COMMIT;
