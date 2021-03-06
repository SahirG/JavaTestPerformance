/*
 * Copyright (c) 1998, 2003, Oracle and/or its affiliates. All rights reserved.
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

/*
 * (C) Copyright IBM Corp. 1998 - All Rights Reserved
 *
 */

package sun.io;

import sun.nio.cs.ext.IBM1141;

/**
 * Tables and data to convert Unicode to Cp1141
 *
 * @author  ConverterGenerator tool
 */

public class CharToByteCp1141 extends CharToByteSingleByte {

    private final static IBM1141 nioCoder = new IBM1141();

    public String getCharacterEncoding() {
        return "Cp1141";
    }

    public CharToByteCp1141() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
    }
}
