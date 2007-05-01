package net.spy.diggwatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.digg.Comment;
import net.spy.digg.Story;

/**
 * Display comments for the given domain.
 */
public class DomainCommentsDisplayServlet extends BaseDiggServlet {

	@Override
	protected void processPath(String path, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		req.setAttribute("domain", path);
		DiggInterface di=DiggInterface.getInstance();
		Collection<Comment> comments=di.getCommentsForDomain(path);
		if(comments.isEmpty()) {
			req.getRequestDispatcher("/nodcomments.jsp").forward(req, res);
		} else {
			Map<Integer, Story> stories = di.getStoriesForComments(comments);
			List<StoryComment> sc=new ArrayList<StoryComment>(comments.size());
			for(Comment c : comments) {
				// Skip broken stories.
				if(stories.containsKey(c.getStoryId())) {
					sc.add(new StoryComment(stories.get(c.getStoryId()), c));
				}
			}
			req.setAttribute("storyComments", sc);
			req.getRequestDispatcher("/dcomments.jsp").forward(req, res);
		}
	}

}
