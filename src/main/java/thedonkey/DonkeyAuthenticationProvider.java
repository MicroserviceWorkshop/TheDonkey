package thedonkey;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import thedonkey.persistence.Account;
import thedonkey.persistence.AccountRepository;
import thedonkey.util.PasswordHash;

/**
 * Security provider that uses the {@link AccountRepository} to check for valid users.
 */
@Service
public class DonkeyAuthenticationProvider implements AuthenticationProvider {
  @Autowired
  private AccountRepository accountRepository;

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.springframework.security.authentication.AuthenticationProvider#authenticate(org.springframework
   * .security.core.Authentication)
   */
  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String username = authentication.getName();
    String password = authentication.getCredentials().toString();

    Optional<Account> person = accountRepository.findByUsername(username);
    if (person.isPresent()) {

      String hashedPw = PasswordHash.computeHash(password);
      if (hashedPw.equals(person.get().getPassword())) {
        return new UsernamePasswordAuthenticationToken(username, "password", getGrantedAuthorities(username));
      }

      // TODO [security] (poc) I really don't like this. Security recommendations are not to tell
      // the client whether the username or password was wrong.
      throw new BadCredentialsException("Wrong password");
    }

    throw new UsernameNotFoundException("Username " + username + " not found");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.springframework.security.authentication.AuthenticationProvider#supports(java.lang.Class)
   */
  @Override
  public boolean supports(Class<?> authentication) {
    return true;
  }

  private Collection<? extends GrantedAuthority> getGrantedAuthorities(String username) {
    Collection<? extends GrantedAuthority> authorities;
    if (username.equals("jlong")) {
      authorities = Arrays.asList(() -> "ROLE_ADMIN", () -> "ROLE_BASIC");
    } else {
      authorities = Arrays.asList(() -> "ROLE_BASIC");
    }
    return authorities;
  }
}
