CREATE TABLE roles (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE TABLE projects (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          project_type VARCHAR(50) NOT NULL,
                          topic VARCHAR(255) NOT NULL,
                          client_name VARCHAR(100) NOT NULL,
                          total_price DECIMAL(10, 2) NOT NULL,
                          advance_payment DECIMAL(10, 2),
                          deadline DATE NOT NULL,
                          user_id BIGINT NOT NULL,
                          status ENUM('В розробці', 'Завершено', 'Скасовано') DEFAULT 'В розробці',
                          FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);