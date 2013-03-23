create table realms ( id text primary key, realm_def_id text not null, user_id text not null, configuration text not null );
create table users ( id text primary key, realm_id text not null, realm_user_id text not null, last_properties_sync_date datetime, last_contacts_sync_date datetime, last_chats_sync_date datetime, last_user_icons_sync_date datetime );
create table user_properties ( user_id text not null, property_name text not null, property_value text, foreign key(user_id) references users(id) on delete cascade, unique(user_id, property_name) );
create table user_contacts ( user_id text not null, contact_id text not null, foreign key(user_id) references users(id) on delete cascade, foreign key(contact_id) references users(id) on delete cascade, unique(user_id, contact_id));
create table chats ( id text primary key, realm_id text not null, realm_chat_id text not null, last_messages_sync_date datetime);
create table chat_properties ( chat_id text not null, property_name text not null, property_value text, foreign key(chat_id) references chats(id) on delete cascade, unique(chat_id, property_name) );
create table user_chats ( user_id text not null, chat_id text not null, foreign key(user_id) references users(id) on delete cascade, foreign key(chat_id) references chats(id) on delete cascade, unique(user_id, chat_id));
create table messages ( id text primary key, realm_id text not null, realm_message_id text not null, chat_id text not null, author_id text not null, recipient_id text, send_date datetime not null, send_time integer  not null, title text not null, body text not null, read integer not null, foreign key(chat_id) references chats(id) on delete cascade, foreign key(author_id) references users(id) on delete cascade, foreign key(recipient_id) references users(id) on delete cascade);


