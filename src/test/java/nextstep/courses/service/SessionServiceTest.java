package nextstep.courses.service;

import nextstep.courses.domain.*;
import nextstep.courses.fixture.SessionFixture;
import nextstep.users.domain.NsUserTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
public class SessionServiceTest {
    @Autowired
    private SessionService sessionService;

    @Test
    void save() {
        Session session = SessionFixture.createRecruitingSession();

        Session savedSession = sessionService.save(session, 1L);
        Session findSession = sessionService.findById(savedSession.getId());

        assertThat(findSession.getId()).isEqualTo(savedSession.getId());
    }

    @Test
    void findByCourseId() {
        sessionService.save(SessionFixture.createRecruitingSession(), 2L);
        sessionService.save(SessionFixture.createRecruitingSession(), 2L);
        List<Session> findSession = sessionService.findByCourseId(2L);

        assertThat(findSession).hasSize(2);
    }

    @Test
    void enroll() {
        Session session = sessionService.save(SessionFixture.createRecruitingSession(), 1L);

        sessionService.enroll(session, new SessionUser(NsUserTest.JAVAJIGI.getId()));
        List<SessionUser> sessionUsers = sessionService.findAllUserBySessionId(session.getId());

        assertThat(sessionUsers).hasSize(1);
        assertThat(sessionUsers.get(0).getUserId()).isEqualTo(NsUserTest.JAVAJIGI.getId());
    }

    @Test
    @DisplayName("강의 최대 인원이 초과된 경우 수강신청할 수 없다")
    void saveSessionUser_Fail1() {
        Session session = sessionService.save(SessionFixture.create(SessionProgressStatus.PROGRESSING, SessionRecruitmentStatus.RECRUITING, 1), 1L);
        SessionUser sessionUser = new SessionUser(NsUserTest.JAVAJIGI.getId(), SessionUserStatus.APPROVAL);
        sessionService.enroll(session, sessionUser);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> sessionService.enroll(session, new SessionUser(NsUserTest.SANJIGI.getId())))
                .withMessageMatching("강의 최대 수강 인원이 초과되었습니다.");
    }

    @Test
    @DisplayName("강의가 모집중이 아닌 경우 수강신청할 수 없다")
    void saveSessionUser_Fail2() {
        Session session = sessionService.save(SessionFixture.create(SessionProgressStatus.PREPARING, SessionRecruitmentStatus.NOT_RECRUITING, 1), 1L);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> sessionService.enroll(session, new SessionUser(NsUserTest.JAVAJIGI.getId())))
                .withMessageMatching("모집중인 강의가 아닙니다.");
    }

    @Test
    @DisplayName("사용자, 강의 정보로 수강신청 사용자조회")
    void findUserByUserIdAndSessionId() {
        Session session = sessionService.save(SessionFixture.createRecruitingSession(), 1L);
        SessionUser sessionUser = new SessionUser(NsUserTest.JAVAJIGI.getId());

        sessionService.enroll(session, sessionUser);
        SessionUser findSessionUser = sessionService.findUserByUserIdAndSessionId(session.getId(), sessionUser.getUserId());

        assertThat(findSessionUser.getUserId()).isEqualTo(sessionUser.getUserId());
        assertThat(findSessionUser.getSessionUserStatus()).isEqualTo(sessionUser.getSessionUserStatus());
    }

    @Test
    @DisplayName("승인된 사용자의 강의 수강신청 승인")
    void approve_enrollment() {
        Session session = sessionService.save(SessionFixture.createRecruitingSession(), 1L);
        SessionUser sessionUser = new SessionUser(NsUserTest.JAVAJIGI.getId());
        sessionService.enroll(session, sessionUser);
        sessionService.addApprovedUser(session, sessionUser.getUserId());

        sessionService.approveEnrollment(session, sessionUser.getUserId());

        SessionUser approvedUser = sessionService.findUserByUserIdAndSessionId(session.getId(), sessionUser.getUserId());
        assertThat(approvedUser.isApproved()).isTrue();
    }

    @Test
    @DisplayName("거절된 사용자의 강의 수강신청 거절")
    void reject_enrollment() {
        Session session = sessionService.save(SessionFixture.createRecruitingSession(), 1L);
        SessionUser sessionUser = new SessionUser(NsUserTest.JAVAJIGI.getId());
        sessionService.enroll(session, sessionUser);

        sessionService.rejectEnrollment(session, sessionUser.getUserId());

        SessionUser rejectedUser = sessionService.findUserByUserIdAndSessionId(session.getId(), sessionUser.getUserId());
        assertThat(rejectedUser.isApproved()).isFalse();
    }
}
