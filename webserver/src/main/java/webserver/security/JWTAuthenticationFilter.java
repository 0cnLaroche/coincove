package webserver.security;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import webserver.model.ApplicationUser;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;
    private String secret;
    private long expirationTime;
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_STRING = "Authorization";

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, String secret,
                                   long expirationTime) {
        this.authenticationManager = authenticationManager;
        this.secret = secret;
        this.expirationTime = expirationTime;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {
        try {
            ApplicationUser creds = new ObjectMapper()
                    .readValue(req.getInputStream(), ApplicationUser.class);

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getUsername(),
                            creds.getPassword(),
                            new ArrayList<>())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {

        String token = JWT.create()
                .withSubject(((User) auth.getPrincipal()).getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .sign(HMAC512(secret.getBytes()));
        res.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
        res.addHeader("Access-Control-Expose-Headers","Authorization");
    }
}
