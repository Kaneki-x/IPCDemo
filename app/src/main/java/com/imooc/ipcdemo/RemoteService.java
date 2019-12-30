package com.imooc.ipcdemo;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.imooc.ipcdemo.IConnectionService.Stub;
import com.imooc.ipcdemo.entity.Message;

public class RemoteService extends Service {

    private boolean isConnected = false;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            Messenger clientMessenger = msg.replyTo;
            Bundle bundle = msg.getData();
            bundle.setClassLoader(Message.class.getClassLoader());
            Message message = bundle.getParcelable("message");
            Toast.makeText(RemoteService.this, message.getContent(), Toast.LENGTH_SHORT).show();

            try {
                Message reply = new Message();
                reply.setContent("message reply from remote");
                android.os.Message data = new android.os.Message();
                data.replyTo = clientMessenger;
                bundle = new Bundle();
                bundle.putParcelable("message", reply);
                data.setData(bundle);
                clientMessenger.send(data);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private RemoteCallbackList<MessageReceiveListener> messageReceiveListenerRemoteCallbackList = new RemoteCallbackList<>();

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private ScheduledFuture scheduledFuture;

    private Messenger messenger = new Messenger(handler);

    private IConnectionService connectionService = new Stub() {
        @Override
        public void connect() throws RemoteException {
            try {
                Thread.sleep(5000);
                isConnected = true;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RemoteService.this, "connect", Toast.LENGTH_SHORT).show();
                    }
                });
                scheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        int size = messageReceiveListenerRemoteCallbackList.beginBroadcast();
                        for (int i = 0; i < size; i++) {
                            Message message = new Message();
                            message.setContent("this message from remote");
                            try {
                                messageReceiveListenerRemoteCallbackList.getBroadcastItem(i).onReceiveMessage(message);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                        messageReceiveListenerRemoteCallbackList.finishBroadcast();
                    }
                }, 5000, 5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void disconnect() throws RemoteException {
            isConnected = false;
            scheduledFuture.cancel(true);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(RemoteService.this, "disconnect", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public boolean isConnected() throws RemoteException {
            return isConnected;
        }
    };

    private IMessageService messageService = new IMessageService.Stub() {
        @Override
        public void sendMessage(final Message message) throws RemoteException {
            Log.d(RemoteService.class.getSimpleName(), String.valueOf(message.getContent()));
            if (isConnected) {
                message.setSendSuccess(true);
            } else {
                message.setSendSuccess(false);
            }
        }

        @Override
        public void registerMessageReceiveListener(MessageReceiveListener messageReceiveListener)
            throws RemoteException {
            if (messageReceiveListener != null) {
                messageReceiveListenerRemoteCallbackList.register(messageReceiveListener);
            }
        }

        @Override
        public void unRegisterMessageReceiveListener(MessageReceiveListener messageReceiveListener)
            throws RemoteException {
            if (messageReceiveListener != null) {
                messageReceiveListenerRemoteCallbackList.unregister(messageReceiveListener);
            }
        }
    };

    private IServiceManager serviceManager = new IServiceManager.Stub() {
        @Override
        public IBinder getService(String serviceName) throws RemoteException {
            if (IConnectionService.class.getSimpleName().equals(serviceName)) {
                return connectionService.asBinder();
            } else if (IMessageService.class.getSimpleName().equals(serviceName)) {
                return messageService.asBinder();
            } else if (Messenger.class.getSimpleName().equals(serviceName)) {
                return messenger.getBinder();
            } else {
                return null;
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceManager.asBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    }
}
