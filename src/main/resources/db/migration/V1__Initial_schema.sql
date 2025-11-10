-- V1__Initial_schema.sql
-- Meet Application Database Schema

-- Table: tags
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    text VARCHAR(50) UNIQUE NOT NULL,
    state VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT chk_tag_state CHECK (state IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_tags_state ON tags(state);

-- Table: users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    image_url TEXT,
    city VARCHAR(100) DEFAULT '',
    description TEXT DEFAULT '',
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT chk_phone_format CHECK (phone_number ~ '^\+7[0-9]{10}$')
);

CREATE INDEX idx_users_phone ON users(phone_number);
CREATE INDEX idx_users_email ON users(email) WHERE email IS NOT NULL;

-- Table: user_interests
CREATE TABLE user_interests (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    PRIMARY KEY (user_id, tag_id)
);

CREATE INDEX idx_user_interests_user ON user_interests(user_id);
CREATE INDEX idx_user_interests_tag ON user_interests(tag_id);

-- Table: user_social_media
CREATE TABLE user_social_media (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    platform VARCHAR(50) NOT NULL,
    username VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT chk_platform CHECK (platform IN ('TELEGRAM', 'HABR')),
    UNIQUE(user_id, platform)
);

CREATE INDEX idx_user_social_media_user ON user_social_media(user_id);

-- Table: communities
CREATE TABLE communities (
    id BIGSERIAL PRIMARY KEY,
    image_url TEXT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_communities_title ON communities(title);

-- Table: community_tags
CREATE TABLE community_tags (
    community_id BIGINT NOT NULL REFERENCES communities(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    
    PRIMARY KEY (community_id, tag_id)
);

CREATE INDEX idx_community_tags_community ON community_tags(community_id);
CREATE INDEX idx_community_tags_tag ON community_tags(tag_id);

-- Table: community_subscribers
CREATE TABLE community_subscribers (
    community_id BIGINT NOT NULL REFERENCES communities(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subscribed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    PRIMARY KEY (community_id, user_id)
);

CREATE INDEX idx_community_subscribers_community ON community_subscribers(community_id);
CREATE INDEX idx_community_subscribers_user ON community_subscribers(user_id);

-- Table: meetings
CREATE TABLE meetings (
    id BIGSERIAL PRIMARY KEY,
    image_url TEXT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    time BIGINT NOT NULL,
    date VARCHAR(50) NOT NULL,
    address TEXT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    capacity INT NOT NULL DEFAULT 100,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    person_host_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    community_host_id BIGINT REFERENCES communities(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT chk_meeting_status CHECK (status IN ('ACTIVE', 'CANCELLED', 'FINISHED')),
    CONSTRAINT chk_capacity CHECK (capacity > 0),
    CONSTRAINT chk_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT chk_longitude CHECK (longitude BETWEEN -180 AND 180)
);

CREATE INDEX idx_meetings_status ON meetings(status);
CREATE INDEX idx_meetings_time ON meetings(time);
CREATE INDEX idx_meetings_person_host ON meetings(person_host_id) WHERE person_host_id IS NOT NULL;
CREATE INDEX idx_meetings_community_host ON meetings(community_host_id) WHERE community_host_id IS NOT NULL;

-- Table: meeting_tags
CREATE TABLE meeting_tags (
    meeting_id BIGINT NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    
    PRIMARY KEY (meeting_id, tag_id)
);

CREATE INDEX idx_meeting_tags_meeting ON meeting_tags(meeting_id);
CREATE INDEX idx_meeting_tags_tag ON meeting_tags(tag_id);

-- Table: meeting_participants
CREATE TABLE meeting_participants (
    meeting_id BIGINT NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    PRIMARY KEY (meeting_id, user_id)
);

CREATE INDEX idx_meeting_participants_meeting ON meeting_participants(meeting_id);
CREATE INDEX idx_meeting_participants_user ON meeting_participants(user_id);

-- Table: sms_codes
CREATE TABLE sms_codes (
    phone_number VARCHAR(20) PRIMARY KEY,
    code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempts INT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT chk_sms_phone_format CHECK (phone_number ~ '^\+7[0-9]{10}$'),
    CONSTRAINT chk_sms_code_format CHECK (code ~ '^[0-9]{6}$'),
    CONSTRAINT chk_attempts CHECK (attempts >= 0 AND attempts <= 5)
);

CREATE INDEX idx_sms_codes_expires_at ON sms_codes(expires_at);

-- Initial seed data: tags
INSERT INTO tags (text, state) VALUES
    ('Android', 'ACTIVE'),
    ('Kotlin', 'ACTIVE'),
    ('Compose', 'ACTIVE'),
    ('Backend', 'ACTIVE'),
    ('iOS', 'ACTIVE'),
    ('UI/UX', 'ACTIVE'),
    ('DevOps', 'ACTIVE'),
    ('Data Science', 'ACTIVE'),
    ('Flutter', 'ACTIVE'),
    ('React Native', 'ACTIVE'),
    ('JavaScript', 'ACTIVE'),
    ('Python', 'ACTIVE')
ON CONFLICT (text) DO NOTHING;
