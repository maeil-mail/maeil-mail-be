package maeilmail.support;

import org.hibernate.resource.jdbc.spi.StatementInspector;

class QueryCountInspector implements StatementInspector {

    private final QueryCountTester queryCountTester;

    public QueryCountInspector(QueryCountTester queryCountTester) {
        this.queryCountTester = queryCountTester;
    }

    @Override
    public String inspect(String sql) {
        queryCountTester.increase();

        return sql;
    }
}
