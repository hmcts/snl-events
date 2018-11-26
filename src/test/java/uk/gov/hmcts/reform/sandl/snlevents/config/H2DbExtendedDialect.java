package uk.gov.hmcts.reform.sandl.snlevents.config;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.type.StandardBasicTypes;

import java.sql.Types;

public class H2DbExtendedDialect extends H2Dialect {
    public H2DbExtendedDialect() {
        super();
        registerColumnType(Types.TINYINT, "smallint");
        registerHibernateType(Types.TIMESTAMP_WITH_TIMEZONE, StandardBasicTypes.TIMESTAMP.getName());
    }
}
