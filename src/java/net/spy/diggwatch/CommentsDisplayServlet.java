package net.spy.diggwatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.digg.Comment;
import net.spy.digg.Story;

/**
 * Display comments for the given user.
 */
public class CommentsDisplayServlet extends BaseDiggServlet {

	@Override
	protected void processPath(String u, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		req.setAttribute("username", u);
		Collection<Comment> comments =
			DiggInterface.getInstance().getRelevantComments(u);
		if(comments.isEmpty()) {
			req.getRequestDispatcher("/nocomments.jsp").forward(req, res);
		} else {
			DiggInterface di=DiggInterface.getInstance();
			Map<Integer, Story> stories = di.getStoriesForComments(comments);
			List<StoryComment> sc=new ArrayList<StoryComment>(comments.size());
			for(Comment c : comments) {
				// Skip broken stories.
				if(stories.containsKey(c.getStoryId())) {
					sc.add(new StoryComment(stories.get(c.getStoryId()), c,
						c.getUser().toLowerCase().equals(u.toLowerCase())));
				}
			}
			req.setAttribute("storyComments", sc);
			Collections.reverse(sc);

			req.getRequestDispatcher("/comments.jsp").forward(req, res);
		}
	}
}
