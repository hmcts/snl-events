package uk.gov.hmcts.reform.sandl.snlevents.config;

import org.hibernate.dialect.H2Dialect;

import java.sql.Types;

public class H2DBExtendedDialect extends H2Dialect {
    public H2DBExtendedDialect() {
        super();
        registerColumnType(Types.TINYINT, "smallint");
    }
}
