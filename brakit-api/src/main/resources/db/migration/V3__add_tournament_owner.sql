-- Existing rows have no owner to backfill; only dev data exists at this point
delete from tournament;

alter table tournament add column owner_id bigint not null references users (id);

create index tournament_owner_id_idx on tournament (owner_id);
