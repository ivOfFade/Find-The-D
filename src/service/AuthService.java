package service;

import enums.Role;
import exception.AuthenticationException;
import exception.InvalidInputException;
import exception.UnauthorizedActionException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import model.Landlord;
import model.Person;
import model.Tenant;
import repository.LandlordRepository;
import repository.PersonRepository;
import repository.TenantRepository;

public final class AuthService {

  private static Person currentUser;
  private final PersonRepository people = new PersonRepository();
  private final LandlordRepository landlords = new LandlordRepository();
  private final TenantRepository tenants = new TenantRepository();

  public Person register(
    Role role,
    String name,
    int age,
    String contactNum,
    String email,
    String rawPassword,
    String otherContact
  ) throws SQLException, InvalidInputException, AuthenticationException {
    String normalizedEmail =
      email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    if (people.emailExists(normalizedEmail)) {
      throw new InvalidInputException("That email is already registered.");
    }

    Person account;
    if (role == Role.LANDLORD) {
      Landlord landlord = new Landlord(
        name,
        age,
        contactNum,
        normalizedEmail,
        rawPassword,
        otherContact
      );
      landlords.save(landlord);
      account = landlord;
    } else {
      Tenant tenant = new Tenant(
        name,
        age,
        contactNum,
        normalizedEmail,
        rawPassword
      );
      tenants.save(tenant);
      account = tenant;
    }

    account.login(rawPassword);
    people.setLoggedIn(account.getPersonId(), true);
    currentUser = account;
    return account;
  }

  public Person login(String email, String rawPassword, Role requestedRole)
    throws SQLException, AuthenticationException {
    String normalizedEmail =
      email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    Optional<String> storedRole = people.findRoleByEmail(normalizedEmail);
    if (
      storedRole.isEmpty() || !requestedRole.name().equals(storedRole.get())
    ) {
      throw new AuthenticationException("Incorrect email or password.");
    }

    Person account;
    if (requestedRole == Role.LANDLORD) {
      account = landlords
        .findByEmail(normalizedEmail)
        .orElseThrow(() ->
          new AuthenticationException("Incorrect email or password.")
        );
    } else {
      account = tenants
        .findByEmail(normalizedEmail)
        .orElseThrow(() ->
          new AuthenticationException("Incorrect email or password.")
        );
    }

    account.login(rawPassword);
    people.setLoggedIn(account.getPersonId(), true);
    currentUser = account;
    return account;
  }

  public Person currentUser() {
    return currentUser;
  }

  public void logout() throws SQLException {
    Person account = currentUser;
    if (account == null) {
      return;
    }
    people.setLoggedIn(account.getPersonId(), false);
    account.logout();
    currentUser = null;
  }

  public Person requireCurrentUser() throws UnauthorizedActionException {
    Person account = currentUser;
    if (account == null || !account.isLoggedIn()) {
      throw new UnauthorizedActionException("Please sign in first.");
    }
    return account;
  }
}
