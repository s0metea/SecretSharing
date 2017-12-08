import Shamir.Shamir;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
    private TextField pNumber;
    @FXML
    private Button encryptButton;
    @FXML
    private Button decryptButton;

    private SecureRandom secureRandom;
    private BigInteger key;
    private Image image = null;

    private int t;
    private int n;
    private byte[] imageBytes;

    @Override
    public void initialize(URL location, final ResourceBundle resources) {
        secureRandom = new SecureRandom();
    }

    @FXML
    private void encryptButton() {
//        byte[] res  = this.imageBytes;
//        final Shamir shamir = new Shamir(t, n);
//        final BigInteger secret = new BigInteger(800 * 600, random);
//        final BigInteger image = new BigInteger(res);
//        final BigInteger encryptedBytes = image.xor(secret);
//        final Shamir.SecretShare[] shadows = shamir.split(secret);
//        final BigInteger prime = shamir.getPrime();
//
 //       BufferedImage img = null;
 //       BufferedImage secretImage = createRGBImage(secret.toByteArray(), (int) imageEncrypted.getX(), (int) imageEncrypted.getY());
//        try {
//            img = ImageIO.read(new ByteArrayInputStream(secret.toByteArray()));
//            imageEncrypted.setImage(SwingFXUtils.toFXImage(img, null));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
      //  imageKey.setImage(SwingFXUtils.toFXImage(secretImage, null));
    }

    @FXML
    private void generateNewKey() {
        if(image != null) {
            key = new BigInteger((int) image.getWidth() * (int) image.getHeight() * 1000, secureRandom);
            BufferedImage secretImage = createRGBImage(key.toByteArray(), (int) image.getWidth(), (int) image.getHeight());
            imageKey.setImage(SwingFXUtils.toFXImage(secretImage, null));
        }
    }

    @FXML
    private void cancelButton() {
    }

    @FXML
    private void loadOriginal() {
        Stage stage = (Stage) imageOriginal.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open image to encrypt:");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            imageOriginal.setImage(image);
            this.image = image;
            generateNewKey();
            final BigInteger encryptedBytes = image.getBytes().xor(key);
            imageEncrypted.setImage(SwingFXUtils.toFXImage(createRGBImage(encryptedBytes.toByteArray(),
                                                                            (int) image.getWidth(),
                                                                            (int) image.getHeight()), null));

        }
    }


    @FXML
    private void loadEncrypted() {
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

    private BufferedImage createRGBImage(byte[] bytes, int width, int height) {
        DataBufferByte buffer = new DataBufferByte(bytes, bytes.length);
        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        return new BufferedImage(cm, Raster.createInterleavedRaster(buffer, width, height, width * 3, 3, new int[]{0, 1, 2}, null), false, null);
    }
}
