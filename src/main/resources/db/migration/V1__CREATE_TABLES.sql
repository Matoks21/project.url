
 CREATE TABLE roles (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY,
    name VARCHAR(20) UNIQUE CHECK (name IN ('ROLE_USER', 'ROLE_ADMIN')),
    PRIMARY KEY (id)
);

CREATE TABLE users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);


  CREATE TABLE user_roles (
    role_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE short_urls (
    id BIGSERIAL PRIMARY KEY,
    original_url TEXT NOT NULL,
    short_url VARCHAR(8) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_date TIMESTAMP,
    visit_count INT NOT NULL DEFAULT 0,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE
);


