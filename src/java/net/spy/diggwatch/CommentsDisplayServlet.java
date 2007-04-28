package net.spy.diggwatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.digg.Comment;
import net.spy.digg.Story;
import net.spy.jwebkit.JWHttpServlet;

/**
 * Display comments for the given user.
 */
public class CommentsDisplayServlet extends JWHttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		String pi=req.getPathInfo();
		if(pi == null || pi.length() == 0 || pi.equals("/")) {
			// some sort of index thing?
			throw new ServletException("No username.");
		} else {
			assert pi.startsWith("/");
			String u=pi.substring(1);
			try {
				processUser(u, req, res);
			} catch(Exception e) {
				throw new ServletException("Error processing user " + u, e);
			}
		}
	}

	private void processUser(String u, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		req.setAttribute("username", u);
		Collection<Comment> comments = new CommentFetcher().getComments(u);
		if(comments.isEmpty()) {
			req.getRequestDispatcher("/nocomments.jsp").forward(req, res);
		} else {
			req.setAttribute("comments", comments);
			Map<Integer, Story> stories=new HashMap<Integer, Story>();
			DiggInterface di=DiggInterface.getInstance();
			for(Comment c : comments) {
				if(!stories.containsKey(c.getStoryId())) {
					stories.put(c.getStoryId(), di.getStory(c.getStoryId()));
				}
			}
			req.setAttribute("stories", stories);
			List<StoryComment> sc=new ArrayList<StoryComment>(comments.size());
			for(Comment c : comments) {
				sc.add(new StoryComment(stories.get(c.getStoryId()), c));
			}
			req.setAttribute("storyComments", sc);
			Collections.reverse(sc);

			req.getRequestDispatcher("/comments.jsp").forward(req, res);
		}
	}

	public static class StoryComment {
		private Story story=null;
		private Comment comment=null;
		public StoryComment(Story s, Comment c) {
			super();
			assert s != null;
			assert c != null;
			story = s;
			comment = c;
			story.getDiggLink();
		}
		public Comment getComment() {
			return comment;
		}
		public Story getStory() {
			return story;
		}
		public String getFormattedComment() {
			return comment.getComment().replace("\n", "<br/>\n");
		}
		public String getParentLink() {
			int repId=comment.getReplyId() == null
				? comment.getEventId() : comment.getReplyId();
			return story.getDiggLink() + "#c" + repId;
		}
		public String getCommentLink() {
			return story.getDiggLink() + "#c" + comment.getEventId();
		}
	}
}
