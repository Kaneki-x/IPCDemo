package com.imooc.ipcdemo;

interface IServiceManager {
    IBinder getService(String serviceName);
}
