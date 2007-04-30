package net.spy.diggwatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.spy.SpyObject;
import net.spy.digg.Comment;
import net.spy.digg.Digg;
import net.spy.digg.DiggException;
import net.spy.digg.EventParameters;
import net.spy.digg.PagedItems;
import net.spy.digg.PagingParameters;
import net.spy.digg.Story;
import net.spy.digg.StoryParameters;
import net.spy.digg.User;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

/**
 * Interface to digg.
 */
public class DiggInterface extends SpyObject {

	private static final String APP_KEY="http://bleu.west.spy.net/diggwatch/";

	// How long to cache user comments.
	private static final int USER_COMMENTS_TIME = 900;
	// How many comemnts to fetch for a user.
	private static final int NUM_USER_COMMENTS = 20;
	// How long stories are cached.
	private static final int STORY_TIME = 86400*14;
	// How long we negatively cache stories
	private static final int NEG_STORY_TIME = STORY_TIME/2;
	// How long incremental comments are cached
	private static final int MIN_COMMENT_REPLY_TIME = 180;
	private static final int MAX_COMMENT_REPLY_TIME = 86400;
	// How long to cache users
	private static final int USER_TIME = 3600;

	// How far back to go for user comments (ms). (two weeks should be enough)
	private static final long MIN_COMMENT_AGE = STORY_TIME*1000;

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
	 * Get the user with the given username.
	 */
	public User getUser(String username) throws Exception {
		String key="digg/user/" + username;
		User rv=(User)mc.get(key);
		if(rv == null) {
			rv=digg.getUser(username);
			mc.set(key, USER_TIME, rv);
		}
		return rv;
	}

	/**
	 * Get the comments for the given user.
	 */
	public Collection<? extends Comment> getUserComments(String user)
		throws Exception {
		String key="digg/comments/user/" + user;
		@SuppressWarnings("unchecked") // parameterized cache
		Collection<? extends Comment> rv=(Collection<Comment>)mc.get(key);
		if(rv == null) {
			EventParameters ep=new EventParameters();
			ep.setMinDate(System.currentTimeMillis() - MIN_COMMENT_AGE);
			ep.setCount(NUM_USER_COMMENTS);
			PagedItems<Comment> tmp = digg.getUserComments(user, ep);
			// TODO:  Loop
			if(tmp.getTotal() > tmp.getCount()) {
				getLogger().warn("Only got %d of %d comments for %s",
					tmp.getCount(), tmp.getTotal());
			}
			rv=tmp;
			mc.set(key, USER_COMMENTS_TIME, rv);
		}
		return rv;
	}

	/**
	 * Get the story with the given ID.
	 */
	public Story getStory(int storyId) throws Exception {
		String key=makeStoryKey(storyId);
		Object o=mc.get(key);
		// The exceptions get memoized into the cache.
		if(o instanceof DiggException) {
			throw (DiggException)o;
		}
		Story rv=(Story)o;
		if(rv == null) {
			try {
				rv=digg.getStory(storyId);
				mc.set(key, STORY_TIME, rv);
			} catch(DiggException e) {
				mc.set(key, NEG_STORY_TIME, e);
				throw e;
			}
		}
		return rv;
	}

	private String makeStoryKey(int storyId) {
		return "digg/story/" + storyId;
	}

	/**
	 * Get a collection of stories.  Try to get them from the cache where
	 * possible.
	 */
	public Map<Integer, Story> getStories(Collection<Integer> sids)
		throws Exception {
		getLogger().info("Fetching %d stories", sids.size());
		Map<Integer, Story> rv=new HashMap<Integer, Story>(sids.size());
		Map<String, Integer> keysToFetch=new HashMap<String, Integer>();
		for(int i : sids) {
			keysToFetch.put(makeStoryKey(i), i);
		}

		// Store the remaining itemss
		Collection<Integer> remainingStories=new HashSet<Integer>(sids);

		// See how many stories we can get from the cache.
		Map<String, Object> mcResponse = mc.getBulk(keysToFetch.keySet());
		for(Map.Entry<String, Object> me : mcResponse.entrySet()) {
			if(!(me.getValue() instanceof DiggException)) {
				Story s=(Story)me.getValue();
				rv.put(s.getId(), s);
			}
			remainingStories.remove(keysToFetch.get(me.getKey()));
		}

		// Get the rest from digg
		if(!remainingStories.isEmpty()) {
			getLogger().info("Fetching %d stories from digg",
				remainingStories.size());
			assert remainingStories.size() < 100
				: "Trying to fetch too many stories.";
			StoryParameters sp=new StoryParameters();
			sp.setCount(PagingParameters.MAX_COUNT);
			try {
				PagedItems<Story> stories = digg.getStories(
						remainingStories, sp);
				assert stories.getTotal() == stories.getCount()
					: "Total was " + stories.getTotal() + ", but I got "
						+ stories.getCount();
				for(Story s : stories) {
					rv.put(s.getId(), s);
					mc.set(makeStoryKey(s.getId()), STORY_TIME, s);
				}
			} catch(DiggException e) {
				// Something went wrong, do them one at a time.
				for(int sid : remainingStories) {
					try {
						Story story = getStory(sid);
						rv.put(sid, story);
					} catch(DiggException e2) {
						if(e2.getErrorId() != 1008) {
							throw e;
						}
						getLogger().info("Story %d was not found", sid);
					}
				}
			}
		}
		return rv;
	}

	/**
	 * Get all of the available stories for all of the given comemnts.
	 */
	public Map<Integer, Story> getStoriesForComments(
			Collection<Comment> comments) throws Exception {
		Set<Integer> sids=new HashSet<Integer>();
		for(Comment c : comments) {
			sids.add(c.getStoryId());
		}
		return getStories(sids);
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
				Math.min(MAX_COMMENT_REPLY_TIME,
						howLongAgo * Math.log(howLongAgo)));
		assert rv >= MIN_COMMENT_REPLY_TIME && rv <= MAX_COMMENT_REPLY_TIME
			: rv + " is out of range";

		getLogger().debug("Caching %s for %s (%d secs ago)", c, rv, howLongAgo);
		return rv;
	}

	/**
	 * Get the recent comments for the given user.
	 */
	public Collection<Comment> getRelevantComments(String user)
		throws Exception {
		getLogger().info("Fetching comments for %s", user);
		Collection<Comment> rv=new TreeSet<Comment>(new CommentComparator());
		rv.addAll(getUserComments(user));
		rv.addAll(findRelatedComments(rv));
		return rv;
	}

	private Collection<? extends Comment> findRelatedComments(
			Collection<Comment> comments) throws Exception {

		Collection<Comment> rv=new ArrayList<Comment>();

		DiggInterface di=DiggInterface.getInstance();
		for(Comment c : comments) {
			rv.addAll(di.getReplies(c));
		}

		return rv;
	}

	static class CommentComparator implements Comparator<Comment> {
		public int compare(Comment o1, Comment o2) {
			return o1.getEventId() - o2.getEventId();
		}
	}
}
