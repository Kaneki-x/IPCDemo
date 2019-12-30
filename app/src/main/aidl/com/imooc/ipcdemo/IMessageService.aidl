package com.imooc.ipcdemo;
import com.imooc.ipcdemo.entity.Message;
import com.imooc.ipcdemo.MessageReceiveListener;

// 消息服务
interface IMessageService {

    void sendMessage(inout Message message);

    void registerMessageReceiveListener(MessageReceiveListener messageReceiveListener);

    void unRegisterMessageReceiveListener(MessageReceiveListener messageReceiveListener);

}
