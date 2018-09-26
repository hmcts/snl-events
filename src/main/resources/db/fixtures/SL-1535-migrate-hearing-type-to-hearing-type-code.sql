-- Added into hearing_type table hearing_types that were used previously as string
INSERT INTO hearing_type (description, code) (SELECT DISTINCT hearing_type, hearing_type FROM hearing_part) ON CONFLICT DO NOTHING;

-- Copy value of hearing type to hearing type code
UPDATE hearing_part SET hearing_type_code = hearing_type;

-- Set default hearing type code for these that has hearing type == NULL
UPDATE hearing_part SET hearing_type_code = (SELECT hearing_type FROM hearing_part GROUP BY hearing_type ORDER BY COUNT(*) DESC LIMIT 1) WHERE hearing_type_code = NULL;

-- Copy value of hearing type audit to new column
UPDATE hearing_part_aud SET hearing_type_code = hearing_type;
