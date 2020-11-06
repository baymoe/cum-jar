package moe.bay.cumjar;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.util.logging.ExceptionLogger;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class App {
    public static ArrayList<String> jars = new ArrayList<>();
    public static List<Long> botAdminIds = new ArrayList<>();

    public static EmbedBuilder jarEmbed = new EmbedBuilder()
            .setColor(new Color(255,255,255))
            .setDescription("[**invite cum jar**]" +
                    "(https://discordapp.com/oauth2/authorize?client_id=650106404522295326&scope=bot&permissions=19457" +
                    " \"add cum jar to your server\")")
            .addField("suggest new cum jars: ", "https://discord.gg/krykhwS");
    public static EmbedBuilder statsEmbed = new EmbedBuilder()
            .setColor(new Color(255,255,255))
            .setDescription("[**invite cum jar**]" +
                    "(https://discordapp.com/oauth2/authorize?client_id=650106404522295326&scope=bot&permissions=19457" +
                    " \"add cum jar to your server\")");

    private static File jarsTxt = new File(System.getProperty("user.dir") + File.separator + "jars.txt");
//    private static File getJarsTxt() {return jarsTxt;}

    public static void main(String[] args) {
        final String token = args[0];

        new DiscordApiBuilder()
                .setToken(token)
                .setRecommendedTotalShards().join()
                .setAllIntents()
                .loginAllShards()
                .forEach(shardFuture -> shardFuture
                        .thenAccept(App::onShardLogin).exceptionally(ExceptionLogger.get())
                );
    }

    private static void onShardLogin(DiscordApi api)  {
        // add activity
        api.updateActivity(ActivityType.WATCHING, "!jar");

        botAdminIds.add(618248905703948299L);
        botAdminIds.add(102102717165506560L);
        botAdminIds.add(770605596554690580L);

        // load jar images from file
        try {
            jarIn();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // debug
        System.out.println("Connected to shard " + api.getCurrentShard());
        Collection<Server> serverCollection = api.getServers();
        System.out.println(String.format("Total guilds: %s", serverCollection.size()));

        api.addListener(new Listener());
    }

    public static void jarOut() throws IOException {
        FileWriter jarTxtOut = new FileWriter("jars.txt");
        jarTxtOut.write("");
        jars.forEach(jar -> {
            try {
                if (jars.get(0).equals(jar)) {
                    System.out.println("Adding jar: " + jar);
                    jarTxtOut.append(jar);
                } else {
                    System.out.println("Adding jar: " + jar);
                    jarTxtOut.append("\n" + jar);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        jarTxtOut.close();
    }

    public static void jarIn() throws IOException {
        FileReader jarTxtIn = new FileReader(jarsTxt);
        String jarsString = "";
        int i;
        while ((i=jarTxtIn.read()) != -1) {
            jarsString = jarsString + (char) i;
        }

        String[] jarsStringArray = jarsString.split("\n");
        for (String s : jarsStringArray) {
            System.out.println(s);
            jars.add(s);
        }
        jarTxtIn.close();
    }
}

