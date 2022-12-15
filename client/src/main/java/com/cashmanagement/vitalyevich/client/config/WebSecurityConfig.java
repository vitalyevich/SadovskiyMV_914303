package com.cashmanagement.vitalyevich.client.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    /*    @Autowired
    private BCryptPasswordEncoder passwordEncoder;*/

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.cors().disable().csrf().disable();
       http
                .authorizeRequests()
                .mvcMatchers("/profile-admin", "/users/**", "/analytics/**","/collection/**", "/planning-collection/**", "/monitoring/**", "/planning/**", "/monitoring-storage/**",
                        "/storage-collection/**, /monitoring-storages/**", "/edit-password").hasRole("ADMIN")
                .mvcMatchers("/profile-cashier", "/monitoring/**", "/planning/**").hasRole("CASHIER")
               .mvcMatchers("/profile-cashier-storage/**", "/monitoring-storage/**", "/storage-collection/**, /monitoring-storages/**").hasRole("CASHIERSTORAGE")
               .mvcMatchers("/profile-dealer", "/analytics/**").hasRole("DIALER")
               .mvcMatchers("/profile-collection", "/collection/**","/planning-collection/**").hasRole("COLLECTOR")

                .antMatchers("/authorization").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/authorization")
                .failureUrl("/authorization?error=true")
                .defaultSuccessUrl("/profile",true)
                .usernameParameter("login")
                .passwordParameter("password")
                .permitAll()
                .and()
                .httpBasic()
                .and()
                .csrf().disable()
                .logout()
                .logoutSuccessUrl("/authorization");

    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**", "/fonts/**",
                        "/blocks/**", "/users/**", "/profiles/**","/confirmations/**", "/planning/**");
    }

 @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // .passwordEncoder(passwordEncoder);

  auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(NoOpPasswordEncoder.getInstance())
                .usersByUsernameQuery("select login, user_password, active from access where login=?")
                .authoritiesByUsernameQuery("select A.login, r.role_name from access A \n" +
                        "inner join user_roles ur on A.user_id = ur.user_id \n" +
                        "inner join roles r on ur.role_id = r.role_id where a.login=?");
    }

}
