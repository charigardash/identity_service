package com.learning.responseDTO;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Set;

@Data
@Builder
public class JwtResponse {
    private String token;
    private String type;
    private Long id;
    private String userName;
    private String email;
    private Set<String> roles;

}
