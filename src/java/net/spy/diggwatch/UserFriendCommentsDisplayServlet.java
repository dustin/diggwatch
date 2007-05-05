package net.spy.diggwatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.digg.Comment;
import net.spy.digg.Story;

public class UserFriendCommentsDisplayServlet extends BaseDiggServlet {

	@Override
	protected void processPath(String u, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		req.setAttribute("username", u);
		Collection<Comment> comments =
			DiggInterface.getInstance().getCommentsFromFriends(u);
		if(comments.isEmpty()) {
			req.getRequestDispatcher("/nofcomments.jsp").forward(req, res);
		} else {
			DiggInterface di=DiggInterface.getInstance();
			Map<Integer, Story> stories = di.getStoriesForComments(comments);
			List<StoryComment> sc=new ArrayList<StoryComment>(comments.size());
			for(Comment c : comments) {
				// Skip broken stories.
				if(stories.containsKey(c.getStoryId())) {
					sc.add(new StoryComment(stories.get(c.getStoryId()), c));
				}
			}
			req.setAttribute("storyComments", sc);

			req.getRequestDispatcher("/fcomments.jsp").forward(req, res);
		}
	}

}
