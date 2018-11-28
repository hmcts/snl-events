package uk.gov.hmcts.reform.sandl.snlevents.config;

import org.hibernate.dialect.PostgreSQL95Dialect;

import java.sql.Types;

public class ExtendedPostgresDialect extends PostgreSQL95Dialect {
    public ExtendedPostgresDialect() {
        super();
        registerHibernateType(Types.OTHER, "org.hibernate.type.PostgresUUIDType");
    }
}
