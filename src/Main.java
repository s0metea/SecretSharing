import Shamir.Shamir;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.math.BigInteger;
import java.util.ArrayList;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Image Encryption using Shamir's Secret Sharing");
        primaryStage.setScene(new Scene(root, 800, 300));
        primaryStage.setResizable(false);
        primaryStage.show();
//        Checking Shamir algorithm:
//        Shamir shamir = new Shamir(3, 5);
//        String str = "Hello world!";
//        BigInteger secret = new BigInteger(str.getBytes());
//
//        shamir.split(secret);
//        shamir.saveShares("./keys/");
//        shamir.savePrime("./keys/");
//
//        Shamir shamir2 = new Shamir(3, 5);
//        BigInteger prime = shamir2.loadPrime("./keys/");
//        ArrayList<Shamir.SecretShare> shares = shamir2.loadShares("./keys/");
//        shares.remove(8000);
//        shares.remove(1);
//        BigInteger combined = shamir2.combine(shares, prime);
//        String str2 = new String(combined.toByteArray());
//        System.out.println(strprime);
//        System.out.println(str2);

    }


    public static void main(String[] args) {
        launch(args);
    }
}
