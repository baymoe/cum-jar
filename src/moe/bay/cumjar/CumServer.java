package moe.bay.cumjar;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.server.Server;

public class CumServer {

    private final Server server;

    public CumServer(Server server) {
        this.server = server;
    }

    public Server getServer() {
        return this.server;
    }

    public void setVerificationChannel(String id) {}

    public ServerTextChannel getVerificationChannel() {
        return null;
    }

}
