#!/usr/bin/env python3
import random
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "SQL files" / "DML.sql"
RNG = random.Random(42)


def q(value):
    if value is None:
        return "NULL"
    if isinstance(value, int):
        return str(value)
    return "'" + str(value).replace("'", "''") + "'"


def insert(table, columns, rows):
    lines = [f"INSERT INTO {table} ({', '.join(columns)}) VALUES"]
    values = []
    for row in rows:
        values.append("(" + ", ".join(q(v) for v in row) + ")")
    lines.append(",\n".join(values) + ";")
    return "\n".join(lines)


def ts(day, hour, minute=0):
    return f"2026-03-{day:02d} {hour:02d}:{minute:02d}:00"


users = [
    (1, "hamza_db", "hamza@clixky.app", "hash_hamza_001", "admin", "2026-01-10 09:15:00"),
    (2, "aayan_db", "aayan@clixky.app", "hash_aayan_002", "user", "2026-01-11 11:20:00"),
    (3, "ali_db", "ali@clixky.app", "hash_ali_003", "user", "2026-01-14 15:05:00"),
    (4, "maryam_db", "maryam@clixky.app", "hash_maryam_004", "user", "2026-01-16 17:45:00"),
]

extra_names = [
    "sara_dev", "noor_ui", "zain_ml", "omar_linux", "huda_stats", "fatima_cloud",
    "bilal_api", "ayesha_sql", "danish_ops", "iqra_design", "usman_mobile", "maha_data",
    "rayyan_java", "sana_security", "talha_web", "laiba_ai", "faris_backend", "eman_frontend",
    "kashif_devops", "nimra_react", "saad_python", "rabia_product", "junaid_test", "mehwish_docs",
    "arham_go", "hania_cloud", "waleed_kotlin", "bisma_qa",
]
for i, username in enumerate(extra_names, start=5):
    users.append((i, username, f"{username.split('_')[0]}@clixky.app", f"hash_{username}_{i:03d}", "user", ts(1 + i % 18, 8 + i % 10, (i * 7) % 60)))

profile_titles = [
    "Database learner and community builder.",
    "Full-stack developer focused on useful products.",
    "Backend engineer who likes clean schemas.",
    "Data scientist exploring practical machine learning.",
    "UI designer who cares about readable interfaces.",
    "Cloud engineer automating deployments.",
    "Student building portfolio projects.",
    "DevOps enthusiast learning Linux every day.",
]
profiles = []
for user_id, username, email, *_ in users:
    display = " ".join(part.capitalize() for part in username.replace("_", " ").split())
    profiles.append((
        user_id,
        display,
        profile_titles[user_id % len(profile_titles)],
        "Pakistan",
        f"+92-300-{1000000 + user_id:07d}",
        1997 + (user_id % 8),
        1 if user_id in {5, 8, 14, 21, 27} else 0,
    ))

communities = [
    (1, "JavaProgramming", "Java, Swing, Spring Boot, JDBC, and JVM ecosystem discussions.", 2, "2026-01-12 10:00:00"),
    (2, "DatabaseDesign", "Schema design, SQL, normalization, indexing, and query tuning.", 1, "2026-01-12 11:00:00"),
    (3, "WebDevelopment", "Frontend, backend, APIs, and full-stack architecture.", 5, "2026-01-13 09:30:00"),
    (4, "MachineLearning", "Models, metrics, datasets, and practical AI workflows.", 7, "2026-01-15 14:20:00"),
    (5, "CloudComputing", "Azure, AWS, GCP, containers, and infrastructure automation.", 10, "2026-01-16 08:45:00"),
    (6, "LinuxSystems", "Linux administration, shell scripting, networking, and servers.", 8, "2026-01-17 10:15:00"),
    (7, "MobileDev", "Android, iOS, Kotlin, Flutter, and React Native.", 15, "2026-01-18 12:00:00"),
    (8, "CareerDev", "Resumes, interviews, internships, and professional growth.", 3, "2026-01-19 14:30:00"),
    (9, "CyberSecurity", "Secure coding, auth, threat modeling, and defensive security.", 18, "2026-01-20 09:00:00"),
    (10, "DataAnalytics", "Dashboards, BI, statistics, visualization, and reporting.", 16, "2026-01-21 10:00:00"),
    (11, "DevOpsLab", "CI/CD, Docker, Kubernetes, observability, and release workflows.", 23, "2026-01-22 11:00:00"),
    (12, "StudentProjects", "Semester projects, demos, documentation, and project reviews.", 1, "2026-01-23 12:00:00"),
    (13, "PythonSpace", "Python apps, automation, scripting, and backend services.", 25, "2026-01-24 13:00:00"),
    (14, "ProductDesign", "Product thinking, UX flows, accessibility, and design critique.", 28, "2026-01-25 14:00:00"),
]

memberships_by_pair = {}
for cid, _name, _desc, creator, created_at in communities:
    memberships_by_pair[(creator, cid)] = created_at
for uid in range(1, len(users) + 1):
    count = 5 if uid <= 4 else RNG.randint(3, 7)
    for cid in RNG.sample(range(1, len(communities) + 1), count):
        memberships_by_pair.setdefault((uid, cid), ts(2 + (uid + cid) % 20, 9 + cid % 8, (uid * cid) % 60))
memberships = sorted((uid, cid, joined_at) for (uid, cid), joined_at in memberships_by_pair.items())

topics = {
    1: [
        "When should I use SwingWorker in a Java UI?",
        "Best way to structure DAO classes in a desktop app",
        "SQLite JDBC connection handling tips",
        "Java records vs classic DTO classes",
        "How to debug Event Dispatch Thread freezes",
        "Packaging a Java Swing app for submission",
    ],
    2: [
        "Composite keys vs surrogate keys in real schemas",
        "How many indexes are too many for SQLite?",
        "Normal forms with social app examples",
        "Designing vote tables with uniqueness constraints",
        "Foreign key cascades: when are they dangerous?",
        "Query plan reading for beginners",
    ],
    3: [
        "REST endpoint naming for community platforms",
        "Frontend state management for feeds",
        "How to paginate posts without duplicates",
        "Server-side validation patterns",
        "Search UX for posts and communities",
        "Handling empty states in dashboards",
    ],
    4: [
        "Evaluating an imbalanced classifier",
        "Feature engineering checklist for student projects",
        "Train/test leakage examples",
        "When should I normalize numeric features?",
        "Explaining model results to non-technical users",
        "Simple recommender ideas for Clixky",
    ],
}
fallback_titles = [
    "What is your current workflow for this topic?",
    "Beginner mistakes worth avoiding",
    "Tools that made your work easier",
    "How would you design this feature?",
    "Share a useful checklist",
    "What would you improve in a semester project?",
]

posts = []
post_id = 1
for cid, community_name, _desc, _creator, _created in communities:
    titles = topics.get(cid, fallback_titles)
    for index in range(6):
        author = RNG.choice([uid for uid, mcid, _ in memberships if mcid == cid])
        title = titles[index % len(titles)]
        body = (
            f"I am working in r/{community_name} and want practical advice on this: {title}. "
            "Please share tradeoffs, examples, and what you would do in a real project demo."
        )
        posts.append((post_id, author, cid, title, body, ts(3 + post_id % 24, 7 + post_id % 12, (post_id * 5) % 60), None))
        post_id += 1

comment_templates = [
    "I would start small, verify the behavior, and only add complexity when the data proves it is needed.",
    "This is a good place to document assumptions because future changes can break the relationship silently.",
    "For a semester demo, clarity matters more than using every advanced feature at once.",
    "Try writing two or three test queries first. They reveal schema problems faster than UI debugging.",
    "The tradeoff is usually between simplicity now and flexibility later.",
    "I used a similar approach and the biggest improvement came from better naming.",
    "Make sure the UI shows the failure clearly instead of only printing to the console.",
    "This depends on read patterns. Measure the common queries before changing the design.",
]
comments = []
comment_id = 1
root_comments_by_post = {}
for pid, author, cid, *_ in posts:
    roots = []
    commenter_pool = [uid for uid, mcid, _ in memberships if mcid == cid and uid != author]
    if len(commenter_pool) < 4:
        commenter_pool = [uid for uid, *_ in users if uid != author]
    for _ in range(RNG.randint(3, 5)):
        commenter = RNG.choice(commenter_pool)
        body = RNG.choice(comment_templates)
        comments.append((comment_id, pid, commenter, body, ts(4 + pid % 24, 9 + comment_id % 10, (comment_id * 3) % 60), None, None))
        roots.append(comment_id)
        comment_id += 1
    root_comments_by_post[pid] = roots
    for parent in RNG.sample(roots, RNG.randint(1, min(3, len(roots)))):
        commenter = RNG.choice(commenter_pool)
        body = "Replying here because this point is important for implementation details and demo reliability."
        comments.append((comment_id, pid, commenter, body, ts(5 + pid % 23, 10 + comment_id % 9, (comment_id * 7) % 60), None, parent))
        comment_id += 1

post_votes = []
vote_id = 1
for pid, author, cid, *_ in posts:
    voters = RNG.sample([uid for uid, *_ in users if uid != author], RNG.randint(10, 20))
    for voter in voters:
        vote_type = -1 if RNG.random() < 0.12 else 1
        post_votes.append((vote_id, pid, voter, vote_type))
        vote_id += 1

comment_votes = []
cv_id = 1
for cid_value, _pid, commenter, *_ in comments:
    voters = RNG.sample([uid for uid, *_ in users if uid != commenter], RNG.randint(4, 10))
    for voter in voters:
        vote_type = -1 if RNG.random() < 0.10 else 1
        comment_votes.append((cv_id, cid_value, voter, vote_type))
        cv_id += 1

saved_posts = set()
for uid, *_ in users:
    for pid in RNG.sample(range(1, len(posts) + 1), RNG.randint(5, 10)):
        saved_posts.add((uid, pid, ts(6 + (uid + pid) % 22, 8 + uid % 8, (uid * pid) % 60)))
saved_posts = sorted(saved_posts)

messages = []
message_id = 1
conversation_pairs = set()
for uid in range(1, len(users) + 1):
    for other in RNG.sample([x for x in range(1, len(users) + 1) if x != uid], 3):
        conversation_pairs.add(tuple(sorted((uid, other))))
for a, b in sorted(conversation_pairs):
    count = RNG.randint(2, 5)
    for idx in range(count):
        sender, recipient = (a, b) if idx % 2 == 0 else (b, a)
        body = RNG.choice([
            "Can you review my latest Clixky change?",
            "I found a useful query pattern for the dashboard.",
            "Let's test the demo flow before submission.",
            "The database seed data is much better now.",
            "I will check the comments and reports panel.",
        ])
        sent = ts(7 + message_id % 20, 9 + message_id % 11, (message_id * 4) % 60)
        read = sent if RNG.random() < 0.75 else None
        messages.append((message_id, sender, recipient, body, sent, read))
        message_id += 1

follows = set()
for uid, *_ in users:
    for followed in RNG.sample([x for x in range(1, len(users) + 1) if x != uid], RNG.randint(6, 11)):
        follows.add((uid, followed, ts(2 + (uid + followed) % 23, 8 + uid % 9, (uid * followed) % 60)))
follows = sorted(follows)

report_reasons = [
    "Off-topic content",
    "Spam or advertisement",
    "Inappropriate language",
    "Duplicate post",
    "Privacy violation",
    "Misleading information",
]
reports = []
for rid in range(1, 61):
    reporter = RNG.randint(1, len(users))
    target_type = RNG.choice(["post", "comment", "community"])
    if target_type == "post":
        target_id = RNG.randint(1, len(posts))
    elif target_type == "comment":
        target_id = RNG.randint(1, len(comments))
    else:
        target_id = RNG.randint(1, len(communities))
    reports.append((
        rid,
        reporter,
        target_type,
        target_id,
        RNG.choice(report_reasons),
        "Generated moderation case for testing admin review workflows.",
        RNG.choice(["open", "reviewed", "dismissed"]),
        ts(8 + rid % 18, 8 + rid % 10, (rid * 11) % 60),
    ))

sections = [
    "PRAGMA foreign_keys = ON;",
    "PRAGMA temp_store = MEMORY;",
    "BEGIN TRANSACTION;",
    """DELETE FROM Comment_Votes;
DELETE FROM Chat_Access_Requests;
DELETE FROM User_Blocks;
DELETE FROM Community_Blocks;
DELETE FROM Reports;
DELETE FROM Saved_Posts;
DELETE FROM User_Follows;
DELETE FROM Messages;
DELETE FROM Post_Votes;
DELETE FROM Comments;
DELETE FROM Posts;
DELETE FROM Community_Membership;
DELETE FROM Communities;
DELETE FROM User_Profiles;
DELETE FROM Users;""",
    """DELETE FROM sqlite_sequence
WHERE name IN (
    'Users', 'Communities', 'Posts', 'Comments', 'Post_Votes',
    'Comment_Votes', 'Reports', 'Messages', 'Chat_Access_Requests'
);""",
    "-- Main users: hamza_db is admin; aayan_db, ali_db, and maryam_db are regular users.",
    insert("Users", ["user_id", "username", "email", "password_hash", "role", "created_at"], users),
    insert("User_Profiles", ["user_id", "display_name", "bio_text", "country", "phone_no", "birth_year", "is_private"], profiles),
    insert("Communities", ["community_id", "community_name", "description", "created_by", "created_at"], communities),
    insert("Community_Membership", ["user_id", "community_id", "joined_at"], memberships),
    insert("Posts", ["post_id", "posted_by", "community_id", "title", "body", "created_at", "updated_at"], posts),
    insert("Comments", ["comment_id", "post_id", "commenter_id", "comment_body", "commented_at", "updated_at", "parent_comment_id"], comments),
    insert("Post_Votes", ["vote_id", "post_id", "user_id", "vote_type"], post_votes),
    insert("Comment_Votes", ["comment_vote_id", "comment_id", "comment_voter_id", "comment_vote_type"], comment_votes),
    insert("Saved_Posts", ["user_id", "post_id", "saved_at"], saved_posts),
    insert("Messages", ["message_id", "sender_id", "recipient_id", "message_body", "sent_at", "read_at"], messages),
    insert("User_Follows", ["follower_id", "followed_id", "followed_at"], follows),
    insert("Reports", ["report_id", "reported_by", "target_type", "target_id", "reason", "details", "status", "created_at"], reports),
    "COMMIT;",
]

OUT.write_text("\n\n".join(sections) + "\n", encoding="utf-8")
print(f"Wrote {OUT}")
print(f"Users={len(users)} Communities={len(communities)} Posts={len(posts)} Comments={len(comments)}")
print(f"PostVotes={len(post_votes)} CommentVotes={len(comment_votes)} Saved={len(saved_posts)}")
print(f"Messages={len(messages)} Follows={len(follows)} Reports={len(reports)}")
