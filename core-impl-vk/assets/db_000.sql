create table users ( id integer primary key autoincrement, version integer not null, last_properties_sync_date datetime, last_friends_sync_date datetime, last_chats_sync_date datetime, last_user_icons_sync_date datetime );
create table user_properties ( user_id integer not null, property_name text not null, property_value text, foreign key(user_id) references users(id) on delete cascade, unique(user_id, property_name) );
create table user_friends ( user_id integer not null, friend_id integer not null, foreign key(user_id) references users(id) on delete cascade, foreign key(friend_id) references users(id) on delete cascade, unique(user_id, friend_id));
create table chats ( id text primary key, messages_count integer not null, last_messages_sync_date datetime);
create table chat_properties ( chat_id text not null, property_name text not null, property_value text, foreign key(chat_id) references chats(id) on delete cascade, unique(chat_id, property_name) );
create table user_chats ( user_id integer not null, chat_id text not null, foreign key(user_id) references users(id) on delete cascade, foreign key(chat_id) references chats(id) on delete cascade, unique(user_id, chat_id));
create table messages ( id integer primary key autoincrement, chat_id text not null, author_id integer not null, recipient_id integer, send_date datetime not null, title text not null, body text not null, foreign key(chat_id) references chats(id) on delete cascade, foreign key(author_id) references users(id) on delete cascade, foreign key(recipient_id) references users(id) on delete cascade);


