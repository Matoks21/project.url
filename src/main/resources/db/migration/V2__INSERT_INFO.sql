
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

INSERT INTO users (username, email, password) VALUES ('admin', 'admin@gmail.com', '$2a$10$xMKt25LVYwUhZJMPBzNkFO2lE85HQDGOTBpCcqR4Frk/0tSG820CK');
INSERT INTO users (username, email, password) VALUES ('user', 'user@gmail.com', '$2a$10$yY0AflO/Q3pfuEegCtHhLuATgwR5aUyLKCeiG1BE25jiy.k3gIu6u');


INSERT INTO user_roles (role_id, user_id)
VALUES ((SELECT id FROM roles WHERE name = 'ROLE_USER'), (SELECT id FROM users WHERE username = 'user')),
       ((SELECT id FROM roles WHERE name = 'ROLE_ADMIN'), (SELECT id FROM users WHERE username = 'admin'));
