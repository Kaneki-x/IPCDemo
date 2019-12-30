package com.imooc.ipcdemo;
import com.imooc.ipcdemo.entity.Message;

interface MessageReceiveListener {

    void onReceiveMessage(in Message message);
}
