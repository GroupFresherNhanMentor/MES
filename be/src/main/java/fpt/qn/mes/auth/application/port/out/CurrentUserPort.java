package fpt.qn.mes.auth.application.port.out;

import java.util.UUID;

import fpt.qn.mes.auth.application.security.AppUserPrincipal;

public interface CurrentUserPort {

    AppUserPrincipal getCurrentUser();

    UUID getCurrentUserId();

    String getCurrentUsername();
}
