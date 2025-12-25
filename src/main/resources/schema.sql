
-- drop index if exists idx_vid_id;
-- drop index if exists idx_last_accessed_at;
drop table if exists heatmap;

create table heatmap(
    id bigint primary key auto_increment,
    title varchar(255) not null,
    vid_id varchar(255) not null unique ,
    heatmap text not null,
    last_accessed_at datetime not null default now()

);

create index  idx_vid_id on heatmap (vid_id);
create index  idx_last_accessed_at on heatmap (vid_id desc);

