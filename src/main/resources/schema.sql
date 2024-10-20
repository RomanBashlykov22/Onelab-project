CREATE TABLE usrs(
                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                     `name` VARCHAR(256)
);

CREATE TABLE bank_account(
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             user_id BIGINT,
                             `name` VARCHAR(256),
                             balance DECIMAL(19, 4),
                             FOREIGN KEY(user_id) REFERENCES usrs(id)
);

CREATE TABLE cost_category(
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              user_id BIGINT,
                              `name` VARCHAR(256),
                              category_type VARCHAR(15),
                              FOREIGN KEY(user_id) REFERENCES usrs(id)
);

CREATE TABLE operation(
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          amount DECIMAL(19, 4),
                          `date` DATE,
                          bank_account_id BIGINT,
                          cost_category_id BIGINT,
                          FOREIGN KEY(bank_account_id) REFERENCES bank_account(id),
                          FOREIGN KEY(cost_category_id) REFERENCES cost_category(id)
);