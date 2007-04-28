package net.spy.diggwatch;

import java.util.ArrayList;
import java.util.Collection;

import net.spy.SpyObject;
import net.spy.digg.Comment;
import net.spy.digg.Digg;
import net.spy.digg.EventParameters;
import net.spy.digg.PagingParameters;
import net.spy.digg.Story;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

/**
 * Interface to digg.
 */
public class DiggInterface extends SpyObject {

	private static final String APP_KEY="http://bleu.west.spy.net/diggwatch/";

	// How long to cache user comments.
	private static final int USER_COMMENTS_TIME = 900;
	// How long stories are cached
	private static final int STORY_TIME = 86400;
	// How long incremental comments are cached
	private static final int MIN_COMMENT_REPLY_TIME = 60;
	private static final int MAX_COMMENT_REPLY_TIME = 3600*12;

	// How far back to go for user comments (ms). (one week should be enough)
	private static final long MIN_COMMENT_AGE = 86000*7*1000;

	private static DiggInterface instance=null;

	private Digg digg=new Digg(APP_KEY);
	private MemcachedClient mc=null;

	/**
	 * Get the singleton digg interface.
	 */
	public static DiggInterface getInstance() {
		assert instance != null : "Not initialized";
		return instance;
	}

	/**
	 * Initialize the digg interface.
	 */
	public static void initialize() throws Exception {
		instance=new DiggInterface();
		instance.mc=new MemcachedClient(AddrUtil.getAddresses(
				"red:11211 purple:11211"));
	}

	/**
	 * Sht down the digg interface.
	 */
	public static void shutdown() {
		instance.mc.shutdown();
		instance=null;
	}

	/**
	 * Get the comments for the given user.
	 */
	public Collection<Comment> getUserComments(String user) throws Exception {
		String key="digg/comments/user/" + user;
		@SuppressWarnings("unchecked") // parameterized cache
		Collection<Comment> rv=(Collection<Comment>)mc.get(key);
		if(rv == null) {
			EventParameters ep=new EventParameters();
			ep.setMinDate(System.currentTimeMillis() - MIN_COMMENT_AGE);
			rv=digg.getUserComments(user, ep);
			mc.set(key, USER_COMMENTS_TIME, rv);
		}
		return rv;
	}

	/**
	 * Get the story with the given ID.
	 */
	public Story getStory(int storyId) throws Exception {
		String key="digg/story/" + storyId;
		Story rv=(Story) mc.get(key);
		if(rv == null) {
			rv=digg.getStory(storyId);
			mc.set(key, STORY_TIME, rv);
		}
		return rv;
	}

	/**
	 * Get potential replies to the given comment.
	 */
	public Collection<Comment> getReplies(Comment c) throws Exception {
		int repId=c.getReplyId() == null ? c.getEventId() : c.getReplyId();
		String key="digg/comments/replies/" + c.getStoryId() + "/" + repId;
		@SuppressWarnings("unchecked") // parameterized cast
		Collection<Comment> rv=(Collection<Comment>) mc.get(key);
		if(rv == null) {
			EventParameters ep=new EventParameters();
			ep.setCount(PagingParameters.MAX_COUNT);
			rv=digg.getCommentReplies(c.getStoryId(), repId, ep);
			mc.set(key, getCommentReplyTime(c), rv);
		}
		// Filter it since we cache all of them.
		ArrayList<Comment> frv=new ArrayList<Comment>();
		for(Comment rc : rv) {
			if(rc.getTimestamp() > c.getTimestamp()) {
				frv.add(rc);
			}
		}
		return frv;
	}

	private int getCommentReplyTime(Comment c) {
		long now=System.currentTimeMillis();
		// How many seconds ago was this comment
		long howLongAgo=(int)((now-c.getTimestamp())/1000);
		if(howLongAgo < 1) {
			howLongAgo=1;
		}

		// Keep it in range.
		int rv=(int)Math.max(MIN_COMMENT_REPLY_TIME,
				Math.min(MAX_COMMENT_REPLY_TIME, howLongAgo * howLongAgo));
		assert rv >= MIN_COMMENT_REPLY_TIME && rv <= MAX_COMMENT_REPLY_TIME
			: rv + " is out of range";

		getLogger().debug("Caching %s for %s (%d secs ago)", c, rv, howLongAgo);
		return rv;
	}
}
