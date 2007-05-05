package net.spy.diggwatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import net.spy.digg.UserParameters;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

/**
 * Interface to digg.
 */
public class DiggInterface extends SpyObject {

	private static final String APP_KEY="http://bleu.west.spy.net/diggwatch/";

	// How long to cache user comments.
	private static final int USER_COMMENTS_TIME = 60;
	// How many comemnts to fetch for a user.
	private static final int NUM_USER_COMMENTS = 20;
	// How long stories are cached.
	private static final int STORY_TIME = 300;
	// How long we negatively cache stories
	private static final int NEG_STORY_TIME = 86400;
	// How long incremental comments are cached (invalidated by story comments)
	private static final int COMMENT_REPLY_TIME = 86400;
	// How long friends of a user are cached
	private static final int USER_FRIEND_TIME = 86400;
	// How long to cache users
	private static final int USER_TIME = 86400;

	// How long to cache digg stories by domain.
	private static final int DOMAIN_TIME = 900;

	// How far back to go for user comments (ms). (two weeks should be enough)
	private static final long MIN_COMMENT_AGE = 86400*14*1000;

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
	 * Get a user from the cache.
	 */
	public User getUserFromCache(String username) {
		String key="digg/user/" + username;
		return (User)mc.get(key);
	}

	/**
	 * Get the users for a collection of strings.
	 */
	public Map<String, User> getUsers(Collection<String> usernames)
		throws Exception {
		Map<String, User> rv = new HashMap<String, User>(
			getCachedUsers(usernames));

		Collection<String> remainingUsers=new HashSet<String>(usernames);
		remainingUsers.removeAll(rv.keySet());
		for(String u : remainingUsers) {
			User user = getUser(u);
			rv.put(user.getName(), user);
		}

		return rv;
	}

	/**
	 * Get all of the users currently in the cache.
	 */
	public Map<String, User> getCachedUsers(Collection<String> usernames) {
		String keyBase="digg/user/";
		Map<String, User> rv=new HashMap<String, User>();
		Collection<String> keys=new HashSet<String>();
		for(String s : usernames) {
			keys.add(keyBase + s);
		}
		Map<String, Object> fromCache=mc.getBulk(keys);
		for(Map.Entry<String, Object> me : fromCache.entrySet()) {
			assert me.getKey().startsWith(keyBase);
			String u=me.getKey().substring(keyBase.length());
			assert !u.startsWith("/");
			rv.put(u, (User)me.getValue());
		}
		return rv;
	}

	/**
	 * Get the cached users for the given comments.
	 */
	public Map<String, User> getCachedUsersForComments(
		Collection<Comment> comments) {
		Collection<String> users=new HashSet<String>();
		for(Comment c : comments) {
			users.add(c.getUser());
		}
		return getCachedUsers(users);
	}

	/**
	 * Get all the comments for all the given users.
	 */
	public Map<String, Collection<Comment>> getUsersComments(
			Collection<String> users) throws Exception {
		String baseKey="digg/comments/user/";
		Collection<String> keys=new HashSet<String>(users.size());
		for(String u : users) {
			keys.add(u);
		}
		Map<String, Object> mcResult = mc.getBulk(keys);
		Map<String, Collection<Comment>> rv=
			new HashMap<String, Collection<Comment>>();
		for(Map.Entry<String, Object> me : mcResult.entrySet()) {
			assert me.getKey().startsWith(baseKey);
			String u=me.getKey().substring(baseKey.length());
			assert !u.startsWith("/");
			@SuppressWarnings("unchecked") // generic cast
			Collection<Comment> v=(Collection<Comment>)me.getValue();
			rv.put(u, v);
		}
		Collection<String> remainingUsers=new HashSet<String>(users);
		remainingUsers.removeAll(rv.keySet());

		assert remainingUsers.size() <= 100 : "Too many remaining users";
		Map<String, Collection<Comment>> newItems=
			new HashMap<String, Collection<Comment>>();
		EventParameters p=new EventParameters();
		p.setMinDate(System.currentTimeMillis() - MIN_COMMENT_AGE);
		p.setCount(NUM_USER_COMMENTS);

		// This doesn't work...
		// PagedItems<Comment> comments = digg.getUserComments(users, p);
		for(String u : users) {
			for(Comment c : getUserComments(u)) {
				Collection<Comment> col=newItems.get(c.getUser());
				if(col == null) {
					col=new ArrayList<Comment>();
					newItems.put(c.getUser(), col);
				}
				col.add(c);
			}
		}

		// Cache the newly found items. -- If the users comments worked...
		// for(Map.Entry<String, Collection<Comment>> me : newItems.entrySet()) {
		// 	mc.set(baseKey + me.getKey(), USER_COMMENTS_TIME, me.getValue());
		// }

		rv.putAll(newItems);
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
			assert remainingStories.size() <= 100
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
		Story story=getStory(c.getStoryId());
		int repId=c.getReplyId() == null ? c.getEventId() : c.getReplyId();
		String key="digg/comments/replies/" + c.getStoryId() + "."
			+ story.getComments() + "/" + repId;
		@SuppressWarnings("unchecked") // parameterized cast
		Collection<Comment> rv=(Collection<Comment>) mc.get(key);
		if(rv == null) {
			EventParameters ep=new EventParameters();
			ep.setCount(PagingParameters.MAX_COUNT);
			rv=digg.getCommentReplies(c.getStoryId(), repId, ep);
			mc.set(key, COMMENT_REPLY_TIME, rv);
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

	/**
	 * Get the recent comments for the given user.
	 */
	public Collection<Comment> getRelevantComments(String user)
		throws Exception {
		getLogger().info("Fetching comments for %s", user);
		Collection<Comment> rv=new TreeSet<Comment>(new CommentComparator());
		rv.addAll(getUserComments(user));
		// Get the stories cached
		getStoriesForComments(rv);
		// Now fetch related comments.
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

	/**
	 * Get the stories for the given domain.
	 */
	public Collection<Story> getStoriesForDomain(String domain)
		throws Exception {
		Collection<Story> rv=null;
		String key="digg/stories/domain/" + domain;
		@SuppressWarnings("unchecked") // generic cast
		Collection<Integer> sids=(Collection<Integer>)mc.get(key);
		if(sids == null) {
			sids=new HashSet<Integer>();
			StoryParameters sp=new StoryParameters();
			sp.setDomain(domain);
			sp.setCount(PagingParameters.MAX_COUNT);
			rv = digg.getStories(sp);
			for(Story s : rv) {
				mc.set(makeStoryKey(s.getId()), STORY_TIME, s);
				sids.add(s.getId());
			}
			mc.set(key, DOMAIN_TIME, sids);
		} else {
			rv=getStories(sids).values();
		}
		return rv;
	}

	/**
	 * Get comments for the given domain.
	 */
	public Collection<Comment> getCommentsForDomain(String domain)
		throws Exception {
		Collection<Integer> sids=new HashSet<Integer>();
		for(Story s : getStoriesForDomain(domain)) {
			sids.add(s.getId());
		}
		String key="digg/comments/domain/" + domain;
		@SuppressWarnings("unchecked") // generic cast
		Collection<Comment> comments=(Collection<Comment>) mc.get(key);
		if(comments == null) {
			EventParameters ep=new EventParameters();
			ep.setCount(PagingParameters.MAX_COUNT);
			if(sids.isEmpty()) {
				comments=new ArrayList<Comment>();
			} else {
				comments = digg.getComments(sids, ep);
			}
			mc.set(key, DOMAIN_TIME, comments);
		}
		Collection<Comment> rv=new TreeSet<Comment>(new CommentComparator());
		rv.addAll(comments);
		return rv;
	}

	public Collection<Comment> getCommentsFromFriends(String user)
		throws Exception {
		String friendKey="digg/comments/friends/" + user;
		@SuppressWarnings("unchecked") // generic cast
		List<String> friends=(List<String>)mc.get(friendKey);
		if(friends == null) {
			UserParameters p=new UserParameters();
			p.setCount(PagingParameters.MAX_COUNT);
			PagedItems<User> friendObs = digg.getFriends(user, p);
			if(friendObs.getTotal() > friendObs.size()) {
				getLogger().warn("User %s has %d friends", user,
					friendObs.getTotal());
			}
			friends=new ArrayList<String>(friendObs.size());
			for(User u : friendObs) {
				friends.add(u.getName());
			}
			mc.set(friendKey, USER_FRIEND_TIME, friends);
		}
		assert friends.size() <= 100 : "Too many users.";
		List<Comment> rv=new ArrayList<Comment>();
		for(Map.Entry<String, Collection<Comment>> me
			: getUsersComments(friends).entrySet()) {
			rv.addAll(me.getValue());
		}
		Collections.sort(rv, new CommentComparator());
		getLogger().info("Found %d comments for friends of %s", rv.size(),
			user);
		if(rv.size() > NUM_USER_COMMENTS) {
			rv=rv.subList(0, NUM_USER_COMMENTS);
		}
		return rv;
	}

	static class CommentComparator implements Comparator<Comment> {
		public int compare(Comment o1, Comment o2) {
			return o2.getEventId() - o1.getEventId();
		}
	}
}
