package Shamir;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

public final class Shamir {

    private BigInteger prime;
    private final int t;
    private final int n;
    private final Random random;
    private ArrayList<SecretShare> currentShares;
    private static final int CERTAINTY = 100;

    public Shamir(final int t, final int n) {
        this.t = t;
        this.n = n;
        random = new Random();
    }

    public ArrayList<SecretShare> split(final BigInteger secret) {
        final BigInteger[] coefficients = new BigInteger[t - 1];
        final int modLength = secret.bitLength() + 1;

        prime = new BigInteger(modLength, CERTAINTY, random).abs();

        for (int i = 0; i < t - 1; i++) {
            coefficients[i] = generateRandomCoefficient(prime);
        }

        final ArrayList<SecretShare> shadows = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            BigInteger shadow = secret;
            for (int j = 1; j < t; j++) {
                final BigInteger t1 = BigInteger.valueOf(i).modPow(BigInteger.valueOf(j), prime);
                final BigInteger t2 = coefficients[j - 1].multiply(t1).mod(prime);
                shadow = shadow.add(t2).mod(prime);
            }
            shadows.add(new SecretShare(i, shadow));
        }
        currentShares = shadows;
        return shadows;
    }

    public BigInteger combine(final ArrayList<SecretShare> choseShadows, final BigInteger primeNumber) {
        BigInteger sum = BigInteger.ZERO;
        System.out.println("Shadows array size = " + choseShadows.size());
        for (int i = 0; i < choseShadows.size(); i++) {

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < choseShadows.size(); j++) {
                if (i != j) {
                    numerator = numerator.multiply(BigInteger.valueOf(-choseShadows.get(j).getX())).mod(primeNumber);
                    denominator = denominator.multiply(BigInteger.valueOf(choseShadows.get(i).getX() - choseShadows.get(j).getX())).mod(primeNumber);
                }
            }
            final BigInteger value = choseShadows.get(i).getShadow();
            final BigInteger tmp = value.multiply(numerator).multiply(denominator.modInverse(primeNumber)).mod(primeNumber);
            sum = sum.add(tmp).mod(primeNumber);
        }
        return sum;
    }

    private BigInteger generateRandomCoefficient(final BigInteger p) {
        while (true) {
            final BigInteger r = new BigInteger(p.bitLength(), random);
            if (r.compareTo(BigInteger.ZERO) > 0 && r.compareTo(p) < 0) {
                return r;
            }
        }
    }

    public BigInteger getPrime() {
        return prime;
    }

    public void setPrime(BigInteger prime) {
        this.prime = prime;
    }


    public void saveShares(String path) {
        if (currentShares != null) {
            for (SecretShare currentShare : currentShares) {
                try {
                    FileOutputStream fos = new FileOutputStream(path + currentShare.getX());
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(currentShare);
                    oos.close();
                    System.out.println("Shadow for x = " + currentShare.getX() + " with value = " + currentShare.getShadow() + " was saved");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public ArrayList<SecretShare> loadShares(String path) {
        ArrayList<SecretShare> shares = new ArrayList<>();
        for (int i = 0; i < this.n; i++) {
            String fullPath = path + (i + 1);
            FileInputStream fis;
            ObjectInputStream ois;
            if (new File(fullPath).exists()) {
                try {
                    fis = new FileInputStream(fullPath);
                    ois = new ObjectInputStream(fis);
                    SecretShare shadow = ((SecretShare) ois.readObject());
                    shares.add(shadow);
                    System.out.println("Shadow for x = " + shadow.getX() + " with value = " + shadow.getShadow() + " was loaded");
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        System.out.println("Total shares loaded: " + shares.size());
        return shares;
    }

    public void savePrime(String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path + "prime");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(prime);
            oos.close();
            System.out.println("Prime was succesfully saved!");
            System.out.println(prime);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public BigInteger loadPrime(String path) {
        BigInteger loadedPrime = null;
        try {
            FileInputStream fis = new FileInputStream(path + "prime");
            ObjectInputStream ois = new ObjectInputStream(fis);
            loadedPrime = ((BigInteger) ois.readObject());
            System.out.println("Prime was loaded from file");
            System.out.println(loadedPrime);
        } catch (IOException e) {
            System.out.println("Prime wasn't found!");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Prime file corrupted!");
        }
        if(loadedPrime == null)
            System.out.println("Null prime in loadPrime method");
        return loadedPrime;
    }
}