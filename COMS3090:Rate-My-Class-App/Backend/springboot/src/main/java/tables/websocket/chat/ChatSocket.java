package tables.websocket.chat;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
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
import tables.course.Course;
import tables.course.CourseRepository;
import tables.user.User;
import tables.user.UserRepository;

@Component
@ServerEndpoint(value = "/chat/{courseId}/{username}")
public class ChatSocket {

    private static MessageRepository msgRepo;
    private static UserRepository userRepo;
    private static CourseRepository courseRepo;

    @Autowired
    public void setMessageRepository(MessageRepository repo) {
        msgRepo = repo;
    }

    @Autowired
    public void setUserRepository(UserRepository repo) {
        userRepo = repo;
    }

    @Autowired
    public void setCourseRepository(CourseRepository repo) {
        courseRepo = repo;
    }

    private static Map<Long, Map<String, Session>> courseSessionMap = new Hashtable<>();
    private final Logger logger = LoggerFactory.getLogger(ChatSocket.class);

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("courseId") Long courseId,
                       @PathParam("username") String username) throws IOException {

        logger.info("User " + username + " joining course chat: " + courseId);

        courseSessionMap.putIfAbsent(courseId, new Hashtable<>());
        courseSessionMap.get(courseId).put(username, session);

        session.getUserProperties().put("username", username);
        session.getUserProperties().put("courseId", courseId);

        sendMessageToUser(session, getChatHistory(courseId));
        broadcastToCourse(courseId, "User: " + username + " has joined the chat");
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        String username = (String) session.getUserProperties().get("username");
        Long courseId   = (Long) session.getUserProperties().get("courseId");

        logger.info("Message from " + username + " in course " + courseId + ": " + message);

        broadcastToCourse(courseId, username + ": " + message);

        User user     = userRepo.findByUsername(username);
        Course course = courseRepo.findById(courseId).orElse(null);

        if (user != null && course != null) {
            msgRepo.save(new Message(user, message, course));
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        String username = (String) session.getUserProperties().get("username");
        Long courseId   = (Long) session.getUserProperties().get("courseId");

        if (courseId != null && courseSessionMap.containsKey(courseId)) {
            courseSessionMap.get(courseId).remove(username);
            broadcastToCourse(courseId, username + " has left the chat");
        }

        logger.info("User " + username + " disconnected from course chat: " + courseId);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.info("Chat socket error: " + throwable.getMessage());
        throwable.printStackTrace();
    }

    private void broadcastToCourse(Long courseId, String message) {
        Map<String, Session> users = courseSessionMap.get(courseId);
        if (users != null) {
            users.values().forEach(s -> sendMessageToUser(s, message));
        }
    }

    private void sendMessageToUser(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            logger.info("Exception sending message: " + e.getMessage());
        }
    }

    private String getChatHistory(Long courseId) {
        List<Message> messages = msgRepo.findByCourse_CourseId(courseId);
        StringBuilder sb = new StringBuilder();
        for (Message m : messages) {
            sb.append(m.getUser().getUsername()).append(": ").append(m.getContent()).append("\n");
        }
        return sb.toString();
    }
}