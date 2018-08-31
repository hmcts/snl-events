/*
Script that create SQL INSERT statement base on passed Referenced Data xml

Execution:
npm install
node generate-sql ./path/to/file.xlsm

Example call:
node generate-sql ./Reference-Data-Initial-Load.xlsm
*/

const readXlsxFile = require('read-excel-file/node');
const uuidv4 = require('uuid/v4');
const moment = require('moment');

const refDataTables = ["session_type", "hearing_type", "case_type", "room_type"];
let roomNames = [];
let roomIds = [];
let judgeIds = [];
let roomIdOfRoomWithOutRoomType = [];

console.log(`
TRUNCATE table hearing_part CASCADE;
TRUNCATE table session CASCADE;
TRUNCATE table availability CASCADE;
TRUNCATE table person CASCADE;
TRUNCATE table room CASCADE;
TRUNCATE table problem_reference CASCADE;
TRUNCATE table problem CASCADE;
TRUNCATE table user_transaction_data CASCADE;
TRUNCATE table user_transaction CASCADE;
TRUNCATE table room_type CASCADE;
TRUNCATE table case_type_session_type CASCADE;
TRUNCATE table hearing_type_case_type CASCADE;
TRUNCATE table case_type CASCADE;
TRUNCATE table hearing_type_session_type CASCADE;
TRUNCATE table session_type CASCADE;
TRUNCATE table hearing_type CASCADE;
`);

const fileName = process.argv.splice(2)[0];

readXlsxFile(fileName, { sheet: 1 }).then((rows) => {
  rows.forEach(row => {
    const tableName = parseTableName(row[0]);
    const description = row[1];
    const code = createCodeFrom(description);

    if (refDataTables.indexOf(tableName) > -1) {
      console.log(createReferenceDataSQL(tableName, description, code));
    } else if (tableName === "judge") {
      console.log(createPersonSQL(description));
    } else if (tableName === "room") {
      // room require room_type_code that is set in second sheet
      roomNames.push(description);
    } else {
      console.error(`Error -> Table name: ${tableName} not supported`);
    }
  });
}).then(() => {
    readXlsxFile(fileName, { sheet: 2 }).then((rows) => {
    rows.forEach(row => {
      const associactionA = row[0];
      const tableNameA = extractTableNameFrom(associactionA);
      const descriptionOfAssociactionA = extractDescriptionFrom(associactionA);
      const associactionB = row[1];
      const tableNameB = extractTableNameFrom(associactionB);
      const descriptionOfAssociactionB = extractDescriptionFrom(associactionB);
      let sqlStatement;

      switch (resolveTableName(tableNameA, tableNameB)) {
        case "case_type_session_type":
          sqlStatement = generateCaseTypeSessionTypeJoinTableSQL(tableNameA, descriptionOfAssociactionA, tableNameB, descriptionOfAssociactionB);
          break;
        case "hearing_type_session_type":
          sqlStatement = generateSessionTypeHearingTypeJoinTableSQL(tableNameA, descriptionOfAssociactionA, tableNameB, descriptionOfAssociactionB);
          break;
        case "hearing_type_case_type":
          sqlStatement = generateHearingTypeCaseTypeJoinTableSQL(tableNameA, descriptionOfAssociactionA, tableNameB, descriptionOfAssociactionB);
          break;
        case "room":
          sqlStatement = generateRoomSQL(tableNameA, descriptionOfAssociactionA, tableNameB, descriptionOfAssociactionB);
          break;
      }

      console.log(sqlStatement);
    });

    console.log(generateAvailabilitySQL(judgeIds, roomIds));

    if (roomNames.length != 0) {
      console.error('Following rooms arent used in second sheet')
      roomNames.forEach(roomname => console.log(roomname))
      console.error('Generated SQL statements for these rooms - THERE ARENT INCLUDED IN AVAILABILITY')
      roomNames.forEach(roomName => console.log(generateRoomSQLWithNullRoomType(roomName)));
    }
});
}).catch(error => console.log('Errors ' + error));


function generateCaseTypeSessionTypeJoinTableSQL(tableNameA, descriptionA, tableNameB, descriptionB) {
  const caseType = (tableNameA.toLowerCase().indexOf("case") >= 0) ? [tableNameA, descriptionA] : [tableNameB, descriptionB];
  const sessionType = (tableNameA.toLowerCase().indexOf("session") >= 0) ? [tableNameA, descriptionA] : [tableNameB, descriptionB];

  return `INSERT INTO case_type_session_type (case_type_code, session_type_code) values ((Select code from case_type where description = '${caseType[1]}'), (Select code from session_type where description = '${sessionType[1]}'));`;
}

function generateHearingTypeCaseTypeJoinTableSQL(tableNameA, descriptionA, tableNameB, descriptionB) {
  const caseType = (tableNameA.toLowerCase().indexOf("case") >= 0) ? [tableNameA, descriptionA] : [tableNameB, descriptionB];
  const hearingType = (tableNameA.toLowerCase().indexOf("hearing") >= 0) ? [tableNameA, descriptionA] : [tableNameB, descriptionB];

  return `INSERT INTO hearing_type_case_type (case_type_code, hearing_type_code) values ((Select code from case_type where description = '${caseType[1]}'), (Select code from hearing_type where description = '${hearingType[1]}'));`;
}

function generateSessionTypeHearingTypeJoinTableSQL(tableNameA, descriptionA, tableNameB, descriptionB) {
  const sessionType = (tableNameA.toLowerCase().indexOf("session") >= 0) ? [tableNameA, descriptionA] : [tableNameB, descriptionB];
  const hearingType = (tableNameA.toLowerCase().indexOf("hearing") >= 0) ? [tableNameA, descriptionA] : [tableNameB, descriptionB];

  return `INSERT INTO hearing_type_session_type (session_type_code, hearing_type_code) values ((Select code from session_type where description = '${sessionType[1]}'), (Select code from hearing_type where description = '${hearingType[1]}'));`;
}

function generateRoomSQL(tableNameA, descriptionA, tableNameB, descriptionB) {
  const roomType = (tableNameA.toLowerCase().indexOf("room_type") >= 0) ? [tableNameA, descriptionA] : [tableNameB, descriptionB];
  const room = (tableNameA.toLowerCase().indexOf("room_type") < 0) ? [tableNameA, descriptionA] : [tableNameB, descriptionB];
  
  const indexOfUsedRoomDescription = roomNames.indexOf(room[1]);
  if (indexOfUsedRoomDescription > -1) {
    roomNames.splice(indexOfUsedRoomDescription, 1);
  } else {
    console.error(`Room with name/description ${room[1]} do not exist on first sheet`)
  }
  
    const roomId = uuidv4();
    roomIds.push(roomId);
  return `INSERT INTO room (id, name, room_type_code) VALUES ('${roomId}', '${room[1]}', (Select code from room_type where description = '${roomType[1]}'));`;
}

function generateRoomSQLWithNullRoomType(roomName) { 
  const roomId = uuidv4();
  roomIdOfRoomWithOutRoomType.push(roomId);
  return `INSERT INTO room (id, name, room_type_code) VALUES ('${roomId}', '${roomName}', NULL);`;
}

function resolveTableName(tableName1, tableName2) {
  function isSessionTypeOrCaseType(tableName) {
    return (tableName === "session_type" || tableName === "case_type");
  }

  function isSessionTypeOrHearingType(tableName) {
    return (tableName === "session_type" || tableName === "hearing_type");
  }

  function isRoomTypeOrRoom(tableName) {
    return (tableName === "room" || tableName === "room_type");
  }

  function isHearingTypeOrCaseType(tableName) {
    return (tableName === "hearing_type" || tableName === "case_type");
  }

  let tableName;

  if (isSessionTypeOrCaseType(tableName1) && isSessionTypeOrCaseType(tableName2)) {
    tableName = "case_type_session_type";
  } else if (isSessionTypeOrHearingType(tableName1) && isSessionTypeOrHearingType(tableName2)) {
    tableName = "hearing_type_session_type";
  } else if (isHearingTypeOrCaseType(tableName1) && isHearingTypeOrCaseType(tableName2)) {
    tableName = "hearing_type_case_type";
  } else if (isRoomTypeOrRoom(tableName1) && isRoomTypeOrRoom(tableName2)) {
    tableName = "room";
  } else {
    console.error("Can't resolve table name: " + tableName1 + " & " + tableName2);
    tableName = "";
  }

  return tableName;
}

function parseTableName(tableName) {
  return tableName.trim().toLowerCase().replace(" ", "_");
}

function createCodeFrom(description) {
  return description.trim().toLowerCase().replace(/\W/g, "-").substring(0, 255);
}

function createReferenceDataSQL(tableName, description, code) {
  return `INSERT INTO ${tableName} (description, code) VALUES ('${description}','${code}');`;
}

function createPersonSQL(name) {
    judgeId = uuidv4();
    judgeIds.push(judgeId);
  return `INSERT INTO person (id, person_type, name, username) VALUES ('${judgeId}', 'JUDGE', '${name}', '${generateUserName(name)}');`;
}

function generateUserName(name) {
  return name.toLowerCase().trim().replace(" ", "");
}

function extractTableNameFrom(cell) {
  return parseTableName(cell.slice(0, cell.indexOf("(")));
}

function extractDescriptionFrom(cell) {
  return cell.slice(cell.indexOf("(") + 1, cell.lastIndexOf(")")).trim();
}

function generateAvailabilitySQL(judgeIds, roomsIds) {
    function createJudgeAvailability(judgeIds) {
        return judgeIds.map(judgeId => {
            return `((SELECT uuid_generate_v4()), startDateTime, 28800, '${judgeId}', null)`;
        });
    }

    function createRoomsValues(roomIds) {
        return roomIds.map(roomId => {
            return `((SELECT uuid_generate_v4()), startDateTime, 28800, null, '${roomId}')`;
        }); 
    }
    const values = createJudgeAvailability(judgeIds).concat(createRoomsValues(roomsIds));
    return `
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

    DO $$
    DECLARE startDateTime timestamp with time zone = '${moment().format('YYYY-MM-DD')} 07:00:00+00';
    BEGIN
    FOR counter IN 1..365 LOOP
        IF (extract(dow from startDateTime) NOT IN (0,6)) THEN
            INSERT INTO availability (id, start, duration, person_id, room_id ) VALUES
            ${values.join(',\n')};
        END IF;
        startDateTime = startDateTime + interval '1' day;
    END LOOP;
    END; $$
    `;
}