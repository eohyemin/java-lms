package nextstep.courses.domain;

import nextstep.users.domain.NsUserTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionUserTest {
    @Test
    void approval_status() {
        SessionUser sessionUser = new SessionUser(NsUserTest.JAVAJIGI.getId());
        sessionUser.approve();
        assertThat(sessionUser.isApproved()).isTrue();
    }

    @Test
    void reject_status() {
        SessionUser sessionUser = new SessionUser(NsUserTest.JAVAJIGI.getId());
        sessionUser.reject();
        assertThat(sessionUser.isApproved()).isFalse();
    }

    @Test
    void wait_status() {
        SessionUser sessionUser = new SessionUser(NsUserTest.JAVAJIGI.getId());
        assertThat(sessionUser.isApproved()).isFalse();
    }
}
