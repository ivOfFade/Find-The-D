import java.util.Objects;

/** Data and behavior shared by every user. Parent of Tenant and Landlord. */
public abstract class Person {
    private final int personId;
    private String name;
    private int age;
    private String contactNum;
    private String email;
    private final String passwordHash;
    private final Role role;
    private boolean loggedIn;

    protected Person(String name, int age, String contactNum, String email, String rawPassword, Role role) {
        this.personId = IdGenerator.next();
        this.name = Validation.required(name, "Name");
        this.age = Validation.age(age);
        this.contactNum = Validation.contactNum(contactNum);
        this.email = Validation.email(email);
        this.passwordHash = PasswordHasher.hash(rawPassword);
        this.role = role;
    }

    /** Database loading: values are trusted to be already validated and hashed. */
    protected Person(int personId, String name, int age, String contactNum, String email, String passwordHash, Role role) {
        this.personId = personId;
        this.name = name;
        this.age = age;
        this.contactNum = contactNum;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String displayInfo() {
        return name + " (" + role + "), age " + age + ", " + contactNum + ", " + email;
    }

    public void login(String rawPassword) {
        if (!checkPassword(rawPassword)) {
            throw new AuthenticationException("Incorrect email or password.");
        }
        loggedIn = true;
    }

    public void logout() { loggedIn = false; }

    public boolean checkPassword(String rawPassword) { return PasswordHasher.matches(rawPassword, passwordHash); }

    public abstract DashboardType getDashboardType();

    public int getPersonId() { return personId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getContactNum() { return contactNum; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public boolean isLoggedIn() { return loggedIn; }

    public void setName(String name) { this.name = Validation.required(name, "Name"); }
    public void setAge(int age) { this.age = Validation.age(age); }
    public void setContactNum(String contactNum) { this.contactNum = Validation.contactNum(contactNum); }
    public void setEmail(String email) { this.email = Validation.email(email); }

    /** Only the repository layer may read the hash. */
    String getPasswordHashForRepository() { return passwordHash; }

    @Override public boolean equals(Object o) { return o instanceof Person && ((Person) o).personId == personId; }
    @Override public int hashCode() { return Objects.hash(personId); }
    @Override public String toString() { return getClass().getSimpleName() + "[id=" + personId + ", name=" + name + "]"; }
}
