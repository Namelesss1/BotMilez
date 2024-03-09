package commands.trivia.triviaeditor;

import commands.Stoppable;
import commands.trivia.TriviaType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class TriviaEditSession extends ListenerAdapter implements Stoppable {

    /* Specifies the potential ways user interacts with the trivia editor */
    private enum State {

        UNDEFINED, /* User has not-yet selected one of the below */
        CREATE, /* Creating a new trivia */
        MODIFY, /* Modifying a specific element of an existing trivia */
        DELETE /* Deleting a trivia */
    }

    /* For state machine : what the user is currently inputting */
    private enum InputType {
        START, /* Just beginning */
        NAME, /* Inputting name */
        TAGS, /* Inputting tags */
        UNIVERSAL, /* Inputting whether trivia is universally viewable */
        SERVERS, /* Input allowed servers that can view trivia */
        EDITORS, /* Input allowed contributors to trivia */
        QUESTION, /* Input a question */
        ANSWERS, /* Input answers to a question */
        POINTS /* Input amount of points a quesion is worth */
    }

    /* User that is currently editing/creating this trivia */
   private User user;

   /* Channel for DM with user */
   private String channelId;


   private InputType inputType;

  private State state;

   /* TriviaType object containing the modified information to write back */
   private TriviaType triviaType;

   private String startStr = "Type one of the below options to either create a new" +
           "trivia, modify an existing one, or delete a trivia entirely.\n" +
           "Remember, to end this process at anytime you can type 'stop' \n\n" +
           "```create``` | ```modify``` | ```delete```";

   public TriviaEditSession(User user) {
       this.user = user;
       inputType = InputType.START;
       state = State.UNDEFINED;

       user.openPrivateChannel().flatMap(
               privChannel -> {
                   channelId = privChannel.getId();
                   return privChannel.sendMessage(startStr);
               }
       ).queue();

       user.getJDA().addEventListener(this);
   }

   @Override
   public void stop(User user, MessageChannel channel) {
       destroySession();
   }

   public void destroySession() {
       user.getJDA().removeEventListener(this);
   }

    /**
     * Sends a Private Message to a user.
     * @param msg message content
     */
    private void sendPM(String msg) {
        user.openPrivateChannel().complete()
                .sendMessage(msg).queue();
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        /* If bot, ignore */
        if (event.getAuthor().isBot()) {
            return;
        }

        /* If not the correct user, ignore */
        if (!event.getChannel().getId().equals(channelId)) {
            return;
        }
        if (!event.getAuthor().equals(user)) {
            return;
        }

        String msg = event.getMessage().getContentRaw();
        MessageChannel channel = event.getChannel();

        if (state == State.UNDEFINED) {
            if (msg.equalsIgnoreCase("create")) {
                state = State.CREATE;
            }
            else if (msg.equalsIgnoreCase("modify")) {
                state = State.MODIFY;
            }
            else if (msg.equalsIgnoreCase("delete")) {
                state = State.DELETE;
            }
            else {
                channel.sendMessage("You did not select an appropriate option").queue();
            }
        }

        if (state == State.CREATE) {
            handleCreate(event);
        }
        else if (state == State.MODIFY) {
            handleModify(event);
        }
        else if (state == State.DELETE) {
            handleDelete(event);
        }

    }

    /**
     * Handles queries when a user chooses to create a new trivia
     * @param event
     */
    private void handleCreate(MessageReceivedEvent event) {

        String msg = event.getMessage().getContentRaw();
        MessageChannel channel = event.getChannel();

        if (inputType == InputType.START) {
            triviaType = new TriviaType(user.getName());
            channel.sendMessage("What would you like to name this new trivia?").queue();
            inputType = InputType.NAME;
        }
        else if (inputType == InputType.NAME) {
            triviaType.setName(msg);
            String tagStr = "Enter any tags you want for this trivia. These tags help identify" +
                    "what the trivia is about. Seperate each one with a comma. For example," +
                    "if I make a trivia for Super Mario Bros., I might type this:\n\n" +
                    "super mario bros, mario, nintendo, nes, fun game, mario bros";

            channel.sendMessage(tagStr).queue();
            inputType = InputType.TAGS;
        }
        else if (inputType == InputType.TAGS) {
            List<String> tags = TriviaEditor.parseCommaSeparatedList(msg);
            triviaType.setTags(tags);
            String universalStr = "Do you want this trivia to be viewable across all servers i'm in?" +
                    "Type yes or no.";

            channel.sendMessage(universalStr).queue();
            inputType = InputType.UNIVERSAL;
        }
        else if (inputType == InputType.UNIVERSAL) {
            boolean universal;
            if (msg.equalsIgnoreCase("yes")) {
                universal = true;
            }
            else if (msg.equalsIgnoreCase("no")) {
                universal = false;
            }
            else {
                channel.sendMessage(msg + " is not recognized. Please type yes or no.").queue();
                return;
            }
            triviaType.setUniversal(universal);

            String str;
            if (universal) {
                str = "Enter the discord usernames of all people you want to be able to " +
                        "contribute to this trivia. These people will be able to edit, modify," +
                        "contribute to it, or delete it. Use a comma-separated list e.g.:\n\n" +
                        "bob41, mike10, awesomeUser";

                inputType = InputType.EDITORS;
            }
            else {
                str = "Enter the names all of the servers you want this trivia to be" +
                        "playable in. Use a comma-separated list e.g. \n\n" +
                        "my test server, The Clubhouse, Jim's Hangout Server";

                inputType = InputType.SERVERS;
            }

            channel.sendMessage(str).queue();
        }

    }


    /**
     * Handles queries when a user chooses to modify an existing trivia
     * @param event
     */
    private void handleModify(MessageReceivedEvent event) {

        String msg = event.getMessage().getContentRaw();
        MessageChannel channel = event.getChannel();

    }


    /**
     * Handles queries when a user chooses to delete an existing trivia
     * @param event
     */
    private void handleDelete(MessageReceivedEvent event) {

        String msg = event.getMessage().getContentRaw();
        MessageChannel channel = event.getChannel();

    }








}
