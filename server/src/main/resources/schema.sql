CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(255) NOT NULL,
    email varchar(512) NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    description varchar(255) NOT NULL,
    requester_id BIGINT NOT NULL REFERENCES users(id),
    created TIMESTAMP NOT NULL
);
CREATE TABLE IF NOT EXISTS items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(255) NOT NULL,
    description varchar(255) NOT NULL,
    available boolean NOT NULL,
    owner_id BIGINT NOT NULL REFERENCES users(id),
    request_id BIGINT REFERENCES requests(id)
);
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booker_id BIGINT NOT NULL REFERENCES users(id),
    item_id BIGINT NOT NULL REFERENCES items(id),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status varchar(50) NOT NULL
);
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES items(id),
    author_id BIGINT NOT NULL REFERENCES users(id),
    text varchar(255) NOT NULL,
    created TIMESTAMP NOT NULL
);
