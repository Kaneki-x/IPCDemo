package com.imooc.ipcdemo;

// 连接服务
interface IConnectionService {

    oneway void connect();

    void disconnect();

    boolean isConnected();
}
