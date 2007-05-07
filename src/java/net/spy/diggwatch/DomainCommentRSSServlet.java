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

/**
 * Display comments RSS-style for the domain (from path).
 */
public class DomainCommentRSSServlet extends BaseDiggServlet {

	@Override
	protected void processPath(String path,
		HttpServletRequest req, HttpServletResponse res) throws Exception {
		List<Comment> comments = new ArrayList<Comment>(
			di.getCommentsForDomain(path));
		sendXml(new DomainCommentFeed(path,
				"Comments on articles from " + path, comments), res);
	}

	public static class DomainCommentFeed extends CommentFeed {
		public DomainCommentFeed(String p, String title,
				Collection<Comment> c) {
			super(p, title, c);
		}
		@Override
		protected RSSItem adaptComment(Story s, Comment c) {
			return new DomainCommentAdaptor(path, s, c);
		}
	}

	public static class DomainCommentAdaptor extends CommentAdaptor {

		public DomainCommentAdaptor(String domain, Story s, Comment c) {
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
