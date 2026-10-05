// Concrete factory for creating individual customer accounts.
// Creates both the User account and the associated Customer profile, applying default values, password encoding, customer role assignment,
// and registration information.

@Component
public class IndividualCustomerFactory implements CustomerFactory {

    private final PasswordEncoder passwordEncoder;

    // Injects the password encoder used to securely hash customer passwords
    public IndividualCustomerFactory(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // Creates the User account associated with an individual customer
    @Override
    public User createUser(...) {
        ...
    }

    // Creates the Customer profile associated with the generated User account
    @Override
    public Customer createCustomer(...) {
        ...
    }
}