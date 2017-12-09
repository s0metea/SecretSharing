import Shamir.Shamir;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.TextField;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private GridPane pane;
    @FXML
    private ImageView imageOriginal;
    @FXML
    private ImageView keyImage;
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

    @FXML
    private void encryptButton() {
        System.out.println("Encrypt button was pressed!");
        shamirSystem = new Shamir(Integer.parseInt(tNumber.getText()), Integer.parseInt(nNumber.getText()));
        if(key == null)
            generateNewKeyButton();
        ArrayList<Shamir.SecretShare> shares = shamirSystem.split(key);
        shamirSystem.saveShares("./keys/");
        shamirSystem.savePrime("./keys/prime");
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        byte[] pixels = extractBytes(getImageWithoutTransparency(bufferedImage));
        //Here we need to encrypt our bytes with key using AES like:
        //pixels = AES.encrypt(key)
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
    private void decryptButton() {
        System.out.println("Decrypt button ws pressed!");
        shamirSystem = new Shamir(Integer.parseInt(tNumber.getText()), Integer.parseInt(nNumber.getText()));
        ArrayList<Shamir.SecretShare> shares = shamirSystem.loadShares("keys/");
        BigInteger prime = shamirSystem.loadPrime("keys/prime");
        key = shamirSystem.combine(shares, prime);
        updateKeyImage();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        byte[] pixels = extractBytes(getImageWithoutTransparency(bufferedImage));
        //Here we need to decrypt our image with AES key like:
        //pixels = AES.decrypt(key)
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
            key = new BigInteger(256 * 8, secureRandom);
            updateKeyImage();
        }
    }

    private void updateKeyImage() {
        int width = 8, height = 8;
        BufferedImage keyImage = createRGBImage(key.toByteArray(), width, height);
        this.keyImage.setImage(SwingFXUtils.toFXImage(keyImage, null));
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

    private static BufferedImage createRGBImage(byte[] bytes, int width, int height) {
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
}
