package net.spy.diggwatch;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.spy.SpyObject;
import net.spy.digg.Comment;
import net.spy.digg.Digg;
import net.spy.digg.EventParameters;
import net.spy.digg.PagedItems;
import net.spy.digg.PagingParameters;
import net.spy.digg.User;
import net.spy.util.RingBuffer;

/**
 * Update users and stories.
 */
public class Updater extends SpyObject implements Runnable {

	private static final String APP_KEY="http://bleu.west.spy.net/diggwatch/";
	private static final int MAX_BATCH_SIZE=100;
	private static final int RING_BUFFER_SIZE=10;

	Digg d=new Digg(APP_KEY);

	// Username -> user
	ConcurrentMap<String, User> users=new ConcurrentHashMap<String, User>();
	// Username -> recent comments by that user
	ConcurrentMap<String, Collection<Comment>> userComments=
		new ConcurrentHashMap<String, Collection<Comment>>();
	// Story id -> root comments being tracked
	ConcurrentMap<Integer, Set<Integer>> stories=
		new ConcurrentHashMap<Integer, Set<Integer>>();
	// reply id -> recent comments
	ConcurrentMap<Integer, TrackedItem<Collection<Comment>>> replies=
		new ConcurrentHashMap<Integer, TrackedItem<Collection<Comment>>>();

	private long lastUserUpdate=-1;
	private long lastArticleUpdate=-1;

	public void run() {
		try {
			lastUserUpdate=doUpdates(users.keySet(), MAX_BATCH_SIZE,
					lastUserUpdate, new UserUpdater());
			lastArticleUpdate=doUpdates(stories.keySet(), 25,
					lastArticleUpdate, new ArticleUpdater());
		} catch(Exception e) {
			getLogger().warn("Problem updating stuff", e);
		}
	}

	// Update a collection of things and return the lowest date from all
	// batches
	private <T> long doUpdates(Collection<T> c, int batchSize,
			long lastUpdate, TypeUpdater<T> u)
		throws Exception {
		Collection<T> batch=new HashSet<T>();
		long nextTs=lastUpdate > 0 ? lastUpdate : Long.MAX_VALUE;
		for(T i : c) {
			batch.add(i);
			if(batch.size() == batchSize) {
				long ts=u.doUpdate(batch, lastUserUpdate);
				nextTs=Math.min(ts, nextTs);
				batch.clear();
			}
		}
		if(!batch.isEmpty()) {
			long ts=u.doUpdate(batch, lastUserUpdate);
			nextTs=Math.min(ts, nextTs);
		}
		return nextTs;
	}

	private static interface TypeUpdater<T> {
		long doUpdate(Collection<T> keys, long lastUpdate) throws Exception;
	}

	class UserUpdater implements TypeUpdater<String> {
		public long doUpdate(Collection<String> usernames, long lastUpdate)
			throws Exception {
			EventParameters ep=new EventParameters();
			ep.setCount(PagingParameters.MAX_COUNT);
			if(lastUpdate > 0) {
				ep.setMinDate(lastUpdate);
			}
			PagedItems<Comment> comments = d.getUserComments(usernames, ep);
			for(Comment c : comments) {
				Collection<Comment> uc=userComments.get(c.getUser());
				if(uc == null) {
					uc=new RingBuffer<Comment>(RING_BUFFER_SIZE);
					userComments.put(c.getUser(), uc);
				}
				uc.add(c);

				Set<Integer> storyComments=stories.get(c.getStoryId());
				int rid=c.getReplyId() == null
					? c.getEventId() : c.getReplyId();
				if(storyComments == null) {
					Set<Integer> tmp=stories.putIfAbsent(
							c.getStoryId(), new HashSet<Integer>());
					storyComments=tmp == null ? stories.get(c.getStoryId())
							: tmp;
					replies.putIfAbsent(rid,
						new TrackedItem<Collection<Comment>>(
								new RingBuffer<Comment>(RING_BUFFER_SIZE)));
				}
				assert storyComments != null : "Didn't get story comments";
				storyComments.add(rid);
			}
			return comments.getTimestamp();
		}
	}

	class ArticleUpdater implements TypeUpdater<Integer> {

		public long doUpdate(Collection<Integer> keys, long lastUpdate)
			throws Exception {
			Set<Integer> watchingComments=new HashSet<Integer>();
			for(int k : keys) {
				Set<Integer> cids = stories.get(k);
				assert cids != null : "No comments for story " + k;
				watchingComments.addAll(cids);
			}
			EventParameters p=new EventParameters();
			if(lastUpdate > 0) {
				p.setMinDate(lastUpdate);
			}
			PagedItems<Comment> comments = d.getComments(keys, p);
			for(Comment c : comments) {
				Integer repId=c.getReplyId();
				if(repId != null && watchingComments.contains(repId)) {
					TrackedItem<Collection<Comment>> tci=replies.get(repId);
					assert tci != null;
					tci.getItem().add(c);
				}
			}
			return comments.getTimestamp();
		}
		
	}
}
