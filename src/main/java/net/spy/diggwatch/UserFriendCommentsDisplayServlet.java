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

public class UserFriendCommentsDisplayServlet extends BaseDiggServlet {

	@Override
	protected void processPath(String u, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		req.setAttribute("username", u);
		Collection<Comment> comments = di.getCommentsFromFriends(u);
		if(comments.isEmpty()) {
			req.getRequestDispatcher("/nofcomments.jsp").forward(req, res);
		} else {
			Map<String, User> users = di.getCachedUsersForComments(comments);
			Map<Integer, Story> stories = di.getStoriesForComments(comments);
			List<StoryComment> sc=new ArrayList<StoryComment>(comments.size());
			for(Comment c : comments) {
				// Skip broken stories.
				if(stories.containsKey(c.getStoryId())) {
					String icon = "/diggwatch/icon/" + c.getUser();
					User user = users.get(c.getUser());
					if(user != null) {
						icon = user.getIcon();
					}
					sc.add(new StoryComment(stories.get(c.getStoryId()),
						c, icon));
				}
			}
			req.setAttribute("storyComments", sc);

			req.getRequestDispatcher("/fcomments.jsp").forward(req, res);
		}
	}

	@Override
	protected String getEtag(String path) throws Exception {
		return getEtagFromEvents(di.getCommentsFromFriends(path));
	}

}
