package com.learning.service.identity.serviceImp;

import com.learning.customException.IdentityAppExcpetion;
import com.learning.customException.UserNotFoundException;
import com.learning.dbentity.identity.User;
import com.learning.dbentity.identity.User2FA;
import com.learning.repository.identity.User2FARepository;
import com.learning.repository.identity.UserRepository;
import com.learning.service.identity.OTPService;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;

@Service("TOTP")
public class TotpServiceImpl implements OTPService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private User2FARepository user2FARepository;

    private static final String TOTP_ALGORITHM = "HmacSHA1";
    private static final int TIME_STEP = 30; // 30 seconds
    private static final int CODE_DIGITS = 6;
    private static final int SECRET_KEY_LENGTH = 20;

    /**
     * Generate a new secret key for totp
     * @return
     */
    public String generateSecretKey(){
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[CODE_DIGITS];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeAsString(bytes).replace("=","");
    }

    @Override
    public String generateLoginOtp(Long userId, String deviceId) {
        return "Enter the code from your authenticator app";
    }

    /**
     * Verify TOTP code with time drift tolerance
     */
    @Override
    public boolean verifyLoginAttempt(Long userId, String otp) {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(userId));
        User2FA user2FA = user2FARepository.findByUser(user).orElseThrow(() -> new IdentityAppExcpetion("2FA not enabled for user", HttpStatus.BAD_REQUEST));
        String secretKey = user2FA.getSecretKey();
        if(secretKey == null || otp == null || otp.length() != CODE_DIGITS)return false;
        long currentTimeStamp = Instant.now().getEpochSecond();
        // Allow time drift of ±1 time step (30 seconds before/after)
        for (int i = -1; i <= 1; i++) {
            String expectedCode = generateTOTP(secretKey, currentTimeStamp + i);
            if (expectedCode.equals(otp)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValidFormat(String code) {
        return code.length() == CODE_DIGITS && code.chars().allMatch(Character::isDigit);
    }

    @Override
    public void deleteAllExpiredOtp() {

    }

    /**
     * Generate TOTP code for a specific time step
     */
    public String generateTOTP(String secretKey, long timeStep) {
        try {
            // Decode Base32 secret key
            Base32 base32 = new Base32();
            byte[] keyBytes = base32.decode(secretKey);

            // Convert time step to bytes (big-endian)
            byte[] timeBytes = new byte[8];
            for (int i = 8; i-- > 0; timeStep >>>= 8) {
                timeBytes[i] = (byte) timeStep;
            }

            // Compute HMAC-SHA1
            SecretKeySpec signKey = new SecretKeySpec(keyBytes, TOTP_ALGORITHM);
            Mac mac = Mac.getInstance(TOTP_ALGORITHM);
            mac.init(signKey);
            byte[] hash = mac.doFinal(timeBytes);

            // Dynamic truncation
            int offset = hash[hash.length - 1] & 0xF;

            // Extract 4-byte dynamic binary code
            long binary = ((hash[offset] & 0x7F) << 24) |
                    ((hash[offset + 1] & 0xFF) << 16) |
                    ((hash[offset + 2] & 0xFF) << 8) |
                    (hash[offset + 3] & 0xFF);

            // Generate 6-digit code
            long otp = binary % (long) Math.pow(10, CODE_DIGITS);
            return String.format("%0" + CODE_DIGITS + "d", otp);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("TOTP generation failed", e);
        }
    }


    /**
     * Generate QR Code URL for authenticator apps
     */
    public String generateQRCodeURL(String appName, String username, String secretKey){
        String format = "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30";
        return String.format(format, appName, username, secretKey, appName);
    }

    /**
     * Get remaining seconds in current time step
     */
    public int getRemainingSeconds() {
        long currentTime = Instant.now().getEpochSecond();
        return (int) (TIME_STEP - (currentTime % TIME_STEP));
    }

    /**
     * Get current time step (for debugging)
     */
    public long getCurrentTimeStep() {
        return Instant.now().getEpochSecond() / TIME_STEP;
    }


}
