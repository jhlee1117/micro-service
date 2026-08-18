package com.microservices.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

  private String username;
  private String password;
  private String email;
  // private String name;
  private String firstName;
  private String lastName;
  private boolean terms;
}
