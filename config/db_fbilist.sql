CREATE TABLE IF NOT EXISTS "tbl_wanted_suspects"
(
    id bigserial,
    refno text,
    details jsonb DEFAULT '{}'::jsonb,
    status text,
    created timestamp with time zone default now(),
    CONSTRAINT tbl_wanted_suspects_pkey PRIMARY KEY (id)
    )
    WITH (
        OIDS=FALSE
        );
ALTER TABLE "tbl_wanted_suspects"
    OWNER TO postgres;