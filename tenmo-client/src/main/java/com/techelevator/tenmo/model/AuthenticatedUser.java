package com.techelevator.tenmo.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticatedUser {
	
	private String token;
	private User user;

}
