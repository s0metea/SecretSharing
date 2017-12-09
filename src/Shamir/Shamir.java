package Shamir;

import java.io.*;
import java.math.BigInteger;
import java.util.Random;

public final class Shamir {

    private BigInteger prime;

    private final int t;
    private final int n;
    private final Random random;
    private SecretShare[] currentShares;

    private static final int CERTAINTY = 50;

    public Shamir(final int t, final int n) {
        this.t = t;
        this.n = n;

        random = new Random();
    }

    public SecretShare[] split(final BigInteger secret) {
        final BigInteger[] coefficients = new BigInteger[t - 1];
        final int modLength = secret.bitLength() + 1;

        prime = new BigInteger(modLength, CERTAINTY, random);

        for (int i = 0; i < t - 1; i++) {
            coefficients[i] = generateRandomCoefficient(prime);
        }

        final SecretShare[] shadows = new SecretShare[n];
        for (int i = 1; i <= n; i++) {
            BigInteger shadow = secret;
            for (int j = 1; j < t; j++) {
                final BigInteger t1 = BigInteger.valueOf(i).modPow(BigInteger.valueOf(j), prime);
                final BigInteger t2 = coefficients[j - 1].multiply(t1).mod(prime);
                shadow = shadow.add(t2).mod(prime);
            }
            shadows[i - 1] = new SecretShare(i, shadow);
        }
        currentShares = shadows;
        return shadows;
    }

    public BigInteger combine(final SecretShare[] choseShadows, final BigInteger primeNumber) {
        BigInteger sum = BigInteger.ZERO;
        for (int i = 0; i < t; i++) {

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < t; j++) {
                if (i != j) {
                    numerator = numerator.multiply(BigInteger.valueOf(-choseShadows[j].getX())).mod(primeNumber);
                    denominator = denominator.multiply(BigInteger.valueOf(choseShadows[i].getX() - choseShadows[j].getX())).mod(primeNumber);
                }
            }

            final BigInteger value = choseShadows[i].getShadow();

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

    public final class SecretShare {

        private final int x;
        private final BigInteger shadow;

        public SecretShare(final int x, final BigInteger shadow) {
            this.x = x;
            this.shadow = shadow;
        }

        public int getX() {
            return x;
        }
        public BigInteger getShadow() {
            return shadow;
        }
    }

    public BigInteger getPrime() {
        return prime;
    }
    public void setPrime(BigInteger prime) { this.prime = prime; }


    public void saveShares(String path) {
        if (currentShares != null) {
            for (int i = 1; i < currentShares.length; i++) {
                Shamir.SecretShare currentShare = currentShares[i];
                try {
                    FileOutputStream fos = new FileOutputStream(path + i);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(currentShare.getShadow());
                    oos.close();
                    System.out.println("Shadow for x = " + currentShare.x + " was succesfully saved!");
                    System.out.println("Value = " + currentShare.getShadow().intValue());

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public SecretShare[] loadShares(String path) {
        SecretShare[] shares = new SecretShare[this.n];
        for (int i = 1; i < this.n; i++) {
            String fullPath = path + i;
            FileInputStream fis;
            ObjectInputStream ois;
            if(new File(fullPath).exists()) {
                try {
                    fis = new FileInputStream(path + i);
                    ois = new ObjectInputStream(fis);
                    BigInteger shadow = ((BigInteger) ois.readObject());
                    shares[i] = new SecretShare(i, shadow);
                    System.out.println("Shadow for x = " + i + " with value: " + shadow.intValue() + " was loaded");
                } catch (IOException e) {
                    System.out.println("Shadow " + fullPath + " not found. Skipping...");
                } catch (ClassNotFoundException cnfe) {
                    System.out.println(fullPath + " corrupted!");
                }
                shares[i] = new SecretShare(i, BigInteger.ZERO);
            }

        }
        return shares;
    }
}