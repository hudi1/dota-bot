package org.tomass.dota.gc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.WebApplicationInitializer;
import org.tomass.dota.gc.service.AuthService;
import org.tomass.dota.gc.service.RestAuthenticationProvider;

@SpringBootApplication
@EnableGlobalMethodSecurity(securedEnabled = true)
public class Dota2GCApplication extends SpringBootServletInitializer implements WebApplicationInitializer {

    public static void main(String[] args) {
        // LogManager.addListener(new DefaultLogListener());
        SpringApplication.run(Dota2GCApplication.class, args);
    }

    @Configuration
    public class ApplicationSecurity extends WebSecurityConfigurerAdapter {
        @Autowired
        private AuthService authService;

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
            RestAuthenticationProvider provider = new RestAuthenticationProvider();
            provider.setUserDetailsService(authService);
            provider.setPasswordEncoder(new BCryptPasswordEncoder());
            return provider;
        }

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(authenticationProvider());
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().anyRequest().fullyAuthenticated().and().httpBasic().and().csrf().disable();
        }
    }

}