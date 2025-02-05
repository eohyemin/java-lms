package nextstep.courses.service;

import nextstep.courses.domain.Session;
import nextstep.courses.domain.SessionRepository;
import nextstep.courses.domain.SessionUser;
import nextstep.courses.domain.SessionUsers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public Session save(Session session, Long courseId) {
        return sessionRepository.save(session, courseId);
    }

    @Transactional(readOnly = true)
    public Session findById(Long sessionId) {
        return sessionRepository.findById(sessionId);
    }

    @Transactional(readOnly = true)
    public List<Session> findByCourseId(Long courseId) {
        return sessionRepository.findByCourseId(courseId);
    }

    @Transactional
    public long enroll(Session session, SessionUser sessionUser) {
        session.enroll(sessionUser);
        return sessionRepository.saveSessionUser(session, sessionUser);
    }

    @Transactional(readOnly = true)
    public List<SessionUser> findAllUserBySessionId(Long sessionId) {
        return sessionRepository.findAllUserBySessionId(sessionId);
    }

    @Transactional(readOnly = true)
    public SessionUser findUserByUserIdAndSessionId(Long sessionId, Long userId) {
        return sessionRepository.findUserByUserIdAndSessionId(sessionId, userId);
    }

    @Transactional
    public void addApprovedUser(Session session, Long userId) {
        session.addApprovedUser(userId);
        sessionRepository.saveApprovedUser(session, userId);
    }

    @Transactional
    public void approveEnrollment(Session session, Long userId) {
        SessionUser sessionUser = sessionRepository.findUserByUserIdAndSessionId(session.getId(), userId);
        session.approve(sessionUser);
        sessionRepository.updateSessionUserStatus(session.getId(), sessionUser);
    }

    @Transactional
    public void rejectEnrollment(Session session, Long userId) {
        SessionUser sessionUser = sessionRepository.findUserByUserIdAndSessionId(session.getId(), userId);
        session.reject(sessionUser);
        sessionRepository.updateSessionUserStatus(session.getId(), sessionUser);
    }
}
