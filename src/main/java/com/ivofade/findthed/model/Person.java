package com.ivofade.findthed.model;

import com.ivofade.findthed.enums.DashboardType;
import com.ivofade.findthed.enums.Role;
import com.ivofade.findthed.exception.AuthenticationException;
import com.ivofade.findthed.exception.InvalidInputException;
import com.ivofade.findthed.util.PasswordHasher;
import com.ivofade.findthed.util.Validator;

public abstract class Person {

  // Attributes
  private int personId;
  private String name;
  private int age;
  private String contactNum;
  private String email;
  private String passwordHash;
  private final Role role;
  private boolean loggedIn = false;

  // Constructor for registration
  protected Person(
    String name,
    int age,
    String contactNum,
    String email,
    String rawPassword,
    Role role
  ) throws InvalidInputException {
    this.role = Validator.role(role);
    setName(name);
    setAge(age);
    setContactNum(contactNum);
    setEmail(email);
    setPasswordHash(rawPassword);
  }

  // Constructor for database loading
  protected Person(
    int personId,
    String name,
    int age,
    String contactNum,
    String email,
    String passwordHash,
    Role role
  ) throws InvalidInputException {
    this.role = Validator.role(role);
    this.personId = personId;
    setName(name);
    setAge(age);
    setContactNum(contactNum);
    setEmail(email);
    this.passwordHash = passwordHash;
  }

  // Behaviors
  public String displayInfo() {
    return String.format(
      "%s | Age: %d | Email: %s | Contact: %s",
      name,
      age,
      email,
      contactNum
    );
  }

  public void login(String rawPassword) throws AuthenticationException {
    if (!checkPassword(rawPassword)) {
      throw new AuthenticationException("Incorrect email or password.");
    }
    loggedIn = true;
  }

  public void logout() {
    loggedIn = false;
  }

  public boolean checkPassword(String rawPassword) {
    return PasswordHasher.matches(rawPassword, passwordHash);
  }

  public abstract DashboardType getDashboardType();

  // Getters
  public int getPersonId() {
    return personId;
  }

  public String getName() {
    return name;
  }

  public int getAge() {
    return age;
  }

  public String getContactNum() {
    return contactNum;
  }

  public String getEmail() {
    return email;
  }

  public Role getRole() {
    return role;
  }

  public boolean isLoggedIn() {
    return loggedIn;
  }

  // Getter for Repository
  public String getPasswordHashForPersistence() {
    return passwordHash;
  }

  // For repository layer only. This will store the id that the database generated.
  public void assignPersonId(int generatedId) {
    if (personId != 0) {
      throw new IllegalStateException("personId is already set.");
    }
    this.personId = generatedId;
  }

  // Setters
  public final void setName(String name) throws InvalidInputException {
    this.name = Validator.name(name);
  }

  public final void setAge(int age) throws InvalidInputException {
    this.age = Validator.age(age);
  }

  public final void setContactNum(String contactNum)
    throws InvalidInputException {
    this.contactNum = Validator.contactNum(contactNum);
  }

  public final void setEmail(String email) throws InvalidInputException {
    this.email = Validator.email(email);
  }

  private void setPasswordHash(String rawPassword)
    throws InvalidInputException {
    this.passwordHash = PasswordHasher.hash(Validator.password(rawPassword));
  }
}
