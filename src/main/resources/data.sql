INSERT INTO usrs(`name`) VALUES
                             ('Roman'),
                             ('Alex');
INSERT INTO bank_account(user_id, `name`, balance) VALUES
                                                       (1, 'Kaspi', 20666.52),
                                                       (1, 'Jusan', 5668.13),
                                                       (2, 'Kaspi', 17102.4);
INSERT INTO cost_category(user_id, `name`, category_type) VALUES
                                                              (1, 'Sport', 'EXPENSE'),
                                                              (1, 'Shopping', 'EXPENSE'),
                                                              (1, 'Transport', 'EXPENSE'),
                                                              (1, 'Work', 'INCOME'),
                                                              (2, 'Shop', 'EXPENSE'),
                                                              (2, 'Stipend', 'INCOME');
INSERT INTO operation(amount, `date`, bank_account_id, cost_category_id) VALUES
                                                                             (1330.7, '2024-10-13', 1, 2),
                                                                             (6000, '2024-10-13', 1, 1),
                                                                             (2861.52, '2024-10-03', 1, 2),
                                                                             (9831.07, '2024-10-01', 1, 4),
                                                                             (1330, '2024-10-19', 3, 6),
                                                                             (6000, '2024-10-19', 3, 5);