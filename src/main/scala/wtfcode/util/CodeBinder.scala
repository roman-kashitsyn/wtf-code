package wtfcode.util

import xml.NodeSeq
import wtfcode.model.{LastSeen, Bookmark, User, Post}
import net.liftweb.util.Helpers._
import net.liftweb.textile.TextileParser
import net.liftweb.http.{S, SHtml}
import net.liftweb.mapper.By
import net.liftweb.http.js.jquery.JqJsCmds.Hide
import net.liftweb.common.Full

object CodeBinder {

  def apply(post: Post): (NodeSeq => NodeSeq) = {
    val langObj = post.language.obj
    val ratingTemplate = S.runTemplate(List("templates-hidden", "rating")).open_!
    ".entry-id *" #> post.id &
      ".language *" #> post.getLanguage &
      ".content *" #> post.content &
      ".description *" #> TextileParser.toHtml(post.description) &
      ".link_to_author *" #> post.author.map(_.nickName.get).openOr("Guest") &
      ".date *" #> post.createdAt &
      ".commentsNum *" #> post.comments.size &
      ".newCommentsNum *" #> LastSeen.unseenCount(User.currentUser, Full(post)) &
      ".bookmark *" #> bookmarkAction(post) &
      ".post-rating *" #> RateBinder(post)(ratingTemplate) &
      ".link_to_author [href]" #> post.author.map {_.link}.openOr("#") &
      ".link_to_code [href]" #> post.link &
      ".link_to_lang_filter [href]" #> langObj.map {_.link}.openOr("#") &
      ".lang_code [href]" #> langObj.map {_.code.is}.openOr("")
  }

  private def mkAddBookmarkItem(id: String) = <i class="icon-star-empty" id={id} title={S ? "bookmark.add"}></i>
  private def mkRemBookmarkItem(id: String) = <i class="icon-star" id={id} title={S ? "bookmark.remove"}></i>

  private def bookmarkAction(post: Post) = {
    val id = "bookmark_" + post.id

    User.currentUser map { user =>
      val maybeBookmark = Bookmark.find(By(Bookmark.user, user), By(Bookmark.post, post))
      maybeBookmark match {
        case Full(bookmark) => SHtml.a(() => {
          bookmark.delete_!
          Hide(id)
        }, mkRemBookmarkItem(id))
        case _ => SHtml.a(() => {
          Bookmark.create.user(user).post(post).save()
          Hide(id)
        }, mkAddBookmarkItem(id))
      }
    }
  }
}
