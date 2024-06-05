package org.ergea.securityapp.dto.req;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class RegisterModel {
    @NotEmpty(message = "username is required.")
    private String username;
    @NotEmpty(message = "password is required.")
    private String password;
    @NotEmpty(message = "fullname is required.")
    private String fullname;
    public String email;

    public String name;

    public String gender;

    public String domicile;

    public String phone_number;

    public String interest;

}
