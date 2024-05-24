package org.ergea.securityapp.service.oauth;

import org.ergea.securityapp.dto.req.LoginModel;
import org.ergea.securityapp.dto.req.RegisterModel;

import java.security.Principal;
import java.util.Map;

public interface UserService {
    Map registerManual(RegisterModel objModel);

    Map registerByGoogle(RegisterModel objModel);

    public Map getDetailProfile(Principal principal);

    public Map login(LoginModel loginModel);
}

