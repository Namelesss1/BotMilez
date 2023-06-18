package botmilez;

import commands.*;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

import static botmilez.config.BOTMILEZ_TOKEN;

public class BotMain {
    public static void main(String[] args) throws LoginException {

        JDABuilder jdaBuilder = JDABuilder.createDefault(BOTMILEZ_TOKEN);

        JDA jda = jdaBuilder.addEventListeners(new CommandManager())
                .setActivity(Activity.watching("Fresh Pillow"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                .build();


    }


}
