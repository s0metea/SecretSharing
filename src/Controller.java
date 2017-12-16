import Shamir.Shamir;
import Shamir.SecretShare;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.image.PixelWriter;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.scene.control.TextField;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private GridPane pane;
    @FXML
    private ImageView imageOriginal;
    @FXML
    private Canvas keyImage;
    @FXML
    private ImageView imageEncrypted;
    @FXML
    private TextField tNumber;
    @FXML
    private TextField nNumber;

    private BigInteger key = null;
    private Image image = null;
    private Shamir shamirSystem;

    private int t;
    private int n;
    private byte[] imageBytes;

    @Override
    public void initialize(URL location, final ResourceBundle resources) {
    }

    private void encryptAction() {
        System.out.println("Encrypt button was pressed!");
        shamirSystem = new Shamir(Integer.parseInt(tNumber.getText()), Integer.parseInt(nNumber.getText()));

        ArrayList<SecretShare> shares = shamirSystem.split(key);
        shamirSystem.saveShares("./keys/");
        shamirSystem.savePrime("./keys/");

        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        byte[] pixels = extractBytes(getImageWithoutTransparency(bufferedImage));
        SecretKey secretKey = new SecretKeySpec(this.key.toByteArray(), 0, this.key.toByteArray().length, "AES");
        pixels = encrypt(pixels, secretKey);
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage encryptedBufferedImage = createRGBImage(pixels, width, height);
        try {
            System.out.println("Encrypted file was successfully saved.");
            File outputFile = new File("./encrypted.png");
            ImageIO.write(encryptedBufferedImage, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        imageEncrypted.setImage(SwingFXUtils.toFXImage(encryptedBufferedImage, null));
    }

    @FXML
    private void decryptAction() {
        System.out.println("Decrypt button was pressed!");
        shamirSystem = new Shamir(Integer.parseInt(tNumber.getText()), Integer.parseInt(nNumber.getText()));
        ArrayList<SecretShare> shares = shamirSystem.loadShares("keys/");
        BigInteger prime = shamirSystem.loadPrime("keys/");
        shamirSystem.setPrime(prime);
        this.key = shamirSystem.combine(shares, prime);
        System.out.println("Combined key: " + this.key);
        updateKeyImage();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        byte[] pixels = extractBytes(getImageWithoutTransparency(bufferedImage));
        SecretKey secretKey = new SecretKeySpec(this.key.toByteArray(), 0, this.key.toByteArray().length, "AES");
        pixels = decrypt(pixels, secretKey);
        BufferedImage decryptedBufferedImage = createRGBImage(pixels, width, height);
        try {
            System.out.println("Decrypted file was successfully saved.");
            File outputFile = new File("./decrypted.png");
            ImageIO.write(decryptedBufferedImage, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        imageOriginal.setImage(SwingFXUtils.toFXImage(decryptedBufferedImage, null));
    }


    @FXML
    private void generateNewKeyButton() {
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.init(256, random);
            SecretKey key = keyGen.generateKey();
            this.key = new BigInteger(key.getEncoded()).abs();
            shamirSystem = new Shamir(Integer.parseInt(tNumber.getText()), Integer.parseInt(nNumber.getText()));
            ArrayList<SecretShare> shares = shamirSystem.split(this.key);
            shamirSystem.saveShares("./keys/");
            shamirSystem.savePrime("./keys/");
            image = imageOriginal.getImage();
            System.out.println("Key: " + this.key);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        updateKeyImage();
        encryptAction();
    }


    private void updateKeyImage() {
        createKeyGraphicalRepresentation();
    }


    @FXML
    private void loadOriginalButton() {
        Stage stage = (Stage) imageOriginal.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open image to encrypt:");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            imageOriginal.setImage(image);
            this.image = image;
            generateNewKeyButton();
        }
    }

    @FXML
    private void loadEncryptedButton() {
        Stage stage = (Stage) imageEncrypted.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open image to decrypt:");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            imageEncrypted.setImage(image);
            this.image = image;
            decryptAction();
        }
    }

    private byte[] extractBytes(BufferedImage image) {
        WritableRaster raster = image.getRaster();
        DataBufferByte data = (DataBufferByte)raster.getDataBuffer();
        return data.getData();
    }

    private void createKeyGraphicalRepresentation() {
        PixelWriter writer = keyImage.getGraphicsContext2D().getPixelWriter();
        for(int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                int currentBit = x * 16 + y;
                if (key.testBit(currentBit)) {
                    drawPixel(writer, x, y, javafx.scene.paint.Color.rgb(0 , 0, 128));
                } else {
                    drawPixel(writer, x, y, javafx.scene.paint.Color.rgb(255, 255, 255));
                }
            }
        }
    }

    private void drawPixel(PixelWriter writer, int x, int y, javafx.scene.paint.Color color) {
        int xEnd = x * 10 + 10;
        int yEnd = y * 10 + 10;
        for(int currentX = x * 10; currentX <= xEnd; currentX++)
            for(int currentY = y * 10; currentY <= yEnd; currentY++) {
                writer.setColor(currentX, currentY, color);
            }
    }

    private BufferedImage createRGBImage(byte[] bytes, int width, int height) {
        System.out.println("Bytes length of image: " + bytes.length);
        DataBufferByte buffer = new DataBufferByte(bytes, bytes.length);
        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        return new BufferedImage(cm, Raster.createInterleavedRaster(buffer, width, height, width * 3, 3, new int[]{2, 1, 0}, null), false, null);
    }

    private BufferedImage getImageWithoutTransparency(BufferedImage image){
        BufferedImage converted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = converted.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return converted;
    }

    private byte[] encrypt(byte[] data, SecretKey key) {
        System.out.println("Key for encryption: " + this.key);
        byte[] encrypted = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            byte[] iv = new byte[cipher.getBlockSize()];
            AlgorithmParameterSpec spec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            encrypted = cipher.doFinal(data);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            System.out.println(e.getMessage());
        }
        return encrypted;
    }

    private byte[] decrypt(byte[] data, SecretKey key) {
        byte[] decrypted = null;
        System.out.println("Size before decryption: " + data.length);
        System.out.println("Key for decryption: " + this.key);
        try {
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            byte[] iv = new byte[cipher.getBlockSize()];
            AlgorithmParameterSpec spec = new IvParameterSpec(iv);
            // decrypt the message
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            decrypted = cipher.doFinal(data);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            System.out.println(e.getMessage());
        }
        return decrypted;
    }
}
