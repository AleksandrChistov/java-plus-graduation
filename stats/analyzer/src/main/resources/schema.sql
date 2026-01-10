CREATE TABLE IF NOT EXISTS interactions (
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    rating DOUBLE PRECISION NOT NULL,
    action_ts TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (user_id, event_id)
);

CREATE TABLE IF NOT EXISTS similarities (
    event1 BIGINT NOT NULL,
    event2 BIGINT NOT NULL,
    similarity DOUBLE PRECISION NOT NULL,
    action_ts TIMESTAMP WITH TIME ZONE NOT NULL,
    CHECK (event1 < event2),
    PRIMARY KEY (event1, event2)
);

DROP INDEX IF EXISTS interactions_user_id_idx;
DROP INDEX IF EXISTS interactions_event_id_idx;
DROP INDEX IF EXISTS similarities_event1_idx;
DROP INDEX IF EXISTS similarities_event2_idx;

CREATE INDEX IF NOT EXISTS interactions_user_id_idx ON interactions(user_id);
CREATE INDEX IF NOT EXISTS interactions_event_id_idx ON interactions(event_id);
CREATE INDEX IF NOT EXISTS similarities_event1_idx ON similarities(event1);
CREATE INDEX IF NOT EXISTS similarities_event2_idx ON similarities(event2);
