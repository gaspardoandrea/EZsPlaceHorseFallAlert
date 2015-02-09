create table if not exists alert_events (
    _id integer primary key autoincrement unique,
    entry_datetime datetime not null default CURRENT_TIMESTAMP,
    event_text varchar(255)
);