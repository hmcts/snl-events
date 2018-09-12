-- availablity
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$
DECLARE startDateTimeMarch timestamp with time zone = '2018-03-01 07:00:00+00';
DECLARE startDateTime timestamp with time zone = '2018-09-03 07:00:00+00';
BEGIN
 FOR counter IN 1..365 LOOP
  IF (extract(dow from startDateTimeMarch) NOT IN (0,6)) THEN
    INSERT INTO availability (id, start, duration, person_id, room_id ) VALUES
    ((SELECT uuid_generate_v4()), startDateTime, 28800, '8d65caf8-1337-4938-8f98-5f6e37e0787a', null),
    ((SELECT uuid_generate_v4()), startDateTime, 28800, 'b5bc80ec-8306-4f0f-8c6e-af218bb116c2', null),
    ((SELECT uuid_generate_v4()), startDateTime, 28800, null, '30bcf571-45ca-4528-9d05-ce51b5e3fcde'),
    ((SELECT uuid_generate_v4()), startDateTime, 28800, null, '3e699f29-6ea9-46bf-a338-00622fe0ae1b');
END IF;
   startDateTime = startDateTime + interval '1' day;
 END LOOP;

 FOR counter IN 1..365 LOOP
IF (extract(dow from startDateTime) NOT IN (0,6)) THEN
  INSERT INTO availability (id, start, duration, person_id, room_id ) VALUES
  ((SELECT uuid_generate_v4()), startDateTime, 28800, '1143b1ea-1813-4acc-8b08-f37d1db59492', null),
  ((SELECT uuid_generate_v4()), startDateTime, 28800, '651de386-a786-46db-be43-8c03e3ba7a52', null),
  ((SELECT uuid_generate_v4()), startDateTime, 28800, 'ce9f377d-10c9-478c-838a-574612579db7', null),
  ((SELECT uuid_generate_v4()), startDateTime, 28800, 'e747c174-895e-48c4-9182-4f4cc4897a75', null),
  ((SELECT uuid_generate_v4()), startDateTime, 28800, '45c0ebb1-d81f-4bbc-ae8e-9e8c5f389640', null),
  ((SELECT uuid_generate_v4()), startDateTime, 28800, '118e02d6-74a8-49da-b2f9-11fd357dd559', null),
  ((SELECT uuid_generate_v4()), startDateTime, 28800, null, 'bdba871e-17ab-44b0-9390-98a86bde83b9'),
  ((SELECT uuid_generate_v4()), startDateTime, 28800, null, '4c611d21-2ce2-4e59-8a80-3b54863976e7'),
  ((SELECT uuid_generate_v4()), startDateTime, 28800, null, 'd26e77ed-80b4-4d83-a529-bc5ab41641af'),
  ((SELECT uuid_generate_v4()), startDateTime, 28800, null, '15c5c715-8435-4c69-85fa-0e38e7e75173'),
  ((SELECT uuid_generate_v4()), startDateTime, 28800, null, '0487f327-876c-43f9-bf9d-66ad80739794'),
  ((SELECT uuid_generate_v4()), startDateTime, 28800, null, 'ff10f215-be1e-4434-bb08-a86c9e9ec944'),
  ((SELECT uuid_generate_v4()), startDateTime, 28800, null, 'eda0ac18-8411-44b6-b6ba-308c02253764'),
  ((SELECT uuid_generate_v4()), startDateTime, 28800, null, '3ecb4f76-12ae-4544-a95b-3e391454a7de'),
  ((SELECT uuid_generate_v4()), startDateTime, 28800, null, '3331ff54-836a-471f-ad3f-31162557a365'),
  ((SELECT uuid_generate_v4()), startDateTime, 28800, null, '63905007-2299-4264-b20f-111c0809bc64'),
  ((SELECT uuid_generate_v4()), startDateTime, 28800, null, '543a9c2c-ce6a-4109-a4f7-caa2a80d0ebc');
END IF;
startDateTime = startDateTime + interval '1' day;
END LOOP;
END; $$


