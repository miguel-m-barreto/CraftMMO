create table players (
    player_uuid uuid primary key,
    last_known_name varchar(16) not null,
    ruleset_version varchar(64) not null,
    profile_version bigint not null default 0,
    lock_version bigint not null default 0,
    last_login_at timestamptz not null,
    playtime_seconds bigint not null default 0,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_players_last_known_name_not_blank check (length(trim(last_known_name)) > 0),
    constraint chk_players_ruleset_version_not_blank check (length(trim(ruleset_version)) > 0),
    constraint chk_players_profile_version_non_negative check (profile_version >= 0),
    constraint chk_players_lock_version_non_negative check (lock_version >= 0),
    constraint chk_players_playtime_seconds_non_negative check (playtime_seconds >= 0)
);

create table player_skill_progress (
    player_uuid uuid not null references players(player_uuid) on delete cascade,
    skill_id varchar(64) not null,
    level integer not null check (level >= 0),
    current_xp bigint not null check (current_xp >= 0),
    total_xp bigint not null check (total_xp >= 0),
    version bigint not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_player_skill_progress_skill_id_not_blank check (length(trim(skill_id)) > 0),
    constraint chk_player_skill_progress_total_xp_gte_current_xp check (total_xp >= current_xp),
    constraint chk_player_skill_progress_version_non_negative check (version >= 0),
    primary key (player_uuid, skill_id)
);

create table player_cooldowns (
    player_uuid uuid not null references players(player_uuid) on delete cascade,
    namespace varchar(64) not null,
    cooldown_key varchar(128) not null,
    starts_at timestamptz not null,
    ends_at timestamptz not null,
    ruleset_version varchar(64) not null,
    content_version varchar(64) not null,
    version bigint not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_player_cooldowns_namespace_not_blank check (length(trim(namespace)) > 0),
    constraint chk_player_cooldowns_key_not_blank check (length(trim(cooldown_key)) > 0),
    constraint chk_player_cooldowns_ruleset_version_not_blank check (length(trim(ruleset_version)) > 0),
    constraint chk_player_cooldowns_content_version_not_blank check (length(trim(content_version)) > 0),
    constraint chk_player_cooldowns_end_after_start check (ends_at > starts_at),
    constraint chk_player_cooldowns_version_non_negative check (version >= 0),
    primary key (player_uuid, namespace, cooldown_key)
);

create table session_ownership (
    player_uuid uuid primary key references players(player_uuid) on delete cascade,
    server_id varchar(128) not null,
    lease_id uuid not null,
    expires_at timestamptz not null,
    version bigint not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_session_ownership_server_id_not_blank check (length(trim(server_id)) > 0),
    constraint chk_session_ownership_version_non_negative check (version >= 0)
);

create index idx_session_ownership_expires_at on session_ownership(expires_at);

create table progression_operations (
    operation_id varchar(128) primary key,
    player_uuid uuid not null references players(player_uuid) on delete cascade,
    skill_id varchar(64) not null,
    operation_type varchar(64) not null,
    source varchar(128) not null,
    payload_hash char(64) not null,
    xp bigint not null check (xp >= 0),
    status varchar(32) not null,
    result_level integer,
    result_current_xp bigint,
    result_total_xp bigint,
    result_version bigint,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    applied_at timestamptz,
    constraint chk_progression_operations_operation_id_not_blank check (length(trim(operation_id)) > 0),
    constraint chk_progression_operations_skill_id_not_blank check (length(trim(skill_id)) > 0),
    constraint chk_progression_operations_type_not_blank check (length(trim(operation_type)) > 0),
    constraint chk_progression_operations_source_not_blank check (length(trim(source)) > 0),
    constraint chk_progression_operations_payload_hash_sha256 check (payload_hash ~ '^[0-9a-f]{64}$'),
    constraint chk_progression_operations_status check (status in ('STARTED', 'APPLIED', 'FAILED')),
    constraint chk_progression_operations_result_xp check (
        (
            result_level is null
            and result_current_xp is null
            and result_total_xp is null
            and result_version is null
            and applied_at is null
        )
        or (
            result_level is not null
            and result_current_xp is not null
            and result_total_xp is not null
            and result_version is not null
            and applied_at is not null
            and result_level >= 0
            and result_current_xp >= 0
            and result_total_xp >= result_current_xp
            and result_version >= 0
        )
    )
);

create index idx_progression_operations_player_uuid on progression_operations(player_uuid);

create table admin_audit_logs (
    audit_id bigserial primary key,
    actor varchar(128) not null,
    action varchar(128) not null,
    target_player_uuid uuid,
    operation_id varchar(128),
    sanitized_details text not null,
    created_at timestamptz not null,
    constraint chk_admin_audit_logs_actor_not_blank check (length(trim(actor)) > 0),
    constraint chk_admin_audit_logs_action_not_blank check (length(trim(action)) > 0)
);
