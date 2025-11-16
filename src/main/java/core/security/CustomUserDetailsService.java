package core.security;

import core.entity.UserEntity;
import core.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByName(username);

        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userEntity.getRole());

        return new User(
                userEntity.getName(),
                userEntity.getPasswordHash(),
                Collections.singletonList(authority)
        );
    }
}