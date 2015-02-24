package thedonkey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.openid.OpenIDLoginConfigurer;

/**
 * Configures the web security.
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	private DonkeyAuthenticationProvider authenticationProvider;

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		// auth.inMemoryAuthentication().withUser("user").password("password").roles("USER");
		auth.authenticationProvider(authenticationProvider);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				//
				.antMatchers("/resources/**").permitAll()
				//
				.antMatchers("/hello").hasAnyRole("ANONYMOUS", "USER")
				.anyRequest().authenticated();

		http.formLogin()//
				.loginPage("/login")//
				.permitAll();

		OpenIDLoginConfigurer<HttpSecurity> oid = http
				.openidLogin()
				.loginPage("/login")
				.permitAll()
				.authenticationUserDetailsService(
						new CustomUserDetailsService());
		oid.attributeExchange("https://www.google.com/.*")//
				.attribute("email")//
				.type("http://axschema.org/contact/email")//
				.required(true)//
				.and()//
				.attribute("firstname")// //
				.type("http://axschema.org/namePerson/first")//
				.required(true)//
				.and()//
				.attribute("lastname")//
				.type("http://axschema.org/namePerson/last")//
				.required(true);

		oid.attributeExchange(".*yahoo.com.*")//
				.attribute("email")//
				.type("http://axschema.org/contact/email")//
				.required(true)//
				.and()//
				.attribute("fullname")//
				.type("http://axschema.org/namePerson")//
				.required(true);
		oid.attributeExchange(".*myopenid.com.*")//
				.attribute("email")//
				.type("http://schema.openid.net/contact/email")//
				.required(true)//
				.and()//
				.attribute("fullname")//
				.type("http://schema.openid.net/namePerson")//
				.required(true);
	}
}
