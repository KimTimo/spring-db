package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    @Test
    void driverManager() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection1={}, class={}", con1, con1.getClass());
        log.info("connection2={}, class={}", con2, con2.getClass());
    }

    @Test
    void dataSourceDriverManager() throws SQLException {
        // DriverManagerDataSource - 항상 새로운 커넥션을 획득한다.
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        useDataSource(dataSource);
    }

    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        // 커넥션 풀링
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("Teemo Pool");

        useDataSource(dataSource);
        Thread.sleep(2000);
    }

    /**
     * 커넥션 풀 초과 시 문제 해결 알고리즘 전략!
     * <ol>
     *     <h4>1. 대기열 관리</h4>
     *          <li>커넥션 풀이 최대 크기에 도달했을 때, 요청을 대기열에 넣고 커넥션이 반환될 때까지 기다리게 한다.</li>
     *          <li>Blocking Queue: 커넥션 요청이 오면 풀의 최대 크기를 초과한 경우 요청을 대기열에 넣고,
     *          커넥션이 반환되면 대기열에서 요청을 꺼내어 커넥션을 제공</li>
     *
     *    <h4>2. 재시도 메커니즘</h4>
     *         <li>일정 시간 동안 대기 후에도 커넥션을 얻지 못한 경우 재시도를 하는 방법</li>
     *         <li>Exponential Backoff: 처음 실패 시 짧은 시간 대기 후 재시도하고, 계속 실패할 경우 대기 시간을 점진적으로 늘려가는 방법</li>
     *
     *    <h4>3. 타임아웃 설정</h4>
     *         <li>커넥션을 얻기 위한 최대 대기 시간을 설정합니다. 대기 시간이 초과되면 예외를 발생시켜 애플리케이션이 이를 처리하도록 한다.</li>
     *         <li>Connection Timeout: 커넥션 풀에서 커넥션을 얻기 위한 최대 대기 시간을 설정</li>
     *
     *    <h4>4. 적절한 풀 크기 설정</h4>
     *         <li>애플리케이션의 부하와 동시 연결 수를 분석하여 적절한 풀 크기를 설정한다. </li>
     *         <li>너무 작은 풀 크기는 성능 저하를 일으키고, 너무 큰 풀 크기는 리소스 낭비를 초래할 수 있다.</li>
     *         <li>Dynamic Pool Sizing: 애플리케이션의 부하에 따라 동적으로 풀 크기를 조정하는 방법 그러나 이는 구현이 복잡할 수 있다.</li>
     *
     *    <h4>5. Fallback Mechanisms(폴백 매커니즘)</h4>
     *         <li>커넥션을 얻지 못한 경우 대체 동작을 정의하는 방법</li>
     *         <li>Graceful Degradation(그레이스펄 데그러데이션): 커넥션을 얻지 못한 경우 서비스 품질을 낮추어 일부 기능을 제한하거나 캐시된 데이터를 사용하는 방법</li>
     *
     *    <h4>6. 분산 트랜잭션</h4>
     *         <li>분산 트랜잭션 관리자를 사용하여 여러 데이터베이스 커넥션을 관리하고, 각 커넥션의 사용 빈도와 시간을 최적화</li>
     *
     *    <h4>7. Connection Pool Monitoring</h4>
     *         <li>풀의 상태를 모니터링하고, 문제가 발생하기 전에 경고를 보내어 예방 조치를 취함</li>
     *
     *
     * </ol>
     *
     * @throws SQLException
     * @throws InterruptedException
     */
    @DisplayName("커넥션 풀 초과 테스트")
    @Test
    void connectionPoolExceeds() throws SQLException, InterruptedException {
        // 커넥션 풀링
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(5); // 풀의 최대 크기를 5로 설정
        dataSource.setPoolName("Teemo Pool");

        List<Connection> connections = new ArrayList<>();

        try {
            for (int i = 0; i < 7; i++) {
                Connection connection = dataSource.getConnection();
                log.info("connection{}={}, class={}", i + 1, connection, connection.getClass());
                connections.add(connection);
            }
        } catch (SQLException e) {
            log.error("Connection pool exceeded: {}", e.getMessage());
        } finally {
            // 커넥션 닫기
            for (Connection connection : connections) {
                if (connection != null) {
                    connection.close();
                }
            }
        }

        Thread.sleep(2000);
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection1={}, class={}", con1, con1.getClass());
        log.info("connection2={}, class={}", con2, con2.getClass());
    }

}
