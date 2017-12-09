import Shamir.Shamir;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextArea;

import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.TextField;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private GridPane pane;
    @FXML
    private ImageView imageOriginal;
    @FXML
    private ImageView imageKey;
    @FXML
    private ImageView imageEncrypted;
    @FXML
    private TextField tNumber;
    @FXML
    private TextField nNumber;
    @FXML
    private TextArea pNumber;
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
        Shamir.SecretShare[] shares = shamirSystem.split(key);
        pNumber.setText(shamirSystem.getPrime().toString());
        shamirSystem.saveShares("./keys/");
        PixelReader pr = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        byte[] pixels = new byte[width * height * 4];
        pr.getPixels(0, 0, width, height, WritablePixelFormat.getByteBgraInstance(), pixels, 0, width * 4);
        //Here we need to encrypt our bytes with key using AES
        imageEncrypted.setImage(SwingFXUtils.toFXImage(createRGBImage(pixels,
                (int) image.getWidth(),
                (int) image.getHeight()), null));
    }

    @FXML
    private void decryptButton() {
        System.out.println("Decrypt button ws pressed!");
        shamirSystem = new Shamir(Integer.parseInt(tNumber.getText()), Integer.parseInt(nNumber.getText()));
        Shamir.SecretShare[] shares = shamirSystem.loadShares("keys/");
        key = shamirSystem.combine(shares, new BigInteger(pNumber.getText().getBytes()));
        PixelReader pr = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        byte[] pixels = new byte[width * height * 4];
        pr.getPixels(0, 0, width, height, WritablePixelFormat.getByteBgraInstance(), pixels, 0, width * 4);
        //Here we need to decrypt our image with AES key
        imageOriginal.setImage(SwingFXUtils.toFXImage(createRGBImage(pixels,
                (int) image.getWidth(),
                (int) image.getHeight()), null));
    }


    @FXML
    private void generateNewKeyButton() {
        if (image != null) {
            int width = 8, height = 8;
            key = new BigInteger(256 * 8, secureRandom);
            BufferedImage secretImage = createRGBImage(key.toByteArray(), width, height);
            imageKey.setImage(SwingFXUtils.toFXImage(secretImage, null));
        }
    }

    @FXML
    private void loadOriginalButton() {
        Stage stage = (Stage) imageOriginal.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open image to encrypt:");
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


    private BufferedImage createRGBImage(byte[] bytes, int width, int height) {
        DataBufferByte buffer = new DataBufferByte(bytes, bytes.length);
        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        return new BufferedImage(cm, Raster.createInterleavedRaster(buffer, width, height, width * 3, 3, new int[]{0, 1, 2}, null), false, null);
    }

}
