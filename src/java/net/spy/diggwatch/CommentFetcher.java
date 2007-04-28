package net.spy.diggwatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import net.spy.SpyObject;
import net.spy.digg.Comment;

/**
 * Fetch comments for a user.
 */
public class CommentFetcher extends SpyObject {

	/**
	 * Get the recent comments for the given user.
	 */
	public Collection<Comment> getComments(String user) throws Exception {
		getLogger().info("Fetching comments for %s", user);
		Collection<Comment> rv=new TreeSet<Comment>(new CommentComparator());
		DiggInterface di=DiggInterface.getInstance();
		rv.addAll(di.getUserComments(user));
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
