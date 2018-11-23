package pl.mgr.hs.chat;

import com.blogspot.debukkitsblog.net.Datapackage;
import com.blogspot.debukkitsblog.net.Server;
import com.google.gson.Gson;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static pl.mgr.hs.chat.Constants.messageId.*;

/** Created by dominik on 23.11.18. */
public class ChatServer extends Server {

  private static final int PAYLOAD = 1;
  private static final String SERVER_ID = "server";
  private String chatDescription;
  private Gson gson = new Gson();

  public static ChatServer createServer(String port, String chatDescription) {
    return new ChatServer(Integer.parseInt(port), chatDescription);
  }

  private ChatServer(int port, String chatDescription) {
    super(port, false);
    this.chatDescription = chatDescription;
  }

  @Override
  public void preStart() {
    registerMethod(CLIENT_INITIAL_MESSAGE, this::handleClientInitialMessage);
    registerMethod(CLIENT_MESSAGE, this::handleClientMessage);
  }

  @Override
  public synchronized int broadcastMessage(Datapackage message) {
    toBeDeleted = new ArrayList<>();

    List<RemoteClient> clientsToSend =
        clients
            .stream()
            .filter(remoteClient -> !message.getSenderID().equals(remoteClient.getId()))
            .collect(Collectors.toList());

    // send message to all clients
    int txCounter = 0;
    for (RemoteClient current : clientsToSend) {
      sendMessage(current, message);
      txCounter++;
    }

    // remove all clients which produced errors while sending
    txCounter -= toBeDeleted.size();
    for (RemoteClient current : toBeDeleted) {
      clients.remove(current);
      onClientRemoved(current);
    }

    toBeDeleted = null;

    return txCounter;
  }

  private void handleClientMessage(Datapackage pack, Socket socket) {
    broadcastMessage(pack);
    sendReply(socket, "received");
  }

  private void handleClientInitialMessage(Datapackage pack, Socket socket) {
    Message message = gson.fromJson((String) pack.get(PAYLOAD), Message.class);
    sendReply(socket, "received");

    sendWelcome(message.getId(), pack.getSenderID());
    sendJoinAdvertisementMessage(message.getId());
  }

  private void sendJoinAdvertisementMessage(String newUserId) {
    String joinAdvertisement = String.format("%s has joined chat", newUserId);
    broadcastMessage(
        new Datapackage(SERVER_MESSAGE, gson.toJson(new Message(SERVER_ID, joinAdvertisement))));
  }

  private void sendWelcome(String newUserId, String senderId) {
    String replyMessage = String.format("Hi %s in the chat! %s", newUserId, chatDescription);

    sendMessage(
        senderId,
        new Datapackage(SERVER_MESSAGE, gson.toJson(new Message(SERVER_ID, replyMessage))));

    waitAfterSend();
  }

  // FIXME - ugly
  private void waitAfterSend() {
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
