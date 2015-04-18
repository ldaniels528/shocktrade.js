create table chat (
	contest_id text,
	sent_by text,
	sent_time timestamp,
	message text,
	primary key(contest_id, message));