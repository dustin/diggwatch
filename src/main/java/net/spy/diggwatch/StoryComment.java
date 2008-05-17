/**
 *
 */
package net.spy.diggwatch;

import net.spy.digg.Comment;
import net.spy.digg.Story;

/**
 * A comment with a reference to a story.
 */
public class StoryComment {
	private Story story=null;
	private Comment comment=null;
	private final String iconUrl;
	public StoryComment(Story s, Comment c, String url) {
		super();
		assert s != null;
		assert c != null;
		assert url != null;
		story = s;
		comment = c;
		iconUrl = url;
	}
	public Comment getComment() {
		return comment;
	}
	public Story getStory() {
		return story;
	}
	public String getIconUrl() {
		return iconUrl;
	}
	public String getFormattedComment() {
		return comment.getComment().replace("\n", "<br/>\n");
	}
	public String getParentLink() {
		int repId=comment.getReplyId() == null
			? comment.getEventId() : comment.getReplyId();
		return story.getDiggLink() + "/?t=" + comment.getRoot()
			+ "#c" + repId;
	}
	public String getCommentLink() {
		return story.getDiggLink() + "/?t=" + comment.getRoot()
			+ "#c" + comment.getEventId();
	}
}