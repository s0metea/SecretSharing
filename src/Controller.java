import Shamir.Shamir;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextArea;

import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.TextField;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
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
    private void decryptButton() {

    }


    @FXML
    private void generateNewKey() {
        if (image != null) {
            int width = 8, height = 8;
            key = new BigInteger(256 * 8, secureRandom);
            BufferedImage secretImage = createRGBImage(key.toByteArray(), width, height);
            imageKey.setImage(SwingFXUtils.toFXImage(secretImage, null));
        }
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
//          imageEncrypted.setImage(SwingFXUtils.toFXImage(createRGBImage(encryptedBytes.toByteArray(),
//                                                                            (int) image.getWidth(),
//                                                                            (int) image.getHeight()), null));
            shamirSystem = new Shamir(Integer.parseInt(tNumber.getText()), Integer.parseInt(nNumber.getText()));
            Shamir.SecretShare[] shares = shamirSystem.split(key);
            pNumber.setText(shamirSystem.getPrime().toString());
            System.out.println(shares.length);
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

    @FXML
    private void helpButton() {
        System.out.println("Help button was pressed");
    }


    private BufferedImage createRGBImage(byte[] bytes, int width, int height) {
        DataBufferByte buffer = new DataBufferByte(bytes, bytes.length);
        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        return new BufferedImage(cm, Raster.createInterleavedRaster(buffer, width, height, width * 3, 3, new int[]{0, 1, 2}, null), false, null);
    }


    private void saveToFile(byte[] file, String fileName){

    }

    private byte[] loadFromFile(String fileName){
        return new byte[1];
    }

}
