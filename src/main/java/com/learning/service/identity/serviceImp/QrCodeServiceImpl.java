package com.learning.service.identity.serviceImp;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.learning.service.identity.QrCodeService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class QrCodeServiceImpl implements QrCodeService {
    @Override
    public String generateTOTPQRCode(String appName, String username, String secretKey) {
        String qrCodeUrl = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", appName, username, secretKey, appName);
        return generateQRCodeBase64(qrCodeUrl, 200, 200);
    }

    /**
     * Generate QR code as Base64 string
     * @param text
     * @param width
     * @param height
     * @return
     */
    // TODO : why base 64 encoding needed here
    private String generateQRCodeBase64(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (WriterException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
