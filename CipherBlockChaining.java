/*
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package com.sun.crypto.provider;

import java.security.InvalidKeyException;

/**
 * This class represents ciphers in cipher block chaining (CBC) mode.
 *
 * <p>This mode is implemented independently of a particular cipher.
 * Ciphers to which this mode should apply (e.g., DES) must be
 * <i>plugged-in</i> using the constructor.
 *
 * <p>NOTE: This class does not deal with buffering or padding.
 *
 * @author Gigi Ankeny
 */

class CipherBlockChaining extends FeedbackCipher  {

    /*
     * random bytes that are initialized with iv
     */
    protected byte[] r;

    /*
     * output buffer
     */
    private byte[] k;

    // variables for save/restore calls
    private byte[] rSave = null;

    CipherBlockChaining(SymmetricCipher embeddedCipher) {
        super(embeddedCipher);
        k = new byte[blockSize];
        r = new byte[blockSize];
    }

    /**
     * Gets the name of this feedback mode.
     *
     * @return the string <code>CBC</code>
     */
    String getFeedback() {
        return "CBC";
    }

    /**
     * Initializes the cipher in the specified mode with the given key
     * and iv.
     *
     * @param decrypting flag indicating encryption or decryption
     * @param algorithm the algorithm name
     * @param key the key
     * @param iv the iv
     *
     * @exception InvalidKeyException if the given key is inappropriate for
     * initializing this cipher
     */
    void init(boolean decrypting, String algorithm, byte[] key, byte[] iv)
            throws InvalidKeyException {
        if ((key == null) || (iv == null) || (iv.length != blockSize)) {
            throw new InvalidKeyException("Internal error");
        }
        this.iv = iv;
        reset();
        embeddedCipher.init(decrypting, algorithm, key);
    }

    /**
     * Resets the iv to its original value.
     * This is used when doFinal is called in the Cipher class, so that the
     * cipher can be reused (with its original iv).
     */
    void reset() {
        System.arraycopy(iv, 0, r, 0, blockSize);
    }

    /**
     * Save the current content of this cipher.
     */
    void save() {
        if (rSave == null) {
            rSave = new byte[blockSize];
        }
        System.arraycopy(r, 0, rSave, 0, blockSize);
    }

    /**
     * Restores the content of this cipher to the previous saved one.
     */
    void restore() {
        System.arraycopy(rSave, 0, r, 0, blockSize);
    }

    /**
     * Performs encryption operation.
     *
     * <p>The input plain text <code>plain</code>, starting at
     * <code>plainOffset</code> and ending at
     * <code>(plainOffset + len - 1)</code>, is encrypted.
     * The result is stored in <code>cipher</code>, starting at
     * <code>cipherOffset</code>.
     *
     * <p>It is the application's responsibility to make sure that
     * <code>plainLen</code> is a multiple of the embedded cipher's block size,
     * as any excess bytes are ignored.
     *
     * @param plain the buffer with the input data to be encrypted
     * @param plainOffset the offset in <code>plain</code>
     * @param plainLen the length of the input data
     * @param cipher the buffer for the result
     * @param cipherOffset the offset in <code>cipher</code>
     */
    void encrypt(byte[] plain, int plainOffset, int plainLen,
                 byte[] cipher, int cipherOffset)
    {
        int i;
        int endIndex = plainOffset + plainLen;

        for (; plainOffset < endIndex;
             plainOffset+=blockSize, cipherOffset += blockSize) {
            for (i=0; i<blockSize; i++) {
                k[i] = (byte)(plain[i+plainOffset] ^ r[i]);
            }
            embeddedCipher.encryptBlock(k, 0, cipher, cipherOffset);
            System.arraycopy(cipher, cipherOffset, r, 0, blockSize);
        }
    }

    /**
     * Performs decryption operation.
     *
     * <p>The input cipher text <code>cipher</code>, starting at
     * <code>cipherOffset</code> and ending at
     * <code>(cipherOffset + len - 1)</code>, is decrypted.
     * The result is stored in <code>plain</code>, starting at
     * <code>plainOffset</code>.
     *
     * <p>It is the application's responsibility to make sure that
     * <code>cipherLen</code> is a multiple of the embedded cipher's block
     * size, as any excess bytes are ignored.
     *
     * <p>It is also the application's responsibility to make sure that
     * <code>init</code> has been called before this method is called.
     * (This check is omitted here, to avoid double checking.)
     *
     * @param cipher the buffer with the input data to be decrypted
     * @param cipherOffset the offset in <code>cipherOffset</code>
     * @param cipherLen the length of the input data
     * @param plain the buffer for the result
     * @param plainOffset the offset in <code>plain</code>
     *
     * @exception IllegalBlockSizeException if input data whose length does
     * not correspond to the embedded cipher's block size is passed to the
     * embedded cipher
     */
    void decrypt(byte[] cipher, int cipherOffset, int cipherLen,
                 byte[] plain, int plainOffset)
    {
        int i;
        byte[] cipherOrig=null;
        int endIndex = cipherOffset + cipherLen;

        if (cipher==plain && (cipherOffset >= plainOffset)
            && ((cipherOffset - plainOffset) < blockSize)) {
            // Save the original ciphertext blocks, so they can be
            // stored in the feedback register "r".
            // This is necessary because in this constellation, a
            // ciphertext block (or parts of it) will be overridden by
            // the plaintext result.
            cipherOrig = (byte[])cipher.clone();
        }

        for (; cipherOffset < endIndex;
             cipherOffset += blockSize, plainOffset += blockSize) {
            embeddedCipher.decryptBlock(cipher, cipherOffset, k, 0);
            for (i = 0; i < blockSize; i++) {
                plain[i+plainOffset] = (byte)(k[i] ^ r[i]);
            }
            if (cipherOrig==null) {
                System.arraycopy(cipher, cipherOffset, r, 0, blockSize);
            } else {
                System.arraycopy(cipherOrig, cipherOffset, r, 0, blockSize);
            }
        }
    }
}
