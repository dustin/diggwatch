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