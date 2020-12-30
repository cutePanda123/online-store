package com.imooc.seckill.utils;

import com.google.common.io.FileBackedOutputStream;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.List;

public class VerificationCodeUtil {
    private static int width = 90;
    private static int height = 20;
    private static int codeCount = 4;
    private static int leftOffset = 15;
    private static int topOffset = 16;
    private static int fontSize = 18;
    private static List<Character> codes = new ArrayList<>();

    public static Map<String, Object> generateQrCode() {
        for (char c = 'A'; c <= 'Z'; ++c) {
            codes.add(c);
        }
        for (char c = '0'; c <= '9'; ++c) {
            codes.add(c);
        }
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = bufferedImage.getGraphics();
        Random random = new Random();

        // draw background
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        Font font = new Font("verification-code-font", Font.BOLD, fontSize);
        graphics.setFont(font);

        // draw edges
        graphics.setColor(Color.BLACK);
        graphics.drawRect(0, 0, width - 1, height - 1);

        // generate random lines to avoid being detected by bot
        graphics.setColor(Color.BLACK);
        for (int i = 0; i < 30; ++i) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xOffset = random.nextInt(12);
            int yOffset = random.nextInt(12);
            graphics.drawLine(x, y, x + xOffset, y + yOffset);
        }
        StringBuffer codeStr = new StringBuffer();
        for (int i = 0; i < codeCount; ++i) {
            String curChar = String.valueOf(codes.get(random.nextInt(codes.size())));
            int red = random.nextInt(255);
            int blue = random.nextInt(255);
            int green = random.nextInt(255);
            graphics.setColor(new Color(red, green, blue));
            graphics.drawString(curChar, (i + 1) * leftOffset, topOffset);
            codeStr.append(curChar);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("code", codeStr.toString());
        map.put("picture", bufferedImage);
        return map;
    }

    public static void main(String[] args) throws IOException {
        OutputStream outputStream = new FileOutputStream("./code-example.jpeg");
        Map<String, Object> map = VerificationCodeUtil.generateQrCode();
        ImageIO.write((RenderedImage) map.get("picture"), "jpeg", outputStream);
        System.out.println((String)map.get("code"));
    }
}
