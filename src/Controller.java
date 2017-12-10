import Shamir.Shamir;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.image.PixelWriter;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.TextField;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    @FXML
    private Button encryptButton;
    @FXML
    private Button decryptButton;

    private SecureRandom secureRandom;
    private BigInteger key;
    private Image image = null;
    private Shamir shamirSystem;

    private int t;
    private int n;
    private byte[] imageBytes;

    @Override
    public void initialize(URL location, final ResourceBundle resources) {
        secureRandom = new SecureRandom();
    }

    private void encryptAction() {
        System.out.println("Encrypt button was pressed!");
        shamirSystem = new Shamir(Integer.parseInt(tNumber.getText()), Integer.parseInt(nNumber.getText()));
        if(key == null)
            generateNewKeyButton();
        System.out.println(key.intValue());
        ArrayList<Shamir.SecretShare> shares = shamirSystem.split(key);
        shamirSystem.saveShares("./keys/");
        shamirSystem.savePrime("./keys/");
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        byte[] pixels = extractBytes(getImageWithoutTransparency(bufferedImage));
        SecretKey secretKey = new SecretKeySpec(this.key.toByteArray(), 0, this.key.toByteArray().length, "AES");
        pixels = encrypt(pixels, secretKey);
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
        ArrayList<Shamir.SecretShare> shares = shamirSystem.loadShares("keys/");
        BigInteger prime = shamirSystem.loadPrime("keys/");
        this.key = shamirSystem.combine(shares, prime);
        System.out.println(key.intValue());
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
        if (image != null) {
            KeyGenerator keyGen = null;
            try {
                keyGen = KeyGenerator.getInstance("AES");
                SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
                keyGen.init(256, random);
                SecretKey key = keyGen.generateKey();
                this.key = new BigInteger(key.getEncoded());
                encryptAction();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            updateKeyImage();
        }
    }

    private void updateKeyImage() {
        int width = 8, height = 4;
        createKeyGraphicalRepresentation(key, width, height);
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

    @FXML
    private void helpButton() {
        System.out.println("Help button was pressed");
    }

    private byte[] extractBytes(BufferedImage image) {
        WritableRaster raster = image.getRaster();
        DataBufferByte data = (DataBufferByte)raster.getDataBuffer();
        return data.getData();
    }

    private void createKeyGraphicalRepresentation(BigInteger key, int width, int height) {
        PixelWriter writer = keyImage.getGraphicsContext2D().getPixelWriter();
        for(int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                int currentBit = x * 16 + y;
                if (key.testBit(currentBit)) {
                    drawPixel(writer, x, y, javafx.scene.paint.Color.rgb(255, 255, 255));
                } else {
                    drawPixel(writer, x, y, javafx.scene.paint.Color.rgb(0, 0, 0));
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

    private static byte[] encrypt(byte[] data, SecretKey key) {
        byte[] encrypted = null;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encrypted = cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            System.out.println(e.getMessage());
        }
        return encrypted;
    }

    private static byte[] decrypt(byte[] data, SecretKey key) {
        byte[] decrypted = null;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            decrypted = cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            System.out.println(e.getMessage());
        }
        return decrypted;
    }
}
