package nextstep.courses.infrastructure;

import nextstep.courses.domain.*;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class JdbcSessionRepository implements SessionRepository {
    private JdbcOperations jdbcTemplate;

    public JdbcSessionRepository(JdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Session save(Session session, Long courseId) {
        String sql = "insert into session(started_at, end_at, payment_type, status, recruitment_status, maximum_user_count, image_url, course_id) values(?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setTimestamp(1, toTimeStamp(session.getSessionPeriod().getStartedAt()));
            ps.setTimestamp(2, toTimeStamp(session.getSessionPeriod().getEndAt()));
            ps.setString(3, session.getPaymentType().getKey());
            ps.setString(4, session.getProgressStatus());
            ps.setString(5, session.getRecruitmentStatus());
            ps.setInt(6, session.getMaximumEnrollmentCount());
            ps.setString(7, session.getSessionImageUrl().value());
            ps.setLong(8, courseId);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        long sessionId = key != null ? key.longValue() : -1;

        return new Session.Builder()
                .with(session)
                .withId(sessionId)
                .build();
    }

    @Override
    public Session findById(Long sessionId) {
        String sql = "select id, started_at, end_at, payment_type, status, recruitment_status, maximum_user_count, image_url from session where id = ?";

        RowMapper<Session> rowMapper = sessionRowMapper();
        return jdbcTemplate.queryForObject(sql, rowMapper, sessionId);
    }

    @Override
    public List<Session> findByCourseId(Long courseId) {
        String sql = "select id, started_at, end_at, payment_type, status, recruitment_status, maximum_user_count, image_url from session where course_id = ?";

        RowMapper<Session> rowMapper = sessionRowMapper();
        return jdbcTemplate.query(sql, rowMapper, courseId);
    }

    @Override
    public long saveSessionUser(Session session, SessionUser sessionUser) {
        String sql = "insert into session_users(user_id, session_id, status) values(?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, sessionUser.getUserId());
            ps.setLong(2, session.getId());
            ps.setString(3, sessionUser.getSessionUserStatus().getKey());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : -1;
    }

    @Override
    public List<SessionUser> findAllUserBySessionId(Long sessionId) {
        String sql = "select user_id, status from session_users where session_id = ?";

        RowMapper<SessionUser> rowMapper = userRowMapper();
        return jdbcTemplate.query(sql, rowMapper, sessionId);
    }

    @Override
    public SessionUser findUserByUserIdAndSessionId(Long sessionId, Long userId) {
        String sql = "select user_id, status from session_users where session_id = ? and user_id = ?";

        RowMapper<SessionUser> rowMapper = userRowMapper();
        return jdbcTemplate.queryForObject(sql, rowMapper, sessionId, userId);
    }

    @Override
    public void updateSessionUserStatus(Long sessionId, SessionUser sessionUser) {
        String sql = "update session_users set status = ? where session_id = ? and user_id = ?";
        jdbcTemplate.update(sql, sessionUser.getSessionUserStatus().getKey(), sessionId, sessionUser.getUserId());
    }

    @Override
    public void saveApprovedUser(Session session, Long userId) {
        String sql = "insert into approved_users(user_id, session_id) values(?, ?)";
        jdbcTemplate.update(sql, userId, session.getId());
    }

    private RowMapper<Session> sessionRowMapper() {
        return (rs, rowNum) -> new Session(
                rs.getLong(1),
                new SessionPeriod(toLocalDateTime(rs.getTimestamp(2)), toLocalDateTime(rs.getTimestamp(3))),
                PaymentType.valueOf(rs.getString(4)),
                new SessionStatus(
                        SessionProgressStatus.valueOf(rs.getString(5)),
                        SessionRecruitmentStatus.valueOf(rs.getString(6))
                ),
                rs.getInt(7),
                new SessionImageUrl(rs.getString(8))
        );
    }

    private RowMapper<SessionUser> userRowMapper() {
        return (rs, rowNum) -> new SessionUser(
                rs.getLong(1),
                SessionUserStatus.valueOf(rs.getString(2))
        );
    }

    private Timestamp toTimeStamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
