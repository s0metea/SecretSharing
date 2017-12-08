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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Random;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    @FXML
    private GridPane pane;
    @FXML
    private Label selectLabel;
    @FXML
    private ImageView imageToEncrypt;
    @FXML
    private ImageView imageEncrypted;
    @FXML
    private Button openImageButton;
    @FXML
    private Label key;

    private int t;
    private int n;
    private byte[] imageBytes;

    @Override
    public void initialize(URL location, final ResourceBundle resources) {
        imageToEncrypt.setImage(new Image("add.png"));
    }

    @FXML
    private void loadImageButton() {
        Stage stage = (Stage) imageToEncrypt.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open image to encrypt:");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            imageToEncrypt.setImage(image);
            try {
                //imageBytes = getImageBytes(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void encryptButton() {
        byte[] res  = this.imageBytes;
        final Shamir shamir = new Shamir(t, n);
        SecureRandom random = new SecureRandom();
        final BigInteger secret = new BigInteger(imageBytes.length, random);
        final BigInteger image = new BigInteger(res);
        final BigInteger encryptedBytes = image.xor(secret);

        final Shamir.SecretShare[] shadows = shamir.split(secret);
        final BigInteger prime = shamir.getPrime();

        BufferedImage img = null;

        try {
            img = ImageIO.read(new ByteArrayInputStream(encryptedBytes.toByteArray()));
            imageEncrypted.setImage(SwingFXUtils.toFXImage(img, null));
        } catch (IOException e) {
            e.printStackTrace();
        }


        // get DataBufferBytes from Raster


        final Shamir shamir2 = new Shamir(t, n);

        final BigInteger result = shamir2.combine(shadows, prime);
    }

    @FXML
    private void cancelButton() {
    }

}
