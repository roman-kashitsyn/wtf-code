package wtfcode.comet

import net.liftweb.http.{CometListener, RenderOut, CometActor}
import wtfcode.model.{User, Comment}
import wtfcode.snippet.ViewPost
import wtfcode.util.CommentJs
import net.liftweb.util.Helpers.asLong
import xml.NodeSeq

/**
 * Actor that sends new messages to users who
 * are looking a post and (possibly) writing response.
 *
 * @author Roman Kashitsyn
 */

class CommentActor extends CometActor with CometListener {

  def registerWith = CommentServer

  private lazy val postId: Long = name.map { s =>
    val i = s.lastIndexOf("-")
    asLong(s.substring(i + 1)).getOrElse(0L)
  }.getOrElse(0L)

  def render: RenderOut = new RenderOut(NodeSeq.Empty)

  override def lowPriority = {
    case List(comment: Comment) => {
      User.currentUser.map(user =>
        if (comment.author != user && comment.post.obj.get.id == postId) {
          partialUpdate(ViewPost.appendCommentHtml(comment) & CommentJs.notify(comment))
        })
    }
  }
}

