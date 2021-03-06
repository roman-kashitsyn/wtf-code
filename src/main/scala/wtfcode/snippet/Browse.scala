package wtfcode.snippet

import xml.NodeSeq
import wtfcode.model.Post
import net.liftweb.util.Helpers._
import net.liftweb.mapper._
import wtfcode.util.AjaxCommentsCodeBinder
import net.liftweb.mapper.StartAt
import net.liftweb.mapper.MaxRows

class Browse extends BootstrapPaginatorSnippet[Post] {
  override def itemsPerPage = 20
  override def count = Post.count
  override def page = Post.findAll(
    By(Post.deleted, false),
    By_>=(Post.rating, Post.MinRating),
    OrderBy(Post.createdAt, Descending),
    StartAt(curPage * itemsPerPage),
    MaxRows(itemsPerPage))

  def renderPage() =
    ".posts *" #> ((in: NodeSeq) => page.flatMap { code => AjaxCommentsCodeBinder(code)(in) })
}

