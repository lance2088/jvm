package com.mcintyret.jvm.core.opcode.load;

import com.mcintyret.jvm.core.util.ByteIterator;

abstract class SingleWidthLoad_2 extends SingleWidthLoad {
    
    @Override
    protected final int getIndex(ByteIterator bytes) {
        return 2;
    }
}
