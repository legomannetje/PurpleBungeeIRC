package com.cnaude.purpleirc;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.scheduler.ScheduledTask;

/**
 *
 * @author Chris Naude Poll the command queue and dispatch to Bukkit
 */
public class IRCMessageQueueWatcher {

    private final PurpleIRC plugin;
    private final PurpleBot ircBot;
    private final ScheduledTask bt;
    private final BlockingQueue<IRCMessage> queue = new LinkedBlockingQueue<>();

    /**
     *
     * @param plugin
     * @param ircBot
     */
    public IRCMessageQueueWatcher(final PurpleBot ircBot, final PurpleIRC plugin) {
        this.plugin = plugin;
        this.ircBot = ircBot;
        bt = this.plugin.getProxy().getScheduler().schedule(this.plugin, new Runnable() {
            @Override
            public void run() {
                queueAndSend();
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void queueAndSend() {
        IRCMessage ircMessage = queue.poll();
        if (ircMessage != null) {
            plugin.logDebug("[" + queue.size() + "]: queueAndSend message detected");
            if (ircMessage.ctcpResponse) {
                ircBot.blockingCTCPMessage(ircMessage.target, ircMessage.message);
            } else {
                ircBot.blockingIRCMessage(ircMessage.target, ircMessage.message);
            }
        }
    }

    public void cancel() {
        bt.cancel();
    }

    public String clearQueue() {
        int size = queue.size();
        if (!queue.isEmpty()) {
            queue.clear();
        }
        return "Elements removed from message queue: " + size;
    }

    /**
     *
     * @param ircMessage
     */
    public void add(IRCMessage ircMessage) {
        queue.offer(ircMessage);
    }
}
