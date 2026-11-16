-- Users the creator added to manage the tournament with them; tournament.owner_id stays the creator
create table tournament_co_owner (
    tournament_id bigint not null references tournament (id) on delete cascade,
    user_id       bigint not null references users (id) on delete cascade,
    primary key (tournament_id, user_id)
);

-- Serves the tournament list, which looks up the tournaments a viewer co-owns
create index tournament_co_owner_user_id_idx on tournament_co_owner (user_id);
