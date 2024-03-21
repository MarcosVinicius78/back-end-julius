package com.julius.julius.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.julius.julius.models.auth.Authority;
import com.julius.julius.models.auth.Usuario;
import com.julius.julius.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AutheticationProvider implements AuthenticationProvider{

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String senha = authentication.getCredentials().toString();

        List<Usuario> user = usuarioRepository.findAll();

        if (!user.isEmpty()) {
        
            if (passwordEncoder.matches(senha,user.get(0).getSenha()) && email.equals(user.get(0).getEmail())) {
                return new UsernamePasswordAuthenticationToken(email, senha, grantedAuthorities(user.get(0).getAuthorities()));
            }else{
                throw new BadCredentialsException("E-mail ou senha errado");
            }
            
        }else{

            Usuario usuarioSalvo = Usuario.builder()
                                        .email(email)
                                        .senha(passwordEncoder.encode(senha))
                                        .authorities(new ArrayList<>())
                                        .build();
            
            usuarioRepository.save(usuarioSalvo);

            return new UsernamePasswordAuthenticationToken(email, senha, grantedAuthorities(usuarioSalvo.getAuthorities()));
        }
    }

    private List<GrantedAuthority> grantedAuthorities(List<Authority> authorities){
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        for (Authority authority : authorities) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.getNome()));
        }

        return grantedAuthorities;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
    
}
