package org.ergea.securityapp.service.oauth.impl;

import org.ergea.securityapp.config.Config;
import org.ergea.securityapp.config.Response;
import org.ergea.securityapp.dto.req.LoginModel;
import org.ergea.securityapp.dto.req.RegisterModel;
import org.ergea.securityapp.model.oauth2.Role;
import org.ergea.securityapp.model.oauth2.User;
import org.ergea.securityapp.repository.RoleRepository;
import org.ergea.securityapp.repository.UserRepository;
import org.ergea.securityapp.service.oauth.Oauth2UserDetailService;
import org.ergea.securityapp.service.oauth.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;

import java.security.Principal;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    Config config = new Config();
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    RoleRepository repoRole;

    @Autowired
    UserRepository repoUser;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    public Response templateResponse;

    @Autowired
    private Oauth2UserDetailService userDetailsService;

    @Value("${BASEURL}")
    private String baseUrl;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Map registerManual(RegisterModel objModel) {
        Map map = new HashMap();
        try {
            String[] roleNames = {"ROLE_USER", "ROLE_READ", "ROLE_WRITE"}; // admin
            User user = new User();
            user.setUsername(objModel.getUsername().toLowerCase());
            user.setFullname(objModel.getFullname());

            //step 1 :
//            user.setEnabled(false); // matikan user

            String password = encoder.encode(objModel.getPassword().replaceAll("\\s+", ""));
            List<Role> r = repoRole.findByNameIn(roleNames);

            user.setRoles(r);
            user.setPassword(password);
            User obj = repoUser.save(user);

            return templateResponse.sukses(obj);

        } catch (Exception e) {
            logger.error("Eror registerManual=", e);
            return templateResponse.error("eror:" + e, e);
        }
    }

    @Override
    public Map registerByGoogle(RegisterModel objModel) {
        Map map = new HashMap();
        try {
            String[] roleNames = {"ROLE_USER", "ROLE_READ", "ROLE_WRITE"}; // ROLE DEFAULE
            User user = new User();
            user.setUsername(objModel.getUsername().toLowerCase());
            user.setFullname(objModel.getFullname());
            //step 1 :
            user.setEnabled(false); // matikan user
            String password = encoder.encode(objModel.getPassword().replaceAll("\\s+", ""));
            List<Role> r = repoRole.findByNameIn(roleNames);
            user.setRoles(r);
            user.setPassword(password);
            User obj = repoUser.save(user);
            return templateResponse.sukses(obj);

        } catch (Exception e) {
            logger.error("Eror registerManual=", e);
            return templateResponse.error("eror:" + e, e);
        }
    }

    @Override
    public Map getDetailProfile(Principal principal) {
        User idUser = getUserIdToken(principal, userDetailsService);
        try {
            User obj = repoUser.save(idUser);
            return templateResponse.sukses(obj);
        } catch (Exception e) {
            return templateResponse.error(e, "500");
        }
    }

    private User getUserIdToken(Principal principal, Oauth2UserDetailService userDetailsService) {
        UserDetails user = null;
        String username = principal.getName();
        if (!StringUtils.isEmpty(username)) {
            user = userDetailsService.loadUserByUsername(username);
        }

        if (null == user) {
            throw new UsernameNotFoundException("User not found");
        }
        User idUser = repoUser.findOneByUsername(user.getUsername());
        if (null == idUser) {
            throw new UsernameNotFoundException("User name not found");
        }
        return idUser;
    }

    @Override
    public Map login(LoginModel loginModel) {
        /**
         * bussines logic for login here
         * **/
        try {
            Map<String, Object> map = new HashMap<>();

            User checkUser = repoUser.findOneByUsername(loginModel.getUsername());

            if ((checkUser != null) && (encoder.matches(loginModel.getPassword(), checkUser.getPassword()))) {
                if (!checkUser.isEnabled()) {
                    map.put("is_enabled", checkUser.isEnabled());
                    return templateResponse.error(map, HttpStatus.NOT_FOUND);
                }
            }
            if (checkUser == null) {
                return templateResponse.error("user not found", HttpStatus.NOT_FOUND);
            }
            if (!(encoder.matches(loginModel.getPassword(), checkUser.getPassword()))) {
                return templateResponse.error("wrong password", HttpStatus.BAD_REQUEST);
            }
            String url = baseUrl + "/oauth/token?username=" + loginModel.getUsername() +
                    "&password=" + loginModel.getPassword() +
                    "&grant_type=password" +
                    "&client_id=my-client-web" +
                    "&client_secret=password";
            ResponseEntity<Map> response = restTemplateBuilder.build().exchange(url, HttpMethod.POST, null, new
                    ParameterizedTypeReference<Map>() {
                    });

            if (response.getStatusCode() == HttpStatus.OK) {
                User user = userRepository.findOneByUsername(loginModel.getUsername());
                List<String> roles = new ArrayList<>();

                for (Role role : user.getRoles()) {
                    roles.add(role.getName());
                }

                map.put("access_token", response.getBody().get("access_token"));
                map.put("token_type", response.getBody().get("token_type"));
                map.put("refresh_token", response.getBody().get("refresh_token"));
                map.put("expires_in", response.getBody().get("expires_in"));
                map.put("scope", response.getBody().get("scope"));
                map.put("jti", response.getBody().get("jti"));

                return map;
            } else {
                return templateResponse.error("user not found", HttpStatus.NOT_FOUND);
            }
        } catch (HttpStatusCodeException e) {
            e.printStackTrace();
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return templateResponse.error("invalid login", HttpStatus.BAD_REQUEST);
            }
            return templateResponse.error(e, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();

            return templateResponse.error(e, HttpStatus.BAD_REQUEST);
        }
    }

}

