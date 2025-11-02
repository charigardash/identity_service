package com.learning.utility.identity;

import java.util.*;
import java.util.stream.Collectors;

public class TwoFactorAuthUtil {


    public static String generateSecretKey() {
        // In production, use a proper TOTP secret generation
        return Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes()).substring(0, 32);
    }

    public static List<String> generateBackupCodes(){
        List<String> codes = new ArrayList<>();
        for(int i=0;i<10;i++){
            codes.add(String.format("%08d", new Random().nextInt(1000000000)));
        }
        return codes;
    }

    public static String convertBackupCodesToJson(List<String> codes) {
        return "[\"" + String.join("\", \"", codes) + "\"]";
    }

    public static List<String> parseBackupCodes(String jsonBackupCode){
        if(jsonBackupCode == null || jsonBackupCode.trim().isEmpty()){
            return new ArrayList<>();
        }
        String clean = jsonBackupCode.replaceAll("[\\[\\]\"]", "");
        return Arrays.stream(clean.split(",")).map(String::trim).toList();
    }
}
