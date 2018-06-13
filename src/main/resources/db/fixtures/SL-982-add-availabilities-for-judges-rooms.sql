CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$
DECLARE startDateTime timestamp with time zone = '2018-03-01 07:00:00+00';
BEGIN
  FOR counter IN 1..365 LOOP
  	IF (extract(dow from startDateTime) NOT IN (0,6)) THEN
	    INSERT INTO availability (id, start, duration, person_id, room_id ) VALUES
	    ((SELECT uuid_generate_v4()), startDateTime, 28800, '8d65caf8-1337-4938-8f98-5f6e37e0787a', null),
	    ((SELECT uuid_generate_v4()), startDateTime, 28800, 'b5bc80ec-8306-4f0f-8c6e-af218bb116c2', null),
	    ((SELECT uuid_generate_v4()), startDateTime, 28800, null, '30bcf571-45ca-4528-9d05-ce51b5e3fcde'),
	    ((SELECT uuid_generate_v4()), startDateTime, 28800, null, '3e699f29-6ea9-46bf-a338-00622fe0ae1b');
	END IF;
    startDateTime = startDateTime + interval '1' day;
  END LOOP;
END; $$
