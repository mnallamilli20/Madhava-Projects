package tables.websocket.notification;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import tables.user.User;
import tables.user.UserRepository;
import tables.user.Role;

@Component
@ServerEndpoint(value = "/notifications/{userId}")
public class NotificationSocket {

    private static UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository repo) {
        userRepository = repo;
    }

    // Map of userId -> Session for all connected users
    private static Map<Long, Session> userSessionMap = new Hashtable<>();

    private final Logger logger = LoggerFactory.getLogger(NotificationSocket.class);

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) throws IOException {
        logger.info("User " + userId + " connected to notifications");

        userSessionMap.put(userId, session);
        session.getUserProperties().put("userId", userId);
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        // Clients don't send messages through this socket
        // it is receive only
        logger.info("Unexpected message received on notification socket: " + message);
    }

    @OnClose
    public void onClose(Session session) {
        Long userId = (Long) session.getUserProperties().get("userId");
        if (userId != null) {
            userSessionMap.remove(userId);
            logger.info("User " + userId + " disconnected from notifications");
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.info("Notification socket error: " + throwable.getMessage());
        throwable.printStackTrace();
    }

    // Called by ReviewVoteController when a vote is cast
    // Sends notification only to the review author
    public static void sendVoteNotification(Long reviewAuthorId, String message) {
        Session session = userSessionMap.get(reviewAuthorId);
        if (session != null) {
            sendMessageToUser(session, message);
        }
    }

    // Called by ReviewFlagController when a flag is submitted
    // Sends notification to all connected admins
    public static void sendFlagNotification(String message) {
        userSessionMap.forEach((userId, session) -> {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.getRole() == Role.ADMIN) {
                sendMessageToUser(session, message);
            }
        });
    }

    private static void sendMessageToUser(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            LoggerFactory.getLogger(NotificationSocket.class)
                    .info("Exception sending notification: " + e.getMessage());
        }
    }
}