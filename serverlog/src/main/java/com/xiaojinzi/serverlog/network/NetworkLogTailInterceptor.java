package com.xiaojinzi.serverlog.network;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.xiaojinzi.serverlog.bean.MessageBean;
import com.xiaojinzi.serverlog.bean.NetWorkLogInfoBean;
import com.xiaojinzi.serverlog.impl.ServerLog;

/**
 * 这个类不能删除, 因为字节码会创建这个类的
 */
@Keep
public class NetworkLogTailInterceptor extends BaseNetworkLogInterceptor {

    @Override
    protected void doSend(@NonNull NetWorkLogInfoBean netWorkLogInfo) {
        super.doSend(netWorkLogInfo);
        MessageBean<NetWorkLogInfoBean> messageBean = new MessageBean();
        messageBean.setType(MessageBean.TYPE_NETWORK_TAIL);
        messageBean.setData(netWorkLogInfo);
        ServerLog.INSTANCE.send(messageBean);
    }

}
