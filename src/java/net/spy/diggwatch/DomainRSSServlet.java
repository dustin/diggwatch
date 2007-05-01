package net.spy.diggwatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.digg.Story;
import net.spy.jwebkit.rss.RSSChannel;
import net.spy.jwebkit.rss.RSSItem;

public class DomainRSSServlet extends BaseDiggServlet {

	@Override
	protected void processPath(String path, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		DiggInterface di=DiggInterface.getInstance();
		Collection<Story> stories = di.getStoriesForDomain(path);
		sendXml(new DomainCommentFeed(path,
			"Stories linking to " + path + " on digg", stories), res);
	}


	private static class DomainCommentFeed extends RSSChannel {
		private Collection<Story> stories=null;
		public DomainCommentFeed(String path, String descr,
				Collection<Story> s) {
			super(path + " @ digg", CommentFeed.BASE_URL, descr);
			stories=s;
		}
		@Override
		protected Collection<? extends RSSItem> getItems() {
			Collection<RSSItem> rv=new ArrayList<RSSItem>();
			for(Story s : stories) {
				rv.add(new StoryAdaptor(s));
			}
			return rv;
		}
	}


	private static class StoryAdaptor implements RSSItem {
		private Story story=null;
		public StoryAdaptor(Story s) {
			super();
			story=s;
		}

		public String getDescription() {
			return story.getDescription();
		}
		public String getGuid() {
			return story.getDiggLink();
		}
		public String getLink() {
			return story.getDiggLink();
		}
		public Date getPubDate() {
			return new Date(story.getSubmittedTimestamp());
		}
		public String getTitle() {
			return story.getTitle();
		}
	}
}
