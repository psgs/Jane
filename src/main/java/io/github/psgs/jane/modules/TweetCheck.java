package io.github.psgs.jane.modules;

import io.github.psgs.jane.Jane;
import io.github.psgs.jane.utilities.AudioUtils;
import io.github.psgs.jane.utilities.BrowserUtils;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TweetCheck extends Module {

    static Twitter twitter = ReadTweet.getTwitterInstance();

    // TODO: Pass arguments (particular accounts) to module from speech
    public TweetCheck() {
        super("TweetCheck", "Reads tweets from a hardcoded list of accounts", 1);
    }

    /**
     * Schedules a task to check for tweets every 5 minutes
     *
     * @param args System arguments
     */
    public static void main(String[] args) {
        if (Jane.twitterUsed) {
            ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
            ses.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    execute();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }, 0, 3, TimeUnit.MINUTES);
        }
    }

    /**
     * Executes a check for @BreakingNews and @AFP tweets
     */
    public static void execute() {
        if (Jane.twitterUsed) {
            try {
                List<Status> statuses = twitter.getUserTimeline("BreakingNews");
                statuses.addAll(twitter.getUserTimeline("AFP"));
                Date date = new Date(System.currentTimeMillis() - 305000);
                for (Status status : statuses) {
                    if (status.getCreatedAt().after(date)) {
                        ReadTweet.execute(status);
                    }
                }
            } catch (TwitterException ex) {
                System.out.println("An error occurred while trying to fetch a tweet!");
                if (ex.getErrorCode() == 34) {
                    System.out.println("The tweet you requested wasn't found!");
                    try {
                        AudioUtils.talk("I tried to access a tweet, but it wasn't found.");
                    } catch (FileNotFoundException exception) {
                        System.out.println("A result couldn't be spoken!!");
                    }
                }
                if (BrowserUtils.isConnected()) ex.printStackTrace();
            }
        }
    }
}
