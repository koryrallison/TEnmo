package com.techelevator.tenmo.model;

public class AuthenticatedUser {
	
	private String token;
	private User user;
	private Long activeAccountId;
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Long getActiveAccountId() {
		return activeAccountId;
	}
	public void setActiveAccountId(Long activeAccountId) {
		this.activeAccountId = activeAccountId;
	}
}
