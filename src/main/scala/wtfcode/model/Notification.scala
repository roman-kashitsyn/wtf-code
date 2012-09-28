package wtfcode.model

import net.liftweb.mapper._
import wtfcode.util.MentionsExtractor
import net.liftweb.common.{Full, Box}

class Notification extends LongKeyedMapper[Notification] with IdPK with CreatedTrait {
  def getSingleton = Notification

  override val createdAtIndexed_? = true

  object user extends MappedLongForeignKey(this, User) {
    override def dbIndexed_? = true
  }

  object preview extends MappedText(this)
  object from extends MappedLongForeignKey(this, User)
  object read extends MappedBoolean(this)
  object link extends MappedText(this)
}

object Notification extends Notification with LongKeyedMetaMapper[Notification] {
  override def dbTableName = "notifications"

  def newComment(newComment: Comment) {

    def notify(maybeUser: Box[User]) {
      maybeUser.map { user =>
        if (newComment.author != user) //idiotic Box.equals is not symmetric!
          Notification
            .create
            .user(user)
            .preview(buildPreview(newComment))
            .from(newComment.author)
            .link(newComment.link)
            .save()
      }
    }

    def buildPreview(comment: Comment) = {
      val content = comment.content.get
      if (content.length > 80)
        content.slice(0, 79) + "…"
      else
        content
    }

    //new comment to post
    newComment.post.map { post =>
      notify(post.author)
    }

    //new response to comment
    newComment.responseTo.map { to =>
      notify(to.author)
    }

    //mentions in new comment
    new MentionsExtractor(newComment.content).mentions.foreach { user =>
      notify(Full(user))
    }
  }

  def deletedComment(comment: Comment) {
    comment.author.map { author =>
      Notification.create.user(author).from(User.currentUser).link(comment.link).save()
    }
  }

  def deletedPost(post: Post) {
    post.author.map { author =>
      Notification.create.user(author).from(User.currentUser).link(post.link).save()
    }
  }
}