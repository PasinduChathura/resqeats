-- INSERT SUPER_ADMIN ROLE
insert IGNORE INTO trackable.roles VALUES ('1', '2023-12-23 20:24:56', 'system', '2023-12-23 20:24:56', 'system', 'Super Admin Role', '0', null, null, 'SuperAdmin', 'SUPER_ADMIN');
insert IGNORE INTO trackable.roles_seq VALUES (1);

-- INSERT PRIVILEGES
insert IGNORE INTO trackable.privilege VALUES ('1', '2023-12-23 20:24:56', 'system', '2023-12-23 20:24:56', 'system', null, '0', null, null, 'USER_READ_PRIVILEGE');
insert IGNORE INTO trackable.privilege VALUES ('2', '2023-12-23 20:24:56', 'system', '2023-12-23 20:24:56', 'system', null, '0', null, null, 'USER_WRITE_PRIVILEGE');
insert IGNORE INTO trackable.privilege VALUES ('3', '2023-12-23 20:24:56', 'system', '2023-12-23 20:24:56', 'system', null, '0', null, null, 'USER_DELETE_PRIVILEGE');
insert IGNORE INTO trackable.privilege VALUES ('4', '2023-12-23 20:24:56', 'system', '2023-12-23 20:24:56', 'system', null, '0', null, null, 'USER_UPDATE_PRIVILEGE');
insert IGNORE INTO trackable.privilege VALUES ('5', '2023-12-23 20:24:56', 'system', '2023-12-23 20:24:56', 'system', null, '0', null, null, 'USER_ADMINISTRATION_PRIVILEGE');
insert IGNORE INTO trackable.privilege VALUES ('6', '2023-12-23 20:24:56', 'system', '2023-12-23 20:24:56', 'system', null, '0', null, null, 'WORKFLOW_READ_PRIVILEGE');
insert IGNORE INTO trackable.privilege VALUES ('7', '2023-12-23 20:24:56', 'system', '2023-12-23 20:24:56', 'system', null, '0', null, null, 'WORKFLOW_WRITE_PRIVILEGE');
insert IGNORE INTO trackable.privilege VALUES ('8', '2023-12-23 20:24:56', 'system', '2023-12-23 20:24:56', 'system', null, '0', null, null, 'WORKFLOW_UPDATE_PRIVILEGE');
insert IGNORE INTO trackable.privilege VALUES ('9', '2023-12-23 20:24:56', 'system', '2023-12-23 20:24:56', 'system', null, '0', null, null, 'WORKFLOW_DELETE_PRIVILEGE');
insert IGNORE INTO trackable.privilege VALUES ('10', '2023-12-23 20:24:56', 'system', '2023-12-23 20:24:56', 'system', null, '0', null, null, 'WORKFLOW_ADMINISTRATION_PRIVILEGE');
insert IGNORE INTO trackable.privilege VALUES ('11', '2023-12-23 20:24:56', 'system', '2023-12-23 20:24:56', 'system', null, '0', null, null, 'ROLE_READ_PRIVILEGE');
insert IGNORE INTO trackable.privilege VALUES ('12', '2023-12-23 20:24:56', 'system', '2023-12-23 20:24:56', 'system', null, '0', null, null, 'ROLE_WRITE_PRIVILEGE');
insert IGNORE INTO trackable.privilege VALUES ('13', '2023-12-23 20:24:56', 'system', '2023-12-23 20:24:56', 'system', null, '0', null, null, 'ROLE_UPDATE_PRIVILEGE');
insert IGNORE INTO trackable.privilege VALUES ('14', '2023-12-23 20:24:56', 'system', '2023-12-23 20:24:56', 'system', null, '0', null, null, 'ROLE_DELETE_PRIVILEGE');
insert IGNORE INTO trackable.privilege VALUES ('15', '2023-12-23 20:24:56', 'system', '2023-12-23 20:24:56', 'system', null, '0', null, null, 'ROLE_ADMINISTRATION_PRIVILEGE');
insert IGNORE INTO trackable.privilege_seq VALUES (15);

-- INSERT ROLE PRIVILEGES
insert IGNORE INTO trackable.roles_privileges VALUES (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13),(1,14),(1,15);

-- INSERT SUPER_ADMIN USER
insert IGNORE INTO trackable.users VALUES ('1', '2023-12-25 21:38:46', 'system', '2023-12-25 21:40:05', 'system', null, 'chathura.pasindu@gmail.com', null, 'Super', 'Admin', '$2a$10$aufPbSN7Vb3RULnjIcFrmuBD64acNL8OrqK3Rrbh9jvFApYL7wsh2', null, 'ACTIVE', 'SUPER_ADMIN', 'SuperAdmin', '1');
insert IGNORE INTO trackable.users_seq VALUES (1);

insert IGNORE INTO trackable.workflow_sub_type VALUES ('1', '2023-12-25 21:38:46', 'system', '2023-12-25 21:38:46', 'system', NULL, '0', NULL, NULL, 'type 1', 'SERVICE_REQUEST');





