-- session type, hearing type, case type, room type

INSERT INTO session_type (description, code) VALUES ('Small Claims','small-claims');
INSERT INTO session_type (description, code) VALUES ('Fast Track','fast-track');
INSERT INTO session_type (description, code) VALUES ('Fast Track - Trial Only','fast-track---trial-only');
INSERT INTO session_type (description, code) VALUES ('Multi Track','multi-track');
INSERT INTO session_type (description, code) VALUES ('Multi Track - Trial Only','multi-track---trial-only');
INSERT INTO session_type (description, code) VALUES ('Preliminary Hearings (Small Claims / Fast Track / Multi Track)','preliminary-hearings--small-claims---fast-track---multi-track-');
INSERT INTO case_type (description, code) VALUES ('Small Claims','small-claims');
INSERT INTO case_type (description, code) VALUES ('Fast Track','fast-track');
INSERT INTO case_type (description, code) VALUES ('Multi Track','multi-track');
INSERT INTO hearing_type (description, code) VALUES ('Preliminary Hearing','preliminary-hearing');
INSERT INTO hearing_type (description, code) VALUES ('Trial','trial');
INSERT INTO hearing_type (description, code) VALUES ('Adjourned Hearing','adjourned-hearing');

INSERT INTO room_type (description, code) VALUES ('Court Room','court-room');
INSERT INTO room_type (description, code) VALUES ('Chambers','chambers');
INSERT INTO room_type (description, code) VALUES ('Witness Booth','witness-booth');
INSERT INTO room_type (description, code) VALUES ('Conference Room','conference-room');

insert into session_type(code, description)
values ('K-Fast Track and Applications', 'K-Fast Track and Applications');

insert into case_type(code, description)
values('K-Fast Track', 'K-Fast Track');
insert into case_type(code, description)
values('K-Small Claims', 'K-Small Claims');

insert into hearing_type(code, description)
values('K-Application', 'K-Application');
insert into hearing_type(code, description)
values('K-ASAJ', 'K-ASAJ');
insert into hearing_type(code, description)
values('K-Someting else', 'K-Something else');

insert into hearing_type_case_type(hearing_type_code, case_type_code)
values('K-Application', 'K-Fast Track');
insert into hearing_type_case_type(hearing_type_code, case_type_code)
values('K-ASAJ', 'K-Fast Track');
insert into hearing_type_case_type(hearing_type_code, case_type_code)
values('K-Someting else', 'K-Small Claims');
insert into hearing_type_case_type(hearing_type_code, case_type_code)
values('K-ASAJ', 'K-Small Claims');

insert into case_type_session_type(case_type_code, session_type_code)
values('K-Fast Track', 'K-Fast Track and Applications');

insert into hearing_type_session_type(hearing_type_code, session_type_code)
values ('K-Application', 'K-Fast Track and Applications');

INSERT INTO case_type_session_type (case_type_code, session_type_code) values ((Select code from case_type where description = 'Small Claims'), (Select code from session_type where description = 'Small Claims'));
INSERT INTO case_type_session_type (case_type_code, session_type_code) values ((Select code from case_type where description = 'Fast Track'), (Select code from session_type where description = 'Fast Track'));
INSERT INTO case_type_session_type (case_type_code, session_type_code) values ((Select code from case_type where description = 'Fast Track'), (Select code from session_type where description = 'Fast Track - Trial Only'));
INSERT INTO hearing_type_session_type (session_type_code, hearing_type_code) values ((Select code from session_type where description = 'Fast Track - Trial Only'), (Select code from hearing_type where description = 'Trial'));
INSERT INTO case_type_session_type (case_type_code, session_type_code) values ((Select code from case_type where description = 'Multi Track'), (Select code from session_type where description = 'Multi Track'));
INSERT INTO case_type_session_type (case_type_code, session_type_code) values ((Select code from case_type where description = 'Multi Track'), (Select code from session_type where description = 'Multi Track - Trial Only'));
INSERT INTO hearing_type_session_type (session_type_code, hearing_type_code) values ((Select code from session_type where description = 'Multi Track - Trial Only'), (Select code from hearing_type where description = 'Trial'));
INSERT INTO hearing_type_session_type (session_type_code, hearing_type_code) values ((Select code from session_type where description = 'Preliminary Hearings (Small Claims / Fast Track / Multi Track)'), (Select code from hearing_type where description = 'Preliminary Hearing'));
INSERT INTO hearing_type_case_type (case_type_code, hearing_type_code) values ((Select code from case_type where description = 'Small Claims'), (Select code from hearing_type where description = 'Preliminary Hearing'));
INSERT INTO hearing_type_case_type (case_type_code, hearing_type_code) values ((Select code from case_type where description = 'Small Claims'), (Select code from hearing_type where description = 'Trial'));
INSERT INTO hearing_type_case_type (case_type_code, hearing_type_code) values ((Select code from case_type where description = 'Fast Track'), (Select code from hearing_type where description = 'Trial'));
INSERT INTO hearing_type_case_type (case_type_code, hearing_type_code) values ((Select code from case_type where description = 'Fast Track'), (Select code from hearing_type where description = 'Adjourned Hearing'));

-- rooms
INSERT INTO room (id, name, room_type_code) VALUES ('bdba871e-17ab-44b0-9390-98a86bde83b9', 'Court 1', (Select code from room_type where description = 'Court Room'));
INSERT INTO room (id, name, room_type_code) VALUES ('4c611d21-2ce2-4e59-8a80-3b54863976e7', 'Court 2', (Select code from room_type where description = 'Court Room'));
INSERT INTO room (id, name, room_type_code) VALUES ('d26e77ed-80b4-4d83-a529-bc5ab41641af', 'Court 3', (Select code from room_type where description = 'Court Room'));
INSERT INTO room (id, name, room_type_code) VALUES ('15c5c715-8435-4c69-85fa-0e38e7e75173', 'Court 4', (Select code from room_type where description = 'Court Room'));
INSERT INTO room (id, name, room_type_code) VALUES ('0487f327-876c-43f9-bf9d-66ad80739794', 'Chambers (Judge James)', (Select code from room_type where description = 'Chambers'));
INSERT INTO room (id, name, room_type_code) VALUES ('ff10f215-be1e-4434-bb08-a86c9e9ec944', 'Chambers (Judge Nancy)', (Select code from room_type where description = 'Chambers'));
INSERT INTO room (id, name, room_type_code) VALUES ('eda0ac18-8411-44b6-b6ba-308c02253764', 'Court 1 - Witness Booth', (Select code from room_type where description = 'Witness Booth'));
INSERT INTO room (id, name, room_type_code) VALUES ('3ecb4f76-12ae-4544-a95b-3e391454a7de', 'Court 2 - Witness Booth', (Select code from room_type where description = 'Witness Booth'));
INSERT INTO room (id, name, room_type_code) VALUES ('3331ff54-836a-471f-ad3f-31162557a365', 'Conference Room 1', (Select code from room_type where description = 'Conference Room'));
INSERT INTO room (id, name, room_type_code) VALUES ('63905007-2299-4264-b20f-111c0809bc64', 'Conference Room 2', (Select code from room_type where description = 'Conference Room'));
INSERT INTO room (id, name, room_type_code) VALUES ('543a9c2c-ce6a-4109-a4f7-caa2a80d0ebc', 'Conference Room 3', (Select code from room_type where description = 'Conference Room'));

insert into room(id, name, room_type_code) values('30bcf571-45ca-4528-9d05-ce51b5e3fcde', 'Room A', 'court-room');

insert into room(id, name, room_type_code) values('3e699f29-6ea9-46bf-a338-00622fe0ae1b', 'Room B', 'court-room');

insert into room(id, name, room_type_code) values('fb878f53-a2dc-4cfc-aac1-ca1a1c697a8e', 'Court 5', 'court-room');
insert into room(id, name, room_type_code) values('c8c5554c-cd8d-42ca-8503-7eae334e255e', 'Court 6', 'court-room');
insert into room(id, name, room_type_code) values('b08d0570-1759-412d-8141-d8e1fd46de30', 'Court 7', 'court-room');

-- judges
INSERT INTO person (id, person_type, name, username) VALUES ('1143b1ea-1813-4acc-8b08-f37d1db59492', 'JUDGE', 'Judge Linda', 'judgelinda');
INSERT INTO person (id, person_type, name, username) VALUES ('651de386-a786-46db-be43-8c03e3ba7a52', 'JUDGE', 'Judge Barbara', 'judgebarbara');
INSERT INTO person (id, person_type, name, username) VALUES ('ce9f377d-10c9-478c-838a-574612579db7', 'JUDGE', 'Judge Nancy', 'judgenancy');
INSERT INTO person (id, person_type, name, username) VALUES ('e747c174-895e-48c4-9182-4f4cc4897a75', 'JUDGE', 'Judge James', 'judgejames');
INSERT INTO person (id, person_type, name, username) VALUES ('45c0ebb1-d81f-4bbc-ae8e-9e8c5f389640', 'JUDGE', 'Judge John', 'judgejohn');
INSERT INTO person (id, person_type, name, username) VALUES ('118e02d6-74a8-49da-b2f9-11fd357dd559', 'JUDGE', 'Judge David', 'judgedavid');

insert into person(id, person_type, name) values('b5bc80ec-8306-4f0f-8c6e-af218bb116c2', 'JUDGE', 'John Harris');
insert into person(id, person_type, name) values('8d65caf8-1337-4938-8f98-5f6e37e0787a', 'JUDGE', 'Amy Wessome');

insert into person(id, person_type, name, username) values('6ce3aa58-6986-4ed7-8c61-94fa2af73afb', 'JUDGE', 'DJ Cope', 'djcope');
insert into person(id, person_type, name, username) values('a537cc0e-c275-479d-a744-44c782ec7d04', 'JUDGE', 'DJ Cronin', 'djcronin');
insert into person(id, person_type, name, username) values('d5effa95-877f-4eaf-a029-8aaa2d088289', 'JUDGE', 'DJ Howell', 'djhowell');
insert into person(id, person_type, name, username) values('0010fa96-22e6-4d56-98da-29adb125b246', 'JUDGE', 'DJ Michaels', 'djmichaels');
insert into person(id, person_type, name, username) values('d85450a9-4925-40d1-b6ae-918e0f8741a7', 'JUDGE', 'DJ Smith', 'djsmith');
insert into person(id, person_type, name, username) values('a70f3975-379c-431c-b999-d96cba7d2b4a', 'JUDGE', 'DJ Watson', 'djwatson');


-- sessions
insert into session (id, person_id, room_id, start, duration, session_type_code)
values ('27ba6290-5068-4372-b831-8c01b06bbc34', 'b5bc80ec-8306-4f0f-8c6e-af218bb116c2',
'30bcf571-45ca-4528-9d05-ce51b5e3fcde', '2018-04-24 14:30:00', 6000, 'fast-track');

insert into session (id, person_id, room_id, start, duration, session_type_code)
values ('947e7065-5867-40a8-88de-0d5a787cc301', 'b5bc80ec-8306-4f0f-8c6e-af218bb116c2',
'30bcf571-45ca-4528-9d05-ce51b5e3fcde', '2018-04-24 12:30:00', 6000, 'fast-track');

insert into session (id, person_id, room_id, start, duration, session_type_code)
values ('7fd158b2-1025-49ca-bc82-09f4c60e2672', 'b5bc80ec-8306-4f0f-8c6e-af218bb116c2',
'30bcf571-45ca-4528-9d05-ce51b5e3fcde', '2018-04-25 11:30:00', 6000, 'fast-track');

insert into session (id, person_id, room_id, start, duration, session_type_code)
values ('22299479-90e6-4fdf-8232-456056899f12', null,
'3e699f29-6ea9-46bf-a338-00622fe0ae1b', '2018-04-26 11:30:00', 6000, 'fast-track');

insert into session (id, person_id, room_id, start, duration, session_type_code)
values ('817f50ea-31e2-4d02-bf13-9ffd866b7667', null,
null, '2018-04-26 11:30:00', 6000, 'fast-track');

insert into session (id, person_id, room_id, start, duration, session_type_code)
values ('26571383-0a2f-434f-9971-b230f9364748' ,'8d65caf8-1337-4938-8f98-5f6e37e0787a',
null, '2018-04-26 11:30:00', 6000, 'fast-track');

-- hearings
INSERT INTO hearing_part (id, case_number, case_title, case_type, hearing_type, duration, schedule_start, schedule_end, session_id, start, created_at, created_by)
VALUES ('b262fc78-dc98-484b-849b-7b96d7753918','1234','case title 1','FTRACK','hearing type',3600,'2018-05-02','2018-06-02',NULL,NULL, '2018-03-01 7:11:11', 'joe_script');

INSERT INTO hearing_part (id, case_number, case_title, case_type, hearing_type, duration, schedule_start, schedule_end, session_id, start, created_at, created_by)
VALUES ('13d4e4da-e817-4e7d-8e29-ed467c5d5d2f','abcde','case title 2','SCLAIMS','hearing type',3600,'2018-05-02','2018-06-02',NULL,NULL, '2018-03-01 7:11:11', 'joe_script');

INSERT INTO hearing_part (id, case_number, case_title, case_type, hearing_type, duration, schedule_start, schedule_end, session_id, start, created_at, created_by)
VALUES ('dd77dbe1-7527-4dc7-b18a-6898a4a92b7d','9999','case title 3','MTRACK','hearing type 1',1800,'2018-05-02','2018-06-02',NULL,NULL, '2018-03-01 7:11:11', 'joe_script');

INSERT INTO hearing_part (id, case_number, case_title, case_type, hearing_type, duration, schedule_start, schedule_end, session_id, start, created_at, created_by)
VALUES ('cf3a7605-5d27-45ef-a1a3-6424fe0a7376','7777','case title 4','FTRACK','hearing type 2',900,'2018-03-02','2018-06-02',
'7fd158b2-1025-49ca-bc82-09f4c60e2672', '2018-04-25 11:30:00', '2018-03-01 7:11:11', 'joe_script');




