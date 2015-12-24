package com.mcintyret.jvm.core.opcode.store;

import com.mcintyret.jvm.core.util.ByteIterator;

abstract class DoubleWidthStoreIndexed extends DoubleWidthStore {

    @Override
    protected final int getIndex(ByteIterator bytes) {
        return bytes.nextByte();
    }

}
