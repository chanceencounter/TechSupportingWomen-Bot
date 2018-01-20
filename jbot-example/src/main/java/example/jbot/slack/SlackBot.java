package example.jbot.slack;

import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.Controller;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import java.util.*;

import java.util.regex.Matcher;

/**
 * A Slack Bot sample. You can create multiple bots by just
 * extending {@link Bot} class like this one.
 *
 * @author ramswaroop
 * @version 1.0.0, 05/06/2016
 */
@Component
public class SlackBot extends Bot {

    private static final Logger logger = LoggerFactory.getLogger(SlackBot.class);

    /**
     * Slack token from application.properties file. You can get your slack token
     * next <a href="https://my.slack.com/services/new/bot">creating a new bot</a>.
     */
    @Value("${slackBotToken}")
    private String slackToken;

    @Override
    public String getSlackToken() {
        return slackToken;
    }

    @Override
    public Bot getSlackBot() {
        return this;
    }

    /**
     * Invoked when the bot receives a direct mention (@botname: message)
     * or a direct message. NOTE: These two event types are added by jbot
     * to make your task easier, Slack doesn't have any direct way to
     * determine these type of events.
     *
     * @param session
     * @param event
     */
    @Controller(events = {EventType.DIRECT_MENTION, EventType.DIRECT_MESSAGE})
    public void onReceiveDM(WebSocketSession session, Event event) {
        reply(session, event, new Message("Hi, I am " + slackService.getCurrentUser().getName()));
    }

    /**
     * Invoked when bot receives an event of type message with text satisfying
     * the pattern {@code ([a-z ]{2})(\d+)([a-z ]{2})}. For example,
     * messages like "ab12xy" or "ab2bc" etc will invoke this method.
     *
     * @param session
     * @param event
     */
    @Controller(events = EventType.MESSAGE, pattern = "^([a-z ]{2})(\\d+)([a-z ]{2})$")
    public void onReceiveMessage(WebSocketSession session, Event event, Matcher matcher) {
        reply(session, event, new Message("First group: " + matcher.group(0) + "\n" +
                "Second group: " + matcher.group(1) + "\n" +
                "Third group: " + matcher.group(2) + "\n" +
                "Fourth group: " + matcher.group(3)));
    }

    /**
     * Invoked when an item is pinned in the channel.
     *
     * @param session
     * @param event
     */
    @Controller(events = EventType.PIN_ADDED)
    public void onPinAdded(WebSocketSession session, Event event) {
        reply(session, event, new Message("Thanks for the pin! You can find all pinned items under channel details."));
    }

    /**
     * Invoked when bot receives an event of type file shared.
     * NOTE: You can't reply to this event as slack doesn't send
     * a channel id for this event type. You can learn more about
     * <a href="https://api.slack.com/events/file_shared">file_shared</a>
     * event from Slack's Api documentation.
     *
     * @param session
     * @param event
     */
    @Controller(events = EventType.FILE_SHARED)
    public void onFileShared(WebSocketSession session, Event event) {
        logger.info("File shared: {}", event);
    }

  //Boilerplate above, our chatbot starts here.

  /**
   * This method is for asking for help.
   * @param session
   * @param event
   */

  @Controller(pattern = "(help me)", next = "helpQuestion")
    public void helpMe(WebSocketSession session, Event event) {
        startConversation(event, "helpQuestion");   // start conversation
        reply(session, event, new Message("What do you need help with?"));
    }

  /**
   * This method has a list of all the possible inputs.
   * @param session
   * @param event
   */

    @Controller
    public void helpQuestion(WebSocketSession session, Event event) {
        if (event.getText().matches("^.*?(?i)(suicide|suicidal|bitcoin|ether).*")) {
          reply(session, event, new Message("I recommend that you call the National Suicide Hotline at 1-800-273-8255."));
          stopConversation(event);
        } else if (event.getText().matches("^.*?(?i)(postpartum|depression).*")) {
            reply(session, event, new Message("I recommend that you call the English/Spanish support line: 1-800-944-4773"));
            stopConversation(event);
        } else if (event.getText().matches("^.*?(?i)(test|testing).*")) {
          reply(session, event, new Message("Hi, thanks for your test message."));
          stopConversation(event);
        }
    }

    @Controller
    public void helpAdvice(WebSocketSession session, Event event, String[] args) {
        List<String> advice = new ArrayList<String>(5);

        advice.add("Take a deep breath");
        advice.add("If you think you may have post partum depression, read more at www.postpartum.net");
        advice.add("Drink a glass of water");
        advice.add("Call up a friend");
        advice.add("Make sure you are getting enough sleep");

        Random rand = new Random();
        int num = rand.nextInt(5);

        if (event.getText().matches("^.*?(?i)(advice|tips).*")) {
            reply(session, event, new Message(advice.get(num)));
            stopConversation(event);

        } else if (event.getText().matches("^.*?(?i)(test|testing).*")) {
            reply(session, event, new Message("Hi, thanks for your test message."));
            stopConversation(event);
        }
    }


    @Controller
    public void helpSymptoms(WebSocketSession session, Event event) {
        if (event.getText().matches("^.*?(?i)(facts|symptoms|postpartum).*")) {
            reply(session, event, new
                    Message("Symptoms include: " +
                    " Obsessive or intensive thoughts," +
                    " compulsions, " +
                    " feeling fear of being left" +
                    " alone with your baby," +
                    " hypervigilance in protecting your baby.\n" +
                    " If you think you need help please reach out: + type 'help me' for more info "
            ));
            stopConversation(event);
        } else if (event.getText().matches("^.*?(?i)(test|testing).*")) {
            reply(session, event, new Message("Hi, thanks for your test message."));
            stopConversation(event);
        }
    }

  @Controller(pattern ="(mood)", next = "moodHelp")
  public  void currentMood(WebSocketSession session, Event event) {
    startConversation(event, "moodHelp");   // start conversation
    reply(session, event, new Message("How are you doing today?"));
  }

  @Controller(next = "checkBadMood")
  public void moodHelp(WebSocketSession session, Event event) {
    if (event.getText().matches("^.*?(?i)(happy|good|great).*")) {
      reply(session, event, new Message("That's great, I'm glad your day is going well!"));
      stopConversation(event);
    } else if (event.getText().matches("^.*?(?i)(sad|not good|not great|bad).*")) {
      reply(session, event, new Message("I'm sorry about that, what's wrong?"));
      nextConversation(event);
    } else {
      reply(session, event, new Message("Invalid input, sorry."));
      stopConversation(event);
    }
  }

  @Controller
  public void checkBadMood(WebSocketSession session, Event event) {
    if (event.getText().matches("^.*?(?i)(work|manager).*")) {
      reply(session, event, new Message("I'm sorry, here are a list of designated individuals you can talk to: Jane Doe ext:1234, or you can send an anonymous email to HR here: [placeholder]"));
      stopConversation(event);
    } else if (event.getText().matches("^.*?(?i)(personal|home).*")) {
      reply(session, event, new Message("May I suggest a walk, or talking to a mental health counselor?"));
      stopConversation(event);
    } else {
      reply(session, event, new Message("I'm sorry, I'm not well equipped to handle this problem. We'll do our best here at Ovia Health to address this in the future, you can e-mail [email address] with suggestions, or ask your PCP for recommendations."));
      stopConversation(event);
    }
  }
}