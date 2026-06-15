package com.parcinformatique.app.security;

import com.parcinformatique.app.exception.ResourceNotFoundException;
import com.parcinformatique.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findWithSecurityByEmailIgnoreCase(username)
            .map(UserPrincipal::new)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }
}
