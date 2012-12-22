package wtfcode.snippet

import xml.NodeSeq
import wtfcode.model._
import net.liftweb.util.Helpers
import Helpers._
import net.liftweb.http.{ SHtml, S }
import net.liftweb.common.{Full, Empty}
import wtfcode.util._
import net.liftweb.http.js.jquery.JqJsCmds.AppendHtml
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import wtfcode.util.JqAddClass
import wtfcode.util.JqRemoveClass
import net.liftweb.http.js.jquery.JqJE.JqId
import xml.Text
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.jquery.JqJE.JqRemove
import net.liftweb.http.js.JE.Str
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.mapper.By
import wtfcode.comet.CommentServer

class ViewPost {
  val maybeId = asLong(S.param("id"))

  val post = maybeId match {
    case Full(id) => Post.findByKey(id)
    case _ => Empty
  }

  def view(in: NodeSeq): NodeSeq = {
    post map ({
      i => CodeBinder(i)(in)
    }) openOr Text("Not found")
  }

  def comments() = {
      post.map(p =>
        "#comments *" #>
          CommentBinders.applyToRoots(RecursiveCommentBinder, p)).openOr("#comments *" #> NodeSeq.Empty)
  }

  def updateUnseen() = {
    LastSeen.update(User.currentUser, post)
    NodeSeq.Empty
  }

  private val DeletePreviewCmd = (JqId("preview") ~> JqRemove()).cmd
  private val EnableAddCommentButton = JsRaw("""Comments.enableAddButton()""").cmd

  def addComment() = {
    post.isDefined match {
      case true => addCommentReal()
      case false => "#add-comment" #> NodeSeq.Empty
    }
  }



  private def addCommentReal() = {
    var content = ""

    def createComment(): Comment = {
      val parentId: Long = asLong(S.param("parentId")).openOr(0)
      val parent = Comment.find(By(Comment.id, parentId), By(Comment.post, post))
      Comment.create.author(User.currentUser).post(post).content(content).responseTo(parent)
    }

    def process(func: () => JsCmd): JsCmd = {
      clearErrors() &
        (content.trim.length match {
          case 0 => compilationError(S ? "comment.commentNotFound")
          case _ => {
            val errors = if (!User.loggedIn_?) ReCaptcha.validateCaptcha() else Nil
            if (errors.isEmpty) func() else compilationError (errors.map(_.msg).mkString("\n"))
          }
        })
    }



    def processAdd(): JsCmd = {
      val newComment = createComment().saveMe()
      Notification.newComment(newComment)

      CommentServer ! newComment

      DeletePreviewCmd &
        EnableAddCommentButton &
        JsHideId("add-comment") &
        JsRaw("Comments.clearTextarea()") &
        ViewPost.appendCommentHtml(newComment)

    }

    def processPreview(): JsCmd = {
      val newComment = createComment()

      DeletePreviewCmd &
        AppendHtml(ViewPost.appendToId(newComment), <li id="preview"/>) &
        SetHtml("preview", CommentBinder(newComment)(CommentBinders.CommentTemplate)) &
        SyntaxHighlighter.highlightBlock("preview")
    }

    def compilationError(s: String): JsCmd = {
      SetHtml("errors", Text(S ? "comment.compilationError" + ": " + s)) &
        (JqId("errors") ~> JqAddClass(Str("compile-error"))).cmd &
        (if (!User.loggedIn_?) ReCaptcha.reloadCaptcha() else Noop)
    }

    def clearErrors(): JsCmd = {
      SetHtml("errors", Text("")) &
        (JqId("errors") ~> JqRemoveClass(Str("compile-error"))).cmd
    }

    ".content" #> SHtml.textarea(content, content = _, "cols" -> "80", "rows" -> "8") &
      ".submit" #> SHtml.ajaxSubmit(S ? "comment.add", () => process(processAdd), "class" -> "btn btn-primary") &
      ".preview" #> SHtml.ajaxSubmit(S ? "comment.preview", () => process(processPreview), "class" -> "btn") &
      bindCaptcha()
  }

  def makeChat = <lift:comet type="CommentActor" name={"post-" + maybeId.getOrElse(0)}/>

  private def bindCaptcha() =
    if (User.currentUser.isDefined) ".reCaptcha" #> NodeSeq.Empty
    else ".reCaptcha *" #> ReCaptcha.captchaXhtml
}

object ViewPost {
  private def appendToId(comment: Comment) = comment.responseTo.map{ _.repliesAnchor }.openOr("comments")

  private [wtfcode] def appendCommentHtml(newComment: Comment): JsCmd =
    AppendHtml(appendToId(newComment), RecursiveCommentBinder(newComment)(CommentBinders.CommentTemplate)) &
      SyntaxHighlighter.highlightPage()
}
