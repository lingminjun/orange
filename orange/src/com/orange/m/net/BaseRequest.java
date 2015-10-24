package com.orange.m.net;

import com.ssn.framework.foundation.RPC;

/**
 * Created by lingminjun on 15/10/21.
 */
public class BaseRequest<T> extends RPC.Request<T> {
    @Override
    public T call() {
        return null;
    }
}
