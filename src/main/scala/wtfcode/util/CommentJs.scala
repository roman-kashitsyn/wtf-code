package wtfcode.util

import wtfcode.model.Comment
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.jquery.JqJsCmds.{AppendHtml, FadeOut}
import net.liftweb.common.Empty

/**
 * Commands to work with comments on client side.
 *
 * @author Kashitsyn Roman
 */
object CommentJs {
  val NoticesContainer = "comment-notices"

  def notify(comment: Comment): JsCmd = {
    val link = "#" + comment.anchor
    val noticeId = "notice-" + comment.id.is

    AppendHtml(NoticesContainer,
      <div class="media" id={noticeId} onclick={"location.href='" + link + "';"}>
        <a class="pull-left" href={link}>
          <img class="media-object avatar" src={Avatar.apply(comment.author, Empty)} alt="avatar"></img>
        </a>
        <h4 class="media-heading">{comment.author.obj.map(_.nickName).getOrElse("Guest")}</h4>
        <div class="media-body">{WtfBbParser.toHtml(comment.content)}</div>
      </div>) &
      FadeOut(noticeId)
  }
}
