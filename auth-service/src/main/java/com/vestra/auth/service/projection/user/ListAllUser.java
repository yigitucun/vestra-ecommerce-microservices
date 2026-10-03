package com.vestra.auth.service.projection.user;

import java.util.UUID;

public interface ListAllUser {
    UUID getId();
    String getFirstName();
    String getLastName();
    String getEmail();
    String getRole();
    boolean getIsActive();

}
