-- Verification Queries for Clixky Database
-- Use these queries to verify your database is populated correctly

-- ============================================================
-- 1. USER VERIFICATION
-- ============================================================

-- Count total users
SELECT COUNT(*) as Total_Users FROM Users;

-- List all users
SELECT user_id, username, email, role FROM Users ORDER BY user_id;

-- Show the 4 main users
SELECT user_id, username, email, role FROM Users WHERE user_id <= 4;

-- User profiles with bio
SELECT u.user_id, u.username, p.display_name, p.bio_text, p.country
FROM Users u
LEFT JOIN User_Profiles p ON u.user_id = p.user_id
ORDER BY u.user_id;

-- Count admin users
SELECT COUNT(*) as Admin_Count FROM Users WHERE role = 'admin';

-- ============================================================
-- 2. COMMUNITY VERIFICATION
-- ============================================================

-- Count total communities
SELECT COUNT(*) as Total_Communities FROM Communities;

-- List all communities
SELECT community_id, community_name, description, created_by
FROM Communities ORDER BY community_id;

-- Community membership count
SELECT c.community_id, c.community_name, COUNT(cm.user_id) as Member_Count
FROM Communities c
LEFT JOIN Community_Membership cm ON c.community_id = cm.community_id
GROUP BY c.community_id
ORDER BY c.community_id;

-- Users in JavaProgramming community
SELECT u.username FROM Users u
JOIN Community_Membership cm ON u.user_id = cm.user_id
JOIN Communities c ON cm.community_id = c.community_id
WHERE c.community_name = 'JavaProgramming'
ORDER BY u.username;

-- ============================================================
-- 3. POST VERIFICATION
-- ============================================================

-- Count total posts
SELECT COUNT(*) as Total_Posts FROM Posts;

-- List all posts with author and community
SELECT p.post_id, p.title, u.username, c.community_name, p.created_at
FROM Posts p
JOIN Users u ON p.posted_by = u.user_id
JOIN Communities c ON p.community_id = c.community_id
ORDER BY p.post_id;

-- Posts with comment count
SELECT p.post_id, p.title, COUNT(c.comment_id) as Comment_Count
FROM Posts p
LEFT JOIN Comments c ON p.post_id = c.post_id
GROUP BY p.post_id
ORDER BY p.post_id;

-- Posts with vote count
SELECT p.post_id, p.title,
       SUM(CASE WHEN pv.vote_type = 1 THEN 1 ELSE 0 END) as Upvotes,
       SUM(CASE WHEN pv.vote_type = -1 THEN 1 ELSE 0 END) as Downvotes,
       COUNT(pv.vote_id) as Total_Votes
FROM Posts p
LEFT JOIN Post_Votes pv ON p.post_id = pv.post_id
GROUP BY p.post_id
ORDER BY p.post_id;

-- Most voted posts
SELECT p.post_id, p.title, COUNT(pv.vote_id) as Vote_Count
FROM Posts p
LEFT JOIN Post_Votes pv ON p.post_id = pv.post_id
GROUP BY p.post_id
ORDER BY COUNT(pv.vote_id) DESC
LIMIT 10;

-- ============================================================
-- 4. COMMENT VERIFICATION
-- ============================================================

-- Count total comments
SELECT COUNT(*) as Total_Comments FROM Comments;

-- Comments with replies (nested)
SELECT parent_id.comment_id as Parent_Comment,
       parent_id.comment_body as Parent_Body,
       child_id.comment_id as Reply_Comment,
       child_id.comment_body as Reply_Body
FROM Comments parent_id
JOIN Comments child_id ON parent_id.comment_id = child_id.parent_comment_id
ORDER BY parent_id.comment_id, child_id.comment_id;

-- Comment count per post
SELECT p.post_id, p.title, COUNT(c.comment_id) as Comment_Count
FROM Posts p
LEFT JOIN Comments c ON p.post_id = c.post_id
GROUP BY p.post_id
ORDER BY COUNT(c.comment_id) DESC;

-- Show comment threads for post 1
SELECT c.comment_id, c.comment_body, u.username, c.parent_comment_id, c.commented_at
FROM Comments c
JOIN Users u ON c.commenter_id = u.user_id
WHERE c.post_id = 1
ORDER BY c.parent_comment_id, c.comment_id;

-- ============================================================
-- 5. VOTING VERIFICATION
-- ============================================================

-- Count total votes
SELECT COUNT(*) as Total_Post_Votes FROM Post_Votes;
SELECT COUNT(*) as Total_Comment_Votes FROM Comment_Votes;

-- Voting summary
SELECT 'Post Votes' as Type, COUNT(*) as Count FROM Post_Votes
UNION ALL
SELECT 'Comment Votes' as Type, COUNT(*) as Count FROM Comment_Votes;

-- Vote type distribution
SELECT
  CASE WHEN vote_type = 1 THEN 'Upvote' ELSE 'Downvote' END as Vote_Type,
  COUNT(*) as Count
FROM Post_Votes
GROUP BY vote_type;

-- Users who voted
SELECT u.username, COUNT(pv.vote_id) as Votes_Cast
FROM Users u
LEFT JOIN Post_Votes pv ON u.user_id = pv.user_id
GROUP BY u.user_id, u.username
ORDER BY COUNT(pv.vote_id) DESC;

-- ============================================================
-- 6. MESSAGE VERIFICATION
-- ============================================================

-- Count total messages
SELECT COUNT(*) as Total_Messages FROM Messages;

-- List all messages
SELECT m.message_id, u1.username as From_User, u2.username as To_User,
       m.message_body, m.sent_at, m.read_at
FROM Messages m
JOIN Users u1 ON m.sender_id = u1.user_id
JOIN Users u2 ON m.recipient_id = u2.user_id
ORDER BY m.sent_at;

-- Unread messages
SELECT COUNT(*) as Unread_Messages FROM Messages WHERE read_at IS NULL;

-- Messages per user
SELECT u.username, COUNT(m.message_id) as Received_Messages
FROM Users u
LEFT JOIN Messages m ON u.user_id = m.recipient_id
GROUP BY u.user_id, u.username
ORDER BY COUNT(m.message_id) DESC;

-- ============================================================
-- 7. SOCIAL FEATURES VERIFICATION
-- ============================================================

-- Count total follows
SELECT COUNT(*) as Total_Follows FROM User_Follows;

-- Users followed by each user
SELECT u.username, COUNT(uf.followed_id) as Following_Count
FROM Users u
LEFT JOIN User_Follows uf ON u.user_id = uf.follower_id
GROUP BY u.user_id, u.username
ORDER BY COUNT(uf.followed_id) DESC;

-- Followers of each user
SELECT u.username, COUNT(uf.follower_id) as Follower_Count
FROM Users u
LEFT JOIN User_Follows uf ON u.user_id = uf.followed_id
GROUP BY u.user_id, u.username
ORDER BY COUNT(uf.follower_id) DESC;

-- ============================================================
-- 8. SAVED POSTS VERIFICATION
-- ============================================================

-- Count total saved posts
SELECT COUNT(*) as Total_Saved_Posts FROM Saved_Posts;

-- Saved posts per user
SELECT u.username, COUNT(sp.post_id) as Saved_Count
FROM Users u
LEFT JOIN Saved_Posts sp ON u.user_id = sp.user_id
GROUP BY u.user_id, u.username
ORDER BY COUNT(sp.post_id) DESC;

-- Most saved posts
SELECT p.post_id, p.title, COUNT(sp.user_id) as Save_Count
FROM Posts p
LEFT JOIN Saved_Posts sp ON p.post_id = sp.post_id
GROUP BY p.post_id
ORDER BY COUNT(sp.user_id) DESC;

-- ============================================================
-- 9. REPORTS VERIFICATION
-- ============================================================

-- Count total reports
SELECT COUNT(*) as Total_Reports FROM Reports;

-- Reports by status
SELECT status, COUNT(*) as Count
FROM Reports
GROUP BY status;

-- All reports with details
SELECT r.report_id, u.username, r.target_type, r.target_id, r.reason, r.status
FROM Reports r
JOIN Users u ON r.reported_by = u.user_id
ORDER BY r.created_at DESC;

-- ============================================================
-- 10. COMPREHENSIVE STATISTICS
-- ============================================================

-- Overall statistics
SELECT
  (SELECT COUNT(*) FROM Users) as Total_Users,
  (SELECT COUNT(*) FROM Communities) as Total_Communities,
  (SELECT COUNT(*) FROM Posts) as Total_Posts,
  (SELECT COUNT(*) FROM Comments) as Total_Comments,
  (SELECT COUNT(*) FROM Post_Votes) as Total_Votes,
  (SELECT COUNT(*) FROM Messages) as Total_Messages,
  (SELECT COUNT(*) FROM User_Follows) as Total_Follows,
  (SELECT COUNT(*) FROM Saved_Posts) as Total_Saved,
  (SELECT COUNT(*) FROM Reports) as Total_Reports;

-- User activity summary
SELECT
  u.user_id,
  u.username,
  COUNT(DISTINCT p.post_id) as Posts_Created,
  COUNT(DISTINCT c.comment_id) as Comments_Made,
  COUNT(DISTINCT pv.post_id) as Posts_Voted,
  COUNT(DISTINCT m1.message_id) as Messages_Sent,
  COUNT(DISTINCT uf.followed_id) as Users_Following,
  COUNT(DISTINCT sp.post_id) as Posts_Saved
FROM Users u
LEFT JOIN Posts p ON u.user_id = p.posted_by
LEFT JOIN Comments c ON u.user_id = c.commenter_id
LEFT JOIN Post_Votes pv ON u.user_id = pv.user_id
LEFT JOIN Messages m1 ON u.user_id = m1.sender_id
LEFT JOIN User_Follows uf ON u.user_id = uf.follower_id
LEFT JOIN Saved_Posts sp ON u.user_id = sp.user_id
GROUP BY u.user_id, u.username
ORDER BY u.user_id;

-- ============================================================
-- SAMPLE QUERY: View a specific post with all its data
-- ============================================================

-- Example: View post 1 with all comments
.mode column
.headers on
SELECT
  p.post_id,
  p.title,
  p.body,
  u.username as Author,
  c.community_name as Community,
  p.created_at
FROM Posts p
JOIN Users u ON p.posted_by = u.user_id
JOIN Communities c ON p.community_id = c.community_id
WHERE p.post_id = 1;

-- Comments on post 1
SELECT
  c.comment_id,
  c.comment_body,
  u.username,
  c.commented_at,
  c.parent_comment_id as Reply_To
FROM Comments c
JOIN Users u ON c.commenter_id = u.user_id
WHERE c.post_id = 1
ORDER BY c.parent_comment_id, c.comment_id;

-- ============================================================
-- DATA VALIDATION QUERIES
-- ============================================================

-- Check for orphaned records (should return 0)
SELECT COUNT(*) as Orphaned_Posts
FROM Posts p
WHERE NOT EXISTS (SELECT 1 FROM Communities c WHERE c.community_id = p.community_id);

SELECT COUNT(*) as Orphaned_Comments
FROM Comments c
WHERE NOT EXISTS (SELECT 1 FROM Posts p WHERE p.post_id = c.post_id);

-- Check for duplicate votes (should return 0)
SELECT user_id, post_id, COUNT(*) as Duplicate_Count
FROM Post_Votes
GROUP BY user_id, post_id
HAVING COUNT(*) > 1;

-- ============================================================
-- END OF VERIFICATION QUERIES
-- ============================================================

