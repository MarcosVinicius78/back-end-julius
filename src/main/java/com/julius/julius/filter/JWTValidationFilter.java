package com.julius.julius.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.julius.julius.constants.SecurityConstants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JWTValidationFilter extends OncePerRequestFilter{

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = request.getHeader(SecurityConstants.JWT_HEADER);
        
        if (null != jwt) {
            try {
                SecretKey key = Keys.hmacShaKeyFor(SecurityConstants.JWT_KEY.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parserBuilder()
                                    .setSigningKey(key)
                                    .build()
                                    .parseClaimsJws(jwt)
                                    .getBody();
                String username = String.valueOf(claims.get("username"));
                String authorities = (String) claims.get("authorities");
                Authentication authentication = new UsernamePasswordAuthenticationToken(username,null, 
                AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token inválido");
                response.getWriter().flush();
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        String path = request.getServletPath();
        String metodo = request.getMethod();

        switch (metodo) {
            case "POST":
                if (path.contains("/produto")) {
                    return false;
                }else if(path.contains("/categoria")){
                    return false;
                }else if(path.contains("/loja")){
                    return false;
                }else if(path.contains("/post")){
                    return false;
                }else if (path.contains("/promos")) {
                    return false;
                }else if(path.contains("/mensagem")){

                }
            case "PUT":
                if (path.contains("/produto")) {
                    return false;
                }else if(path.contains("/categoria")){
                    return false;
                }else if(path.contains("/loja")){
                    return false;
                }else if(path.contains("/post")){
                    return false;
                }else if(path.contains("/banners")){
                    return false;
                }else if(path.contains("/promos")){
                    return false;
                }
            case "DELETE":
                if (path.contains("/produto")) {
                    return false;
                }else if(path.contains("/categoria")){
                    return false;
                }else if(path.contains("/report")){
                    return false;
                }else if(path.contains("/loja")){
                    return false;
                }else if(path.contains("/post")){
                    return false;
                }else if(path.contains("/banners")){
                    return false;
                }else if(path.contains("/promos")){
                    return false;
                }
        }

        return true;
    }
    
}
