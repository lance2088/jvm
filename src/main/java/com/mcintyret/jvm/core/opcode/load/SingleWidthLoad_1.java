package com.mcintyret.jvm.core.opcode.load;

import com.mcintyret.jvm.core.ByteIterator;

abstract class SingleWidthLoad_1 extends SingleWidthLoad {
    
    @Override
    protected final int getIndex(ByteIterator bytes) {
        return 1;
    }
}
