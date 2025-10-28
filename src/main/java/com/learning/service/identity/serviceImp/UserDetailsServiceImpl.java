package com.learning.service.identity.serviceImp;

import com.learning.customException.UserNotFoundException;
import com.learning.dbentity.identity.User;
import com.learning.repository.identity.UserRepository;
import com.learning.security.UserPrincipal;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username).orElseThrow(() -> new UserNotFoundException(username));
        return UserPrincipal.build(user);
    }
}
