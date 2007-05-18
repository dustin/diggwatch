package net.spy.diggwatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.digg.Comment;
import net.spy.digg.Story;
import net.spy.digg.User;

/**
 * Display comments for the given user.
 */
public class UserCommentsDisplayServlet extends BaseDiggServlet {

	@Override
	protected void processPath(String u, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		req.setAttribute("username", u);
		Collection<Comment> comments = di.getRelevantComments(u);
		if(comments.isEmpty()) {
			req.getRequestDispatcher("/nocomments.jsp").forward(req, res);
		} else {
			Map<String, User> users = di.getCachedUsersForComments(comments);
			Map<Integer, Story> stories = di.getStoriesForComments(comments);
			List<StoryComment> sc=new ArrayList<StoryComment>(comments.size());
			for(Comment c : comments) {
				// Skip broken stories.
				if(stories.containsKey(c.getStoryId())) {
					String icon="/diggwatch/icon/" + c.getUser();
					User user=users.get(c.getUser());
					if(user != null) {
						icon=user.getIcon();
					}
					sc.add(new UStoryComment(stories.get(c.getStoryId()), c,
						icon,
						c.getUser().toLowerCase().equals(u.toLowerCase())));
				}
			}
			req.setAttribute("storyComments", sc);

			req.getRequestDispatcher("/comments.jsp").forward(req, res);
		}
	}

	@Override
	protected String getEtag(String path) throws Exception {
		return getEtagFromEvents(di.getRelevantComments(path));
	}

	public static class UStoryComment extends StoryComment {
		private boolean isCurrentUser;

		public UStoryComment(Story s, Comment c, String icon,
				boolean currentUser) {
			super(s, c, icon);
			isCurrentUser=currentUser;
		}
		public boolean getIsCurrentUser() {
			return isCurrentUser;
		}
	}

}
