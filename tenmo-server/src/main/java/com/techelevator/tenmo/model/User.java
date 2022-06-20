package com.techelevator.tenmo.model;


import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class User {
   private Long user_id;
   private String username;
   private String password;
   private boolean activated;
   private Set<Authority> authorities = new HashSet<>();

   public User() { }

   public User(Long user_id, String username, String password, String authorities) {
      this.user_id = user_id;
      this.username = username;
      this.password = password;
      this.activated = true;
   }

   public Long getUser_id() {
      return user_id;
   }

   public void setUser_id(Long user_id) {
      this.user_id = user_id;
   }

   public String getUsername() {
      return username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public boolean isActivated() {
      return activated;
   }

   public void setActivated(boolean activated) {
      this.activated = activated;
   }

   public Set<Authority> getAuthorities() {
      return authorities;
   }

   public void setAuthorities(Set<Authority> authorities) {
      this.authorities = authorities;
   }

   public void setAuthorities(String authorities) {
      String[] roles = authorities.split(",");
      for(String role : roles) {
         this.authorities.add(new Authority("ROLE_" + role));
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      User user = (User) o;
      return user_id == user.user_id &&
              activated == user.activated &&
              Objects.equals(username, user.username) &&
              Objects.equals(password, user.password) &&
              Objects.equals(authorities, user.authorities);
   }

   @Override
   public int hashCode() {
      return Objects.hash(user_id, username, password, activated, authorities);
   }

   @Override
   public String toString() {
      return "User{" +
              "user_id=" + user_id +
              ", username='" + username + '\'' +
              ", activated=" + activated +
              ", authorities=" + authorities +
              '}';
   }
}
