package com.mcintyret.jvm.core.opcode.store;

import com.mcintyret.jvm.core.util.ByteIterator;

abstract class SingleWidthStore_1 extends SingleWidthStore {
    
    @Override
    protected final int getIndex(ByteIterator bytes) {
        return 1;
    }
}
