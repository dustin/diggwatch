package net.spy.diggwatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.digg.Comment;
import net.spy.digg.Story;
import net.spy.diggwatch.CommentFeed.CommentAdaptor;
import net.spy.jwebkit.rss.RSSItem;

public class UserFriendCommentRSSServlet extends BaseDiggServlet {

	@Override
	protected void processPath(String user, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
			List<Comment> comments = new ArrayList<Comment>(
				di.getCommentsFromFriends(user));
			sendXml(new FriendCommentFeed("friends of " + user,
					"Comments from friends of " + user, comments), res);
		}

		public static class FriendCommentFeed extends CommentFeed {
			public FriendCommentFeed(String p, String title,
					Collection<Comment> c) {
				super(p, title, c);
			}
			@Override
			protected RSSItem adaptComment(Story s, Comment c) {
				return new FriendCommentAdaptor(path, s, c);
			}
		}

		public static class FriendCommentAdaptor extends CommentAdaptor {

			public FriendCommentAdaptor(String domain, Story s, Comment c) {
				super(domain, s, c);
			}

			@Override
			public String getTitle() {
				String rv="Comment on ``"
					+ story.getTitle() + "'' by " + comment.getUser() + " (+"
					+ comment.getDiggsUp() + "/-" + comment.getDiggsDown()
					+ ")";
				return rv;
			}
			
		}
}
