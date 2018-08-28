insert into room (id, name, room_type_code)
values ('30bcf571-45ca-4528-9d05-ce51b5e3fcde', 'Room A', 'court_room');

insert into room (id, name, room_type_code)
values ('3e699f29-6ea9-46bf-a338-00622fe0ae1b', 'Room B', 'court_room');


insert into person (id, person_type, name)
values ('b5bc80ec-8306-4f0f-8c6e-af218bb116c2', 'JUDGE', 'John Harris');

insert into person (id, person_type, name)
values ('8d65caf8-1337-4938-8f98-5f6e37e0787a', 'JUDGE', 'Amy Wessome');


insert into session (id, person_id, room_id, start, duration, case_type)
values ('27ba6290-5068-4372-b831-8c01b06bbc34', 'b5bc80ec-8306-4f0f-8c6e-af218bb116c2',
'30bcf571-45ca-4528-9d05-ce51b5e3fcde', '2018-04-24 14:30:00', 6000, null);

insert into session (id, person_id, room_id, start, duration, case_type)
values ('947e7065-5867-40a8-88de-0d5a787cc301', 'b5bc80ec-8306-4f0f-8c6e-af218bb116c2',
'30bcf571-45ca-4528-9d05-ce51b5e3fcde', '2018-04-24 12:30:00', 6000, 'FTRACK');

insert into session (id, person_id, room_id, start, duration, case_type)
values ('7fd158b2-1025-49ca-bc82-09f4c60e2672', 'b5bc80ec-8306-4f0f-8c6e-af218bb116c2',
'30bcf571-45ca-4528-9d05-ce51b5e3fcde', '2018-04-25 11:30:00', 6000, 'SCLAIMS');

insert into session (id, person_id, room_id, start, duration, case_type)
values ('22299479-90e6-4fdf-8232-456056899f12', null,
'3e699f29-6ea9-46bf-a338-00622fe0ae1b', '2018-04-26 11:30:00', 6000, 'MTRACK');

insert into session (id, person_id, room_id, start, duration, case_type)
values ('817f50ea-31e2-4d02-bf13-9ffd866b7667', null,
null, '2018-04-26 11:30:00', 6000, 'FTRACK');

insert into session (id, person_id, room_id, start, duration, case_type)
values ('26571383-0a2f-434f-9971-b230f9364748' ,'8d65caf8-1337-4938-8f98-5f6e37e0787a',
null, '2018-04-26 11:30:00', 6000, 'SCLAIMS');
