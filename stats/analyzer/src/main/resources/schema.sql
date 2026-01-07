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
    CHECK (event1 < event2)
    PRIMARY KEY (event1, event2)
);
