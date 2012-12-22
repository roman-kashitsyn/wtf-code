package wtfcode.util

import wtfcode.model.Comment
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.jquery.JqJsCmds.FadeOut

/**
 * Commands to work with comments on client side.
 *
 * @author Kashitsyn Roman
 */
object CommentJs {
  val NoticesContainer = "comment-notices"

  def notify(comment: Comment): JsCmd =
    SetHtml(NoticesContainer,
      <div onclick={"location.href='#" + comment.anchor + "';"}>
        <b>{comment.author.obj.map(_.nickName).getOrElse("Guest")}</b>
        <p>{WtfBbParser.toHtml(comment.content)}</p>
      </div>) &
    JsShowId(NoticesContainer) &
      FadeOut(NoticesContainer)
}