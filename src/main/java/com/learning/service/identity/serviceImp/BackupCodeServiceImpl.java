package com.learning.service.identity.serviceImp;

import com.learning.customException.IdentityAppExcpetion;
import com.learning.customException.UserNotFoundException;
import com.learning.dbentity.identity.User;
import com.learning.dbentity.identity.User2FA;
import com.learning.repository.identity.User2FARepository;
import com.learning.repository.identity.UserRepository;
import com.learning.service.identity.OTPService;
import com.learning.utility.identity.TwoFactorAuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service("BACKUP")
public class BackupCodeServiceImpl implements OTPService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private User2FARepository user2FARepository;

    @Override
    public String generateLoginOtp(Long userId, String deviceId) {
        throw new IdentityAppExcpetion("Not a valid method to authenticate user", HttpStatus.BAD_REQUEST);
    }

    @Override
    public boolean verifyLoginAttempt(Long userId, String otp) {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(userId));
        User2FA user2FA = user2FARepository.findByUser(user).orElseThrow(() -> new IdentityAppExcpetion("2FA not enabled for user", HttpStatus.BAD_REQUEST));
        String backupCodes = user2FA.getBackupCodes();
        if(backupCodes == null){
            throw new IdentityAppExcpetion("Can't authenticate user using backup codes", HttpStatus.BAD_REQUEST);
        }
        boolean verify = false;
        List<String> codes = new ArrayList<>(TwoFactorAuthUtil.parseBackupCodes(backupCodes));
        if(!codes.isEmpty()){
            verify = codes.contains(otp);
            codes.remove(otp);
        }
        user2FA.setBackupCodes(TwoFactorAuthUtil.convertBackupCodesToJson(codes));
        user2FARepository.save(user2FA);
        return verify;
    }

    @Override
    public boolean isValidFormat(String otp) {
        return false;
    }

    @Override
    public void deleteAllExpiredOtp() {

    }
}
