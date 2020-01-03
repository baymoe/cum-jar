package moe.bay.cumjar;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.server.ServerJoinListener;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;
import org.javacord.api.listener.server.role.UserRoleAddListener;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static java.lang.Integer.parseInt;

public class Listener implements MessageCreateListener, ServerJoinListener, ServerMemberJoinListener, ServerMemberLeaveListener, UserRoleAddListener, UserRoleRemoveListener {
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        MessageAuthor author = event.getMessageAuthor();
        Message message = event.getMessage();
        if (author.isRegularUser()) {
            CumUser u = new CumUser(author.asUser().get());
            List<String> args = Arrays.asList(event.getMessageContent().split(" "));
            if (args.get(0).equals("!jar")) {
                if (args.size() == 1) {
                    int random = (int) (Math.random() * App.jars.size());
                    message.getChannel().sendMessage(App.jarEmbed.setImage(App.jars.get(random)));
                } else {
                    if (u.isAdmin()) {
                        if (args.get(1).equals("add")) {
                            String jar;
                            if (message.getAttachments().size() > 0) {
                                App.jars.add(message.getAttachments().get(0).getUrl().toString());
                                jar = message.getAttachments().get(0).getUrl().toString();
                                message.getChannel().sendMessage("Your jar (<" + jar + ">) has been added with index " + App.jars.size());
                            } else if (args.size() > 2 &&
                                    (args.get(2).startsWith("https://")
                                            && (args.get(2).endsWith(".jpg")
                                            || args.get(2).endsWith(".png")))) {
                                App.jars.add(args.get(2));
                                jar = args.get(2);
                                message.getChannel().sendMessage("Your jar (<" + jar + ">) has been added with index " + (App.jars.size() - 1));
                            } else {
                                message.getChannel().sendMessage("Please attach or provide a URL to the image you wish to add");
                            }
                            try {
                                App.jarOut();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (args.get(1).equals("remove") && args.size() > 2) {
                            int index = parseInt(args.get(2));
                            if (!(index > -1))
                                message.getChannel().sendMessage("Please provide the index of the jar you would like to remove.");
                            App.jars.remove(index);
                            try {
                                App.jarOut();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            message.getChannel().sendMessage("Jar with index " + index + " has been removed");
                        } else if (args.get(1).equals("list")) {
                            String[] jarList = {"All of the jars:"};
                            App.jars.forEach(jar -> {
                                jarList[0] = jarList[0] + "\n<" + jar + ">";
                            });
                            message.getChannel().sendMessage(jarList[0]);
                        } else if (args.get(1).equals("roles")) {
                            event.getServer().ifPresent(server -> {
                                List<Role> roles = new ArrayList<>();
                                args.subList(2, args.size()).forEach(roleId -> {
                                    System.out.println("Adding " + roleId + " to roles");
                                    server.getRoleById(roleId).ifPresent(roles::add);
                                });
                                server.getMembers().forEach(member -> {
                                    if (member.isBot()) {
                                        System.out.println(member.getDiscriminatedName() + " is a bot, skipping...");
                                    } else {
                                        roles.forEach(role -> {
                                            if (member.getRoles(server).contains(role)) {
                                                System.out.println(member.getDiscriminatedName() + " Already has " + role.getName() + ", skipping...");
                                            } else {
                                                System.out.println("Adding role " + role.getName() + " to user " + member.getDiscriminatedName());
                                                member.addRole(role, "role addition by " + u.getUser().getDiscriminatedName());
                                                event.getChannel().sendMessage("Role " + role.getMentionTag() + " added to user " + member.getMentionTag());
                                            }
                                        });
                                    }
                                });
                            });
                        } else {
                            boolean isInt = true;
                            int i = 0;
                            try {
                                i = parseInt(args.get(1));
                            } catch (NumberFormatException e) {
                                isInt = false;
                            }
                            if (isInt && i <= (App.jars.size() - 1)) {
                                message.getChannel().sendMessage(App.jarEmbed.setImage(App.jars.get(i)));
                            } else {
                                message.getChannel().sendMessage("Please provide a valid index");
                            }
                        }
                    }
                }
//            }
                if (args.get(0).equals("!stats")) {
                    Collection<Server> servers = event.getApi().getServers();
                    ArrayList<User> users = new ArrayList<>();
                    ArrayList<ServerTextChannel> channels = new ArrayList<>();
                    servers.forEach(server -> {
                        users.addAll(server.getMembers());
                        channels.addAll(server.getTextChannels());
                    });

                    App.statsEmbed.removeAllFields();
                    message.getChannel().sendMessage(App.statsEmbed
                            .addField("suggest new cum jars: ", "https://discord.gg/GsqT7GP")
                            .addInlineField("Users", "" + users.size())
                            .addInlineField("Guilds", "" + servers.size())
                            .addInlineField("Jars", "" + App.jars.size())
                            .addInlineField("Channels", "" + channels.size()));
                }
            }
        }
    }


    @Override
    public void onServerJoin(ServerJoinEvent event) {
        // establish which server was joined
        Server server = event.getServer();

        // debug
        System.out.println("Joined guild: " + event.getServer().getName());

        event.getApi().getServerById(650146481969561600L).flatMap(home -> home.getTextChannelById(650179401027158026L)).ifPresent(homeChannel -> {
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
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        // sending jars in every channel the bot can send messages to
//        event.getApi().getThreadPool().getExecutorService().execute(() -> {
//            server.getTextChannels().forEach(serverTextChannel -> {
//                // random integer for jar selection
//                int random = (int) (Math.random()*App.jars.size());
//
//                // send jar
//                serverTextChannel.sendMessage(App.jarEmbed.setImage(App.jars.get(random)));
//
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            });
//        });
    }

    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent event) {
        List<User> reibies = new ArrayList<>();
        event.getServer().getMembers().forEach(member -> {
            if (!member.isBot()) {
                reibies.add(member);
            }
        });
        System.out.println(event.getUser().getDiscriminatedName() + " Joined the server");
        event.getServer().getChannelById(662462809019252747L).ifPresent(serverChannel -> {
            System.out.println("Updating stats...");
            serverChannel.updateName("REIBIES: " + reibies.size());
        });
        reibies.clear();
    }

    @Override
    public void onServerMemberLeave(ServerMemberLeaveEvent event) {
        List<User> reibies = new ArrayList<>();
        event.getServer().getMembers().forEach(member -> {
            if (!member.isBot()) {
                reibies.add(member);
            }
        });
        System.out.println(event.getUser().getDiscriminatedName() + " Left the server");
        event.getServer().getChannelById(662462809019252747L).ifPresent(serverChannel -> {
            System.out.println("Updating stats...");
            serverChannel.updateName("REIBIES: " + reibies.size());
        });
        reibies.clear();
    }

    @Override
    public void onUserRoleAdd(UserRoleAddEvent event) {
        event.getServer().getChannelById(662465908878475290L).ifPresent(serverChannel -> {
            List<Role> teams = new ArrayList<>();
            teams.add(event.getServer().getRoleById(648723994299727923L).get());
            teams.add(event.getServer().getRoleById(649241841497276427L).get());
            teams.add(event.getServer().getRoleById(651229210790330418L).get());
            teams.sort(new RoleSizeComparator());
            event.getServer().getRoleById(648723994299727923L).ifPresent(role -> {
                serverChannel.updateName("TOP: " + teams.get(0).getName());;
            });
        });
    }

    @Override
    public void onUserRoleRemove(UserRoleRemoveEvent event) {
        event.getServer().getChannelById(662465908878475290L).ifPresent(serverChannel -> {
            List<Role> teams = new ArrayList<>();
            teams.add(event.getServer().getRoleById(648723994299727923L).get());
            teams.add(event.getServer().getRoleById(649241841497276427L).get());
            teams.add(event.getServer().getRoleById(651229210790330418L).get());
            teams.sort(new RoleSizeComparator());
            event.getServer().getRoleById(648723994299727923L).ifPresent(role -> {
                serverChannel.updateName("TOP: " + teams.get(0).getName());;
            });
        });
    }
}
