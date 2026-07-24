alter table file_metadata
    add column uploading_at datetime;

alter table file_metadata
    add column completed_at datetime;

alter table file_metadata
    add column failed_at datetime;

alter table file_metadata
    add column deleting_at datetime;

alter table file_metadata
    add column deleted_at datetime;

