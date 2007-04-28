package net.spy.diggwatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import net.spy.digg.Comment;
import net.spy.digg.Story;
import net.spy.jwebkit.rss.RSSChannel;
import net.spy.jwebkit.rss.RSSItem;

public class CommentFeed extends RSSChannel {

	private static final String BASE_URL
		= "http://bleu.west.spy.net/diggwatch/";
	private String user=null;
	private Collection<Comment> comments=null;

	public CommentFeed(String u, Collection<Comment> c) {
		super(u + " @ digg", BASE_URL + "comments/" + u,
			u + "'s comments (and replies) on digg");
		user=u;
		comments = c;
	}

	@Override
	protected Collection<? extends RSSItem> getItems() {
		Collection<CommentAdaptor> rv=new ArrayList<CommentAdaptor>(
			comments.size());
		DiggInterface di=DiggInterface.getInstance();
		for(Comment c : comments) {
			try {
				rv.add(new CommentAdaptor(user, di.getStory(c.getStoryId()),
						c));
			} catch (Exception e) {
				throw new RuntimeException("Error making feed", e);
			}
		}
		return rv;
	}

	static class CommentAdaptor implements RSSItem {

		private Story story=null;
		private Comment comment=null;
		private String user=null;

		public CommentAdaptor(String u, Story s, Comment c) {
			super();
			story=s;
			comment=c;
			user=u;
		}

		public String getDescription() {
			return comment.getComment();
		}

		public String getGuid() {
			return getLink();
		}

		public String getLink() {
			return story.getDiggLink() + "#c" + comment.getEventId();
		}

		public Date getPubDate() {
			return new Date(comment.getTimestamp());
		}

		public String getTitle() {
			String rv=null;
			if(user.equals(comment.getUser())) {
				rv="your comment on ``" + story.getTitle() + "'' (+"
					+ comment.getDiggsUp() + "/-" + comment.getDiggsDown()
					+ ")";
			} else {
				rv="possible reply to comment on ``"
					+ story.getTitle() + "'' by " + comment.getUser() + "(+"
					+ comment.getDiggsUp() + "/-" + comment.getDiggsDown()
					+ ")";
			}
			return rv;
		}
		
	}
}
