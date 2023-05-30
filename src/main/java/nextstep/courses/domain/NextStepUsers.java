package nextstep.courses.domain;

import nextstep.users.domain.NsUser;

import java.util.ArrayList;
import java.util.List;

public class NextStepUsers {
    private static final String MAXIMUM_ENROLLMENT_MESSAGE = "강의 최대 수강 인원이 초과되었습니다.";
    public static final String ALREADY_ENROLLED_USER = "이미 수강신청한 사용자입니다.";

    private final List<SessionUser> sessionUsers = new ArrayList<>();
    private final int maximumUserCount;

    public NextStepUsers(int maximumUserCount) {
        this.maximumUserCount = maximumUserCount;
    }

    public void enroll(NsUser nextStepUser) {
        enroll(new SessionUser(nextStepUser));
    }

    public void enroll(SessionUser sessionUser) {
        validateMaximumUserCount();
        validateEnrolledUser(sessionUser);
        sessionUsers.add(sessionUser);
    }

    public void cancel(SessionUser sessionUser) {
        if (sessionUser.isApproved()) {
            throw new IllegalArgumentException("수강 신청이 승인된 사용자입니다.");
        }
        sessionUsers.remove(sessionUser);
    }

    public void validateMaximumUserCount() {
        if (enrollmentCount() >= maximumUserCount) {
            throw new IllegalArgumentException(MAXIMUM_ENROLLMENT_MESSAGE);
        }
    }

    public void validateEnrolledUser(SessionUser sessionUser) {
        if (sessionUsers.contains(sessionUser)) {
            throw new IllegalArgumentException(ALREADY_ENROLLED_USER);
        }
    }

    public int enrollmentCount() {
        return (int) sessionUsers.stream()
                .filter(SessionUser::isApproved)
                .count();
    }

    public int getMaximumUserCount() {
        return this.maximumUserCount;
    }
}
